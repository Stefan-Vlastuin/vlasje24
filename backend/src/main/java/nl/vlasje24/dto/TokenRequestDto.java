package nl.vlasje24.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenRequestDto(
        @NotBlank(message = "Token is verplicht")
        @Size(max = 128, message = "Token is ongeldig")
        String token
) {
}
