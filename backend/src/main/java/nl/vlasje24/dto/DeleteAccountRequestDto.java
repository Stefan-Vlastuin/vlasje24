package nl.vlasje24.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteAccountRequestDto(
        @NotBlank(message = "Vul je wachtwoord in")
        @Size(max = 72, message = "Wachtwoord is ongeldig")
        String password
) {
}
