package pl.barbershopproject.barbershop.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * repository interface for managing system audit log database operations
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * finds all audit logs ordered by timestamp in descending order
     *
     * @return list of audit log entities sorted newest first
     */
    List<AuditLog> findAllByOrderByTimestampDesc();
}
