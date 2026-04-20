package nl.vlasje24.dto;

import java.util.List;

public record TopArtistsPageDto(
        List<TopArtistEntryDto> artists,
        boolean hasMore,
        int total
) {}
