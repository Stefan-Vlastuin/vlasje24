package nl.vlasje24.controller;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.SongDetailDto;
import nl.vlasje24.dto.TopSongsPageDto;
import nl.vlasje24.service.SongService;
import nl.vlasje24.service.TopSongsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final TopSongsService topSongsService;

    @GetMapping("/top")
    public TopSongsPageDto getTopSongs(
            @RequestParam(defaultValue = "points") String sort,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size
    ) {
        return topSongsService.getTopSongs(sort, year, page, size);
    }

    @GetMapping("/{songId}")
    public SongDetailDto getSong(@PathVariable Integer songId) {
        return songService.getSongDetail(songId);
    }
}
