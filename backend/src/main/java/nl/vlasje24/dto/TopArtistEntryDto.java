package nl.vlasje24.dto;

public record TopArtistEntryDto(
        ArtistDto artist,
        int points,
        int hitCount
) {}
