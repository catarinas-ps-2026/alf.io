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
package alfio.manager.payment;

import static alfio.model.system.ConfigurationKeys.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.model.Event;
import alfio.model.PurchaseContext;
import alfio.model.system.ConfigurationKeys;
import alfio.model.transaction.PaymentContext;
import alfio.model.transaction.PaymentProxy;
import alfio.model.transaction.StaticPaymentMethods;
import alfio.model.transaction.Transaction;
import alfio.repository.TicketRepository;
import alfio.repository.TicketReservationRepository;
import alfio.repository.TransactionRepository;
import alfio.test.util.TestUtil;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaferpayManagerTest {

    private static final String CAPTURE_RESPONSE_BODY =
            """
        {
          "ResponseHeader": {
            "SpecVersion": "[current Spec-Version]",
            "RequestId": "[your request id]"
          },
          "CaptureId": "captureId",
          "Status": "CAPTURED",
          "Date": "2015-01-30T12:45:22.258+01:00"
        }\
        """;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TicketReservationRepository ticketReservationRepository;

    @Mock
    private HttpClient httpClient;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private Event event;

    @Mock
    private PaymentContext paymentContext;

    @Mock
    private ConfigurationManager.MaybeConfiguration maybeConfiguration;

    private SaferpayManager manager;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {
        var configurationManager = mock(ConfigurationManager.class);
        var configuration = mock(Map.class);
        lenient().when(configurationManager.getFor(
                        eq(EnumSet.of(
                                SAFERPAY_ENABLED,
                                SAFERPAY_API_USERNAME,
                                SAFERPAY_API_PASSWORD,
                                SAFERPAY_CUSTOMER_ID,
                                SAFERPAY_TERMINAL_ID,
                                SAFERPAY_LIVE_MODE,
                                BASE_URL,
                                RESERVATION_TIMEOUT)),
                        any()))
                .thenReturn(configuration);
        lenient().when(maybeConfiguration.getRequiredValue()).thenReturn("");
        lenient().when(configuration.get(any(ConfigurationKeys.class))).thenReturn(maybeConfiguration);
        lenient().when(paymentContext.getPurchaseContext()).thenReturn(event);
        manager = new SaferpayManager(
                configurationManager,
                httpClient,
                ticketReservationRepository,
                transactionRepository,
                ticketRepository,
                TestUtil.clockProvider());
    }

    @Test
    void internalProcessCapturedWebhook() throws IOException, InterruptedException {
        var transaction = mock(Transaction.class);
        when(transaction.getId()).thenReturn(2);
        @SuppressWarnings("unchecked")
        HttpResponse<Object> response = mock(HttpResponse.class);
        when(httpClient.send(any(), any())).thenReturn(response);
        when(response.body()).thenReturn(getJsonBody(true));
        when(response.statusCode()).thenReturn(200);
        var webhookResult = manager.internalProcessWebhook(transaction, paymentContext);
        assertTrue(webhookResult.isSuccessful());
        verify(transactionRepository)
                .update(
                        eq(2),
                        eq("transactionId"),
                        eq("captureId"),
                        any(),
                        eq(0L),
                        eq(0L),
                        eq(Transaction.Status.COMPLETE),
                        anyMap());
        verify(httpClient).send(any(), any());
    }

    @Test
    void internalProcessAuthorizedWebhook() throws IOException, InterruptedException {
        var transaction = mock(Transaction.class);
        when(transaction.getId()).thenReturn(2);
        @SuppressWarnings("unchecked")
        HttpResponse<Object> response = mock(HttpResponse.class);
        when(httpClient.send(any(), any())).thenReturn(response);
        when(response.body()).thenReturn(getJsonBody(false), CAPTURE_RESPONSE_BODY);
        when(response.statusCode()).thenReturn(200);
        var webhookResult = manager.internalProcessWebhook(transaction, paymentContext);
        assertTrue(webhookResult.isSuccessful());
        verify(transactionRepository)
                .update(
                        eq(2),
                        eq("transactionId"),
                        eq("captureId"),
                        any(),
                        eq(0L),
                        eq(0L),
                        eq(Transaction.Status.COMPLETE),
                        anyMap());
        verify(httpClient, times(2)).send(any(), any());
    }

    @Test
    void internalProcessCancelledWebhook() throws IOException, InterruptedException {
        var transaction = mock(Transaction.class);
        when(transaction.getId()).thenReturn(2);
        when(transaction.getReservationId()).thenReturn("reservationId");
        @SuppressWarnings("unchecked")
        HttpResponse<Object> response = mock(HttpResponse.class);
        when(httpClient.send(any(), any())).thenReturn(response);
        when(response.statusCode()).thenReturn(404);
        var webhookResult = manager.internalProcessWebhook(transaction, paymentContext);
        assertFalse(webhookResult.isSuccessful());
        verify(transactionRepository, never())
                .update(anyInt(), any(), any(), any(), anyLong(), anyLong(), any(), anyMap());
        verify(transactionRepository).invalidateById(2);
        verify(ticketReservationRepository).updateValidity(eq("reservationId"), any());
        verify(httpClient).send(any(), any());
    }

    @Test
    void saferpayOffline() throws IOException, InterruptedException {
        var transaction = mock(Transaction.class);
        @SuppressWarnings("unchecked")
        HttpResponse<Object> response = mock(HttpResponse.class);
        when(httpClient.send(any(), any())).thenReturn(response);
        when(response.statusCode()).thenReturn(500);
        assertThrows(IllegalStateException.class, () -> manager.internalProcessWebhook(transaction, paymentContext));
    }

    @Test
    @SuppressWarnings("unchecked")
    void isActiveWhenConfigured() {
        var configurationManager = mock(ConfigurationManager.class);
        var configMap = mock(Map.class);
        when(configurationManager.getFor(
                        eq(EnumSet.of(
                                SAFERPAY_ENABLED,
                                SAFERPAY_API_USERNAME,
                                SAFERPAY_API_PASSWORD,
                                SAFERPAY_CUSTOMER_ID,
                                SAFERPAY_TERMINAL_ID)),
                        any()))
                .thenReturn(configMap);
        var presentConfig = mock(MaybeConfiguration.class);
        when(presentConfig.isPresent()).thenReturn(true);
        when(configMap.values()).thenReturn(java.util.List.of(presentConfig, presentConfig, presentConfig, presentConfig, presentConfig));
        var activeManager = new SaferpayManager(
                configurationManager,
                httpClient,
                ticketReservationRepository,
                transactionRepository,
                ticketRepository,
                TestUtil.clockProvider());
        assertTrue(activeManager.isActive(paymentContext));
    }

    @Test
    @SuppressWarnings("unchecked")
    void isActiveWhenMissingConfig() {
        var configurationManager = mock(ConfigurationManager.class);
        var configMap = mock(Map.class);
        when(configurationManager.getFor(
                        eq(EnumSet.of(
                                SAFERPAY_ENABLED,
                                SAFERPAY_API_USERNAME,
                                SAFERPAY_API_PASSWORD,
                                SAFERPAY_CUSTOMER_ID,
                                SAFERPAY_TERMINAL_ID)),
                        any()))
                .thenReturn(configMap);
        var missingConfig = mock(MaybeConfiguration.class);
        when(missingConfig.isPresent()).thenReturn(false);
        when(configMap.values()).thenReturn(java.util.List.of(missingConfig));
        var incompleteManager = new SaferpayManager(
                configurationManager,
                httpClient,
                ticketReservationRepository,
                transactionRepository,
                ticketRepository,
                TestUtil.clockProvider());
        assertFalse(incompleteManager.isActive(paymentContext));
    }

    @Test
    void acceptForSaferpayProxy() {
        var saferpayTransaction = mock(Transaction.class);
        when(saferpayTransaction.getPaymentProxy()).thenReturn(PaymentProxy.SAFERPAY);
        assertTrue(manager.accept(saferpayTransaction));

        var stripeTransaction = mock(Transaction.class);
        when(stripeTransaction.getPaymentProxy()).thenReturn(PaymentProxy.STRIPE);
        assertFalse(manager.accept(stripeTransaction));
    }

    @Test
    void getSupportedPaymentMethods() {
        var methods = manager.getSupportedPaymentMethods(paymentContext, null);
        assertEquals(EnumSet.of(StaticPaymentMethods.CREDIT_CARD), methods);
    }

    @Test
    void getPaymentProxy() {
        assertEquals(PaymentProxy.SAFERPAY, manager.getPaymentProxy());
    }

    @Test
    void getPaymentMethodForTransaction() {
        var transaction = mock(Transaction.class);
        assertEquals(StaticPaymentMethods.CREDIT_CARD, manager.getPaymentMethodForTransaction(transaction));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getInfoSuccess() throws IOException, InterruptedException {
        var transaction = mock(Transaction.class);
        when(transaction.getTransactionId()).thenReturn("tx-123");
        when(transaction.getReservationId()).thenReturn("res-123");

        @SuppressWarnings("unchecked")
        HttpResponse<Object> response = mock(HttpResponse.class);
        when(httpClient.send(any(), any())).thenReturn(response);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("""
                {
                  "Transaction": {
                    "Amount": {
                      "Value": "1000",
                      "CurrencyCode": "CHF"
                    }
                  }
                }
                """);

        var result = manager.getInfo(transaction, event);
        assertTrue(result.isPresent());
        verify(httpClient).send(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getInfoHttpError() throws IOException, InterruptedException {
        var transaction = mock(Transaction.class);
        when(transaction.getTransactionId()).thenReturn("tx-123");
        when(transaction.getReservationId()).thenReturn("res-123");

        @SuppressWarnings("unchecked")
        HttpResponse<Object> response = mock(HttpResponse.class);
        when(httpClient.send(any(), any())).thenReturn(response);
        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("error");

        var result = manager.getInfo(transaction, event);
        assertTrue(result.isEmpty());
    }

    // @formatter:off
    private String getJsonBody(boolean captured) {
        return "{" + "\"Transaction\": {"
                + "\"Status\": \""
                + (captured ? "CAPTURED" : "AUTHORIZED") + "\"," + "\"Id\": \"transactionId\""
                + (captured ? "," : "")
                + (captured ? "\"CaptureId\": \"captureId\", \"Date\": \"2015-01-30T12:45:22.258+01:00\"" : "")
                + "}"
                + "}";
    }
    // @formatter:on

}
