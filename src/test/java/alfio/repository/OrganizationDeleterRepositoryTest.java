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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationDeleterRepositoryTest {

    private final OrganizationDeleterRepository organizationDeleterRepository = mock(OrganizationDeleterRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testDeleteEmptyOrganizations() {
        List<Integer> orgIds = List.of(1, 2);
        organizationDeleterRepository.deleteEmptyOrganizations(orgIds);
        
        verify(organizationDeleterRepository).deleteInvoiceSequencesForEmptyOrganizations(orgIds);
        verify(organizationDeleterRepository).deleteAuditingForEmptyOrganizations(orgIds);
        verify(organizationDeleterRepository).deleteGroupMembersForEmptyOrganizations(orgIds);
        verify(organizationDeleterRepository).deleteGroupsForEmptyOrganizations(orgIds);
        verify(organizationDeleterRepository).deleteConfigurationForEmptyOrganizations(orgIds);
        verify(organizationDeleterRepository).deleteConfigurationForPurchaseContexts(orgIds);
        verify(organizationDeleterRepository).deleteResourcesForEmptyOrganizations(orgIds);
        verify(organizationDeleterRepository).deleteEmailMessages(orgIds);
        verify(organizationDeleterRepository).deleteSubscriptions(orgIds);
        verify(organizationDeleterRepository).deleteSubscriptionDescriptors(orgIds);
        verify(organizationDeleterRepository).deleteAllTransactions(orgIds);
        verify(organizationDeleterRepository).deleteBillingDocuments(orgIds);
        verify(organizationDeleterRepository).deleteReservations(orgIds);
        verify(organizationDeleterRepository).deleteAdminReservationRequests(orgIds);
        verify(organizationDeleterRepository).deletePromoCodes(orgIds);
        verify(organizationDeleterRepository).deleteOrganizationsIfEmpty(orgIds);
    }
}
