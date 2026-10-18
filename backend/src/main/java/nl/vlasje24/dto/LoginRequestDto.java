package nl.vlasje24.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank(message = "Gebruikersnaam of e-mailadres is verplicht")
        @Size(max = 254, message = "Gebruikersnaam of e-mailadres is te lang")
        String username,

        @NotBlank(message = "Wachtwoord is verplicht")
        @Size(max = 72, message = "Wachtwoord is ongeldig")
        String password
) {
    public LoginRequestDto {
        if (username != null) {
            username = username.trim();
        }
    }
}
