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

import alfio.model.AdditionalServiceText;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AdditionalServiceTextRepositoryTest {

    private final AdditionalServiceTextRepository additionalServiceTextRepository = mock(AdditionalServiceTextRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testGetDescriptionsByAdditionalServiceIds() {
        int serviceId = 1;
        AdditionalServiceText text = new AdditionalServiceText(10, serviceId, "en", AdditionalServiceText.TextType.DESCRIPTION, "Value");
        doReturn(List.of(text)).when(additionalServiceTextRepository).findAllByAdditionalServiceIds(anyCollection());

        Map<Integer, Map<AdditionalServiceText.TextType, Map<String, String>>> result = additionalServiceTextRepository.getDescriptionsByAdditionalServiceIds(List.of(serviceId));
        
        assertEquals(1, result.size());
        assertEquals("Value", result.get(serviceId).get(AdditionalServiceText.TextType.DESCRIPTION).get("en"));
    }

    @Test
    void testFindBestMatchByLocaleAndType() {
        int serviceId = 1;
        String locale = "en";
        AdditionalServiceText.TextType type = AdditionalServiceText.TextType.DESCRIPTION;
        
        // case 1: found by locale
        AdditionalServiceText text = new AdditionalServiceText(10, serviceId, locale, type, "Value");
        doReturn(Optional.of(text)).when(additionalServiceTextRepository).findByLocaleAndType(serviceId, locale, type);
        
        AdditionalServiceText result = additionalServiceTextRepository.findBestMatchByLocaleAndType(serviceId, locale, type);
        assertEquals("Value", result.getValue());
        
        // case 2: not found by locale, but other exists
        doReturn(Optional.empty()).when(additionalServiceTextRepository).findByLocaleAndType(serviceId, locale, type);
        AdditionalServiceText text2 = new AdditionalServiceText(11, serviceId, "it", type, "Valore");
        doReturn(List.of(text2)).when(additionalServiceTextRepository).findAllByAdditionalServiceIdAndType(serviceId, type);
        
        result = additionalServiceTextRepository.findBestMatchByLocaleAndType(serviceId, locale, type);
        assertEquals("Valore", result.getValue());
        
        // case 3: nothing found
        doReturn(Collections.emptyList()).when(additionalServiceTextRepository).findAllByAdditionalServiceIdAndType(serviceId, type);
        result = additionalServiceTextRepository.findBestMatchByLocaleAndType(serviceId, locale, type);
        assertEquals("N/A", result.getValue());
    }
}
