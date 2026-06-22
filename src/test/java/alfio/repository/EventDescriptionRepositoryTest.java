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
import static org.mockito.Mockito.*;

import alfio.model.EventDescription;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventDescriptionRepositoryTest {

    private final EventDescriptionRepository eventDescriptionRepository =
            mock(EventDescriptionRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testFindByEventIdAsMap() {
        EventDescription ed1 = mock(EventDescription.class);
        when(ed1.getLocale()).thenReturn("en");
        when(ed1.getDescription()).thenReturn("desc en");
        EventDescription ed2 = mock(EventDescription.class);
        when(ed2.getLocale()).thenReturn("it");
        when(ed2.getDescription()).thenReturn("desc it");

        when(eventDescriptionRepository.findByEventId(1)).thenReturn(List.of(ed1, ed2));

        Map<String, String> result = eventDescriptionRepository.findByEventIdAsMap(1);
        assertEquals(2, result.size());
        assertEquals("desc en", result.get("en"));
        assertEquals("desc it", result.get("it"));
    }

    @Test
    void testFindDescriptionByEventIdAsMap() {
        EventDescription.LocaleDescription ld1 = mock(EventDescription.LocaleDescription.class);
        when(ld1.getLocale()).thenReturn("en");
        when(ld1.getDescription()).thenReturn("desc en");
        EventDescription.LocaleDescription ld2 = mock(EventDescription.LocaleDescription.class);
        when(ld2.getLocale()).thenReturn("it");
        when(ld2.getDescription()).thenReturn("desc it");

        when(eventDescriptionRepository.findDescriptionByEventId(1)).thenReturn(List.of(ld1, ld2));

        Map<String, String> result = eventDescriptionRepository.findDescriptionByEventIdAsMap(1);
        assertEquals(2, result.size());
        assertEquals("desc en", result.get("en"));
        assertEquals("desc it", result.get("it"));
    }
}
