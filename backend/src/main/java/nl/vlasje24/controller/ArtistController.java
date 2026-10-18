package nl.vlasje24.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.ArtistDetailDto;
import nl.vlasje24.dto.TopArtistsPageDto;
import nl.vlasje24.service.ArtistService;
import nl.vlasje24.service.TopArtistsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Validated
public class ArtistController {

    private final ArtistService artistService;
    private final TopArtistsService topArtistsService;

    @GetMapping("/top")
    public TopArtistsPageDto getTopArtists(
            @RequestParam(defaultValue = "points")
            @Pattern(regexp = "points|hits", message = "Ongeldige sortering") String sort,
            @RequestParam(required = false) @Min(1900) @Max(2100) Integer year,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "24") @Min(1) @Max(50) int size
    ) {
        return topArtistsService.getTopArtists(sort, year, page, size);
    }

    @GetMapping("/{artistId}")
    public ArtistDetailDto getArtist(@PathVariable Integer artistId) {
        return artistService.getArtistDetail(artistId);
    }
}
