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

import alfio.manager.i18n.MessageSourceManager;
import alfio.manager.support.reservation.ReservationEmailContentHelper;
import alfio.model.*;
import alfio.model.PurchaseContext.PurchaseContextType;
import alfio.model.modification.AdminReservationModification;
import alfio.model.modification.AdminReservationModification.Notification;
import alfio.model.subscription.SubscriptionDescriptor;
import alfio.repository.*;
import alfio.repository.user.UserRepository;
import alfio.util.ClockProvider;
import alfio.util.TemplateManager;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.ZonedDateTime;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReservationManagerMoreTests {

    private AdminReservationManager adminReservationManager;

    private PurchaseContextManager purchaseContextManager;
    private EventManager eventManager;
    private TicketReservationManager ticketReservationManager;
    private TicketCategoryRepository ticketCategoryRepository;
    private TicketRepository ticketRepository;
    private SpecialPriceRepository specialPriceRepository;
    private TicketReservationRepository ticketReservationRepository;
    private EventRepository eventRepository;
    private PlatformTransactionManager transactionManager;
    private SpecialPriceTokenGenerator specialPriceTokenGenerator;
    private PurchaseContextFieldRepository purchaseContextFieldRepository;
    private PaymentManager paymentManager;
    private NotificationManager notificationManager;
    private MessageSourceManager messageSourceManager;
    private TemplateManager templateManager;
    private AdditionalServiceItemRepository additionalServiceItemRepository;
    private AuditingRepository auditingRepository;
    private UserRepository userRepository;
    private ExtensionManager extensionManager;
    private BillingDocumentRepository billingDocumentRepository;
    private FileUploadManager fileUploadManager;
    private PromoCodeDiscountRepository promoCodeDiscountRepository;
    private AdditionalServiceRepository additionalServiceRepository;
    private BillingDocumentManager billingDocumentManager;
    private ClockProvider clockProvider;
    private SubscriptionRepository subscriptionRepository;
    private ReservationEmailContentHelper reservationEmailContentHelper;
    private TransactionRepository transactionRepository;
    private AccessService accessService;

    @BeforeEach
    void setUp() {
        try {
            ClockProvider.init(Clock.systemDefaultZone());
        } catch (Exception ignored) {}

        purchaseContextManager = mock(PurchaseContextManager.class);
        eventManager = mock(EventManager.class);
        ticketReservationManager = mock(TicketReservationManager.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        ticketRepository = mock(TicketRepository.class);
        specialPriceRepository = mock(SpecialPriceRepository.class);
        ticketReservationRepository = mock(TicketReservationRepository.class);
        eventRepository = mock(EventRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        specialPriceTokenGenerator = mock(SpecialPriceTokenGenerator.class);
        purchaseContextFieldRepository = mock(PurchaseContextFieldRepository.class);
        paymentManager = mock(PaymentManager.class);
        notificationManager = mock(NotificationManager.class);
        messageSourceManager = mock(MessageSourceManager.class);
        templateManager = mock(TemplateManager.class);
        additionalServiceItemRepository = mock(AdditionalServiceItemRepository.class);
        auditingRepository = mock(AuditingRepository.class);
        userRepository = mock(UserRepository.class);
        extensionManager = mock(ExtensionManager.class);
        billingDocumentRepository = mock(BillingDocumentRepository.class);
        fileUploadManager = mock(FileUploadManager.class);
        promoCodeDiscountRepository = mock(PromoCodeDiscountRepository.class);
        additionalServiceRepository = mock(AdditionalServiceRepository.class);
        billingDocumentManager = mock(BillingDocumentManager.class);
        clockProvider = mock(ClockProvider.class);
        when(clockProvider.getClock()).thenReturn(Clock.systemDefaultZone());
        subscriptionRepository = mock(SubscriptionRepository.class);
        reservationEmailContentHelper = mock(ReservationEmailContentHelper.class);
        transactionRepository = mock(TransactionRepository.class);
        accessService = mock(AccessService.class);

        adminReservationManager = new AdminReservationManager(
            purchaseContextManager, eventManager, ticketReservationManager,
            ticketCategoryRepository, ticketRepository, specialPriceRepository,
            ticketReservationRepository, eventRepository, transactionManager,
            specialPriceTokenGenerator, purchaseContextFieldRepository,
            paymentManager, notificationManager, messageSourceManager,
            templateManager, additionalServiceItemRepository, auditingRepository,
            userRepository, extensionManager, billingDocumentRepository,
            fileUploadManager, promoCodeDiscountRepository, additionalServiceRepository,
            billingDocumentManager, clockProvider, subscriptionRepository,
            reservationEmailContentHelper, transactionRepository, accessService
        );
    }

    @Test
    void testNotify() {
        String publicIdentifier = "pubId";
        String reservationId = "resId";
        String username = "admin";
        AdminReservationModification arm = mock(AdminReservationModification.class);
        Notification notification = mock(Notification.class);
        when(arm.getNotification()).thenReturn(notification);
        when(notification.isCustomer()).thenReturn(true);

        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(any(), anyString());
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getUserLanguage()).thenReturn("en");
        when(ticketReservationRepository.findOptionalReservationById(anyString())).thenReturn(Optional.of(reservation));

        var result = adminReservationManager.notify(PurchaseContextType.event, publicIdentifier, reservationId, arm, username);
        assertTrue(result.isSuccess());
        verify(ticketReservationManager).sendConfirmationEmail(eq(purchaseContext), eq(reservation), any(), eq(username));
    }

    @Test
    void testConfirmReservation() {
        String publicIdentifier = "pubId";
        String reservationId = "resId";
        String username = "admin";
        Notification notification = new Notification(false, false);

        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        when(purchaseContext.getType()).thenReturn(PurchaseContextType.event);
        when(purchaseContext.mustUseFirstAndLastName()).thenReturn(false);
        when(purchaseContext.getZoneId()).thenReturn(java.time.ZoneId.systemDefault());
        when(purchaseContext.event()).thenReturn(Optional.of(mock(Event.class)));
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(any(), anyString());
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findByReservationId(anyString());

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getStatus()).thenReturn(TicketReservation.TicketReservationStatus.PENDING);
        when(reservation.getUserLanguage()).thenReturn("en");
        when(reservation.getFinalPriceCts()).thenReturn(1000);
        when(reservation.getEmail()).thenReturn("test@test.com");
        when(reservation.getCurrencyCode()).thenReturn("CHF");
        when(reservation.getFullName()).thenReturn("Full Name");
        when(ticketReservationRepository.findOptionalReservationById(anyString())).thenReturn(Optional.of(reservation));
        
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        when(ticketRepository.findTicketsInReservation(anyString())).thenReturn(Collections.emptyList());

        var result = adminReservationManager.confirmReservation(PurchaseContextType.event, publicIdentifier, reservationId, username, notification);
        
        assertTrue(result.isSuccess(), () -> "Result failed with errors: " + result.getErrors());
        verify(ticketReservationManager).completeReservation(any(), any(), anyBoolean(), anyBoolean(), eq(username));
    }

    @Test
    void testRemoveSubscription() {
        String reservationId = "resId";
        UUID subscriptionId = UUID.randomUUID();
        UUID descriptorId = UUID.randomUUID();
        String username = "admin";
        SubscriptionDescriptor descriptor = mock(SubscriptionDescriptor.class);
        when(descriptor.getId()).thenReturn(descriptorId);
        
        when(subscriptionRepository.cancelSubscription(anyString(), any(), any())).thenReturn(1);
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn(reservationId);
        when(ticketReservationManager.findById(anyString())).thenReturn(Optional.of(reservation));
        when(userRepository.nullSafeFindIdByUserName(anyString())).thenReturn(Optional.of(1));
        
        var result = adminReservationManager.removeSubscription(descriptor, reservationId, subscriptionId, username);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
    }

    @Test
    void testRefund() {
        String publicIdentifier = "pubId";
        String reservationId = "resId";
        BigDecimal refundAmount = BigDecimal.TEN;
        String username = "admin";

        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getCurrencyCode()).thenReturn("CHF");
        when(reservation.getPaymentMethod()).thenReturn(alfio.model.transaction.PaymentProxy.STRIPE);
        
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(any(), anyString());
        when(ticketReservationRepository.findOptionalReservationById(anyString())).thenReturn(Optional.of(reservation));
        when(ticketRepository.findTicketsInReservation(anyString())).thenReturn(Collections.emptyList());
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findByReservationId(anyString());

        when(paymentManager.refund(eq(reservation), eq(purchaseContext), anyInt(), eq(username))).thenReturn(true);

        var result = adminReservationManager.refund(PurchaseContextType.event, publicIdentifier, reservationId, refundAmount, username);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
    }

    @Test
    void testUpdateReservation() {
        String publicIdentifier = "pubId";
        String reservationId = "resId";
        String username = "admin";
        AdminReservationModification arm = mock(AdminReservationModification.class);
        when(arm.getExpiration()).thenReturn(alfio.model.modification.DateTimeModification.fromZonedDateTime(ZonedDateTime.now()));
        
        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        when(purchaseContext.getZoneId()).thenReturn(java.time.ZoneId.systemDefault());
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(any(), anyString());
        
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getStatus()).thenReturn(TicketReservation.TicketReservationStatus.PENDING);
        when(ticketReservationRepository.findOptionalReservationById(anyString())).thenReturn(Optional.of(reservation));
        
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));

        var result = adminReservationManager.updateReservation(PurchaseContextType.event, publicIdentifier, reservationId, arm, username);
        assertTrue(result.isSuccess());
    }

    @Test
    void testRemoveReservation() {
        String publicIdentifier = "pubId";
        String reservationId = "resId";
        String username = "admin";
        
        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        when(purchaseContext.getType()).thenReturn(PurchaseContextType.event);
        Event event = mock(Event.class);
        when(purchaseContext.event()).thenReturn(Optional.of(event));
        
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn(reservationId);
        
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(any(), anyString());
        when(ticketReservationRepository.findOptionalReservationById(anyString())).thenReturn(Optional.of(reservation));
        when(ticketRepository.findTicketsInReservation(anyString())).thenReturn(Collections.emptyList());
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findByReservationId(anyString());
        when(userRepository.nullSafeFindIdByUserName(anyString())).thenReturn(Optional.of(1));

        var result = adminReservationManager.removeReservation(PurchaseContextType.event, publicIdentifier, reservationId, false, false, false, username);
        assertTrue(result.isSuccess());
    }

    @Test
    void testCreditReservation() {
        String publicIdentifier = "pubId";
        String reservationId = "resId";
        String username = "admin";
        
        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        when(purchaseContext.getType()).thenReturn(PurchaseContextType.event);
        Event event = mock(Event.class);
        when(purchaseContext.event()).thenReturn(Optional.of(event));
        
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getStatus()).thenReturn(TicketReservation.TicketReservationStatus.OFFLINE_PAYMENT);
        
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(any(), anyString());
        when(ticketReservationRepository.findOptionalReservationById(anyString())).thenReturn(Optional.of(reservation));
        when(ticketRepository.findTicketsInReservation(anyString())).thenReturn(Collections.emptyList());
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findByReservationId(anyString());

        adminReservationManager.creditReservation(PurchaseContextType.event, publicIdentifier, reservationId, false, false, username);
        verify(ticketReservationManager).deleteOfflinePayment(any(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyString());
    }
}
