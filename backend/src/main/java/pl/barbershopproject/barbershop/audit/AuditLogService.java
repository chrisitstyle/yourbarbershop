package pl.barbershopproject.barbershop.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * service handling query operations for system activity audit logs
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * retrieves all audit log entries from database ordered by timestamp descending
     *
     * @return list of audit log dtos sorted newest first
     */
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(log -> new AuditLogDTO(
                        log.getId(),
                        log.getTimestamp(),
                        log.getActorEmail(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getDetails()
                ))
                .toList();
    }
}
