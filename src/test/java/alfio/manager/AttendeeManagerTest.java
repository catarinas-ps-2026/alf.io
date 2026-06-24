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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import alfio.manager.support.CheckInStatus;
import alfio.manager.support.SponsorAttendeeData;
import alfio.manager.support.TicketAndCheckInResult;
import alfio.manager.user.UserManager;
import alfio.model.*;
import alfio.model.result.ErrorCode;
import alfio.model.result.Result;
import alfio.model.support.TicketWithAdditionalFields;
import alfio.model.user.Organization;
import alfio.model.user.User;
import alfio.repository.AdditionalServiceItemRepository;
import alfio.repository.EventRepository;
import alfio.repository.SponsorScanRepository;
import alfio.repository.TicketRepository;
import alfio.repository.user.UserRepository;
import alfio.util.ClockProvider;
import alfio.util.EventUtil;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

public class AttendeeManagerTest {

    @Mock
    private SponsorScanRepository sponsorScanRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserManager userManager;

    @Mock
    private PurchaseContextFieldManager purchaseContextFieldManager;

    @Mock
    private AdditionalServiceItemRepository additionalServiceItemRepository;

    @Mock
    private ClockProvider clockProvider;

    private AttendeeManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Clock fixedClock = Clock.fixed(
                ZonedDateTime.of(2026, 6, 4, 12, 0, 0, 0, ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        when(clockProvider.getClock()).thenReturn(fixedClock);
        when(clockProvider.withZone(any())).thenReturn(fixedClock);
        manager = new AttendeeManager(
                sponsorScanRepository,
                eventRepository,
                ticketRepository,
                userRepository,
                userManager,
                purchaseContextFieldManager,
                additionalServiceItemRepository,
                clockProvider);
    }

    @Test
    public void testRegisterSponsorScanEventNotFound() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(10);
        when(userRepository.getByUsername("sponsor")).thenReturn(user);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event1"))
                .thenReturn(Optional.empty());

        TicketAndCheckInResult res = manager.registerSponsorScan(
                "event1", "uid1", "notes", SponsorScan.LeadStatus.WARM, "sponsor", "op1", null);

        assertNull(res.getTicket());
        assertEquals(CheckInStatus.EVENT_NOT_FOUND, res.getResult().getStatus());
    }

    @Test
    public void testRegisterSponsorScanTicketNotFound() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(10);
        when(userRepository.getByUsername("sponsor")).thenReturn(user);

        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event1"))
                .thenReturn(Optional.of(event));

        when(ticketRepository.findOptionalByUUID("uid1")).thenReturn(Optional.empty());

        TicketAndCheckInResult res = manager.registerSponsorScan(
                "event1", "uid1", "notes", SponsorScan.LeadStatus.WARM, "sponsor", "op1", null);

        assertNull(res.getTicket());
        assertEquals(CheckInStatus.TICKET_NOT_FOUND, res.getResult().getStatus());
    }

    @Test
    public void testRegisterSponsorScanInvalidTicketState() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(10);
        when(userRepository.getByUsername("sponsor")).thenReturn(user);

        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(event.getId()).thenReturn(20);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event1"))
                .thenReturn(Optional.of(event));

        Ticket ticket = mock(Ticket.class);
        when(ticket.getStatus()).thenReturn(Ticket.TicketStatus.ACQUIRED); // Not checked-in
        when(ticket.getEventId()).thenReturn(20);
        when(ticketRepository.findOptionalByUUID("uid1")).thenReturn(Optional.of(ticket));

        TicketAndCheckInResult res = manager.registerSponsorScan(
                "event1", "uid1", "notes", SponsorScan.LeadStatus.WARM, "sponsor", "op1", null);

        assertNotNull(res.getTicket());
        assertEquals(CheckInStatus.INVALID_TICKET_STATE, res.getResult().getStatus());
    }

    @Test
    public void testRegisterSponsorScanSuccess() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(10);
        when(userRepository.getByUsername("sponsor")).thenReturn(user);

        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(event.getId()).thenReturn(20);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event1"))
                .thenReturn(Optional.of(event));

        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(30);
        when(ticket.getStatus()).thenReturn(Ticket.TicketStatus.CHECKED_IN);
        when(ticket.getEventId()).thenReturn(20);
        when(ticketRepository.findOptionalByUUID("uid1")).thenReturn(Optional.of(ticket));

        when(sponsorScanRepository.getRegistrationTimestamp(10, 20, 30, "op1")).thenReturn(Optional.empty());
        when(eventRepository.getZoneIdByEventId(20)).thenReturn(ZoneId.of("UTC"));

        TicketAndCheckInResult res = manager.registerSponsorScan(
                "event1", "uid1", "notes", SponsorScan.LeadStatus.WARM, "sponsor", "op1", null);

        assertNotNull(res.getTicket());
        assertEquals(CheckInStatus.SUCCESS, res.getResult().getStatus());
        verify(sponsorScanRepository)
                .insert(
                        eq(10),
                        any(ZonedDateTime.class),
                        eq(20),
                        eq(30),
                        eq("notes"),
                        eq(SponsorScan.LeadStatus.WARM),
                        eq("op1"));
    }

    @Test
    public void testRetrieveTicketEventNotFound() {
        when(eventRepository.findOptionalByShortName("event1")).thenReturn(Optional.empty());
        Result<TicketWithAdditionalFields> result = manager.retrieveTicket("event1", "uid1", "user");
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.EventError.NOT_FOUND, result.getFirstErrorOrNull());
    }

    @Test
    public void testRetrieveTicketSuccess() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(20);
        when(event.getOrganizationId()).thenReturn(100);
        when(eventRepository.findOptionalByShortName("event1")).thenReturn(Optional.of(event));

        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(100);
        when(userManager.findUserOrganizations("user")).thenReturn(Collections.singletonList(org));

        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(20);
        when(ticketRepository.findOptionalByUUID("uid1")).thenReturn(Optional.of(ticket));

        try (MockedStatic<EventUtil> eventUtilMockedStatic = mockStatic(EventUtil.class)) {
            BiFunction<Ticket, Event, List<FieldConfigurationDescriptionAndValue>> mockBiFunction =
                    mock(BiFunction.class);
            when(mockBiFunction.apply(any(), any())).thenReturn(Collections.emptyList());
            eventUtilMockedStatic
                    .when(() -> EventUtil.retrieveFieldValues(any(), any(), any(), anyBoolean()))
                    .thenReturn(mockBiFunction);

            Result<TicketWithAdditionalFields> result = manager.retrieveTicket("event1", "uid1", "user");
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
        }
    }

    @Test
    public void testRetrieveScannedAttendees() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(10);
        when(userRepository.getByUsername("sponsor")).thenReturn(user);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(20);
        when(eventRepository.findOptionalByShortName("event1")).thenReturn(Optional.of(event));

        DetailedScanData scan = mock(DetailedScanData.class);
        Ticket ticket = mock(Ticket.class);
        when(ticket.getUuid()).thenReturn("ticket-uuid");
        when(ticket.getFullName()).thenReturn("John Doe");
        when(ticket.getEmail()).thenReturn("john@example.com");
        when(scan.getTicket()).thenReturn(ticket);

        SponsorScan ss = mock(SponsorScan.class);
        ZonedDateTime timestamp = ZonedDateTime.of(2026, 6, 4, 12, 0, 0, 0, ZoneId.of("UTC"));
        when(ss.getTimestamp()).thenReturn(timestamp);
        when(scan.getSponsorScan()).thenReturn(ss);

        when(sponsorScanRepository.loadSponsorData(eq(20), eq(10), any(ZonedDateTime.class)))
                .thenReturn(Collections.singletonList(scan));

        Optional<List<SponsorAttendeeData>> result =
                manager.retrieveScannedAttendees("event1", "sponsor", ZonedDateTime.now());
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("ticket-uuid", result.get().get(0).getTicketId());
    }
}
