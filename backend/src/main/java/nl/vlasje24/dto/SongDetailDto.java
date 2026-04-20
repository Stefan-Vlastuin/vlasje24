package nl.vlasje24.dto;

import java.util.List;

public record SongDetailDto(
        Integer songId,
        String title,
        String imageUrl,
        String previewUrl,
        List<ArtistDto> artists,
        Integer weeksInChart,
        Integer highestPosition,
        Integer totalPoints,
        List<ChartHistoryEntryDto> chartHistory
) {}
