package nl.vlasje24.dto;

import java.util.List;

public record TopSongsPageDto(
        List<TopSongEntryDto> songs,
        boolean hasMore,
        int total
) {}
