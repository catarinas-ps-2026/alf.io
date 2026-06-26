package alfio.controller.api.v2.user;

import static alfio.test.util.IntegrationTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import alfio.TestConfiguration;
import alfio.config.DataSourceConfiguration;
import alfio.config.Initializer;
import alfio.controller.api.ControllerConfiguration;
import alfio.manager.EventManager;
import alfio.manager.user.UserManager;
import alfio.model.*;
import alfio.model.metadata.AlfioMetadata;
import alfio.model.modification.DateTimeModification;
import alfio.model.modification.TicketCategoryModification;
import alfio.model.transaction.PaymentProxy;
import alfio.model.transaction.StaticPaymentMethods;
import alfio.controller.form.SearchOptions;
import alfio.repository.*;
import alfio.repository.system.ConfigurationRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.test.util.AlfioIntegrationTest;
import alfio.util.ClockProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class, ControllerConfiguration.class})
@ActiveProfiles({Initializer.PROFILE_DEV, Initializer.PROFILE_DISABLE_JOBS, Initializer.PROFILE_INTEGRATION_TEST})
class EventApiV2ControllerIntegrationTest {

    @Autowired private EventApiV2Controller eventApiV2Controller;
    @Autowired private EventManager eventManager;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserManager userManager;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private TicketCategoryRepository ticketCategoryRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private TicketReservationRepository ticketReservationRepository;
    @Autowired private alfio.manager.TicketReservationManager ticketReservationManager;
    @Autowired private ConfigurationRepository configurationRepository;

    @BeforeEach
    void setup() {
        ensureMinimalConfiguration(configurationRepository);
    }

    private List<TicketCategoryModification> defaultCategories() {
        return List.of(new TicketCategoryModification(
                null, "default", TicketCategory.TicketAccessType.INHERIT, AVAILABLE_SEATS,
                new DateTimeModification(LocalDate.now(ClockProvider.clock()).minusDays(1), LocalTime.now(ClockProvider.clock())),
                new DateTimeModification(LocalDate.now(ClockProvider.clock()).plusDays(1), LocalTime.now(ClockProvider.clock())),
                DESCRIPTION, BigDecimal.TEN, false, "", false, null, null, null, null, null, 0, null, null, AlfioMetadata.empty()));
    }

    @Test
    void listEventsReturnsEmptyWhenNoEvents() {
        var result = eventApiV2Controller.listEvents(SearchOptions.empty());
        assertNotNull(result.getBody());
    }

    @Test
    void listEventsReturnsPublishedEvents() {
        var eventAndUser = initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        var event = eventAndUser.getLeft();
        eventManager.toggleActiveFlag(event.getId(), eventAndUser.getRight(), true);
        var result = eventApiV2Controller.listEvents(SearchOptions.empty());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().stream().anyMatch(e -> e.getShortName().equals(event.getShortName())));
    }

    @Test
    void getEventReturnsPublishedEvent() {
        var eventAndUser = initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        var event = eventAndUser.getLeft();
        eventManager.toggleActiveFlag(event.getId(), eventAndUser.getRight(), true);
        var result = eventApiV2Controller.getEvent(event.getShortName(), new MockHttpSession());
        assertNotNull(result.getBody());
        assertEquals(event.getShortName(), result.getBody().getShortName());
    }

    @Test
    void getEventReturns404ForNonexistent() {
        var result = eventApiV2Controller.getEvent("nonexistent-event-xyz", new MockHttpSession());
        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    void getTicketCategoriesReturnsEventCategories() {
        var eventAndUser = initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        var event = eventAndUser.getLeft();
        eventManager.toggleActiveFlag(event.getId(), eventAndUser.getRight(), true);
        var result = eventApiV2Controller.getTicketCategories(event.getShortName(), null);
        assertNotNull(result.getBody());
        assertFalse(result.getBody().ticketCategories().isEmpty());
    }

    @Test
    void getTicketCategoriesReturns404ForNonexistent() {
        var result = eventApiV2Controller.getTicketCategories("nonexistent-event-xyz", null);
        assertEquals(404, result.getStatusCode().value());
    }

    @Test
    void getEventCategoryCount() {
        var eventAndUser = initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        var event = eventAndUser.getLeft();
        var categories = ticketCategoryRepository.findAllTicketCategories(event.getId());
        assertEquals(1, categories.size());
        assertEquals("default", categories.get(0).getName());
    }

    @Test
    void eventHasCorrectDateFormat() {
        var eventAndUser = initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        var event = eventAndUser.getLeft();
        assertNotNull(event.getBegin());
        assertNotNull(event.getEnd());
        assertTrue(event.getBegin().isBefore(event.getEnd()));
    }

    @Test
    void eventBelongsToOrganization() {
        var eventAndUser = initEvent(defaultCategories(), organizationRepository, userManager, eventManager, eventRepository);
        var event = eventAndUser.getLeft();
        assertTrue(event.getOrganizationId() > 0);
    }
}
