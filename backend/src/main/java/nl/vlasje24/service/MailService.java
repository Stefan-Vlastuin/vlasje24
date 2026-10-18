package nl.vlasje24.service;

import java.time.LocalDate;

public interface MailService {

    void sendEmailVerification(String recipient, String username, String rawToken);

    void sendPasswordReset(String recipient, String username, String rawToken);

    void sendChartPublished(String recipient, String username, Integer weekId, LocalDate chartDate);
}
