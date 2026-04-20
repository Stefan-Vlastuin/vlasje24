package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.ChartEntry;
import nl.vlasje24.domain.Song;
import nl.vlasje24.dto.ArtistDto;
import nl.vlasje24.dto.ChartHistoryEntryDto;
import nl.vlasje24.dto.SongDetailDto;
import nl.vlasje24.dto.SongDto;
import nl.vlasje24.exception.NotFoundException;
import nl.vlasje24.repository.ChartEntryRepository;
import nl.vlasje24.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final ChartEntryRepository chartEntryRepository;

    @Transactional(readOnly = true)
    public SongDetailDto getSongDetail(Integer songId) {
        Song song = songRepository.findByIdWithArtists(songId)
                .orElseThrow(() -> new NotFoundException("Song not found: " + songId));

        List<ChartEntry> history = chartEntryRepository.findBySongIdWithChartWeek(songId);

        int weeksInChart = history.size();
        int highestPosition = history.stream()
                .mapToInt(ce -> ce.getId().getPosition())
                .min()
                .orElse(0);
        int totalPoints = history.stream()
                .mapToInt(ce -> 25 - ce.getId().getPosition())
                .sum();

        List<ChartHistoryEntryDto> chartHistory = history.stream()
                .map(ce -> new ChartHistoryEntryDto(
                        ce.getChartWeek().getWeekId(),
                        ce.getChartWeek().getDate(),
                        ce.getId().getPosition()
                ))
                .toList();

        List<ArtistDto> artists = song.getArtists().stream()
                .map(aos -> new ArtistDto(aos.getArtist().getArtistId(), aos.getArtist().getName()))
                .toList();

        return new SongDetailDto(
                song.getSongId(),
                song.getTitle(),
                song.getImageUrl(),
                song.getPreviewUrl(),
                artists,
                weeksInChart,
                highestPosition,
                totalPoints,
                chartHistory
        );
    }

    public SongDto toSongDto(Song song) {
        List<ArtistDto> artists = song.getArtists().stream()
                .map(aos -> new ArtistDto(aos.getArtist().getArtistId(), aos.getArtist().getName()))
                .toList();
        return new SongDto(song.getSongId(), song.getTitle(), song.getImageUrl(), song.getPreviewUrl(), artists);
    }
}
