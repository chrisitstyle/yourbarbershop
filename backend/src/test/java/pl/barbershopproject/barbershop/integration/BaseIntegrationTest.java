package pl.barbershopproject.barbershop.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaService;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
public abstract class BaseIntegrationTest {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    protected CaptchaService captchaService;

    @SuppressWarnings("resource")
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("barbershop-with-roles")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("barbershop-with-roles_dump_integration_tests.sql");

    @SuppressWarnings("resource")
    static final GenericContainer<?> valkeyContainer = new GenericContainer<>("valkey/valkey:8")
            .withExposedPorts(6379);

    static {
        mysqlContainer.start();
        valkeyContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);

        registry.add("spring.data.redis.host", valkeyContainer::getHost);
        registry.add("spring.data.redis.port", () -> valkeyContainer.getMappedPort(6379));
    }
}