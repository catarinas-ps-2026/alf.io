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

import alfio.model.AdminReservationRequestStats;
import alfio.model.Event;
import alfio.model.EventAndOrganizationId;
import alfio.model.api.v1.admin.AttendeesByCategory;
import alfio.model.api.v1.admin.TicketReservationCreationRequest;
import alfio.model.modification.AdminReservationModification;
import alfio.model.modification.AttendeeData;
import alfio.model.result.Result;
import alfio.repository.AdminReservationRequestRepository;
import alfio.repository.EventRepository;
import alfio.repository.user.UserRepository;
import alfio.util.ClockProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReservationRequestManagerUnitTest {

    private AdminReservationRequestManager manager;
    private AdminReservationManager adminReservationManager;
    private EventManager eventManager;
    private UserRepository userRepository;
    private AdminReservationRequestRepository adminReservationRequestRepository;
    private EventRepository eventRepository;
    private PlatformTransactionManager transactionManager;
    private ClockProvider clockProvider;

    @BeforeEach
    void setUp() {
        adminReservationManager = mock(AdminReservationManager.class);
        eventManager = mock(EventManager.class);
        userRepository = mock(UserRepository.class);
        adminReservationRequestRepository = mock(AdminReservationRequestRepository.class);
        eventRepository = mock(EventRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        clockProvider = mock(ClockProvider.class);

        manager = new AdminReservationRequestManager(
            adminReservationManager, eventManager, userRepository,
            adminReservationRequestRepository, eventRepository,
            transactionManager, clockProvider
        );
    }

    @Test
    void testGetRequestStatus() {
        String requestId = "req-1";
        String eventName = "event";
        String username = "user";
        
        EventAndOrganizationId eaoi = mock(EventAndOrganizationId.class);
        when(eaoi.getId()).thenReturn(1);
        when(eventManager.getOptionalEventAndOrganizationIdByName(eventName, username)).thenReturn(Optional.of(eaoi));
        
        AdminReservationRequestStats stats = mock(AdminReservationRequestStats.class);
        when(adminReservationRequestRepository.findStatsByRequestIdAndEventId(requestId, 1)).thenReturn(Optional.of(stats));
        
        Result<AdminReservationRequestStats> result = manager.getRequestStatus(requestId, eventName, username);
        
        assertTrue(result.isSuccess());
        assertEquals(stats, result.getData());
    }

    @Test
    void testGetRequestStatusAccessDenied() {
        String requestId = "req-1";
        String eventName = "event";
        String username = "user";
        
        when(eventManager.getOptionalEventAndOrganizationIdByName(eventName, username)).thenReturn(Optional.empty());
        
        Result<AdminReservationRequestStats> result = manager.getRequestStatus(requestId, eventName, username);
        
        assertFalse(result.isSuccess());
        assertEquals("access_denied", result.getErrors().iterator().next().getCode());
    }

    @Test
    void testScheduleReservations() {
        String eventName = "event";
        String username = "user";
        
        AttendeeData attendee = new AttendeeData("fn", "ln", "e@e.com", "ref", null, null);
        AttendeesByCategory attendeesByCategory = new AttendeesByCategory(1, 1, Collections.singletonList(attendee), null);
        TicketReservationCreationRequest request = new TicketReservationCreationRequest(
            Collections.singletonList(attendeesByCategory),
            null, null, null, null, "en", null, null
        );
        
        when(clockProvider.getClock()).thenReturn(java.time.Clock.systemUTC());
        
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(eventManager.getOptionalByName(eventName, username)).thenReturn(Optional.of(event));
        
        AdminReservationModification mod = mock(AdminReservationModification.class);
        when(adminReservationManager.validateTickets(any(), eq(event))).thenReturn(Result.success(Pair.of(event, mod)));
        when(userRepository.findIdByUserName(username)).thenReturn(Optional.of(123));

        Result<String> result = manager.scheduleReservations(eventName, "en", Collections.singletonList(request), username);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(adminReservationRequestRepository).insertRequest(anyString(), eq(123L), eq(event), any());
    }
}
