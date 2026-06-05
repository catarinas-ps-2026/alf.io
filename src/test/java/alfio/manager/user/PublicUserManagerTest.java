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

import alfio.controller.form.UpdateProfileForm;
import alfio.manager.ExtensionManager;
import alfio.model.user.PublicUserProfile;
import alfio.model.user.User;
import alfio.repository.PurchaseContextFieldRepository;
import alfio.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PublicUserManagerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ExtensionManager extensionManager;
    @Mock
    private UserManager userManager;
    @Mock
    private PurchaseContextFieldRepository purchaseContextFieldRepository;

    private PublicUserManager publicUserManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        publicUserManager = new PublicUserManager(
                userRepository,
                extensionManager,
                userManager,
                purchaseContextFieldRepository
        );
    }

    @Test
    public void testDeleteUserProfileSuccess() {
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getName()).thenReturn("test-user");

        User user = mock(User.class);
        when(user.getId()).thenReturn(42);
        when(user.getType()).thenReturn(User.Type.PUBLIC);
        when(userManager.findOptionalEnabledUserByUsername("test-user")).thenReturn(Optional.of(user));

        assertTrue(publicUserManager.deleteUserProfile(token));

        verify(userRepository).deleteUserProfile(42);
        verify(userRepository).invalidatePublicUser(eq(42), anyString());
        verify(extensionManager).handlePublicUserDelete(token, user);
    }

    @Test
    public void testDeleteUserProfileUserNotPublic() {
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getName()).thenReturn("test-user");

        User user = mock(User.class);
        when(user.getType()).thenReturn(User.Type.INTERNAL);
        when(userManager.findOptionalEnabledUserByUsername("test-user")).thenReturn(Optional.of(user));

        assertFalse(publicUserManager.deleteUserProfile(token));

        verifyNoInteractions(userRepository, extensionManager);
    }

    @Test
    public void testFindOptionalProfileForUser() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test-user");

        User user = mock(User.class);
        when(user.getId()).thenReturn(42);
        when(userManager.findOptionalEnabledUserByUsername("test-user")).thenReturn(Optional.of(user));

        PublicUserProfile profile = mock(PublicUserProfile.class);
        when(userRepository.loadUserProfile(42)).thenReturn(Optional.of(profile));

        var result = publicUserManager.findOptionalProfileForUser(auth);

        assertTrue(result.isPresent());
        assertEquals(user, result.get().getLeft());
        assertEquals(profile, result.get().getRight().orElse(null));
    }

    @Test
    public void testUpdateProfile() {
        User original = mock(User.class);
        when(original.getId()).thenReturn(42);
        when(original.getUsername()).thenReturn("username");
        when(original.getEmailAddress()).thenReturn("email@example.com");
        when(original.getDescription()).thenReturn("desc");

        UpdateProfileForm form = mock(UpdateProfileForm.class);
        when(form.getFirstName()).thenReturn("First");
        when(form.getLastName()).thenReturn("Last");
        when(form.getBillingAddressCompany()).thenReturn("Company");
        when(form.getBillingAddressLine1()).thenReturn("L1");
        when(form.getBillingAddressLine2()).thenReturn("L2");
        when(form.getBillingAddressZip()).thenReturn("12345");
        when(form.getBillingAddressCity()).thenReturn("City");
        when(form.getBillingAddressState()).thenReturn("State");
        when(form.getVatCountryCode()).thenReturn("IT");
        when(form.getVatNr()).thenReturn("12345");
        when(form.hasAdditionalInfo()).thenReturn(false);

        PublicUserProfile profile = mock(PublicUserProfile.class);
        when(profile.getAdditionalData()).thenReturn(Collections.emptyMap());
        when(userRepository.loadUserProfile(42)).thenReturn(Optional.of(profile));
        when(userRepository.persistUserProfile(eq(42), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);

        Optional<PublicUserProfile> result = publicUserManager.updateProfile(original, form, false);

        assertTrue(result.isPresent());
        verify(userRepository).update(42, "username", "First", "Last", "email@example.com", "desc");
        verify(userRepository).persistUserProfile(eq(42), eq("Company"), eq("L1"), eq("L2"), eq("12345"), eq("City"), eq("State"), eq("IT"), eq("12345"), any(), any());
    }

    @Test
    public void testPersistProfileForPublicUser() {
        java.security.Principal principal = mock(java.security.Principal.class);
        when(principal.getName()).thenReturn("test-user");
        
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        
        alfio.model.TicketReservationAdditionalInfo info = mock(alfio.model.TicketReservationAdditionalInfo.class);
        when(info.getBillingAddressCompany()).thenReturn("Company");
        
        when(userRepository.findIdByUserName("test-user")).thenReturn(Optional.of(42));
        
        publicUserManager.persistProfileForPublicUser(principal, new Object(), bindingResult, info, Map.of());
        
        verify(userRepository).persistUserProfile(eq(42), eq("Company"), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
