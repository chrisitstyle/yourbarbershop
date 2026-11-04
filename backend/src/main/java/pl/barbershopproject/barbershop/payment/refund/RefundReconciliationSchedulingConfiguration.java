
package pl.barbershopproject.barbershop.payment.refund;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduled refund reconciliation when explicitly configured.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "application.refunds.reconciliation.enabled",
        havingValue = "true")
public class RefundReconciliationSchedulingConfiguration { }
