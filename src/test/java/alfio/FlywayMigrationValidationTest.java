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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class})
@ActiveProfiles({Initializer.PROFILE_DEV, Initializer.PROFILE_DISABLE_JOBS, Initializer.PROFILE_INTEGRATION_TEST})
class FlywayMigrationValidationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Test
    void allMigrationsAppliedSuccessfully() {
        // If we reach this point, Flyway has run all migrations on startup without error.
        // Verify the flyway_schema_history table exists and has no failed migrations.
        var tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_name IN ('flyway_schema_history', 'schema_version')",
                Map.of());
        assertFalse(tables.isEmpty(), "Flyway schema history table not found");
    }

    @Test
    void schemaHistoryIsConsistent() {
        // Determine which table name is used
        var tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_name IN ('flyway_schema_history', 'schema_version')",
                Map.of());
        String tableName = (String) tables.get(0).get("table_name");

        var failed = jdbc.queryForList(
                "SELECT installed_rank, version, description, type, success FROM " + tableName
                        + " WHERE success = false",
                Map.of());
        assertTrue(failed.isEmpty(), "Failed migrations found: " + failed);

        var pending = jdbc.queryForList("SELECT count(*) as cnt FROM " + tableName + " WHERE success = true", Map.of());
        long appliedCount = ((Number) pending.get(0).get("cnt")).longValue();
        assertTrue(appliedCount > 0, "No migrations have been applied");
    }
}
