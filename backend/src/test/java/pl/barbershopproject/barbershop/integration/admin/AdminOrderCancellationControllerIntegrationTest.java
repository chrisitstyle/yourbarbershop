package pl.barbershopproject.barbershop.integration.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.exception.OrderStatusChangeNotAllowedException;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.order.AdminCancellationResult;
import pl.barbershopproject.barbershop.order.OrderCancellationService;
import pl.barbershopproject.barbershop.payment.refund.RefundResult;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class AdminOrderCancellationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OrderCancellationService orderCancellationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldCancelUnpaidOrderAsAdmin() throws Exception {
        AdminCancellationResult cancellationResult = new AdminCancellationResult(
                7L,
                AdminCancellationResult.ReservationType.REGISTERED,
                OrderStatus.ANULOWANE,
                null);

        when(orderCancellationService.cancelRegisteredOrder(7L))
                .thenReturn(cancellationResult);

        mockMvc.perform(post("/admin/orders/7/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reservationId").value(7))
                .andExpect(jsonPath("$.reservationType").value("REGISTERED"))
                .andExpect(jsonPath("$.orderStatus").value("ANULOWANE"))
                .andExpect(jsonPath("$.refund").doesNotExist());

        verify(orderCancellationService).cancelRegisteredOrder(7L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnAcceptedWhenRefundIsPending() throws Exception {
        AdminCancellationResult cancellationResult = new AdminCancellationResult(
                8L,
                AdminCancellationResult.ReservationType.REGISTERED,
                OrderStatus.ANULOWANE,
                new RefundResult(
                        15L,
                        "re_test_pending",
                        RefundStatus.PENDING));

        when(orderCancellationService.cancelRegisteredOrder(8L))
                .thenReturn(cancellationResult);

        mockMvc.perform(post("/admin/orders/8/cancel"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderStatus").value("ANULOWANE"))
                .andExpect(jsonPath("$.refund.refundId").value(15))
                .andExpect(jsonPath("$.refund.status").value("PENDING"))
                .andExpect(jsonPath("$.refund.stripeRefundId")
                        .value("re_test_pending"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldCancelGuestOrderAsAdmin() throws Exception {
        AdminCancellationResult cancellationResult = new AdminCancellationResult(
                9L,
                AdminCancellationResult.ReservationType.GUEST,
                OrderStatus.ANULOWANE,
                null);

        when(orderCancellationService.cancelGuestOrder(9L))
                .thenReturn(cancellationResult);

        mockMvc.perform(post("/admin/guest-orders/9/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationType").value("GUEST"))
                .andExpect(jsonPath("$.orderStatus").value("ANULOWANE"));

        verify(orderCancellationService).cancelGuestOrder(9L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnAcceptedWhenRefundRequiresReconciliation() throws Exception {

        AdminCancellationResult cancellationResult = new AdminCancellationResult(
                10L,
                AdminCancellationResult.ReservationType.REGISTERED,
                OrderStatus.ANULOWANE,
                new RefundResult(
                        20L,
                        null,
                        RefundStatus.RECONCILIATION_REQUIRED));

        when(orderCancellationService.cancelRegisteredOrder(10L))
                .thenReturn(cancellationResult);

        mockMvc.perform(post("/admin/orders/10/cancel"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderStatus").value("ANULOWANE"))
                .andExpect(jsonPath("$.refund.status")
                        .value("RECONCILIATION_REQUIRED"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        when(orderCancellationService.cancelRegisteredOrder(999L))
                .thenThrow(new NoSuchElementException("Zamówienie o ID: 999 nie istnieje"));

        mockMvc.perform(post("/admin/orders/999/cancel"))
                .andExpect(status().isNotFound());

        verify(orderCancellationService).cancelRegisteredOrder(999L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldRejectAlreadyCancelledOrder() throws Exception {
        when(orderCancellationService.cancelRegisteredOrder(11L))
                .thenThrow(new OrderStatusChangeNotAllowedException("Można anulować tylko aktywną rezerwację"));

        mockMvc.perform(post("/admin/orders/11/cancel"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldRejectCancellationByRegularUser() throws Exception {
        mockMvc.perform(post("/admin/orders/7/cancel"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderCancellationService);
    }

    @Test
    void shouldRejectCancellationByUnauthenticatedUser() throws Exception {
        mockMvc.perform(post("/admin/orders/7/cancel"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderCancellationService);
    }
}
