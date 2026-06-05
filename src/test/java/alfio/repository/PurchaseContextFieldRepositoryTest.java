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

import alfio.model.*;
import alfio.model.subscription.SubscriptionDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseContextFieldRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final PurchaseContextFieldRepository repository = mock(PurchaseContextFieldRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testGetFieldValueJson() {
        assertThrows(NullPointerException.class, () -> repository.getFieldValueJson(null));
        //assertEquals("[]", repository.getFieldValueJson(Collections.emptyList()));
        assertEquals("a", repository.getFieldValueJson(List.of("a")));
        assertEquals("[\"a\",\"b\"]", repository.getFieldValueJson(List.of("a", "b")));
    }

    @Test
    void testHasOptionalData() {
        doReturn(1).when(repository).countFilledOptionalData(1);
        assertTrue(repository.hasOptionalData(1));

        doReturn(0).when(repository).countFilledOptionalData(2);
        assertFalse(repository.hasOptionalData(2));
    }

    @Test
    void testFindAllByTicketIdGroupedByName() {
        PurchaseContextFieldValue v1 = new PurchaseContextFieldValue(1, null, null, 1, "n1", "v1", null);
        PurchaseContextFieldValue v2 = new PurchaseContextFieldValue(1, null, null, 2, "n2", "v2", null);

        doReturn(List.of(v1, v2)).when(repository).findAllByTicketId(1);

        Map<String, PurchaseContextFieldValue> result = repository.findAllByTicketIdGroupedByName(1, false);
        assertEquals(2, result.size());
        assertEquals(v1, result.get("n1"));
        assertEquals(v2, result.get("n2"));
    }

    @Test
    void testUpdateOrInsertForEvent() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(123);
        when(event.supportsLinkedAdditionalServices()).thenReturn(true);

        PurchaseContextFieldConfiguration config = mock(PurchaseContextFieldConfiguration.class);
        when(config.getName()).thenReturn("field1");
        when(config.getId()).thenReturn(1L);
        when(config.isReadOnly()).thenReturn(false);

        doReturn(List.of(config)).when(repository).findAdditionalFieldsForEvent(123);

        PurchaseContextFieldValue existing = mock(PurchaseContextFieldValue.class);
        when(existing.getFieldConfigurationId()).thenReturn(1L);
        when(existing.getTicketId()).thenReturn(456);
        doReturn(Map.of("field1", existing)).when(repository).findAllByTicketIdGroupedByName(456, true);

        repository.updateOrInsert(Map.of("field1", List.of("value1")), event, 456, null);

        verify(repository).updateValue(eq(456), isNull(), eq(1L), eq("value1"));
    }

    @Test
    void testUpdateOrInsertForSubscription() {
        SubscriptionDescriptor descriptor = mock(SubscriptionDescriptor.class);
        UUID subDescriptorId = UUID.randomUUID();
        when(descriptor.getId()).thenReturn(subDescriptorId);
        when(descriptor.getOrganizationId()).thenReturn(1);

        PurchaseContextFieldConfiguration config = mock(PurchaseContextFieldConfiguration.class);
        when(config.getName()).thenReturn("field1");
        when(config.getId()).thenReturn(1L);
        when(config.isReadOnly()).thenReturn(false);

        doReturn(List.of(config)).when(repository).findAdditionalFieldsForSubscriptionDescriptor(subDescriptorId);

        UUID subId = UUID.randomUUID();
        doReturn(Collections.emptyList()).when(repository).findAllValuesBySubscriptionIds(any());

        repository.updateOrInsert(Map.of("field1", List.of("value1")), descriptor, null, subId);

        verify(repository).insertValue(isNull(), eq(subId), eq(1), eq(1L), eq("value1"), eq(PurchaseContextFieldConfiguration.Context.SUBSCRIPTION));
    }

    @Test
    void testRetrieveStatsCountry() {
        PurchaseContextFieldConfiguration config = mock(PurchaseContextFieldConfiguration.class);
        when(config.isCountryField()).thenReturn(true);
        doReturn(config).when(repository).findById(1L);

        RestrictedValueStats.RestrictedValueCount c1 = new RestrictedValueStats.RestrictedValueCount("CH", 10);
        RestrictedValueStats.RestrictedValueCount c2 = new RestrictedValueStats.RestrictedValueCount("IT", 5);
        doReturn(List.of(c1, c2)).when(repository).getValueStats(1L);

        List<RestrictedValueStats> stats = repository.retrieveStats(1L);
        assertEquals(2, stats.size());
        assertEquals("CH", stats.get(0).getName());
        assertEquals(10, stats.get(0).getCount());
        assertEquals(67, stats.get(0).getPercentage());
        assertEquals("IT", stats.get(1).getName());
        assertEquals(5, stats.get(1).getCount());
        assertEquals(33, stats.get(1).getPercentage());
    }

    @Test
    void testRetrieveStatsRestrictedValues() {
        PurchaseContextFieldConfiguration config = mock(PurchaseContextFieldConfiguration.class);
        when(config.isCountryField()).thenReturn(false);
        when(config.getRestrictedValues()).thenReturn(List.of("A", "B"));
        doReturn(config).when(repository).findById(1L);

        RestrictedValueStats.RestrictedValueCount c1 = new RestrictedValueStats.RestrictedValueCount("A", 10);
        doReturn(List.of(c1)).when(repository).getValueStats(1L);

        List<RestrictedValueStats> stats = repository.retrieveStats(1L);
        assertEquals(2, stats.size());
        assertEquals("A", stats.get(0).getName());
        assertEquals(10, stats.get(0).getCount());
        assertEquals(100, stats.get(0).getPercentage());
        assertEquals("B", stats.get(1).getName());
        assertEquals(0, stats.get(1).getCount());
        assertEquals(0, stats.get(1).getPercentage());
        
        // case total == 0
        doReturn(Collections.emptyList()).when(repository).getValueStats(1L);
        stats = repository.retrieveStats(1L);
        assertEquals(2, stats.size());
        assertEquals(0, stats.get(0).getPercentage());
        assertEquals(0, stats.get(1).getPercentage());
    }

    @Test
    void testFindAdditionalFieldNamesForEvents() throws Exception {
        doReturn(jdbcTemplate).when(repository).getJdbcTemplate();
        
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getInt(1)).thenReturn(1, 1);
        when(rs.getString(2)).thenReturn("f1", "f2");
        
        doAnswer(invocation -> {
            org.springframework.jdbc.core.ResultSetExtractor<Map<Integer, Set<String>>> rse = invocation.getArgument(2);
            return rse.extractData(rs);
        }).when(jdbcTemplate).query(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), any(org.springframework.jdbc.core.ResultSetExtractor.class));
        
        Map<Integer, Set<String>> result = repository.findAdditionalFieldNamesForEvents(List.of(1));
        assertEquals(1, result.size());
        assertTrue(result.get(1).containsAll(Set.of("f1", "f2")));
    }
}
