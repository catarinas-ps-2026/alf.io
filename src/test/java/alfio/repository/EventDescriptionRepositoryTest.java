package alfio.repository;

import alfio.model.EventDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventDescriptionRepositoryTest {

    private final EventDescriptionRepository eventDescriptionRepository = mock(EventDescriptionRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

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
