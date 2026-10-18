package nl.vlasje24.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.SongDetailDto;
import nl.vlasje24.dto.TopSongsPageDto;
import nl.vlasje24.service.SongService;
import nl.vlasje24.service.TopSongsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
@Validated
public class SongController {

    private final SongService songService;
    private final TopSongsService topSongsService;

    @GetMapping("/top")
    public TopSongsPageDto getTopSongs(
            @RequestParam(defaultValue = "points")
            @Pattern(regexp = "points|weeks|position", message = "Ongeldige sortering") String sort,
            @RequestParam(required = false) @Min(1900) @Max(2100) Integer year,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "24") @Min(1) @Max(50) int size
    ) {
        return topSongsService.getTopSongs(sort, year, page, size);
    }

    @GetMapping("/{songId}")
    public SongDetailDto getSong(@PathVariable Integer songId) {
        return songService.getSongDetail(songId);
    }
}
