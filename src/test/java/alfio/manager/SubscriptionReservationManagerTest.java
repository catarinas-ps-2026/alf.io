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
package alfio.manager;

import alfio.model.DescriptorIdAndReservationId;
import alfio.model.modification.SubscriptionDescriptorModification;
import alfio.model.subscription.SubscriptionDescriptor;
import alfio.model.system.command.CleanupReservations;
import alfio.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.Mockito.*;

public class SubscriptionReservationManagerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private ExtensionManager extensionManager;

    private SubscriptionReservationManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new SubscriptionReservationManager(subscriptionRepository, extensionManager);
    }

    @Test
    public void testCleanupReservationsWithSubscriptionDescriptorExpired() {
        SubscriptionDescriptor sd = mock(SubscriptionDescriptor.class);
        when(sd.getMaxAvailable()).thenReturn(10);
        UUID sdId = UUID.randomUUID();
        when(sd.getId()).thenReturn(sdId);

        List<String> reservationIds = Arrays.asList("res-1", "res-2");
        CleanupReservations cleanup = new CleanupReservations(sd, reservationIds, true, false, false);

        when(subscriptionRepository.deleteSubscriptionWithReservationId(reservationIds)).thenReturn(2);

        manager.cleanupReservations(cleanup);

        verify(subscriptionRepository).deleteSubscriptionWithReservationId(reservationIds);
        verify(subscriptionRepository).preGenerateSubscriptions(any(SubscriptionDescriptorModification.class), eq(sdId), eq(2));
        verify(extensionManager).handleReservationsExpired(sd, reservationIds);
        verify(extensionManager, never()).handleReservationsCancelled(any(), any());
    }

    @Test
    public void testCleanupReservationsWithSubscriptionDescriptorCancelled() {
        SubscriptionDescriptor sd = mock(SubscriptionDescriptor.class);
        when(sd.getMaxAvailable()).thenReturn(10);
        UUID sdId = UUID.randomUUID();
        when(sd.getId()).thenReturn(sdId);

        List<String> reservationIds = Arrays.asList("res-1", "res-2");
        CleanupReservations cleanup = new CleanupReservations(sd, reservationIds, false, false, false);

        when(subscriptionRepository.deleteSubscriptionWithReservationId(reservationIds)).thenReturn(2);

        manager.cleanupReservations(cleanup);

        verify(subscriptionRepository).deleteSubscriptionWithReservationId(reservationIds);
        verify(subscriptionRepository).preGenerateSubscriptions(any(SubscriptionDescriptorModification.class), eq(sdId), eq(2));
        verify(extensionManager).handleReservationsCancelled(sd, reservationIds);
        verify(extensionManager, never()).handleReservationsExpired(any(), any());
    }

    @Test
    public void testCleanupReservationsNullPurchaseContextNoDescriptors() {
        List<String> reservationIds = Arrays.asList("res-1", "res-2");
        CleanupReservations cleanup = new CleanupReservations(null, reservationIds, false, false, false);

        when(subscriptionRepository.findDescriptorsByReservationIds(reservationIds)).thenReturn(Collections.emptyList());
        when(subscriptionRepository.deleteSubscriptionWithReservationId(reservationIds)).thenReturn(0);

        manager.cleanupReservations(cleanup);

        verify(subscriptionRepository).deleteSubscriptionWithReservationId(reservationIds);
        verify(extensionManager).handleReservationsCancelled(null, reservationIds);
    }

    @Test
    public void testCleanupReservationsNullPurchaseContextWithDescriptors() {
        List<String> reservationIds = Arrays.asList("res-1", "res-2");
        CleanupReservations cleanup = new CleanupReservations(null, reservationIds, false, false, false);

        UUID descId = UUID.randomUUID();
        DescriptorIdAndReservationId descAndRes1 = mock(DescriptorIdAndReservationId.class);
        when(descAndRes1.descriptorId()).thenReturn(descId);
        when(descAndRes1.reservationId()).thenReturn("res-1");
        when(descAndRes1.maxAvailable()).thenReturn(10);
        
        DescriptorIdAndReservationId descAndRes2 = mock(DescriptorIdAndReservationId.class);
        when(descAndRes2.descriptorId()).thenReturn(descId);
        when(descAndRes2.reservationId()).thenReturn("res-2");
        when(descAndRes2.maxAvailable()).thenReturn(10);

        when(subscriptionRepository.findDescriptorsByReservationIds(reservationIds)).thenReturn(Arrays.asList(descAndRes1, descAndRes2));

        SubscriptionDescriptor sd = mock(SubscriptionDescriptor.class);
        when(sd.getMaxAvailable()).thenReturn(10);
        when(sd.getId()).thenReturn(descId);
        when(subscriptionRepository.findByIds(Set.of(descId))).thenReturn(Collections.singletonList(sd));

        when(subscriptionRepository.deleteSubscriptionWithReservationId(Arrays.asList("res-1", "res-2"))).thenReturn(2);

        manager.cleanupReservations(cleanup);

        verify(subscriptionRepository).deleteSubscriptionWithReservationId(Arrays.asList("res-1", "res-2"));
        verify(subscriptionRepository).preGenerateSubscriptions(any(SubscriptionDescriptorModification.class), eq(descId), eq(2));
        verify(extensionManager).handleReservationsCancelled(sd, Arrays.asList("res-1", "res-2"));
    }
}
