package alfio.repository;

import alfio.model.EmailMessage;
import alfio.model.Event;
import alfio.model.LightweightMailMessage;
import alfio.model.PurchaseContext;
import alfio.model.subscription.SubscriptionDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailMessageRepositoryTest {

    private final EmailMessageRepository emailMessageRepository = mock(EmailMessageRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testFindIdByPurchaseContextAndChecksum_Event() {
        Event event = mock(Event.class);
        when(event.ofType(PurchaseContext.PurchaseContextType.event)).thenReturn(true);
        when(event.getId()).thenReturn(123);
        String checksum = "abc";
        when(emailMessageRepository.findIdByEventIdAndChecksum(123, checksum)).thenReturn(Optional.of(456));

        Optional<Integer> result = emailMessageRepository.findIdByPurchaseContextAndChecksum(event, checksum);
        assertEquals(Optional.of(456), result);
        verify(emailMessageRepository).findIdByEventIdAndChecksum(123, checksum);
    }

    @Test
    void testFindIdByPurchaseContextAndChecksum_Subscription() {
        SubscriptionDescriptor sd = mock(SubscriptionDescriptor.class);
        when(sd.ofType(PurchaseContext.PurchaseContextType.event)).thenReturn(false);
        UUID sdId = UUID.randomUUID();
        when(sd.getId()).thenReturn(sdId);
        String checksum = "abc";
        when(emailMessageRepository.findIdBySubscriptionDescriptorAndChecksum(sdId, checksum)).thenReturn(Optional.of(789));

        Optional<Integer> result = emailMessageRepository.findIdByPurchaseContextAndChecksum(sd, checksum);
        assertEquals(Optional.of(789), result);
        verify(emailMessageRepository).findIdBySubscriptionDescriptorAndChecksum(sdId, checksum);
    }

    @Test
    void testFindByPurchaseContextAndReservationId_Event() {
        Event event = mock(Event.class);
        when(event.ofType(PurchaseContext.PurchaseContextType.event)).thenReturn(true);
        when(event.getId()).thenReturn(123);
        String reservationId = "res1";
        List<LightweightMailMessage> messages = List.of(mock(LightweightMailMessage.class));
        when(emailMessageRepository.findByEventIdAndReservationId(123, reservationId)).thenReturn(messages);

        List<LightweightMailMessage> result = emailMessageRepository.findByPurchaseContextAndReservationId(event, reservationId);
        assertEquals(messages, result);
        verify(emailMessageRepository).findByEventIdAndReservationId(123, reservationId);
    }

    @Test
    void testFindByPurchaseContextAndReservationId_Subscription() {
        SubscriptionDescriptor sd = mock(SubscriptionDescriptor.class);
        when(sd.ofType(PurchaseContext.PurchaseContextType.event)).thenReturn(false);
        UUID sdId = UUID.randomUUID();
        when(sd.getId()).thenReturn(sdId);
        String reservationId = "res2";
        List<LightweightMailMessage> messages = List.of(mock(LightweightMailMessage.class));
        when(emailMessageRepository.findBySubscriptionDescriptorAndReservationId(sdId, reservationId)).thenReturn(messages);

        List<LightweightMailMessage> result = emailMessageRepository.findByPurchaseContextAndReservationId(sd, reservationId);
        assertEquals(messages, result);
        verify(emailMessageRepository).findBySubscriptionDescriptorAndReservationId(sdId, reservationId);
    }
}
