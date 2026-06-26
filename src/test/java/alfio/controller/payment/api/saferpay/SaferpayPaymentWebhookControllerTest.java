/**
 * This file is part of alf.io.
 *
 * alf.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * alf.io is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with alf.io.  If not, see <http://www.gnu.org/licenses/>.
 */
package alfio.controller.payment.api.saferpay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.manager.PurchaseContextManager;
import alfio.manager.TicketReservationManager;
import alfio.manager.support.PaymentWebhookResult;
import alfio.model.PurchaseContext;
import alfio.model.transaction.PaymentProxy;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class SaferpayPaymentWebhookControllerTest {

    @Mock
    private TicketReservationManager ticketReservationManager;

    @Mock
    private PurchaseContextManager purchaseContextManager;

    @Mock
    private PurchaseContext purchaseContext;

    private SaferpayPaymentWebhookController controller;

    private static final String RESERVATION_ID = "res-123";

    @BeforeEach
    void setUp() {
        controller = new SaferpayPaymentWebhookController(ticketReservationManager, purchaseContextManager);
        lenient().when(purchaseContext.getType()).thenReturn(PurchaseContext.PurchaseContextType.event);
        lenient().when(purchaseContext.getPublicIdentifier()).thenReturn("event-abc");
    }

    @Test
    void handleTransactionNotificationSuccess() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.of(purchaseContext));
        when(ticketReservationManager.processTransactionWebhook(
                        anyString(), isNull(), eq(PaymentProxy.SAFERPAY), anyMap(), any()))
                .thenReturn(PaymentWebhookResult.successful(null));

        var result = controller.handleTransactionNotification(RESERVATION_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("OK", result.getBody());
    }

    @Test
    void handleTransactionNotificationMissingReservation() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.empty());

        var result = controller.handleTransactionNotification(RESERVATION_ID);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("NOK", result.getBody());
        verifyNoInteractions(ticketReservationManager);
    }

    @Test
    void handleTransactionNotificationError() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.of(purchaseContext));
        when(ticketReservationManager.processTransactionWebhook(
                        anyString(), isNull(), eq(PaymentProxy.SAFERPAY), anyMap(), any()))
                .thenReturn(PaymentWebhookResult.error("webhook processing failed"));

        var result = controller.handleTransactionNotification(RESERVATION_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("webhook processing failed", result.getBody());
    }

    @Test
    void handleTransactionNotificationIntermediateState() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.of(purchaseContext));
        when(ticketReservationManager.processTransactionWebhook(
                        anyString(), isNull(), eq(PaymentProxy.SAFERPAY), anyMap(), any()))
                .thenReturn(PaymentWebhookResult.pending());

        var result = controller.handleTransactionNotification(RESERVATION_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }
}
