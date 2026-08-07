package pl.barbershopproject.barbershop.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base environment for all tests requiring a database.
 * Uses a shared MySQL Testcontainer so repository and integration tests
 * run against the same database engine as the application.
 * The database schema is created exclusively by Flyway migrations.
 */
public abstract class BaseTestEnvironment {

    @ServiceConnection
    @SuppressWarnings("resource")
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("barbershop-with-roles")
                    .withUsername("test")
                    .withPassword("test");

    static {mysqlContainer.start();}
}
