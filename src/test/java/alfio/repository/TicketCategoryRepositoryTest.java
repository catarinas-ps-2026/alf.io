package alfio.repository;

import alfio.model.EntityIdAndMetadata;
import alfio.model.TicketCategory;
import alfio.model.TicketCategoryStatisticView;
import alfio.model.metadata.AlfioMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TicketCategoryRepositoryTest {

    private final TicketCategoryRepository ticketCategoryRepository = mock(TicketCategoryRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testFindByEventIdAsMap() {
        TicketCategory tc1 = mock(TicketCategory.class);
        when(tc1.getId()).thenReturn(1);
        TicketCategory tc2 = mock(TicketCategory.class);
        when(tc2.getId()).thenReturn(2);
        
        when(ticketCategoryRepository.findAllTicketCategories(1)).thenReturn(List.of(tc1, tc2));
        
        Map<Integer, TicketCategory> result = ticketCategoryRepository.findByEventIdAsMap(1);
        assertEquals(2, result.size());
        assertEquals(tc1, result.get(1));
        assertEquals(tc2, result.get(2));
    }

    @Test
    void testFindCategoryMetadataForEventGroupByCategoryId() {
        EntityIdAndMetadata em1 = mock(EntityIdAndMetadata.class);
        when(em1.getId()).thenReturn(1);
        AlfioMetadata m1 = mock(AlfioMetadata.class);
        when(em1.getMetadata()).thenReturn(m1);
        
        EntityIdAndMetadata em2 = mock(EntityIdAndMetadata.class);
        when(em2.getId()).thenReturn(2);
        when(em2.getMetadata()).thenReturn(null);
        
        when(ticketCategoryRepository.findMetadataForCategoriesInEvent(1)).thenReturn(List.of(em1, em2));
        
        Map<Integer, AlfioMetadata> result = ticketCategoryRepository.findCategoryMetadataForEventGroupByCategoryId(1);
        assertEquals(1, result.size());
        assertEquals(m1, result.get(1));
    }

    @Test
    void testGetTicketAllocation() {
        TicketCategory tc1 = mock(TicketCategory.class);
        when(tc1.isBounded()).thenReturn(true);
        when(tc1.getMaxTickets()).thenReturn(10);
        
        TicketCategory tc2 = mock(TicketCategory.class);
        when(tc2.isBounded()).thenReturn(false);
        when(tc2.getMaxTickets()).thenReturn(100);
        
        TicketCategory tc3 = mock(TicketCategory.class);
        when(tc3.isBounded()).thenReturn(true);
        when(tc3.getMaxTickets()).thenReturn(5);
        
        when(ticketCategoryRepository.findAllTicketCategories(1)).thenReturn(List.of(tc1, tc2, tc3));
        
        assertEquals(15, ticketCategoryRepository.getTicketAllocation(1));
    }

    @Test
    void testFindStatisticsForEventIdByCategoryId() {
        TicketCategoryStatisticView s1 = mock(TicketCategoryStatisticView.class);
        when(s1.getId()).thenReturn(1);
        TicketCategoryStatisticView s2 = mock(TicketCategoryStatisticView.class);
        when(s2.getId()).thenReturn(2);
        
        when(ticketCategoryRepository.findStatisticsForEventId(1)).thenReturn(List.of(s1, s2));
        
        Map<Integer, TicketCategoryStatisticView> result = ticketCategoryRepository.findStatisticsForEventIdByCategoryId(1);
        assertEquals(2, result.size());
        assertEquals(s1, result.get(1));
        assertEquals(s2, result.get(2));
    }
}
