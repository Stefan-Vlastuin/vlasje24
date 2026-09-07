package nl.vlasje24.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.vlasje24.domain.User;
import nl.vlasje24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=",
        "server.servlet.session.cookie.secure=false",
        "server.servlet.session.cookie.name=VLASJE24_SESSION",
        "server.servlet.session.cookie.http-only=true",
        "server.servlet.session.cookie.same-site=lax",
        "server.servlet.session.cookie.max-age=14d",
        "spring.session.timeout=14d"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class SessionAuthenticationIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createAccount() {
        userRepository.deleteAll();
        userRepository.saveAndFlush(new User(
                "member", "member@example.nl", passwordEncoder.encode("a-long-password")));
    }

    @Test
    void loginPersistsSessionAndLogoutDeletesIt() throws Exception {
        var csrfResult = mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode csrfBody = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
        String csrfToken = csrfBody.get("token").asText();
        var csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrfCookie).isNotNull();

        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "member@example.nl",
                                "password", "a-long-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("member"))
                .andReturn();
        var sessionCookie = loginResult.getResponse().getCookie("VLASJE24_SESSION");
        assertThat(sessionCookie).isNotNull();
        assertThat(sessionCookie.isHttpOnly()).isTrue();
        assertThat(sessionCookie.getAttribute("SameSite")).isEqualTo("Lax");
        assertThat(sessionCookie.getMaxAge()).isEqualTo(14 * 24 * 60 * 60);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `SPRING_SESSION`", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT `MAX_INACTIVE_INTERVAL` FROM `SPRING_SESSION`", Integer.class))
                .isEqualTo(14 * 24 * 60 * 60);

        mockMvc.perform(get("/api/v1/auth/me").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("member"));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(sessionCookie, csrfCookie)
                        .header("X-XSRF-TOKEN", csrfToken))
                .andExpect(status().isNoContent());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `SPRING_SESSION`", Integer.class)).isZero();
    }
}
