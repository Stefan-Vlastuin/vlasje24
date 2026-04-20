package nl.vlasje24.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateChartDto(LocalDate date, List<Integer> songIds) {
}
