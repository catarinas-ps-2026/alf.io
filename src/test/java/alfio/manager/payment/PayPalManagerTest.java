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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.model.*;
import alfio.model.system.ConfigurationKeys;
import alfio.model.transaction.*;
import alfio.model.transaction.token.PayPalToken;
import alfio.repository.TicketRepository;
import alfio.repository.TicketReservationRepository;
import alfio.repository.TransactionRepository;
import alfio.test.util.TestUtil;
import alfio.util.Json;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayPalManagerTest {

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private TicketReservationRepository ticketReservationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentContext paymentContext;

    @Mock
    private PurchaseContext purchaseContext;

    @Mock
    private ConfigurationManager.MaybeConfiguration maybeConfig;

    private PayPalManager payPalManager;

    @BeforeEach
    void setUp() {
        payPalManager = new PayPalManager(
                configurationManager,
                ticketReservationRepository,
                ticketRepository,
                transactionRepository,
                new Json(),
                TestUtil.clockProvider());
    }

    @SuppressWarnings("unchecked")
    private void setupConfigForActive() {
        var configMap = mock(Map.class);
        lenient().when(configurationManager.getFor(
                        eq(Set.of(PAYPAL_ENABLED, ConfigurationKeys.PAYPAL_CLIENT_ID, ConfigurationKeys.PAYPAL_CLIENT_SECRET)),
                        any()))
                .thenReturn(configMap);
        var enabledConfig = mock(MaybeConfiguration.class);
        lenient().when(enabledConfig.getValueAsBooleanOrDefault()).thenReturn(true);
        var clientIdConfig = mock(MaybeConfiguration.class);
        lenient().when(clientIdConfig.isPresent()).thenReturn(true);
        var clientSecretConfig = mock(MaybeConfiguration.class);
        lenient().when(clientSecretConfig.isPresent()).thenReturn(true);
        lenient().when(configMap.get(PAYPAL_ENABLED)).thenReturn(enabledConfig);
        lenient().when(configMap.get(ConfigurationKeys.PAYPAL_CLIENT_ID)).thenReturn(clientIdConfig);
        lenient().when(configMap.get(ConfigurationKeys.PAYPAL_CLIENT_SECRET)).thenReturn(clientSecretConfig);
    }

    @SuppressWarnings("unchecked")
    private void setupConfigForInactive() {
        var configMap = mock(Map.class);
        lenient().when(configurationManager.getFor(
                        eq(Set.of(PAYPAL_ENABLED, ConfigurationKeys.PAYPAL_CLIENT_ID, ConfigurationKeys.PAYPAL_CLIENT_SECRET)),
                        any()))
                .thenReturn(configMap);
        var disabledConfig = mock(MaybeConfiguration.class);
        lenient().when(disabledConfig.getValueAsBooleanOrDefault()).thenReturn(false);
        lenient().when(configMap.get(PAYPAL_ENABLED)).thenReturn(disabledConfig);
        lenient().when(configMap.get(ConfigurationKeys.PAYPAL_CLIENT_ID)).thenReturn(disabledConfig);
        lenient().when(configMap.get(ConfigurationKeys.PAYPAL_CLIENT_SECRET)).thenReturn(disabledConfig);
    }

    @Test
    void acceptReturnsTrueWhenConfigured() {
        setupConfigForActive();
        when(paymentContext.getConfigurationLevel()).thenReturn(alfio.manager.system.ConfigurationLevel.system());

        assertTrue(payPalManager.accept(StaticPaymentMethods.PAYPAL, paymentContext, null));
    }

    @Test
    void acceptReturnsFalseWhenMissingConfig() {
        setupConfigForInactive();
        when(paymentContext.getConfigurationLevel()).thenReturn(alfio.manager.system.ConfigurationLevel.system());

        assertFalse(payPalManager.accept(StaticPaymentMethods.PAYPAL, paymentContext, null));
    }

    @Test
    void acceptReturnsFalseForNonPaypalMethod() {
        assertFalse(payPalManager.accept(StaticPaymentMethods.CREDIT_CARD, paymentContext, null));
    }

    @Test
    void isActiveWhenConfigured() {
        setupConfigForActive();
        when(paymentContext.getConfigurationLevel()).thenReturn(alfio.manager.system.ConfigurationLevel.system());

        assertTrue(payPalManager.isActive(paymentContext));
    }

    @Test
    void isActiveWhenDisabled() {
        setupConfigForInactive();
        when(paymentContext.getConfigurationLevel()).thenReturn(alfio.manager.system.ConfigurationLevel.system());

        assertFalse(payPalManager.isActive(paymentContext));
    }

    @Test
    void getSupportedPaymentMethods() {
        var methods = payPalManager.getSupportedPaymentMethods(paymentContext, null);
        assertEquals(EnumSet.of(StaticPaymentMethods.PAYPAL), methods);
    }

    @Test
    void getPaymentProxy() {
        assertEquals(PaymentProxy.PAYPAL, payPalManager.getPaymentProxy());
    }

    @Test
    void getPaymentMethodForTransaction() {
        var transaction = mock(Transaction.class);
        assertEquals(StaticPaymentMethods.PAYPAL, payPalManager.getPaymentMethodForTransaction(transaction));
    }

    @Test
    void acceptForPaypalProxy() {
        var paypalTransaction = mock(Transaction.class);
        when(paypalTransaction.getPaymentProxy()).thenReturn(PaymentProxy.PAYPAL);
        assertTrue(payPalManager.accept(paypalTransaction));

        var stripeTransaction = mock(Transaction.class);
        when(stripeTransaction.getPaymentProxy()).thenReturn(PaymentProxy.STRIPE);
        assertFalse(payPalManager.accept(stripeTransaction));
    }

    @Test
    void saveTokenInsertsTransaction() {
        when(transactionRepository.loadOptionalByReservationId("res-123")).thenReturn(Optional.empty());
        var token = new PayPalToken("payer-456", "pay-789", "hmac-value");

        payPalManager.saveToken("res-123", purchaseContext, token);

        verify(transactionRepository).insert(
                eq("res-123"),
                eq("pay-789"),
                eq("res-123"),
                any(),
                eq(0),
                any(),
                eq("Paypal token"),
                eq(PaymentProxy.PAYPAL.name()),
                eq(0L),
                eq(0L),
                eq(Transaction.Status.PENDING),
                anyMap());
    }

    @Test
    void removeTokenWithMatchingTransaction() {
        var transaction = mock(Transaction.class);
        when(transaction.getPaymentId()).thenReturn("pay-789");
        when(transaction.getPaymentProxy()).thenReturn(PaymentProxy.PAYPAL);
        when(transaction.getStatus()).thenReturn(Transaction.Status.PENDING);
        when(transactionRepository.loadOptionalByReservationId("res-123"))
                .thenReturn(Optional.of(transaction), Optional.empty());
        when(transactionRepository.invalidateForReservation("res-123", PaymentProxy.PAYPAL.name()))
                .thenReturn(1);

        var reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("res-123");

        payPalManager.removeToken(reservation, "pay-789");

        verify(transactionRepository).invalidateForReservation("res-123", PaymentProxy.PAYPAL.name());
    }

    @Test
    void removeTokenWithNonMatchingPaymentId() {
        var transaction = mock(Transaction.class);
        when(transaction.getPaymentId()).thenReturn("other-pay-id");
        when(transactionRepository.loadOptionalByReservationId("res-123"))
                .thenReturn(Optional.of(transaction));

        var reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("res-123");

        payPalManager.removeToken(reservation, "pay-789");

        verify(transactionRepository, never()).invalidateForReservation(any(), any());
    }

    @Test
    void extractTokenReturnsEmptyWhenNoMetadata() {
        var transaction = mock(Transaction.class);
        when(transaction.getMetadata()).thenReturn(Map.of());

        var result = payPalManager.extractToken(transaction);

        assertTrue(result.isEmpty());
    }

    @Test
    void extractTokenReturnsTokenWhenPresent() {
        var transaction = mock(Transaction.class);
        var token = new PayPalToken("payer-456", "pay-789", "hmac-value");
        when(transaction.getMetadata()).thenReturn(Map.of("PAYMENT_TOKEN", new Json().asJsonString(token)));

        var result = payPalManager.extractToken(transaction);

        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof PayPalToken);
    }
}
