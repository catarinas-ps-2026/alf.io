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
import alfio.model.BillingDocument;
import alfio.model.Event;
import alfio.model.PurchaseContext;
import alfio.model.PurchaseContext.PurchaseContextType;
import alfio.model.Ticket;
import alfio.model.TicketReservation;
import alfio.model.metadata.SubscriptionMetadata;
import alfio.model.result.Result;
import alfio.repository.*;
import alfio.repository.user.UserRepository;
import alfio.util.ClockProvider;
import alfio.util.TemplateManager;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReservationManagerUnitTest {

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
    void testFindSubscriptionMetadata() {
        UUID subscriptionId = UUID.randomUUID();
        SubscriptionMetadata metadata = mock(SubscriptionMetadata.class);
        when(subscriptionRepository.getSubscriptionMetadata(subscriptionId)).thenReturn(metadata);

        SubscriptionMetadata result = adminReservationManager.findSubscriptionMetadata(subscriptionId);
        assertEquals(metadata, result);
        verify(subscriptionRepository).getSubscriptionMetadata(subscriptionId);
    }

    @Test
    void testGetTicketIdsWithAdditionalData() {
        String reservationId = "resId";
        String publicIdentifier = "pubId";
        List<Integer> expectedIds = List.of(1, 2, 3);
        when(ticketRepository.findTicketsWithAdditionalData(reservationId, publicIdentifier)).thenReturn(expectedIds);

        List<Integer> result = adminReservationManager.getTicketIdsWithAdditionalData(PurchaseContextType.event, publicIdentifier, reservationId);
        assertEquals(expectedIds, result);
        verify(ticketRepository).findTicketsWithAdditionalData(reservationId, publicIdentifier);

        List<Integer> resultNotEvent = adminReservationManager.getTicketIdsWithAdditionalData(PurchaseContextType.subscription, publicIdentifier, reservationId);
        assertTrue(resultNotEvent.isEmpty());
        verifyNoMoreInteractions(ticketRepository);
    }

    @Test
    void testNotifyAttendees() {
        String eventName = "event";
        String reservationId = "resId";
        List<Integer> ids = List.of(1);
        String username = "admin";

        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        Event event = mock(Event.class);
        when(purchaseContext.event()).thenReturn(Optional.of(event));
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(PurchaseContextType.event, eventName);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getUserLanguage()).thenReturn("en");
        when(ticketReservationRepository.findOptionalReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(1);
        when(ticket.getAssigned()).thenReturn(true);
        when(ticketRepository.findTicketsInReservation(reservationId)).thenReturn(List.of(ticket));

        Result<Boolean> result = adminReservationManager.notifyAttendees(eventName, reservationId, ids, username);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());

        verify(reservationEmailContentHelper).sendTicketByEmail(eq(ticket), eq(Locale.ENGLISH), eq(event), isNull());
    }

    @Test
    void testLoadReservation() {
        String reservationId = "resId";
        String publicIdentifier = "pubId";
        String username = "admin";

        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(PurchaseContextType.event, publicIdentifier);

        TicketReservation reservation = mock(TicketReservation.class);
        when(ticketReservationRepository.findOptionalReservationById(reservationId)).thenReturn(Optional.of(reservation));

        List<Ticket> tickets = List.of(mock(Ticket.class));
        when(ticketRepository.findTicketsInReservation(reservationId)).thenReturn(tickets);
        when(purchaseContextManager.findByReservationId(reservationId)).thenReturn(Optional.of(purchaseContext));

        Result<Triple<TicketReservation, List<Ticket>, PurchaseContext>> result = adminReservationManager.loadReservation(PurchaseContextType.event, publicIdentifier, reservationId, username);
        assertTrue(result.isSuccess());
        assertEquals(reservation, result.getData().getLeft());
        assertEquals(tickets, result.getData().getMiddle());
        assertEquals(purchaseContext, result.getData().getRight());
    }

    @Test
    void testRemoveTickets() {
        String publicIdentifier = "pubId";
        String reservationId = "resId";
        List<Integer> ticketIds = List.of(1);
        List<Integer> toRefund = List.of();
        String username = "admin";

        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        Event event = mock(Event.class);
        when(purchaseContext.event()).thenReturn(Optional.of(event));
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(PurchaseContextType.event, publicIdentifier);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn(reservationId);
        when(ticketReservationRepository.findOptionalReservationById(reservationId)).thenReturn(Optional.of(reservation));

        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(1);
        when(ticket.isCheckedIn()).thenReturn(false);
        List<Ticket> tickets = List.of(ticket);
        when(ticketRepository.findTicketsInReservation(reservationId)).thenReturn(tickets);
        when(purchaseContextManager.findByReservationId(reservationId)).thenReturn(Optional.of(purchaseContext));

        //for removeTicketsFromReservation
        when(ticketRepository.batchReleaseTickets(eq(reservationId), eq(ticketIds), eq(event))).thenReturn(new int[]{1});

        Result<Boolean> result = adminReservationManager.removeTickets(publicIdentifier, reservationId, ticketIds, toRefund, true, false, username);
        assertTrue(result.isSuccess(), () -> "Error: " + result.getErrors());
        assertFalse(result.getData());

        verify(ticketRepository).batchReleaseTickets(eq(reservationId), eq(ticketIds), eq(event));
        verify(ticketReservationRepository).updateReservationStatus(reservationId, "CANCELLED");
    }

    @Test
    void testRegenerateBillingDocument() {
        String publicIdentifier = "pubId";
        String reservationId = "resId";
        String username = "admin";

        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        doReturn(Optional.of(purchaseContext)).when(purchaseContextManager).findBy(PurchaseContextType.event, publicIdentifier);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getPromoCodeDiscountId()).thenReturn(null);
        when(ticketReservationRepository.findOptionalReservationById(reservationId)).thenReturn(Optional.of(reservation));
        when(ticketRepository.findTicketsInReservation(reservationId)).thenReturn(List.of());
        when(purchaseContextManager.findByReservationId(reservationId)).thenReturn(Optional.of(purchaseContext));

        BillingDocument billingDocument = mock(BillingDocument.class);
        when(billingDocument.getType()).thenReturn(BillingDocument.Type.INVOICE);
        when(billingDocument.getId()).thenReturn(1L);
        when(billingDocumentManager.createBillingDocument(eq(purchaseContext), eq(reservation), eq(username), any())).thenReturn(billingDocument);

        Result<Boolean> result = adminReservationManager.regenerateBillingDocument(PurchaseContextType.event, publicIdentifier, reservationId, username);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());

        verify(billingDocumentManager).createBillingDocument(eq(purchaseContext), eq(reservation), eq(username), any());
        verify(billingDocumentRepository).invalidateAllPreviousDocumentsOfType(BillingDocument.Type.INVOICE, 1L, reservationId);
    }

    @Test
    void testInvalidateBillingDocument() {
        String reservationId = "resId";
        long documentId = 1L;
        String username = "admin";

        PurchaseContext purchaseContext = mock(PurchaseContext.class);
        TicketReservation reservation = mock(TicketReservation.class);
        when(ticketReservationRepository.findOptionalReservationById(reservationId)).thenReturn(Optional.of(reservation));
        when(ticketRepository.findTicketsInReservation(reservationId)).thenReturn(List.of());
        when(purchaseContextManager.findByReservationId(reservationId)).thenReturn(Optional.of(purchaseContext));

        when(billingDocumentRepository.updateStatus(documentId, BillingDocument.Status.NOT_VALID, reservationId)).thenReturn(1);
        when(userRepository.findIdByUserName(username)).thenReturn(Optional.of(123));

        Result<Boolean> result = adminReservationManager.invalidateBillingDocument(reservationId, documentId, username);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());

        verify(billingDocumentRepository).updateStatus(documentId, BillingDocument.Status.NOT_VALID, reservationId);
        verify(auditingRepository).insert(eq(reservationId), eq(123), any(PurchaseContext.class), eq(alfio.model.Audit.EventType.BILLING_DOCUMENT_INVALIDATED), any(), eq(alfio.model.Audit.EntityType.RESERVATION), eq("1"));
    }
}
