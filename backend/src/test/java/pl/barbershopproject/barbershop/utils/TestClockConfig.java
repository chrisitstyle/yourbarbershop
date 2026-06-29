package pl.barbershopproject.barbershop.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfig {

    @Bean
    public Clock clock() {
        return Clock.fixed(
                Instant.parse("2026-01-16T12:00:00Z"),
                ZoneId.of("Europe/Warsaw")
        );
    }
}
