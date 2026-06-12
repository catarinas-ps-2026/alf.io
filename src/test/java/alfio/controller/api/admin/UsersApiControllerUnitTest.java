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
package alfio.controller.api.admin;

import alfio.manager.AccessService;
import alfio.manager.FileUploadManager;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.user.UserManager;
import alfio.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersApiControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private UserManager userManager;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private AccessService accessService;

    @Mock
    private FileUploadManager fileUploadManager;

    @Mock
    private Principal principal;

    @InjectMocks
    private UsersApiController usersApiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usersApiController).build();
    }

    @Test
    void testRetrieveDetails() throws Exception {
        User mockUser = new User(1, "admin", "Admin", "User", "admin@admin.com", true, User.Type.INTERNAL, null, "Desc");
        when(principal.getName()).thenReturn("admin");
        when(userManager.findUserByUsername("admin")).thenReturn(mockUser);

        var authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        var securityContext = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        org.springframework.security.core.authority.SimpleGrantedAuthority authority = 
            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERATOR");
        java.util.Collection authorities = java.util.Collections.singleton(authority);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        Map<String, String> details = usersApiController.retrieveDetails(principal);

        assertEquals("admin", details.get("username"));
        assertEquals("Admin", details.get("firstName"));
        assertEquals("User", details.get("lastName"));
        assertEquals("Desc", details.get("description"));
        assertEquals("OPERATOR", details.get("userType"));
        
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void testGetLoggedUserType_Operator() {
        var authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        var securityContext = org.mockito.Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        
        org.springframework.security.core.authority.SimpleGrantedAuthority authority = 
            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERATOR");
            
        java.util.Collection authorities = java.util.Collections.singleton(authority);
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        String userType = usersApiController.getLoggedUserType();

        assertEquals("OPERATOR", userType);
        
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void testGetAllOrganizations() {
        when(principal.getName()).thenReturn("admin");
        alfio.model.user.Organization mockOrg = org.mockito.Mockito.mock(alfio.model.user.Organization.class);
        when(userManager.findUserOrganizations("admin")).thenReturn(java.util.Collections.singletonList(mockOrg));

        java.util.List<alfio.model.user.Organization> orgs = usersApiController.getAllOrganizations(principal);

        assertEquals(1, orgs.size());
        assertEquals(mockOrg, orgs.get(0));
    }
}
