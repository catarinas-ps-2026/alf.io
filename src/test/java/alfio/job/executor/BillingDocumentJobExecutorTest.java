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
package alfio.job.executor;

import alfio.manager.BillingDocumentManager;
import alfio.manager.NotificationManager;
import alfio.manager.TicketReservationManager;
import alfio.manager.system.AdminJobExecutor;
import alfio.model.BillingDocument;
import alfio.model.Event;
import alfio.model.OrderSummary;
import alfio.model.TicketReservation;
import alfio.model.system.AdminJobSchedule;
import alfio.model.user.Organization;
import alfio.repository.EventRepository;
import alfio.repository.user.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BillingDocumentJobExecutorTest {

    private BillingDocumentManager billingDocumentManager;
    private TicketReservationManager ticketReservationManager;
    private EventRepository eventRepository;
    private NotificationManager notificationManager;
    private OrganizationRepository organizationRepository;
    private BillingDocumentJobExecutor executor;

    @BeforeEach
    void setUp() {
        billingDocumentManager = mock(BillingDocumentManager.class);
        ticketReservationManager = mock(TicketReservationManager.class);
        eventRepository = mock(EventRepository.class);
        notificationManager = mock(NotificationManager.class);
        organizationRepository = mock(OrganizationRepository.class);
        executor = new BillingDocumentJobExecutor(billingDocumentManager, ticketReservationManager, eventRepository, notificationManager, organizationRepository);
    }

    @Test
    void processRegeneratesDocumentsForTrimmedCommaSeparatedIdsAndSendsCompletionEmail() {
        var event = mock(Event.class);
        when(event.getOrganizationId()).thenReturn(12);
        when(event.getDisplayName()).thenReturn("My Event");
        when(eventRepository.findById(42)).thenReturn(event);
        when(organizationRepository.getById(12)).thenReturn(new Organization(12, "org", "desc", "billing@example.org", null, "org"));

        var firstDocument = mock(BillingDocument.class);
        when(firstDocument.getReservationId()).thenReturn("reservation-1");
        var secondDocument = mock(BillingDocument.class);
        when(secondDocument.getReservationId()).thenReturn("reservation-2");
        when(billingDocumentManager.getDocumentById(10L)).thenReturn(Optional.of(firstDocument));
        when(billingDocumentManager.getDocumentById(20L)).thenReturn(Optional.of(secondDocument));

        var firstReservation = mock(TicketReservation.class);
        var secondReservation = mock(TicketReservation.class);
        var firstSummary = mock(OrderSummary.class);
        var secondSummary = mock(OrderSummary.class);
        when(ticketReservationManager.findById("reservation-1")).thenReturn(Optional.of(firstReservation));
        when(ticketReservationManager.findById("reservation-2")).thenReturn(Optional.of(secondReservation));
        when(ticketReservationManager.orderSummaryForReservation(firstReservation, event)).thenReturn(firstSummary);
        when(ticketReservationManager.orderSummaryForReservation(secondReservation, event)).thenReturn(secondSummary);

        assertEquals("generated", executor.process(schedule(Map.of("eventId", 42, "username", "admin", "ids", "10, 20"))));

        verify(billingDocumentManager).createBillingDocument(event, firstReservation, "admin", firstSummary);
        verify(billingDocumentManager).createBillingDocument(event, secondReservation, "admin", secondSummary);
        verify(notificationManager).sendSimpleEmail(eq(event), isNull(), eq("billing@example.org"), eq("Invoice Regeneration complete"), any());
    }

    @Test
    void processFailsFastWhenDocumentCannotBeFound() {
        var event = mock(Event.class);
        when(eventRepository.findById(42)).thenReturn(event);
        when(billingDocumentManager.getDocumentById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> executor.process(schedule(Map.of("eventId", 42, "username", "admin", "ids", "10"))));

        verify(billingDocumentManager, never()).createBillingDocument(any(), any(), any(), any());
        verifyNoInteractions(notificationManager, organizationRepository);
    }

    private static AdminJobSchedule schedule(Map<String, Object> metadata) {
        return new AdminJobSchedule(1L, AdminJobExecutor.JobName.REGENERATE_INVOICES.name(), null, AdminJobSchedule.Status.SCHEDULED, null, metadata, 0);
    }
}
