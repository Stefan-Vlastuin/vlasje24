package nl.vlasje24.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers(disabledWithoutDocker = true)
class SmtpMailServiceIntegrationTest {

    @Container
    static final GenericContainer<?> MAILPIT = new GenericContainer<>(
            DockerImageName.parse("axllent/mailpit:v1.31.6"))
            .withExposedPorts(1025, 8025)
            .waitingFor(Wait.forHttp("/api/v1/info").forPort(8025));

    @Test
    void verificationEmailArrivesAtSmtpServer() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(MAILPIT.getHost());
        mailSender.setPort(MAILPIT.getMappedPort(1025));
        mailSender.getJavaMailProperties().setProperty("mail.smtp.connectiontimeout", "5000");
        mailSender.getJavaMailProperties().setProperty("mail.smtp.timeout", "5000");

        SmtpMailService mailService = new SmtpMailService(mailSender);
        ReflectionTestUtils.setField(mailService, "from", "noreply@vlasje24.nl");
        ReflectionTestUtils.setField(mailService, "replyTo", "info@vlasje24.nl");
        ReflectionTestUtils.setField(mailService, "appBaseUrl", "https://vlasje24.nl");

        mailService.sendEmailVerification(
                "member@example.nl", "member", "raw-verification-token");

        HttpClient httpClient = HttpClient.newHttpClient();
        URI messagesUri = URI.create("http://%s:%d/api/v1/messages".formatted(
                MAILPIT.getHost(), MAILPIT.getMappedPort(8025)));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder(messagesUri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body())
                    .contains("member@example.nl")
                    .contains("Bevestig je e-mailadres voor Vlasje24");
        });
    }
}
