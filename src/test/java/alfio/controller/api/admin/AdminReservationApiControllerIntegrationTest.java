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

import static alfio.test.util.IntegrationTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import alfio.TestConfiguration;
import alfio.config.DataSourceConfiguration;
import alfio.config.Initializer;
import alfio.controller.api.ControllerConfiguration;
import alfio.manager.AccessService;
import alfio.manager.EventManager;
import alfio.manager.TicketReservationManager;
import alfio.manager.support.AccessDeniedException;
import alfio.manager.payment.PaymentSpecification;
import alfio.manager.support.PaymentResult;
import alfio.manager.user.UserManager;
import alfio.model.*;
import alfio.model.PurchaseContext.PurchaseContextType;
import alfio.model.metadata.AlfioMetadata;
import alfio.model.modification.DateTimeModification;
import alfio.model.modification.TicketCategoryModification;
import alfio.model.modification.TicketReservationModification;
import alfio.model.modification.TicketReservationWithOptionalCodeModification;
import alfio.model.transaction.PaymentProxy;
import alfio.model.transaction.StaticPaymentMethods;
import alfio.repository.*;
import alfio.repository.system.ConfigurationRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.test.util.AlfioIntegrationTest;
import alfio.test.util.IntegrationTestUtil;
import alfio.util.ClockProvider;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@AlfioIntegrationTest
@ContextConfiguration(
        classes = {
            DataSourceConfiguration.class,
            TestConfiguration.class,
            ControllerConfiguration.class,
        })
@ActiveProfiles({
    Initializer.PROFILE_DEV,
    Initializer.PROFILE_DISABLE_JOBS,
    Initializer.PROFILE_INTEGRATION_TEST,
})
class AdminReservationApiControllerIntegrationTest {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserManager userManager;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketReservationManager ticketReservationManager;

    @Autowired
    private AdminReservationApiController adminReservationApiController;

    @Autowired
    private TicketReservationRepository ticketReservationRepository;

    private Event event;
    private String username;
    private Principal principal;

    @BeforeEach
    void setUp() {
        IntegrationTestUtil.ensureMinimalConfiguration(configurationRepository);
        initTestEvent();
        principal = mockPrincipal();
    }

    // ========================================================================
    // A4: GET /admin/api/reservation/{type}/{id}/reservations/list
    // ========================================================================

    @Test
    void listReservationsReturnsResults() {
        String reservationId = createAndConfirmReservation();

        var response = adminReservationApiController.findAll(
                PurchaseContextType.event, event.getShortName(), 0, null, null, principal);
        assertNotNull(response);
        var reservations = response.getLeft();
        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
        assertEquals(1, reservations.size());

        // Verificar que la reservación retornada coincide con la creada
        var found = reservations.get(0);
        assertEquals(reservationId, found.getId());
        assertEquals(TicketReservation.TicketReservationStatus.COMPLETE, found.getStatus());
        assertEquals("email@example.com", found.getEmail());
    }

    @Test
    void listReservationsWithPaginationReturnsPageZero() {
        createAndConfirmReservation();
        createAndConfirmReservation();

        var response = adminReservationApiController.findAll(
                PurchaseContextType.event, event.getShortName(), 0, null, null, principal);
        assertNotNull(response);
        // Verificar count total
        assertTrue(response.getRight() >= 2);
        // Verificar que la lista tiene los elementos esperados
        assertNotNull(response.getLeft());
        assertEquals(2, response.getLeft().size());
    }

    @Test
    void listReservationsWithSearchFilter() {
        String reservationId = createAndConfirmReservation();

        // Buscar por los primeros 5 caracteres del ID
        var response = adminReservationApiController.findAll(
                PurchaseContextType.event, event.getShortName(), 0, reservationId.substring(0, 5), null, principal);
        assertNotNull(response);
        var reservations = response.getLeft();
        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());

        // Verificar que el filtro funciona: el resultado contiene la reservación buscada
        assertTrue(reservations.stream().anyMatch(r -> r.getId().equals(reservationId)));
    }

    @Test
    void listReservationsWithStatusFilter() {
        createAndConfirmReservation();

        var response = adminReservationApiController.findAll(
                PurchaseContextType.event,
                event.getShortName(),
                0,
                null,
                List.of(TicketReservation.TicketReservationStatus.COMPLETE),
                principal);
        assertNotNull(response);
        var reservations = response.getLeft();
        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());

        // Verificar que TODOS los resultados tienen el status filtrado
        assertTrue(reservations.stream()
                .allMatch(r -> r.getStatus() == TicketReservation.TicketReservationStatus.COMPLETE));
    }

    // ========================================================================
    // A7: POST /admin/api/reservation/{type}/{id}/{reservationId}/refund
    // ========================================================================

    @Test
    void refundPaidReservationSuccessfully() {
        String reservationId = createAndConfirmReservation();

        // Verificar estado BEFORE en DB
        var reservationBefore = ticketReservationRepository.findOptionalReservationById(reservationId);
        assertTrue(reservationBefore.isPresent());
        assertEquals(TicketReservation.TicketReservationStatus.COMPLETE, reservationBefore.get().getStatus());

        var refundAmount = new AdminReservationApiController.RefundAmount("10.00");
        var response = adminReservationApiController.refund(
                PurchaseContextType.event, event.getShortName(), reservationId, refundAmount, principal);
        assertNotNull(response);
        assertTrue(response.isSuccess());

        // Para pagos OFFLINE, la reservación sigue existiendo después del reembolso
        var reservationAfter = ticketReservationRepository.findOptionalReservationById(reservationId);
        assertTrue(reservationAfter.isPresent());
    }

    @Test
    void refundPartialAmount() {
        String reservationId = createAndConfirmReservation();

        var refundAmount = new AdminReservationApiController.RefundAmount("5.00");
        var response = adminReservationApiController.refund(
                PurchaseContextType.event, event.getShortName(), reservationId, refundAmount, principal);
        assertNotNull(response);
        assertTrue(response.isSuccess());

        // Verificar en DB que la reservación sigue existiendo con status COMPLETE
        // (un reembolso parcial no cancela la reservación)
        var reservationAfter = ticketReservationRepository.findOptionalReservationById(reservationId);
        assertTrue(reservationAfter.isPresent());
        assertEquals(TicketReservation.TicketReservationStatus.COMPLETE, reservationAfter.get().getStatus());
    }

    @Test
    void refundNonexistentReservationReturnsError() {
        var refundAmount = new AdminReservationApiController.RefundAmount("10.00");
        assertThrows(AccessDeniedException.class, () -> adminReservationApiController.refund(
                PurchaseContextType.event, event.getShortName(), "nonexistent-id", refundAmount, principal));
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private void initTestEvent() {
        List<TicketCategoryModification> categories = List.of(new TicketCategoryModification(
                null,
                "default",
                TicketCategory.TicketAccessType.INHERIT,
                AVAILABLE_SEATS,
                new DateTimeModification(
                        LocalDate.now(ClockProvider.clock()).minusDays(1), LocalTime.now(ClockProvider.clock())),
                new DateTimeModification(
                        LocalDate.now(ClockProvider.clock()).plusDays(5), LocalTime.now(ClockProvider.clock())),
                DESCRIPTION,
                BigDecimal.TEN,
                false,
                "",
                false,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                AlfioMetadata.empty()));
        var result = initEvent(
                categories,
                organizationRepository,
                userManager,
                eventManager,
                eventRepository,
                List.of(),
                Event.EventFormat.IN_PERSON);
        event = result.getLeft();
        username = result.getRight();
    }

    private String createReservation() {
        var categories = ticketCategoryRepository.findAllTicketCategories(event.getId());
        TicketReservationModification tr = new TicketReservationModification();
        tr.setQuantity(1);
        tr.setTicketCategoryId(categories.get(0).getId());
        var tickets = new TicketReservationWithOptionalCodeModification(tr, Optional.empty());
        return ticketReservationManager.createTicketReservation(
                event,
                List.of(tickets),
                List.of(),
                DateUtils.addDays(new Date(), 1),
                Optional.empty(),
                Locale.ENGLISH,
                false,
                null);
    }

    private String createAndConfirmReservation() {
        String reservationId = createReservation();
        Pair<TotalPrice, Optional<PromoCodeDiscount>> priceAndDiscount =
                ticketReservationManager.totalReservationCostWithVAT(reservationId);
        TotalPrice reservationCost = priceAndDiscount.getLeft();
        PaymentSpecification specification = new PaymentSpecification(
                reservationId,
                null,
                null,
                reservationCost.getPriceWithVAT(),
                event,
                "email@example.com",
                new CustomerName("full name", "full", "name", event.mustUseFirstAndLastName()),
                "billing address",
                null,
                Locale.ENGLISH,
                true,
                false,
                null,
                "IT",
                "123456",
                PriceContainer.VatStatus.INCLUDED,
                true,
                false);
        PaymentResult result = ticketReservationManager.performPayment(
                specification, reservationCost, PaymentProxy.OFFLINE, StaticPaymentMethods.BANK_TRANSFER, null);
        assertTrue(result.isSuccessful());
        ticketReservationManager.confirmOfflinePayment(event, reservationId, null, username);
        return reservationId;
    }

    private Principal mockPrincipal() {
        var p = mock(Authentication.class);
        when(p.getName()).thenReturn(owner(username));
        return p;
    }
}
