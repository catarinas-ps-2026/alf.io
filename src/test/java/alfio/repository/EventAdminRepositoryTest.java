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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventAdminRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final EventAdminRepository eventAdminRepository = mock(EventAdminRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testExistsBySlug() {
        when(eventAdminRepository.getJdbcTemplate()).thenReturn(jdbcTemplate);
        
        // RLS check
        when(jdbcTemplate.queryForObject(eq("select coalesce(current_setting('alfio.checkRowAccess', true), 'false') = 'true'"), any(SqlParameterSource.class), eq(Boolean.class)))
            .thenReturn(true);
            
        // Disable RLS
        when(jdbcTemplate.queryForObject(eq("select set_config('alfio.checkRowAccess', 'false', true)"), any(SqlParameterSource.class), eq(Boolean.class)))
            .thenReturn(false);
            
        // Actual check
        when(jdbcTemplate.queryForObject(eq("select exists(select 1 from event where short_name = :slug)"), eq(Map.of("slug", "test-slug")), eq(Boolean.class)))
            .thenReturn(true);
            
        // Re-enable RLS
        when(jdbcTemplate.queryForObject(eq("select set_config('alfio.checkRowAccess', 'true', true)"), any(SqlParameterSource.class), eq(Boolean.class)))
            .thenReturn(true);

        boolean result = eventAdminRepository.existsBySlug("test-slug");
        assertTrue(result);
        
        verify(jdbcTemplate).queryForObject(eq("select exists(select 1 from event where short_name = :slug)"), eq(Map.of("slug", "test-slug")), eq(Boolean.class));
    }
}
