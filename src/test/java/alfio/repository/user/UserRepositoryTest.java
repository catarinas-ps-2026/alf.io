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
package alfio.repository.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    private final UserRepository userRepository =
            mock(UserRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testNullSafeFindIdByUserName() {
        when(userRepository.findIdByUserName("user1")).thenReturn(Optional.of(123));

        assertEquals(Optional.of(123), userRepository.nullSafeFindIdByUserName("user1"));
        assertTrue(userRepository.nullSafeFindIdByUserName(null).isEmpty());
        assertTrue(userRepository.nullSafeFindIdByUserName("").isEmpty());
        assertTrue(userRepository.nullSafeFindIdByUserName("  ").isEmpty());
    }

    @Test
    void testDeleteUserAndReferences() {
        int userId = 123;
        userRepository.deleteUserAndReferences(userId);

        verify(userRepository).deleteUserFromSponsorScan(userId);
        verify(userRepository).deleteUserFromOrganization(userId);
        verify(userRepository).deleteUserFromAuthority(userId);
        verify(userRepository).deleteUserProfile(userId);
        verify(userRepository).deleteUser(userId);
    }
}
