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

import alfio.model.AdditionalServiceItem;
import alfio.model.Event;
import alfio.model.PurchaseContext;
import alfio.model.ReservationIdAndEventId;
import alfio.model.system.command.CleanupReservations;
import alfio.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EventReservationManagerTest {

    @Mock
    private SpecialPriceRepository specialPriceRepository;
    @Mock
    private GroupManager groupManager;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private AdditionalServiceItemRepository additionalServiceItemRepository;
    @Mock
    private AdditionalServiceManager additionalServiceManager;
    @Mock
    private TicketReservationRepository ticketReservationRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ExtensionManager extensionManager;
    @Mock
    private BillingDocumentRepository billingDocumentRepository;
    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private EventReservationManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new EventReservationManager(
                specialPriceRepository,
                groupManager,
                ticketRepository,
                additionalServiceItemRepository,
                additionalServiceManager,
                ticketReservationRepository,
                eventRepository,
                extensionManager,
                billingDocumentRepository,
                jdbcTemplate
        );
    }

    @Test
    public void testCleanupReservationsNonEventContext() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.ofType(PurchaseContext.PurchaseContextType.event)).thenReturn(false);

        List<String> reservationIds = Arrays.asList("res1");
        CleanupReservations cleanup = new CleanupReservations(context, reservationIds, true, false, false);

        manager.cleanupReservations(cleanup);

        verifyNoInteractions(specialPriceRepository, ticketRepository, groupManager, additionalServiceItemRepository, additionalServiceManager);
    }

    @Test
    public void testCleanupReservationsEventContextExpired() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(100);
        when(event.ofType(PurchaseContext.PurchaseContextType.event)).thenReturn(true);

        List<String> reservationIds = Arrays.asList("res1");
        CleanupReservations cleanup = new CleanupReservations(event, reservationIds, true, false, false);

        when(ticketRepository.findTicketIdsInReservation("res1")).thenReturn(Arrays.asList(1, 2));
        when(jdbcTemplate.batchUpdate(any(), any(SqlParameterSource[].class))).thenReturn(new int[]{1, 1});

        manager.cleanupReservations(cleanup);

        verify(specialPriceRepository).resetToFreeAndCleanupForReservation(reservationIds);
        verify(ticketRepository).resetCategoryIdForUnboundedCategories(reservationIds);
        verify(groupManager).deleteWhitelistedTicketsForReservation("res1");
        verify(additionalServiceItemRepository).deleteAdditionalServiceItemsByReservationId(100, "res1");
        verify(additionalServiceItemRepository).revertAdditionalServiceItemsByReservationId(100, "res1");
        verify(additionalServiceManager).updateStatusForReservationId(100, "res1", AdditionalServiceItem.AdditionalServiceItemStatus.EXPIRED);
        verify(extensionManager).handleReservationsExpired(event, reservationIds);
    }

    @Test
    public void testCleanupReservationsNullContextExpired() {
        List<String> reservationIds = Arrays.asList("res1");
        CleanupReservations cleanup = new CleanupReservations(null, reservationIds, true, false, false);

        ReservationIdAndEventId resEvent = mock(ReservationIdAndEventId.class);
        when(resEvent.getEventId()).thenReturn(100);
        when(resEvent.getId()).thenReturn("res1");
        when(ticketReservationRepository.getReservationIdAndEventId(reservationIds)).thenReturn(Collections.singletonList(resEvent));

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(100);
        when(eventRepository.findById(100)).thenReturn(event);

        when(ticketRepository.findTicketIdsInReservation("res1")).thenReturn(Arrays.asList(1));
        when(jdbcTemplate.batchUpdate(any(), any(SqlParameterSource[].class))).thenReturn(new int[]{1});

        manager.cleanupReservations(cleanup);

        verify(specialPriceRepository).resetToFreeAndCleanupForReservation(reservationIds);
        verify(ticketRepository).resetCategoryIdForUnboundedCategories(reservationIds);
        verify(groupManager).deleteWhitelistedTicketsForReservation("res1");
        verify(additionalServiceItemRepository).deleteAdditionalServiceItemsByReservationId(100, "res1");
        verify(additionalServiceManager).updateStatusForReservationId(100, "res1", AdditionalServiceItem.AdditionalServiceItemStatus.EXPIRED);
        verify(extensionManager).handleReservationsExpired(event, reservationIds);
        verify(billingDocumentRepository).deleteForReservations(reservationIds, 100);
    }

    @Test
    public void testCleanupReservationsNoTicketsReleasedThrows() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(100);
        when(event.ofType(PurchaseContext.PurchaseContextType.event)).thenReturn(true);

        List<String> reservationIds = Arrays.asList("res1");
        CleanupReservations cleanup = new CleanupReservations(event, reservationIds, false, false, false);

        when(ticketRepository.findTicketIdsInReservation("res1")).thenReturn(Collections.emptyList());
        when(jdbcTemplate.batchUpdate(any(), any(SqlParameterSource[].class))).thenReturn(new int[]{});
        when(additionalServiceManager.updateStatusForReservationId(100, "res1", AdditionalServiceItem.AdditionalServiceItemStatus.CANCELLED)).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> manager.cleanupReservations(cleanup));
    }
}
