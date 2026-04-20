package nl.vlasje24.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.vlasje24.config.SecurityConfig;
import nl.vlasje24.domain.User;
import nl.vlasje24.repository.UserRepository;
import nl.vlasje24.security.JwtAuthFilter;
import nl.vlasje24.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserRepository userRepository;
    @MockBean JwtUtil jwtUtil;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        User user = mock(User.class);
        when(user.getPassword()).thenReturn(encoder.encode("secret"));
        when(user.getUsername()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("admin")).thenReturn("generated-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("generated-token"));
    }

    @Test
    void login_unknownUser_returns401() throws Exception {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "unknown", "password", "any"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        User user = mock(User.class);
        when(user.getPassword()).thenReturn(encoder.encode("correct"));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }
}
