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

import alfio.model.EventAndOrganizationId;
import alfio.model.modification.AdminReservationModification;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminReservationRequestRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final AdminReservationRequestRepository adminReservationRequestRepository = mock(AdminReservationRequestRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testInsertRequest() {
        doReturn(jdbcTemplate).when(adminReservationRequestRepository).getNamedParameterJdbcTemplate();
        
        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(event.getId()).thenReturn(1);
        AdminReservationModification mod = mock(AdminReservationModification.class);
        
        adminReservationRequestRepository.insertRequest("req1", 123L, event, Stream.of(mod));
        
        verify(jdbcTemplate).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }

    @Test
    void testUpdateStatus() {
        doReturn(jdbcTemplate).when(adminReservationRequestRepository).getNamedParameterJdbcTemplate();
        
        adminReservationRequestRepository.updateStatus(List.of(new MapSqlParameterSource()));
        
        verify(jdbcTemplate).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }
}
