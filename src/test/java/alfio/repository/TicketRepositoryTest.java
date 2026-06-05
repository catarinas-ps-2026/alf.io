package alfio.repository;

import alfio.model.Event;
import alfio.model.TicketCategory;
import alfio.model.modification.AttendeeData;
import alfio.model.PriceContainer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TicketRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

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
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class))).thenReturn(new int[]{1});
        
        TicketCategory category = mock(TicketCategory.class);
        when(category.getId()).thenReturn(1);
        when(category.getSrcPriceCts()).thenReturn(1000);
        when(category.getCurrencyCode()).thenReturn("CHF");
        
        IntFunction<AttendeeData> attendeeDataSupplier = i -> AttendeeData.empty();
        
        ticketRepository.reserveTickets("resId", List.of(123), category, "en", PriceContainer.VatStatus.INCLUDED, attendeeDataSupplier);
        
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
