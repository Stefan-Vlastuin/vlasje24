package nl.vlasje24.dto;

import nl.vlasje24.domain.User;
import nl.vlasje24.domain.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDtoTest {

    @Test
    void from_doesNotExposePrivateAccountFields() {
        AccountDto dto = AccountDto.from(
                new User("member", "private@example.nl", "$2a$10$private-hash"));

        assertThat(dto.username()).isEqualTo("member");
        assertThat(dto.role()).isEqualTo(UserRole.USER);
        assertThat(dto.emailVerified()).isFalse();
        assertThat(AccountDto.class.getRecordComponents())
                .extracting(component -> component.getName())
                .containsExactly("userId", "username", "role", "emailVerified");
    }
}
