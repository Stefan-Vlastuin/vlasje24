package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.ArtistDto;
import nl.vlasje24.dto.SearchResultDto;
import nl.vlasje24.dto.SongDto;
import nl.vlasje24.repository.ArtistRepository;
import nl.vlasje24.repository.SongRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final SongService songService;

    @Transactional(readOnly = true)
    public SearchResultDto search(String query) {
        List<SongDto> songs = songRepository
                .searchByTitle(query, PageRequest.of(0, 5))
                .stream()
                .map(songService::toSongDto)
                .toList();

        List<ArtistDto> artists = artistRepository
                .findByNameContainingIgnoreCase(query, PageRequest.of(0, 5))
                .stream()
                .map(a -> new ArtistDto(a.getArtistId(), a.getName()))
                .toList();

        return new SearchResultDto(songs, artists);
    }
}
