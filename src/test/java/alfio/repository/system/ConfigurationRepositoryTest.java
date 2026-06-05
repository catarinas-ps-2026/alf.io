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
