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
        OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
        ticketRepository = mock(TicketRepository.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        Event event = mock(Event.class);
        Organization organization = mock(Organization.class);
        ConfigurationLevel cl = ConfigurationLevel.event(event);
        when(event.getConfigurationLevel()).thenReturn(cl);
        when(eventRepository.findOptionalByShortName(EVENT_NAME)).thenReturn(Optional.of(event));
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getOrganizationId()).thenReturn(ORG_ID);
        when(organizationRepository.findOrganizationForUser(USERNAME, ORG_ID)).thenReturn(Optional.of(organization));
        when(organization.getId()).thenReturn(ORG_ID);
        when(eventRepository.retrieveCheckInStatisticsForEvent(eq(EVENT_ID), isNull()))
                .thenReturn(new CheckInStatistics(0, 0, new Date()));
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
                null);
    }

    @Test
    void getStatistics() {
        when(configurationManager.getFor(eq(CHECK_IN_STATS), any(ConfigurationLevel.class)))
                .thenReturn(new ConfigurationManager.MaybeConfiguration(
                        CHECK_IN_STATS, new ConfigurationKeyValuePathLevel(null, "true", null)));
        CheckInStatistics statistics = checkInManager.getStatistics(EVENT_NAME, null, USERNAME);
        assertNotNull(statistics);
        verify(eventRepository).retrieveCheckInStatisticsForEvent(eq(EVENT_ID), isNull());
    }

    @Test
    void getStatisticsDisabled() {
        when(configurationManager.getFor(eq(CHECK_IN_STATS), any(ConfigurationLevel.class)))
                .thenReturn(new ConfigurationManager.MaybeConfiguration(
                        CHECK_IN_STATS, new ConfigurationKeyValuePathLevel(null, "false", null)));
        CheckInStatistics statistics = checkInManager.getStatistics(EVENT_NAME, null, USERNAME);
        assertNull(statistics);
        verify(eventRepository, never()).retrieveCheckInStatisticsForEvent(eq(EVENT_ID), isNull());
    }

    @Test
    void testExtractStatus_EventNotFound() {
        when(eventRepository.findOptionalById(anyInt())).thenReturn(Optional.empty());
        var result = checkInManager.checkIn(1, "uuid", Optional.of("code"), "user");
        assertEquals(CheckInStatus.EVENT_NOT_FOUND, result.getResult().getStatus());
    }

    @Test
    void testExtractStatus_TicketNotFound() {
        Event event = mock(Event.class);
        when(eventRepository.findOptionalById(anyInt())).thenReturn(Optional.of(event));
        when(ticketRepository.findByUUIDForUpdate(anyString())).thenReturn(Optional.empty());
        var result = checkInManager.checkIn(1, "uuid", Optional.of("code"), "user");
        assertEquals(CheckInStatus.TICKET_NOT_FOUND, result.getResult().getStatus());
    }

    @Test
    void testExtractStatus_InvalidTicketCode() {
        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);
        TicketCategory tc = mock(TicketCategory.class);
        when(eventRepository.findOptionalById(anyInt())).thenReturn(Optional.of(event));
        when(ticketRepository.findByUUIDForUpdate(anyString())).thenReturn(Optional.of(ticket));
        when(ticket.getCategoryId()).thenReturn(10);
        when(ticketCategoryRepository.getById(10)).thenReturn(tc);
        when(event.getPrivateKey()).thenReturn("key");
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now());
        when(tc.hasValidCheckIn(any(), any())).thenReturn(true);
        when(ticket.ticketCode(anyString(), anyBoolean())).thenReturn("correct-code");

        var result = checkInManager.checkIn(1, "uuid", Optional.of("wrong-code"), "user");
        assertEquals(CheckInStatus.INVALID_TICKET_CODE, result.getResult().getStatus());
    }

    @Test
    void testExtractStatus_NullCategoryId() {
        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);
        when(eventRepository.findOptionalById(anyInt())).thenReturn(Optional.of(event));
        when(ticketRepository.findByUUIDForUpdate(anyString())).thenReturn(Optional.of(ticket));
        when(ticket.getCategoryId()).thenReturn(null);
        var result = checkInManager.checkIn(1, "uuid", Optional.of("code"), "user");
        assertEquals(CheckInStatus.INVALID_TICKET_STATE, result.getResult().getStatus());
        assertEquals(
                "Invalid ticket state", ((alfio.manager.support.DefaultCheckInResult) result.getResult()).getMessage());
    }

    @Test
    void testExtractStatus_InvalidCheckInDate() {
        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);
        TicketCategory tc = mock(TicketCategory.class);
        when(eventRepository.findOptionalById(anyInt())).thenReturn(Optional.of(event));
        when(ticketRepository.findByUUIDForUpdate(anyString())).thenReturn(Optional.of(ticket));
        when(ticket.getCategoryId()).thenReturn(10);
        when(ticketCategoryRepository.getById(10)).thenReturn(tc);
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now());
        when(tc.hasValidCheckIn(any(), any())).thenReturn(false);
        var result = checkInManager.checkIn(1, "uuid", Optional.of("code"), "user");
        assertEquals(
                CheckInStatus.INVALID_TICKET_CATEGORY_CHECK_IN_DATE,
                result.getResult().getStatus());
    }

    @Test
    void testExtractStatus_ToBePaid() {
        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);
        TicketCategory tc = mock(TicketCategory.class);
        when(eventRepository.findOptionalById(anyInt())).thenReturn(Optional.of(event));
        when(ticketRepository.findByUUIDForUpdate(anyString())).thenReturn(Optional.of(ticket));
        when(ticket.getCategoryId()).thenReturn(10);
        when(ticketCategoryRepository.getById(10)).thenReturn(tc);
        when(event.getPrivateKey()).thenReturn("key");
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now());
        when(tc.hasValidCheckIn(any(), any())).thenReturn(true);
        when(ticket.ticketCode(anyString(), anyBoolean())).thenReturn("code");
        when(ticket.getStatus()).thenReturn(Ticket.TicketStatus.TO_BE_PAID);

        var result = checkInManager.checkIn(1, "uuid", Optional.of("code"), "user");
        assertEquals(CheckInStatus.MUST_PAY, result.getResult().getStatus());
    }

    @Test
    void testExtractStatus_NotAcquired() {
        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);
        TicketCategory tc = mock(TicketCategory.class);
        when(eventRepository.findOptionalById(anyInt())).thenReturn(Optional.of(event));
        when(ticketRepository.findByUUIDForUpdate(anyString())).thenReturn(Optional.of(ticket));
        when(ticket.getCategoryId()).thenReturn(10);
        when(ticketCategoryRepository.getById(10)).thenReturn(tc);
        when(event.getPrivateKey()).thenReturn("key");
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now());
        when(tc.hasValidCheckIn(any(), any())).thenReturn(true);
        when(ticket.ticketCode(anyString(), anyBoolean())).thenReturn("code");
        when(ticket.getStatus()).thenReturn(Ticket.TicketStatus.PENDING);

        var result = checkInManager.checkIn(1, "uuid", Optional.of("code"), "user");
        assertEquals(CheckInStatus.INVALID_TICKET_STATE, result.getResult().getStatus());
        assertTrue(((alfio.manager.support.DefaultCheckInResult) result.getResult())
                .getMessage()
                .contains("expected ACQUIRED state"));
    }

    @Test
    void testPerformCheckinForOnlineEvent() {
        Ticket ticket = mock(Ticket.class);
        Event event = mock(Event.class);
        TicketCategory tc = mock(TicketCategory.class);

        when(event.getFormat()).thenReturn(Event.EventFormat.ONLINE);
        when(tc.hasValidCheckIn(any(), any())).thenReturn(true);
        when(ticket.isCheckedIn()).thenReturn(false);
        when(ticket.getUuid()).thenReturn("uuid");
        when(event.getId()).thenReturn(1);
        when(ticketRepository.performCheckIn("uuid", 1)).thenReturn(1);

        var auditingRepository = mock(alfio.repository.AuditingRepository.class);
        var extensionManager = mock(alfio.manager.ExtensionManager.class);
        checkInManager = new CheckInManager(
                ticketRepository,
                eventRepository,
                null,
                null,
                ticketCategoryRepository,
                null,
                auditingRepository,
                configurationManager,
                null,
                null,
                null,
                extensionManager,
                null,
                null,
                TestUtil.clockProvider(),
                null);

        var status = checkInManager.performCheckinForOnlineEvent(ticket, event, tc);
        assertEquals(CheckInStatus.SUCCESS, status);
        verify(auditingRepository).insert(any(), any(), anyInt(), any(), any(), any(), any());
        verify(extensionManager).handleTicketCheckedIn(ticket);
    }

    @Test
    void testPerformCheckinForOnlineEvent_Fail() {
        Ticket ticket = mock(Ticket.class);
        Event event = mock(Event.class);
        TicketCategory tc = mock(TicketCategory.class);

        when(event.getFormat()).thenReturn(Event.EventFormat.ONLINE);
        when(tc.hasValidCheckIn(any(), any())).thenReturn(true);
        when(ticket.isCheckedIn()).thenReturn(false);
        when(ticket.getUuid()).thenReturn("uuid");
        when(event.getId()).thenReturn(1);
        when(ticketRepository.performCheckIn("uuid", 1)).thenReturn(0);

        var status = checkInManager.performCheckinForOnlineEvent(ticket, event, tc);
        assertEquals(CheckInStatus.ERROR, status);
    }

    @Test
    void testPerformCheckinForOnlineEvent_AlreadyCheckedIn() {
        Ticket ticket = mock(Ticket.class);
        Event event = mock(Event.class);
        TicketCategory tc = mock(TicketCategory.class);

        when(event.getFormat()).thenReturn(Event.EventFormat.ONLINE);
        when(tc.hasValidCheckIn(any(), any())).thenReturn(true);
        when(ticket.isCheckedIn()).thenReturn(true);

        var status = checkInManager.performCheckinForOnlineEvent(ticket, event, tc);
        assertEquals(CheckInStatus.ALREADY_CHECK_IN, status);
    }

    @Test
    void testPerformCheckinForOnlineEvent_InvalidDate() {
        Ticket ticket = mock(Ticket.class);
        Event event = mock(Event.class);
        TicketCategory tc = mock(TicketCategory.class);

        when(event.getFormat()).thenReturn(Event.EventFormat.ONLINE);
        when(tc.hasValidCheckIn(any(), any())).thenReturn(false);

        var status = checkInManager.performCheckinForOnlineEvent(ticket, event, tc);
        assertEquals(CheckInStatus.INVALID_TICKET_CATEGORY_CHECK_IN_DATE, status);
    }

    @Test
    void testAcquire() {
        Ticket ticket = mock(Ticket.class);
        Event event = mock(Event.class);
        when(ticketRepository.findByUUIDForUpdate("uuid")).thenReturn(Optional.of(ticket));
        when(ticketRepository.findByUUID("uuid")).thenReturn(ticket);
        when(ticket.getStatus()).thenReturn(Ticket.TicketStatus.TO_BE_PAID);
        when(ticket.getUuid()).thenReturn("uuid");
        when(ticket.getEventId()).thenReturn(1);
        when(eventRepository.findById(1)).thenReturn(event);

        var ticketReservationManager = mock(alfio.manager.TicketReservationManager.class);
        checkInManager = new CheckInManager(
                ticketRepository,
                eventRepository,
                null,
                null,
                ticketCategoryRepository,
                null,
                null,
                configurationManager,
                null,
                null,
                ticketReservationManager,
                null,
                null,
                null,
                TestUtil.clockProvider(),
                null);

        checkInManager.confirmOnSitePayment("uuid");
        verify(ticketRepository).updateTicketStatusWithUUID("uuid", Ticket.TicketStatus.ACQUIRED.toString());
        verify(ticketReservationManager).registerAlfioTransactionForOnsitePayment(eq(event), any());
    }

    @Test
    void testManualCheckInLambda() {
        Ticket ticket = mock(Ticket.class);
        Event event = mock(Event.class);
        when(ticketRepository.findByUUIDForUpdate("uuid")).thenReturn(Optional.of(ticket));
        when(ticket.getStatus()).thenReturn(Ticket.TicketStatus.ACQUIRED);
        when(ticket.getEventId()).thenReturn(1);
        when(eventRepository.findById(1)).thenReturn(event);
        when(ticketRepository.findByUUID("uuid")).thenReturn(ticket);

        var scanAuditRepository = mock(alfio.repository.audit.ScanAuditRepository.class);
        var auditingRepository = mock(alfio.repository.AuditingRepository.class);
        var userRepository = mock(alfio.repository.user.UserRepository.class);

        checkInManager = new CheckInManager(
                ticketRepository,
                eventRepository,
                null,
                null,
                ticketCategoryRepository,
                scanAuditRepository,
                auditingRepository,
                configurationManager,
                null,
                userRepository,
                null,
                mock(alfio.manager.ExtensionManager.class),
                null,
                null,
                TestUtil.clockProvider(),
                null);

        assertTrue(checkInManager.manualCheckIn(1, "uuid", "user"));
        verify(scanAuditRepository).insert(eq("uuid"), eq(1), any(), eq("user"), eq(CheckInStatus.SUCCESS), any());
    }

    @Test
    void testCheckInLambdaWithAutoPayment() {
        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);
        TicketCategory tc = mock(TicketCategory.class);
        OrganizationRepository organizationRepository = mock(OrganizationRepository.class);

        when(eventRepository.findOptionalByShortName(EVENT_NAME)).thenReturn(Optional.of(event));
        when(event.getShortName()).thenReturn(EVENT_NAME);
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getOrganizationId()).thenReturn(ORG_ID);
        when(organizationRepository.findOrganizationForUser(USERNAME, ORG_ID))
                .thenReturn(Optional.of(mock(Organization.class)));

        doReturn(alfio.manager.system.ConfigurationLevel.event(event))
                .when(event)
                .getConfigurationLevel();
        when(event.supportsLinkedAdditionalServices()).thenReturn(true);
        when(event.getFormat()).thenReturn(Event.EventFormat.ONLINE);

        // Setup for evaluateTicketStatus (extractStatus)
        when(ticketRepository.findOptionalByUUID("uuid")).thenReturn(Optional.of(ticket));
        when(ticket.getCategoryId()).thenReturn(10);
        when(ticketCategoryRepository.getById(10)).thenReturn(tc);
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now());
        when(tc.hasValidCheckIn(any(), any())).thenReturn(true);
        when(event.getPrivateKey()).thenReturn("key");
        when(ticket.ticketCode(anyString(), anyBoolean())).thenReturn("code");
        when(ticket.getStatus()).thenReturn(Ticket.TicketStatus.TO_BE_PAID);
        when(ticket.getCurrencyCode()).thenReturn("CHF");
        when(ticket.getFinalPriceCts()).thenReturn(1000);
        when(ticket.getUuid()).thenReturn("uuid");
        when(ticket.getTicketsReservationId()).thenReturn("resId");

        // Setup for checkIn(int eventId, ...)
        when(eventRepository.findOptionalById(EVENT_ID)).thenReturn(Optional.of(event));
        when(eventRepository.findEventAndOrganizationIdById(EVENT_ID)).thenReturn(event);
        when(ticketRepository.findByUUIDForUpdate("uuid")).thenReturn(Optional.of(ticket));
        when(ticketRepository.findByUUID("uuid")).thenReturn(ticket);

        // Mock dependencies for checkIn call
        var scanAuditRepository = mock(alfio.repository.audit.ScanAuditRepository.class);
        var auditingRepository = mock(alfio.repository.AuditingRepository.class);
        var userRepository = mock(alfio.repository.user.UserRepository.class);
        var ticketReservationManager = mock(alfio.manager.TicketReservationManager.class);
        var extensionManager = mock(alfio.manager.ExtensionManager.class);
        var purchaseContextFieldRepository = mock(alfio.repository.PurchaseContextFieldRepository.class);

        checkInManager = new CheckInManager(
                ticketRepository,
                eventRepository,
                mock(alfio.repository.TicketReservationRepository.class),
                purchaseContextFieldRepository,
                ticketCategoryRepository,
                scanAuditRepository,
                auditingRepository,
                configurationManager,
                organizationRepository,
                userRepository,
                ticketReservationManager,
                extensionManager,
                mock(alfio.repository.AdditionalServiceItemRepository.class),
                null,
                TestUtil.clockProvider(),
                null);

        // We need to make it OK_READY_TO_BE_CHECKED_IN for the second call
        // The first call returns MUST_PAY, which triggers confirmOnSitePayment
        // confirmOnSitePayment calls acquire, which should update status to ACQUIRED
        // But since we are mocking, we need to handle the state change if we want a full flow,
        // or just verify the calls.

        checkInManager.checkIn(EVENT_NAME, "uuid", Optional.of("code"), USERNAME, "auditUser", true);

        verify(ticketReservationManager).registerAlfioTransactionForOnsitePayment(any(), any());
    }

    @Test
    void testGetEncryptedAttendeesInformationLambdas() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getPrivateKey()).thenReturn("key");
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.supportsLinkedAdditionalServices()).thenReturn(true);

        when(configurationManager.areBooleanSettingsEnabledForEvent(any(), any()))
                .thenReturn(ev -> true);
        when(configurationManager.getFor(eq(alfio.model.system.ConfigurationKeys.CHECK_IN_COLOR_CONFIGURATION), any()))
                .thenReturn(new ConfigurationManager.MaybeConfiguration(
                        alfio.model.system.ConfigurationKeys.CHECK_IN_COLOR_CONFIGURATION,
                        new alfio.model.system.ConfigurationKeyValuePathLevel(null, null, null)));

        TicketCategory tc = mock(TicketCategory.class);
        when(tc.getName()).thenReturn("Cat");
        when(tc.getTicketCheckInStrategy()).thenReturn(TicketCategory.TicketCheckInStrategy.ONCE_PER_EVENT);
        when(ticketCategoryRepository.findByEventIdAsMap(1)).thenReturn(Collections.singletonMap(10, tc));

        alfio.model.FullTicketInfo fi = mock(alfio.model.FullTicketInfo.class);
        when(fi.getFirstName()).thenReturn("John");
        when(fi.getLastName()).thenReturn("Doe");
        when(fi.getStatus()).thenReturn(Ticket.TicketStatus.ACQUIRED);
        when(fi.getUuid()).thenReturn("uuid");
        when(fi.getCategoryId()).thenReturn(10);
        when(fi.getTicketCategory()).thenReturn(tc);
        when(fi.ticketCode(anyString(), anyBoolean())).thenReturn("code");
        when(fi.hmacTicketInfo(anyString(), anyBoolean())).thenReturn("hmac");

        alfio.model.BillingDetails bd = mock(alfio.model.BillingDetails.class);
        when(fi.getBillingDetails()).thenReturn(bd);
        when(fi.getTicketsReservationId()).thenReturn("resId");
        when(ticketRepository.findFirstTicketIdInReservation("resId")).thenReturn(Optional.of(1));
        when(fi.getId()).thenReturn(1);

        when(ticketRepository.findAllFullTicketInfoAssignedByEventId(1, Collections.singletonList(1)))
                .thenReturn(Collections.singletonList(fi));

        var purchaseContextFieldRepository = mock(alfio.repository.PurchaseContextFieldRepository.class);
        alfio.model.FieldValueAndDescription fvd = mock(alfio.model.FieldValueAndDescription.class);
        when(fvd.getName()).thenReturn("field");
        when(fvd.getValue()).thenReturn("value");
        when(fvd.getDescription()).thenReturn("{\"restrictedValues\":{\"value\":\"Label\"}}");
        when(purchaseContextFieldRepository.findValueForTicketId(anyInt(), anySet()))
                .thenReturn(Collections.singletonList(fvd));

        var pollRepository = mock(alfio.repository.PollRepository.class);
        when(pollRepository.findAllForEvent(1)).thenReturn(Collections.emptyList());

        checkInManager = new CheckInManager(
                ticketRepository,
                eventRepository,
                null,
                purchaseContextFieldRepository,
                ticketCategoryRepository,
                null,
                null,
                configurationManager,
                null,
                null,
                null,
                null,
                mock(alfio.repository.AdditionalServiceItemRepository.class),
                pollRepository,
                TestUtil.clockProvider(),
                null);

        var result = checkInManager.getEncryptedAttendeesInformation(
                event, Collections.singleton("field"), Collections.singletonList(1));
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetEncryptedAttendeesInformation_Disabled() {
        Event event = mock(Event.class);
        when(configurationManager.areBooleanSettingsEnabledForEvent(any(), any()))
                .thenReturn(ev -> false);
        when(configurationManager.getFor(eq(OFFLINE_CHECKIN_ENABLED), any(ConfigurationLevel.class)))
                .thenReturn(new ConfigurationManager.MaybeConfiguration(
                        OFFLINE_CHECKIN_ENABLED, new ConfigurationKeyValuePathLevel(null, "false", null)));
        var result =
                checkInManager.getEncryptedAttendeesInformation(event, Collections.emptySet(), Collections.emptyList());
        assertEquals(Collections.emptyMap(), result);
    }
}
