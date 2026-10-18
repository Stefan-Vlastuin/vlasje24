package nl.vlasje24.service;

public interface MailService {

    void sendEmailVerification(String recipient, String username, String rawToken);

    void sendPasswordReset(String recipient, String username, String rawToken);
}
