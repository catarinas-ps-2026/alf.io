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
package alfio.manager.user;

import alfio.manager.AccessService;
import alfio.model.user.Organization;
import alfio.model.user.User;
import alfio.repository.InvoiceSequencesRepository;
import alfio.repository.user.AuthorityRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.repository.user.UserRepository;
import alfio.repository.user.join.UserOrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserManagerUnitTest {

    private UserManager userManager;
    private AuthorityRepository authorityRepository;
    private OrganizationRepository organizationRepository;
    private UserOrganizationRepository userOrganizationRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private InvoiceSequencesRepository invoiceSequencesRepository;
    private FindByIndexNameSessionRepository<?> sessionsByPrincipalFinder;
    private AccessService accessService;

    @BeforeEach
    void setUp() {
        authorityRepository = mock(AuthorityRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        userOrganizationRepository = mock(UserOrganizationRepository.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        invoiceSequencesRepository = mock(InvoiceSequencesRepository.class);
        sessionsByPrincipalFinder = mock(FindByIndexNameSessionRepository.class);
        accessService = mock(AccessService.class);

        userManager = new UserManager(
            authorityRepository, organizationRepository, userOrganizationRepository,
            userRepository, passwordEncoder, invoiceSequencesRepository,
            sessionsByPrincipalFinder, accessService
        );
    }

    @Test
    void testFindUserOrganizations() {
        String username = "user";
        Organization org = mock(Organization.class);
        when(organizationRepository.findAllForUser(username)).thenReturn(Collections.singletonList(org));
        
        var result = userManager.findUserOrganizations(username);
        
        assertEquals(1, result.size());
        assertEquals(org, result.get(0));
    }

    @Test
    void testCreateOrganization() {
        alfio.model.modification.OrganizationModification om = new alfio.model.modification.OrganizationModification(null, "name", "email", "desc", "extId", "slug");
        
        Authentication principal = mock(Authentication.class);
        when(principal.getName()).thenReturn("admin");
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_ADMIN");
        doReturn(Collections.singletonList(authority)).when(principal).getAuthorities();
        
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("admin");
        when(userRepository.findEnabledByUsername("admin")).thenReturn(Optional.of(user));
        when(authorityRepository.checkRole("admin", Collections.singleton("ROLE_ADMIN"))).thenReturn(true);
        
        ch.digitalfondue.npjt.AffectedRowCountAndKey<Integer> arcak = mock(ch.digitalfondue.npjt.AffectedRowCountAndKey.class);
        when(arcak.getKey()).thenReturn(1);
        when(organizationRepository.create(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(arcak);
        when(invoiceSequencesRepository.initFor(1)).thenReturn(2);

        int result = userManager.createOrganization(om, principal);
        
        assertEquals(1, result);
        verify(organizationRepository).create("name", "desc", "email", "extId", "slug");
    }

    @Test
    void testUpdateOrganization() {
        alfio.model.modification.OrganizationModification om = new alfio.model.modification.OrganizationModification(10, "name", "email", "desc", "extId", "slug");
        
        Authentication principal = mock(Authentication.class);
        when(principal.getName()).thenReturn("admin");
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_ADMIN");
        doReturn(Collections.singletonList(authority)).when(principal).getAuthorities();
        
        Organization existing = mock(Organization.class);
        when(organizationRepository.getById(10)).thenReturn(existing);

        userManager.updateOrganization(om, principal);
        
        verify(organizationRepository).update(eq(10), eq("name"), eq("desc"), eq("email"), eq("extId"), eq("slug"));
    }
}
