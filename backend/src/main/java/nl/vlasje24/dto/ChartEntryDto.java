package nl.vlasje24.dto;

public record ChartEntryDto(
        Integer position,
        Integer positionChange,
        Integer weeksInChart,
        SongDto song
) {}
