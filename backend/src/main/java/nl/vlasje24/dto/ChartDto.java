package nl.vlasje24.dto;

import java.time.LocalDate;
import java.util.List;

public record ChartDto(
        Integer weekId,
        LocalDate date,
        Integer prevWeekId,
        Integer nextWeekId,
        List<ChartEntryDto> entries,
        List<SongDto> exitedSongs
) {}
