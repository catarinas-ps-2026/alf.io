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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import alfio.TestConfiguration;
import alfio.config.DataSourceConfiguration;
import alfio.config.Initializer;
import alfio.controller.api.ControllerConfiguration;
import alfio.controller.api.admin.PassedIdDoesNotExistException;
import alfio.controller.api.v2.user.ReservationApiV2Controller;
import alfio.controller.form.ContactAndTicketsForm;
import alfio.controller.form.PaymentForm;
import alfio.controller.form.UpdateTicketOwnerForm;
import alfio.manager.EventManager;
import alfio.manager.TicketReservationManager;
import alfio.manager.payment.PaymentSpecification;
import alfio.manager.payment.custom.offline.CustomOfflineConfigurationManager;
import alfio.manager.payment.custom.offline.CustomOfflineConfigurationManager.CustomOfflinePaymentMethodAlreadyExistsException;
import alfio.manager.payment.custom.offline.CustomOfflineConfigurationManager.CustomOfflinePaymentMethodDoesNotExistException;
import alfio.manager.user.UserManager;
import alfio.model.CustomerName;
import alfio.model.Event;
import alfio.model.PriceContainer;
import alfio.model.TicketCategory;
import alfio.model.TicketReservation;
import alfio.model.TotalPrice;
import alfio.model.metadata.AlfioMetadata;
import alfio.model.modification.DateTimeModification;
import alfio.model.modification.TicketCategoryModification;
import alfio.model.modification.TicketReservationWithOptionalCodeModification;
import alfio.model.transaction.PaymentProxy;
import alfio.model.transaction.UserDefinedOfflinePaymentMethod;
import alfio.model.user.Organization;
import alfio.repository.EventRepository;
import alfio.repository.TicketCategoryRepository;
import alfio.repository.TicketRepository;
import alfio.repository.TicketReservationRepository;
import alfio.repository.system.ConfigurationRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.test.util.AlfioIntegrationTest;
import alfio.test.util.IntegrationTestUtil;
import alfio.util.ClockProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;

@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class, ControllerConfiguration.class})
@ActiveProfiles({Initializer.PROFILE_DEV, Initializer.PROFILE_DISABLE_JOBS, Initializer.PROFILE_INTEGRATION_TEST})
class ReservationApiV2ControllerIntegrationTest {
    private static final String DEFAULT_CATEGORY_NAME = "default";

    @Autowired
    private ReservationApiV2Controller reservationApiV2Controller;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserManager userManager;

    @Autowired
    private ClockProvider clockProvider;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private TicketReservationRepository ticketReservationRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private TicketReservationManager ticketReservationManager;

    @Autowired
    private CustomOfflineConfigurationManager customOfflineConfigurationManager;

    private Authentication mockPrincipal;
    private Organization organization;
    private Event event;
    private String username;
    private List<UserDefinedOfflinePaymentMethod> paymentMethods;

    @BeforeEach
    void ensureConfiguration()
            throws CustomOfflinePaymentMethodAlreadyExistsException, CustomOfflinePaymentMethodDoesNotExistException {
        IntegrationTestUtil.ensureMinimalConfiguration(configurationRepository);

        List<TicketCategoryModification> categories = Arrays.asList(
                new TicketCategoryModification(
                        null,
                        DEFAULT_CATEGORY_NAME,
                        TicketCategory.TicketAccessType.INHERIT,
                        AVAILABLE_SEATS,
                        new DateTimeModification(
                                LocalDate.now(clockProvider.getClock()).minusDays(1),
                                LocalTime.now(clockProvider.getClock())),
                        new DateTimeModification(
                                LocalDate.now(clockProvider.getClock()).plusDays(1),
                                LocalTime.now(clockProvider.getClock())),
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
                        AlfioMetadata.empty()),
                new TicketCategoryModification(
                        null,
                        "hidden",
                        TicketCategory.TicketAccessType.INHERIT,
                        2,
                        new DateTimeModification(
                                LocalDate.now(clockProvider.getClock()).minusDays(1),
                                LocalTime.now(clockProvider.getClock())),
                        new DateTimeModification(
                                LocalDate.now(clockProvider.getClock()).plusDays(1),
                                LocalTime.now(clockProvider.getClock())),
                        DESCRIPTION,
                        BigDecimal.ONE,
                        true,
                        "",
                        true,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        null,
                        null,
                        AlfioMetadata.empty()));
        Pair<Event, String> eventAndUser = initEvent(
                categories,
                organizationRepository,
                userManager,
                eventManager,
                eventRepository,
                null,
                Event.EventFormat.ONLINE,
                PriceContainer.VatStatus.INCLUDED,
                List.of(PaymentProxy.CUSTOM_OFFLINE));
        event = eventAndUser.getLeft();

        organization = organizationRepository.getById(event.getOrganizationId());

        username = eventAndUser.getRight();
        mockPrincipal = Mockito.mock(Authentication.class);
        Mockito.when(mockPrincipal.getName()).thenReturn(owner(username));

        paymentMethods = List.of(
                new UserDefinedOfflinePaymentMethod(
                        "15146df3-2436-4d2e-90b9-0d6cb273e291",
                        Map.of(
                                "en",
                                new UserDefinedOfflinePaymentMethod.Localization(
                                        "Interac E-Transfer",
                                        "Instant bank transfer from any Canadian account.",
                                        "Send the payment to `payments@example.com`."))),
                new UserDefinedOfflinePaymentMethod(
                        "ec6c5268-4122-4b27-98ee-fa070df11c5b",
                        Map.of(
                                "en",
                                new UserDefinedOfflinePaymentMethod.Localization(
                                        "Venmo",
                                        "Instant money transfers via the Venmo app.",
                                        "Send the payment to user `exampleco` on Venmo."))));

        for (var pm : paymentMethods) {
            customOfflineConfigurationManager.createOrganizationCustomOfflinePaymentMethod(organization.getId(), pm);
        }

        List<String> eventSelectedMethodIds = paymentMethods.stream()
                .map(UserDefinedOfflinePaymentMethod::getPaymentMethodId)
                .toList();

        customOfflineConfigurationManager.setAllowedCustomOfflinePaymentMethodsForEvent(event, eventSelectedMethodIds);
    }

    @Test
    void canGetApplicablePaymentMethodDetails() throws PassedIdDoesNotExistException {
        var reservationId = UUID.randomUUID().toString();
        ticketReservationRepository.createNewReservation(
                reservationId,
                ZonedDateTime.now(event.getZoneId()),
                DateUtils.addMinutes(new Date(), 1),
                null,
                "en",
                event.getId(),
                null,
                null,
                null,
                event.getOrganizationId(),
                null);

        var response = reservationApiV2Controller.getApplicableCustomPaymentMethodDetails(reservationId, mockPrincipal);
        assertTrue(response.getStatusCode().is2xxSuccessful());

        var returnedPaymentMethods = response.getBody();

        assertEquals(paymentMethods.size(), returnedPaymentMethods.size());

        var returnedEqualsExpected = returnedPaymentMethods.stream().allMatch(pm1 -> paymentMethods.stream()
                .anyMatch(pm2 -> pm1.getPaymentMethodId().equals(pm2.getPaymentMethodId())));

        assertTrue(returnedEqualsExpected);
    }

    @Test
    void canGetSelectedCustomPaymentMethodDetailsForReservation()
            throws PassedIdDoesNotExistException, CustomOfflinePaymentMethodDoesNotExistException {
        var reservationId = UUID.randomUUID().toString();
        ticketReservationRepository.createNewReservation(
                reservationId,
                ZonedDateTime.now(event.getZoneId()),
                DateUtils.addMinutes(new Date(), 1),
                null,
                "en",
                event.getId(),
                null,
                null,
                null,
                event.getOrganizationId(),
                null);
        ticketReservationRepository.updateTicketReservation(
                reservationId,
                TicketReservation.TicketReservationStatus.CUSTOM_OFFLINE_PAYMENT.name(),
                "subscription+buyer@test.com",
                "full name",
                "full",
                "name",
                "en",
                "",
                ZonedDateTime.now(),
                PaymentProxy.CUSTOM_OFFLINE.name(),
                null);

        List<Integer> ids = ticketRepository.selectNotAllocatedTicketsForUpdate(
                event.getId(), 5, Collections.singletonList(TicketRepository.FREE));

        var ticketCategory =
                ticketCategoryRepository.findAllTicketCategories(event.getId()).get(0);
        ticketRepository.reserveTickets(reservationId, ids, ticketCategory, "en", event.getVatStatus(), i -> null);

        var paymentForm = new PaymentForm();
        paymentForm.setPrivacyPolicyAccepted(true);
        paymentForm.setTermAndConditionsAccepted(true);
        paymentForm.setPaymentProxy(PaymentProxy.CUSTOM_OFFLINE);
        paymentForm.setSelectedPaymentMethod(paymentMethods.get(0));

        var confirmOverviewRes = reservationApiV2Controller.confirmOverview(
                reservationId,
                "en",
                paymentForm,
                new BeanPropertyBindingResult(paymentForm, "paymentForm"),
                new MockHttpServletRequest(),
                null);
        assertTrue(confirmOverviewRes.getStatusCode().is2xxSuccessful());

        var selected = reservationApiV2Controller
                .getSelectedCustomPaymentMethodDetails(reservationId, mockPrincipal)
                .getBody();

        assertEquals(paymentMethods.get(0).getPaymentMethodId(), selected.getPaymentMethodId());
    }

    @Test
    void cannotGetSelectedCustomPaymentMethodDetailsForOrgWithNoCustomMethods()
            throws CustomOfflinePaymentMethodDoesNotExistException, CustomOfflinePaymentMethodAlreadyExistsException {
        var orgPaymentMethods =
                customOfflineConfigurationManager.getOrganizationCustomOfflinePaymentMethods(organization.getId());

        var reservationId = UUID.randomUUID().toString();
        ticketReservationRepository.createNewReservation(
                reservationId,
                ZonedDateTime.now(event.getZoneId()),
                DateUtils.addMinutes(new Date(), 1),
                null,
                "en",
                event.getId(),
                null,
                null,
                null,
                event.getOrganizationId(),
                null);

        TotalPrice totalPrice = new TotalPrice(1130, 130, 0, 0, "USD");
        PaymentSpecification specification = new PaymentSpecification(
                reservationId,
                null,
                paymentMethods.get(0),
                totalPrice.getPriceWithVAT(),
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

        var paymentResult = ticketReservationManager.performPayment(
                specification, totalPrice, PaymentProxy.CUSTOM_OFFLINE, paymentMethods.get(0), null);
        assertTrue(paymentResult.isSuccessful());

        for (var pm : orgPaymentMethods) {
            customOfflineConfigurationManager.deleteOrganizationCustomOfflinePaymentMethod(organization.getId(), pm);
        }

        var reinsertedPaymentMethod = new UserDefinedOfflinePaymentMethod(
                UUID.randomUUID().toString(), paymentMethods.get(1).getLocalizations());

        customOfflineConfigurationManager.createOrganizationCustomOfflinePaymentMethod(
                organization.getId(), reinsertedPaymentMethod);

        assertThrows(
                CustomOfflinePaymentMethodDoesNotExistException.class,
                () -> reservationApiV2Controller.getSelectedCustomPaymentMethodDetails(reservationId, mockPrincipal));
    }

    @Test
    void testActivePaymentMethodsDeniedMethodsCorrect() throws CustomOfflinePaymentMethodDoesNotExistException {
        var reservationId = UUID.randomUUID().toString();
        ticketReservationRepository.createNewReservation(
                reservationId,
                ZonedDateTime.now(event.getZoneId()),
                DateUtils.addMinutes(new Date(), 1),
                null,
                "en",
                event.getId(),
                null,
                null,
                null,
                event.getOrganizationId(),
                null);
        var firstCategory = CollectionUtils.get(ticketCategoryRepository.findByEventIdAsMap(event.getId()), 0);
        var tickets = ticketRepository.findFreeByEventId(event.getId());
        var firstTicket = tickets.get(0);
        int ticketId = firstTicket.getId();
        ticketRepository.reserveTickets(
                reservationId, List.of(ticketId), firstCategory.getValue(), "en", event.getVatStatus(), i -> null);

        customOfflineConfigurationManager.setDeniedPaymentMethodsByTicketCategory(
                event, firstCategory.getValue(), paymentMethods);

        var response = reservationApiV2Controller.getReservationInfo(reservationId, mockPrincipal);
        assertTrue(response.getStatusCode().is2xxSuccessful());

        var info = response.getBody();

        var paymentMethodIds = info.getActivePaymentMethods().keySet();

        assertEquals(0, paymentMethodIds.size());
    }

    // ========================================================================
    // R4: GET /api/v2/public/reservation/{id} — Get reservation details
    // ========================================================================

    @Test
    void getReservationDetailsReturnsReservationInfo() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createReservationForEvent(inPersonEvent);

        var response = reservationApiV2Controller.getReservationInfo(reservationId, null);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(reservationId, response.getBody().getId());
    }

    @Test
    void getReservationDetailsReturns404ForNonexistent() {
        var response = reservationApiV2Controller.getReservationInfo("nonexistent-id", null);
        assertEquals(404, response.getStatusCode().value());
    }

    // ========================================================================
    // R5: POST /api/v2/public/reservation/{id}/validate-to-overview
    // ========================================================================

    @Test
    void validateToOverviewWithMissingFieldsReturns422() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createReservationForEvent(inPersonEvent);

        var contactForm = new ContactAndTicketsForm();

        var response = reservationApiV2Controller.validateToOverview(
                reservationId,
                "en",
                false,
                contactForm,
                new BeanPropertyBindingResult(contactForm, "paymentForm"),
                null);
        assertEquals(422, response.getStatusCode().value());
    }

    // ========================================================================
    // R6: POST /api/v2/public/reservation/{id} — Confirm overview
    // ========================================================================

    @Test
    void confirmOverviewWithOfflinePayment() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createValidatedReservationForEvent(inPersonEvent);

        var paymentForm = new PaymentForm();
        paymentForm.setPrivacyPolicyAccepted(true);
        paymentForm.setTermAndConditionsAccepted(true);
        paymentForm.setPaymentProxy(PaymentProxy.OFFLINE);
        paymentForm.setSelectedPaymentMethod(alfio.model.transaction.StaticPaymentMethods.BANK_TRANSFER);

        var response = reservationApiV2Controller.confirmOverview(
                reservationId,
                "en",
                paymentForm,
                new BeanPropertyBindingResult(paymentForm, "paymentForm"),
                new MockHttpServletRequest(),
                null);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void confirmOverviewWithoutAcceptingTermsReturns422() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createValidatedReservationForEvent(inPersonEvent);

        var paymentForm = new PaymentForm();
        paymentForm.setPrivacyPolicyAccepted(false);
        paymentForm.setTermAndConditionsAccepted(false);

        var response = reservationApiV2Controller.confirmOverview(
                reservationId,
                "en",
                paymentForm,
                new BeanPropertyBindingResult(paymentForm, "paymentForm"),
                new MockHttpServletRequest(),
                null);
        assertEquals(422, response.getStatusCode().value());
    }

    // ========================================================================
    // P1: POST /api/v2/public/reservation/{id}/payment/{method}/init
    // ========================================================================

    @Test
    void initBankTransferPayment() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createValidatedReservationForEvent(inPersonEvent);

        var allParams = new LinkedMultiValueMap<String, String>();
        var response = reservationApiV2Controller.initTransaction(
                reservationId, alfio.model.transaction.StaticPaymentMethods.BANK_TRANSFER.name(), allParams);
        assertNotNull(response);
    }

    // ========================================================================
    // P2: GET /api/v2/public/reservation/{id}/payment/{method}/status
    // ========================================================================

    @Test
    void checkPaymentStatusForCompletedPayment() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createConfirmedReservationForEvent(inPersonEvent);

        var response = reservationApiV2Controller.getTransactionStatus(
                reservationId, alfio.model.transaction.StaticPaymentMethods.BANK_TRANSFER.name());
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    // ========================================================================
    // E1: GET /api/v2/public/reservation/{id}/status — Poll status
    // ========================================================================

    @Test
    void pollReservationStatusReturnsPending() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createReservationForEvent(inPersonEvent);

        var response = reservationApiV2Controller.getReservationStatus(reservationId);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
                TicketReservation.TicketReservationStatus.PENDING,
                response.getBody().getStatus());
    }

    @Test
    void pollReservationStatusReturns404ForNonexistent() {
        var response = reservationApiV2Controller.getReservationStatus("nonexistent-id");
        assertEquals(404, response.getStatusCode().value());
    }

    // ========================================================================
    // E2: POST /api/v2/public/reservation/{id}/back-to-booking
    // ========================================================================

    @Test
    void backToBookingRevertsValidatedState() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createValidatedReservationForEvent(inPersonEvent);

        var statusBefore = reservationApiV2Controller.getReservationStatus(reservationId);
        assertTrue(statusBefore.getBody().isValidatedBookingInformation());

        var response = reservationApiV2Controller.backToBooking(reservationId);
        assertEquals(200, response.getStatusCode().value());

        var statusAfter = reservationApiV2Controller.getReservationStatus(reservationId);
        assertFalse(statusAfter.getBody().isValidatedBookingInformation());
    }

    // ========================================================================
    // E4: DELETE /api/v2/public/reservation/{id} — Cancel reservation
    // ========================================================================

    @Test
    void cancelPendingReservation() {
        var inPersonEvent = createInPersonEvent();
        String reservationId = createReservationForEvent(inPersonEvent);

        var response = reservationApiV2Controller.cancelPendingReservation(reservationId);
        assertEquals(200, response.getStatusCode().value());

        var statusResponse = reservationApiV2Controller.getReservationStatus(reservationId);
        assertEquals(404, statusResponse.getStatusCode().value());
    }

    // ========================================================================
    // IN_PERSON event helpers (for tests needing validateToOverview)
    // ========================================================================

    private Pair<Event, String> createInPersonEvent() {
        List<TicketCategoryModification> categories = Arrays.asList(new TicketCategoryModification(
                null,
                DEFAULT_CATEGORY_NAME,
                TicketCategory.TicketAccessType.INHERIT,
                AVAILABLE_SEATS,
                new DateTimeModification(
                        LocalDate.now(clockProvider.getClock()).minusDays(1), LocalTime.now(clockProvider.getClock())),
                new DateTimeModification(
                        LocalDate.now(clockProvider.getClock()).plusDays(5), LocalTime.now(clockProvider.getClock())),
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
        return initEvent(
                categories,
                organizationRepository,
                userManager,
                eventManager,
                eventRepository,
                null,
                Event.EventFormat.IN_PERSON,
                PriceContainer.VatStatus.INCLUDED,
                List.of(PaymentProxy.OFFLINE));
    }

    private String createReservationForEvent(Pair<Event, String> eventAndUser) {
        var evt = eventAndUser.getLeft();
        var categories = ticketCategoryRepository.findAllTicketCategories(evt.getId());
        alfio.model.modification.TicketReservationModification tr =
                new alfio.model.modification.TicketReservationModification();
        tr.setQuantity(1);
        tr.setTicketCategoryId(categories.get(0).getId());
        var tickets = new TicketReservationWithOptionalCodeModification(tr, Optional.empty());
        return ticketReservationManager.createTicketReservation(
                evt,
                List.of(tickets),
                List.of(),
                DateUtils.addDays(new Date(), 1),
                Optional.empty(),
                Locale.ENGLISH,
                false,
                null);
    }

    private String createValidatedReservationForEvent(Pair<Event, String> eventAndUser) {
        String reservationId = createReservationForEvent(eventAndUser);
        var tickets = ticketRepository.findTicketsInReservation(reservationId);

        var contactForm = new ContactAndTicketsForm();
        contactForm.setEmail("test@test.com");
        contactForm.setFirstName("full");
        contactForm.setLastName("name");

        var ticketForm = new UpdateTicketOwnerForm();
        ticketForm.setFirstName("ticketfull");
        ticketForm.setLastName("ticketname");
        ticketForm.setEmail("tickettest@test.com");
        contactForm.setTickets(
                Collections.singletonMap(tickets.get(0).getPublicUuid().toString(), ticketForm));

        reservationApiV2Controller.validateToOverview(
                reservationId,
                "en",
                false,
                contactForm,
                new BeanPropertyBindingResult(contactForm, "paymentForm"),
                null);
        return reservationId;
    }

    private String createConfirmedReservationForEvent(Pair<Event, String> eventAndUser) {
        String reservationId = createValidatedReservationForEvent(eventAndUser);

        var paymentForm = new PaymentForm();
        paymentForm.setPrivacyPolicyAccepted(true);
        paymentForm.setTermAndConditionsAccepted(true);
        paymentForm.setPaymentProxy(PaymentProxy.OFFLINE);
        paymentForm.setSelectedPaymentMethod(alfio.model.transaction.StaticPaymentMethods.BANK_TRANSFER);

        reservationApiV2Controller.confirmOverview(
                reservationId,
                "en",
                paymentForm,
                new BeanPropertyBindingResult(paymentForm, "paymentForm"),
                new MockHttpServletRequest(),
                null);

        ticketReservationManager.confirmOfflinePayment(
                eventAndUser.getLeft(), reservationId, null, eventAndUser.getRight());
        return reservationId;
    }

    // ========================================================================
    // Existing ONLINE event helpers (for existing tests)
    // ========================================================================

    private String createReservation() {
        var reservationId = UUID.randomUUID().toString();
        ticketReservationRepository.createNewReservation(
                reservationId,
                ZonedDateTime.now(event.getZoneId()),
                DateUtils.addMinutes(new Date(), 1),
                null,
                "en",
                event.getId(),
                null,
                null,
                null,
                event.getOrganizationId(),
                null);
        var firstCategory = CollectionUtils.get(ticketCategoryRepository.findByEventIdAsMap(event.getId()), 0);
        var tickets = ticketRepository.findFreeByEventId(event.getId());
        var firstTicket = tickets.get(0);
        ticketRepository.reserveTickets(
                reservationId,
                List.of(firstTicket.getId()),
                firstCategory.getValue(),
                "en",
                event.getVatStatus(),
                i -> null);
        return reservationId;
    }

    private String createAndValidateReservation() {
        String reservationId = createReservation();
        var tickets = ticketRepository.findTicketsInReservation(reservationId);

        var contactForm = new ContactAndTicketsForm();
        contactForm.setEmail("test@test.com");
        contactForm.setFirstName("full");
        contactForm.setLastName("name");

        var ticketForm = new UpdateTicketOwnerForm();
        ticketForm.setFirstName("ticketfull");
        ticketForm.setLastName("ticketname");
        ticketForm.setEmail("tickettest@test.com");
        contactForm.setTickets(Collections.singletonMap(tickets.get(0).getUuid(), ticketForm));

        reservationApiV2Controller.validateToOverview(
                reservationId,
                "en",
                false,
                contactForm,
                new BeanPropertyBindingResult(contactForm, "paymentForm"),
                mockPrincipal);
        return reservationId;
    }

    private String createAndConfirmReservation() {
        return createAndConfirmPayment();
    }

    private String createAndConfirmPayment() {
        String reservationId = createAndValidateReservation();

        var paymentForm = new PaymentForm();
        paymentForm.setPrivacyPolicyAccepted(true);
        paymentForm.setTermAndConditionsAccepted(true);
        paymentForm.setPaymentProxy(PaymentProxy.OFFLINE);
        paymentForm.setSelectedPaymentMethod(alfio.model.transaction.StaticPaymentMethods.BANK_TRANSFER);

        reservationApiV2Controller.confirmOverview(
                reservationId,
                "en",
                paymentForm,
                new BeanPropertyBindingResult(paymentForm, "paymentForm"),
                new MockHttpServletRequest(),
                mockPrincipal);

        ticketReservationManager.confirmOfflinePayment(event, reservationId, null, username);
        return reservationId;
    }
}
