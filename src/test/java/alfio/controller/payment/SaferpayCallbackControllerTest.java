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
import static org.mockito.Mockito.*;

import alfio.manager.PurchaseContextManager;
import alfio.manager.TicketReservationManager;
import alfio.model.PurchaseContext;
import alfio.model.TicketReservation;
import alfio.model.transaction.TransactionInitializationToken;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaferpayCallbackControllerTest {

    @Mock
    private TicketReservationManager ticketReservationManager;

    @Mock
    private PurchaseContextManager purchaseContextManager;

    @Mock
    private PurchaseContext purchaseContext;

    @Mock
    private TicketReservation ticketReservation;

    private SaferpayCallbackController controller;

    private static final String RESERVATION_ID = "res-123";
    private static final String PURCHASE_CONTEXT_ID = "event-abc";

    @BeforeEach
    void setUp() {
        controller = new SaferpayCallbackController(ticketReservationManager, purchaseContextManager);
        lenient().when(purchaseContext.getType()).thenReturn(PurchaseContext.PurchaseContextType.event);
        lenient().when(purchaseContext.getPublicIdentifier()).thenReturn(PURCHASE_CONTEXT_ID);
        lenient().when(ticketReservation.getId()).thenReturn(RESERVATION_ID);
    }

    @Test
    void saferpayCancelWithTransaction() {
        when(purchaseContextManager.findBy(PurchaseContext.PurchaseContextType.event, PURCHASE_CONTEXT_ID))
                .thenAnswer(inv -> Optional.of(purchaseContext));
        when(ticketReservationManager.findById(RESERVATION_ID)).thenReturn(Optional.of(ticketReservation));
        when(ticketReservationManager.forceTransactionCheck(purchaseContext, ticketReservation))
                .thenAnswer(inv -> Optional.of(mock(TransactionInitializationToken.class)));

        String result = controller.saferpayCancel(
                PurchaseContext.PurchaseContextType.event, PURCHASE_CONTEXT_ID, RESERVATION_ID);

        assertTrue(result.startsWith("redirect:"));
        assertTrue(result.contains("event"));
        assertTrue(result.contains(PURCHASE_CONTEXT_ID));
        assertTrue(result.contains(RESERVATION_ID));
    }

    @Test
    void saferpayCancelWithoutTransaction() {
        when(purchaseContextManager.findBy(PurchaseContext.PurchaseContextType.event, PURCHASE_CONTEXT_ID))
                .thenAnswer(inv -> Optional.of(purchaseContext));
        when(ticketReservationManager.findById(RESERVATION_ID)).thenReturn(Optional.of(ticketReservation));
        when(ticketReservationManager.forceTransactionCheck(purchaseContext, ticketReservation))
                .thenReturn(Optional.empty());

        String result = controller.saferpayCancel(
                PurchaseContext.PurchaseContextType.event, PURCHASE_CONTEXT_ID, RESERVATION_ID);

        assertTrue(result.startsWith("redirect:/"));
        assertTrue(result.contains(PURCHASE_CONTEXT_ID));
        assertFalse(result.contains(RESERVATION_ID));
    }

    @Test
    void saferpayCancelMissingPurchaseContext() {
        when(purchaseContextManager.findBy(PurchaseContext.PurchaseContextType.event, PURCHASE_CONTEXT_ID))
                .thenReturn(Optional.empty());

        String result = controller.saferpayCancel(
                PurchaseContext.PurchaseContextType.event, PURCHASE_CONTEXT_ID, RESERVATION_ID);

        assertEquals("redirect:/", result);
        verifyNoInteractions(ticketReservationManager);
    }

    @Test
    void saferpayCancelMissingReservation() {
        when(purchaseContextManager.findBy(PurchaseContext.PurchaseContextType.event, PURCHASE_CONTEXT_ID))
                .thenAnswer(inv -> Optional.of(purchaseContext));
        when(ticketReservationManager.findById(RESERVATION_ID)).thenReturn(Optional.empty());

        String result = controller.saferpayCancel(
                PurchaseContext.PurchaseContextType.event, PURCHASE_CONTEXT_ID, RESERVATION_ID);

        assertTrue(result.startsWith("redirect:/"));
        assertTrue(result.contains(PURCHASE_CONTEXT_ID));
        assertFalse(result.contains(RESERVATION_ID));
    }
}
