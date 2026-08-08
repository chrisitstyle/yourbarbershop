package pl.barbershopproject.barbershop.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.payment.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyRequestServiceTest {

    private static final Long REQUEST_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final Long ORDER_ID = 30L;

    private static final String IDEMPOTENCY_KEY = "order-creation-test-key";

    private static final String REQUEST_HASH = "a".repeat(64);

    private static final String OTHER_REQUEST_HASH = "b".repeat(64);

    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_123";

    private static final Long PAYMENT_ID = 40L;

    private static final String STRIPE_CHECKOUT_IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-04T10:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private IdempotencyRequestRepository idempotencyRequestRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private IdempotencyRequestService idempotencyRequestService;

    @BeforeEach
    void setUp() {
        idempotencyRequestService = new IdempotencyRequestService(
                idempotencyRequestRepository,
                paymentRepository,
                CLOCK);
    }

    @Test
    void shouldRegisterNewOrderCreationRequest() {
        when(idempotencyRequestRepository
                .findByOperationAndIdempotencyKey(
                        IdempotencyOperation.ORDER_CREATION,
                        IDEMPOTENCY_KEY
                ))
                .thenReturn(Optional.empty());

        when(idempotencyRequestRepository.saveAndFlush(any()))
                .thenAnswer(invocation -> {
                    IdempotencyRequest request = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            request,
                            "idIdempotencyRequest",
                            REQUEST_ID
                    );

                    return request;
                });

        IdempotencyRequestResult result =
                idempotencyRequestService.startOrderCreation(
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        USER_ID
                );

        assertThat(result.requestId()).isEqualTo(REQUEST_ID);
        assertThat(result.resolution()).isEqualTo(IdempotencyResolution.NEW);
        assertThat(result.resourceId()).isNull();
        assertThat(result.checkoutRequest()).isNull();
        assertThat(result.checkoutUrl()).isNull();

        verify(idempotencyRequestRepository)
                .findByOperationAndIdempotencyKey(
                        IdempotencyOperation.ORDER_CREATION,
                        IDEMPOTENCY_KEY
                );

        verify(idempotencyRequestRepository)
                .saveAndFlush(any(IdempotencyRequest.class));
    }

    @Test
    void shouldReturnInProgressForExistingRequest() {
        IdempotencyRequest request = createProcessingRequest(
                REQUEST_HASH,
                USER_ID
        );

        when(idempotencyRequestRepository
                .findByOperationAndIdempotencyKey(
                        IdempotencyOperation.ORDER_CREATION,
                        IDEMPOTENCY_KEY
                ))
                .thenReturn(Optional.of(request));

        IdempotencyRequestResult result =
                idempotencyRequestService.startOrderCreation(
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        USER_ID
                );

        assertThat(result.requestId()).isEqualTo(REQUEST_ID);
        assertThat(result.resolution())
                .isEqualTo(IdempotencyResolution.IN_PROGRESS);
        assertThat(result.resourceId()).isNull();
        assertThat(result.checkoutRequest()).isNull();
        assertThat(result.checkoutUrl()).isNull();

        verify(idempotencyRequestRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void shouldRejectExistingKeyUsedWithDifferentRequestData() {
        IdempotencyRequest request = createProcessingRequest(
                REQUEST_HASH,
                USER_ID);

        when(idempotencyRequestRepository
                .findByOperationAndIdempotencyKey(
                        IdempotencyOperation.ORDER_CREATION,
                        IDEMPOTENCY_KEY
                ))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() ->
                idempotencyRequestService.startOrderCreation(
                        IDEMPOTENCY_KEY,
                        OTHER_REQUEST_HASH,
                        USER_ID
                )
        )
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessage(
                        "Idempotency-Key został już użyty dla innych danych żądania"
                );

        verify(idempotencyRequestRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void shouldRejectExistingKeyUsedByAnotherUser() {
        IdempotencyRequest request = createProcessingRequest(
                REQUEST_HASH,
                USER_ID
        );

        when(idempotencyRequestRepository
                .findByOperationAndIdempotencyKey(
                        IdempotencyOperation.ORDER_CREATION,
                        IDEMPOTENCY_KEY
                ))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() ->
                idempotencyRequestService.startOrderCreation(
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        999L
                )
        )
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessage(
                        "Idempotency-Key został już użyty przez innego użytkownika"
                );

        verify(idempotencyRequestRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void shouldReturnCreatedResourceForRequestAwaitingCompletion() {
        PaymentCheckoutRequest checkoutRequest = createCheckoutRequest();

        IdempotencyRequest request = createProcessingRequest(
                REQUEST_HASH,
                USER_ID);

        request.markResourceCreated(
                ORDER_ID,
                checkoutRequest,
                CLOCK);

        givenPaymentWithStripeCheckoutIdempotencyKey();

        when(idempotencyRequestRepository
                .findByOperationAndIdempotencyKey(
                        IdempotencyOperation.ORDER_CREATION,
                        IDEMPOTENCY_KEY
                ))
                .thenReturn(Optional.of(request));

        IdempotencyRequestResult result =
                idempotencyRequestService.startOrderCreation(
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        USER_ID
                );

        assertThat(result.requestId()).isEqualTo(REQUEST_ID);
        assertThat(result.resolution())
                .isEqualTo(IdempotencyResolution.RESOURCE_CREATED);
        assertThat(result.resourceId()).isEqualTo(ORDER_ID);
        assertThat(result.checkoutRequest()).isEqualTo(checkoutRequest);
        assertThat(result.checkoutUrl()).isNull();
    }

    @Test
    void shouldReturnCompletedStoredResult() {
        PaymentCheckoutRequest checkoutRequest = createCheckoutRequest();

        IdempotencyRequest request = createProcessingRequest(
                REQUEST_HASH,
                USER_ID);

        request.markResourceCreated(
                ORDER_ID,
                checkoutRequest,
                CLOCK);

        givenPaymentWithStripeCheckoutIdempotencyKey();

        request.markCompleted(
                CHECKOUT_URL,
                CLOCK);

        when(idempotencyRequestRepository
                .findByOperationAndIdempotencyKey(
                        IdempotencyOperation.ORDER_CREATION,
                        IDEMPOTENCY_KEY
                ))
                .thenReturn(Optional.of(request));

        IdempotencyRequestResult result = idempotencyRequestService.startOrderCreation(
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        USER_ID);

        assertThat(result.requestId()).isEqualTo(REQUEST_ID);
        assertThat(result.resolution())
                .isEqualTo(IdempotencyResolution.COMPLETED);
        assertThat(result.resourceId()).isEqualTo(ORDER_ID);
        assertThat(result.checkoutRequest()).isEqualTo(checkoutRequest);
        assertThat(result.checkoutUrl()).isEqualTo(CHECKOUT_URL);
    }

    @Test
    void shouldMarkResourceAsCreated() {
        IdempotencyRequest request = createProcessingRequest(
                REQUEST_HASH,
                USER_ID
        );

        PaymentCheckoutRequest checkoutRequest = createCheckoutRequest();

        when(idempotencyRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        idempotencyRequestService.markResourceCreated(
                REQUEST_ID,
                ORDER_ID,
                checkoutRequest
        );

        assertThat(request.getStatus())
                .isEqualTo(IdempotencyStatus.RESOURCE_CREATED);
        assertThat(request.getResourceId()).isEqualTo(ORDER_ID);
        assertThat(request.getPaymentId())
                .isEqualTo(checkoutRequest.paymentId());

        InOrder inOrder = inOrder(idempotencyRequestRepository);

        inOrder.verify(idempotencyRequestRepository)
                .findById(REQUEST_ID);

        inOrder.verify(idempotencyRequestRepository)
                .save(request);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldMarkRequestAsCompleted() {
        IdempotencyRequest request = createProcessingRequest(
                REQUEST_HASH,
                USER_ID
        );

        request.markResourceCreated(
                ORDER_ID,
                createCheckoutRequest(),
                CLOCK
        );

        when(idempotencyRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(request));

        idempotencyRequestService.markCompleted(
                REQUEST_ID,
                CHECKOUT_URL
        );

        assertThat(request.getStatus())
                .isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(request.getCheckoutUrl()).isEqualTo(CHECKOUT_URL);
        assertThat(request.getCompletedAt()).isNotNull();

        verify(idempotencyRequestRepository)
                .findById(REQUEST_ID);

        verify(idempotencyRequestRepository)
                .save(request);
    }

    @Test
    void shouldWrapConcurrentRegistrationCollision() {
        when(idempotencyRequestRepository
                .findByOperationAndIdempotencyKey(
                        IdempotencyOperation.ORDER_CREATION,
                        IDEMPOTENCY_KEY
                ))
                .thenReturn(Optional.empty());

        DataIntegrityViolationException databaseException =
                new DataIntegrityViolationException(
                        "Duplicate idempotency key"
                );

        when(idempotencyRequestRepository.saveAndFlush(any()))
                .thenThrow(databaseException);

        assertThatThrownBy(() ->
                idempotencyRequestService.startOrderCreation(
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        USER_ID
                )
        )
                .isInstanceOf(IdempotencyRequestCollisionException.class)
                .hasMessage(
                        "Idempotency-Key został równocześnie użyty przez inne żądanie"
                )
                .hasCause(databaseException);
    }

    private IdempotencyRequest createProcessingRequest(
            String requestHash,
            Long ownerUserId
    ) {
        IdempotencyRequest request = IdempotencyRequest.start(
                IdempotencyOperation.ORDER_CREATION,
                IDEMPOTENCY_KEY,
                requestHash,
                ownerUserId,
                CLOCK
        );

        ReflectionTestUtils.setField(
                request,
                "idIdempotencyRequest",
                REQUEST_ID
        );

        return request;
    }

    private PaymentCheckoutRequest createCheckoutRequest() {
        return new PaymentCheckoutRequest(
                40L,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                STRIPE_CHECKOUT_IDEMPOTENCY_KEY,
                new BigDecimal("80.00"),
                "PLN",
                "Strzyżenie"
        );
    }

    private void givenPaymentWithStripeCheckoutIdempotencyKey() {
        Payment payment = Payment.builder()
                .idPayment(PAYMENT_ID)
                .stripeCheckoutIdempotencyKey(
                        STRIPE_CHECKOUT_IDEMPOTENCY_KEY
                )
                .build();

        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.of(payment));
    }
}