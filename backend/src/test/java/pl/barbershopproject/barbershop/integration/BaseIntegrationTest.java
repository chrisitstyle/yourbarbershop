package pl.barbershopproject.barbershop.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.testcontainers.containers.GenericContainer;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaService;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Sql(scripts = "/sql/integration-base-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))

@Sql(scripts = "/sql/clear-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(
                transactionMode = SqlConfig.TransactionMode.ISOLATED))
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public abstract class BaseIntegrationTest extends BaseTestEnvironment {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    protected CaptchaService captchaService;

    @SuppressWarnings("resource")
    static final GenericContainer<?> valkeyContainer = new GenericContainer<>("valkey/valkey:8")
                    .withExposedPorts(6379);

    static {valkeyContainer.start();}

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.redis.host",
                valkeyContainer::getHost
        );
        registry.add(
                "spring.data.redis.port",
                () -> valkeyContainer.getMappedPort(6379)
        );
    }
}