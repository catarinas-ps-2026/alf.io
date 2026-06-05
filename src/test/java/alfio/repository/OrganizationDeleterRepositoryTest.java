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
