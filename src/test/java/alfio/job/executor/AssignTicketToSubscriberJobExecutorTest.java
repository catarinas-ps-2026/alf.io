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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import alfio.manager.AdminReservationRequestManager;
import alfio.manager.NotificationManager;
import alfio.manager.system.AdminJobExecutor;
import alfio.manager.system.ConfigurationManager;
import alfio.model.ContentLanguage;
import alfio.model.Event;
import alfio.model.TicketCategory;
import alfio.model.modification.AdminReservationModification;
import alfio.model.subscription.AvailableSubscriptionsByEvent;
import alfio.model.system.AdminJobSchedule;
import alfio.repository.EventRepository;
import alfio.repository.PurchaseContextFieldRepository;
import alfio.repository.SubscriptionRepository;
import alfio.repository.TicketCategoryRepository;
import alfio.util.ClockProvider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AssignTicketToSubscriberJobExecutorTest {

    private AdminReservationRequestManager requestManager;
    private ConfigurationManager configurationManager;
    private SubscriptionRepository subscriptionRepository;
    private EventRepository eventRepository;
    private ClockProvider clockProvider;
    private TicketCategoryRepository ticketCategoryRepository;
    private PurchaseContextFieldRepository purchaseContextFieldRepository;
    private AssignTicketToSubscriberJobExecutor executor;

    @BeforeEach
    void setUp() {
        requestManager = mock(AdminReservationRequestManager.class);
        configurationManager = mock(ConfigurationManager.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        eventRepository = mock(EventRepository.class);
        clockProvider = mock(ClockProvider.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        purchaseContextFieldRepository = mock(PurchaseContextFieldRepository.class);
        executor = new AssignTicketToSubscriberJobExecutor(
                requestManager,
                configurationManager,
                subscriptionRepository,
                eventRepository,
                clockProvider,
                ticketCategoryRepository,
                purchaseContextFieldRepository);
    }

    @Test
    void processDoesNothingWhenNoSubscriptionsAreAvailable() {
        when(subscriptionRepository.loadAvailableSubscriptionsByEvent(1, 2)).thenReturn(Map.of());

        assertNull(executor.process(schedule(Map.of(
                AssignTicketToSubscriberJobExecutor.EVENT_ID, 1,
                AssignTicketToSubscriberJobExecutor.ORGANIZATION_ID, 2))));

        verify(subscriptionRepository).loadAvailableSubscriptionsByEvent(1, 2);
        verifyNoInteractions(
                requestManager,
                eventRepository,
                ticketCategoryRepository,
                purchaseContextFieldRepository,
                configurationManager);
    }

    @Test
    void processCreatesReservationRequestForAvailableSubscriptionWhenForced() {
        var subscriptionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var descriptorId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var subscription = new AvailableSubscriptionsByEvent(
                42,
                7,
                subscriptionId,
                descriptorId,
                "attendee@example.org",
                "Jane",
                "Doe",
                "en",
                "buyer@example.org",
                "[{\"name\":\"company\",\"value\":\"Acme\"},{\"name\":\"ignored\",\"value\":\"Nope\"}]",
                "[100]");
        when(subscriptionRepository.loadAvailableSubscriptionsByEvent(42, 7))
                .thenReturn(Map.of(42, List.of(subscription)));
        when(purchaseContextFieldRepository.findAdditionalFieldNamesForEvents(Set.of(42)))
                .thenReturn(Map.of(42, Set.of("company")));

        var event = mock(Event.class);
        when(event.getId()).thenReturn(42);
        when(event.getShortName()).thenReturn("event");
        when(event.getContentLanguages()).thenReturn(List.of(ContentLanguage.ENGLISH));
        when(eventRepository.findByIds(Set.of(42))).thenReturn(List.of(event));

        var category = mock(TicketCategory.class);
        when(category.getId()).thenReturn(100);
        when(category.getName()).thenReturn("default");
        when(category.getPrice()).thenReturn(BigDecimal.TEN);
        when(category.getTicketAccessType()).thenReturn(TicketCategory.TicketAccessType.ONLINE);
        when(ticketCategoryRepository.findAllWithAvailableTickets(42)).thenReturn(List.of(category));
        when(clockProvider.getClock()).thenReturn(Clock.fixed(Instant.parse("2024-04-10T12:34:56Z"), ZoneId.of("UTC")));

        assertNull(executor.process(schedule(Map.of(
                AssignTicketToSubscriberJobExecutor.EVENT_ID, 42,
                AssignTicketToSubscriberJobExecutor.ORGANIZATION_ID, 7,
                AssignTicketToSubscriberJobExecutor.FORCE_GENERATION, true))));

        var requestIdCaptor = ArgumentCaptor.forClass(String.class);
        var bodyCaptor = ArgumentCaptor.forClass(AdminReservationModification.class);
        verify(requestManager)
                .insertRequest(requestIdCaptor.capture(), bodyCaptor.capture(), eq(event), eq(false), eq("admin"));
        assertEquals("AUTO_event_2024-04-10T12:34:56", requestIdCaptor.getValue());

        var body = bodyCaptor.getValue();
        assertEquals("en", body.getLanguage());
        assertEquals(1, body.getTicketsInfo().size());
        var ticketInfo = body.getTicketsInfo().get(0);
        assertEquals(100, ticketInfo.getCategory().getExistingCategoryId());
        assertEquals(1, ticketInfo.getAttendees().size());
        var attendee = ticketInfo.getAttendees().get(0);
        assertEquals("Jane", attendee.getFirstName());
        assertEquals("Doe", attendee.getLastName());
        assertEquals("attendee@example.org", attendee.getEmailAddress());
        assertEquals(subscriptionId + "_auto", attendee.getReference());
        assertEquals(subscriptionId, attendee.getSubscriptionId());
        assertEquals(Map.of("company", List.of("Acme")), attendee.getAdditionalInfo());
        assertEquals(Map.of(NotificationManager.SEND_TICKET_CC, "[\"buyer@example.org\"]"), attendee.getMetadata());
        verifyNoInteractions(configurationManager);
    }

    private static AdminJobSchedule schedule(Map<String, Object> metadata) {
        return new AdminJobSchedule(
                1L,
                AdminJobExecutor.JobName.ASSIGN_TICKETS_TO_SUBSCRIBERS.name(),
                null,
                AdminJobSchedule.Status.SCHEDULED,
                null,
                metadata,
                0);
    }
}
