package pl.barbershopproject.barbershop.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;

/**
 * listens to domain audit events and persists them asynchronously after transaction commit
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogListener {

    private final AuditLogRepository auditLogRepository;

    /**
     * handles published audit events asynchronously after database transaction commits
     *
     * @param event published domain audit event containing event details
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleAuditEvent(AuditEvent event) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .actorEmail(event.actorEmail() != null ? event.actorEmail() : "SYSTEM")
                    .action(event.action())
                    .entityType(event.entityType())
                    .entityId(event.entityId())
                    .details(event.detailsJson())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("audit log saved for action: {}", event.action());
        } catch (Exception e) {
            log.error("failed to save audit log entry for action: {}", event.action(), e);
        }
    }
}
