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

import alfio.model.modification.SubscriptionDescriptorModification;
import alfio.model.subscription.AvailableSubscriptionsByEvent;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final SubscriptionRepository repository = mock(SubscriptionRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testPreGenerateSubscriptions() {
        doReturn(jdbcTemplate).when(repository).getJdbcTemplate();
        SubscriptionDescriptorModification modification = mock(SubscriptionDescriptorModification.class);
        when(modification.getOrganizationId()).thenReturn(1);
        when(modification.getTimeZone()).thenReturn(ZoneId.of("UTC"));
        when(modification.getValidityFrom()).thenReturn(ZonedDateTime.now());
        when(modification.getValidityTo()).thenReturn(ZonedDateTime.now());
        
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class))).thenReturn(new int[]{10});
        
        UUID id = UUID.randomUUID();
        repository.preGenerateSubscriptions(modification, id, 10);
        
        verify(jdbcTemplate).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLoadAvailableSubscriptionsByEvent() throws Exception {
        doReturn(jdbcTemplate).when(repository).getJdbcTemplate();

        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("event_id")).thenReturn(1);
        when(rs.getInt("organization_id")).thenReturn(2);
        UUID subId = UUID.randomUUID();
        when(rs.getObject("subscription_id", UUID.class)).thenReturn(subId);
        UUID descId = UUID.randomUUID();
        when(rs.getObject("descriptor_id", UUID.class)).thenReturn(descId);
        when(rs.getString("email_address")).thenReturn("test@test.com");
        when(rs.getString("first_name")).thenReturn("First");
        when(rs.getString("last_name")).thenReturn("Last");
        when(rs.getString("user_language")).thenReturn("en");
        when(rs.getString("reservation_email")).thenReturn("res@test.com");
        when(rs.getString("additional_fields")).thenReturn("[]");
        when(rs.getString("compatible_categories")).thenReturn("[]");

        doAnswer(invocation -> {
            org.springframework.jdbc.core.RowCallbackHandler rch = invocation.getArgument(2);
            rch.processRow(rs);
            return null;
        }).when(jdbcTemplate).query(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), any(org.springframework.jdbc.core.RowCallbackHandler.class));

        var result = repository.loadAvailableSubscriptionsByEvent(1, 2);
        assertNotNull(result);
        assertTrue(result.containsKey(1));
        assertEquals(1, result.get(1).size());
        AvailableSubscriptionsByEvent sub = result.get(1).get(0);
        assertEquals(1, sub.getEventId());
        assertEquals(subId, sub.getSubscriptionId());
    }
}
