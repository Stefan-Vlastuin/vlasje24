package nl.vlasje24.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.vlasje24.config.SecurityConfig;
import nl.vlasje24.security.AppUserDetailsService;
import nl.vlasje24.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AdminService adminService;
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
}
