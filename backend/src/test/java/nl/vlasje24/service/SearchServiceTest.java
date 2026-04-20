package nl.vlasje24.service;

import nl.vlasje24.domain.Artist;
import nl.vlasje24.domain.Song;
import nl.vlasje24.dto.ArtistDto;
import nl.vlasje24.dto.SearchResultDto;
import nl.vlasje24.dto.SongDto;
import nl.vlasje24.repository.ArtistRepository;
import nl.vlasje24.repository.SongRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock SongRepository songRepository;
    @Mock ArtistRepository artistRepository;
    @Mock SongService songService;

    @InjectMocks SearchService searchService;

    @Test
    void search_returnsSongsAndArtists() {
        Song song = mock(Song.class);
        when(songRepository.searchByTitle(eq("adele"), any())).thenReturn(List.of(song));
        when(songService.toSongDto(song)).thenReturn(new SongDto(1, "Hello", null, null, List.of()));

        Artist artist = mock(Artist.class);
        when(artist.getArtistId()).thenReturn(1);
        when(artist.getName()).thenReturn("Adele");
        when(artistRepository.findByNameContainingIgnoreCase(eq("adele"), any())).thenReturn(List.of(artist));

        SearchResultDto result = searchService.search("adele");

        assertThat(result.songs()).hasSize(1);
        assertThat(result.songs().get(0).title()).isEqualTo("Hello");
        assertThat(result.artists()).hasSize(1);
        assertThat(result.artists().get(0).name()).isEqualTo("Adele");
    }

    @Test
    void search_noMatches_returnsEmptyLists() {
        when(songRepository.searchByTitle(eq("xyz"), any())).thenReturn(List.of());
        when(artistRepository.findByNameContainingIgnoreCase(eq("xyz"), any())).thenReturn(List.of());

        SearchResultDto result = searchService.search("xyz");

        assertThat(result.songs()).isEmpty();
        assertThat(result.artists()).isEmpty();
    }

    @Test
    void search_mapsArtistFieldsCorrectly() {
        when(songRepository.searchByTitle(any(), any())).thenReturn(List.of());

        Artist artist = mock(Artist.class);
        when(artist.getArtistId()).thenReturn(7);
        when(artist.getName()).thenReturn("Dua Lipa");
        when(artistRepository.findByNameContainingIgnoreCase(any(), any())).thenReturn(List.of(artist));

        SearchResultDto result = searchService.search("dua");

        ArtistDto dto = result.artists().get(0);
        assertThat(dto.artistId()).isEqualTo(7);
        assertThat(dto.name()).isEqualTo("Dua Lipa");
    }
}
