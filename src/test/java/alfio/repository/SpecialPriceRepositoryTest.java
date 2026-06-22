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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.model.SpecialPrice;
import alfio.model.TicketCategory;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@ExtendWith(MockitoExtension.class)
class SpecialPriceRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final SpecialPriceRepository specialPriceRepository =
            mock(SpecialPriceRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testBulkInsert() {
        when(specialPriceRepository.getNamedParameterJdbcTemplate()).thenReturn(jdbcTemplate);

        TicketCategory tc = mock(TicketCategory.class);
        when(tc.getId()).thenReturn(1);
        when(tc.getSrcPriceCts()).thenReturn(1000);

        specialPriceRepository.bulkInsert(tc, 5);

        verify(jdbcTemplate)
                .batchUpdate(
                        eq(
                                "insert into special_price (code, price_cts, ticket_category_id, status, sent_ts) values(:code, :priceInCents, :ticketCategoryId, :status, null)"),
                        any(MapSqlParameterSource[].class));
    }

    @Test
    void testFindAllByCategoriesIdsMapped() {
        SpecialPrice sp1 = mock(SpecialPrice.class);
        when(sp1.getTicketCategoryId()).thenReturn(1);
        SpecialPrice sp2 = mock(SpecialPrice.class);
        when(sp2.getTicketCategoryId()).thenReturn(1);
        SpecialPrice sp3 = mock(SpecialPrice.class);
        when(sp3.getTicketCategoryId()).thenReturn(2);

        Collection<Integer> ids = List.of(1, 2);
        when(specialPriceRepository.findAllByCategoriesIds(ids)).thenReturn(List.of(sp1, sp2, sp3));

        Map<Integer, List<SpecialPrice>> result = specialPriceRepository.findAllByCategoriesIdsMapped(ids);
        assertEquals(2, result.size());
        assertEquals(2, result.get(1).size());
        assertEquals(1, result.get(2).size());
    }

    @Test
    void testFindAllByCategoriesIdsMapped_Empty() {
        Map<Integer, List<SpecialPrice>> result =
                specialPriceRepository.findAllByCategoriesIdsMapped(Collections.emptyList());
        assertTrue(result.isEmpty());
        verify(specialPriceRepository, never()).findAllByCategoriesIds(anyCollection());
    }
}
