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

import static alfio.manager.TicketReservationManager.NOT_YET_PAID_TRANSACTION_ID;
import static alfio.model.system.ConfigurationKeys.ON_SITE_ENABLED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import alfio.manager.system.ConfigurationLevel;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.model.Event;
import alfio.model.transaction.*;
import alfio.repository.TransactionRepository;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OnSiteManagerTest {

    @Test
    void onSiteNotAvailableIfEventIsOnline() {
        var configurationManager = mock(ConfigurationManager.class);
        var event = mock(Event.class);
        var cl = ConfigurationLevel.event(event);
        when(event.getConfigurationLevel()).thenReturn(cl);
        when(event.event()).thenReturn(Optional.of(event));
        var configuration = mock(MaybeConfiguration.class);
        when(configurationManager.getFor(eq(ON_SITE_ENABLED), any(ConfigurationLevel.class)))
                .thenReturn(configuration);
        when(configuration.getValueAsBooleanOrDefault()).thenReturn(true);
        var onSiteManager = new OnSiteManager(configurationManager, null);
        when(event.isOnline()).thenReturn(true);
        assertFalse(onSiteManager.accept(StaticPaymentMethods.ON_SITE, new PaymentContext(event), null));
        when(event.isOnline()).thenReturn(false);
        assertTrue(onSiteManager.accept(StaticPaymentMethods.ON_SITE, new PaymentContext(event), null));
    }

    @Test
    void doPaymentReturnsSuccess() {
        var configurationManager = mock(ConfigurationManager.class);
        var transactionRepository = mock(TransactionRepository.class);
        var onSiteManager = new OnSiteManager(configurationManager, transactionRepository);

        var spec = mock(PaymentSpecification.class);
        when(spec.getReservationId()).thenReturn("res-123");

        try (MockedStatic<PaymentManagerUtils> mockedUtils = mockStatic(PaymentManagerUtils.class)) {
            var result = onSiteManager.doPayment(spec);
            assertTrue(result.isSuccessful());
            assertEquals(NOT_YET_PAID_TRANSACTION_ID, result.getGatewayIdOrNull());
            mockedUtils.verify(
                    () -> PaymentManagerUtils.invalidateExistingTransactions("res-123", transactionRepository));
        }
    }

    @Test
    void isActiveWhenOnSiteEnabled() {
        var configurationManager = mock(ConfigurationManager.class);
        var event = mock(Event.class);
        var cl = ConfigurationLevel.event(event);
        when(event.getConfigurationLevel()).thenReturn(cl);
        when(event.event()).thenReturn(Optional.of(event));
        var configuration = mock(MaybeConfiguration.class);
        when(configurationManager.getFor(eq(ON_SITE_ENABLED), any(ConfigurationLevel.class)))
                .thenReturn(configuration);
        when(configuration.getValueAsBooleanOrDefault()).thenReturn(true);
        when(event.isOnline()).thenReturn(false);

        var onSiteManager = new OnSiteManager(configurationManager, null);
        assertTrue(onSiteManager.isActive(new PaymentContext(event)));
    }

    @Test
    void isActiveWhenOnlineEvent() {
        var configurationManager = mock(ConfigurationManager.class);
        var event = mock(Event.class);
        var cl = ConfigurationLevel.event(event);
        when(event.getConfigurationLevel()).thenReturn(cl);
        when(event.event()).thenReturn(Optional.of(event));
        var configuration = mock(MaybeConfiguration.class);
        when(configurationManager.getFor(eq(ON_SITE_ENABLED), any(ConfigurationLevel.class)))
                .thenReturn(configuration);
        when(configuration.getValueAsBooleanOrDefault()).thenReturn(true);
        when(event.isOnline()).thenReturn(true);

        var onSiteManager = new OnSiteManager(configurationManager, null);
        assertFalse(onSiteManager.isActive(new PaymentContext(event)));
    }

    @Test
    void isActiveWhenDisabled() {
        var configurationManager = mock(ConfigurationManager.class);
        var event = mock(Event.class);
        var cl = ConfigurationLevel.event(event);
        when(event.getConfigurationLevel()).thenReturn(cl);
        when(event.event()).thenReturn(Optional.of(event));
        var configuration = mock(MaybeConfiguration.class);
        when(configurationManager.getFor(eq(ON_SITE_ENABLED), any(ConfigurationLevel.class)))
                .thenReturn(configuration);
        when(configuration.getValueAsBooleanOrDefault()).thenReturn(false);

        var onSiteManager = new OnSiteManager(configurationManager, null);
        assertFalse(onSiteManager.isActive(new PaymentContext(event)));
    }

    @Test
    void getSupportedPaymentMethods() {
        var onSiteManager = new OnSiteManager(mock(ConfigurationManager.class), null);
        var methods = onSiteManager.getSupportedPaymentMethods(mock(PaymentContext.class), null);
        assertEquals(EnumSet.of(StaticPaymentMethods.ON_SITE), methods);
    }

    @Test
    void getPaymentProxy() {
        var onSiteManager = new OnSiteManager(mock(ConfigurationManager.class), null);
        assertEquals(PaymentProxy.ON_SITE, onSiteManager.getPaymentProxy());
    }

    @Test
    void acceptForOnSiteProxy() {
        var onSiteManager = new OnSiteManager(mock(ConfigurationManager.class), null);

        var onSiteTransaction = mock(Transaction.class);
        when(onSiteTransaction.getPaymentProxy()).thenReturn(PaymentProxy.ON_SITE);
        assertTrue(onSiteManager.accept(onSiteTransaction));

        var stripeTransaction = mock(Transaction.class);
        when(stripeTransaction.getPaymentProxy()).thenReturn(PaymentProxy.STRIPE);
        assertFalse(onSiteManager.accept(stripeTransaction));
    }
}
