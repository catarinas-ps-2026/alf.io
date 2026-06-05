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

import alfio.controller.api.support.TicketHelper;
import alfio.controller.support.TemplateProcessor;
import alfio.manager.*;
import alfio.manager.i18n.I18nManager;
import alfio.manager.payment.custom.offline.CustomOfflineConfigurationManager;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.user.UserManager;
import alfio.model.Event;
import alfio.model.TicketReservation;
import alfio.model.TicketReservationInvoicingAdditionalInfo;
import alfio.model.BillingDetails;
import alfio.model.BillingDocument;
import alfio.model.TicketReservationWithTransaction;
import alfio.model.modification.TicketCategoryModification;
import alfio.model.result.ValidationResult;
import alfio.model.system.ConfigurationKeys;
import alfio.model.transaction.PaymentProxy;
import alfio.model.user.Organization;
import alfio.repository.EventDescriptionRepository;
import alfio.repository.PurchaseContextFieldRepository;
import alfio.repository.SponsorScanRepository;
import alfio.repository.TicketCategoryRepository;
import alfio.util.ClockProvider;
import alfio.util.TemplateManager;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import static alfio.controller.api.admin.EventApiController.FIXED_FIELDS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

class EventApiControllerUnitTest {

    @TempDir
    java.nio.file.Path tempDir;

    private EventApiController controller;
    private EventManager eventManager;
    private EventStatisticsManager eventStatisticsManager;
    private I18nManager i18nManager;
    private TicketReservationManager ticketReservationManager;
    private TicketCategoryRepository ticketCategoryRepository;
    private PurchaseContextFieldRepository purchaseContextFieldRepository;
    private EventDescriptionRepository eventDescriptionRepository;
    private TicketHelper ticketHelper;
    private UserManager userManager;
    private SponsorScanRepository sponsorScanRepository;
    private PaymentManager paymentManager;
    private TemplateManager templateManager;
    private FileUploadManager fileUploadManager;
    private ConfigurationManager configurationManager;
    private ExtensionManager extensionManager;
    private ClockProvider clockProvider;
    private AccessService accessService;
    private CustomOfflineConfigurationManager customOfflineConfigurationManager;

    @BeforeEach
    void setUp() {
        eventManager = mock(EventManager.class);
        eventStatisticsManager = mock(EventStatisticsManager.class);
        i18nManager = mock(I18nManager.class);
        ticketReservationManager = mock(TicketReservationManager.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        purchaseContextFieldRepository = mock(PurchaseContextFieldRepository.class);
        eventDescriptionRepository = mock(EventDescriptionRepository.class);
        ticketHelper = mock(TicketHelper.class);
        userManager = mock(UserManager.class);
        sponsorScanRepository = mock(SponsorScanRepository.class);
        paymentManager = mock(PaymentManager.class);
        templateManager = mock(TemplateManager.class);
        fileUploadManager = mock(FileUploadManager.class);
        configurationManager = mock(ConfigurationManager.class);
        extensionManager = mock(ExtensionManager.class);
        clockProvider = mock(ClockProvider.class);
        accessService = mock(AccessService.class);
        customOfflineConfigurationManager = mock(CustomOfflineConfigurationManager.class);

        var mockConfiguration = mock(ConfigurationManager.MaybeConfiguration.class);
        when(mockConfiguration.getValueAsIntOrDefault(anyInt())).thenReturn(4096);
        when(configurationManager.getFor(any(ConfigurationKeys.class), any())).thenReturn(mockConfiguration);

        controller = new EventApiController(
            eventManager,
            eventStatisticsManager,
            i18nManager,
            ticketReservationManager,
            ticketCategoryRepository,
            purchaseContextFieldRepository,
            eventDescriptionRepository,
            ticketHelper,
            userManager,
            sponsorScanRepository,
            paymentManager,
            templateManager,
            fileUploadManager,
            configurationManager,
            extensionManager,
            clockProvider,
            accessService,
            customOfflineConfigurationManager
        );
    }

    @Test
    void getTicketsStatistics_withDateRange() {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));

        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        when(eventStatisticsManager.getFirstReservationConfirmedTimestamp(1)).thenReturn(Optional.empty());
        when(eventStatisticsManager.getFirstReservationCreatedTimestamp(1)).thenReturn(Optional.empty());
        when(eventStatisticsManager.getTicketSoldStatistics(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(eventStatisticsManager.getTicketReservedStatistics(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(clockProvider.getClock()).thenReturn(java.time.Clock.system(ZoneId.of("Europe/Rome")));

        var responseEntity = controller.getTicketsStatistics(eventName, "2024-01-01", "2024-01-31", principal);

        assertNotNull(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        assertEquals("day", responseEntity.getBody().granularity());
    }

    @Test
    void getTicketsStatistics_withNoDateRange() {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));

        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        when(eventStatisticsManager.getFirstReservationConfirmedTimestamp(1)).thenReturn(Optional.of(ZonedDateTime.now()));
        when(eventStatisticsManager.getFirstReservationCreatedTimestamp(1)).thenReturn(Optional.of(ZonedDateTime.now()));
        when(eventStatisticsManager.getTicketSoldStatistics(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(eventStatisticsManager.getTicketReservedStatistics(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(clockProvider.getClock()).thenReturn(java.time.Clock.system(ZoneId.of("Europe/Rome")));

        var responseEntity = controller.getTicketsStatistics(eventName, null, null, principal);

        assertNotNull(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void getTicketsStatistics_eventNotFound() {
        String eventName = "nonexistent-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.empty());

        var responseEntity = controller.getTicketsStatistics(eventName, null, null, principal);

        assertNotNull(responseEntity);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
    }

    @Test
    void getTicketsStatistics_longDateRange_monthGranularity() {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));

        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        when(eventStatisticsManager.getFirstReservationConfirmedTimestamp(1)).thenReturn(Optional.of(ZonedDateTime.now().minusYears(2)));
        when(eventStatisticsManager.getFirstReservationCreatedTimestamp(1)).thenReturn(Optional.of(ZonedDateTime.now().minusYears(2)));
        when(eventStatisticsManager.getTicketSoldStatistics(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(eventStatisticsManager.getTicketReservedStatistics(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(clockProvider.getClock()).thenReturn(java.time.Clock.system(ZoneId.of("Europe/Rome")));

        var responseEntity = controller.getTicketsStatistics(eventName, null, null, principal);

        assertNotNull(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        assertEquals("month", responseEntity.getBody().granularity());
    }

    @Test
    void getTicketsStatistics_mediumDateRange_weekGranularity() {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));

        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        when(eventStatisticsManager.getFirstReservationConfirmedTimestamp(1)).thenReturn(Optional.of(ZonedDateTime.now().minusMonths(6)));
        when(eventStatisticsManager.getFirstReservationCreatedTimestamp(1)).thenReturn(Optional.of(ZonedDateTime.now().minusMonths(6)));
        when(eventStatisticsManager.getTicketSoldStatistics(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(eventStatisticsManager.getTicketReservedStatistics(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(clockProvider.getClock()).thenReturn(java.time.Clock.system(ZoneId.of("Europe/Rome")));

        var responseEntity = controller.getTicketsStatistics(eventName, null, null, principal);

        assertNotNull(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        assertEquals("week", responseEntity.getBody().granularity());
    }

    @Test
    void bulkConfirmation_success() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        doNothing().when(ticketReservationManager).validateAndConfirmOfflinePayment(any(), any(), any(), any());

        String csvContent = "RES001,100.00\nRES002,200.00";
        var fileModification = new alfio.model.modification.UploadBase64FileModification();
        fileModification.setFile(csvContent.getBytes(StandardCharsets.UTF_8));

        var result = controller.bulkConfirmation(eventName, principal, fileModification);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getLeft());
        assertTrue(result.get(1).getLeft());
    }

    @Test
    void bulkConfirmation_partialFailure() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        doThrow(new RuntimeException("Payment failed")).when(ticketReservationManager).validateAndConfirmOfflinePayment(any(), any(), any(), any());

        String csvContent = "RES001,100.00\nRES002,200.00";
        var fileModification = new alfio.model.modification.UploadBase64FileModification();
        fileModification.setFile(csvContent.getBytes(StandardCharsets.UTF_8));

        var result = controller.bulkConfirmation(eventName, principal, fileModification);

        assertEquals(2, result.size());
        assertFalse(result.get(0).getLeft());
        // Triple<Boolean, String, String>: (success, reservationId, errorMessage)
        // The second element is the reservationId (line.reservationId)
        // The third element is the error message (e.getMessage())
        assertEquals("RES001", result.get(0).getMiddle());
        assertEquals("Payment failed", result.get(0).getRight());
        assertFalse(result.get(1).getLeft());
    }

    @Test
    void bulkConfirmation_emptyCSV() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));

        String csvContent = "";
        var fileModification = new alfio.model.modification.UploadBase64FileModification();
        fileModification.setFile(csvContent.getBytes(StandardCharsets.UTF_8));

        var result = controller.bulkConfirmation(eventName, principal, fileModification);

        assertTrue(result.isEmpty());
    }

    @Test
    void bulkConfirmation_singleColumnLine() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        doNothing().when(ticketReservationManager).validateAndConfirmOfflinePayment(any(), any(), any(), any());

        String csvContent = "RES001\nRES002";
        var fileModification = new alfio.model.modification.UploadBase64FileModification();
        fileModification.setFile(csvContent.getBytes(StandardCharsets.UTF_8));

        var result = controller.bulkConfirmation(eventName, principal, fileModification);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllDocumentsXls_withoutItalianEInvoicing() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        var eventAndOrgId = mock(alfio.model.EventAndOrganizationId.class);
        when(configurationManager.isItalianEInvoicingEnabled(eventAndOrgId)).thenReturn(false);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getLineSplittedBillingAddress()).thenReturn(List.of("Billing Address Line 1"));
        when(reservation.getVatNr()).thenReturn("123456789");
        when(reservation.getCurrencyCode()).thenReturn("EUR");
        when(reservation.getPaymentMethod()).thenReturn(PaymentProxy.OFFLINE);
        when(reservation.getUserLanguage()).thenReturn("en");

        TicketReservationWithTransaction reservationWithTransaction = mock(TicketReservationWithTransaction.class);
        when(reservationWithTransaction.getTicketReservation()).thenReturn(reservation);
        when(reservationWithTransaction.getBillingDetails()).thenReturn(mock(BillingDetails.class));

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.INVOICE);
        when(document.getNumber()).thenReturn("INV-001");
        when(document.getModel()).thenReturn(Map.of("orderSummary", Map.of(
            "totalNetPrice", "100.00",
            "totalVAT", "22.00",
            "totalPrice", "122.00"
        )));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        when(ticketReservationManager.streamAllDocumentsFor(anyInt()))
            .thenReturn(java.util.stream.Stream.of(Pair.of(reservationWithTransaction, List.of(document))));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getAllDocumentsXls(eventName, response, principal);

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(response.getContentAsString().length() > 0);
    }

    @Test
    void getAllDocumentsXls_withItalianEInvoicing() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        var eventAndOrgId = mock(alfio.model.EventAndOrganizationId.class);
        when(configurationManager.isItalianEInvoicingEnabled(eventAndOrgId)).thenReturn(true);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getLineSplittedBillingAddress()).thenReturn(List.of("Billing Address Line 1"));
        when(reservation.getVatNr()).thenReturn("123456789");
        when(reservation.getCurrencyCode()).thenReturn("EUR");
        when(reservation.getPaymentMethod()).thenReturn(PaymentProxy.OFFLINE);
        when(reservation.getUserLanguage()).thenReturn("en");

        TicketReservationInvoicingAdditionalInfo italianEInvoicing = new TicketReservationInvoicingAdditionalInfo(
            new TicketReservationInvoicingAdditionalInfo.ItalianEInvoicing("ABCDEF12GHIJLM12", TicketReservationInvoicingAdditionalInfo.ItalianEInvoicing.ReferenceType.ADDRESSEE_CODE, "ABCDEF", "pec@example.com", true)
        );

        BillingDetails billingDetails = mock(BillingDetails.class);
        when(billingDetails.getInvoicingAdditionalInfo()).thenReturn(italianEInvoicing);

        TicketReservationWithTransaction reservationWithTransaction = mock(TicketReservationWithTransaction.class);
        when(reservationWithTransaction.getTicketReservation()).thenReturn(reservation);
        when(reservationWithTransaction.getBillingDetails()).thenReturn(billingDetails);

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.INVOICE);
        when(document.getNumber()).thenReturn("INV-001");
        when(document.getModel()).thenReturn(Map.of("orderSummary", Map.of(
            "totalNetPrice", "100.00",
            "totalVAT", "22.00",
            "totalPrice", "122.00"
        )));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        when(ticketReservationManager.streamAllDocumentsFor(anyInt()))
            .thenReturn(java.util.stream.Stream.of(Pair.of(reservationWithTransaction, List.of(document))));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getAllDocumentsXls(eventName, response, principal);

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(response.getContentAsString().length() > 0);
    }

    @Test
    void getAllDocumentsXls_receiptType() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        var eventAndOrgId = mock(alfio.model.EventAndOrganizationId.class);
        when(configurationManager.isItalianEInvoicingEnabled(eventAndOrgId)).thenReturn(false);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getLineSplittedBillingAddress()).thenReturn(List.of("Billing Address Line 1"));
        when(reservation.getVatNr()).thenReturn("123456789");
        when(reservation.getCurrencyCode()).thenReturn("EUR");
        when(reservation.getPaymentMethod()).thenReturn(PaymentProxy.OFFLINE);
        when(reservation.getUserLanguage()).thenReturn("en");

        TicketReservationWithTransaction reservationWithTransaction = mock(TicketReservationWithTransaction.class);
        when(reservationWithTransaction.getTicketReservation()).thenReturn(reservation);
        when(reservationWithTransaction.getBillingDetails()).thenReturn(mock(BillingDetails.class));

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.RECEIPT);
        when(document.getNumber()).thenReturn("RCP-001");
        when(document.getModel()).thenReturn(Map.of("orderSummary", Map.of(
            "totalNetPrice", "80.00",
            "totalVAT", "17.60",
            "totalPrice", "97.60"
        )));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        when(ticketReservationManager.streamAllDocumentsFor(anyInt()))
            .thenReturn(java.util.stream.Stream.of(Pair.of(reservationWithTransaction, List.of(document))));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getAllDocumentsXls(eventName, response, principal);

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(response.getContentAsString().length() > 0);
    }

    @Test
    void getAllDocumentsXls_creditNoteType() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        var eventAndOrgId = mock(alfio.model.EventAndOrganizationId.class);
        when(configurationManager.isItalianEInvoicingEnabled(eventAndOrgId)).thenReturn(false);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getLineSplittedBillingAddress()).thenReturn(List.of("Billing Address Line 1"));
        when(reservation.getVatNr()).thenReturn("123456789");
        when(reservation.getCurrencyCode()).thenReturn("EUR");
        when(reservation.getPaymentMethod()).thenReturn(PaymentProxy.OFFLINE);
        when(reservation.getUserLanguage()).thenReturn("en");

        TicketReservationWithTransaction reservationWithTransaction = mock(TicketReservationWithTransaction.class);
        when(reservationWithTransaction.getTicketReservation()).thenReturn(reservation);
        when(reservationWithTransaction.getBillingDetails()).thenReturn(mock(BillingDetails.class));

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.CREDIT_NOTE);
        when(document.getNumber()).thenReturn("CN-001");
        when(document.getModel()).thenReturn(Map.of("orderSummary", Map.of(
            "totalNetPrice", "-50.00",
            "totalVAT", "-11.00",
            "totalPrice", "-61.00"
        )));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        when(ticketReservationManager.streamAllDocumentsFor(anyInt()))
            .thenReturn(java.util.stream.Stream.of(Pair.of(reservationWithTransaction, List.of(document))));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getAllDocumentsXls(eventName, response, principal);

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(response.getContentAsString().length() > 0);
    }

    @Test
    void getAllDocumentsXls_emptyInvoicingInfo() throws IOException {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        var eventAndOrgId = mock(alfio.model.EventAndOrganizationId.class);
        when(configurationManager.isItalianEInvoicingEnabled(eventAndOrgId)).thenReturn(true);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getLineSplittedBillingAddress()).thenReturn(List.of("Billing Address Line 1"));
        when(reservation.getVatNr()).thenReturn("123456789");
        when(reservation.getCurrencyCode()).thenReturn("EUR");
        when(reservation.getPaymentMethod()).thenReturn(PaymentProxy.OFFLINE);
        when(reservation.getUserLanguage()).thenReturn("en");

        TicketReservationInvoicingAdditionalInfo emptyInfo = new TicketReservationInvoicingAdditionalInfo(null);

        BillingDetails billingDetails = mock(BillingDetails.class);
        when(billingDetails.getInvoicingAdditionalInfo()).thenReturn(emptyInfo);

        TicketReservationWithTransaction reservationWithTransaction = mock(TicketReservationWithTransaction.class);
        when(reservationWithTransaction.getTicketReservation()).thenReturn(reservation);
        when(reservationWithTransaction.getBillingDetails()).thenReturn(billingDetails);

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.INVOICE);
        when(document.getNumber()).thenReturn("INV-001");
        when(document.getModel()).thenReturn(Map.of("orderSummary", Map.of(
            "totalNetPrice", "100.00",
            "totalVAT", "22.00",
            "totalPrice", "122.00"
        )));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        when(ticketReservationManager.streamAllDocumentsFor(anyInt()))
            .thenReturn(java.util.stream.Stream.of(Pair.of(reservationWithTransaction, List.of(document))));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getAllDocumentsXls(eventName, response, principal);

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(response.getContentAsString().length() > 0);
    }

    @Test
    void addPdfToZip_invoiceType() throws Exception {
        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getUserLanguage()).thenReturn("en");

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.INVOICE);
        when(document.getId()).thenReturn(1L);
        when(document.getNumber()).thenReturn("INV-001");
        when(document.getModel()).thenReturn(Map.of("confirmationDate", "2024-01-15T10:30:00Z"));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        try (var mockedTemplateProcessor = mockStatic(TemplateProcessor.class)) {
            mockedTemplateProcessor.when(() -> TemplateProcessor.buildInvoicePdf(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of("PDF content".getBytes(StandardCharsets.UTF_8)));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOS = new ZipOutputStream(baos)) {
                invokeAddPdfToZip(controller, event, zipOS, reservation, document);
            }

            assertTrue(baos.size() > 0);
        }
    }

    @Test
    void addPdfToZip_receiptType() throws Exception {
        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getUserLanguage()).thenReturn("en");

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.RECEIPT);
        when(document.getId()).thenReturn(1L);
        when(document.getModel()).thenReturn(Map.of("test", "data"));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        try (var mockedTemplateProcessor = mockStatic(TemplateProcessor.class)) {
            mockedTemplateProcessor.when(() -> TemplateProcessor.buildReceiptPdf(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of("PDF content".getBytes(StandardCharsets.UTF_8)));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOS = new ZipOutputStream(baos)) {
                invokeAddPdfToZip(controller, event, zipOS, reservation, document);
            }

            assertTrue(baos.size() > 0);
        }
    }

    @Test
    void addPdfToZip_creditNoteType() throws Exception {
        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getUserLanguage()).thenReturn("en");

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.CREDIT_NOTE);
        when(document.getId()).thenReturn(1L);
        when(document.getNumber()).thenReturn("CN-001");
        when(document.getModel()).thenReturn(Map.of("confirmationDate", "2024-01-15T10:30:00Z"));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        try (var mockedTemplateProcessor = mockStatic(TemplateProcessor.class)) {
            mockedTemplateProcessor.when(() -> TemplateProcessor.buildCreditNotePdf(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of("PDF content".getBytes(StandardCharsets.UTF_8)));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOS = new ZipOutputStream(baos)) {
                invokeAddPdfToZip(controller, event, zipOS, reservation, document);
            }

            assertTrue(baos.size() > 0);
        }
    }

    @Test
    void addPdfToZip_noPdfGenerated() throws Exception {
        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getUserLanguage()).thenReturn("en");

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.INVOICE);
        when(document.getId()).thenReturn(1L);
        when(document.getNumber()).thenReturn("INV-001");
        when(document.getModel()).thenReturn(Map.of("confirmationDate", "2024-01-15T10:30:00Z"));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        try (var mockedTemplateProcessor = mockStatic(TemplateProcessor.class)) {
            mockedTemplateProcessor.when(() -> TemplateProcessor.buildInvoicePdf(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOS = new ZipOutputStream(baos)) {
                invokeAddPdfToZip(controller, event, zipOS, reservation, document);
            }

            // When no PDF is generated, the method doesn't add any entry to the zip.
            // We verify that no entry was added by checking the zip file content.
            // The minimum zip entry is at least 30 bytes, so if pdf is empty, baos should be small.
            // Let's just verify the flow works without errors - the actual size depends on ZipOutputStream internals.
            assertTrue(baos.size() < 30, "Expected small size when no PDF is generated");
        }
    }

    private void invokeAddPdfToZip(EventApiController controller, Event event, ZipOutputStream zipOS, TicketReservation reservation, BillingDocument document) throws Exception {
        Method f = controller.getClass().getDeclaredMethod("addPdfToZip", Event.class, ZipOutputStream.class, TicketReservation.class, BillingDocument.class);
        f.setAccessible(true);
        f.invoke(controller, event, zipOS, reservation, document);
    }

    @Test
    void getAllFields_withoutItalianEInvoicing() {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        var eventAndOrgId = mock(alfio.model.EventAndOrganizationId.class);
        when(eventManager.getEventAndOrganizationId(eventName, "admin")).thenReturn(eventAndOrgId);
        when(configurationManager.isItalianEInvoicingEnabled(eventAndOrgId)).thenReturn(false);
        when(purchaseContextFieldRepository.findFieldsForEvent(eventName)).thenReturn(List.of("custom-field-1", "custom-field-2"));

        var result = controller.getAllFields(eventName, principal);

        assertEquals(FIXED_FIELDS.size() + 2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getKey().equals("custom:custom-field-1")));
        assertTrue(result.stream().anyMatch(p -> p.getKey().equals("custom:custom-field-2")));
    }

    @Test
    void getAllFields_withItalianEInvoicing() {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        var eventAndOrgId = mock(alfio.model.EventAndOrganizationId.class);
        when(eventManager.getEventAndOrganizationId(eventName, "admin")).thenReturn(eventAndOrgId);
        when(configurationManager.isItalianEInvoicingEnabled(eventAndOrgId)).thenReturn(true);
        when(purchaseContextFieldRepository.findFieldsForEvent(eventName)).thenReturn(List.of());

        var result = controller.getAllFields(eventName, principal);

        // FIXED_FIELDS + Italian e-invoicing fields (Fiscal Code, Reference Type, Addressee Code, PEC)
        assertEquals(FIXED_FIELDS.size() + 4, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getKey().equals("Fiscal Code")));
        assertTrue(result.stream().anyMatch(p -> p.getKey().equals("Reference Type")));
        assertTrue(result.stream().anyMatch(p -> p.getKey().equals("Addressee Code")));
        assertTrue(result.stream().anyMatch(p -> p.getKey().equals("PEC")));
    }

    @Test
    void updateExistingCategory_success() {
        int eventId = 1;
        int categoryId = 10;
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        ZonedDateTime now = ZonedDateTime.now();
        when(event.getBegin()).thenReturn(now);
        when(event.getEnd()).thenReturn(now.plusDays(10));
        when(eventManager.getSingleEventById(eventId, "admin")).thenReturn(event);
        doNothing().when(eventManager).updateCategory(anyInt(), anyInt(), any(), any());

        var inception = new alfio.model.modification.DateTimeModification(LocalDate.now(), LocalTime.of(10, 0));
        var expiration = new alfio.model.modification.DateTimeModification(LocalDate.now().plusDays(5), LocalTime.of(18, 0));

        TicketCategoryModification categoryModification = new TicketCategoryModification(
            categoryId, "Updated Category", alfio.model.TicketCategory.TicketAccessType.INHERIT,
            100, inception, expiration, Map.of("en", "Description"),
            BigDecimal.valueOf(50), false, "", true, "", null, null, null, null, null, null, null, null
        );

        var errors = mock(org.springframework.validation.BindingResult.class);
        when(errors.hasErrors()).thenReturn(false);

        var result = controller.updateExistingCategory(eventId, categoryId, categoryModification, errors, principal);

        assertTrue(result.isSuccess());
        verify(eventManager).updateCategory(categoryId, eventId, categoryModification, "admin");
    }

    @Test
    void updateExistingCategory_mismatchedIds() {
        int eventId = 1;
        int categoryId = 10;
        int differentCategoryId = 999;
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(eventManager.getSingleEventById(eventId, "admin")).thenReturn(event);

        TicketCategoryModification categoryModification = new TicketCategoryModification(
            differentCategoryId, "Category", alfio.model.TicketCategory.TicketAccessType.INHERIT,
            100, null, null, Map.of("en", "Description"),
            BigDecimal.valueOf(50), false, "", true, "", null, null, null, null, null, null, null, null
        );

        var errors = mock(org.springframework.validation.BindingResult.class);

        assertThrows(IllegalArgumentException.class, () -> {
            controller.updateExistingCategory(eventId, categoryId, categoryModification, errors, principal);
        });
    }

    @Test
    void getAllInvoices_withDocuments() throws Exception {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("RES001");
        when(reservation.getUserLanguage()).thenReturn("en");

        TicketReservationWithTransaction reservationWithTransaction = mock(TicketReservationWithTransaction.class);
        when(reservationWithTransaction.getTicketReservation()).thenReturn(reservation);

        BillingDocument document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.INVOICE);
        when(document.getId()).thenReturn(1L);
        when(document.getNumber()).thenReturn("INV-001");
        when(document.getModel()).thenReturn(Map.of("confirmationDate", "2024-01-15T10:30:00Z"));
        when(document.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        when(ticketReservationManager.streamAllDocumentsFor(anyInt()))
            .thenReturn(java.util.stream.Stream.of(Pair.of(reservationWithTransaction, List.of(document))));

        try (var mockedTemplateProcessor = mockStatic(TemplateProcessor.class)) {
            mockedTemplateProcessor.when(() -> TemplateProcessor.buildInvoicePdf(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of("PDF content".getBytes(StandardCharsets.UTF_8)));

            MockHttpServletResponse response = new MockHttpServletResponse();
            controller.getAllInvoices(eventName, response, principal);

            assertEquals("application/zip", response.getContentType());
            assertTrue(response.getContentAsString().length() > 0);
        }
    }

    @Test
    void getAllInvoices_emptyDocuments() throws Exception {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));

        when(ticketReservationManager.streamAllDocumentsFor(anyInt()))
            .thenReturn(java.util.stream.Stream.empty());

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.getAllInvoices(eventName, response, principal);

        assertEquals("application/zip", response.getContentType());
        assertTrue(response.getContentAsString().length() > 0);
    }

    @Test
    void getAllInvoices_withMultipleDocumentTypes() throws Exception {
        String eventName = "test-event";
        var principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("admin");

        Event event = mock(Event.class);
        when(event.getShortName()).thenReturn("test-event");
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));

        TicketReservation reservation1 = mock(TicketReservation.class);
        when(reservation1.getId()).thenReturn("RES001");
        when(reservation1.getUserLanguage()).thenReturn("en");

        TicketReservation reservation2 = mock(TicketReservation.class);
        when(reservation2.getId()).thenReturn("RES002");
        when(reservation2.getUserLanguage()).thenReturn("en");

        TicketReservationWithTransaction reservationWithTransaction1 = mock(TicketReservationWithTransaction.class);
        when(reservationWithTransaction1.getTicketReservation()).thenReturn(reservation1);

        TicketReservationWithTransaction reservationWithTransaction2 = mock(TicketReservationWithTransaction.class);
        when(reservationWithTransaction2.getTicketReservation()).thenReturn(reservation2);

        BillingDocument invoice = mock(BillingDocument.class);
        when(invoice.getType()).thenReturn(BillingDocument.Type.INVOICE);
        when(invoice.getId()).thenReturn(1L);
        when(invoice.getNumber()).thenReturn("INV-001");
        when(invoice.getModel()).thenReturn(Map.of("confirmationDate", "2024-01-15T10:30:00Z"));
        when(invoice.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        BillingDocument receipt = mock(BillingDocument.class);
        when(receipt.getType()).thenReturn(BillingDocument.Type.RECEIPT);
        when(receipt.getId()).thenReturn(2L);
        when(receipt.getModel()).thenReturn(Map.of());
        when(receipt.getGenerationTimestamp()).thenReturn(ZonedDateTime.now());

        when(ticketReservationManager.streamAllDocumentsFor(anyInt()))
            .thenReturn(java.util.stream.Stream.of(
                Pair.of(reservationWithTransaction1, List.of(invoice)),
                Pair.of(reservationWithTransaction2, List.of(receipt))
            ));

        try (var mockedTemplateProcessor = mockStatic(TemplateProcessor.class)) {
            mockedTemplateProcessor.when(() -> TemplateProcessor.buildInvoicePdf(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of("PDF content".getBytes(StandardCharsets.UTF_8)));
            mockedTemplateProcessor.when(() -> TemplateProcessor.buildReceiptPdf(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of("PDF content".getBytes(StandardCharsets.UTF_8)));

            MockHttpServletResponse response = new MockHttpServletResponse();
            controller.getAllInvoices(eventName, response, principal);

            assertEquals("application/zip", response.getContentType());
            assertTrue(response.getContentAsString().length() > 0);
        }
    }
}