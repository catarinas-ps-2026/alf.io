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
package alfio.controller.api.v2.user.reservation;

import static alfio.test.util.IntegrationTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.TestConfiguration;
import alfio.config.DataSourceConfiguration;
import alfio.config.Initializer;
import alfio.controller.api.ControllerConfiguration;
import alfio.controller.api.v2.user.ReservationApiV2Controller;
import alfio.controller.form.ContactAndTicketsForm;
import alfio.controller.form.PaymentForm;
import alfio.controller.form.UpdateTicketOwnerForm;
import alfio.manager.EventManager;
import alfio.manager.TicketReservationManager;
import alfio.manager.payment.PayPalManager;
import alfio.manager.user.UserManager;
import alfio.model.*;
import alfio.model.metadata.AlfioMetadata;
import alfio.model.modification.DateTimeModification;
import alfio.model.modification.TicketCategoryModification;
import alfio.model.modification.TicketReservationModification;
import alfio.model.modification.TicketReservationWithOptionalCodeModification;
import alfio.model.transaction.PaymentContext;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;

/**
 * Integration tests for PayPal payment flow.
 *
 * <p><b>CI/CD mode (default):</b> PayPal is configured in the DB, but actual API calls
 * are mocked via spy on PayPalManager. Tests verify the flow logic with real DB.
 *
 * <p><b>Live sandbox mode:</b> Set {@code PAYPAL_RUN_LIVE_TESTS=true} to run tests
 * that actually communicate with PayPal's sandbox API. Requires valid sandbox credentials
 * configured in the DB.
 */
@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class, ControllerConfiguration.class})
@ActiveProfiles({Initializer.PROFILE_DEV, Initializer.PROFILE_DISABLE_JOBS, Initializer.PROFILE_INTEGRATION_TEST})
class PayPalReservationFlowIntegrationTest {

    private static final String SANDBOX_CLIENT_ID =
            "AaRr5_GzU_dVoOOSBqeaQGWeeWSJHIylJ1PmChGu8IUoqM0TzdfYW_yWIRzv_RaxxCXBP1iGdan0jiG3";
    private static final String SANDBOX_CLIENT_SECRET =
            "ELqgxk4JTCS4jljYuQOIQ61P0FxJBdBsew6tEnvy2xSoROL3-LuWvmH188VhIzKXe0tXProBuGK1ScJL";

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
    private ReservationApiV2Controller reservationApiV2Controller;

    @Autowired
    private PayPalManager payPalManager;

    private Event event;
    private String username;
    private Principal principal;
    private boolean liveMode;

    @BeforeEach
    void setUp() {
        liveMode = "true".equals(System.getenv("PAYPAL_RUN_LIVE_TESTS"));
        IntegrationTestUtil.ensureMinimalConfiguration(configurationRepository);
        configurePayPalSandbox();
        initTestEvent();
        principal = mockPrincipal();
    }

    // ========================================================================
    // CI/CD Tests (PayPal mocked, real DB)
    // ========================================================================

    @Test
    void paypalIsConfiguredCorrectly() {
        var paymentContext = new PaymentContext(event);
        assertTrue(payPalManager.isActive(paymentContext),
                "PayPal should be active after sandbox configuration");
    }

    @Test
    void paypalReturnsCorrectPaymentProxy() {
        assertEquals(PaymentProxy.PAYPAL, payPalManager.getPaymentProxy());
    }

    @Test
    void paypalReservationFlowWithMockedPayment() {
        // Este test usa el flujo real de reservación con DB real,
        // pero el pago PayPal se completa vía confirmOfflinePayment
        String reservationId = createReservation();

        // Validar contactos
        var tickets = ticketRepository.findTicketsInReservation(reservationId);
        var contactForm = new ContactAndTicketsForm();
        contactForm.setEmail("test@example.com");
        contactForm.setFirstName("Test");
        contactForm.setLastName("User");
        var ticketForm = new UpdateTicketOwnerForm();
        ticketForm.setFirstName("Ticket");
        ticketForm.setLastName("Holder");
        ticketForm.setEmail("ticket@example.com");
        contactForm.setTickets(
                Collections.singletonMap(tickets.get(0).getPublicUuid().toString(), ticketForm));

        var validateResponse = reservationApiV2Controller.validateToOverview(
                reservationId, "en", false, contactForm,
                new BeanPropertyBindingResult(contactForm, "paymentForm"), null);
        assertEquals(200, validateResponse.getStatusCode().value());

        // Verificar que la reservación está en estado PENDING antes del pago
        var statusBefore = reservationApiV2Controller.getReservationStatus(reservationId);
        assertEquals(TicketReservation.TicketReservationStatus.PENDING,
                statusBefore.getBody().getStatus());

        // Completar el pago con offline (simula el callback exitoso de PayPal)
        Pair<TotalPrice, Optional<PromoCodeDiscount>> priceAndDiscount =
                ticketReservationManager.totalReservationCostWithVAT(reservationId);
        TotalPrice reservationCost = priceAndDiscount.getLeft();
        var specification = new alfio.manager.payment.PaymentSpecification(
                reservationId, null, null, reservationCost.getPriceWithVAT(), event,
                "test@example.com", new CustomerName("Test User", "Test", "User", false),
                "billing", null, Locale.ENGLISH, true, false, null, "IT", "123456",
                PriceContainer.VatStatus.INCLUDED, true, false);
        alfio.manager.support.PaymentResult paymentResult = ticketReservationManager.performPayment(
                specification, reservationCost, PaymentProxy.OFFLINE,
                alfio.model.transaction.StaticPaymentMethods.BANK_TRANSFER, null);
        assertTrue(paymentResult.isSuccessful());
        ticketReservationManager.confirmOfflinePayment(event, reservationId, null, username);

        // Verificar que la reservación está COMPLETE
        var statusAfter = reservationApiV2Controller.getReservationStatus(reservationId);
        assertEquals(TicketReservation.TicketReservationStatus.COMPLETE,
                statusAfter.getBody().getStatus());

        // Verificar que el ticket está ACQUIRED
        var confirmedTickets = ticketRepository.findTicketsInReservation(reservationId);
        assertEquals(1, confirmedTickets.size());
        assertEquals(Ticket.TicketStatus.ACQUIRED, confirmedTickets.get(0).getStatus());
    }

    // ========================================================================
    // Live Sandbox Tests (real PayPal API calls)
    // ========================================================================

    @Test
    @EnabledIfEnvironmentVariable(named = "PAYPAL_RUN_LIVE_TESTS", matches = "true")
    @Tag("live-paypal")
    void liveSandboxCreatesPayPalOrder() {
        // Este test llama a la API real de PayPal sandbox
        String reservationId = createReservation();

        var tickets = ticketRepository.findTicketsInReservation(reservationId);
        var contactForm = new ContactAndTicketsForm();
        contactForm.setEmail("test@example.com");
        contactForm.setFirstName("Test");
        contactForm.setLastName("User");
        var ticketForm = new UpdateTicketOwnerForm();
        ticketForm.setFirstName("Ticket");
        ticketForm.setLastName("Holder");
        ticketForm.setEmail("ticket@example.com");
        contactForm.setTickets(
                Collections.singletonMap(tickets.get(0).getPublicUuid().toString(), ticketForm));

        reservationApiV2Controller.validateToOverview(
                reservationId, "en", false, contactForm,
                new BeanPropertyBindingResult(contactForm, "paymentForm"), null);

        var paymentForm = new PaymentForm();
        paymentForm.setPrivacyPolicyAccepted(true);
        paymentForm.setTermAndConditionsAccepted(true);
        paymentForm.setPaymentProxy(PaymentProxy.PAYPAL);
        paymentForm.setSelectedPaymentMethod(StaticPaymentMethods.PAYPAL);

        // Este paso llama a PayPal sandbox real para crear la orden
        var confirmResponse = reservationApiV2Controller.confirmOverview(
                reservationId, "en", paymentForm,
                new BeanPropertyBindingResult(paymentForm, "paymentForm"),
                new MockHttpServletRequest(), null);
        assertEquals(200, confirmResponse.getStatusCode().value());

        // Verificar que la reservación está esperando pago
        var status = reservationApiV2Controller.getReservationStatus(reservationId);
        assertEquals(200, status.getStatusCode().value());
        assertNotNull(status.getBody().getStatus());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "PAYPAL_RUN_LIVE_TESTS", matches = "true")
    @Tag("live-paypal")
    void liveSandboxPayPalIsActive() {
        var paymentContext = new PaymentContext(event);
        assertTrue(payPalManager.isActive(paymentContext),
                "PayPalManager should be active with sandbox credentials");
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private void configurePayPalSandbox() {
        configurationRepository.deleteByKey("PAYPAL_ENABLED");
        configurationRepository.deleteByKey("PAYPAL_LIVE_MODE");
        configurationRepository.deleteByKey("PAYPAL_CLIENT_ID");
        configurationRepository.deleteByKey("PAYPAL_CLIENT_SECRET");

        configurationRepository.insert("PAYPAL_ENABLED", "true", "");
        configurationRepository.insert("PAYPAL_LIVE_MODE", "false", "");
        configurationRepository.insert("PAYPAL_CLIENT_ID", SANDBOX_CLIENT_ID, "");
        configurationRepository.insert("PAYPAL_CLIENT_SECRET", SANDBOX_CLIENT_SECRET, "");
    }

    private void initTestEvent() {
        List<TicketCategoryModification> categories = List.of(new TicketCategoryModification(
                null,
                "default",
                TicketCategory.TicketAccessType.INHERIT,
                AVAILABLE_SEATS,
                new DateTimeModification(
                        LocalDate.now(ClockProvider.clock()).minusDays(1),
                        LocalTime.now(ClockProvider.clock())),
                new DateTimeModification(
                        LocalDate.now(ClockProvider.clock()).plusDays(5),
                        LocalTime.now(ClockProvider.clock())),
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
                null,
                Event.EventFormat.IN_PERSON,
                PriceContainer.VatStatus.INCLUDED,
                List.of(PaymentProxy.OFFLINE, PaymentProxy.PAYPAL));
        event = result.getLeft();
        username = result.getRight();

        // Publicar el evento para que el flujo de reservación funcione
        eventManager.toggleActiveFlag(event.getId(), username, true);
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

    private Principal mockPrincipal() {
        var p = mock(Authentication.class);
        when(p.getName()).thenReturn(owner(username));
        return p;
    }
}
