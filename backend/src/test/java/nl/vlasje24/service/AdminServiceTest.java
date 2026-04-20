package nl.vlasje24.service;

import nl.vlasje24.domain.*;
import nl.vlasje24.dto.CreateArtistDto;
import nl.vlasje24.dto.CreateChartDto;
import nl.vlasje24.dto.CreateSongDto;
import nl.vlasje24.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock ArtistRepository artistRepository;
    @Mock SongRepository songRepository;
    @Mock ArtistOfSongRepository artistOfSongRepository;
    @Mock ChartWeekRepository chartWeekRepository;
    @Mock ChartEntryRepository chartEntryRepository;

    @InjectMocks AdminService adminService;

    @Test
    void createArtist_savesArtistAndReturnsId() {
        Artist saved = mock(Artist.class);
        when(saved.getArtistId()).thenReturn(42);
        when(artistRepository.save(any())).thenReturn(saved);

        Integer id = adminService.createArtist(new CreateArtistDto("Adele"));

        assertThat(id).isEqualTo(42);
        ArgumentCaptor<Artist> captor = ArgumentCaptor.forClass(Artist.class);
        verify(artistRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Adele");
    }

    @Test
    void createSong_savesSongAndArtistsInOrder() {
        Song savedSong = mock(Song.class);
        when(savedSong.getSongId()).thenReturn(10);
        when(songRepository.save(any())).thenReturn(savedSong);

        Artist artist1 = mock(Artist.class);
        when(artist1.getArtistId()).thenReturn(1);
        Artist artist2 = mock(Artist.class);
        when(artist2.getArtistId()).thenReturn(2);
        when(artistRepository.findById(1)).thenReturn(Optional.of(artist1));
        when(artistRepository.findById(2)).thenReturn(Optional.of(artist2));
        when(artistOfSongRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Integer id = adminService.createSong(
                new CreateSongDto("Hello", "img.jpg", "preview.mp3", List.of(1, 2)));

        assertThat(id).isEqualTo(10);

        ArgumentCaptor<ArtistOfSong> captor = ArgumentCaptor.forClass(ArtistOfSong.class);
        verify(artistOfSongRepository, times(2)).save(captor.capture());
        List<ArtistOfSong> saved = captor.getAllValues();
        assertThat(saved.get(0).getArtistOrder()).isEqualTo((byte) 0);
        assertThat(saved.get(1).getArtistOrder()).isEqualTo((byte) 1);
    }

    @Test
    void createSong_artistNotFound_throwsException() {
        Song savedSong = mock(Song.class);
        when(songRepository.save(any())).thenReturn(savedSong);
        when(artistRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                adminService.createSong(new CreateSongDto("Song", null, null, List.of(99))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Artiest niet gevonden");
    }

    @Test
    void createChart_savesChartWeekAndEntriesWithCorrectPositions() {
        List<Integer> songIds = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24);

        ChartWeek savedWeek = mock(ChartWeek.class);
        when(savedWeek.getWeekId()).thenReturn(5);
        when(chartWeekRepository.save(any())).thenReturn(savedWeek);

        Song mockSong = mock(Song.class);
        when(songRepository.findById(anyInt())).thenReturn(Optional.of(mockSong));
        when(chartEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Integer id = adminService.createChart(new CreateChartDto(LocalDate.of(2024, 1, 1), songIds));

        assertThat(id).isEqualTo(5);

        ArgumentCaptor<ChartEntry> captor = ArgumentCaptor.forClass(ChartEntry.class);
        verify(chartEntryRepository, times(24)).save(captor.capture());
        List<ChartEntry> entries = captor.getAllValues();
        assertThat(entries.get(0).getId().getPosition()).isEqualTo(1);
        assertThat(entries.get(23).getId().getPosition()).isEqualTo(24);
    }

    @Test
    void createChart_wrongSongCount_throwsException() {
        List<Integer> tooFew = List.of(1, 2, 3);

        assertThatThrownBy(() ->
                adminService.createChart(new CreateChartDto(LocalDate.now(), tooFew)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("24");
    }

    @Test
    void createChart_songNotFound_throwsException() {
        List<Integer> songIds = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 999);

        ChartWeek savedWeek = mock(ChartWeek.class);
        when(savedWeek.getWeekId()).thenReturn(1);
        when(chartWeekRepository.save(any())).thenReturn(savedWeek);

        Song mockSong = mock(Song.class);
        when(songRepository.findById(intThat(id -> id != 999))).thenReturn(Optional.of(mockSong));
        when(songRepository.findById(999)).thenReturn(Optional.empty());
        when(chartEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() ->
                adminService.createChart(new CreateChartDto(LocalDate.now(), songIds)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nummer niet gevonden");
    }
}
