package nl.vlasje24.service;

import nl.vlasje24.domain.*;
import nl.vlasje24.dto.SongDetailDto;
import nl.vlasje24.dto.SongDto;
import nl.vlasje24.exception.NotFoundException;
import nl.vlasje24.repository.ChartEntryRepository;
import nl.vlasje24.repository.SongRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock SongRepository songRepository;
    @Mock ChartEntryRepository chartEntryRepository;

    @InjectMocks SongService songService;

    @Test
    void getSongDetail_songNotFound_throwsNotFoundException() {
        when(songRepository.findByIdWithArtists(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> songService.getSongDetail(99))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getSongDetail_calculatesStatsCorrectly() {
        Song song = mockSongWithArtist(1, "Hello", "Adele", 1);
        when(songRepository.findByIdWithArtists(1)).thenReturn(Optional.of(song));

        // Positions 1, 5, 10 → weeks=3, highest=1, points=(24+20+15)=59
        ChartEntry e1 = mockChartEntry(1, LocalDate.of(2024, 1, 1));
        ChartEntry e2 = mockChartEntry(5, LocalDate.of(2024, 1, 8));
        ChartEntry e3 = mockChartEntry(10, LocalDate.of(2024, 1, 15));
        when(chartEntryRepository.findBySongIdWithChartWeek(1)).thenReturn(List.of(e1, e2, e3));

        SongDetailDto result = songService.getSongDetail(1);

        assertThat(result.weeksInChart()).isEqualTo(3);
        assertThat(result.highestPosition()).isEqualTo(1);
        assertThat(result.totalPoints()).isEqualTo(59);
        assertThat(result.chartHistory()).hasSize(3);
    }

    @Test
    void getSongDetail_noChartHistory_returnsZeroStats() {
        Song song = mockSongWithArtist(1, "Obscure Song", "Unknown", 1);
        when(songRepository.findByIdWithArtists(1)).thenReturn(Optional.of(song));
        when(chartEntryRepository.findBySongIdWithChartWeek(1)).thenReturn(List.of());

        SongDetailDto result = songService.getSongDetail(1);

        assertThat(result.weeksInChart()).isEqualTo(0);
        assertThat(result.highestPosition()).isEqualTo(0);
        assertThat(result.totalPoints()).isEqualTo(0);
    }

    @Test
    void toSongDto_mapsAllFieldsCorrectly() {
        Song song = mockSongWithArtist(7, "Rolling in the Deep", "Adele", 5);

        SongDto dto = songService.toSongDto(song);

        assertThat(dto.songId()).isEqualTo(7);
        assertThat(dto.title()).isEqualTo("Rolling in the Deep");
        assertThat(dto.artists()).hasSize(1);
        assertThat(dto.artists().get(0).name()).isEqualTo("Adele");
        assertThat(dto.artists().get(0).artistId()).isEqualTo(5);
    }

    // --- helpers ---

    private Song mockSongWithArtist(int songId, String title, String artistName, int artistId) {
        Artist artist = mock(Artist.class);
        when(artist.getArtistId()).thenReturn(artistId);
        when(artist.getName()).thenReturn(artistName);

        ArtistOfSong aos = mock(ArtistOfSong.class);
        when(aos.getArtist()).thenReturn(artist);

        Song song = mock(Song.class);
        when(song.getSongId()).thenReturn(songId);
        when(song.getTitle()).thenReturn(title);
        when(song.getImageUrl()).thenReturn(null);
        when(song.getPreviewUrl()).thenReturn(null);
        when(song.getArtists()).thenReturn(List.of(aos));

        return song;
    }

    // Song is not accessed from ChartEntry in SongService — only position, weekId and date are used.
    private ChartEntry mockChartEntry(int position, LocalDate date) {
        ChartWeek week = mock(ChartWeek.class);
        when(week.getDate()).thenReturn(date);
        when(week.getWeekId()).thenReturn(position);

        ChartEntryId id = mock(ChartEntryId.class);
        when(id.getPosition()).thenReturn(position);

        ChartEntry entry = mock(ChartEntry.class);
        when(entry.getId()).thenReturn(id);
        when(entry.getChartWeek()).thenReturn(week);
        return entry;
    }
}
