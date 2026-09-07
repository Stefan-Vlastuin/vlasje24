package nl.vlasje24.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Gebruikersnaam of e-mailadres is verplicht") String username,
        @NotBlank(message = "Wachtwoord is verplicht") String password
) {
    public LoginRequestDto {
        if (username != null) {
            username = username.trim();
        }
    }
}
