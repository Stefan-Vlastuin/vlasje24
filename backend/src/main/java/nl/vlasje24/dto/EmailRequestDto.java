package nl.vlasje24.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailRequestDto(
        @NotBlank(message = "E-mailadres is verplicht")
        @Email(message = "E-mailadres is ongeldig")
        @Size(max = 254, message = "E-mailadres mag maximaal 254 tekens bevatten")
        String email
) {
    public EmailRequestDto {
        if (email != null) {
            email = email.trim();
        }
    }
}
