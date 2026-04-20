package nl.vlasje24.dto;

import java.time.LocalDate;

public record ChartHistoryEntryDto(
        Integer weekId,
        LocalDate date,
        Integer position
) {}
