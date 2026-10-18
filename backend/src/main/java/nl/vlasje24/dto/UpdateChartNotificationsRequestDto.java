package nl.vlasje24.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateChartNotificationsRequestDto(
        @NotNull(message = "Meldingsvoorkeur is verplicht") Boolean enabled
) {
}
