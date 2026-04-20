package nl.vlasje24.dto;

import java.util.List;

public record SearchResultDto(
        List<SongDto> songs,
        List<ArtistDto> artists
) {}
