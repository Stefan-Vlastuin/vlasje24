package nl.vlasje24.controller;

import tools.jackson.databind.ObjectMapper;
import nl.vlasje24.config.SecurityConfig;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.dto.CommentDto;
import nl.vlasje24.dto.CommentPageDto;
import nl.vlasje24.repository.UserRepository;
import nl.vlasje24.security.AccountPrincipal;
import nl.vlasje24.security.AppUserDetailsService;
import nl.vlasje24.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import({SecurityConfig.class, AppUserDetailsService.class})
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CommentService commentService;
    @MockitoBean UserRepository userRepository;

    @Test
    void commentsArePublicAndPaginated() throws Exception {
        when(commentService.getComments(7, 0, 20)).thenReturn(
                new CommentPageDto(List.of(comment()), 0, 20, 1, 1));

        mockMvc.perform(get("/api/v1/charts/7/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments[0].username").value("stefan"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void createRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/charts/7/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("body", "Hallo"))))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(commentService);
    }

    @Test
    void authenticatedAccountCanCreateValidComment() throws Exception {
        when(commentService.createComment(7, 42, "Hallo")).thenReturn(comment());

        mockMvc.perform(post("/api/v1/charts/7/comments")
                        .with(csrf())
                        .with(authentication(authenticationFor(42)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("body", "Hallo"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value(1));

        verify(commentService).createComment(7, 42, "Hallo");
    }

    @Test
    void blankCommentIsRejectedBeforeService() throws Exception {
        mockMvc.perform(post("/api/v1/charts/7/comments")
                        .with(csrf())
                        .with(authentication(authenticationFor(42)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("body", "   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.body").exists());

        verifyNoInteractions(commentService);
    }

    @Test
    void pageSizeAboveLimitIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/charts/7/comments?size=51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private UsernamePasswordAuthenticationToken authenticationFor(int userId) {
        AccountPrincipal principal = new AccountPrincipal(
                userId, "stefan", null, UserRole.USER, true);
        return UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
    }

    private CommentDto comment() {
        Instant createdAt = Instant.parse("2026-10-17T10:00:00Z");
        return new CommentDto(1L, 42, "stefan", "Hallo", createdAt, createdAt, false);
    }
}
