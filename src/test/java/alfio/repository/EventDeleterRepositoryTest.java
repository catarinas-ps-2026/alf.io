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
package alfio.repository;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventDeleterRepositoryTest {

    private final EventDeleterRepository eventDeleterRepository =
            mock(EventDeleterRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

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
