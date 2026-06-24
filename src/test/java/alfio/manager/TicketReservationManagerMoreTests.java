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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.manager.i18n.MessageSourceManager;
import alfio.manager.payment.custom.offline.CustomOfflineConfigurationManager;
import alfio.manager.support.PaymentWebhookResult;
import alfio.manager.support.reservation.OrderSummaryGenerator;
import alfio.manager.support.reservation.ReservationCostCalculator;
import alfio.manager.support.reservation.ReservationEmailContentHelper;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.user.UserManager;
import alfio.model.*;
import alfio.model.TicketReservation.TicketReservationStatus;
import alfio.model.modification.TransactionMetadataModification;
import alfio.model.system.ConfigurationKeyValuePathLevel;
import alfio.model.system.ConfigurationKeys;
import alfio.model.system.ConfigurationPathLevel;
import alfio.model.transaction.PaymentProvider;
import alfio.model.transaction.PaymentProxy;
import alfio.model.transaction.TransactionWebhookPayload;
import alfio.model.transaction.capabilities.WebhookHandler;
import alfio.repository.*;
import alfio.repository.user.OrganizationRepository;
import alfio.repository.user.UserRepository;
import alfio.test.util.TestUtil;
import alfio.util.ClockProvider;
import alfio.util.Json;
import alfio.util.TemplateManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

class TicketReservationManagerMoreTests {

    private TicketReservationManager manager;

    private PromoCodeDiscountRepository promoCodeDiscountRepository;
    private TicketRepository ticketRepository;
    private TicketReservationRepository ticketReservationRepository;
    private EventRepository eventRepository;
    private OrganizationRepository organizationRepository;
    private TicketCategoryRepository ticketCategoryRepository;
    private TicketCategoryDescriptionRepository ticketCategoryDescriptionRepository;
    private ConfigurationManager configurationManager;
    private PaymentManager paymentManager;
    private SpecialPriceRepository specialPriceRepository;
    private TransactionRepository transactionRepository;
    private NotificationManager notificationManager;
    private MessageSourceManager messageSourceManager;
    private TemplateManager templateManager;
    private PlatformTransactionManager transactionManager;
    private WaitingQueueManager waitingQueueManager;
    private PurchaseContextFieldRepository purchaseContextFieldRepository;
    private AuditingRepository auditingRepository;
    private UserRepository userRepository;
    private ExtensionManager extensionManager;
    private GroupManager groupManager;
    private BillingDocumentRepository billingDocumentRepository;
    private BillingDocumentManager billingDocumentManager;
    private PurchaseContextManager purchaseContextManager;
    private ReservationCostCalculator reservationCostCalculator;
    private ReservationEmailContentHelper reservationHelper;
    private ReservationFinalizer reservationFinalizer;
    private OrderSummaryGenerator orderSummaryGenerator;
    private CustomOfflineConfigurationManager customOfflineConfigurationManager;
    private ApplicationEventPublisher applicationEventPublisher;
    private ClockProvider clockProvider;

    private Event event;
    private TicketReservation reservation;

    @BeforeEach
    void setUp() {
        promoCodeDiscountRepository = mock(PromoCodeDiscountRepository.class);
        ticketRepository = mock(TicketRepository.class);
        ticketReservationRepository = mock(TicketReservationRepository.class);
        eventRepository = mock(EventRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        ticketCategoryDescriptionRepository = mock(TicketCategoryDescriptionRepository.class);
        configurationManager = mock(ConfigurationManager.class);
        paymentManager = mock(PaymentManager.class);
        specialPriceRepository = mock(SpecialPriceRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        notificationManager = mock(NotificationManager.class);
        messageSourceManager = mock(MessageSourceManager.class);
        templateManager = mock(TemplateManager.class);
        transactionManager = mock(PlatformTransactionManager.class);
        waitingQueueManager = mock(WaitingQueueManager.class);
        purchaseContextFieldRepository = mock(PurchaseContextFieldRepository.class);
        auditingRepository = mock(AuditingRepository.class);
        userRepository = mock(UserRepository.class);
        extensionManager = mock(ExtensionManager.class);
        groupManager = mock(GroupManager.class);
        billingDocumentRepository = mock(BillingDocumentRepository.class);
        billingDocumentManager = mock(BillingDocumentManager.class);
        purchaseContextManager = mock(PurchaseContextManager.class);
        reservationCostCalculator = mock(ReservationCostCalculator.class);
        reservationHelper = mock(ReservationEmailContentHelper.class);
        reservationFinalizer = mock(ReservationFinalizer.class);
        orderSummaryGenerator = mock(OrderSummaryGenerator.class);
        customOfflineConfigurationManager = mock(CustomOfflineConfigurationManager.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        clockProvider = mock(ClockProvider.class);
        when(clockProvider.getClock()).thenReturn(TestUtil.clockProvider().getClock());
        Json json = mock(Json.class);

        event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getOrganizationId()).thenReturn(1);
        when(event.getCurrency()).thenReturn("CHF");
        when(event.getZoneId()).thenReturn(ZoneId.of("UTC"));
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now(ZoneId.of("UTC")));
        when(event.event()).thenReturn(Optional.of(event));

        reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("res-1");
        when(reservation.getStatus()).thenReturn(TicketReservationStatus.PENDING);
        when(reservation.getEmail()).thenReturn("test@test.com");
        when(reservation.getUserLanguage()).thenReturn("en");

        when(configurationManager.getShortReservationID(any(), any())).thenReturn("short-id");

        manager = new TicketReservationManager(
                eventRepository,
                organizationRepository,
                ticketRepository,
                ticketReservationRepository,
                ticketCategoryRepository,
                ticketCategoryDescriptionRepository,
                configurationManager,
                paymentManager,
                promoCodeDiscountRepository,
                specialPriceRepository,
                transactionRepository,
                notificationManager,
                messageSourceManager,
                templateManager,
                transactionManager,
                waitingQueueManager,
                purchaseContextFieldRepository,
                mock(AdditionalServiceManager.class),
                auditingRepository,
                userRepository,
                extensionManager,
                mock(TicketSearchRepository.class),
                groupManager,
                billingDocumentRepository,
                mock(NamedParameterJdbcTemplate.class),
                json,
                billingDocumentManager,
                clockProvider,
                purchaseContextManager,
                mock(SubscriptionRepository.class),
                mock(UserManager.class),
                applicationEventPublisher,
                reservationCostCalculator,
                reservationHelper,
                reservationFinalizer,
                orderSummaryGenerator,
                customOfflineConfigurationManager);
    }

    @Test
    void testConfirmOfflinePayment() {
        TransactionMetadataModification mod = mock(TransactionMetadataModification.class);
        manager.confirmOfflinePayment(event, "res-1", mod, "user");
        verify(reservationFinalizer).confirmOfflinePayment(event, "res-1", mod, "user");
    }

    @Test
    void testTotalReservationCostWithVAT() {
        manager.totalReservationCostWithVAT("res-1");
        verify(reservationCostCalculator).totalReservationCostWithVAT("res-1");

        manager.totalReservationCostWithVAT(reservation);
        verify(reservationCostCalculator).totalReservationCostWithVAT(reservation);
    }

    @Test
    void testOrderSummaryForReservationId() {
        manager.orderSummaryForReservationId("res-1", event);
        verify(orderSummaryGenerator).orderSummaryForReservationId("res-1", event);
    }

    @Test
    void testCancelPendingReservation() {
        when(ticketReservationRepository.findReservationById("res-1")).thenReturn(reservation);
        when(reservation.getStatus()).thenReturn(TicketReservationStatus.PENDING);
        when(purchaseContextManager.findByReservationId("res-1")).thenReturn(Optional.of(event));
        when(ticketReservationRepository.remove(anyList())).thenReturn(1);

        manager.cancelPendingReservation("res-1", false, "user");

        verify(ticketReservationRepository).remove(anyList());
        verify(auditingRepository, atLeastOnce())
                .insert(eq("res-1"), any(), anyInt(), any(), any(), any(), eq("res-1"));
    }

    @Test
    void testDeleteOfflinePayment() {
        when(ticketReservationRepository.findOptionalReservationById("res-1")).thenReturn(Optional.of(reservation));
        when(reservation.getStatus()).thenReturn(TicketReservationStatus.OFFLINE_PAYMENT);
        when(purchaseContextManager.findByReservationId("res-1")).thenReturn(Optional.of(event));
        when(ticketReservationRepository.remove(anyList())).thenReturn(1);

        when(reservationHelper.getReservationEmailSubject(any(), any(), any(), any()))
                .thenReturn("subject");

        manager.deleteOfflinePayment(event, "res-1", false, false, true, "user");

        verify(notificationManager).sendSimpleEmail(eq(event), eq("res-1"), anyString(), any(), any());
        verify(ticketReservationRepository).remove(anyList());
    }

    @Test
    void testIssueCreditNoteForReservation() {
        OrderSummary summary = mock(OrderSummary.class);
        when(orderSummaryGenerator.orderSummaryForReservation(reservation, event))
                .thenReturn(summary);
        when(summary.getOriginalTotalPrice()).thenReturn(new TotalPrice(1000, 0, 0, 0, "CHF"));
        BillingDocument billingDocument = mock(BillingDocument.class);
        when(billingDocumentManager.createBillingDocument(any(), any(), any(), any(), any()))
                .thenReturn(billingDocument);
        when(organizationRepository.getById(anyInt())).thenReturn(mock(alfio.model.user.Organization.class));

        Map<String, Object> model = new HashMap<>();
        model.put("orderSummary", summary);
        when(reservationHelper.prepareModelForReservationEmail(any(), any(), any(), any(), any(), any()))
                .thenReturn(model);

        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("subject");
        when(messageSourceManager.getMessageSourceFor(any())).thenReturn(messageSource);

        manager.issueCreditNoteForReservation(event, reservation, "user", true);

        verify(ticketReservationRepository)
                .updateReservationStatus("res-1", TicketReservationStatus.CREDIT_NOTE_ISSUED.toString());
        verify(billingDocumentManager)
                .createBillingDocument(
                        eq(event), eq(reservation), eq("user"), eq(BillingDocument.Type.CREDIT_NOTE), eq(summary));
        verify(notificationManager).sendSimpleEmail(eq(event), eq("res-1"), anyString(), any(), any(), anyList());
    }

    @Test
    void testSendReminderForOfflinePayments() {
        ConfigurationManager.MaybeConfiguration config = mock(ConfigurationManager.MaybeConfiguration.class);
        when(config.getValueAsIntOrDefault(anyInt())).thenReturn(24);
        when(configurationManager.getForSystem(ConfigurationKeys.OFFLINE_REMINDER_HOURS))
                .thenReturn(config);
        when(configurationManager.getFor(eq(ConfigurationKeys.OFFLINE_REMINDER_HOURS), any()))
                .thenReturn(config);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1); // validity was yesterday
        when(reservation.getValidity()).thenReturn(cal.getTime());
        when(ticketReservationRepository.findAllOfflinePaymentReservationForNotificationForUpdate(any()))
                .thenReturn(Collections.singletonList(reservation));

        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getUserLanguage()).thenReturn("en");
        when(ticketRepository.findFirstTicketInReservation("res-1")).thenReturn(Optional.of(ticket));
        when(eventRepository.findById(1)).thenReturn(event);

        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("subject");
        when(messageSourceManager.getMessageSourceFor(event)).thenReturn(messageSource);
        when(reservationHelper.prepareModelForReservationEmail(any(), any())).thenReturn(new HashMap<>());

        manager.sendReminderForOfflinePayments();

        verify(ticketReservationRepository).flagAsOfflinePaymentReminderSent("res-1");
        verify(notificationManager).sendSimpleEmail(eq(event), eq("res-1"), anyString(), eq("subject"), any());
    }

    @Test
    void testProcessTransactionWebhook() {
        interface WebhookProvider extends PaymentProvider, WebhookHandler {}
        WebhookProvider provider = mock(WebhookProvider.class);
        when(paymentManager.streamActiveProvidersByProxyAndCapabilities(any(), any(), any()))
                .thenReturn(Stream.of(provider));

        TransactionWebhookPayload payload = mock(TransactionWebhookPayload.class);
        when(payload.getReservationId()).thenReturn("res-1");
        when(provider.parseTransactionPayload(anyString(), any(), anyMap(), any()))
                .thenReturn(Optional.of(payload));

        when(ticketReservationRepository.findOptionalReservationById("res-1")).thenReturn(Optional.of(reservation));

        PaymentWebhookResult result =
                manager.processTransactionWebhook("body", "sig", PaymentProxy.STRIPE, Collections.emptyMap());

        assertNotNull(result);
        verify(provider).parseTransactionPayload(eq("body"), eq("sig"), anyMap(), any());
    }

    @Test
    void testSendReminderForOfflinePaymentsToEventManagers() {
        ZonedDateTime fiveAM = ZonedDateTime.now(ZoneId.of("UTC")).withHour(5).withMinute(0);
        when(clockProvider.getClock()).thenReturn(Clock.fixed(fiveAM.toInstant(), fiveAM.getZone()));
        when(event.now(any(ClockProvider.class))).thenReturn(fiveAM);
        when(eventRepository.findAllActives(any())).thenReturn(Collections.singletonList(event));

        List<TicketReservationInfo> reservations = Collections.singletonList(mock(TicketReservationInfo.class));
        when(ticketReservationRepository.findAllOfflinePaymentReservationWithExpirationBeforeForUpdate(any(), anyInt()))
                .thenReturn(reservations);
        when(organizationRepository.getById(anyInt())).thenReturn(mock(alfio.model.user.Organization.class));
        ConfigurationKeyValuePathLevel kv = new ConfigurationKeyValuePathLevel(
                ConfigurationKeys.BASE_URL.getValue(), "http://base", ConfigurationPathLevel.SYSTEM);
        when(configurationManager.getFor(eq(ConfigurationKeys.BASE_URL), any()))
                .thenReturn(new ConfigurationManager.MaybeConfiguration(ConfigurationKeys.BASE_URL, kv));

        manager.sendReminderForOfflinePaymentsToEventManagers();

        verify(notificationManager).sendSimpleEmail(eq(event), any(), any(), anyList(), anyString(), any());
    }

    @Test
    void testValidateAndConfirmOfflinePayment() {
        when(ticketReservationRepository.findByPartialID(anyString()))
                .thenReturn(Collections.singletonList(reservation));
        OrderSummary summary = mock(OrderSummary.class);
        when(orderSummaryGenerator.orderSummaryForReservationId(eq("res-1"), any()))
                .thenReturn(summary);
        when(summary.getOriginalTotalPrice()).thenReturn(new TotalPrice(1000, 0, 0, 0, "CHF"));

        manager.validateAndConfirmOfflinePayment("res-1", event, new BigDecimal("10.00"), "user");

        verify(reservationFinalizer).confirmOfflinePayment(eq(event), eq("res-1"), any(), eq("user"));
    }
}
