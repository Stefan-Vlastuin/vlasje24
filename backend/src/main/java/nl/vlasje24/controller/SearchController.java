package nl.vlasje24.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.SearchResultDto;
import nl.vlasje24.service.SearchService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public SearchResultDto search(
            @RequestParam
            @NotBlank(message = "Zoekterm is verplicht")
            @Size(min = 2, max = 100, message = "Zoekterm moet 2 tot 100 tekens bevatten") String q) {
        return searchService.search(q);
    }
}
