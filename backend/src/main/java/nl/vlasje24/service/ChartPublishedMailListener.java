package nl.vlasje24.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.vlasje24.domain.UserStatus;
import nl.vlasje24.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChartPublishedMailListener {

    private final UserRepository userRepository;
    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(ChartPublishedEvent event) {
        var recipients = userRepository
                .findAllByStatusAndEmailVerifiedAtIsNotNullAndChartNotificationsEnabledTrue(
                        UserStatus.ACTIVE);
        int sent = 0;
        for (var recipient : recipients) {
            try {
                mailService.sendChartPublished(
                        recipient.getEmail(), recipient.getUsername(),
                        event.weekId(), event.chartDate());
                sent++;
            } catch (RuntimeException exception) {
                log.error("Lijstmelding kon niet worden verstuurd (weekId={}, userId={}, oorzaak={})",
                        event.weekId(), recipient.getUserId(),
                        exception.getClass().getSimpleName());
            }
        }
        log.info("Lijstmeldingen verwerkt (weekId={}, ontvangers={}, verstuurd={})",
                event.weekId(), recipients.size(), sent);
    }
}
