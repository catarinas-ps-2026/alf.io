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
package alfio.controller.api.admin;

import alfio.manager.AccessService;
import alfio.manager.BillingDocumentManager;
import alfio.manager.EventManager;
import alfio.manager.payment.custom.offline.CustomOfflineConfigurationManager;
import alfio.manager.system.AdminJobExecutor;
import alfio.manager.system.AdminJobManager;
import alfio.manager.system.ConfigurationLevel;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.user.UserManager;
import alfio.model.Event;
import alfio.model.EventAndOrganizationId;
import alfio.model.modification.ConfigurationModification;
import alfio.model.system.Configuration;
import alfio.model.system.ConfigurationKeys;
import alfio.repository.EventRepository;
import alfio.util.ClockProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConfigurationApiControllerUnitTest {

    private ConfigurationManager configurationManager;
    private BillingDocumentManager billingDocumentManager;
    private AdminJobManager adminJobManager;
    private EventRepository eventRepository;
    private EventManager eventManager;
    private ClockProvider clockProvider;
    private UserManager userManager;
    private AccessService accessService;
    private CustomOfflineConfigurationManager customOfflineConfigurationManager;

    private ConfigurationApiController controller;
    private Principal principal;

    @BeforeEach
    void setUp() {
        configurationManager = mock(ConfigurationManager.class);
        billingDocumentManager = mock(BillingDocumentManager.class);
        adminJobManager = mock(AdminJobManager.class);
        eventRepository = mock(EventRepository.class);
        eventManager = mock(EventManager.class);
        clockProvider = mock(ClockProvider.class);
        userManager = mock(UserManager.class);
        accessService = mock(AccessService.class);
        customOfflineConfigurationManager = mock(CustomOfflineConfigurationManager.class);

        controller = new ConfigurationApiController(
            configurationManager,
            billingDocumentManager,
            adminJobManager,
            eventRepository,
            eventManager,
            clockProvider,
            userManager,
            accessService,
            customOfflineConfigurationManager
        );

        principal = mock(Principal.class);
        when(principal.getName()).thenReturn("admin");
    }

    @Test
    void loadConfiguration_checksAdminAndLoadsSystemConfiguration() {
        Map<ConfigurationKeys.SettingCategory, List<Configuration>> expected = Map.of();
        when(configurationManager.loadAllSystemConfigurationIncludingMissing("admin")).thenReturn(expected);

        Map<ConfigurationKeys.SettingCategory, List<Configuration>> result = controller.loadConfiguration(principal);

        verify(accessService).ensureAdmin(principal);
        verify(configurationManager).loadAllSystemConfigurationIncludingMissing("admin");
        assertEquals(expected, result);
    }

    @Test
    void isBasicConfigurationNeeded_returnsExpectedStatus() {
        when(configurationManager.isBasicConfigurationNeeded()).thenReturn(true);
        assertTrue(controller.isBasicConfigurationNeeded());

        when(configurationManager.isBasicConfigurationNeeded()).thenReturn(false);
        assertFalse(controller.isBasicConfigurationNeeded());
    }

    @Test
    void updateConfiguration_single_updatesSystemConfig() {
        ConfigurationModification modification = new ConfigurationModification();
        modification.setKey("BASE_URL");
        modification.setValue("https://example.com");

        boolean result = controller.updateConfiguration(modification, principal);

        assertTrue(result);
        verify(accessService).ensureAdmin(principal);
        verify(configurationManager).saveSystemConfiguration(ConfigurationKeys.BASE_URL, "https://example.com");
    }

    @Test
    void updateConfiguration_bulk_updatesSystemConfigs() {
        ConfigurationModification mod1 = new ConfigurationModification();
        mod1.setKey("BASE_URL");
        mod1.setValue("https://example.com");

        Map<ConfigurationKeys.SettingCategory, List<ConfigurationModification>> input = Map.of(
            ConfigurationKeys.SettingCategory.SYSTEM, List.of(mod1)
        );

        boolean result = controller.updateConfiguration(input, principal);

        assertTrue(result);
        verify(accessService).ensureAdmin(principal);
        verify(configurationManager).saveAllSystemConfiguration(List.of(mod1));
    }

    @Test
    void loadOrganizationConfiguration_checksOwnershipAndLoadsConfig() {
        int orgId = 42;
        Map<ConfigurationKeys.SettingCategory, List<Configuration>> expected = Map.of();
        when(configurationManager.loadOrganizationConfig(orgId, "admin")).thenReturn(expected);

        Map<ConfigurationKeys.SettingCategory, List<Configuration>> result = controller.loadOrganizationConfiguration(orgId, principal);

        verify(accessService).checkOrganizationOwnership(principal, orgId);
        verify(configurationManager).loadOrganizationConfig(orgId, "admin");
        assertEquals(expected, result);
    }

    @Test
    void updateOrganizationConfiguration_checksOwnershipAndSaves() {
        int orgId = 42;
        ConfigurationModification mod = new ConfigurationModification();
        mod.setKey("VAT_NR");
        mod.setValue("CHE-123");

        Map<ConfigurationKeys.SettingCategory, List<ConfigurationModification>> input = Map.of(
            ConfigurationKeys.SettingCategory.ORGANIZATION, List.of(mod)
        );

        boolean result = controller.updateOrganizationConfiguration(orgId, input, principal);

        assertTrue(result);
        verify(accessService).checkOrganizationOwnership(principal, orgId);
        verify(configurationManager).saveAllOrganizationConfiguration(orgId, List.of(mod), "admin");
    }

    @Test
    void loadEventConfiguration_checksOwnershipAndLoadsConfig() {
        int eventId = 7;
        Map<ConfigurationKeys.SettingCategory, List<Configuration>> expected = Map.of();
        when(configurationManager.loadEventConfig(eventId, "admin")).thenReturn(expected);

        Map<ConfigurationKeys.SettingCategory, List<Configuration>> result = controller.loadEventConfiguration(eventId, principal);

        verify(accessService).checkEventOwnership(principal, eventId);
        verify(configurationManager).loadEventConfig(eventId, "admin");
        assertEquals(expected, result);
    }

    @Test
    void getSingleConfigForEvent_eventNotFound_returnsNotFound() {
        String eventName = "test-event";
        String key = "SOME_KEY";
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.empty());

        ResponseEntity<String> response = controller.getSingleConfigForEvent(eventName, key, principal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accessService).checkEventOwnership(principal, eventName);
    }

    @Test
    void getSingleConfigForEvent_configNull_returnsNoContent() {
        String eventName = "test-event";
        String key = "SOME_KEY";
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(7);
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        when(configurationManager.getSingleConfigForEvent(7, key, "admin")).thenReturn(null);

        ResponseEntity<String> response = controller.getSingleConfigForEvent(eventName, key, principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void getSingleConfigForEvent_configExists_returnsValue() {
        String eventName = "test-event";
        String key = "SOME_KEY";
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(7);
        when(eventManager.getOptionalByName(eventName, "admin")).thenReturn(Optional.of(event));
        when(configurationManager.getSingleConfigForEvent(7, key, "admin")).thenReturn("value");

        ResponseEntity<String> response = controller.getSingleConfigForEvent(eventName, key, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("value", response.getBody());
    }

    @Test
    void getSingleConfigForOrganization_configNull_returnsNoContent() {
        int orgId = 42;
        String key = "SOME_KEY";
        when(configurationManager.getSingleConfigForOrganization(orgId, key, "admin")).thenReturn(null);

        ResponseEntity<String> response = controller.getSingleConfigForOrganization(orgId, key, principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accessService).checkOrganizationOwnership(principal, orgId);
    }

    @Test
    void getSingleConfigForOrganization_configExists_returnsValue() {
        int orgId = 42;
        String key = "SOME_KEY";
        when(configurationManager.getSingleConfigForOrganization(orgId, key, "admin")).thenReturn("value");

        ResponseEntity<String> response = controller.getSingleConfigForOrganization(orgId, key, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("value", response.getBody());
    }

    @Test
    void deleteOrganizationLevelKey_deletesAndReturnsTrue() {
        int orgId = 42;
        ConfigurationKeys key = ConfigurationKeys.VAT_NR;

        boolean result = controller.deleteOrganizationLevelKey(orgId, key, principal);

        assertTrue(result);
        verify(accessService).checkOrganizationOwnership(principal, orgId);
        verify(configurationManager).deleteOrganizationLevelByKey("VAT_NR", orgId, "admin");
    }

    @Test
    void deleteEventLevelKey_deletesAndReturnsTrue() {
        int eventId = 7;
        ConfigurationKeys key = ConfigurationKeys.VAT_NR;

        boolean result = controller.deleteEventLevelKey(eventId, key, principal);

        assertTrue(result);
        verify(accessService).checkEventOwnership(principal, eventId);
        verify(configurationManager).deleteEventLevelByKey("VAT_NR", eventId, "admin");
    }

    @Test
    void deleteCategoryLevelKey_deletesAndReturnsTrue() {
        int eventId = 7;
        int categoryId = 3;
        ConfigurationKeys key = ConfigurationKeys.VAT_NR;

        boolean result = controller.deleteCategoryLevelKey(eventId, categoryId, key, principal);

        assertTrue(result);
        verify(accessService).checkCategoryOwnership(principal, eventId, categoryId);
        verify(configurationManager).deleteCategoryLevelByKey("VAT_NR", eventId, categoryId, "admin");
    }

    @Test
    void deleteKey_deletesAndReturnsTrue() {
        String key = "SOME_KEY";

        boolean result = controller.deleteKey(key, principal);

        assertTrue(result);
        verify(accessService).ensureAdmin(principal);
        verify(configurationManager).deleteKey(key);
    }

    @Test
    void loadEUCountries_returnsLocalizedCountries() {
        ConfigurationManager.MaybeConfiguration config = mock(ConfigurationManager.MaybeConfiguration.class);
        when(config.getRequiredValue()).thenReturn("IT,FR,DE");
        when(configurationManager.getForSystem(ConfigurationKeys.EU_COUNTRIES_LIST)).thenReturn(config);

        List<Pair<String, String>> countries = controller.loadEUCountries();

        assertNotNull(countries);
        assertFalse(countries.isEmpty());
    }

    @Test
    void loadInstanceSettings_returnsInstanceSettings() {
        ConfigurationManager.MaybeConfiguration maxLengthConfig = mock(ConfigurationManager.MaybeConfiguration.class);
        when(maxLengthConfig.getValueAsIntOrDefault(4096)).thenReturn(2000);

        ConfigurationManager.MaybeConfiguration baseUrlConfig = mock(ConfigurationManager.MaybeConfiguration.class);
        when(baseUrlConfig.getRequiredValue()).thenReturn("http://localhost");

        Map<ConfigurationKeys, ConfigurationManager.MaybeConfiguration> settings = Map.of(
            ConfigurationKeys.DESCRIPTION_MAXLENGTH, maxLengthConfig,
            ConfigurationKeys.BASE_URL, baseUrlConfig
        );
        when(configurationManager.getFor(EnumSet.of(ConfigurationKeys.DESCRIPTION_MAXLENGTH, ConfigurationKeys.BASE_URL), ConfigurationLevel.system()))
            .thenReturn(settings);

        ConfigurationApiController.InstanceSettings result = controller.loadInstanceSettings();

        assertEquals(2000, result.getDescriptionMaxLength());
        assertEquals("http://localhost", result.getBaseUrl());
    }

    @Test
    void loadPlatformModeStatus_notConnected_returnsPlatformStatus() {
        int orgId = 42;
        ConfigurationManager.MaybeConfiguration platformModeConfig = mock(ConfigurationManager.MaybeConfiguration.class);
        when(platformModeConfig.getValueAsBooleanOrDefault()).thenReturn(true);
        when(configurationManager.getForSystem(ConfigurationKeys.PLATFORM_MODE_ENABLED)).thenReturn(platformModeConfig);

        ConfigurationManager.MaybeConfiguration stripeConfig = mock(ConfigurationManager.MaybeConfiguration.class);
        when(stripeConfig.isPresent()).thenReturn(false);

        ConfigurationManager.MaybeConfiguration mollieConfig = mock(ConfigurationManager.MaybeConfiguration.class);
        when(mollieConfig.isPresent()).thenReturn(false);

        Map<ConfigurationKeys, ConfigurationManager.MaybeConfiguration> options = Map.of(
            ConfigurationKeys.STRIPE_CONNECTED_ID, stripeConfig,
            ConfigurationKeys.MOLLIE_CONNECT_REFRESH_TOKEN, mollieConfig
        );
        when(configurationManager.getFor(List.of(ConfigurationKeys.STRIPE_CONNECTED_ID, ConfigurationKeys.MOLLIE_CONNECT_REFRESH_TOKEN), ConfigurationLevel.organization(orgId)))
            .thenReturn(options);

        Map<String, Boolean> result = controller.loadPlatformModeStatus(orgId, principal);

        assertTrue(result.get("enabled"));
        assertFalse(result.get("stripeConnected"));
        assertFalse(result.get("mollieConnected"));
    }

    @Test
    void getSettingCategories_returnsAllCategories() {
        Collection<ConfigurationKeys.SettingCategory> categories = controller.getSettingCategories();
        assertEquals(EnumSet.allOf(ConfigurationKeys.SettingCategory.class), categories);
    }

    @Test
    void getFirstInvoiceDate_returnsDate() {
        int eventId = 7;
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getSingleEventById(eventId, "admin")).thenReturn(event);

        ZonedDateTime expectedTime = ZonedDateTime.now(ZoneId.of("Europe/Rome"));
        when(billingDocumentManager.findFirstInvoiceDate(eventId)).thenReturn(Optional.of(expectedTime));

        ResponseEntity<ZonedDateTime> response = controller.getFirstInvoiceDate(eventId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTime, response.getBody());
    }

    @Test
    void getMatchingInvoicesForEvent_invalidRange_returnsBadRequest() {
        int eventId = 7;
        Event event = mock(Event.class);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getSingleEventById(eventId, "admin")).thenReturn(event);

        // from > to
        ResponseEntity<List<Integer>> response = controller.getMatchingInvoicesForEvent(eventId, 2000L, 1000L, principal);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getMatchingInvoicesForEvent_validRange_returnsList() {
        int eventId = 7;
        Event event = mock(Event.class);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Rome"));
        when(eventManager.getSingleEventById(eventId, "admin")).thenReturn(event);
        when(billingDocumentManager.findMatchingInvoiceIds(anyInt(), any(), any())).thenReturn(List.of(1, 2));

        ResponseEntity<List<Integer>> response = controller.getMatchingInvoicesForEvent(eventId, 1000L, 2000L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(1, 2), response.getBody());
    }

    @Test
    void regenerateInvoices_emptyIds_returnsBadRequest() {
        int eventId = 7;
        ResponseEntity<Boolean> response = controller.regenerateInvoices(eventId, List.of(), principal);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void regenerateInvoices_validIds_schedulesJob() {
        int eventId = 7;
        when(eventManager.eventExistsById(eventId)).thenReturn(true);
        when(adminJobManager.scheduleExecution(any(), any())).thenReturn(true);

        ResponseEntity<Boolean> response = controller.regenerateInvoices(eventId, List.of(1L, 2L), principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    void generateTicketsForSubscriptions_nonOwner_returnsUnauthorized() {
        int orgId = 42;
        when(userManager.isOwnerOfOrganization("admin", orgId)).thenReturn(false);

        ResponseEntity<Boolean> response = controller.generateTicketsForSubscriptions(null, orgId, principal);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void generateTicketsForSubscriptions_owner_schedulesJob() {
        int orgId = 42;
        when(userManager.isOwnerOfOrganization("admin", orgId)).thenReturn(true);
        when(adminJobManager.scheduleExecution(eq(AdminJobExecutor.JobName.ASSIGN_TICKETS_TO_SUBSCRIBERS), any())).thenReturn(true);

        ResponseEntity<Boolean> response = controller.generateTicketsForSubscriptions(null, orgId, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    void loadTranslations_returnsTranslations() {
        ConfigurationManager.MaybeConfiguration config = mock(ConfigurationManager.MaybeConfiguration.class);
        when(config.getValue()).thenReturn(Optional.of("{\"en\": {\"key\": \"value\"}}"));
        when(configurationManager.getFor(ConfigurationKeys.TRANSLATION_OVERRIDE, ConfigurationLevel.system())).thenReturn(config);

        Map<String, Map<String, String>> result = controller.loadTranslations();

        assertEquals(Map.of("en", Map.of("key", "value")), result);
    }
}
