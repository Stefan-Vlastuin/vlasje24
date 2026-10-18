package nl.vlasje24.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateChartDto(
        @NotNull(message = "Datum is verplicht")
        LocalDate date,

        @NotNull(message = "Nummers zijn verplicht")
        @Size(min = 24, max = 24, message = "Een chart moet precies 24 nummers bevatten")
        List<@Positive(message = "Nummer-id moet positief zijn") Integer> songIds
) {
}
