package pl.barbershopproject.barbershop.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogListenerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogListener auditLogListener;

    @Test
    void shouldSaveAuditLogWhenEventHandled() {
        // given
        AuditEvent event = new AuditEvent(
                "admin@barbershop.pl",
                ActionType.ORDER_CREATED,
                EntityType.ORDER,
                "10",
                "{\"kind\":\"STRZYZENIE\"}"
        );

        // when
        auditLogListener.handleAuditEvent(event);

        // then
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertThat(savedLog.getActorEmail()).isEqualTo("admin@barbershop.pl");
        assertThat(savedLog.getAction()).isEqualTo(ActionType.ORDER_CREATED);
        assertThat(savedLog.getEntityType()).isEqualTo(EntityType.ORDER);
        assertThat(savedLog.getEntityId()).isEqualTo("10");
        assertThat(savedLog.getDetails()).isEqualTo("{\"kind\":\"STRZYZENIE\"}");
    }
}
