package alfio.repository.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    private final UserRepository userRepository = mock(UserRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

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
