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

import alfio.manager.user.UserManager;
import alfio.model.ReservationsByEvent;
import alfio.model.user.Organization;
import alfio.repository.ExportRepository;
import alfio.util.ClockProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ExportManagerTest {

    @Mock
    private ExportRepository exportRepository;
    @Mock
    private ClockProvider clockProvider;
    @Mock
    private UserManager userManager;

    private ExportManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Clock fixedClock = Clock.fixed(
                ZonedDateTime.of(2026, 6, 4, 12, 0, 0, 0, ZoneId.of("UTC")).toInstant(),
                ZoneId.of("UTC")
        );
        when(clockProvider.getClock()).thenReturn(fixedClock);

        manager = new ExportManager(exportRepository, clockProvider, userManager);
    }

    @Test
    public void testReservationsForIntervalInvalidDates() {
        LocalDate from = LocalDate.of(2026, 6, 5);
        LocalDate to = LocalDate.of(2026, 6, 4);
        Principal principal = mock(Principal.class);

        assertThrows(IllegalArgumentException.class, () -> manager.reservationsForInterval(from, to, principal));
    }

    @Test
    public void testReservationsForIntervalEmptyOrganizations() {
        LocalDate from = LocalDate.of(2026, 6, 4);
        LocalDate to = LocalDate.of(2026, 6, 5);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test-user");
        when(userManager.findUserOrganizations("test-user")).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> manager.reservationsForInterval(from, to, principal));
    }

    @Test
    public void testReservationsForIntervalSuccess() {
        LocalDate from = LocalDate.of(2026, 6, 4);
        LocalDate to = LocalDate.of(2026, 6, 5);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("test-user");

        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(100);
        when(userManager.findUserOrganizations("test-user")).thenReturn(Collections.singletonList(org));

        List<ReservationsByEvent> expectedList = Collections.singletonList(mock(ReservationsByEvent.class));
        
        ZoneId zoneId = ZoneId.of("UTC");
        ZonedDateTime zonedFrom = from.atStartOfDay().atZone(zoneId);
        ZonedDateTime zonedTo = to.plusDays(1).atStartOfDay().minusSeconds(1).atZone(zoneId);

        when(exportRepository.allReservationsForInterval(eq(zonedFrom), eq(zonedTo), eq(Collections.singletonList(100))))
                .thenReturn(expectedList);

        List<ReservationsByEvent> actualList = manager.reservationsForInterval(from, to, principal);

        assertEquals(expectedList, actualList);
        verify(exportRepository).allReservationsForInterval(eq(zonedFrom), eq(zonedTo), eq(Collections.singletonList(100)));
    }
}
