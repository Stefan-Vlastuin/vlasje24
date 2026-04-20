package nl.vlasje24.dto;

import java.util.List;

public record CreateSongDto(String title, String imageUrl, String previewUrl, List<Integer> artistIds) {
}
