package nl.vlasje24.controller;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.ChartDto;
import nl.vlasje24.repository.ChartWeekRepository;
import nl.vlasje24.service.ChartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/charts")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;
    private final ChartWeekRepository chartWeekRepository;

    @GetMapping("/latest")
    public ChartDto getLatestChart() {
        return chartService.getLatestChart();
    }

    @GetMapping("/{weekId}")
    public ChartDto getChart(@PathVariable Integer weekId) {
        return chartService.getChart(weekId);
    }

    @GetMapping("/years")
    public List<Integer> getYears() {
        return chartWeekRepository.findDistinctYears();
    }
}
