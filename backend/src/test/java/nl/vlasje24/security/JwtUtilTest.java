package nl.vlasje24.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JwtUtil.class)
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateToken_extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("admin");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    void isValid_withValidToken_returnsTrue() {
        String token = jwtUtil.generateToken("admin");

        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValid_withTamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("admin");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtUtil.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_withGarbageString_returnsFalse() {
        assertThat(jwtUtil.isValid("not.a.token")).isFalse();
    }

    @Test
    void generateToken_differentUsernames_producesDifferentTokens() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");

        assertThat(token1).isNotEqualTo(token2);
    }
}
