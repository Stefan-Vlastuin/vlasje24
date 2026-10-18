package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SmtpMailService implements MailService {

    private static final DateTimeFormatter DUTCH_DATE =
            DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.forLanguageTag("nl-NL"));

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.reply-to}")
    private String replyTo;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Override
    public void sendEmailVerification(String recipient, String username, String rawToken) {
        String link = accountLink("/verify-email", rawToken);
        send(recipient, "Bevestig je e-mailadres voor Vlasje24", """
                Hallo %s,

                Bevestig je e-mailadres via deze link:
                %s

                Deze link is 24 uur geldig en kan één keer worden gebruikt.
                Heb je dit account niet aangemaakt? Dan kun je dit bericht negeren.

                Groet,
                Vlasje24
                """.formatted(username, link));
    }

    @Override
    public void sendPasswordReset(String recipient, String username, String rawToken) {
        String link = accountLink("/reset-password", rawToken);
        send(recipient, "Stel een nieuw Vlasje24-wachtwoord in", """
                Hallo %s,

                Stel via deze link een nieuw wachtwoord in:
                %s

                Deze link is 60 minuten geldig en kan één keer worden gebruikt.
                Heb je dit niet aangevraagd? Dan kun je dit bericht negeren.

                Groet,
                Vlasje24
                """.formatted(username, link));
    }

    @Override
    public void sendChartPublished(
            String recipient, String username, Integer weekId, LocalDate chartDate) {
        String link = UriComponentsBuilder.fromUriString(appBaseUrl)
                .path("/chart/{weekId}")
                .buildAndExpand(weekId)
                .toUriString();
        send(recipient, "De nieuwe Vlasje24-lijst staat online", """
                Hallo %s,

                De Vlasje24-lijst van %s staat online.
                Bekijk de nieuwe lijst: %s

                Je ontvangt dit bericht omdat je lijstmeldingen hebt aangezet.
                Je kunt deze meldingen uitschakelen via %s/account.

                Groet,
                Vlasje24
                """.formatted(username, DUTCH_DATE.format(chartDate), link, appBaseUrl));
    }

    private String accountLink(String path, String rawToken) {
        return UriComponentsBuilder.fromUriString(appBaseUrl)
                .path(path)
                .queryParam("token", rawToken)
                .build()
                .toUriString();
    }

    private void send(String recipient, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        if (replyTo != null && !replyTo.isBlank()) {
            message.setReplyTo(replyTo);
        }
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
