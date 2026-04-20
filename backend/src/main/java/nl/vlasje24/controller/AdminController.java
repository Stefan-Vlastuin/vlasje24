package nl.vlasje24.controller;

import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.CreateArtistDto;
import nl.vlasje24.dto.CreateChartDto;
import nl.vlasje24.dto.CreateSongDto;
import nl.vlasje24.dto.CreatedDto;
import nl.vlasje24.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/artists")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedDto createArtist(@RequestBody CreateArtistDto dto) {
        return new CreatedDto(adminService.createArtist(dto));
    }

    @PostMapping("/songs")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedDto createSong(@RequestBody CreateSongDto dto) {
        return new CreatedDto(adminService.createSong(dto));
    }

    @PostMapping("/charts")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedDto createChart(@RequestBody CreateChartDto dto) {
        return new CreatedDto(adminService.createChart(dto));
    }
}
