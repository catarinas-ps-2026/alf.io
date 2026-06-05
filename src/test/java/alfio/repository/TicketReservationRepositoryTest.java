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
