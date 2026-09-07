package nl.vlasje24.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.vlasje24.config.SecurityConfig;
import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.dto.AccountDto;
import nl.vlasje24.repository.UserRepository;
import nl.vlasje24.security.AppUserDetailsService;
import nl.vlasje24.security.AuthRateLimiter;
import nl.vlasje24.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AppUserDetailsService.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserRepository userRepository;
    @MockBean AccountService accountService;
    @MockBean AuthRateLimiter rateLimiter;

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void csrf_returnsTokenAndCookie() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_validCredentialsCreatesSessionWithoutReturningToken() throws Exception {
        User user = mock(User.class);
        when(user.getPassword()).thenReturn(encoder.encode("secret"));
        when(user.getUsername()).thenReturn("admin");
        when(user.getUserId()).thenReturn(1);
        when(user.getRole()).thenReturn(UserRole.ADMIN);
        when(user.isActive()).thenReturn(true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        var result = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void logout_invalidatesAuthenticatedSession() throws Exception {
        User user = mock(User.class);
        when(user.getPassword()).thenReturn(encoder.encode("secret"));
        when(user.getUsername()).thenReturn("admin");
        when(user.getUserId()).thenReturn(1);
        when(user.getRole()).thenReturn(UserRole.ADMIN);
        when(user.isActive()).thenReturn(true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin", "password", "secret"))))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(post("/api/v1/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void login_unknownUser_returns401() throws Exception {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "unknown", "password", "any"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        User user = mock(User.class);
        when(user.getPassword()).thenReturn(encoder.encode("correct"));
        when(user.isActive()).thenReturn(true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_validRequest_returnsCreatedAccount() throws Exception {
        var account = new AccountDto(42, "member", UserRole.USER, false);
        when(accountService.register(org.mockito.ArgumentMatchers.any())).thenReturn(account);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "member",
                                "email", "member@example.nl",
                                "password", "long-password"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.emailVerified").value(false));
    }

    @Test
    void register_invalidRequest_returnsFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "!",
                                "email", "wrong",
                                "password", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(accountService);
    }

    @Test
    void me_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withoutCsrf_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "secret"))))
                .andExpect(status().isForbidden());
    }
}
