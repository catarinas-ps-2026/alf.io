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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import alfio.model.AdditionalService;
import alfio.model.AdditionalServiceItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class AdditionalServiceRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final AdditionalServiceRepository additionalServiceRepository =
            mock(AdditionalServiceRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testGetCount() throws SQLException {
        doReturn(jdbcTemplate).when(additionalServiceRepository).getJdbcTemplate();

        doAnswer(invocation -> {
                    RowCallbackHandler rch = invocation.getArgument(2);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.getInt("additional_service_id_fk")).thenReturn(1);
                    when(rs.getString("status"))
                            .thenReturn(AdditionalServiceItem.AdditionalServiceItemStatus.ACQUIRED.name());
                    when(rs.getInt("cnt")).thenReturn(5);
                    rch.processRow(rs);
                    return null;
                })
                .when(jdbcTemplate)
                .query(anyString(), anyMap(), any(RowCallbackHandler.class));

        Map<Integer, Map<AdditionalServiceItem.AdditionalServiceItemStatus, Integer>> result =
                additionalServiceRepository.getCount(123);

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
                    when(rs.getString("service_type"))
                            .thenReturn(AdditionalService.AdditionalServiceType.DONATION.name());
                    rch.processRow(rs);
                    return null;
                })
                .when(jdbcTemplate)
                .query(anyString(), anyMap(), any(RowCallbackHandler.class));

        Map<Integer, AdditionalService.AdditionalServiceType> result =
                additionalServiceRepository.getTypeByIds(List.of(1));

        assertEquals(1, result.size());
        assertEquals(AdditionalService.AdditionalServiceType.DONATION, result.get(1));
    }
}
