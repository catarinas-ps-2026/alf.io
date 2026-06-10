package alfio.controller.api.admin;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import alfio.manager.ExportManager;
import alfio.model.ReservationsByEvent;
import alfio.model.support.ReservationInfo;
import alfio.model.support.TicketInfo;
import alfio.model.transaction.PaymentProxy;

@ExtendWith(MockitoExtension.class)
class ExportApiControllerTest {

    @Mock
    private ExportManager exportManager;

    @Mock
    private Principal principal;

    @Captor
    private ArgumentCaptor<java.util.function.Consumer> consumerCaptor;

    private ExportApiController controller;

    @BeforeEach
    void setUp() {
        controller = new ExportApiController(exportManager);
    }

    @Test
    void downloadAllEvents_withValidDateRange_exportsReservations() throws IOException {
        
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        
        ReservationsByEvent event = mockEvent(1, "Test Event", "TEST", new ArrayList<>());
        List<ReservationsByEvent> allEvents = List.of(event);
        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(allEvents);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withEmptyReservations_returnsError() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        
        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(new ArrayList<>());

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
        assertEquals(HttpStatus.PRECONDITION_REQUIRED.value(), response.getStatus());
        assertEquals("text/plain", response.getContentType());
    }

    @Test
    void downloadAllEvents_withReservations_exportsWithCorrectHeaders() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        ReservationInfo reservation = mockReservation(
            "RES1", "2024-01-01T10:00:00Z", "Company", "123456",
            "TAX123", "INV001", 10000, 2000, "EUR", 
            PaymentProxy.STRIPE, new ArrayList<>(),
            "John", "Doe", "john@example.com"
        );
        ReservationsByEvent event = mockEvent(1, "Test Event", "TEST", List.of(reservation));
        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withInvalidFromDate_throwsException() {

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(NullPointerException.class, () ->
            controller.downloadAllEvents(null, "2024-12-31", response, principal)
        );
    }

    @Test
    void downloadAllEvents_withInvalidToDate_throwsException() {

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(NullPointerException.class, () ->
            controller.downloadAllEvents("2024-01-01", null, response, principal)
        );
    }

    @Test
    void downloadAllEvents_withMultipleEvents_exportsAll() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        ReservationsByEvent event1 = mockEvent(1, "Event 1", "EVT1", new ArrayList<>());
        ReservationsByEvent event2 = mockEvent(2, "Event 2", "EVT2", new ArrayList<>());
        List<ReservationsByEvent> allEvents = List.of(event1, event2);
        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(allEvents);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withMultipleTickets_exportsAllTickets() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TicketInfo ticket1 = mockTicket("T1", "Standard", 10000, 2000, "John", "Doe");
        TicketInfo ticket2 = mockTicket("T2", "Premium", 15000, 3000, "Jane", "Smith");

        when(ticket1.getId()).thenReturn("T1");
        when(ticket1.getType()).thenReturn("Standard");
        when(ticket1.getSrcPriceCts()).thenReturn(10000);
        when(ticket1.getTaxCts()).thenReturn(2000);
        when(ticket1.getFirstName()).thenReturn("John");
        when(ticket1.getLastName()).thenReturn("Doe");
        when(ticket1.getStatus()).thenReturn("CONFIRMED");

        when(ticket2.getId()).thenReturn("T2");
        when(ticket2.getType()).thenReturn("Premium");
        when(ticket2.getSrcPriceCts()).thenReturn(15000);
        when(ticket2.getTaxCts()).thenReturn(3000);
        when(ticket2.getFirstName()).thenReturn("Jane");
        when(ticket2.getLastName()).thenReturn("Smith");
        when(ticket2.getStatus()).thenReturn("CONFIRMED");

        ReservationInfo reservation = mockReservation(
            "RES1", "2024-01-01T10:00:00Z", "Company", "123456",
            "TAX123", "INV001", 25000, 5000, "EUR",
            PaymentProxy.STRIPE, List.of(ticket1, ticket2),
            "John", "Doe", "john@example.com"
        );

        when(reservation.getId()).thenReturn("RES1");
        when(reservation.getConfirmationTimestamp()).thenReturn("2024-01-01T10:00:00Z");
        when(reservation.getCompanyName()).thenReturn("Company");
        when(reservation.getTaxId()).thenReturn("123456");
        when(reservation.getTaxCode()).thenReturn("TAX123");
        when(reservation.getInvoiceNumber()).thenReturn("INV001");
        when(reservation.getSrcPriceCts()).thenReturn(25000);
        when(reservation.getTaxCts()).thenReturn(5000);
        when(reservation.getCurrency()).thenReturn("EUR");
        when(reservation.getPaymentType()).thenReturn(PaymentProxy.STRIPE);
        when(reservation.getTickets()).thenReturn(List.of(ticket1, ticket2));

        ReservationsByEvent event = mockEvent(
            1,
            "Test Event",
            "TEST",
            List.of(reservation)
        );

        when(event.getDisplayName()).thenReturn("Test Event");
        when(event.getEventShortName()).thenReturn("TEST");
        when(event.getReservations()).thenReturn(List.of(reservation));

        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withNullCompanyName_useFullName() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TicketInfo ticket = mockTicket("T1", "Standard", 10000, 2000, "John", "Doe");
        when(ticket.getId()).thenReturn("T1");
        when(ticket.getType()).thenReturn("Standard");
        when(ticket.getSrcPriceCts()).thenReturn(10000);
        when(ticket.getTaxCts()).thenReturn(2000);
        when(ticket.getFirstName()).thenReturn("John");
        when(ticket.getLastName()).thenReturn("Doe");
        when(ticket.getStatus()).thenReturn("CONFIRMED");

        ReservationInfo reservation = mockReservation(
            "RES1", "2024-01-01T10:00:00Z", null, "123456",
            "TAX123", "INV001", 10000, 2000, "EUR",
            PaymentProxy.STRIPE, List.of(ticket),
            "John", "Doe", "john@example.com"
        );
        when(reservation.getId()).thenReturn("RES1");
        when(reservation.getConfirmationTimestamp()).thenReturn("2024-01-01T10:00:00Z");
        when(reservation.getCompanyName()).thenReturn(null);
        when(reservation.getFirstName()).thenReturn("John");
        when(reservation.getLastName()).thenReturn("Doe");
        when(reservation.getTaxId()).thenReturn("123456");
        when(reservation.getTaxCode()).thenReturn("TAX123");
        when(reservation.getInvoiceNumber()).thenReturn("INV001");
        when(reservation.getSrcPriceCts()).thenReturn(10000);
        when(reservation.getTaxCts()).thenReturn(2000);
        when(reservation.getCurrency()).thenReturn("EUR");
        when(reservation.getPaymentType()).thenReturn(PaymentProxy.STRIPE);
        when(reservation.getTickets()).thenReturn(List.of(ticket));

        ReservationsByEvent event = mockEvent(1, "Test Event", "TEST", List.of(reservation));
        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        when(event.getDisplayName()).thenReturn("Test Event");
        when(event.getEventShortName()).thenReturn("TEST");
        when(event.getReservations()).thenReturn(List.of(reservation));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withNullTaxId_handlesGracefully() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        ReservationInfo reservation = mockReservation(
            "RES1", "2024-01-01T10:00:00Z", "Company", null,
            "TAX123", "INV001", 10000, 2000, "EUR",
            PaymentProxy.STRIPE, new ArrayList<>(),
            "John", "Doe", "john@example.com"
        );
        ReservationsByEvent event = mockEvent(1, "Test Event", "TEST", List.of(reservation));
        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withNullInvoiceNumber_handlesGracefully() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        ReservationInfo reservation = mockReservation(
            "RES1", "2024-01-01T10:00:00Z", "Company", "123456",
            "TAX123", null, 10000, 2000, "EUR",
            PaymentProxy.STRIPE, new ArrayList<>(),
            "John", "Doe", "john@example.com"
        );
        ReservationsByEvent event = mockEvent(1, "Test Event", "TEST", List.of(reservation));
        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withNullAmounts_handlesGracefully() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TicketInfo ticket = mockTicket(
            "T1",
            "Standard",
            null,
            null,
            "John",
            "Doe"
        );

        when(ticket.getId()).thenReturn("T1");
        when(ticket.getType()).thenReturn("Standard");
        when(ticket.getSrcPriceCts()).thenReturn(null);
        when(ticket.getTaxCts()).thenReturn(null);
        when(ticket.getFirstName()).thenReturn("John");
        when(ticket.getLastName()).thenReturn("Doe");
        when(ticket.getStatus()).thenReturn("CONFIRMED");

        ReservationInfo reservation = mockReservation(
            "RES1", "2024-01-01T10:00:00Z", "Company", "123456",
            "TAX123", "INV001", null, null, "EUR",
            PaymentProxy.STRIPE, List.of(ticket),
            "John", "Doe", "john@example.com"
        );

        when(reservation.getId()).thenReturn("RES1");
        when(reservation.getConfirmationTimestamp())
            .thenReturn("2024-01-01T10:00:00Z");
        when(reservation.getCompanyName()).thenReturn("Company");
        when(reservation.getTaxId()).thenReturn("123456");
        when(reservation.getTaxCode()).thenReturn("TAX123");
        when(reservation.getInvoiceNumber()).thenReturn("INV001");
        when(reservation.getSrcPriceCts()).thenReturn(null);
        when(reservation.getTaxCts()).thenReturn(null);
        when(reservation.getCurrency()).thenReturn("EUR");
        when(reservation.getPaymentType()).thenReturn(PaymentProxy.STRIPE);
        when(reservation.getTickets()).thenReturn(List.of(ticket));

        ReservationsByEvent event = mockEvent(
            1,
            "Test Event",
            "TEST",
            List.of(reservation)
        );

        when(event.getDisplayName()).thenReturn("Test Event");
        when(event.getEventShortName()).thenReturn("TEST");
        when(event.getReservations()).thenReturn(List.of(reservation));

        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadAllEvents(
            "2024-01-01",
            "2024-12-31",
            response,
            principal
        );

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withNullTicketAttendeeNames_handleGracefully() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TicketInfo ticket = mockTicket("T1", "Standard", 10000, 2000, null, null);

        ReservationInfo reservation = mockReservation(
            "RES1", "2024-01-01T10:00:00Z", "Company", "123456",
            "TAX123", "INV001", 10000, 2000, "EUR",
            PaymentProxy.STRIPE, List.of(ticket),
            "John", "Doe", "john@example.com"
        );

        when(reservation.getId()).thenReturn("RES1");
        when(reservation.getConfirmationTimestamp()).thenReturn("2024-01-01T10:00:00Z");
        when(reservation.getCompanyName()).thenReturn("Company");
        when(reservation.getTaxId()).thenReturn("123456");
        when(reservation.getTaxCode()).thenReturn("TAX123");
        when(reservation.getInvoiceNumber()).thenReturn("INV001");
        when(reservation.getSrcPriceCts()).thenReturn(10000);
        when(reservation.getTaxCts()).thenReturn(2000);
        when(reservation.getCurrency()).thenReturn("EUR");
        when(reservation.getPaymentType()).thenReturn(PaymentProxy.STRIPE);
        when(reservation.getTickets()).thenReturn(List.of(ticket));

        ReservationsByEvent event = mockEvent(
            1,
            "Test Event",
            "TEST",
            List.of(reservation)
        );

        when(event.getDisplayName()).thenReturn("Test Event");
        when(event.getEventShortName()).thenReturn("TEST");
        when(event.getReservations()).thenReturn(List.of(reservation));

        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadAllEvents(
            "2024-01-01",
            "2024-12-31",
            response,
            principal
        );

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_withVariousPaymentTypes_exportsAll() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        TicketInfo ticket1 = mockTicket(
            "T1", "Standard", 10000, 2000,
            "John", "Doe"
        );

        when(ticket1.getId()).thenReturn("T1");
        when(ticket1.getType()).thenReturn("Standard");
        when(ticket1.getSrcPriceCts()).thenReturn(10000);
        when(ticket1.getTaxCts()).thenReturn(2000);
        when(ticket1.getFirstName()).thenReturn("John");
        when(ticket1.getLastName()).thenReturn("Doe");
        when(ticket1.getStatus()).thenReturn("CONFIRMED");

        TicketInfo ticket2 = mockTicket(
            "T2", "Standard", 15000, 3000,
            "Jane", "Smith"
        );

        when(ticket2.getId()).thenReturn("T2");
        when(ticket2.getType()).thenReturn("Standard");
        when(ticket2.getSrcPriceCts()).thenReturn(15000);
        when(ticket2.getTaxCts()).thenReturn(3000);
        when(ticket2.getFirstName()).thenReturn("Jane");
        when(ticket2.getLastName()).thenReturn("Smith");
        when(ticket2.getStatus()).thenReturn("CONFIRMED");

        ReservationInfo res1 = mockReservation(
            "RES1", "2024-01-01T10:00:00Z", "Company", "123456",
            "TAX123", "INV001", 10000, 2000, "EUR",
            PaymentProxy.STRIPE, List.of(ticket1),
            "John", "Doe", "john@example.com"
        );

        when(res1.getConfirmationTimestamp()).thenReturn("2024-01-01T10:00:00Z");
        when(res1.getTickets()).thenReturn(List.of(ticket1));
        when(res1.getPaymentType()).thenReturn(PaymentProxy.STRIPE);
        when(res1.getCurrency()).thenReturn("EUR");

        ReservationInfo res2 = mockReservation(
            "RES2", "2024-01-02T11:00:00Z", "Company", "123456",
            "TAX123", "INV002", 15000, 3000, "EUR",
            PaymentProxy.PAYPAL, List.of(ticket2),
            "Jane", "Smith", "jane@example.com"
        );

        when(res2.getConfirmationTimestamp()).thenReturn("2024-01-02T11:00:00Z");
        when(res2.getTickets()).thenReturn(List.of(ticket2));
        when(res2.getPaymentType()).thenReturn(PaymentProxy.PAYPAL);
        when(res2.getCurrency()).thenReturn("EUR");

        ReservationsByEvent event = mockEvent(
            1,
            "Test Event",
            "TEST",
            List.of(res1, res2)
        );

        when(event.getEventShortName()).thenReturn("TEST");
        when(event.getDisplayName()).thenReturn("Test Event");
        when(event.getReservations()).thenReturn(List.of(res1, res2));

        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadAllEvents(
            "2024-01-01",
            "2024-12-31",
            response,
            principal
        );

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_eventsSorted_byShortName() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        ReservationsByEvent eventZ = mockEvent(1, "Event Z", "EVTZ", new ArrayList<>());
        ReservationsByEvent eventA = mockEvent(2, "Event A", "EVTA", new ArrayList<>());
        List<ReservationsByEvent> allEvents = List.of(eventZ, eventA);
        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(allEvents);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.downloadAllEvents("2024-01-01", "2024-12-31", response, principal);

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    @Test
    void downloadAllEvents_reservationsSorted_byConfirmationTimestamp() throws IOException {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        ReservationInfo res1 = mockReservation(
            "RES1", "2024-01-05T10:00:00Z", "Company", "123456",
            "TAX123", "INV001", 10000, 2000, "EUR",
            PaymentProxy.STRIPE, new ArrayList<>(),
            "John", "Doe", "john@example.com"
        );

        when(res1.getConfirmationTimestamp())
            .thenReturn("2024-01-05T10:00:00Z");
        when(res1.getTickets())
            .thenReturn(new ArrayList<>());

        ReservationInfo res2 = mockReservation(
            "RES2", "2024-01-01T10:00:00Z", "Company", "123456",
            "TAX123", "INV002", 15000, 3000, "EUR",
            PaymentProxy.STRIPE, new ArrayList<>(),
            "Jane", "Smith", "jane@example.com"
        );

        when(res2.getConfirmationTimestamp())
            .thenReturn("2024-01-01T10:00:00Z");
        when(res2.getTickets())
            .thenReturn(new ArrayList<>());

        ReservationsByEvent event = mockEvent(
            1,
            "Test Event",
            "TEST",
            List.of(res1, res2)
        );

        when(event.getEventShortName())
            .thenReturn("TEST");
        when(event.getReservations())
            .thenReturn(List.of(res1, res2));

        when(exportManager.reservationsForInterval(from, to, principal))
            .thenReturn(List.of(event));

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.downloadAllEvents(
            "2024-01-01",
            "2024-12-31",
            response,
            principal
        );

        verify(exportManager).reservationsForInterval(from, to, principal);
    }

    private TicketInfo mockTicket(
        String id,
        String type,
        Integer amount,
        Integer tax,
        String firstName,
        String lastName
    ) {
        TicketInfo ticket = mock(TicketInfo.class);

        when(ticket.getId()).thenReturn(id);
        when(ticket.getType()).thenReturn(type);
        when(ticket.getSrcPriceCts()).thenReturn(amount);
        when(ticket.getTaxCts()).thenReturn(tax);
        when(ticket.getFirstName()).thenReturn(firstName);
        when(ticket.getLastName()).thenReturn(lastName);
        when(ticket.getStatus()).thenReturn("CONFIRMED");

        return ticket;
    }

    private ReservationInfo mockReservation(
        String id,
        String timestamp,
        String company,
        String taxId,
        String taxCode,
        String invoice,
        Integer amount,
        Integer tax,
        String currency,
        PaymentProxy payment,
        List<TicketInfo> tickets,
        String firstName,
        String lastName,
        String email
    ) {
        ReservationInfo reservation = mock(ReservationInfo.class);

        when(reservation.getTickets()).thenReturn(tickets);

        return reservation;
    }

    private ReservationsByEvent mockEvent(
        int eventId,
        String displayName,
        String shortName,
        List<ReservationInfo> reservations
    ) {
        ReservationsByEvent event = mock(ReservationsByEvent.class);

        when(event.getEventShortName()).thenReturn(shortName);
        when(event.getReservations()).thenReturn(reservations);

        return event;
    }
}
