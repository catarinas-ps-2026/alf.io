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
package alfio.controller.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import alfio.manager.PurchaseContextManager;
import alfio.manager.TicketReservationManager;
import alfio.manager.payment.PayPalManager;
import alfio.model.PurchaseContext;
import alfio.model.TicketReservation;
import alfio.model.transaction.token.PayPalToken;
import alfio.util.TemplateManager;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayPalCallbackControllerTest {

    @Mock
    private PurchaseContextManager purchaseContextManager;

    @Mock
    private TicketReservationManager ticketReservationManager;

    @Mock
    private PayPalManager payPalManager;

    @Mock
    private TemplateManager templateManager;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PurchaseContext purchaseContext;

    @Mock
    private TicketReservation ticketReservation;

    private PayPalCallbackController controller;

    private static final String RESERVATION_ID = "res-123";
    private static final String PURCHASE_CONTEXT_ID = "event-abc";
    private static final String HMAC = "test-hmac-value";

    @BeforeEach
    void setUp() throws Exception {
        controller = new PayPalCallbackController(
                purchaseContextManager, ticketReservationManager, payPalManager, templateManager);
        lenient().when(purchaseContext.getType()).thenReturn(PurchaseContext.PurchaseContextType.event);
        lenient().when(purchaseContext.getPublicIdentifier()).thenReturn(PURCHASE_CONTEXT_ID);
        lenient().when(ticketReservation.getId()).thenReturn(RESERVATION_ID);
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void payPalSuccessSavesToken() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.of(purchaseContext));
        when(ticketReservationManager.findById(RESERVATION_ID))
                .thenReturn(Optional.of(ticketReservation));

        String result = controller.payPalSuccess(
                PurchaseContext.PurchaseContextType.event,
                PURCHASE_CONTEXT_ID,
                RESERVATION_ID,
                "pay-123",
                "payer-456",
                HMAC);

        verify(payPalManager).saveToken(
                eq(RESERVATION_ID),
                eq(purchaseContext),
                any(PayPalToken.class));
        assertTrue(result.contains("/overview"));
        assertTrue(result.contains("event"));
        assertTrue(result.contains(PURCHASE_CONTEXT_ID));
    }

    @Test
    void payPalSuccessMissingPayerID() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.of(purchaseContext));
        when(ticketReservationManager.findById(RESERVATION_ID))
                .thenReturn(Optional.of(ticketReservation));

        String result = controller.payPalSuccess(
                PurchaseContext.PurchaseContextType.event,
                PURCHASE_CONTEXT_ID,
                RESERVATION_ID,
                "pay-123",
                null,
                HMAC);

        verify(payPalManager).removeToken(ticketReservation, "pay-123");
        verify(payPalManager, never()).saveToken(any(), any(), any());
        assertTrue(result.contains("/overview"));
    }

    @Test
    void payPalSuccessMissingPurchaseContext() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.empty());

        String result = controller.payPalSuccess(
                PurchaseContext.PurchaseContextType.event,
                PURCHASE_CONTEXT_ID,
                RESERVATION_ID,
                "pay-123",
                "payer-456",
                HMAC);

        assertEquals("redirect:/", result);
        verifyNoInteractions(ticketReservationManager, payPalManager);
    }

    @Test
    void payPalCancelRemovesToken() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.of(purchaseContext));
        when(ticketReservationManager.findById(RESERVATION_ID))
                .thenReturn(Optional.of(ticketReservation));

        String result = controller.payPalCancel(
                PurchaseContext.PurchaseContextType.event,
                PURCHASE_CONTEXT_ID,
                RESERVATION_ID,
                "pay-123");

        verify(payPalManager).removeToken(ticketReservation, "pay-123");
        assertTrue(result.contains("/overview"));
        assertTrue(result.contains(PURCHASE_CONTEXT_ID));
    }

    @Test
    void payPalCancelMissingPurchaseContext() {
        when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.empty());

        String result = controller.payPalCancel(
                PurchaseContext.PurchaseContextType.event,
                PURCHASE_CONTEXT_ID,
                RESERVATION_ID,
                "pay-123");

        assertEquals("redirect:/", result);
        verifyNoInteractions(ticketReservationManager, payPalManager);
    }
}
