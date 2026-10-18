package nl.vlasje24.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSongDto(
        @NotBlank(message = "Titel is verplicht")
        @Size(max = 255, message = "Titel mag maximaal 255 tekens bevatten")
        String title,

        @Size(max = 500, message = "Afbeeldings-URL mag maximaal 500 tekens bevatten")
        String imageUrl,

        @Size(max = 500, message = "Preview-URL mag maximaal 500 tekens bevatten")
        String previewUrl,

        @NotEmpty(message = "Selecteer minimaal één artiest")
        @Size(max = 20, message = "Selecteer maximaal 20 artiesten")
        List<@Positive(message = "Artiest-id moet positief zijn") Integer> artistIds
) {
}
