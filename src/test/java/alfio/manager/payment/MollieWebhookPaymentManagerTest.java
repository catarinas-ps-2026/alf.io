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
import static alfio.model.transaction.StaticPaymentMethods.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.manager.PurchaseContextManager;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.model.*;
import alfio.model.transaction.*;
import alfio.model.transaction.webhook.MollieWebhookPayload;
import alfio.repository.TicketRepository;
import alfio.repository.TicketReservationRepository;
import alfio.repository.TransactionRepository;
import alfio.test.util.TestUtil;
import java.net.http.HttpClient;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MollieWebhookPaymentManagerTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private TicketReservationRepository ticketReservationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MollieConnectManager mollieConnectManager;

    @Mock
    private PurchaseContextManager purchaseContextManager;

    @Mock
    private PaymentContext paymentContext;

    @Mock
    private PurchaseContext purchaseContext;

    private MollieWebhookPaymentManager manager;

    @BeforeEach
    void setUp() {
        manager = new MollieWebhookPaymentManager(
                httpClient,
                configurationManager,
                ticketReservationRepository,
                ticketRepository,
                transactionRepository,
                mollieConnectManager,
                TestUtil.clockProvider(),
                purchaseContextManager);
    }

    @SuppressWarnings("unchecked")
    private void setupActiveConfig() {
        var configMap = mock(Map.class);
        lenient()
                .when(configurationManager.getFor(eq(MollieWebhookPaymentManager.ALL_OPTIONS), any()))
                .thenReturn(configMap);
        var ccEnabled = mock(MaybeConfiguration.class);
        lenient().when(ccEnabled.getValueAsBooleanOrDefault()).thenReturn(true);
        var baseUrl = mock(MaybeConfiguration.class);
        lenient().when(baseUrl.isPresent()).thenReturn(true);
        var apiKey = mock(MaybeConfiguration.class);
        lenient().when(apiKey.isPresent()).thenReturn(true);
        var platformMode = mock(MaybeConfiguration.class);
        lenient().when(platformMode.getValueAsBooleanOrDefault()).thenReturn(false);
        lenient().when(configMap.get(MOLLIE_CC_ENABLED)).thenReturn(ccEnabled);
        lenient().when(configMap.get(BASE_URL)).thenReturn(baseUrl);
        lenient().when(configMap.get(MOLLIE_API_KEY)).thenReturn(apiKey);
        lenient().when(configMap.get(PLATFORM_MODE_ENABLED)).thenReturn(platformMode);
    }

    @SuppressWarnings("unchecked")
    private void setupInactiveConfig() {
        var configMap = mock(Map.class);
        lenient()
                .when(configurationManager.getFor(eq(MollieWebhookPaymentManager.ALL_OPTIONS), any()))
                .thenReturn(configMap);
        var ccDisabled = mock(MaybeConfiguration.class);
        lenient().when(ccDisabled.getValueAsBooleanOrDefault()).thenReturn(false);
        lenient().when(configMap.get(MOLLIE_CC_ENABLED)).thenReturn(ccDisabled);
        lenient().when(configMap.get(BASE_URL)).thenReturn(ccDisabled);
        lenient().when(configMap.get(MOLLIE_API_KEY)).thenReturn(ccDisabled);
        lenient().when(configMap.get(PLATFORM_MODE_ENABLED)).thenReturn(ccDisabled);
    }

    @Test
    void getPaymentProxy() {
        assertEquals(PaymentProxy.MOLLIE, manager.getPaymentProxy());
    }

    @Test
    void acceptForMollieProxy() {
        var mollieTransaction = mock(Transaction.class);
        when(mollieTransaction.getPaymentProxy()).thenReturn(PaymentProxy.MOLLIE);
        assertTrue(manager.accept(mollieTransaction));

        var stripeTransaction = mock(Transaction.class);
        when(stripeTransaction.getPaymentProxy()).thenReturn(PaymentProxy.STRIPE);
        assertFalse(manager.accept(stripeTransaction));
    }

    @Test
    void doPaymentThrowsIllegalState() {
        var spec = mock(PaymentSpecification.class);
        assertThrows(IllegalStateException.class, () -> manager.doPayment(spec));
    }

    @Test
    void requiresSignedBodyReturnsFalse() {
        assertFalse(manager.requiresSignedBody());
    }

    @Test
    void parseTransactionPayloadReturnsEmptyOnInvalidBody() {
        var result = manager.parseTransactionPayload("invalid body", null, Map.of(), paymentContext);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseTransactionPayloadReturnsPayloadWithId() {
        var additionalInfo = Map.of(
                MollieWebhookPaymentManager.ADDITIONAL_INFO_PURCHASE_CONTEXT_TYPE, "event",
                MollieWebhookPaymentManager.ADDITIONAL_INFO_PURCHASE_IDENTIFIER, "event-abc",
                MollieWebhookPaymentManager.ADDITIONAL_INFO_RESERVATION_ID, "res-123");
        var result = manager.parseTransactionPayload("id=tr_abc123", null, additionalInfo, paymentContext);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof MollieWebhookPayload);
    }

    @Test
    void parseTransactionPayloadReturnsEmptyOnMissingId() {
        var result = manager.parseTransactionPayload("other=value", null, Map.of(), paymentContext);
        assertTrue(result.isEmpty());
    }

    @Test
    void isActiveWhenConfigured() {
        setupActiveConfig();
        when(paymentContext.getConfigurationLevel()).thenReturn(alfio.manager.system.ConfigurationLevel.system());

        assertTrue(manager.isActive(paymentContext));
    }

    @Test
    void isActiveWhenDisabled() {
        setupInactiveConfig();
        when(paymentContext.getConfigurationLevel()).thenReturn(alfio.manager.system.ConfigurationLevel.system());

        assertFalse(manager.isActive(paymentContext));
    }

    @Test
    void getPaymentMethodForTransactionReturnsNull() {
        var transaction = mock(Transaction.class);
        assertNull(manager.getPaymentMethodForTransaction(transaction));
    }
}
