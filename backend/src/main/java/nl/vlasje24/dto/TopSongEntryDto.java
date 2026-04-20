package nl.vlasje24.dto;

public record TopSongEntryDto(
        SongDto song,
        int points,
        int weeksInChart,
        int highestPosition
) {}
