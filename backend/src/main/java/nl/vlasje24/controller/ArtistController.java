package nl.vlasje24.controller;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.ArtistDetailDto;
import nl.vlasje24.dto.TopArtistsPageDto;
import nl.vlasje24.service.ArtistService;
import nl.vlasje24.service.TopArtistsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final TopArtistsService topArtistsService;

    @GetMapping("/top")
    public TopArtistsPageDto getTopArtists(
            @RequestParam(defaultValue = "points") String sort,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size
    ) {
        return topArtistsService.getTopArtists(sort, year, page, size);
    }

    @GetMapping("/{artistId}")
    public ArtistDetailDto getArtist(@PathVariable Integer artistId) {
        return artistService.getArtistDetail(artistId);
    }
}
