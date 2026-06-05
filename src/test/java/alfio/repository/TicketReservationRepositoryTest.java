package alfio.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketReservationRepositoryTest {

    private final TicketReservationRepository ticketReservationRepository = mock(TicketReservationRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testCountTicketsInReservationForCategories_Empty() {
        when(ticketReservationRepository.countTicketsInReservationNoCategories("res1")).thenReturn(5);
        
        Integer result = ticketReservationRepository.countTicketsInReservationForCategories("res1", Collections.emptyList());
        assertEquals(5, result);
        verify(ticketReservationRepository).countTicketsInReservationNoCategories("res1");
    }

    @Test
    void testCountTicketsInReservationForCategories_Null() {
        when(ticketReservationRepository.countTicketsInReservationNoCategories("res1")).thenReturn(5);
        
        Integer result = ticketReservationRepository.countTicketsInReservationForCategories("res1", null);
        assertEquals(5, result);
        verify(ticketReservationRepository).countTicketsInReservationNoCategories("res1");
    }

    @Test
    void testCountTicketsInReservationForCategories_WithCategories() {
        List<Integer> categories = List.of(1, 2);
        when(ticketReservationRepository.countTicketsInReservationForExistingCategories("res1", categories)).thenReturn(3);
        
        Integer result = ticketReservationRepository.countTicketsInReservationForCategories("res1", categories);
        assertEquals(3, result);
        verify(ticketReservationRepository).countTicketsInReservationForExistingCategories("res1", categories);
    }
}
