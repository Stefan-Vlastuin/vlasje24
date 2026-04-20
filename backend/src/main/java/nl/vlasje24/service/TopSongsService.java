package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.Song;
import nl.vlasje24.dto.TopSongEntryDto;
import nl.vlasje24.dto.TopSongsPageDto;
import nl.vlasje24.repository.ChartEntryRepository;
import nl.vlasje24.repository.SongRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopSongsService {

    private final ChartEntryRepository chartEntryRepository;
    private final SongRepository songRepository;
    private final SongService songService;

    @Transactional(readOnly = true)
    public TopSongsPageDto getTopSongs(String sort, Integer year, int page, int size) {
        var pageable = PageRequest.of(page, size);

        List<Object[]> rows = switch (sort) {
            case "weeks"    -> chartEntryRepository.findTopByWeeks(year, pageable);
            case "position" -> chartEntryRepository.findTopByPosition(year, pageable);
            default         -> chartEntryRepository.findTopByPoints(year, pageable);
        };

        long total = chartEntryRepository.countDistinctSongs(year);
        boolean hasMore = (long) (page + 1) * size < total;

        List<Integer> songIds = rows.stream()
                .map(row -> ((Number) row[0]).intValue())
                .toList();

        Map<Integer, Song> songsById = songRepository.findAllById(songIds).stream()
                .collect(Collectors.toMap(Song::getSongId, s -> s));

        List<TopSongEntryDto> songs = rows.stream().map(row -> {
            int songId       = ((Number) row[0]).intValue();
            int points       = ((Number) row[1]).intValue();
            int weeks        = ((Number) row[2]).intValue();
            int highestPos   = ((Number) row[3]).intValue();
            return new TopSongEntryDto(songService.toSongDto(songsById.get(songId)), points, weeks, highestPos);
        }).toList();

        return new TopSongsPageDto(songs, hasMore, (int) total);
    }
}
