package nl.vlasje24.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "Gebruikersnaam is verplicht")
        @Pattern(regexp = "^[A-Za-z0-9_-]{3,30}$",
                message = "Gebruik 3 tot 30 letters, cijfers, underscores of streepjes")
        String username,

        @NotBlank(message = "E-mailadres is verplicht")
        @Email(message = "E-mailadres is ongeldig")
        @Size(max = 254, message = "E-mailadres mag maximaal 254 tekens bevatten")
        String email,

        @NotBlank(message = "Wachtwoord is verplicht")
        @Size(min = 12, message = "Wachtwoord moet minimaal 12 tekens bevatten")
        String password
) {
    public RegisterRequestDto {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim();
        }
    }
}
