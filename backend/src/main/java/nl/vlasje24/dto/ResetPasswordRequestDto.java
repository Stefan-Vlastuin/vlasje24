package nl.vlasje24.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank(message = "Token is verplicht")
        @Size(max = 128, message = "Token is ongeldig")
        String token,

        @NotBlank(message = "Wachtwoord is verplicht")
        @Size(min = 12, max = 72, message = "Wachtwoord moet 12 tot 72 tekens bevatten")
        String password
) {
}
