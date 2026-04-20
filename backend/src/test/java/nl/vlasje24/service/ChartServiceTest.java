package nl.vlasje24.service;

import nl.vlasje24.domain.*;
import nl.vlasje24.dto.ChartDto;
import nl.vlasje24.dto.SongDto;
import nl.vlasje24.exception.NotFoundException;
import nl.vlasje24.repository.ChartEntryRepository;
import nl.vlasje24.repository.ChartWeekRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChartServiceTest {

    @Mock ChartWeekRepository chartWeekRepository;
    @Mock ChartEntryRepository chartEntryRepository;
    @Mock SongService songService;

    @InjectMocks ChartService chartService;

    @Test
    void getChart_weekNotFound_throwsNotFoundException() {
        when(chartWeekRepository.findByWeekIdWithEntries(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chartService.getChart(99))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getChart_positionImproved_positionChangeIsPositive() {
        // Song moved from position 5 (prev) to position 3 (current) → change = +2
        Song song = mockSong(1);
        ChartWeek currentWeek = mockWeekWithEntry(song, 3, LocalDate.of(2024, 1, 8));
        ChartWeek prevWeek = mockWeekWithEntry(song, 5, LocalDate.of(2024, 1, 1));

        ChartEntry historyEntry1 = mockChartEntry(song, 5, LocalDate.of(2024, 1, 1));
        ChartEntry historyEntry2 = mockChartEntry(song, 3, LocalDate.of(2024, 1, 8));

        when(chartWeekRepository.findByWeekIdWithEntries(2)).thenReturn(Optional.of(currentWeek));
        when(chartWeekRepository.findByWeekIdWithEntries(1)).thenReturn(Optional.of(prevWeek));
        when(chartWeekRepository.existsByWeekId(3)).thenReturn(false);
        when(chartEntryRepository.findBySongIdWithChartWeek(1))
                .thenReturn(List.of(historyEntry1, historyEntry2));
        when(songService.toSongDto(song)).thenReturn(new SongDto(1, "Test", null, null, List.of()));

        ChartDto result = chartService.getChart(2);

        assertThat(result.entries().get(0).positionChange()).isEqualTo(2);
    }

    @Test
    void getChart_positionWorsened_positionChangeIsNegative() {
        // Song moved from position 2 (prev) to position 7 (current) → change = -5
        Song song = mockSong(1);
        ChartWeek currentWeek = mockWeekWithEntry(song, 7, LocalDate.of(2024, 1, 8));
        ChartWeek prevWeek = mockWeekWithEntry(song, 2, LocalDate.of(2024, 1, 1));

        ChartEntry historyEntry1 = mockChartEntry(song, 2, LocalDate.of(2024, 1, 1));
        ChartEntry historyEntry2 = mockChartEntry(song, 7, LocalDate.of(2024, 1, 8));

        when(chartWeekRepository.findByWeekIdWithEntries(2)).thenReturn(Optional.of(currentWeek));
        when(chartWeekRepository.findByWeekIdWithEntries(1)).thenReturn(Optional.of(prevWeek));
        when(chartWeekRepository.existsByWeekId(3)).thenReturn(false);
        when(chartEntryRepository.findBySongIdWithChartWeek(1))
                .thenReturn(List.of(historyEntry1, historyEntry2));
        when(songService.toSongDto(song)).thenReturn(new SongDto(1, "Test", null, null, List.of()));

        ChartDto result = chartService.getChart(2);

        assertThat(result.entries().get(0).positionChange()).isEqualTo(-5);
    }

    @Test
    void getChart_newEntry_positionChangeIsNull() {
        Song song = mockSong(1);
        ChartWeek currentWeek = mockWeekWithEntry(song, 1, LocalDate.of(2024, 1, 8));

        ChartEntry historyEntry = mockChartEntry(song, 1, LocalDate.of(2024, 1, 8));

        when(chartWeekRepository.findByWeekIdWithEntries(2)).thenReturn(Optional.of(currentWeek));
        when(chartWeekRepository.findByWeekIdWithEntries(1)).thenReturn(Optional.empty());
        when(chartWeekRepository.existsByWeekId(3)).thenReturn(false);
        when(chartEntryRepository.findBySongIdWithChartWeek(1)).thenReturn(List.of(historyEntry));
        when(songService.toSongDto(song)).thenReturn(new SongDto(1, "New Song", null, null, List.of()));

        ChartDto result = chartService.getChart(2);

        assertThat(result.entries().get(0).positionChange()).isNull();
    }

    @Test
    void getChart_songDroppedOut_appearsInExitedSongs() {
        Song currentSong = mockSong(1);
        Song exitedSong = mockSong(2);

        ChartWeek currentWeek = mockWeekWithEntry(currentSong, 1, LocalDate.of(2024, 1, 8));
        ChartWeek prevWeek = mockWeekWithTwoEntries(currentSong, 2, exitedSong, 1, LocalDate.of(2024, 1, 1));

        when(chartWeekRepository.findByWeekIdWithEntries(2)).thenReturn(Optional.of(currentWeek));
        when(chartWeekRepository.findByWeekIdWithEntries(1)).thenReturn(Optional.of(prevWeek));
        when(chartWeekRepository.existsByWeekId(3)).thenReturn(false);
        when(chartEntryRepository.findBySongIdWithChartWeek(anyInt())).thenReturn(List.of());
        when(songService.toSongDto(currentSong)).thenReturn(new SongDto(1, "Staying", null, null, List.of()));
        when(songService.toSongDto(exitedSong)).thenReturn(new SongDto(2, "Exited", null, null, List.of()));

        ChartDto result = chartService.getChart(2);

        assertThat(result.exitedSongs()).hasSize(1);
        assertThat(result.exitedSongs().get(0).title()).isEqualTo("Exited");
    }

    @Test
    void getChart_hasNextWeek_nextWeekIdIsSet() {
        Song song = mockSong(1);
        ChartWeek currentWeek = mockWeekWithEntry(song, 1, LocalDate.of(2024, 1, 8));

        when(chartWeekRepository.findByWeekIdWithEntries(2)).thenReturn(Optional.of(currentWeek));
        when(chartWeekRepository.findByWeekIdWithEntries(1)).thenReturn(Optional.empty());
        when(chartWeekRepository.existsByWeekId(3)).thenReturn(true);
        when(chartEntryRepository.findBySongIdWithChartWeek(anyInt())).thenReturn(List.of());
        when(songService.toSongDto(any())).thenReturn(new SongDto(1, "Song", null, null, List.of()));

        ChartDto result = chartService.getChart(2);

        assertThat(result.nextWeekId()).isEqualTo(3);
        assertThat(result.prevWeekId()).isNull();
    }

    @Test
    void getLatestChart_noCharts_throwsNotFoundException() {
        when(chartWeekRepository.findTopByOrderByWeekIdDesc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chartService.getLatestChart())
                .isInstanceOf(NotFoundException.class);
    }

    // --- helpers ---

    private Song mockSong(int songId) {
        Song song = mock(Song.class);
        when(song.getSongId()).thenReturn(songId);
        return song;
    }

    private ChartWeek mockWeekWithEntry(Song song, int position, LocalDate date) {
        ChartEntry entry = mockChartEntry(song, position, date);
        ChartWeek week = mock(ChartWeek.class);
        when(week.getDate()).thenReturn(date);
        when(week.getEntries()).thenReturn(List.of(entry));
        return week;
    }

    private ChartWeek mockWeekWithTwoEntries(Song s1, int pos1, Song s2, int pos2, LocalDate date) {
        ChartEntry e1 = mockChartEntry(s1, pos1, date);
        ChartEntry e2 = mockChartEntry(s2, pos2, date);
        ChartWeek week = mock(ChartWeek.class);
        when(week.getDate()).thenReturn(date);
        when(week.getEntries()).thenReturn(List.of(e1, e2));
        return week;
    }

    private ChartEntry mockChartEntry(Song song, int position, LocalDate date) {
        ChartWeek week = mock(ChartWeek.class);
        when(week.getDate()).thenReturn(date);

        ChartEntryId id = mock(ChartEntryId.class);
        when(id.getPosition()).thenReturn(position);

        ChartEntry entry = mock(ChartEntry.class);
        when(entry.getSong()).thenReturn(song);
        when(entry.getId()).thenReturn(id);
        when(entry.getChartWeek()).thenReturn(week);
        return entry;
    }
}
