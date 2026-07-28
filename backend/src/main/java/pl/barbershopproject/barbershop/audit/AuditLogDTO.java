package pl.barbershopproject.barbershop.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;

import java.time.OffsetDateTime;

@Schema(description = "Data Transfer Object representing an audit log entry")
public record AuditLogDTO(

        @Schema(description = "Unique audit log identifier", example = "1")
        Long id,

        @Schema(description = "Timestamp when the audit action occurred", example = "2026-07-27T20:15:30Z")
        OffsetDateTime timestamp,

        @Schema(description = "Email of the user who performed the action, or SYSTEM", example = "admin@barbershop.pl")
        String actorEmail,

        @Schema(description = "Specific action type performed", example = "ORDER_CREATED")
        ActionType action,

        @Schema(description = "Domain entity type affected by the action", example = "ORDER")
        EntityType entityType,

        @Schema(description = "Identifier of the target entity", example = "42")
        String entityId,

        @Schema(description = "Additional JSON details about the performed change", example = "{\"oldStatus\":\"NOWE\", \"newStatus\":\"ZREALIZOWANE\"}")
        String details
) {}
