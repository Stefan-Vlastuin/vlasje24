package nl.vlasje24.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpMailServiceTest {

    @Mock JavaMailSender mailSender;

    private SmtpMailService service;

    @BeforeEach
    void setUp() {
        service = new SmtpMailService(mailSender);
        ReflectionTestUtils.setField(service, "from", "Vlasje24 <accounts@vlasje24.nl>");
        ReflectionTestUtils.setField(service, "replyTo", "reply@example.nl");
        ReflectionTestUtils.setField(service, "appBaseUrl", "https://vlasje24.nl");
    }

    @Test
    void verificationMailContainsConfiguredHeadersAndLink() {
        service.sendEmailVerification("member@example.nl", "member", "raw_token-123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("Vlasje24 <accounts@vlasje24.nl>");
        assertThat(message.getReplyTo()).isEqualTo("reply@example.nl");
        assertThat(message.getTo()).containsExactly("member@example.nl");
        assertThat(message.getSubject()).isEqualTo("Bevestig je e-mailadres voor Vlasje24");
        assertThat(message.getText()).contains(
                "https://vlasje24.nl/verify-email?token=raw_token-123");
    }

    @Test
    void passwordResetMailUsesResetRoute() {
        service.sendPasswordReset("member@example.nl", "member", "raw_token-123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains(
                "https://vlasje24.nl/reset-password?token=raw_token-123");
    }
}
