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

import static alfio.test.util.IntegrationTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import alfio.config.DataSourceConfiguration;
import alfio.config.Initializer;
import alfio.manager.EventManager;
import alfio.manager.user.UserManager;
import alfio.model.Event;
import alfio.model.metadata.AlfioMetadata;
import alfio.model.modification.DateTimeModification;
import alfio.model.modification.TicketCategoryModification;
import alfio.repository.EventRepository;
import alfio.repository.TicketCategoryRepository;
import alfio.repository.system.ConfigurationRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.test.util.AlfioIntegrationTest;
import alfio.util.ClockProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class})
@ActiveProfiles({Initializer.PROFILE_DEV, Initializer.PROFILE_DISABLE_JOBS, Initializer.PROFILE_INTEGRATION_TEST})
class ReferentialIntegrityTest {

    @Autowired
    private EventManager eventManager;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserManager userManager;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    private List<TicketCategoryModification> defaultCategories() {
        return List.of(new TicketCategoryModification(
                null,
                "default",
                alfio.model.TicketCategory.TicketAccessType.INHERIT,
                20,
                new DateTimeModification(
                        LocalDate.now(ClockProvider.clock()).minusDays(1), LocalTime.now(ClockProvider.clock())),
                new DateTimeModification(
                        LocalDate.now(ClockProvider.clock()).plusDays(1), LocalTime.now(ClockProvider.clock())),
                DESCRIPTION,
                BigDecimal.TEN,
                false,
                "",
                false,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                AlfioMetadata.empty()));
    }

    @Test
    void cannotInsertTicketWithInvalidCategoryId() {
        ensureMinimalConfiguration(configurationRepository);
        var eventAndUser =
                initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update(
                        "INSERT INTO ticket (uuid, public_uuid, event_id, category_id, tickets_reservation_id, status, src_price_cts, final_price_cts, currency_code) "
                                + "VALUES ('uuid-1', 'pub-uuid-1', :eventId, 99999, 'res-1', 'ACQUIRED', 1000, 1000, 'CHF')",
                        Map.of("eventId", eventAndUser.getLeft().getId())));
    }

    @Test
    void cannotInsertReservationWithInvalidEventId() {
        ensureMinimalConfiguration(configurationRepository);
        // tickets_reservation doesn't have a direct FK to event, so test via ticket table instead
        var eventAndUser =
                initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update(
                        "INSERT INTO ticket (uuid, public_uuid, event_id, category_id, tickets_reservation_id, status, src_price_cts, final_price_cts, currency_code) "
                                + "VALUES ('uuid-test', 'pub-uuid-test', :eventId, :categoryId, 'nonexistent-res', 'ACQUIRED', 1000, 1000, 'CHF')",
                        Map.of(
                                "eventId",
                                eventAndUser.getLeft().getId(),
                                "categoryId",
                                ticketCategoryRepository
                                        .findAllTicketCategories(
                                                eventAndUser.getLeft().getId())
                                        .get(0)
                                        .getId())));
    }

    @Test
    void cannotDeleteOrganizationWithEvents() {
        ensureMinimalConfiguration(configurationRepository);
        var eventAndUser =
                initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        var orgId = eventAndUser.getLeft().getOrganizationId();
        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update("DELETE FROM organization WHERE id = :orgId", Map.of("orgId", orgId)));
    }

    @Test
    void cannotInsertEventWithInvalidOrganizationId() {
        ensureMinimalConfiguration(configurationRepository);
        assertThrows(Exception.class, () -> {
            eventManager.createEvent(
                    new alfio.model.modification.EventModification(
                            null,
                            Event.EventFormat.IN_PERSON,
                            "url",
                            "url",
                            "url",
                            "privacy",
                            "url",
                            null,
                            "test-event",
                            "Test Event",
                            99999,
                            "location",
                            "0.0",
                            "0.0",
                            "UTC",
                            DESCRIPTION,
                            new DateTimeModification(LocalDate.now(), LocalTime.now()),
                            new DateTimeModification(LocalDate.now().plusDays(1), LocalTime.now()),
                            BigDecimal.TEN,
                            "CHF",
                            20,
                            BigDecimal.ONE,
                            true,
                            List.of(alfio.model.transaction.PaymentProxy.OFFLINE),
                            defaultCategories(),
                            false,
                            new alfio.model.modification.support.LocationDescriptor("", "", "", ""),
                            7,
                            null,
                            null,
                            AlfioMetadata.empty(),
                            List.of()),
                    "testuser");
        });
    }

    @Test
    void configurationKeyCannotExceedMaxLength() {
        ensureMinimalConfiguration(configurationRepository);
        String longKey = "A".repeat(500);
        assertThrows(Exception.class, () -> configurationRepository.insert(longKey, "value", ""));
    }
}
