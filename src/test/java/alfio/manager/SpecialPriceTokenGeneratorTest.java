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
package alfio.manager;

import alfio.manager.system.ConfigurationLevel;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.model.EventAndOrganizationId;
import alfio.model.SpecialPrice;
import alfio.model.TicketCategory;
import alfio.model.system.ConfigurationKeys;
import alfio.repository.EventRepository;
import alfio.repository.SpecialPriceRepository;
import alfio.repository.TicketCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SpecialPriceTokenGeneratorTest {

    @Mock
    private SpecialPriceRepository specialPriceRepository;
    @Mock
    private TicketCategoryRepository ticketCategoryRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ConfigurationManager configurationManager;

    private SpecialPriceTokenGenerator generator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new SpecialPriceTokenGenerator(
                configurationManager,
                specialPriceRepository,
                ticketCategoryRepository,
                eventRepository
        );
    }

    @Test
    public void testGeneratePendingCodesNoElements() {
        when(specialPriceRepository.findWaitingElements()).thenReturn(Collections.emptyList());
        generator.generatePendingCodes();
        verify(specialPriceRepository, never()).updateCode(anyString(), anyInt());
    }

    @Test
    public void testGeneratePendingCodesWithElements() {
        SpecialPrice.SpecialPriceTicketCategoryId item = mock(SpecialPrice.SpecialPriceTicketCategoryId.class);
        when(item.getTicketCategoryId()).thenReturn(1);
        when(item.getId()).thenReturn(42);
        when(specialPriceRepository.findWaitingElements()).thenReturn(Collections.singletonList(item));

        TicketCategory tc = mock(TicketCategory.class);
        when(tc.getEventId()).thenReturn(2);
        when(tc.getId()).thenReturn(1);
        when(ticketCategoryRepository.getByIdAndActive(1)).thenReturn(Optional.of(tc));

        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(eventRepository.findEventAndOrganizationIdById(2)).thenReturn(event);

        MaybeConfiguration config = mock(MaybeConfiguration.class);
        when(config.getValueAsIntOrDefault(6)).thenReturn(6);
        when(configurationManager.getFor(eq(ConfigurationKeys.SPECIAL_PRICE_CODE_LENGTH), any(ConfigurationLevel.class))).thenReturn(config);

        // First attempt returns count > 0 (code exists), second returns 0
        when(specialPriceRepository.countByCode(anyString())).thenReturn(1).thenReturn(0);

        generator.generatePendingCodes();

        verify(specialPriceRepository).updateCode(anyString(), eq(42));
    }

    @Test
    public void testGeneratePendingCodesDuplicateHandling() {
        SpecialPrice.SpecialPriceTicketCategoryId item = mock(SpecialPrice.SpecialPriceTicketCategoryId.class);
        when(item.getTicketCategoryId()).thenReturn(1);
        when(item.getId()).thenReturn(42);
        when(specialPriceRepository.findWaitingElements()).thenReturn(Collections.singletonList(item));

        TicketCategory tc = mock(TicketCategory.class);
        when(tc.getEventId()).thenReturn(2);
        when(tc.getId()).thenReturn(1);
        when(ticketCategoryRepository.getByIdAndActive(1)).thenReturn(Optional.of(tc));

        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(eventRepository.findEventAndOrganizationIdById(2)).thenReturn(event);

        MaybeConfiguration config = mock(MaybeConfiguration.class);
        when(config.getValueAsIntOrDefault(6)).thenReturn(6);
        when(configurationManager.getFor(eq(ConfigurationKeys.SPECIAL_PRICE_CODE_LENGTH), any(ConfigurationLevel.class))).thenReturn(config);

        when(specialPriceRepository.countByCode(anyString())).thenReturn(0);

        // Throw DataAccessException on the first updateCode call, then succeed
        AtomicInteger count = new AtomicInteger(0);
        doAnswer(invocation -> {
            if (count.incrementAndGet() == 1) {
                throw new DataIntegrityViolationException("Duplicate key error from test");
            }
            return 1;
        }).when(specialPriceRepository).updateCode(anyString(), eq(42));

        generator.generatePendingCodes();

        verify(specialPriceRepository, times(2)).updateCode(anyString(), eq(42));
    }

    @Test
    public void testGeneratePendingCodesCategoryNotFound() {
        SpecialPrice.SpecialPriceTicketCategoryId item = mock(SpecialPrice.SpecialPriceTicketCategoryId.class);
        when(item.getTicketCategoryId()).thenReturn(1);
        when(specialPriceRepository.findWaitingElements()).thenReturn(Collections.singletonList(item));

        when(ticketCategoryRepository.getByIdAndActive(1)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> generator.generatePendingCodes());
    }

    @Test
    public void testGeneratePendingCodesForCategory() {
        SpecialPrice.SpecialPriceTicketCategoryId item = mock(SpecialPrice.SpecialPriceTicketCategoryId.class);
        when(item.getTicketCategoryId()).thenReturn(1);
        when(item.getId()).thenReturn(42);
        when(specialPriceRepository.findWaitingElementsForCategory(1)).thenReturn(Collections.singletonList(item));

        TicketCategory tc = mock(TicketCategory.class);
        when(tc.getEventId()).thenReturn(2);
        when(tc.getId()).thenReturn(1);
        when(ticketCategoryRepository.getByIdAndActive(1)).thenReturn(Optional.of(tc));

        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(eventRepository.findEventAndOrganizationIdById(2)).thenReturn(event);

        MaybeConfiguration config = mock(MaybeConfiguration.class);
        when(config.getValueAsIntOrDefault(6)).thenReturn(6);
        when(configurationManager.getFor(eq(ConfigurationKeys.SPECIAL_PRICE_CODE_LENGTH), any(ConfigurationLevel.class))).thenReturn(config);

        when(specialPriceRepository.countByCode(anyString())).thenReturn(0);

        generator.generatePendingCodesForCategory(1);

        verify(specialPriceRepository).updateCode(anyString(), eq(42));
    }
}
