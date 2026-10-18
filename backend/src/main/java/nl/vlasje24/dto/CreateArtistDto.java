package nl.vlasje24.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateArtistDto(
        @NotBlank(message = "Naam is verplicht")
        @Size(max = 255, message = "Naam mag maximaal 255 tekens bevatten")
        String name
) {
}
