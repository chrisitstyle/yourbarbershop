package pl.barbershopproject.barbershop.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaService;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
public abstract class BaseIntegrationTest {

    @MockitoBean
    private JavaMailSender javaMailSender;
    @MockitoBean
    protected CaptchaService captchaService;
    @SuppressWarnings("resource")
    @Container
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("barbershop-with-roles")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("barbershop-with-roles_dump_integration_tests.sql");

    // Valkey container
    @SuppressWarnings("resource")
    @Container
    static final GenericContainer<?> valkeyContainer = new GenericContainer<>("valkey/valkey:8")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);

        registry.add("spring.data.redis.host", valkeyContainer::getHost);
        registry.add("spring.data.redis.port", () -> valkeyContainer.getMappedPort(6379));
    }
}