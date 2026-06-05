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

import alfio.manager.support.CheckInStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final EventRepository eventRepository = mock(EventRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testGetZoneIdByEventId() {
        doReturn("UTC").when(eventRepository).getTimeZoneByEventId(1);
        assertNotNull(eventRepository.getZoneIdByEventId(1));
        assertEquals(TimeZone.getTimeZone("UTC").toZoneId(), eventRepository.getZoneIdByEventId(1));
    }

    @Test
    void testRetrieveCheckInStatisticsForEventWithCategories() {
        doReturn(jdbcTemplate).when(eventRepository).getJdbcTemplate();
        CheckInStatistics expected = mock(CheckInStatistics.class);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(ResultSetExtractor.class))).thenReturn(expected);

        CheckInStatistics result = eventRepository.retrieveCheckInStatisticsForEvent(1, List.of(10, 11));
        
        verify(jdbcTemplate).query(contains("category_id in (:categories)"), any(MapSqlParameterSource.class), any(ResultSetExtractor.class));
        assertEquals(expected, result);
    }

    @Test
    void testRetrieveCheckInStatisticsForEventWithoutCategories() {
        doReturn(jdbcTemplate).when(eventRepository).getJdbcTemplate();
        CheckInStatistics expected = mock(CheckInStatistics.class);
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(ResultSetExtractor.class))).thenReturn(expected);

        CheckInStatistics result = eventRepository.retrieveCheckInStatisticsForEvent(1, Collections.emptyList());
        
        verify(jdbcTemplate).query(eq(EventRepository.STATISTICS_QUERY), any(MapSqlParameterSource.class), any(ResultSetExtractor.class));
        assertEquals(expected, result);
    }
}
