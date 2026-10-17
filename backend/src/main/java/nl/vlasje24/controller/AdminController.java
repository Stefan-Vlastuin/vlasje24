package nl.vlasje24.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import nl.vlasje24.domain.AdminCommentFilter;
import nl.vlasje24.dto.AdminCommentPageDto;
import nl.vlasje24.dto.AdminUserDto;
import nl.vlasje24.dto.AdminUserPageDto;
import nl.vlasje24.dto.CreateArtistDto;
import nl.vlasje24.dto.CreateChartDto;
import nl.vlasje24.dto.CreateSongDto;
import nl.vlasje24.dto.CreatedDto;
import nl.vlasje24.dto.ModerateCommentRequestDto;
import nl.vlasje24.dto.ModerationAuditPageDto;
import nl.vlasje24.dto.UpdateUserSuspensionRequestDto;
import nl.vlasje24.security.AccountPrincipal;
import nl.vlasje24.service.AdminModerationService;
import nl.vlasje24.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;
    private final AdminModerationService moderationService;

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

    @GetMapping("/comments")
    public AdminCommentPageDto getComments(
            @RequestParam(defaultValue = "ALL") AdminCommentFilter status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return moderationService.getComments(status, page, size);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal AccountPrincipal principal,
            @Valid @RequestBody(required = false) ModerateCommentRequestDto request) {
        moderationService.deleteComment(
                commentId,
                principal.userId(),
                request == null ? null : request.reason());
    }

    @GetMapping("/users")
    public AdminUserPageDto getUsers(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return moderationService.getUsers(query, page, size);
    }

    @PatchMapping("/users/{userId}/suspension")
    public AdminUserDto updateUserSuspension(
            @PathVariable Integer userId,
            @AuthenticationPrincipal AccountPrincipal principal,
            @Valid @RequestBody UpdateUserSuspensionRequestDto request) {
        return moderationService.updateSuspension(
                userId, principal.userId(), request.suspended(), request.reason());
    }

    @GetMapping("/moderation-audit")
    public ModerationAuditPageDto getModerationAudit(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return moderationService.getAudit(page, size);
    }
}
