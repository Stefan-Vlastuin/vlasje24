package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.ChartEntry;
import nl.vlasje24.domain.ChartWeek;
import nl.vlasje24.domain.Song;
import nl.vlasje24.dto.ChartDto;
import nl.vlasje24.dto.ChartEntryDto;
import nl.vlasje24.dto.SongDto;
import nl.vlasje24.exception.NotFoundException;
import nl.vlasje24.repository.ChartEntryRepository;
import nl.vlasje24.repository.ChartWeekRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final ChartWeekRepository chartWeekRepository;
    private final ChartEntryRepository chartEntryRepository;
    private final SongService songService;

    @Transactional(readOnly = true)
    public ChartDto getLatestChart() {
        ChartWeek latest = chartWeekRepository.findTopByOrderByWeekIdDesc()
                .orElseThrow(() -> new NotFoundException("No charts found"));
        return getChart(latest.getWeekId());
    }

    @Transactional(readOnly = true)
    public ChartDto getChart(Integer weekId) {
        ChartWeek chartWeek = chartWeekRepository.findByWeekIdWithEntries(weekId)
                .orElseThrow(() -> new NotFoundException("Chart not found for week: " + weekId));

        // Load previous week for position change calculation
        Map<Integer, Integer> prevPositions = new HashMap<>();
        Set<Integer> prevSongIds = new HashSet<>();
        Optional<ChartWeek> prevWeekOpt = chartWeekRepository.findByWeekIdWithEntries(weekId - 1);
        prevWeekOpt.ifPresent(prev -> {
            for (ChartEntry ce : prev.getEntries()) {
                prevPositions.put(ce.getSong().getSongId(), ce.getId().getPosition());
                prevSongIds.add(ce.getSong().getSongId());
            }
        });

        Set<Integer> currentSongIds = chartWeek.getEntries().stream()
                .map(ce -> ce.getSong().getSongId())
                .collect(Collectors.toSet());

        // Map entries to DTOs with position change and weeks in chart
        List<ChartEntryDto> entries = new ArrayList<>();
        for (ChartEntry ce : chartWeek.getEntries()) {
            Integer songId = ce.getSong().getSongId();
            Integer currentPosition = ce.getId().getPosition();
            Integer positionChange = null;
            if (prevPositions.containsKey(songId)) {
                positionChange = prevPositions.get(songId) - currentPosition;
            }
            int weeksInChart = chartEntryRepository.findBySongIdWithChartWeek(songId).stream()
                    .filter(e -> !e.getChartWeek().getDate().isAfter(chartWeek.getDate()))
                    .mapToInt(e -> 1)
                    .sum();

            SongDto songDto = songService.toSongDto(ce.getSong());
            entries.add(new ChartEntryDto(currentPosition, positionChange, weeksInChart, songDto));
        }

        // Songs that were in prev chart but not in current chart
        List<SongDto> exitedSongs = new ArrayList<>();
        prevWeekOpt.ifPresent(prev -> {
            for (ChartEntry ce : prev.getEntries()) {
                if (!currentSongIds.contains(ce.getSong().getSongId())) {
                    exitedSongs.add(songService.toSongDto(ce.getSong()));
                }
            }
        });

        Integer prevWeekId = prevWeekOpt.isPresent() ? weekId - 1 : null;
        Integer nextWeekId = chartWeekRepository.existsByWeekId(weekId + 1) ? weekId + 1 : null;

        return new ChartDto(weekId, chartWeek.getDate(), prevWeekId, nextWeekId, entries, exitedSongs);
    }
}
