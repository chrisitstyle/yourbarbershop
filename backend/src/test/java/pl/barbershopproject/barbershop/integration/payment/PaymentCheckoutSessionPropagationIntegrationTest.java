package pl.barbershopproject.barbershop.integration.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.payment.checkout.PaymentCheckoutSessionUpdater;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/sql/payment/checkout-session-propagation-setup.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
class PaymentCheckoutSessionPropagationIntegrationTest extends BaseIntegrationTest {

    private static final long PAYMENT_ID = 900001L;
    private static final String SESSION_ID = "cs_test_requires_new_123";

    @Autowired
    private PaymentCheckoutSessionUpdater checkoutSessionUpdater;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCommitSessionIdWhenOuterTransactionRollsBack() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive())
                    .isTrue();

            checkoutSessionUpdater.assignSession(PAYMENT_ID, SESSION_ID);

            status.setRollbackOnly();
        });

        String persistedSessionId = jdbcTemplate.queryForObject(
                """
                        SELECT stripe_checkout_session_id
                        FROM payment
                        WHERE id_payment = ?
                        """,
                String.class,
                PAYMENT_ID
        );

        assertThat(persistedSessionId).isEqualTo(SESSION_ID);
    }
}