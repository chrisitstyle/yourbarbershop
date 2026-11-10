package pl.barbershopproject.barbershop.integration.auth.refresh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.auth.refresh.RefreshToken;
import pl.barbershopproject.barbershop.auth.refresh.RefreshTokenRepository;
import pl.barbershopproject.barbershop.auth.refresh.RefreshTokenRotation;
import pl.barbershopproject.barbershop.auth.refresh.RefreshTokenService;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void rotateRefreshToken_RevokesOldTokenAndPersistsReplacement() {
        String originalToken = createToken();

        RefreshTokenRotation rotation = refreshTokenService
                .rotateRefreshToken(originalToken, request());

        RefreshToken oldToken = refreshTokenRepository
                .findByTokenHash(sha256(originalToken))
                .orElseThrow();

        RefreshToken newToken = refreshTokenRepository
                .findByTokenHash(sha256(rotation.newRefreshToken()))
                .orElseThrow();

        assertTrue(oldToken.isRevoked());
        assertNotNull(oldToken.getRevokedAt());

        assertEquals(newToken.getTokenHash(),
                oldToken.getReplacedByTokenHash());

        assertFalse(newToken.isRevoked());
        assertEquals(
                oldToken.getUser().getIdUser(),
                rotation.user().getIdUser());

        MockHttpServletRequest httpRequest = request();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> refreshTokenService.rotateRefreshToken(
                        originalToken,
                        httpRequest));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void revokeRefreshToken_RejectsSubsequentRotation() {
        String originalToken = createToken();

        refreshTokenService.revokeRefreshToken(originalToken);

        RefreshToken token = refreshTokenRepository
                .findByTokenHash(sha256(originalToken))
                .orElseThrow();

        assertTrue(token.isRevoked());
        assertNotNull(token.getRevokedAt());
        assertNull(token.getReplacedByTokenHash());

        MockHttpServletRequest httpRequest = request();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> refreshTokenService.rotateRefreshToken(
                        originalToken,
                        httpRequest));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    @Timeout(45)
    void rotateRefreshToken_WhenTwoRequestsRace_OnlyOneSucceeds() throws Exception {

        String originalToken = createToken();
        long tokenCountBefore = refreshTokenRepository.count();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Object> rotationTask = () -> {
            ready.countDown();

            if (!start.await(10, SECONDS)) {
                throw new IllegalStateException("Timed out waiting for start signal");
            }

            try {
                return refreshTokenService.rotateRefreshToken(
                        originalToken,
                        request());
            } catch (ResponseStatusException exception) {
                return exception;
            }
        };

        try {
            Future<Object> first = executor.submit(rotationTask);
            Future<Object> second = executor.submit(rotationTask);

            assertTrue(ready.await(10, SECONDS),
                    "Both requests should be ready");

            start.countDown();

            Object firstResult = first.get(20, SECONDS);
            Object secondResult = second.get(20, SECONDS);

            long successfulRotations = Stream.of(firstResult, secondResult)
                    .filter(RefreshTokenRotation.class::isInstance)
                    .count();

            long rejectedRotations = Stream.of(firstResult, secondResult)
                    .filter(ResponseStatusException.class::isInstance)
                    .count();

            assertEquals(1L, successfulRotations);
            assertEquals(1L, rejectedRotations);

            ResponseStatusException rejected =
                    (ResponseStatusException) Stream.of(firstResult, secondResult)
                            .filter(ResponseStatusException.class::isInstance)
                            .findFirst()
                            .orElseThrow();

            assertEquals(HttpStatus.UNAUTHORIZED, rejected.getStatusCode());

            RefreshTokenRotation successful =
                    (RefreshTokenRotation) Stream.of(firstResult, secondResult)
                            .filter(RefreshTokenRotation.class::isInstance)
                            .findFirst()
                            .orElseThrow();

            RefreshToken oldToken = refreshTokenRepository
                    .findByTokenHash(sha256(originalToken))
                    .orElseThrow();

            String replacementHash = sha256(successful.newRefreshToken());

            RefreshToken newToken = refreshTokenRepository
                    .findByTokenHash(replacementHash)
                    .orElseThrow();

            assertTrue(oldToken.isRevoked());
            assertFalse(newToken.isRevoked());

            assertEquals(replacementHash, oldToken.getReplacedByTokenHash());

            assertEquals(tokenCountBefore + 1,
                    refreshTokenRepository.count(),
                    "Only one replacement token should be created");

        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(5, SECONDS);
        }
    }

    @Test
    @Timeout(45)
    void findForUpdateByTokenHash_BlocksConcurrentTransaction() throws Exception {

        String originalToken = createToken();
        String tokenHash = sha256(originalToken);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            Future<?> firstTransaction = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        refreshTokenRepository
                                .findForUpdateByTokenHash(tokenHash)
                                .orElseThrow();

                        lockAcquired.countDown();

                        try {
                            if (!releaseLock.await(15, SECONDS)) {
                                throw new IllegalStateException(
                                        "Timed out waiting to release lock"
                                );
                            }
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(exception);
                        }
                    }));

            assertTrue(lockAcquired.await(10, SECONDS),
                    "First transaction should acquire the lock");

            Future<?> secondTransaction = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        secondStarted.countDown();

                        refreshTokenRepository
                                .findForUpdateByTokenHash(tokenHash)
                                .orElseThrow();
                    })
            );

            assertTrue(
                    secondStarted.await(10, SECONDS),
                    "Second transaction should start"
            );
            assertThrows(
                    TimeoutException.class,
                    () -> secondTransaction.get(500, MILLISECONDS),
                    "Second transaction should wait for the lock");

            releaseLock.countDown();

            assertDoesNotThrow(() -> firstTransaction.get(10, SECONDS));
            assertDoesNotThrow(() -> secondTransaction.get(10, SECONDS));

        } finally {
            releaseLock.countDown();
            executor.shutdownNow();
            executor.awaitTermination(5, SECONDS);
        }
    }

    private String createToken() {
        User user = userRepository
                .findByEmail("johndoe@example.com")
                .orElseThrow();

        return refreshTokenService.createRefreshToken(user, request());
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "IntegrationTest");
        return request;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}