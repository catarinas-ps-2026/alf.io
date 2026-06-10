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

import alfio.controller.api.support.PageAndContent;
import alfio.manager.AccessService;
import alfio.manager.PaymentManager;
import alfio.manager.PurchaseContextManager;
import alfio.manager.PurchaseContextSearchManager;
import alfio.manager.system.ConfigurationManager;
import alfio.model.PurchaseContext;
import alfio.model.ReservationPaymentDetail;
import alfio.model.modification.DateTimeModification;
import alfio.model.modification.TransactionMetadataModification;
import alfio.model.system.ConfigurationKeys;
import alfio.model.system.ConfigurationPathLevel;
import alfio.model.system.ConfigurationKeyValuePathLevel;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPaymentsApiControllerTest {

    @Mock
    private PurchaseContextSearchManager purchaseContextSearchManager;

    @Mock
    private PurchaseContextManager purchaseContextManager;

    @Mock
    private PaymentManager paymentManager;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private AccessService accessService;

    @Mock
    private Principal principal;

    private AdminPaymentsApiController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminPaymentsApiController(
            purchaseContextSearchManager,
            purchaseContextManager,
            paymentManager,
            configurationManager,
            accessService
        );
    }

    @Test
    void getPaymentsForPurchaseContext_withValidContext_returnsPayments() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        List<ReservationPaymentDetail> payments = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsFor(context, null, null))
            .thenReturn(Pair.of(payments, 0));

        PageAndContent<List<ReservationPaymentDetail>> result = controller.getPaymentsForPurchaseContext(
            PurchaseContext.PurchaseContextType.event, "event-123", null, null, principal
        );

        verify(accessService).checkOrganizationOwnership(principal, 1);
        assertNotNull(result);
    }

    @Test
    void getPaymentsForPurchaseContext_withPage_passesPageParameter() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);

        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        List<ReservationPaymentDetail> payments = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsFor(context, 0, null))
            .thenReturn(Pair.of(payments, 10));

        PageAndContent<List<ReservationPaymentDetail>> result = controller.getPaymentsForPurchaseContext(
            PurchaseContext.PurchaseContextType.event, "event-123", 0, null, principal
        );

        verify(purchaseContextSearchManager).findAllPaymentsFor(context, 0, null);
    }

    @Test
    void getPaymentsForPurchaseContext_withSearch_passesSearchParameter() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        List<ReservationPaymentDetail> payments = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsFor(context, null, "search-term"))
            .thenReturn(Pair.of(payments, 1));

        PageAndContent<List<ReservationPaymentDetail>> result = controller.getPaymentsForPurchaseContext(
            PurchaseContext.PurchaseContextType.event, "event-123", null, "search-term", principal
        );

        verify(purchaseContextSearchManager).findAllPaymentsFor(context, null, "search-term");
    }

    @Test
    void getPaymentsForPurchaseContext_withNonExistentContext_returnsEmpty() {
        doReturn(Optional.empty())
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        PageAndContent<List<ReservationPaymentDetail>> result = controller.getPaymentsForPurchaseContext(
            PurchaseContext.PurchaseContextType.event, "non-existent", null, null, principal
        );

        assertNotNull(result);
        assertTrue(result.getLeft().isEmpty());
        assertEquals(0, result.getRight());
    }

    @Test
    void getPaymentsForPurchaseContext_withSubscriptionType_works() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(2);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        List<ReservationPaymentDetail> payments = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsFor(context, null, null))
            .thenReturn(Pair.of(payments, 0));

        PageAndContent<List<ReservationPaymentDetail>> result = controller.getPaymentsForPurchaseContext(
            PurchaseContext.PurchaseContextType.subscription, "sub-123", null, null, principal
        );

        verify(accessService).checkOrganizationOwnership(principal, 2);
    }

    @Test
    void updateTransactionData_withValidData_updatesAndReturns() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        TransactionMetadataModification modification =
            new TransactionMetadataModification(
                null,
                "test notes"
            );

        var result = controller.updateTransactionData(
            PurchaseContext.PurchaseContextType.event, "event-123", "res-123", modification, principal
        );

        verify(accessService).checkOrganizationOwnership(principal, 1);
        verify(paymentManager).updateTransactionDetails(eq("res-123"), eq("test notes"), isNull(), eq(principal));
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("OK", result.getBody());
    }

    @Test
    void updateTransactionData_withTimestamp_updatesWithTimestamp() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        when(context.getZoneId()).thenReturn(ZoneId.of("UTC"));
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        DateTimeModification timestamp =
            new DateTimeModification(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 30)
            );

        TransactionMetadataModification modification =
            new TransactionMetadataModification(
                timestamp,
                "test notes"
            );

        var result = controller.updateTransactionData(
            PurchaseContext.PurchaseContextType.event, "event-123", "res-123", modification, principal
        );

        verify(paymentManager).updateTransactionDetails(eq("res-123"), eq("test notes"), any(ZonedDateTime.class), eq(principal));
    }

    @Test
    void updateTransactionData_withNonExistentContext_returnsEmpty() {
        doReturn(Optional.empty())
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        TransactionMetadataModification modification =
            new TransactionMetadataModification(
                null,
                null
            );
        var result = controller.updateTransactionData(
            PurchaseContext.PurchaseContextType.event, "non-existent", "res-123", modification, principal
        );

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void updateTransactionData_withIllegalArgument_returnsBadRequest() {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        doThrow(new IllegalArgumentException("Invalid argument"))
            .when(paymentManager).updateTransactionDetails(any(), any(), any(), any());

        TransactionMetadataModification modification =
            new TransactionMetadataModification(
                null,
                null
            );
        var result = controller.updateTransactionData(
            PurchaseContext.PurchaseContextType.event, "event-123", "res-123", modification, principal
        );

        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    void exportPayments_withValidContext_exportsExcel() throws IOException {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        when(context.getDisplayName()).thenReturn("Test Event");
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        ConfigurationKeyValuePathLevel value = new ConfigurationKeyValuePathLevel(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID.getValue(),
                "false",
                ConfigurationPathLevel.SYSTEM
            );

        ConfigurationManager.MaybeConfiguration config =
            new ConfigurationManager.MaybeConfiguration(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID,
                value
            );

        when(configurationManager.getFor(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID,
                null))
            .thenReturn(config);

        List<ReservationPaymentDetail> details = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsForExport(context, null))
            .thenReturn(details);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportPayments(
            PurchaseContext.PurchaseContextType.event, "event-123", null, principal, response
        );

        verify(accessService).checkOrganizationOwnership(principal, 1);
        verify(configurationManager).getFor(ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID, null);
    }

    @Test
    void exportPayments_withSearch_includesSearchParameter() throws IOException {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        when(context.getDisplayName()).thenReturn("Test Event");
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        ConfigurationKeyValuePathLevel config =
            new ConfigurationKeyValuePathLevel(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID.getValue(),
                "false",
                ConfigurationPathLevel.SYSTEM
            );

        ConfigurationManager.MaybeConfiguration maybeConfig =
            new ConfigurationManager.MaybeConfiguration(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID,
                config
            );

        when(configurationManager.getFor(
                eq(ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID),
                any()))
            .thenReturn(maybeConfig);

        List<ReservationPaymentDetail> details = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsForExport(context, "search-term"))
            .thenReturn(details);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportPayments(
            PurchaseContext.PurchaseContextType.event, "event-123", "search-term", principal, response
        );

        verify(purchaseContextSearchManager).findAllPaymentsForExport(context, "search-term");
    }

    @Test
    void exportPayments_withUseInvoiceNumberAsId_true_usesInvoiceNumber() throws IOException {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        when(context.getDisplayName()).thenReturn("Test Event");
        when(context.getConfigurationLevel()).thenReturn(null);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        ConfigurationKeyValuePathLevel config =
            new ConfigurationKeyValuePathLevel(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID.getValue(),
                "true",
                ConfigurationPathLevel.SYSTEM
            );

        ConfigurationManager.MaybeConfiguration maybeConfig =
            new ConfigurationManager.MaybeConfiguration(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID,
                config
            );

        when(configurationManager.getFor(
                eq(ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID),
                any()))
            .thenReturn(maybeConfig);

        List<ReservationPaymentDetail> details = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsForExport(context, null))
            .thenReturn(details);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportPayments(
            PurchaseContext.PurchaseContextType.event, "event-123", null, principal, response
        );

        verify(configurationManager).getFor(ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID, null);
    }

    @Test
    void exportPayments_withNonExistentContext_returnsPreconditionRequired() throws IOException {
        doReturn(Optional.empty())
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportPayments(
            PurchaseContext.PurchaseContextType.event, "non-existent", null, principal, response
        );

        assertEquals(HttpStatus.PRECONDITION_REQUIRED.value(), response.getStatus());
        assertEquals("text/plain", response.getContentType());
    }

    @Test
    void exportPayments_withNonExistentContext_writesErrorMessage() throws IOException {
        doReturn(Optional.empty())
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportPayments(
            PurchaseContext.PurchaseContextType.event, "non-existent", null, principal, response
        );

        assertEquals("No payments found", response.getContentAsString());
    }

    @Test
    void exportPayments_withMultiplePaymentDetails_exportsAll() throws IOException {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        when(context.getDisplayName()).thenReturn("Test Event");
        when(context.getZoneId()).thenReturn(ZoneId.of("UTC"));
        when(context.getConfigurationLevel()).thenReturn(null);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        ConfigurationKeyValuePathLevel config =
            new ConfigurationKeyValuePathLevel(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID.getValue(),
                "false",
                ConfigurationPathLevel.SYSTEM
            );

        ConfigurationManager.MaybeConfiguration maybeConfig =
            new ConfigurationManager.MaybeConfiguration(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID,
                config
            );

        when(configurationManager.getFor(
                eq(ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID),
                any()))
            .thenReturn(maybeConfig);

        List<ReservationPaymentDetail> details = List.of(
            new ReservationPaymentDetail(
                "1",
                "John",
                "Doe",
                "john@example.com",
                "STRIPE",
                10000,
                "EUR",
                ZonedDateTime.parse("2024-01-15T10:30:00Z"),
                "Payment notes",
                null
            ),
            new ReservationPaymentDetail(
                "2",
                "Jane",
                "Smith",
                "jane@example.com",
                "PAYPAL",
                20000,
                "EUR",
                ZonedDateTime.parse("2024-01-16T11:45:00Z"),
                "Other notes",
                null
            )
        );
        when(purchaseContextSearchManager.findAllPaymentsForExport(context, null))
            .thenReturn(details);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportPayments(
            PurchaseContext.PurchaseContextType.event, "event-123", null, principal, response
        );

        verify(purchaseContextSearchManager).findAllPaymentsForExport(context, null);
    }

    @Test
    void exportPayments_withNullInvoiceNumber_usesReservationId() throws IOException {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(1);
        when(context.getDisplayName()).thenReturn("Test Event");
        when(context.getConfigurationLevel()).thenReturn(null);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        ConfigurationKeyValuePathLevel config =
            new ConfigurationKeyValuePathLevel(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID.getValue(),
                "false",
                ConfigurationPathLevel.SYSTEM
            );

        ConfigurationManager.MaybeConfiguration maybeConfig =
            new ConfigurationManager.MaybeConfiguration(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID,
                config
            );

        when(configurationManager.getFor(
                eq(ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID),
                any()))
            .thenReturn(maybeConfig);

        List<ReservationPaymentDetail> details = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsForExport(context, null))
            .thenReturn(details);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportPayments(
            PurchaseContext.PurchaseContextType.event, "event-123", null, principal, response
        );

        verify(configurationManager).getFor(ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID, null);
    }

    @Test
    void exportPayments_withSubscriptionType_works() throws IOException {
        PurchaseContext context = mock(PurchaseContext.class);
        when(context.getOrganizationId()).thenReturn(2);
        when(context.getDisplayName()).thenReturn("Test Subscription");
        when(context.getConfigurationLevel()).thenReturn(null);
        doReturn(Optional.of(context))
            .when(purchaseContextManager)
            .findBy(any(), anyString());

        ConfigurationKeyValuePathLevel config =
            new ConfigurationKeyValuePathLevel(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID.getValue(),
                "false",
                ConfigurationPathLevel.SYSTEM
            );

        ConfigurationManager.MaybeConfiguration maybeConfig =
            new ConfigurationManager.MaybeConfiguration(
                ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID,
                config
            );

        when(configurationManager.getFor(
                eq(ConfigurationKeys.USE_INVOICE_NUMBER_AS_ID),
                any()))
            .thenReturn(maybeConfig);

        List<ReservationPaymentDetail> details = new ArrayList<>();
        when(purchaseContextSearchManager.findAllPaymentsForExport(context, null))
            .thenReturn(details);

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportPayments(
            PurchaseContext.PurchaseContextType.subscription, "sub-123", null, principal, response
        );

        verify(accessService).checkOrganizationOwnership(principal, 2);
    }
}
