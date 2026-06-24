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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import alfio.model.Event;
import alfio.model.PriceContainer;
import alfio.model.TicketCategory;
import alfio.model.modification.AttendeeData;
import java.util.List;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class TicketRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final TicketRepository ticketRepository =
            mock(TicketRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testBulkTicketInitialization() {
        doReturn(jdbcTemplate).when(ticketRepository).getNamedParameterJdbcTemplate();
        MapSqlParameterSource[] args = new MapSqlParameterSource[1];
        ticketRepository.bulkTicketInitialization(args);
        verify(jdbcTemplate).batchUpdate(anyString(), eq(args));
    }

    @Test
    void testBulkTicketUpdate() {
        doReturn(jdbcTemplate).when(ticketRepository).getNamedParameterJdbcTemplate();
        TicketCategory ticketCategory = mock(TicketCategory.class);
        when(ticketCategory.getId()).thenReturn(1);
        when(ticketCategory.getSrcPriceCts()).thenReturn(1000);

        ticketRepository.bulkTicketUpdate(List.of(123), ticketCategory);

        ArgumentCaptor<MapSqlParameterSource[]> captor = ArgumentCaptor.forClass(MapSqlParameterSource[].class);
        verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

        MapSqlParameterSource[] captured = captor.getValue();
        assertEquals(1, captured.length);
        assertEquals(123, captured[0].getValue("id"));
        assertEquals(1, captured[0].getValue("categoryId"));
        assertEquals(1000, captured[0].getValue("srcPriceCts"));
    }

    @Test
    void testReserveTickets() {
        doReturn(jdbcTemplate).when(ticketRepository).getNamedParameterJdbcTemplate();
        when(ticketRepository.batchReserveTickets()).thenReturn("UPDATE...");
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class)))
                .thenReturn(new int[] {1});

        TicketCategory category = mock(TicketCategory.class);
        when(category.getId()).thenReturn(1);
        when(category.getSrcPriceCts()).thenReturn(1000);
        when(category.getCurrencyCode()).thenReturn("CHF");

        IntFunction<AttendeeData> attendeeDataSupplier = i -> AttendeeData.empty();

        ticketRepository.reserveTickets(
                "resId", List.of(123), category, "en", PriceContainer.VatStatus.INCLUDED, attendeeDataSupplier);

        verify(jdbcTemplate).batchUpdate(eq("UPDATE..."), any(MapSqlParameterSource[].class));
    }

    @Test
    void testBatchReleaseTickets() {
        doReturn(jdbcTemplate).when(ticketRepository).getNamedParameterJdbcTemplate();
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);

        ticketRepository.batchReleaseTickets("resId", List.of(123), event);

        verify(jdbcTemplate).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }

    @Test
    void testResetTickets() {
        doReturn(jdbcTemplate).when(ticketRepository).getNamedParameterJdbcTemplate();
        ticketRepository.resetTickets(List.of(123));
        verify(jdbcTemplate).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }

    @Test
    void testPreReserveTicket() {
        doReturn(jdbcTemplate).when(ticketRepository).getNamedParameterJdbcTemplate();
        ticketRepository.preReserveTicket(List.of(123));
        verify(jdbcTemplate).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }
}
