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
package alfio.controller.api.admin;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import alfio.manager.AccessService;
import alfio.manager.CheckInManager;
import alfio.manager.EventManager;
import alfio.manager.support.CheckInStatistics;
import alfio.manager.support.TicketAndCheckInResult;
import alfio.manager.support.TicketCheckInStatusResult;
import alfio.manager.system.ConfigurationLevel;
import alfio.manager.system.ConfigurationManager;
import alfio.model.Event;
import alfio.model.EventAndOrganizationId;
import alfio.model.FullTicketInfo;
import alfio.model.checkin.AttendeeSearchResults;
import alfio.model.system.ConfigurationKeyValuePathLevel;
import alfio.model.system.ConfigurationKeys;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.*;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CheckInApiControllerTest {

    @Mock
    private CheckInManager checkInManager;

    @Mock
    private EventManager eventManager;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private AccessService accessService;

    @Mock
    private Principal principal;

    private MockMvc mockMvc;

    private CheckInApiController controller;

    @BeforeEach
    void setUp() {
        controller = new CheckInApiController(checkInManager, eventManager, configurationManager, accessService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void findTicketWithUUID_withValidIdAndIdentifier_returnsTicketResult() {
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.evaluateTicketStatus(1, "ticket-123", Optional.of("qr-code")))
                .thenReturn(result);

        TicketAndCheckInResult actual = controller.findTicketWithUUID(1, "ticket-123", "qr-code", principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(principal, 1, "ticket-123", AccessService.CHECKIN_ROLES);
        verify(checkInManager).evaluateTicketStatus(1, "ticket-123", Optional.of("qr-code"));
    }

    @Test
    void findTicketWithUUID_withEventName_returnsTicketResult() {
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.evaluateTicketStatus("event-name", "ticket-123", Optional.of("qr-code")))
                .thenReturn(result);

        TicketAndCheckInResult actual = controller.findTicketWithUUID("event-name", "ticket-123", "qr-code", principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(
                        principal, "event-name", "ticket-123", AccessService.CHECKIN_ROLES);
        verify(checkInManager).evaluateTicketStatus("event-name", "ticket-123", Optional.of("qr-code"));
    }

    @Test
    void findTicketWithUUID_withNullQrCode_evaluatesWithEmptyOptional() {
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.evaluateTicketStatus(1, "ticket-123", Optional.empty()))
                .thenReturn(result);

        TicketAndCheckInResult actual = controller.findTicketWithUUID(1, "ticket-123", null, principal);

        verify(checkInManager).evaluateTicketStatus(1, "ticket-123", Optional.empty());
    }

    @Test
    void getTicketStatus_withValidIdentifier_returnsStatus() {
        TicketCheckInStatusResult status =
                new TicketCheckInStatusResult(null, null, null, null, Collections.emptyList());
        when(checkInManager.retrieveTicketStatus("ticket-123")).thenReturn(status);

        TicketCheckInStatusResult actual = controller.getTicketStatus("event-name", "ticket-123", principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(
                        principal, "event-name", "ticket-123", AccessService.CHECKIN_ROLES);
        verify(checkInManager).retrieveTicketStatus("ticket-123");
    }

    @Test
    void checkIn_withEventIdAndValidCode_performsCheckIn() {
        when(principal.getName()).thenReturn("user");
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.checkIn(1, "ticket-123", Optional.of("code"), "user"))
                .thenReturn(result);

        CheckInApiController.TicketCode ticketCode = new CheckInApiController.TicketCode();
        ticketCode.setCode("code");
        TicketAndCheckInResult actual = controller.checkIn(1, "ticket-123", ticketCode, principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(principal, 1, "ticket-123", AccessService.CHECKIN_ROLES);
        verify(checkInManager).checkIn(1, "ticket-123", Optional.of("code"), "user");
    }

    @Test
    void checkIn_withEventIdAndNullCode_performsCheckInWithEmptyOptional() {
        when(principal.getName()).thenReturn("user");
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.checkIn(1, "ticket-123", Optional.empty(), "user")).thenReturn(result);

        TicketAndCheckInResult actual = controller.checkIn(1, "ticket-123", null, principal);

        verify(checkInManager).checkIn(1, "ticket-123", Optional.empty(), "user");
    }

    @Test
    void checkIn_withEventNameAndValidCode_performsCheckIn() {
        when(principal.getName()).thenReturn("user");
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.checkIn("event-name", "ticket-123", Optional.of("code"), "user", "user"))
                .thenReturn(result);

        CheckInApiController.TicketCode ticketCode = new CheckInApiController.TicketCode();
        ticketCode.setCode("code");
        TicketAndCheckInResult actual = controller.checkIn("event-name", "ticket-123", ticketCode, null, principal);

        verify(checkInManager).checkIn("event-name", "ticket-123", Optional.of("code"), "user", "user");
    }

    @Test
    void checkIn_withEventNameAndOfflineUser_usesOfflineUserAsAudit() {
        when(principal.getName()).thenReturn("user");
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.checkIn("event-name", "ticket-123", Optional.of("code"), "user", "offline-user"))
                .thenReturn(result);

        CheckInApiController.TicketCode ticketCode = new CheckInApiController.TicketCode();
        ticketCode.setCode("code");
        TicketAndCheckInResult actual =
                controller.checkIn("event-name", "ticket-123", ticketCode, "offline-user", principal);

        verify(checkInManager).checkIn("event-name", "ticket-123", Optional.of("code"), "user", "offline-user");
    }

    @Test
    void bulkCheckIn_withMultipleTickets_checksInAll() {
        when(principal.getName()).thenReturn("user");
        List<CheckInApiController.TicketIdentifierCode> tickets = new ArrayList<>();
        CheckInApiController.TicketIdentifierCode ticket1 = new CheckInApiController.TicketIdentifierCode();
        ticket1.setIdentifier("ticket-1");
        ticket1.setCode("code-1");
        tickets.add(ticket1);

        when(checkInManager.checkIn("event-name", "ticket-1", Optional.of("code-1"), "user", "user", false))
                .thenReturn(new TicketAndCheckInResult(null, null));

        Map<String, TicketAndCheckInResult> results =
                controller.bulkCheckIn("event-name", tickets, null, false, principal);

        verify(accessService).checkEventMembership(principal, "event-name", AccessService.CHECKIN_ROLES);
        verify(checkInManager).checkIn("event-name", "ticket-1", Optional.of("code-1"), "user", "user", false);
    }

    @Test
    void bulkCheckIn_withForceCheckInPaymentOnSite_passesParameterToCheckIn() {
        when(principal.getName()).thenReturn("user");
        List<CheckInApiController.TicketIdentifierCode> tickets = new ArrayList<>();
        CheckInApiController.TicketIdentifierCode ticket = new CheckInApiController.TicketIdentifierCode();
        ticket.setIdentifier("ticket-1");
        ticket.setCode("code-1");
        tickets.add(ticket);

        when(checkInManager.checkIn("event-name", "ticket-1", Optional.of("code-1"), "user", "user", true))
                .thenReturn(new TicketAndCheckInResult(null, null));

        Map<String, TicketAndCheckInResult> results =
                controller.bulkCheckIn("event-name", tickets, null, true, principal);

        verify(checkInManager).checkIn("event-name", "ticket-1", Optional.of("code-1"), "user", "user", true);
    }

    @Test
    void bulkCheckIn_withDuplicateTickets_deduplicates() {
        when(principal.getName()).thenReturn("user");
        List<CheckInApiController.TicketIdentifierCode> tickets = new ArrayList<>();
        CheckInApiController.TicketIdentifierCode ticket = new CheckInApiController.TicketIdentifierCode();
        ticket.setIdentifier("ticket-1");
        ticket.setCode("code-1");
        tickets.add(ticket);
        tickets.add(ticket);

        when(checkInManager.checkIn("event-name", "ticket-1", Optional.of("code-1"), "user", "user", false))
                .thenReturn(new TicketAndCheckInResult(null, null));

        Map<String, TicketAndCheckInResult> results =
                controller.bulkCheckIn("event-name", tickets, null, false, principal);

        verify(checkInManager, times(1))
                .checkIn("event-name", "ticket-1", Optional.of("code-1"), "user", "user", false);
    }

    @Test
    void manualCheckIn_withEventId_performsManualCheckIn() {
        when(principal.getName()).thenReturn("user");
        when(checkInManager.manualCheckIn(1, "ticket-123", "user")).thenReturn(true);

        boolean result = controller.manualCheckIn(1, "ticket-123", principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(principal, 1, "ticket-123", AccessService.CHECKIN_ROLES);
        verify(checkInManager).manualCheckIn(1, "ticket-123", "user");
    }

    @Test
    void manualCheckIn_withEventName_returnsResponseEntity() {
        when(principal.getName()).thenReturn("user");
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.of(event));
        when(checkInManager.manualCheckIn(1, "ticket-123", "user")).thenReturn(true);

        var result = controller.manualCheckIn("event-name", "ticket-123", principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(
                        principal, "event-name", "ticket-123", AccessService.CHECKIN_ROLES);
    }

    @Test
    void revertCheckIn_withEventId_revertsCheckIn() {
        when(principal.getName()).thenReturn("user");
        when(checkInManager.revertCheckIn(1, "ticket-123", "user")).thenReturn(true);

        boolean result = controller.revertCheckIn(1, "ticket-123", principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(principal, 1, "ticket-123", AccessService.CHECKIN_ROLES);
        verify(checkInManager).revertCheckIn(1, "ticket-123", "user");
    }

    @Test
    void revertCheckIn_withEventName_returnsResponseEntity() {
        when(principal.getName()).thenReturn("user");
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(accessService.checkEventTicketIdentifierMembership(
                        principal, "event-name", "ticket-123", AccessService.CHECKIN_ROLES))
                .thenReturn(event);
        when(checkInManager.revertCheckIn(1, "ticket-123", "user")).thenReturn(true);

        var result = controller.revertCheckIn("event-name", "ticket-123", principal);

        verify(checkInManager).revertCheckIn(1, "ticket-123", "user");
    }

    @Test
    void confirmOnSitePayment_withEventName_confirms() {
        when(principal.getName()).thenReturn("user");
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.confirmOnSitePayment("event-name", "ticket-123", Optional.of("code"), "user", "user"))
                .thenReturn(result);

        CheckInApiController.TicketCode ticketCode = new CheckInApiController.TicketCode();
        ticketCode.setCode("code");
        TicketAndCheckInResult actual =
                controller.confirmOnSitePayment("event-name", "ticket-123", ticketCode, null, principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(
                        principal, "event-name", "ticket-123", AccessService.CHECKIN_ROLES);
        verify(checkInManager).confirmOnSitePayment("event-name", "ticket-123", Optional.of("code"), "user", "user");
    }

    @Test
    void confirmOnSitePayment_withEventNameAndOfflineUser_usesOfflineUser() {
        when(principal.getName()).thenReturn("user");
        TicketAndCheckInResult result = new TicketAndCheckInResult(null, null);
        when(checkInManager.confirmOnSitePayment(
                        "event-name", "ticket-123", Optional.of("code"), "user", "offline-user"))
                .thenReturn(result);

        CheckInApiController.TicketCode ticketCode = new CheckInApiController.TicketCode();
        ticketCode.setCode("code");
        TicketAndCheckInResult actual =
                controller.confirmOnSitePayment("event-name", "ticket-123", ticketCode, "offline-user", principal);

        verify(checkInManager)
                .confirmOnSitePayment("event-name", "ticket-123", Optional.of("code"), "user", "offline-user");
    }

    @Test
    void getStatistics_withValidEvent_returnsStatistics() {
        when(principal.getName()).thenReturn("user");
        CheckInStatistics stats = new CheckInStatistics(0, 0, new Date());
        when(checkInManager.getStatistics("event-name", null, "user")).thenReturn(stats);

        CheckInStatistics result = controller.getStatistics("event-name", null, principal);

        verify(accessService).checkEventMembership(principal, "event-name", AccessService.CHECKIN_ROLES);
        verify(checkInManager).getStatistics("event-name", null, "user");
    }

    @Test
    void getStatistics_withCategories_includesCategories() {
        when(principal.getName()).thenReturn("user");
        List<Integer> categories = Arrays.asList(1, 2);
        CheckInStatistics stats = new CheckInStatistics(0, 0, new Date());
        when(checkInManager.getStatistics("event-name", categories, "user")).thenReturn(stats);

        CheckInStatistics result = controller.getStatistics("event-name", categories, principal);

        verify(checkInManager).getStatistics("event-name", categories, "user");
    }

    @Test
    void confirmOnSitePayment_withEventId_confirmAndReturnsStatus() {
        when(checkInManager.confirmOnSitePayment("ticket-123")).thenReturn(Optional.of("ok"));

        CheckInApiController.OnSitePaymentConfirmation result =
                controller.confirmOnSitePayment(1, "ticket-123", principal);

        verify(accessService)
                .checkEventTicketIdentifierMembership(principal, 1, "ticket-123", AccessService.CHECKIN_ROLES);
        verify(checkInManager).confirmOnSitePayment("ticket-123");
    }

    @Test
    void confirmOnSitePayment_withEventId_ticketNotFound_returnsFalse() {
        when(checkInManager.confirmOnSitePayment("ticket-123")).thenReturn(Optional.empty());

        CheckInApiController.OnSitePaymentConfirmation result =
                controller.confirmOnSitePayment(1, "ticket-123", principal);

        verify(checkInManager).confirmOnSitePayment("ticket-123");
    }

    @Test
    void findAllIdentifiersForAdminCheckIn_withNoChangedSince_returnsAllIdentifiers() {
        when(principal.getName()).thenReturn("user");
        List<Integer> identifiers = Arrays.asList(1, 2, 3);
        when(checkInManager.getAttendeesIdentifiers(eq(1), any(Date.class), eq("user")))
                .thenReturn(identifiers);

        HttpServletResponse response = mock(HttpServletResponse.class);

        List<Integer> result = controller.findAllIdentifiersForAdminCheckIn(1, null, response, principal);

        verify(checkInManager).getAttendeesIdentifiers(eq(1), any(Date.class), eq("user"));
    }

    @Test
    void findAllIdentifiersForAdminCheckIn_withChangedSince_usesProvidedDate() {
        when(principal.getName()).thenReturn("user");
        long changedSince = System.currentTimeMillis();
        List<Integer> identifiers = Arrays.asList(1, 2, 3);
        when(checkInManager.getAttendeesIdentifiers(eq(1), any(Date.class), eq("user")))
                .thenReturn(identifiers);

        HttpServletResponse response = mock(HttpServletResponse.class);

        List<Integer> result = controller.findAllIdentifiersForAdminCheckIn(1, changedSince, response, principal);

        verify(checkInManager).getAttendeesIdentifiers(eq(1), any(Date.class), eq("user"));
    }

    @Test
    void searchAttendees_withBlankQuery_returnsNoContent() {

        var result = controller.searchAttendees("event-name", "   ", 0, principal);

        verify(accessService).checkEventMembership(principal, "event-name", AccessService.MEMBERSHIP_ROLES);
    }

    @Test
    void searchAttendees_withBlankEventName_returnsNoContent() {

        var result = controller.searchAttendees("   ", "query", 0, principal);

        verify(accessService).checkEventMembership(principal, "   ", AccessService.MEMBERSHIP_ROLES);
    }

    @Test
    void searchAttendees_withValidQueryAndEvent_returnsResults() {
        when(principal.getName()).thenReturn("user");
        AttendeeSearchResults results = new AttendeeSearchResults(0, 0, 0, 0, Collections.emptyList());
        Event event = mock(Event.class);

        when(eventManager.getOptionalByName("event-name", "user")).thenReturn(Optional.of(event));
        when(checkInManager.searchAttendees(any(), eq("query"), eq(0), eq(principal)))
                .thenReturn(results);

        var result = controller.searchAttendees("event-name", "query", 0, principal);

        verify(checkInManager).searchAttendees(any(), eq("query"), eq(0), eq(principal));
    }

    @Test
    void findAllTicketsForAdminCheckIn_withValidIds_returnsTickets() {
        when(principal.getName()).thenReturn("user");
        List<Integer> ids = Arrays.asList(1, 2, 3);
        List<FullTicketInfo> tickets = new ArrayList<>();
        when(checkInManager.getAttendeesInformation(1, ids, "user")).thenReturn(tickets);

        List<FullTicketInfo> result = controller.findAllTicketsForAdminCheckIn(1, ids, principal);

        verify(accessService).checkEventMembership(principal, 1, AccessService.CHECKIN_ROLES);
        verify(checkInManager).getAttendeesInformation(1, ids, "user");
    }

    @Test
    void findAllTicketsForAdminCheckIn_withEmptyIds_throws() {

        try {
            controller.findAllTicketsForAdminCheckIn(1, new ArrayList<>(), principal);
        } catch (IllegalArgumentException e) {
            verify(accessService).checkEventMembership(principal, 1, AccessService.CHECKIN_ROLES);
        }
    }

    @Test
    void findAllTicketsForAdminCheckIn_withMoreThan200Ids_throws() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            ids.add(i);
        }

        try {
            controller.findAllTicketsForAdminCheckIn(1, ids, principal);
        } catch (IllegalArgumentException e) {
            verify(accessService).checkEventMembership(principal, 1, AccessService.CHECKIN_ROLES);
        }
    }

    @Test
    void getLabelLayoutForEvent_withOfflineCheckInEnabled_returnsLayout() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(accessService.canAccessEvent(principal, "event-name")).thenReturn(event);

        String layoutJson =
                "{\"qrCode\":{\"additionalInfo\":[],\"infoSeparator\":\" | \"},\"content\":{\"firstRow\":\"name\",\"secondRow\":\"email\",\"thirdRow\":[],\"additionalRows\":[],\"checkbox\":false},\"general\":{\"printPartialID\":true},\"mediaName\":\"test\"}";
        ConfigurationKeyValuePathLevel config = new ConfigurationKeyValuePathLevel("LABEL_LAYOUT", layoutJson, null);

        ConfigurationManager.MaybeConfiguration maybeConfig =
                new ConfigurationManager.MaybeConfiguration(ConfigurationKeys.LABEL_LAYOUT, config);

        when(configurationManager.getFor(eq(ConfigurationKeys.LABEL_LAYOUT), any()))
                .thenReturn(maybeConfig);
        Predicate<EventAndOrganizationId> predicate = mock(Predicate.class);

        when(checkInManager.isOfflineCheckInEnabled()).thenReturn(predicate);

        when(predicate.test(event)).thenReturn(true);

        var result = controller.getLabelLayoutForEvent("event-name", principal);

        verify(checkInManager).isOfflineCheckInEnabled();
    }

    @Test
    void getLabelLayoutForEvent_withOfflineCheckInDisabled_returnsPreconditionFailed() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(accessService.canAccessEvent(principal, "event-name")).thenReturn(event);
        Predicate<EventAndOrganizationId> predicate = mock(Predicate.class);

        when(checkInManager.isOfflineCheckInEnabled()).thenReturn(predicate);

        when(predicate.test(event)).thenReturn(false);

        var result = controller.getLabelLayoutForEvent("event-name", principal);

        verify(checkInManager).isOfflineCheckInEnabled();
    }

    @Test
    void getOfflineIdentifiers_withOfflineEnabled_returnsIdentifiers() {
        when(principal.getName()).thenReturn("user");

        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);

        when(accessService.checkEventMembership(principal, "event-name", AccessService.CHECKIN_ROLES))
                .thenReturn(event);

        Predicate<EventAndOrganizationId> predicate = mock(Predicate.class);

        when(checkInManager.isOfflineCheckInEnabled()).thenReturn(predicate);

        when(predicate.test(event)).thenReturn(true);

        List<Integer> identifiers = Arrays.asList(1, 2, 3);

        when(checkInManager.getAttendeesIdentifiers(eq(event), any(Date.class), eq("user")))
                .thenReturn(identifiers);

        HttpServletResponse response = mock(HttpServletResponse.class);

        List<Integer> result = controller.getOfflineIdentifiers("event-name", null, response, principal);

        verify(checkInManager).getAttendeesIdentifiers(eq(event), any(Date.class), eq("user"));
    }

    @Test
    void getOfflineIdentifiers_withOfflineDisabled_returnsEmpty() {

        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);

        when(accessService.checkEventMembership(principal, "event-name", AccessService.CHECKIN_ROLES))
                .thenReturn(event);

        Predicate<EventAndOrganizationId> predicate = mock(Predicate.class);

        when(checkInManager.isOfflineCheckInEnabled()).thenReturn(predicate);

        when(predicate.test(event)).thenReturn(false);

        HttpServletResponse response = mock(HttpServletResponse.class);

        List<Integer> result = controller.getOfflineIdentifiers("event-name", null, response, principal);

        verify(checkInManager, never()).getAttendeesIdentifiers(any(), any(), any());
        assertTrue(result.isEmpty());
    }

    @Test
    void getOfflineEncryptedInfo_withValidEvent_returnsEncryptedInfo() {
        when(principal.getName()).thenReturn("user");

        List<Integer> ids = Arrays.asList(1);

        Event event = mock(Event.class);
        ConfigurationLevel configurationLevel = mock(ConfigurationLevel.class);

        when(event.getConfigurationLevel()).thenReturn(configurationLevel);

        when(eventManager.getOptionalByName("event-name", "user")).thenReturn(Optional.of(event));

        ConfigurationKeyValuePathLevel cfg = new ConfigurationKeyValuePathLevel("LABEL_LAYOUT", null, null);

        ConfigurationManager.MaybeConfiguration maybeCfg =
                new ConfigurationManager.MaybeConfiguration(ConfigurationKeys.LABEL_LAYOUT, cfg);

        when(configurationManager.getFor(ConfigurationKeys.LABEL_LAYOUT, configurationLevel))
                .thenReturn(maybeCfg);

        when(checkInManager.getEncryptedAttendeesInformation(eq(event), any(), eq(ids)))
                .thenReturn(new HashMap<>());

        Map<String, String> result = controller.getOfflineEncryptedInfo("event-name", null, ids, principal);

        verify(accessService).checkEventMembership(principal, "event-name", AccessService.CHECKIN_ROLES);

        verify(checkInManager).getEncryptedAttendeesInformation(eq(event), any(), eq(ids));
    }

    @Test
    void getOfflineEncryptedInfo_withAdditionalFields_includeFields() {
        when(principal.getName()).thenReturn("user");

        List<Integer> ids = Arrays.asList(1);
        List<String> additionalFields = Arrays.asList("field1", "field2");

        Event event = mock(Event.class);
        ConfigurationLevel configurationLevel = mock(ConfigurationLevel.class);

        when(event.getConfigurationLevel()).thenReturn(configurationLevel);

        when(eventManager.getOptionalByName("event-name", "user")).thenReturn(Optional.of(event));

        ConfigurationKeyValuePathLevel cfg = new ConfigurationKeyValuePathLevel("LABEL_LAYOUT", null, null);

        ConfigurationManager.MaybeConfiguration maybeCfg =
                new ConfigurationManager.MaybeConfiguration(ConfigurationKeys.LABEL_LAYOUT, cfg);

        when(configurationManager.getFor(ConfigurationKeys.LABEL_LAYOUT, configurationLevel))
                .thenReturn(maybeCfg);

        when(checkInManager.getEncryptedAttendeesInformation(eq(event), any(), eq(ids)))
                .thenReturn(new HashMap<>());

        Map<String, String> result = controller.getOfflineEncryptedInfo("event-name", additionalFields, ids, principal);

        verify(checkInManager).getEncryptedAttendeesInformation(eq(event), any(), eq(ids));
    }

    @Test
    void getOfflineEncryptedInfo_withNullEvent_returnsEmptyMap() {
        when(principal.getName()).thenReturn("user");
        List<Integer> ids = Arrays.asList(1);
        when(eventManager.getOptionalByName("event-name", "user")).thenReturn(Optional.empty());

        Map<String, String> result = controller.getOfflineEncryptedInfo("event-name", null, ids, principal);

        verify(checkInManager, never()).getEncryptedAttendeesInformation(any(), any(), any());
    }

    @Test
    void getOfflineEncryptedInfo_withEmptyIds_throws() {

        try {
            controller.getOfflineEncryptedInfo("event-name", null, new ArrayList<>(), principal);
        } catch (IllegalArgumentException e) {
            verify(accessService).checkEventMembership(principal, "event-name", AccessService.CHECKIN_ROLES);
        }
    }
}
