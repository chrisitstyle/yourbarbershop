package pl.barbershopproject.barbershop.idempotency;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Generates a deterministic SHA-256 hash from request components.
 *
 * <p>The hash is stored together with the Idempotency-Key and allows
 * the application to detect when the same key is reused with different
 * request data.</p>
 */
@Component
public class IdempotencyRequestHasher {

    public String hash(Object... components) {
        Objects.requireNonNull(components,
                "Elementy żądania nie mogą być null");

        MessageDigest digest = createDigest();

        for (Object component : components) {
            updateDigest(digest, component);
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private void updateDigest(
            MessageDigest digest,
            Object component
    ) {
        if (component == null) {
            digest.update((byte) 0);
            return;
        }

        digest.update((byte) 1);

        byte[] value = component.toString()
                .getBytes(StandardCharsets.UTF_8);

        byte[] length = ByteBuffer.allocate(Integer.BYTES)
                .putInt(value.length)
                .array();

        digest.update(length);
        digest.update(value);
    }

    private MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Algorytm SHA-256 nie jest dostępny",
                    exception
            );
        }
    }
}
