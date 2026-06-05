package alfio.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventDeleterRepositoryTest {

    private final EventDeleterRepository eventDeleterRepository = mock(EventDeleterRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testDeleteAllForEvent() {
        int eventId = 123;
        eventDeleterRepository.deleteAllForEvent(eventId);
        
        verify(eventDeleterRepository).deletePolls(eventId);
        verify(eventDeleterRepository).deleteWaitingQueue(eventId);
        verify(eventDeleterRepository).deleteWhitelistedTickets(eventId);
        verify(eventDeleterRepository).deleteGroupLinks(eventId);
        verify(eventDeleterRepository).deleteConfigurationEvent(eventId);
        verify(eventDeleterRepository).deleteConfigurationPurchaseContext(eventId);
        verify(eventDeleterRepository).deleteConfigurationTicketCategory(eventId);
        verify(eventDeleterRepository).deleteEmailMessage(eventId);
        verify(eventDeleterRepository).deleteLegacyTicketFieldValue(eventId);
        verify(eventDeleterRepository).deleteLegacyTicketFieldDescription(eventId);
        verify(eventDeleterRepository).deleteLegacyTicketFieldConfiguration(eventId);
        verify(eventDeleterRepository).deleteFieldValues(eventId);
        verify(eventDeleterRepository).deleteFieldDescription(eventId);
        verify(eventDeleterRepository).deleteAdditionalServiceDescriptions(eventId);
        verify(eventDeleterRepository).deleteAdditionalServiceItems(eventId);
        verify(eventDeleterRepository).deleteFieldConfigurations(eventId);
        verify(eventDeleterRepository).deleteAdditionalServices(eventId);
        verify(eventDeleterRepository).deleteEventMigration(eventId);
        verify(eventDeleterRepository).deleteSponsorScan(eventId);
        verify(eventDeleterRepository).deleteTicket(eventId);
        verify(eventDeleterRepository).deleteTransactions(eventId);
        verify(eventDeleterRepository).deleteBillingDocuments(eventId);
        verify(eventDeleterRepository).deleteReservation(eventId);
        verify(eventDeleterRepository).deleteSpecialPrice(eventId);
        verify(eventDeleterRepository).deletePromoCode(eventId);
        verify(eventDeleterRepository).deleteTicketCategoryText(eventId);
        verify(eventDeleterRepository).deleteTicketCategory(eventId);
        verify(eventDeleterRepository).deleteEventDescription(eventId);
        verify(eventDeleterRepository).deleteResources(eventId);
        verify(eventDeleterRepository).deleteScanAudit(eventId);
        verify(eventDeleterRepository).deleteSubscriptionLinks(eventId);
        verify(eventDeleterRepository).deleteEvent(eventId);
    }
}
