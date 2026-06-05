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
import alfio.manager.support.DuplicateReferenceException;
import alfio.manager.support.IncompatibleStateException;
import alfio.manager.support.reservation.ReservationEmailContentHelper;
import alfio.model.*;
import alfio.model.PurchaseContext.PurchaseContextType;
import alfio.model.TicketReservation.TicketReservationStatus;
import alfio.model.modification.AdminReservationModification;
import alfio.model.modification.AdminReservationModification.*;
import alfio.model.modification.DateTimeModification;
import alfio.model.result.ErrorCode;
import alfio.model.result.Result;
import alfio.model.subscription.Subscription;
import alfio.model.subscription.SubscriptionDescriptor;
import alfio.model.transaction.PaymentProxy;
import alfio.model.user.User;
import alfio.repository.*;
import alfio.repository.user.UserRepository;
import alfio.test.util.TestUtil;
import alfio.util.ClockProvider;
import alfio.util.TemplateManager;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

class AdminReservationManagerTest {

    private AdminReservationManager adminReservationManager;

    @Mock
    private PurchaseContextManager purchaseContextManager;

    @Mock
    private EventManager eventManager;

    @Mock
    private TicketReservationManager ticketReservationManager;

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SpecialPriceRepository specialPriceRepository;

    @Mock
    private TicketReservationRepository ticketReservationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private SpecialPriceTokenGenerator specialPriceTokenGenerator;

    @Mock
    private PurchaseContextFieldRepository purchaseContextFieldRepository;

    @Mock
    private PaymentManager paymentManager;

    @Mock
    private NotificationManager notificationManager;

    @Mock
    private MessageSourceManager messageSourceManager;

    @Mock
    private TemplateManager templateManager;

    @Mock
    private AdditionalServiceItemRepository additionalServiceItemRepository;

    @Mock
    private AuditingRepository auditingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExtensionManager extensionManager;

    @Mock
    private BillingDocumentRepository billingDocumentRepository;

    @Mock
    private FileUploadManager fileUploadManager;

    @Mock
    private PromoCodeDiscountRepository promoCodeDiscountRepository;

    @Mock
    private AdditionalServiceRepository additionalServiceRepository;

    @Mock
    private BillingDocumentManager billingDocumentManager;

    @Mock
    private ClockProvider clockProvider;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ReservationEmailContentHelper reservationEmailContentHelper;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccessService accessService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminReservationManager = new AdminReservationManager(
            purchaseContextManager,
            eventManager,
            ticketReservationManager,
            ticketCategoryRepository,
            ticketRepository,
            specialPriceRepository,
            ticketReservationRepository,
            eventRepository,
            transactionManager,
            specialPriceTokenGenerator,
            purchaseContextFieldRepository,
            paymentManager,
            notificationManager,
            messageSourceManager,
            templateManager,
            additionalServiceItemRepository,
            auditingRepository,
            userRepository,
            extensionManager,
            billingDocumentRepository,
            fileUploadManager,
            promoCodeDiscountRepository,
            additionalServiceRepository,
            billingDocumentManager,
            clockProvider,
            subscriptionRepository,
            reservationEmailContentHelper,
            transactionRepository,
            accessService
        );

        when(transactionManager.getTransaction(any())).thenReturn(
            mock(TransactionStatus.class)
        );
        when(clockProvider.getClock()).thenReturn(
            TestUtil.clockProvider().getClock()
        );
    }

    @Test
    void testConfirmReservation_EventNotFound() {
        doReturn(Optional.empty())
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        Result<?> result = adminReservationManager.confirmReservation(
            PurchaseContextType.event,
            "event",
            "resId",
            "user",
            new Notification(true, true)
        );
        assertFalse(result.isSuccess());
        assertEquals(
            ErrorCode.ReservationError.NOT_FOUND,
            result.getErrors().get(0)
        );
    }

    @Test
    void testConfirmReservation_ReservationNotFound() {
        Event event = mock(Event.class);
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.mustUseFirstAndLastName()).thenReturn(false);
        doReturn(Optional.of(event))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        when(
            ticketReservationRepository.findOptionalReservationById(anyString())
        ).thenReturn(Optional.empty());

        Result<?> result = adminReservationManager.confirmReservation(
            PurchaseContextType.event,
            "event",
            "resId",
            "user",
            new Notification(true, true)
        );
        assertFalse(result.isSuccess());
        assertEquals(
            ErrorCode.ReservationError.UPDATE_FAILED,
            result.getErrors().get(0)
        );
    }

    @Test
    void testConfirmReservation_InvalidStatus() {
        Event event = mock(Event.class);
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.mustUseFirstAndLastName()).thenReturn(false);
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getStatus()).thenReturn(
            TicketReservationStatus.COMPLETE
        );
        when(reservation.getFullName()).thenReturn("Full Name");
        doReturn(Optional.of(event))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        when(
            ticketReservationRepository.findOptionalReservationById(anyString())
        ).thenReturn(Optional.of(reservation));

        Result<?> result = adminReservationManager.confirmReservation(
            PurchaseContextType.event,
            "event",
            "resId",
            "user",
            new Notification(true, true)
        );
        assertFalse(result.isSuccess());
        assertEquals(
            ErrorCode.ReservationError.UPDATE_FAILED,
            result.getErrors().get(0)
        );
    }

    @Test
    void testUpdateReservation_ReservationNotFound() {
        doReturn(Optional.of(mock(Event.class)))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        when(
            ticketReservationRepository.findOptionalReservationById(anyString())
        ).thenReturn(Optional.empty());

        AdminReservationModification arm = new AdminReservationModification(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        Result<Boolean> result = adminReservationManager.updateReservation(
            PurchaseContextType.event,
            "event",
            "resId",
            arm,
            "user"
        );
        assertFalse(result.isSuccess());
        assertEquals(
            ErrorCode.ReservationError.UPDATE_FAILED,
            result.getErrors().get(0)
        );
    }

    @Test
    void testCreateReservation_EventNotFound() {
        when(
            eventRepository.findOptionalByShortNameForUpdate(anyString())
        ).thenReturn(Optional.empty());
        AdminReservationModification arm = new AdminReservationModification(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        Result<?> result = adminReservationManager.createReservation(
            arm,
            "event",
            "user"
        );
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.EventError.NOT_FOUND, result.getErrors().get(0));
    }

    @Test
    void testValidateTickets_MissingFields() {
        Attendee attendee = new Attendee(
            null,
            "First",
            "Last",
            "test@test.com",
            "en",
            false,
            null,
            null,
            Map.of("field1", List.of("value1")),
            Map.of()
        );
        TicketsInfo ti = new TicketsInfo(null, List.of(attendee), false, false);
        AdminReservationModification input = new AdminReservationModification(
            null,
            null,
            List.of(ti),
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(
            purchaseContextFieldRepository.getExistingFields(anyInt(), anySet())
        ).thenReturn(Collections.emptyList());

        Result<?> result = adminReservationManager.validateTickets(
            input,
            event
        );
        assertFalse(result.isSuccess());
        assertTrue(
            result
                .getErrors()
                .get(0)
                .getCode()
                .contains("error.notfound.field1")
        );
    }

    @Test
    void testRemoveTickets_CheckedInTickets() {
        String resId = "resId";
        TicketReservation reservation = mock(TicketReservation.class);
        Ticket ticket = mock(Ticket.class);
        Event event = mock(Event.class);

        when(reservation.getId()).thenReturn(resId);
        when(event.event()).thenReturn(Optional.of(event));

        when(ticket.getId()).thenReturn(1);
        when(ticket.getStatus()).thenReturn(Ticket.TicketStatus.CHECKED_IN);

        doReturn(Optional.of(event))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        when(
            ticketReservationRepository.findOptionalReservationById(resId)
        ).thenReturn(Optional.of(reservation));
        when(ticketRepository.findTicketsInReservation(resId)).thenReturn(
            List.of(ticket)
        );
        when(purchaseContextManager.findByReservationId(resId)).thenReturn(
            Optional.of(event)
        );

        assertThrows(IncompatibleStateException.class, () ->
            adminReservationManager.removeTickets(
                "event",
                resId,
                List.of(1),
                Collections.emptyList(),
                false,
                false,
                "user"
            )
        );
    }

    @Test
    void testFindReservationIdForSubscription_Success() {
        String descId = "descId";
        UUID subId = UUID.randomUUID();
        UUID descriptorUuid = UUID.randomUUID();
        Principal principal = mock(Principal.class);
        Subscription subscription = mock(Subscription.class);
        SubscriptionDescriptor descriptor = mock(SubscriptionDescriptor.class);

        doReturn(Optional.of(descriptor))
            .when(purchaseContextManager)
            .findBy(eq(PurchaseContextType.subscription), eq(descId));
        when(descriptor.getOrganizationId()).thenReturn(1);
        when(subscriptionRepository.findSubscriptionById(subId)).thenReturn(
            subscription
        );
        when(subscription.getSubscriptionDescriptorId()).thenReturn(
            descriptorUuid
        );
        when(descriptor.getId()).thenReturn(descriptorUuid);
        when(subscription.getReservationId()).thenReturn("resId");

        var result = adminReservationManager.findReservationIdForSubscription(
            descId,
            subId,
            principal
        );
        assertTrue(result.isPresent());
        assertEquals("resId", result.get().getRight());
        verify(accessService).checkOrganizationOwnership(principal, 1);
    }

    @Test
    void testGetSingleBillingDocumentAsPdf_DocumentNotFound() {
        long docId = 1L;
        String resId = "resId";
        Event event = mock(Event.class);
        doReturn(Optional.of(event))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        when(
            ticketReservationRepository.findOptionalReservationById(resId)
        ).thenReturn(Optional.of(mock(TicketReservation.class)));
        when(
            billingDocumentRepository.findByIdAndReservationId(docId, resId)
        ).thenReturn(Optional.empty());
        when(purchaseContextManager.findByReservationId(resId)).thenReturn(
            Optional.of(event)
        );

        assertThrows(IllegalArgumentException.class, () ->
            adminReservationManager.getSingleBillingDocumentAsPdf(
                PurchaseContextType.event,
                "event",
                resId,
                docId,
                "user"
            )
        );
    }

    @Test
    void testConfirmReservation_WithSubscription_Success() {
        String resId = "resId";
        String eventName = "event";
        String username = "user";
        UUID subId = UUID.randomUUID();

        Event event = mock(Event.class);
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.getType()).thenReturn(PurchaseContextType.event);
        when(event.ofType(PurchaseContextType.event)).thenReturn(true);
        when(event.mustUseFirstAndLastName()).thenReturn(false);

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getStatus()).thenReturn(
            TicketReservationStatus.PENDING
        );
        when(reservation.getFinalPriceCts()).thenReturn(1000);
        when(reservation.getEmail()).thenReturn("test@test.com");
        when(reservation.getUserLanguage()).thenReturn("en");
        when(reservation.getFullName()).thenReturn("Test User");

        doReturn(Optional.of(event))
            .when(purchaseContextManager)
            .findBy(PurchaseContextType.event, eventName);
        when(
            ticketReservationRepository.findOptionalReservationById(resId)
        ).thenReturn(Optional.of(reservation));
        when(ticketReservationManager.findById(resId)).thenReturn(
            Optional.of(reservation)
        );

        Subscription sub = mock(Subscription.class);
        when(sub.getEmail()).thenReturn("test@test.com");
        when(subscriptionRepository.findSubscriptionById(subId)).thenReturn(
            sub
        );
        when(
            ticketReservationManager.validateAndApplySubscriptionCode(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(true);

        when(ticketRepository.findTicketsInReservation(resId)).thenReturn(
            Collections.emptyList()
        );
        when(purchaseContextManager.findByReservationId(resId)).thenReturn(
            Optional.of(event)
        );

        Result<
            Triple<TicketReservation, List<Ticket>, PurchaseContext>
        > result = adminReservationManager.confirmReservation(
            PurchaseContextType.event,
            eventName,
            resId,
            username,
            new Notification(true, true),
            TransactionDetails.admin(),
            subId
        );

        assertTrue(result.isSuccess());
        verify(ticketReservationManager).completeReservation(
            any(),
            eq(PaymentProxy.ADMIN),
            anyBoolean(),
            anyBoolean(),
            eq(username)
        );
    }

    @Test
    void testCreateReservation_NotEnoughSeats() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());

        Category cat = new Category(
            10,
            "Cat",
            BigDecimal.TEN,
            TicketCategory.TicketAccessType.IN_PERSON
        );
        Attendee attendee = new Attendee(
            null,
            "First",
            "Last",
            "test@test.com",
            "en",
            false,
            null,
            null,
            Map.of(),
            Map.of()
        );
        TicketsInfo ti = new TicketsInfo(cat, List.of(attendee), false, false);
        AdminReservationModification arm = new AdminReservationModification(
            DateTimeModification.fromZonedDateTime(
                ZonedDateTime.now().plusDays(1)
            ),
            new AdminReservationModification.CustomerData(
                "F",
                "L",
                "test@test.com",
                null,
                "en",
                null,
                null,
                null,
                null
            ),
            List.of(ti),
            "en",
            true,
            false,
            null,
            Notification.EMPTY,
            null,
            null
        );

        when(
            eventRepository.findOptionalByShortNameForUpdate(anyString())
        ).thenReturn(Optional.of(event));
        TicketCategory ticketCategory = mock(TicketCategory.class);
        when(ticketCategory.getId()).thenReturn(10);
        when(
            ticketCategoryRepository.getByIdAndActive(eq(10), eq(1))
        ).thenReturn(ticketCategory);
        when(ticketRepository.countFreeTickets(anyInt(), anyInt())).thenReturn(
            0
        );
        when(
            ticketRepository.countFreeTicketsForUnbounded(anyInt())
        ).thenReturn(0);

        // Mock reserveTickets to return empty list
        when(
            ticketReservationManager.reserveTickets(
                anyInt(),
                anyInt(),
                anyInt(),
                anyList()
            )
        ).thenReturn(Collections.emptyList());

        Result<?> result = adminReservationManager.createReservation(
            arm,
            "event",
            "user"
        );
        assertFalse(result.isSuccess());
        assertEquals(
            ErrorCode.CategoryError.NOT_ENOUGH_SEATS,
            result.getErrors().get(0)
        );
    }

    @Test
    void testCreateReservation_DuplicateReference() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());

        Category cat = new Category(
            10,
            "Cat",
            BigDecimal.TEN,
            TicketCategory.TicketAccessType.IN_PERSON
        );
        Attendee attendee = new Attendee(
            null,
            "First",
            "Last",
            "test@test.com",
            "en",
            false,
            "DUPE",
            null,
            Map.of(),
            Map.of()
        );
        TicketsInfo ti = new TicketsInfo(cat, List.of(attendee), false, false);
        AdminReservationModification arm = new AdminReservationModification(
            DateTimeModification.fromZonedDateTime(
                ZonedDateTime.now().plusDays(1)
            ),
            new AdminReservationModification.CustomerData(
                "F",
                "L",
                "test@test.com",
                null,
                "en",
                null,
                null,
                null,
                null
            ),
            List.of(ti),
            "en",
            true,
            false,
            null,
            Notification.EMPTY,
            null,
            null
        );

        when(
            eventRepository.findOptionalByShortNameForUpdate(anyString())
        ).thenReturn(Optional.of(event));
        TicketCategory ticketCategory = mock(TicketCategory.class);
        when(ticketCategory.getId()).thenReturn(10);
        when(
            ticketCategoryRepository.getByIdAndActive(eq(10), eq(1))
        ).thenReturn(ticketCategory);
        when(ticketRepository.countFreeTickets(anyInt(), anyInt())).thenReturn(
            1
        );

        when(
            ticketReservationManager.reserveTickets(
                anyInt(),
                anyInt(),
                anyInt(),
                anyList()
            )
        ).thenReturn(List.of(100));
        doThrow(
            new org.springframework.dao.DataIntegrityViolationException("dupe")
        )
            .when(ticketRepository)
            .updateExternalReferenceAndLocking(
                anyInt(),
                anyInt(),
                anyString(),
                anyBoolean()
            );

        Result<?> result = adminReservationManager.createReservation(
            arm,
            "event",
            "user"
        );
        assertFalse(result.isSuccess());
        assertEquals("", result.getErrors().get(0).getCode());
    }

    @Test
    void testUpdateReservation_WithVatChange_Success() {
        String resId = "resId";
        Event event = mock(Event.class);
        when(event.getZoneId()).thenReturn(ZoneId.systemDefault());
        when(event.getVatStatus()).thenReturn(
            PriceContainer.VatStatus.INCLUDED
        );

        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getVatStatus()).thenReturn(
            PriceContainer.VatStatus.INCLUDED
        );
        when(reservation.getStatus()).thenReturn(
            TicketReservationStatus.PENDING
        );
        when(reservation.getSrcPriceCts()).thenReturn(1000);
        when(reservation.getCurrencyCode()).thenReturn("CHF");
        when(reservation.withVatStatus(any())).thenReturn(reservation);

        AdminReservationModification arm = new AdminReservationModification(
            DateTimeModification.fromZonedDateTime(
                ZonedDateTime.now().plusDays(1)
            ),
            null,
            Collections.emptyList(),
            "en",
            false,
            true,
            new AdvancedBillingOptions("N"),
            Notification.EMPTY,
            null,
            null
        );

        doReturn(Optional.of(event))
            .when(purchaseContextManager)
            .findBy(any(), anyString());
        when(
            ticketReservationRepository.findOptionalReservationById(resId)
        ).thenReturn(Optional.of(reservation));

        User user = mock(User.class);
        when(user.getId()).thenReturn(1);
        when(userRepository.getByUsername(anyString())).thenReturn(user);

        TotalPrice tp = mock(TotalPrice.class);
        when(tp.getPriceWithVAT()).thenReturn(1000);
        when(tp.getVAT()).thenReturn(0);
        when(tp.getDiscount()).thenReturn(0);
        when(
            ticketReservationManager.totalReservationCostWithVAT(
                any(TicketReservation.class)
            )
        ).thenReturn(Pair.of(tp, Optional.empty()));

        Result<Boolean> result = adminReservationManager.updateReservation(
            PurchaseContextType.event,
            "event",
            resId,
            arm,
            "user"
        );

        assertTrue(result.isSuccess());
        verify(ticketReservationRepository).resetVat(
            eq(resId),
            anyBoolean(),
            eq(PriceContainer.VatStatus.INCLUDED_EXEMPT),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            any()
        );
    }
}
