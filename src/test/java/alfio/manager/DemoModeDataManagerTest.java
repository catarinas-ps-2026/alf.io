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

import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.model.user.User;
import alfio.repository.EventDeleterRepository;
import alfio.repository.EventRepository;
import alfio.repository.OrganizationDeleterRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.repository.user.UserRepository;
import alfio.repository.user.join.UserOrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class DemoModeDataManagerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserOrganizationRepository userOrganizationRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private EventDeleterRepository eventDeleterRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ConfigurationManager configurationManager;
    @Mock
    private OrganizationDeleterRepository organizationDeleterRepository;

    private DemoModeDataManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new DemoModeDataManager(
                userRepository,
                userOrganizationRepository,
                organizationRepository,
                eventDeleterRepository,
                eventRepository,
                configurationManager,
                organizationDeleterRepository
        );
    }

    @Test
    public void testFindExpiredUsers() {
        Date checkDate = new Date();
        List<Integer> expectedIds = Arrays.asList(1, 2, 3);
        when(userRepository.findUsersToDeleteOlderThan(eq(checkDate), anySet())).thenReturn(expectedIds);

        List<Integer> actualIds = manager.findExpiredUsers(checkDate);

        assertEquals(expectedIds, actualIds);
        verify(userRepository).findUsersToDeleteOlderThan(eq(checkDate), eq(Set.of(User.Type.DEMO.name(), User.Type.API_KEY.name())));
    }

    @Test
    public void testDeleteAccountsEmpty() {
        manager.deleteAccounts(Collections.emptyList());
        verifyNoInteractions(userOrganizationRepository, eventRepository, eventDeleterRepository, userRepository, organizationDeleterRepository);
    }

    @Test
    public void testDeleteAccountsWithUsers() {
        List<Integer> userIds = Arrays.asList(10, 20);
        List<Integer> organizationIds = Arrays.asList(100, 200);
        List<Integer> eventIds = Arrays.asList(1000, 2000);

        when(userOrganizationRepository.findOrganizationsForUsers(userIds)).thenReturn(organizationIds);
        when(eventRepository.disableEventsForUsers(userIds)).thenReturn(eventIds);

        manager.deleteAccounts(userIds);

        verify(eventDeleterRepository).deleteAllForEvent(1000);
        verify(eventDeleterRepository).deleteAllForEvent(2000);
        verify(userRepository).deleteUserAndReferences(10);
        verify(userRepository).deleteUserAndReferences(20);
        verify(organizationDeleterRepository).deleteEmptyOrganizations(organizationIds);
    }

    @Test
    public void testCleanupForDemoMode() {
        MaybeConfiguration mockConfig = mock(MaybeConfiguration.class);
        when(mockConfig.getValueAsIntOrDefault(20)).thenReturn(15);
        when(configurationManager.getForSystem(any())).thenReturn(mockConfig);

        List<Integer> expiredUserIds = Arrays.asList(5, 6);
        when(userRepository.findUsersToDeleteOlderThan(any(Date.class), anySet())).thenReturn(expiredUserIds);

        List<Integer> organizationIds = Arrays.asList(300);
        List<Integer> eventIds = Arrays.asList(3000);
        when(userOrganizationRepository.findOrganizationsForUsers(expiredUserIds)).thenReturn(organizationIds);
        when(eventRepository.disableEventsForUsers(expiredUserIds)).thenReturn(eventIds);

        manager.cleanupForDemoMode();

        verify(eventDeleterRepository).deleteAllForEvent(3000);
        verify(userRepository).deleteUserAndReferences(5);
        verify(userRepository).deleteUserAndReferences(6);
        verify(organizationDeleterRepository).deleteEmptyOrganizations(organizationIds);
    }
}
