package nl.vlasje24.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserSuspensionRequestDto(
        boolean suspended,
        @Size(max = 500, message = "Reden mag maximaal 500 tekens bevatten")
        String reason
) {
}
