package nl.vlasje24.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.vlasje24.config.SecurityConfig;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.dto.AdminCommentPageDto;
import nl.vlasje24.dto.AdminUserDto;
import nl.vlasje24.security.AccountPrincipal;
import nl.vlasje24.security.AppUserDetailsService;
import nl.vlasje24.service.AdminModerationService;
import nl.vlasje24.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AdminService adminService;
    @MockBean AdminModerationService moderationService;
    @MockBean AppUserDetailsService appUserDetailsService;

    @Test
    void createArtist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/artists")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Adele"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createArtist_authenticated_returns201WithId() throws Exception {
        when(adminService.createArtist(any())).thenReturn(42);

        mockMvc.perform(post("/api/v1/admin/artists")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Adele"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void createSong_authenticated_returns201WithId() throws Exception {
        when(adminService.createSong(any())).thenReturn(10);

        Map<String, Object> body = Map.of(
                "title", "Hello",
                "imageUrl", "https://img.example.com/hello.jpg",
                "previewUrl", "https://preview.example.com/hello.mp3",
                "artistIds", List.of(1));

        mockMvc.perform(post("/api/v1/admin/songs")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createChart_authenticated_returns201WithId() throws Exception {
        when(adminService.createChart(any())).thenReturn(5);

        List<Integer> songIds = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24);
        Map<String, Object> body = Map.of("date", "2024-01-08", "songIds", songIds);

        mockMvc.perform(post("/api/v1/admin/charts")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createSong_unauthenticated_returns401() throws Exception {
        Map<String, Object> body = Map.of("title", "Hello", "artistIds", List.of(1));

        mockMvc.perform(post("/api/v1/admin/songs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createArtist_userRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/artists")
                        .with(user("member").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Adele"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminService);
    }

    @Test
    void createArtist_withoutCsrf_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/artists")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "Adele"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminService);
    }

    @Test
    void moderationOverviewRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/comments")
                        .with(user("member").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(moderationService);
    }

    @Test
    void moderationOverviewReturnsPagedComments() throws Exception {
        when(moderationService.getComments(any(), eq(0), eq(20)))
                .thenReturn(new AdminCommentPageDto(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/v1/admin/comments?status=DELETED")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void moderatorCanDeleteCommentWithInternalReason() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/comments/9")
                        .with(authentication(adminAuthentication()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Spam"))))
                .andExpect(status().isNoContent());

        verify(moderationService).deleteComment(9L, 1, "Spam");
    }

    @Test
    void moderatorCanSuspendUser() throws Exception {
        AdminUserDto response = new AdminUserDto(
                42, "member", UserRole.USER, nl.vlasje24.domain.UserStatus.SUSPENDED,
                true, java.time.Instant.parse("2026-10-17T10:00:00Z"));
        when(moderationService.updateSuspension(42, 1, true, "Misbruik"))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/users/42/suspension")
                        .with(authentication(adminAuthentication()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "suspended", true, "reason", "Misbruik"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void moderationReasonAboveMaximumIsRejected() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/comments/9")
                        .with(authentication(adminAuthentication()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "x".repeat(501)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reason").exists());
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AccountPrincipal principal = new AccountPrincipal(1, "admin", null, UserRole.ADMIN, true);
        return UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
    }
}
