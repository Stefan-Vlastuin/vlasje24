package nl.vlasje24.dto;

import java.util.List;

public record ArtistDetailDto(
        Integer artistId,
        String name,
        Integer hitCount,
        Integer totalPoints,
        List<SongDetailDto> songs
) {}
