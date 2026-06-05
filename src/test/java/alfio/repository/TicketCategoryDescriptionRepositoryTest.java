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

import alfio.model.TicketCategoryDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketCategoryDescriptionRepositoryTest {

    private final TicketCategoryDescriptionRepository ticketCategoryDescriptionRepository = mock(TicketCategoryDescriptionRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testDescriptionForTicketCategory() {
        TicketCategoryDescription d1 = mock(TicketCategoryDescription.class);
        when(d1.getLocale()).thenReturn("en");
        when(d1.getDescription()).thenReturn("desc en");
        TicketCategoryDescription d2 = mock(TicketCategoryDescription.class);
        when(d2.getLocale()).thenReturn("it");
        when(d2.getDescription()).thenReturn("desc it");
        
        when(ticketCategoryDescriptionRepository.findByTicketCategoryId(1)).thenReturn(List.of(d1, d2));
        
        Map<String, String> result = ticketCategoryDescriptionRepository.descriptionForTicketCategory(1);
        assertEquals(2, result.size());
        assertEquals("desc en", result.get("en"));
        assertEquals("desc it", result.get("it"));
    }

    @Test
    void testDescriptionsByTicketCategory() {
        TicketCategoryDescription d1 = mock(TicketCategoryDescription.class);
        when(d1.getTicketCategoryId()).thenReturn(1);
        when(d1.getLocale()).thenReturn("en");
        when(d1.getDescription()).thenReturn("desc 1 en");
        
        TicketCategoryDescription d2 = mock(TicketCategoryDescription.class);
        when(d2.getTicketCategoryId()).thenReturn(2);
        when(d2.getLocale()).thenReturn("en");
        when(d2.getDescription()).thenReturn("desc 2 en");
        
        Collection<Integer> ids = List.of(1, 2);
        when(ticketCategoryDescriptionRepository.findByTicketCategoryIds(ids)).thenReturn(List.of(d1, d2));
        
        Map<Integer, Map<String, String>> result = ticketCategoryDescriptionRepository.descriptionsByTicketCategory(ids);
        assertEquals(2, result.size());
        assertEquals("desc 1 en", result.get(1).get("en"));
        assertEquals("desc 2 en", result.get(2).get("en"));
    }

    @Test
    void testDescriptionsByTicketCategory_Empty() {
        Map<Integer, Map<String, String>> result = ticketCategoryDescriptionRepository.descriptionsByTicketCategory(Collections.emptyList());
        assertTrue(result.isEmpty());
        verify(ticketCategoryDescriptionRepository, never()).findByTicketCategoryIds(anyCollection());
    }
}
