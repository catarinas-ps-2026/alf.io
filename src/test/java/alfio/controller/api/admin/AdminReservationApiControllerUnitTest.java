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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.controller.api.support.BookingInfoTicketLoader;
import alfio.controller.api.support.PageAndContent;
import alfio.manager.*;
import alfio.model.*;
import alfio.model.PurchaseContext.PurchaseContextType;
import alfio.model.modification.AdminReservationModification;
import alfio.model.result.ErrorCode;
import alfio.model.result.Result;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

class AdminReservationApiControllerUnitTest {

    private AdminReservationManager adminReservationManager;
    private EventManager eventManager;
    private PurchaseContextManager purchaseContextManager;
    private PurchaseContextSearchManager purchaseContextSearchManager;
    private TicketReservationManager ticketReservationManager;
    private BookingInfoTicketLoader bookingInfoTicketLoader;
    private AccessService accessService;

    private AdminReservationApiController controller;
    private Principal principal;

    @BeforeEach
    void setUp() {
        adminReservationManager = mock(AdminReservationManager.class);
        eventManager = mock(EventManager.class);
        purchaseContextManager = mock(PurchaseContextManager.class);
        purchaseContextSearchManager = mock(PurchaseContextSearchManager.class);
        ticketReservationManager = mock(TicketReservationManager.class);
        bookingInfoTicketLoader = mock(BookingInfoTicketLoader.class);
        accessService = mock(AccessService.class);

        controller = new AdminReservationApiController(
                adminReservationManager,
                eventManager,
                purchaseContextManager,
                purchaseContextSearchManager,
                ticketReservationManager,
                bookingInfoTicketLoader,
                accessService);

        principal = mock(Principal.class);
        when(principal.getName()).thenReturn("admin");
    }

    @Test
    void createNew_withSubscriptionType_returnsError() {
        AdminReservationModification modification = mock(AdminReservationModification.class);
        Result<String> result =
                controller.createNew(PurchaseContextType.subscription, "sub-1", modification, principal);
        assertFalse(result.isSuccess());
        assertEquals("not_found", result.getFirstErrorOrNull().getCode());
    }

    @Test
    void createNew_withEventType_delegatesToManager() {
        AdminReservationModification modification = mock(AdminReservationModification.class);
        TicketReservation mockReservation = mock(TicketReservation.class);
        when(mockReservation.getId()).thenReturn("RES-123");
        when(adminReservationManager.createReservation(modification, "event-1", "admin"))
                .thenReturn(Result.success(Pair.of(mockReservation, List.of())));

        Result<String> result = controller.createNew(PurchaseContextType.event, "event-1", modification, principal);

        assertTrue(result.isSuccess());
        assertEquals("RES-123", result.getData());
        verify(accessService).checkEventMembership(principal, "event-1", AccessService.MEMBERSHIP_ROLES);
    }

    @Test
    void getAllStatus_returnsAllEnumValues() {
        TicketReservation.TicketReservationStatus[] result =
                controller.getAllStatus(PurchaseContextType.event, "event-1");
        assertArrayEquals(TicketReservation.TicketReservationStatus.values(), result);
    }

    @Test
    void findAll_purchaseContextNotFound_returnsEmptyPage() {
        doReturn(Optional.empty()).when(purchaseContextManager).findBy(PurchaseContextType.event, "event-1");

        PageAndContent<List<TicketReservation>> result =
                controller.findAll(PurchaseContextType.event, "event-1", 0, "search", List.of(), principal);

        assertNotNull(result);
        assertTrue(result.getLeft().isEmpty());
        assertEquals(0, result.getRight());
    }

    @Test
    void findAll_purchaseContextFound_returnsReservations() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(42);
        doReturn(Optional.of(context)).when(purchaseContextManager).findBy(PurchaseContextType.event, "event-1");

        List<TicketReservation> list = List.of(mock(TicketReservation.class));
        when(purchaseContextSearchManager.findAllReservationsFor(context, 0, "search", List.of()))
                .thenReturn(Pair.of(list, 1));

        PageAndContent<List<TicketReservation>> result =
                controller.findAll(PurchaseContextType.event, "event-1", 0, "search", List.of(), principal);

        verify(accessService).checkOrganizationOwnership(principal, 42);
        assertEquals(list, result.getLeft());
        assertEquals(1, result.getRight());
    }

    @Test
    void confirmReservation_delegatesToManager() {
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES-123");
        PurchaseContext context = mock(PurchaseContext.class);
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(context.event()).thenReturn(Optional.of(event));

        Triple<TicketReservation, List<Ticket>, PurchaseContext> triple = Triple.of(reservation, List.of(), context);
        doReturn(Result.success(triple))
                .when(adminReservationManager)
                .confirmReservation(
                        eq(PurchaseContextType.event),
                        eq("event-1"),
                        eq("RES-123"),
                        eq("admin"),
                        eq(AdminReservationModification.Notification.EMPTY));

        Result<AdminReservationApiController.TicketReservationDescriptor> result =
                controller.confirmReservation(PurchaseContextType.event, "event-1", "RES-123", principal);

        assertTrue(result.isSuccess());
        assertEquals("RES-123", result.getData().getReservation().getId());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void updateReservation_delegatesToManager() {
        AdminReservationModification arm = mock(AdminReservationModification.class);
        when(adminReservationManager.updateReservation(PurchaseContextType.event, "event-1", "RES-123", arm, "admin"))
                .thenReturn(Result.success(true));

        Result<Boolean> result =
                controller.updateReservation(PurchaseContextType.event, "event-1", "RES-123", arm, principal);

        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void notifyReservation_delegatesToManager() {
        AdminReservationModification arm = mock(AdminReservationModification.class);
        when(adminReservationManager.notify(PurchaseContextType.event, "event-1", "RES-123", arm, "admin"))
                .thenReturn(Result.success(true));

        Result<Boolean> result =
                controller.notifyReservation(PurchaseContextType.event, "event-1", "RES-123", arm, principal);

        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void notifyAttendees_delegatesToManager() {
        List<Integer> ids = List.of(1, 2);
        when(adminReservationManager.notifyAttendees("event-1", "RES-123", ids, "admin"))
                .thenReturn(Result.success(true));

        Result<Boolean> result = controller.notifyAttendees("event-1", "RES-123", ids, principal);

        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void getAudit_delegatesToManager() {
        List<Audit> auditList = List.of();
        when(adminReservationManager.getAudit(PurchaseContextType.event, "event-1", "RES-123", "admin"))
                .thenReturn(Result.success(auditList));

        Result<List<Audit>> result = controller.getAudit(PurchaseContextType.event, "event-1", "RES-123", principal);

        assertTrue(result.isSuccess());
        assertEquals(auditList, result.getData());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void getBillingDocuments_delegatesToManager() {
        List<BillingDocument> docList = List.of();
        when(adminReservationManager.getBillingDocuments("event-1", "RES-123", "admin"))
                .thenReturn(Result.success(docList));

        Result<List<BillingDocument>> result =
                controller.getBillingDocuments(PurchaseContextType.event, "event-1", "RES-123", principal);

        assertTrue(result.isSuccess());
        assertEquals(docList, result.getData());
        verify(accessService).checkReservationOwnership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void invalidateBillingDocument_success_returnsOk() {
        when(adminReservationManager.invalidateBillingDocument("RES-123", 456L, "admin"))
                .thenReturn(Result.success(true));

        ResponseEntity<Boolean> response =
                controller.invalidateBillingDocument(PurchaseContextType.event, "event-1", "RES-123", 456L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(accessService)
                .checkBillingDocumentOwnership(principal, PurchaseContextType.event, "event-1", "RES-123", 456L);
    }

    @Test
    void invalidateBillingDocument_failure_returnsBadRequest() {
        when(adminReservationManager.invalidateBillingDocument("RES-123", 456L, "admin"))
                .thenReturn(Result.error(ErrorCode.custom("err", "error")));

        ResponseEntity<Boolean> response =
                controller.invalidateBillingDocument(PurchaseContextType.event, "event-1", "RES-123", 456L, principal);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void restoreBillingDocument_success_returnsOk() {
        when(adminReservationManager.restoreBillingDocument("RES-123", 456L, "admin"))
                .thenReturn(Result.success(true));

        ResponseEntity<Boolean> response =
                controller.restoreBillingDocument(PurchaseContextType.event, "event-1", "RES-123", 456L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(accessService).checkReservationOwnership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void restoreBillingDocument_failure_returnsBadRequest() {
        when(adminReservationManager.restoreBillingDocument("RES-123", 456L, "admin"))
                .thenReturn(Result.error(ErrorCode.custom("err", "error")));

        ResponseEntity<Boolean> response =
                controller.restoreBillingDocument(PurchaseContextType.event, "event-1", "RES-123", 456L, principal);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getBillingDocument_success_returnsOk() {
        byte[] pdfBytes = "pdf-content".getBytes();
        BillingDocument doc = mock(BillingDocument.class);
        when(doc.getType()).thenReturn(BillingDocument.Type.RECEIPT);
        Pair<BillingDocument, byte[]> pdfPair = Pair.of(doc, pdfBytes);
        when(adminReservationManager.getSingleBillingDocumentAsPdf(
                        PurchaseContextType.event, "event-1", "RES-123", 456L, "admin"))
                .thenReturn(Result.success(pdfPair));

        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseEntity<Void> result = controller.getBillingDocument(
                PurchaseContextType.event, "event-1", "RES-123", 456L, principal, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(accessService).checkReservationOwnership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void getBillingDocument_notFound_returnsNotFound() {
        when(adminReservationManager.getSingleBillingDocumentAsPdf(
                        PurchaseContextType.event, "event-1", "RES-123", 456L, "admin"))
                .thenReturn(Result.error(ErrorCode.custom("not_found", "not found")));

        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseEntity<Void> result = controller.getBillingDocument(
                PurchaseContextType.event, "event-1", "RES-123", 456L, principal, response);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void loadReservation_delegatesToManager() {
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES-123");
        PurchaseContext context = mock(PurchaseContext.class);
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(context.event()).thenReturn(Optional.of(event));

        Triple<TicketReservation, List<Ticket>, PurchaseContext> triple = Triple.of(reservation, List.of(), context);
        doReturn(Result.success(triple))
                .when(adminReservationManager)
                .loadReservation(eq(PurchaseContextType.event), eq("event-1"), eq("RES-123"), eq("admin"));

        Result<AdminReservationApiController.TicketReservationDescriptor> result =
                controller.loadReservation(PurchaseContextType.event, "event-1", "RES-123", principal);

        assertTrue(result.isSuccess());
        assertEquals("RES-123", result.getData().getReservation().getId());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void loadTicket_ticketFound_returnsTicket() {
        TicketReservation reservation = mock(TicketReservation.class);
        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(99);
        PurchaseContext context = mock(PurchaseContext.class);
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(context.event()).thenReturn(Optional.of(event));

        Triple<TicketReservation, List<Ticket>, PurchaseContext> triple =
                Triple.of(reservation, List.of(ticket), context);
        doReturn(Result.success(triple))
                .when(adminReservationManager)
                .loadReservation(eq(PurchaseContextType.event), eq("event-1"), eq("RES-123"), eq("admin"));

        Result<Ticket> result = controller.loadTicket(PurchaseContextType.event, "event-1", "RES-123", 99, principal);

        assertTrue(result.isSuccess());
        assertEquals(99, result.getData().getId());
        verify(accessService).checkTicketMembership(principal, "event-1", "RES-123", 99);
    }

    @Test
    void loadTicket_ticketNotFound_returnsError() {
        TicketReservation reservation = mock(TicketReservation.class);
        PurchaseContext context = mock(PurchaseContext.class);
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(context.event()).thenReturn(Optional.of(event));

        Triple<TicketReservation, List<Ticket>, PurchaseContext> triple = Triple.of(reservation, List.of(), context);
        doReturn(Result.success(triple))
                .when(adminReservationManager)
                .loadReservation(eq(PurchaseContextType.event), eq("event-1"), eq("RES-123"), eq("admin"));

        Result<Ticket> result = controller.loadTicket(PurchaseContextType.event, "event-1", "RES-123", 99, principal);

        assertFalse(result.isSuccess());
    }

    @Test
    void ticketsWithAdditionalData_subscriptionType_returnsEmptyList() {
        List<Integer> result =
                controller.ticketsWithAdditionalData(PurchaseContextType.subscription, "sub-1", "RES-123", principal);
        assertTrue(result.isEmpty());
    }

    @Test
    void ticketsWithAdditionalData_eventType_delegatesToManager() {
        when(adminReservationManager.getTicketIdsWithAdditionalData(PurchaseContextType.event, "event-1", "RES-123"))
                .thenReturn(List.of(1, 2));

        List<Integer> result =
                controller.ticketsWithAdditionalData(PurchaseContextType.event, "event-1", "RES-123", principal);

        assertEquals(List.of(1, 2), result);
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void removeTickets_delegatesToManager() {
        AdminReservationApiController.RemoveTicketsModification mod =
                new AdminReservationApiController.RemoveTicketsModification(List.of(1), Map.of(1, true), true, true);

        when(adminReservationManager.removeTickets(
                        eq("event-1"), eq("RES-123"), eq(List.of(1)), eq(List.of(1)), eq(true), eq(true), eq("admin")))
                .thenReturn(Result.success(true));

        Result<AdminReservationApiController.RemoveResult> result =
                controller.removeTickets("event-1", "RES-123", mod, principal);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isSuccess());
        assertTrue(result.getData().isCreditNoteGenerated());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void getPaymentInfo_delegatesToManager() {
        TransactionAndPaymentInfo info = mock(TransactionAndPaymentInfo.class);
        when(adminReservationManager.getPaymentInfo("RES-123")).thenReturn(Result.success(info));

        Result<TransactionAndPaymentInfo> result =
                controller.getPaymentInfo(PurchaseContextType.event, "event-1", "RES-123", principal);

        assertTrue(result.isSuccess());
        assertEquals(info, result.getData());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void removeReservation_delegatesToManager() {
        when(adminReservationManager.removeReservation(
                        PurchaseContextType.event, "event-1", "RES-123", true, true, true, "admin"))
                .thenReturn(Result.success(true));

        Result<Boolean> result = controller.removeReservation(
                PurchaseContextType.event, "event-1", "RES-123", true, true, true, principal);

        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(accessService).checkReservationMembership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void creditReservation_delegatesToManager() {
        Result<Boolean> result =
                controller.creditReservation(PurchaseContextType.event, "event-1", "RES-123", true, true, principal);

        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(accessService).checkReservationOwnership(principal, PurchaseContextType.event, "event-1", "RES-123");
        verify(adminReservationManager)
                .creditReservation(PurchaseContextType.event, "event-1", "RES-123", true, true, "admin");
    }

    @Test
    void regenerateBillingDocument_delegatesToManager() {
        when(adminReservationManager.regenerateBillingDocument(
                        PurchaseContextType.event, "event-1", "RES-123", "admin"))
                .thenReturn(Result.success(true));

        Result<Boolean> result =
                controller.regenerateBillingDocument(PurchaseContextType.event, "event-1", "RES-123", principal);

        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(accessService).checkReservationOwnership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void refund_delegatesToManager() {
        AdminReservationApiController.RefundAmount amount = new AdminReservationApiController.RefundAmount("150.00");
        when(adminReservationManager.refund(
                        PurchaseContextType.event, "event-1", "RES-123", new BigDecimal("150.00"), "admin"))
                .thenReturn(Result.success(true));

        Result<Boolean> result = controller.refund(PurchaseContextType.event, "event-1", "RES-123", amount, principal);

        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(accessService).checkReservationOwnership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }

    @Test
    void getEmailList_delegatesToManager() {
        List<LightweightMailMessage> list = List.of();
        when(adminReservationManager.getEmailsForReservation(PurchaseContextType.event, "event-1", "RES-123", "admin"))
                .thenReturn(Result.success(list));

        Result<List<LightweightMailMessage>> result =
                controller.getEmailList(PurchaseContextType.event, "event-1", "RES-123", principal);

        assertTrue(result.isSuccess());
        assertEquals(list, result.getData());
        verify(accessService).checkReservationOwnership(principal, PurchaseContextType.event, "event-1", "RES-123");
    }
}
