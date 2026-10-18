package nl.vlasje24.controller;

import tools.jackson.databind.ObjectMapper;
import nl.vlasje24.config.SecurityConfig;
import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import nl.vlasje24.dto.AccountDto;
import nl.vlasje24.repository.UserRepository;
import nl.vlasje24.security.AppUserDetailsService;
import nl.vlasje24.security.AuthRateLimiter;
import nl.vlasje24.service.AccountService;
import nl.vlasje24.service.AccountDeletionService;
import nl.vlasje24.service.AccountTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AppUserDetailsService.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserRepository userRepository;
    @MockitoBean AccountService accountService;
    @MockitoBean AccountDeletionService accountDeletionService;
    @MockitoBean AccountTokenService accountTokenService;
    @MockitoBean AuthRateLimiter rateLimiter;

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
        when(accountService.getAccount(1)).thenReturn(
                new AccountDto(1, "admin", UserRole.ADMIN, true, false));

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
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.emailVerified").value(true));

        verify(accountService, org.mockito.Mockito.times(2)).getAccount(1);
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
    void login_passwordAboveBcryptByteLimitReturnsGeneric401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "member",
                                "password", "é".repeat(37)))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        verifyNoInteractions(userRepository);
    }

    @Test
    void register_validRequest_returnsCreatedAccount() throws Exception {
        var account = new AccountDto(42, "member", UserRole.USER, false, true);
        when(accountService.register(org.mockito.ArgumentMatchers.any())).thenReturn(account);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "member",
                                "email", "member@example.nl",
                                "password", "long-password",
                                "chartNotificationsEnabled", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andExpect(jsonPath("$.chartNotificationsEnabled").value(true));
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
    void verifyEmail_validRequest_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "raw-token"))))
                .andExpect(status().isNoContent());

        verify(accountTokenService).verifyEmail("raw-token");
    }

    @Test
    void resendAndForgotPassword_doNotExposeWhetherAccountExists() throws Exception {
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "unknown@example.nl"))))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "unknown@example.nl"))))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));
    }

    @Test
    void resetPassword_validRequest_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "raw-token",
                                "password", "new-long-password"))))
                .andExpect(status().isNoContent());

        verify(accountTokenService).resetPassword(any());
    }

    @Test
    void me_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateChartNotifications_usesAuthenticatedAccount() throws Exception {
        var principal = new nl.vlasje24.security.AccountPrincipal(
                42, "member", null, UserRole.USER, true);
        var authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                .authenticated(principal, null, principal.getAuthorities());
        when(accountService.updateChartNotifications(42, true)).thenReturn(
                new AccountDto(42, "member", UserRole.USER, true, true));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/auth/chart-notifications")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("enabled", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chartNotificationsEnabled").value(true));

        verify(accountService).updateChartNotifications(42, true);
    }

    @Test
    void deleteAccount_requiresPasswordAndUsesAuthenticatedAccount() throws Exception {
        var principal = new nl.vlasje24.security.AccountPrincipal(
                42, "member", null, UserRole.USER, true);
        var authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                .authenticated(principal, null, principal.getAuthorities());

        mockMvc.perform(delete("/api/v1/auth/account")
                        .with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "a-long-password"))))
                .andExpect(status().isNoContent());

        verify(accountDeletionService).deleteAccount(42, "a-long-password");
    }

    @Test
    void login_withoutCsrf_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "admin", "password", "secret"))))
                .andExpect(status().isForbidden());
    }
}
