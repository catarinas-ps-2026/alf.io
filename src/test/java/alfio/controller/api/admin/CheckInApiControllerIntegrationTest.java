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
import alfio.manager.EventManager;
import alfio.manager.TicketReservationManager;
import alfio.manager.payment.PaymentSpecification;
import alfio.manager.support.CheckInStatistics;
import alfio.manager.support.CheckInStatus;
import alfio.manager.support.PaymentResult;
import alfio.manager.support.TicketAndCheckInResult;
import alfio.manager.user.UserManager;
import alfio.model.*;
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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class, ControllerConfiguration.class})
@ActiveProfiles({Initializer.PROFILE_DEV, Initializer.PROFILE_DISABLE_JOBS, Initializer.PROFILE_INTEGRATION_TEST})
class CheckInApiControllerIntegrationTest {

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
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private CheckInApiController checkInApiController;

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
    // C1: GET /admin/api/check-in/event/{name}/attendees
    // ========================================================================

    @Test
    void searchAttendeesWithValidQueryReturnsResults() {
        String reservationId = createAndConfirmReservation();
        var tickets = ticketRepository.findTicketsInReservation(reservationId);
        var ticket = tickets.get(0);

        // the checkin view requires first_name, last_name, email_address to be non-empty
        jdbcTemplate.update(
                "update ticket set first_name = 'John', last_name = 'Doe', email_address = 'john@example.com' where uuid = :uuid",
                Map.of("uuid", ticket.getUuid()));

        var response = checkInApiController.searchAttendees(event.getShortName(), "john@example.com", 0, principal);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().totalResults() > 0);
    }

    @Test
    void searchAttendeesWithBlankQueryReturnsNoContent() {
        var response = checkInApiController.searchAttendees(event.getShortName(), "   ", 0, principal);
        assertEquals(204, response.getStatusCode().value());
    }

    // ========================================================================
    // C2: POST /admin/api/check-in/event/{name}/ticket/{id}
    // ========================================================================

    @Test
    void checkInTicketWithValidCode() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);
        String ticketCode = ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive());

        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode(ticketCode);

        TicketAndCheckInResult result = checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);
        assertEquals(CheckInStatus.SUCCESS, result.getResult().getStatus());
    }

    @Test
    void checkInTicketWithWrongCodeReturnsInvalidTicketCode() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);

        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode("WRONG_CODE_12345");

        TicketAndCheckInResult result = checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);
        assertEquals(CheckInStatus.INVALID_TICKET_CODE, result.getResult().getStatus());
    }

    @Test
    void checkInAlreadyCheckedInTicketReturnsAlreadyCheckedIn() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);
        String ticketCode = ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive());

        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode(ticketCode);

        checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);

        TicketAndCheckInResult secondResult =
                checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);
        assertEquals(CheckInStatus.ALREADY_CHECK_IN, secondResult.getResult().getStatus());
    }

    // CPF-API-04-003: Ticket cuya reserva está en estado PENDING (sin confirmar pago)
    // → el check-in debe retornar INVALID_TICKET_STATE.
    // Ref: Diseño de Casos de Prueba Funcionales §7.B / CPF-API-04
    @Test
    void checkInTicketWithPendingReservationReturnsInvalidState() {
        // Crear reserva SIN confirmar el pago (estado PENDING)
        String reservationId = createReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);
        // Calcular el código de ticket (el código existe, pero el ticket no está ACQUIRED)
        String ticketCode = ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive());

        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode(ticketCode);

        TicketAndCheckInResult result = checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);

        // CPF-API-04-003: ticket no pagado → estado inválido para check-in
        assertEquals(CheckInStatus.INVALID_TICKET_STATE, result.getResult().getStatus());
    }

    // ========================================================================
    // C3: GET /admin/api/check-in/event/{name}/ticket/{id}/status
    // ========================================================================

    @Test
    void getTicketStatusBeforeCheckIn() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);

        var status = checkInApiController.getTicketStatus(event.getShortName(), ticket.getUuid(), principal);
        assertNotNull(status);
        assertEquals(CheckInStatus.OK_READY_TO_BE_CHECKED_IN, status.getCheckInStatus());
    }

    @Test
    void getTicketStatusAfterCheckIn() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);
        String ticketCode = ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive());

        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode(ticketCode);
        checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);

        var status = checkInApiController.getTicketStatus(event.getShortName(), ticket.getUuid(), principal);
        assertNotNull(status);
        assertEquals(CheckInStatus.ALREADY_CHECK_IN, status.getCheckInStatus());
    }

    // ========================================================================
    // C4: GET /admin/api/check-in/event/{name}/statistics
    // ========================================================================

    @Test
    void getCheckInStatisticsReturnsStats() {
        configurationRepository.insertEventLevel(
                event.getOrganizationId(), event.getId(), "CHECK_IN_STATS", "true", "");

        CheckInStatistics stats = checkInApiController.getStatistics(event.getShortName(), null, principal);
        assertNotNull(stats);
    }

    @Test
    void getCheckInStatisticsWithNoCheckIns() {
        configurationRepository.insertEventLevel(
                event.getOrganizationId(), event.getId(), "CHECK_IN_STATS", "true", "");

        CheckInStatistics stats = checkInApiController.getStatistics(event.getShortName(), null, principal);
        assertNotNull(stats);
        assertEquals(0, stats.getCheckedIn());
    }

    // ========================================================================
    // C5: POST /admin/api/check-in/event/{name}/bulk
    // ========================================================================

    @Test
    void bulkCheckInMultipleTickets() {
        String reservationId = createAndConfirmReservation(2);
        var allTickets = ticketRepository.findTicketsInReservation(reservationId);
        assertEquals(2, allTickets.size());

        var ticketIdentifierCodes = new ArrayList<CheckInApiController.TicketIdentifierCode>();
        for (var ticket : allTickets) {
            var tic = new CheckInApiController.TicketIdentifierCode();
            tic.setIdentifier(ticket.getUuid());
            tic.setCode(ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive()));
            ticketIdentifierCodes.add(tic);
        }

        var response =
                checkInApiController.bulkCheckIn(event.getShortName(), ticketIdentifierCodes, null, false, principal);
        assertNotNull(response);
        assertEquals(2, response.size());
        for (var entry : response.values()) {
            assertEquals(CheckInStatus.SUCCESS, entry.getResult().getStatus());
        }
    }

    @Test
    void bulkCheckInWithInvalidTicketReturnsError() {
        var tic = new CheckInApiController.TicketIdentifierCode();
        tic.setIdentifier("nonexistent-uuid");
        tic.setCode("WRONG_CODE");

        var response = checkInApiController.bulkCheckIn(event.getShortName(), List.of(tic), null, false, principal);
        assertNotNull(response);
        assertEquals(1, response.size());
        var result = response.get("nonexistent-uuid");
        assertNotNull(result);
        assertNotEquals(CheckInStatus.SUCCESS, result.getResult().getStatus());
    }

    // ========================================================================
    // C6: POST /admin/api/check-in/event/{name}/ticket/{id}/confirm-on-site-payment
    // ========================================================================

    @Test
    void confirmOnSitePaymentConvertsToAcquired() {
        String reservationId = createReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);
        jdbcTemplate.update(
                "update ticket set status = 'TO_BE_PAID' where uuid = :uuid", Map.of("uuid", ticket.getUuid()));

        String ticketCode = ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive());
        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode(ticketCode);

        var result =
                checkInApiController.confirmOnSitePayment(event.getShortName(), ticket.getUuid(), tc, null, principal);
        assertNotNull(result);
        assertEquals(CheckInStatus.SUCCESS, result.getResult().getStatus());
    }

    @Test
    void confirmOnSitePaymentForNonToBePaidTicket() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);

        String ticketCode = ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive());
        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode(ticketCode);

        var result =
                checkInApiController.confirmOnSitePayment(event.getShortName(), ticket.getUuid(), tc, null, principal);
        assertNotNull(result);
        assertNotEquals(CheckInStatus.SUCCESS, result.getResult().getStatus());
    }

    // ========================================================================
    // E5: POST /admin/api/check-in/event/{name}/ticket/{id}/revert-check-in
    // ========================================================================

    @Test
    void revertCheckInRestoresTicketStatus() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);
        String ticketCode = ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive());

        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode(ticketCode);
        checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);

        boolean reverted = checkInApiController.revertCheckIn(event.getId(), ticket.getUuid(), principal);
        assertTrue(reverted);

        var status = checkInApiController.getTicketStatus(event.getShortName(), ticket.getUuid(), principal);
        assertEquals(CheckInStatus.OK_READY_TO_BE_CHECKED_IN, status.getCheckInStatus());
    }

    @Test
    void revertCheckInForNonCheckedInTicketReturnsFalse() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);

        boolean reverted = checkInApiController.revertCheckIn(event.getId(), ticket.getUuid(), principal);
        assertFalse(reverted);
    }

    // ========================================================================
    // E8: Double check-in returns ALREADY_CHECKED_IN
    // ========================================================================

    @Test
    void doubleCheckInReturnsAlreadyCheckedIn() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);
        String ticketCode = ticket.ticketCode(event.getPrivateKey(), event.supportsQRCodeCaseInsensitive());

        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode(ticketCode);

        var firstResult = checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);
        assertEquals(CheckInStatus.SUCCESS, firstResult.getResult().getStatus());

        var secondResult = checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);
        assertEquals(CheckInStatus.ALREADY_CHECK_IN, secondResult.getResult().getStatus());
    }

    // ========================================================================
    // E9: Wrong QR code returns INVALID_TICKET_CODE
    // ========================================================================

    @Test
    void wrongQrCodeReturnsInvalidTicketCode() {
        String reservationId = createAndConfirmReservation();
        var ticket = ticketRepository.findTicketsInReservation(reservationId).get(0);

        CheckInApiController.TicketCode tc = new CheckInApiController.TicketCode();
        tc.setCode("COMPLETELY_WRONG_CODE");

        var result = checkInApiController.checkIn(event.getId(), ticket.getUuid(), tc, principal);
        assertEquals(CheckInStatus.INVALID_TICKET_CODE, result.getResult().getStatus());
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
        return createReservation(1);
    }

    private String createReservation(int quantity) {
        var categories = ticketCategoryRepository.findAllTicketCategories(event.getId());
        TicketReservationModification tr = new TicketReservationModification();
        tr.setQuantity(quantity);
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
        return createAndConfirmReservation(1);
    }

    private String createAndConfirmReservation(int quantity) {
        String reservationId = createReservation(quantity);
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
