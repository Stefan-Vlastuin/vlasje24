package nl.vlasje24.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import nl.vlasje24.dto.CommentBodyRequestDto;
import nl.vlasje24.dto.CommentDto;
import nl.vlasje24.dto.CommentPageDto;
import nl.vlasje24.security.AccountPrincipal;
import nl.vlasje24.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/charts/{weekId}/comments")
    public CommentPageDto getComments(
            @PathVariable Integer weekId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return commentService.getComments(weekId, page, size);
    }

    @PostMapping("/charts/{weekId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable Integer weekId,
            @AuthenticationPrincipal AccountPrincipal principal,
            @Valid @RequestBody CommentBodyRequestDto request) {
        return commentService.createComment(weekId, principal.userId(), request.body());
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto editComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal AccountPrincipal principal,
            @Valid @RequestBody CommentBodyRequestDto request) {
        return commentService.editComment(commentId, principal.userId(), request.body());
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal AccountPrincipal principal) {
        commentService.deleteComment(commentId, principal.userId());
    }
}
