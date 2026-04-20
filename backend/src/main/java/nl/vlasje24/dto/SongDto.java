package nl.vlasje24.dto;

import java.util.List;

public record SongDto(
        Integer songId,
        String title,
        String imageUrl,
        String previewUrl,
        List<ArtistDto> artists
) {}
