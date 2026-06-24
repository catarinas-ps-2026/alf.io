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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import alfio.model.user.Organization;
import alfio.repository.EventDeleterRepository;
import alfio.repository.EventRepository;
import alfio.repository.OrganizationDeleterRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.repository.user.join.UserOrganizationRepository;
import alfio.util.RequestUtils;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

public class OrganizationDeleterTest {

    @Mock
    private UserOrganizationRepository userOrganizationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventDeleterRepository eventDeleterRepository;

    @Mock
    private OrganizationDeleterRepository organizationDeleterRepository;

    @Mock
    private Principal principal;

    private OrganizationDeleter deleter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        deleter = new OrganizationDeleter(
                userOrganizationRepository,
                organizationRepository,
                eventRepository,
                eventDeleterRepository,
                organizationDeleterRepository);
    }

    @Test
    public void testDeleteOrganizationNotAdmin() {
        try (MockedStatic<RequestUtils> requestUtilsMockedStatic = mockStatic(RequestUtils.class)) {
            requestUtilsMockedStatic.when(() -> RequestUtils.isAdmin(principal)).thenReturn(false);
            requestUtilsMockedStatic
                    .when(() -> RequestUtils.isSystemApiKey(principal))
                    .thenReturn(false);

            boolean result = deleter.deleteOrganization(100, principal);

            assertFalse(result);
            verifyNoInteractions(
                    organizationRepository,
                    eventRepository,
                    eventDeleterRepository,
                    organizationDeleterRepository,
                    userOrganizationRepository);
        }
    }

    @Test
    public void testDeleteOrganizationSuccessAdmin() {
        try (MockedStatic<RequestUtils> requestUtilsMockedStatic = mockStatic(RequestUtils.class)) {
            requestUtilsMockedStatic.when(() -> RequestUtils.isAdmin(principal)).thenReturn(true);
            requestUtilsMockedStatic
                    .when(() -> RequestUtils.isSystemApiKey(principal))
                    .thenReturn(false);
            when(principal.getName()).thenReturn("admin-user");

            Organization org = mock(Organization.class);
            when(org.getName()).thenReturn("Test Org");
            when(organizationRepository.getById(100)).thenReturn(org);

            List<Integer> eventIds = Arrays.asList(1000, 2000);
            when(eventRepository.disableEventsForOrganization(100)).thenReturn(eventIds);
            when(userOrganizationRepository.cleanupOrganization(100)).thenReturn(5);

            boolean result = deleter.deleteOrganization(100, principal);

            assertTrue(result);
            verify(eventDeleterRepository).deleteAllForEvent(1000);
            verify(eventDeleterRepository).deleteAllForEvent(2000);
            verify(organizationDeleterRepository).deleteFieldValues(List.of(100));
            verify(organizationDeleterRepository).deleteFieldDescription(List.of(100));
            verify(organizationDeleterRepository).deleteFieldConfiguration(List.of(100));
            verify(userOrganizationRepository).cleanupOrganization(100);
            verify(organizationDeleterRepository).deleteEmptyOrganizations(List.of(100));
        }
    }

    @Test
    public void testDeleteOrganizationSuccessSystemApiKey() {
        try (MockedStatic<RequestUtils> requestUtilsMockedStatic = mockStatic(RequestUtils.class)) {
            requestUtilsMockedStatic.when(() -> RequestUtils.isAdmin(principal)).thenReturn(false);
            requestUtilsMockedStatic
                    .when(() -> RequestUtils.isSystemApiKey(principal))
                    .thenReturn(true);
            when(principal.getName()).thenReturn("system-api-key");

            Organization org = mock(Organization.class);
            when(org.getName()).thenReturn("Test Org 2");
            when(organizationRepository.getById(200)).thenReturn(org);

            when(eventRepository.disableEventsForOrganization(200)).thenReturn(Collections.emptyList());
            when(userOrganizationRepository.cleanupOrganization(200)).thenReturn(0);

            boolean result = deleter.deleteOrganization(200, principal);

            assertTrue(result);
            verify(eventDeleterRepository, never()).deleteAllForEvent(anyInt());
            verify(organizationDeleterRepository).deleteFieldValues(List.of(200));
            verify(organizationDeleterRepository).deleteFieldDescription(List.of(200));
            verify(organizationDeleterRepository).deleteFieldConfiguration(List.of(200));
            verify(userOrganizationRepository).cleanupOrganization(200);
            verify(organizationDeleterRepository).deleteEmptyOrganizations(List.of(200));
        }
    }
}
