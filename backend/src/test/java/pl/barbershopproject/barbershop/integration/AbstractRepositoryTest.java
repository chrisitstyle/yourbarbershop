package pl.barbershopproject.barbershop.integration;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for repository slice tests.
 * Repository tests use a real MySQL Testcontainer and the production Flyway
 * migrations instead of an embedded H2 database and a separate test schema.
 *
 * @DataJpaTest keeps the Spring context lightweight and automatically rolls
 * back database changes after each test.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractRepositoryTest extends BaseTestEnvironment { }
