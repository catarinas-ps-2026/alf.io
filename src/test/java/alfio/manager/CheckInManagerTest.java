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

import static alfio.model.system.ConfigurationKeys.CHECK_IN_STATS;
import static alfio.model.system.ConfigurationKeys.OFFLINE_CHECKIN_ENABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import alfio.manager.support.CheckInStatistics;
import alfio.manager.support.CheckInStatus;
import alfio.manager.system.ConfigurationLevel;
import alfio.manager.system.ConfigurationManager;
import alfio.model.Event;
import alfio.model.Ticket;
import alfio.model.TicketCategory;
import alfio.model.system.ConfigurationKeyValuePathLevel;
import alfio.model.user.Organization;
import alfio.repository.EventRepository;
import alfio.repository.TicketCategoryRepository;
import alfio.repository.TicketRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.test.util.TestUtil;
import alfio.util.ClockProvider;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckInManagerTest {

    private EventRepository eventRepository;
    private ConfigurationManager configurationManager;
    private TicketRepository ticketRepository;
    private TicketCategoryRepository ticketCategoryRepository;
    private CheckInManager checkInManager;

    private static final String EVENT_NAME = "eventName";
    private static final String USERNAME = "username";
    private static final int EVENT_ID = 0;
    private static final int ORG_ID = 1;

    @BeforeEach
    public void setUp() {
        eventRepository = mock(EventRepository.class);
        configurationManager = mock(ConfigurationManager.class);
        OrganizationRepository organizationRepository = mock(
            OrganizationRepository.class
        );
        ticketRepository = mock(TicketRepository.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        Event event = mock(Event.class);
        Organization organization = mock(Organization.class);
        ConfigurationLevel cl = ConfigurationLevel.event(event);
        when(event.getConfigurationLevel()).thenReturn(cl);
        when(eventRepository.findOptionalByShortName(EVENT_NAME)).thenReturn(
            Optional.of(event)
        );
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getOrganizationId()).thenReturn(ORG_ID);
        when(
            organizationRepository.findOrganizationForUser(USERNAME, ORG_ID)
        ).thenReturn(Optional.of(organization));
        when(organization.getId()).thenReturn(ORG_ID);
        when(
            eventRepository.retrieveCheckInStatisticsForEvent(
                eq(EVENT_ID),
                isNull()
            )
        ).thenReturn(new CheckInStatistics(0, 0, new Date()));
        checkInManager = new CheckInManager(
            ticketRepository,
            eventRepository,
            null,
            null,
            ticketCategoryRepository,
            null,
            null,
            configurationManager,
            organizationRepository,
            null,
            null,
            null,
            null,
            null,
            TestUtil.clockProvider(),
            null
        );
    }

    @Test
    void getStatistics() {
        when(
            configurationManager.getFor(
                eq(CHECK_IN_STATS),
                any(ConfigurationLevel.class)
            )
        ).thenReturn(
            new ConfigurationManager.MaybeConfiguration(
                CHECK_IN_STATS,
                new ConfigurationKeyValuePathLevel(null, "true", null)
            )
        );
        CheckInStatistics statistics = checkInManager.getStatistics(
            EVENT_NAME,
            null,
            USERNAME
        );
        assertNotNull(statistics);
        verify(eventRepository).retrieveCheckInStatisticsForEvent(
            eq(EVENT_ID),
            isNull()
        );
    }

    @Test
    void getStatisticsDisabled() {
        when(
            configurationManager.getFor(
                eq(CHECK_IN_STATS),
                any(ConfigurationLevel.class)
            )
        ).thenReturn(
            new ConfigurationManager.MaybeConfiguration(
                CHECK_IN_STATS,
                new ConfigurationKeyValuePathLevel(null, "false", null)
            )
        );
        CheckInStatistics statistics = checkInManager.getStatistics(
            EVENT_NAME,
            null,
            USERNAME
        );
        assertNull(statistics);
        verify(eventRepository, never()).retrieveCheckInStatisticsForEvent(
            eq(EVENT_ID),
            isNull()
        );
    }

    @Test
    void testExtractStatus_EventNotFound() {
        when(eventRepository.findOptionalById(anyInt())).thenReturn(
            Optional.empty()
        );
        var result = checkInManager.checkIn(
            1,
            "uuid",
            Optional.of("code"),
            "user"
        );
        assertEquals(
            CheckInStatus.EVENT_NOT_FOUND,
            result.getResult().getStatus()
        );
    }

    @Test
    void testExtractStatus_TicketNotFound() {
        Event event = mock(Event.class);
        when(eventRepository.findOptionalById(anyInt())).thenReturn(
            Optional.of(event)
        );
        when(ticketRepository.findByUUIDForUpdate(anyString())).thenReturn(
            Optional.empty()
        );
        var result = checkInManager.checkIn(
            1,
            "uuid",
            Optional.of("code"),
            "user"
        );
        assertEquals(
            CheckInStatus.TICKET_NOT_FOUND,
            result.getResult().getStatus()
        );
    }

    @Test
    void testExtractStatus_InvalidTicketCode() {
        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);
        TicketCategory tc = mock(TicketCategory.class);
        when(eventRepository.findOptionalById(anyInt())).thenReturn(
            Optional.of(event)
        );
        when(ticketRepository.findByUUIDForUpdate(anyString())).thenReturn(
            Optional.of(ticket)
        );
        when(ticket.getCategoryId()).thenReturn(10);
        when(ticketCategoryRepository.getById(10)).thenReturn(tc);
        when(event.getPrivateKey()).thenReturn("key");
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now());
        when(tc.hasValidCheckIn(any(), any())).thenReturn(true);
        when(ticket.ticketCode(anyString(), anyBoolean())).thenReturn(
            "correct-code"
        );

        var result = checkInManager.checkIn(
            1,
            "uuid",
            Optional.of("wrong-code"),
            "user"
        );
        assertEquals(
            CheckInStatus.INVALID_TICKET_CODE,
            result.getResult().getStatus()
        );
    }

    @Test
    void testGetEncryptedAttendeesInformation_Disabled() {
        Event event = mock(Event.class);
        when(configurationManager.areBooleanSettingsEnabledForEvent(any(), any())).thenReturn(ev -> false);
        when(
            configurationManager.getFor(
                eq(OFFLINE_CHECKIN_ENABLED),
                any(ConfigurationLevel.class)
            )
        ).thenReturn(
            new ConfigurationManager.MaybeConfiguration(
                OFFLINE_CHECKIN_ENABLED,
                new ConfigurationKeyValuePathLevel(null, "false", null)
            )
        );
        var result = checkInManager.getEncryptedAttendeesInformation(
            event,
            Collections.emptySet(),
            Collections.emptyList()
        );
        assertEquals(Collections.emptyMap(), result);
    }
}
