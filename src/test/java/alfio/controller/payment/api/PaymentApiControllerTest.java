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
package alfio.controller.payment.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.manager.PaymentManager;
import alfio.manager.PurchaseContextManager;
import alfio.manager.TicketReservationManager;
import alfio.manager.support.PaymentResult;
import alfio.model.PurchaseContext;
import alfio.model.TicketReservation;
import alfio.model.transaction.PaymentMethod;
import alfio.model.transaction.TransactionInitializationToken;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;

@ExtendWith(MockitoExtension.class)
class PaymentApiControllerTest {

    @Mock
    private PaymentManager paymentManager;

    @Mock
    private TicketReservationManager ticketReservationManager;

    @Mock
    private PurchaseContextManager purchaseContextManager;

    @Mock
    private PurchaseContext purchaseContext;

    @Mock
    private TicketReservation ticketReservation;

    private PaymentApiController controller;

    private static final String RESERVATION_ID = "res-123";

    @BeforeEach
    void setUp() {
        controller = new PaymentApiController(paymentManager, ticketReservationManager, purchaseContextManager);
        lenient()
                .when(purchaseContextManager.findByReservationId(RESERVATION_ID))
                .thenReturn(Optional.of(purchaseContext));
        lenient().when(ticketReservationManager.findById(RESERVATION_ID)).thenReturn(Optional.of(ticketReservation));
    }

    @Test
    void initTransactionSuccess() {
        var token = mock(TransactionInitializationToken.class);
        when(ticketReservationManager.initTransaction(
                        eq(purchaseContext), eq(RESERVATION_ID), any(PaymentMethod.class), any()))
                .thenReturn(Optional.of(token));

        var params = new LinkedMultiValueMap<String, String>();
        var result = controller.initTransaction(RESERVATION_ID, "CREDIT_CARD", params);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(token, result.getBody());
    }

    @Test
    void initTransactionInvalidMethod() {
        var params = new LinkedMultiValueMap<String, String>();
        var result = controller.initTransaction(RESERVATION_ID, "INVALID_METHOD", params);

        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    void initTransactionReservationNotFound() {
        when(purchaseContextManager.findByReservationId("unknown")).thenReturn(Optional.empty());

        var params = new LinkedMultiValueMap<String, String>();
        var result = controller.initTransaction("unknown", "CREDIT_CARD", params);

        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    void getTransactionStatusSuccess() {
        var paymentResult = PaymentResult.successful("capture-123");
        when(paymentManager.getTransactionStatus(eq(ticketReservation), any(PaymentMethod.class)))
                .thenReturn(Optional.of(paymentResult));

        var result = controller.getTransactionStatus(RESERVATION_ID, "CREDIT_CARD");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(paymentResult, result.getBody());
    }
}
