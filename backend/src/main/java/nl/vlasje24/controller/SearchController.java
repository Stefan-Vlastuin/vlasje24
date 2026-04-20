package nl.vlasje24.controller;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.SearchResultDto;
import nl.vlasje24.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public SearchResultDto search(@RequestParam String q) {
        return searchService.search(q);
    }
}
