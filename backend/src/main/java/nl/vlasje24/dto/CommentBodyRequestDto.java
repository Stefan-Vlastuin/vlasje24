package nl.vlasje24.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentBodyRequestDto(
        @NotBlank(message = "Reactie mag niet leeg zijn")
        @Size(max = 1000, message = "Reactie mag maximaal 1000 tekens bevatten")
        String body
) {
}
