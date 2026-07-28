package pl.barbershopproject.barbershop.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void shouldReturnAllAuditLogsMappedToDTO() {
        // given
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .timestamp(OffsetDateTime.now())
                .actorEmail("admin@barbershop.pl")
                .action(ActionType.ORDER_CREATED)
                .entityType(EntityType.ORDER)
                .entityId("100")
                .details("{\"key\":\"value\"}")
                .build();

        given(auditLogRepository.findAllByOrderByTimestampDesc())
                .willReturn(List.of(log1));

        // when
        List<AuditLogDTO> result = auditLogService.getAllLogs();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.getFirst().actorEmail()).isEqualTo("admin@barbershop.pl");
        assertThat(result.getFirst().action()).isEqualTo(ActionType.ORDER_CREATED);
        assertThat(result.getFirst().entityType()).isEqualTo(EntityType.ORDER);
    }
}
