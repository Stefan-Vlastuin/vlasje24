package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountMailListener {

    private final MailService mailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(AccountMailEvent event) {
        try {
            switch (event.purpose()) {
                case EMAIL_VERIFICATION -> mailService.sendEmailVerification(
                        event.recipient(), event.username(), event.rawToken());
                case PASSWORD_RESET -> mailService.sendPasswordReset(
                        event.recipient(), event.username(), event.rawToken());
            }
        } catch (RuntimeException exception) {
            log.error("Accountmail kon niet worden verstuurd (doel={}, oorzaak={})",
                    event.purpose(), exception.getClass().getSimpleName());
        }
    }
}
