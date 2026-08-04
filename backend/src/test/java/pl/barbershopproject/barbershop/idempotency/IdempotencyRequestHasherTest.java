package pl.barbershopproject.barbershop.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyRequestHasherTest {

    private IdempotencyRequestHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new IdempotencyRequestHasher();
    }

    @Test
    void shouldGenerateTheSameHashForTheSameRequestData() {
        String firstHash = hashOrderRequest();
        String secondHash = hashOrderRequest();

        assertThat(firstHash)
                .hasSize(64)
                .isEqualTo(secondHash);
    }

    @Test
    void shouldGenerateDifferentHashWhenRequestDataChanges() {
        String firstHash = hashOrderRequest();

        String secondHash = hasher.hash(
                "idOffer",
                2L,
                "visitDate",
                LocalDateTime.of(2030, Month.JANUARY, 16, 12, 0),
                "paymentMethod",
                "KARTA_ONLINE"
        );

        assertThat(firstHash).isNotEqualTo(secondHash);
    }

    @Test
    void shouldPreserveComponentBoundaries() {
        String firstHash = hasher.hash("ab", "c");
        String secondHash = hasher.hash("a", "bc");

        assertThat(firstHash).isNotEqualTo(secondHash);
    }

    @Test
    void shouldDistinguishNullFromTextValue() {
        String firstHash = hasher.hash((Object) null);
        String secondHash = hasher.hash("null");

        assertThat(firstHash).isNotEqualTo(secondHash);
    }

    private String hashOrderRequest() {
        return hasher.hash(
                "idOffer",
                1L,
                "visitDate",
                LocalDateTime.of(2030, Month.JANUARY, 16, 12, 0),
                "paymentMethod",
                "KARTA_ONLINE"
        );
    }
}
