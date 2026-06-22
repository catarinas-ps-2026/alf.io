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
package alfio.manager.system;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.manager.user.UserManager;
import alfio.model.EventAndOrganizationId;
import alfio.model.system.Configuration.ConfigurationPathKey;
import alfio.model.system.Configuration.EventConfigurationPath;
import alfio.model.system.Configuration.OrganizationConfigurationPath;
import alfio.model.system.Configuration.SystemConfigurationPath;
import alfio.model.system.ConfigurationKeyValuePathLevel;
import alfio.model.system.ConfigurationKeys;
import alfio.model.user.User;
import alfio.repository.EventRepository;
import alfio.repository.system.ConfigurationRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

class ConfigurationManagerUnitTest {

    private ConfigurationManager configurationManager;

    private ConfigurationRepository configurationRepository;
    private UserManager userManager;
    private EventRepository eventRepository;
    private ExternalConfiguration externalConfiguration;
    private Environment environment;
    private Cache<Set<ConfigurationKeys>, Map<ConfigurationKeys, MaybeConfiguration>> oneMinuteCache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        configurationRepository = mock(ConfigurationRepository.class);
        userManager = mock(UserManager.class);
        eventRepository = mock(EventRepository.class);
        externalConfiguration = mock(ExternalConfiguration.class);
        environment = mock(Environment.class);
        oneMinuteCache = mock(Cache.class);

        configurationManager = new ConfigurationManager(
                configurationRepository,
                userManager,
                eventRepository,
                externalConfiguration,
                environment,
                oneMinuteCache);
    }

    @Test
    void testSaveSystemConfiguration() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;
        String value = "http://localhost:8080";

        configurationManager.saveSystemConfiguration(key, value);

        verify(configurationRepository).insert(key.getValue(), value, "Base application url");
    }

    @Test
    void testSaveConfig() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;
        String value = "http://localhost:8080";

        // System
        ConfigurationPathKey systemPathKey = new ConfigurationPathKey(new SystemConfigurationPath(), key);
        configurationManager.saveConfig(systemPathKey, value);
        verify(configurationRepository, times(1)).insert(key.getValue(), value, "Base application url");

        // Organization
        ConfigurationPathKey orgPathKey = new ConfigurationPathKey(new OrganizationConfigurationPath(1), key);
        configurationManager.saveConfig(orgPathKey, value);
        verify(configurationRepository).insertOrganizationLevel(1, key.getValue(), value, "Base application url");

        // Event
        ConfigurationPathKey eventPathKey = new ConfigurationPathKey(new EventConfigurationPath(2, 1), key);
        configurationManager.saveConfig(eventPathKey, value);
        verify(configurationRepository).insertEventLevel(2, 1, key.getValue(), value, "Base application url");
    }

    @Test
    void testDeleteKey() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;

        configurationManager.deleteKey(key.getValue());

        verify(configurationRepository).deleteByKey(key.getValue());
    }

    @Test
    void testDeleteOrganizationLevelByKey() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;
        String username = "admin";
        int orgId = 1;

        User user = mock(User.class);
        when(userManager.findUserByUsername(username)).thenReturn(user);
        when(userManager.isOwnerOfOrganization(user, orgId)).thenReturn(true);

        configurationManager.deleteOrganizationLevelByKey(key.getValue(), orgId, username);

        verify(configurationRepository).deleteOrganizationLevelByKey(key.getValue(), orgId);
    }

    @Test
    void testDeleteEventLevelByKey() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;
        String username = "admin";
        int eventId = 2;
        int orgId = 1;

        User user = mock(User.class);
        when(userManager.findUserByUsername(username)).thenReturn(user);
        when(userManager.isOwnerOfOrganization(user, orgId)).thenReturn(true);

        EventAndOrganizationId eventAndOrganizationId = mock(EventAndOrganizationId.class);
        when(eventAndOrganizationId.getOrganizationId()).thenReturn(orgId);
        when(eventRepository.findEventAndOrganizationIdById(eventId)).thenReturn(eventAndOrganizationId);

        configurationManager.deleteEventLevelByKey(key.getValue(), eventId, username);

        verify(configurationRepository).deleteEventLevelByKey(key.getValue(), eventId);
    }

    @Test
    void testDeleteCategoryLevelByKey() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;
        String username = "admin";
        int eventId = 2;
        int categoryId = 3;
        int orgId = 1;

        User user = mock(User.class);
        when(userManager.findUserByUsername(username)).thenReturn(user);
        when(userManager.isOwnerOfOrganization(user, orgId)).thenReturn(true);

        EventAndOrganizationId eventAndOrganizationId = mock(EventAndOrganizationId.class);
        when(eventAndOrganizationId.getOrganizationId()).thenReturn(orgId);
        when(eventRepository.findEventAndOrganizationIdById(eventId)).thenReturn(eventAndOrganizationId);

        configurationManager.deleteCategoryLevelByKey(key.getValue(), eventId, categoryId, username);

        verify(configurationRepository).deleteCategoryLevelByKey(key.getValue(), eventId, categoryId);
    }

    @Test
    void testGetForSystem() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;
        String value = "http://localhost:8080";
        ConfigurationKeyValuePathLevel config = mock(ConfigurationKeyValuePathLevel.class);
        when(config.getValue()).thenReturn(value);
        when(config.getConfigurationKey()).thenReturn(key);
        when(configurationRepository.findByKeysAtSystemLevel(anyCollection()))
                .thenReturn(Collections.singletonList(config));

        MaybeConfiguration result = configurationManager.getForSystem(key);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(value, result.getValueOrNull());
    }

    @Test
    void testGetForOrganization() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;
        String value = "http://localhost:8080";
        int orgId = 1;
        ConfigurationKeyValuePathLevel config = mock(ConfigurationKeyValuePathLevel.class);
        when(config.getValue()).thenReturn(value);
        when(config.getConfigurationKey()).thenReturn(key);
        when(configurationRepository.findByOrganizationAndKeys(eq(orgId), anyCollection()))
                .thenReturn(Collections.singletonList(config));

        MaybeConfiguration result = configurationManager.getFor(key, ConfigurationLevel.organization(orgId));

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(value, result.getValueOrNull());
    }

    @Test
    void testMaybeConfigurationDefaults() {
        ConfigurationKeys key = ConfigurationKeys.HTTPS_FORCE_REDIRECT;

        MaybeConfiguration maybe = new MaybeConfiguration(key);
        Assertions.assertFalse(maybe.isPresent());
        Assertions.assertFalse(maybe.getValueAsBooleanOrDefault()); // default is false
    }

    @Test
    void testGetRequiredValue() {
        ConfigurationKeys key = ConfigurationKeys.BASE_URL;
        MaybeConfiguration maybe = new MaybeConfiguration(key);
        assertThrows(IllegalArgumentException.class, maybe::getRequiredValue);
    }
}
