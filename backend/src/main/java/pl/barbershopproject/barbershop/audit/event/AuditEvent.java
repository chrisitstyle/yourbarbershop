package pl.barbershopproject.barbershop.audit.event;

import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;

/**
 * domain event published whenever a notable business operation occurs
 */
public record AuditEvent(
        String actorEmail,
        ActionType action,
        EntityType entityType,
        String entityId,
        String detailsJson
) {}
