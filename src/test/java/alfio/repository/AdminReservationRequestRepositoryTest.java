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
