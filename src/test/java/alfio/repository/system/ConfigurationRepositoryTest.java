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

package alfio.repository.system;

import alfio.model.system.ConfigurationKeys;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ConfigurationRepositoryTest {

    private final ConfigurationRepository configurationRepository = mock(ConfigurationRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testGetAllCategoriesAndValueWith() {
        ConfigurationRepository.CategoryAndValue cv1 = new ConfigurationRepository.CategoryAndValue(1, "val1");
        ConfigurationRepository.CategoryAndValue cv2 = new ConfigurationRepository.CategoryAndValue(2, "val2");
        
        when(configurationRepository.findAllCategoriesAndValueWith(1, 1, ConfigurationKeys.BASE_URL.name()))
                .thenReturn(List.of(cv1, cv2));
        
        Map<Integer, String> result = configurationRepository.getAllCategoriesAndValueWith(1, 1, ConfigurationKeys.BASE_URL);
        
        assertEquals(2, result.size());
        assertEquals("val1", result.get(1));
        assertEquals("val2", result.get(2));
    }

    @Test
    void testGetAllCategoriesAndValueWithEmpty() {
        when(configurationRepository.findAllCategoriesAndValueWith(1, 1, ConfigurationKeys.BASE_URL.name()))
                .thenReturn(List.of());
        
        Map<Integer, String> result = configurationRepository.getAllCategoriesAndValueWith(1, 1, ConfigurationKeys.BASE_URL);
        
        assertTrue(result.isEmpty());
    }
}
