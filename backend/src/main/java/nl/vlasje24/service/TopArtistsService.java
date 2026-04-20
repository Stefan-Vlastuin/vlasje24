package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.Artist;
import nl.vlasje24.dto.ArtistDto;
import nl.vlasje24.dto.TopArtistEntryDto;
import nl.vlasje24.dto.TopArtistsPageDto;
import nl.vlasje24.repository.ArtistRepository;
import nl.vlasje24.repository.ChartEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopArtistsService {

    private final ChartEntryRepository chartEntryRepository;
    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public TopArtistsPageDto getTopArtists(String sort, Integer year, int page, int size) {
        var pageable = PageRequest.of(page, size);

        List<Object[]> rows = "hits".equals(sort)
                ? chartEntryRepository.findTopArtistsByHits(year, pageable)
                : chartEntryRepository.findTopArtistsByPoints(year, pageable);

        long total = chartEntryRepository.countDistinctArtists(year);
        boolean hasMore = (long) (page + 1) * size < total;

        List<Integer> artistIds = rows.stream()
                .map(row -> ((Number) row[0]).intValue())
                .toList();

        Map<Integer, Artist> artistsById = artistRepository.findAllById(artistIds).stream()
                .collect(Collectors.toMap(Artist::getArtistId, a -> a));

        List<TopArtistEntryDto> artists = rows.stream().map(row -> {
            int artistId = ((Number) row[0]).intValue();
            int points   = ((Number) row[1]).intValue();
            int hits     = ((Number) row[2]).intValue();
            Artist artist = artistsById.get(artistId);
            return new TopArtistEntryDto(new ArtistDto(artist.getArtistId(), artist.getName()), points, hits);
        }).toList();

        return new TopArtistsPageDto(artists, hasMore, (int) total);
    }
}
