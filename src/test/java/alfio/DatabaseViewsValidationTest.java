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
package alfio;

import static org.junit.jupiter.api.Assertions.*;

import alfio.config.DataSourceConfiguration;
import alfio.config.Initializer;
import alfio.test.util.AlfioIntegrationTest;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class})
@ActiveProfiles({Initializer.PROFILE_DEV, Initializer.PROFILE_DISABLE_JOBS, Initializer.PROFILE_INTEGRATION_TEST})
class DatabaseViewsValidationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private static final Set<String> EXPECTED_VIEWS = Set.of(
            "events_statistics",
            "ticket_category_statistics",
            "reservation_and_ticket_and_tx",
            "checkin_ticket_event_and_category_info",
            "reservation_with_purchase_context",
            "admin_reservation_request_stats",
            "auditing_user",
            "latest_ticket_update",
            "ticket_category_with_currency",
            "additional_service_with_currency",
            "reservation_and_subscription_and_tx",
            "subscription_descriptor_statistics",
            "basic_event_with_optional_subscription",
            "extension_capabilities",
            "available_subscriptions_by_event",
            "promocode_usage_details",
            "promocode_count",
            "promocode_count_all",
            "additional_item_field_value_with_ticket_id",
            "field_value_w_additional",
            "all_ticket_field_values");

    @Test
    void allExpectedViewsExist() {
        var views = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'VIEW'",
                Map.of());
        var viewNames = views.stream().map(m -> (String) m.get("table_name")).collect(Collectors.toSet());

        var missingViews =
                EXPECTED_VIEWS.stream().filter(v -> !viewNames.contains(v)).collect(Collectors.toList());
        assertTrue(missingViews.isEmpty(), "Missing views: " + missingViews);
    }

    @Test
    void statisticsViewsAreQueryable() {
        // events_statistics should be queryable even with no data
        var result = jdbc.queryForList("SELECT count(*) as cnt FROM events_statistics", Map.of());
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
