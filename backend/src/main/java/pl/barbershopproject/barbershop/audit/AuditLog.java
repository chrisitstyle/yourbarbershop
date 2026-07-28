package pl.barbershopproject.barbershop.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;

import java.time.OffsetDateTime;

/**
 * entity representing an audit log entry in MySQL
 */
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime timestamp;

    @Column(name = "actor_email", nullable = false, updatable = false)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ActionType action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, updatable = false)
    private EntityType entityType;

    @Column(name = "entity_id", updatable = false)
    private String entityId;

    /**
     * optional JSON payload storing contextual details about the performed change
     * (e.g. old vs new values, price modifications, or event metadata)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(updatable = false)
    private String details;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = OffsetDateTime.now();
        }
    }
}