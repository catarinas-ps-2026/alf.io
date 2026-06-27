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
package alfio.controller.payment.api.stripe;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.manager.TicketReservationManager;
import alfio.manager.support.PaymentWebhookResult;
import alfio.model.transaction.PaymentProxy;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class StripePaymentWebhookControllerTest {

    @Mock
    private TicketReservationManager ticketReservationManager;

    @Mock
    private HttpServletRequest request;

    private StripePaymentWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new StripePaymentWebhookController(ticketReservationManager);
    }

    private void mockInputStream(String content) throws IOException {
        var inputStream = new ServletInputStream() {
            private final ByteArrayInputStream delegate =
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            @Override
            public int read() {
                return delegate.read();
            }

            @Override
            public boolean isFinished() {
                return delegate.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {}
        };
        when(request.getInputStream()).thenReturn(inputStream);
    }

    @Test
    void receivePaymentConfirmationSuccess() throws Exception {
        var content = "{\"id\":\"evt_123\",\"type\":\"payment_intent.succeeded\"}";
        mockInputStream(content);
        when(ticketReservationManager.processTransactionWebhook(
                        anyString(), eq("sig_test"), eq(PaymentProxy.STRIPE), anyMap()))
                .thenReturn(PaymentWebhookResult.successful(null));

        var result = controller.receivePaymentConfirmation("sig_test", request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("OK", result.getBody());
    }

    @Test
    void receivePaymentConfirmationError() throws Exception {
        var content = "{\"id\":\"evt_456\",\"type\":\"payment_intent.payment_failed\"}";
        mockInputStream(content);
        when(ticketReservationManager.processTransactionWebhook(
                        anyString(), eq("sig_test"), eq(PaymentProxy.STRIPE), anyMap()))
                .thenReturn(PaymentWebhookResult.error("payment failed"));

        var result = controller.receivePaymentConfirmation("sig_test", request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("payment failed", result.getBody());
    }

    @Test
    void receivePaymentConfirmationIntermediateState() throws Exception {
        var content = "{\"id\":\"evt_789\",\"type\":\"payment_intent.processing\"}";
        mockInputStream(content);
        when(ticketReservationManager.processTransactionWebhook(
                        anyString(), eq("sig_test"), eq(PaymentProxy.STRIPE), anyMap()))
                .thenReturn(PaymentWebhookResult.pending());

        var result = controller.receivePaymentConfirmation("sig_test", request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void receivePaymentConfirmationEmptyBody() throws Exception {
        when(request.getInputStream()).thenThrow(new IOException("empty"));

        var result = controller.receivePaymentConfirmation("sig_test", request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Malformed request.", result.getBody());
        verifyNoInteractions(ticketReservationManager);
    }
}
