package alfio.repository;

import alfio.model.AdditionalService;
import alfio.model.AdditionalServiceItem;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdditionalServiceRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final AdditionalServiceRepository additionalServiceRepository = mock(AdditionalServiceRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testGetCount() throws SQLException {
        doReturn(jdbcTemplate).when(additionalServiceRepository).getJdbcTemplate();
        
        doAnswer(invocation -> {
            RowCallbackHandler rch = invocation.getArgument(2);
            ResultSet rs = mock(ResultSet.class);
            when(rs.getInt("additional_service_id_fk")).thenReturn(1);
            when(rs.getString("status")).thenReturn(AdditionalServiceItem.AdditionalServiceItemStatus.ACQUIRED.name());
            when(rs.getInt("cnt")).thenReturn(5);
            rch.processRow(rs);
            return null;
        }).when(jdbcTemplate).query(anyString(), anyMap(), any(RowCallbackHandler.class));

    Map<Integer, Map<AdditionalServiceItem.AdditionalServiceItemStatus, Integer>> result = additionalServiceRepository.getCount(123);
        
        assertEquals(1, result.size());
        assertTrue(result.containsKey(1));
        assertEquals(5, result.get(1).get(AdditionalServiceItem.AdditionalServiceItemStatus.ACQUIRED));
    }

    @Test
    void testGetTypeByIds() throws SQLException {
        doReturn(jdbcTemplate).when(additionalServiceRepository).getJdbcTemplate();
        
        doAnswer(invocation -> {
            RowCallbackHandler rch = invocation.getArgument(2);
            ResultSet rs = mock(ResultSet.class);
            when(rs.getInt("id")).thenReturn(1);
            when(rs.getString("service_type")).thenReturn(AdditionalService.AdditionalServiceType.DONATION.name());
            rch.processRow(rs);
            return null;
        }).when(jdbcTemplate).query(anyString(), anyMap(), any(RowCallbackHandler.class));

        Map<Integer, AdditionalService.AdditionalServiceType> result = additionalServiceRepository.getTypeByIds(List.of(1));
        
        assertEquals(1, result.size());
        assertEquals(AdditionalService.AdditionalServiceType.DONATION, result.get(1));
    }
}
