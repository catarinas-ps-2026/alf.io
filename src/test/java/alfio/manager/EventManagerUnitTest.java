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

import alfio.manager.system.ConfigurationManager;
import alfio.manager.user.UserManager;
import alfio.model.Event;
import alfio.model.EventAndOrganizationId;
import alfio.model.Ticket;
import alfio.model.TicketCategory;
import alfio.model.modification.DateTimeModification;
import alfio.model.modification.EventModification;
import alfio.model.modification.TicketCategoryModification;
import alfio.model.user.Organization;
import alfio.repository.*;
import alfio.repository.system.ConfigurationRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.util.ClockProvider;
import ch.digitalfondue.npjt.AffectedRowCountAndKey;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventManagerUnitTest {

    private EventManager eventManager;

    private UserManager userManager;
    private EventRepository eventRepository;
    private EventDescriptionRepository eventDescriptionRepository;
    private TicketCategoryRepository ticketCategoryRepository;
    private TicketCategoryDescriptionRepository ticketCategoryDescriptionRepository;
    private TicketRepository ticketRepository;
    private SpecialPriceRepository specialPriceRepository;
    private PromoCodeDiscountRepository promoCodeRepository;
    private ConfigurationManager configurationManager;
    private EventDeleterRepository eventDeleterRepository;
    private PurchaseContextFieldManager purchaseContextFieldManager;
    private Flyway flyway;
    private Environment environment;
    private OrganizationRepository organizationRepository;
    private AuditingRepository auditingRepository;
    private ExtensionManager extensionManager;
    private GroupRepository groupRepository;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private ConfigurationRepository configurationRepository;
    private PaymentManager paymentManager;
    private ClockProvider clockProvider;
    private SubscriptionRepository subscriptionRepository;
    private AdditionalServiceManager additionalServiceManager;

    @BeforeEach
    void setUp() {
        userManager = mock(UserManager.class);
        eventRepository = mock(EventRepository.class);
        eventDescriptionRepository = mock(EventDescriptionRepository.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        ticketCategoryDescriptionRepository = mock(TicketCategoryDescriptionRepository.class);
        ticketRepository = mock(TicketRepository.class);
        specialPriceRepository = mock(SpecialPriceRepository.class);
        promoCodeRepository = mock(PromoCodeDiscountRepository.class);
        configurationManager = mock(ConfigurationManager.class);
        eventDeleterRepository = mock(EventDeleterRepository.class);
        purchaseContextFieldManager = mock(PurchaseContextFieldManager.class);
        flyway = mock(Flyway.class);
        environment = mock(Environment.class);
        organizationRepository = mock(OrganizationRepository.class);
        auditingRepository = mock(AuditingRepository.class);
        extensionManager = mock(ExtensionManager.class);
        groupRepository = mock(GroupRepository.class);
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        configurationRepository = mock(ConfigurationRepository.class);
        paymentManager = mock(PaymentManager.class);
        clockProvider = mock(ClockProvider.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        additionalServiceManager = mock(AdditionalServiceManager.class);

        eventManager = new EventManager(
            userManager, eventRepository, eventDescriptionRepository,
            ticketCategoryRepository, ticketCategoryDescriptionRepository,
            ticketRepository, specialPriceRepository, promoCodeRepository,
            configurationManager, eventDeleterRepository, purchaseContextFieldManager,
            flyway, environment, organizationRepository, auditingRepository,
            extensionManager, groupRepository, jdbcTemplate, configurationRepository,
            paymentManager, clockProvider, subscriptionRepository, additionalServiceManager
        );
    }

    @Test
    void testToggleActiveFlag() {
        int eventId = 1;
        String username = "admin";
        Event event = mock(Event.class);
        when(event.getOrganizationId()).thenReturn(10);
        when(eventRepository.findById(eventId)).thenReturn(event);
        when(organizationRepository.findOrganizationForUser(username, 10)).thenReturn(Optional.of(mock(Organization.class)));
        when(environment.acceptsProfiles(any(org.springframework.core.env.Profiles.class))).thenReturn(false);

        eventManager.toggleActiveFlag(eventId, username, true);

        verify(eventRepository).updateEventStatus(eventId, Event.Status.PUBLIC);
        verify(extensionManager).handleEventStatusChange(event, Event.Status.PUBLIC);
    }

    @Test
    void testDeleteEvent() {
        int eventId = 1;
        String username = "owner";
        Event event = mock(Event.class);
        when(event.getOrganizationId()).thenReturn(10);
        when(eventRepository.findById(eventId)).thenReturn(event);
        when(organizationRepository.findOrganizationForUser(username, 10)).thenReturn(Optional.of(mock(Organization.class)));

        eventManager.deleteEvent(eventId, username);

        verify(eventDeleterRepository).deleteAllForEvent(eventId);
    }

    @Test
    void testEventExistsById() {
        when(eventRepository.existsById(1)).thenReturn(true);
        when(eventRepository.existsById(2)).thenReturn(false);

        assertTrue(eventManager.eventExistsById(1));
        assertFalse(eventManager.eventExistsById(2));
    }

    @Test
    void testToggleTicketLocking() {
        String eventName = "test-event";
        int categoryId = 10;
        int ticketId = 100;
        String username = "admin";

        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(event.getId()).thenReturn(1);
        when(event.getOrganizationId()).thenReturn(2);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        when(organizationRepository.findOrganizationForUser(username, 2)).thenReturn(Optional.of(mock(Organization.class)));

        TicketCategory category = mock(TicketCategory.class);
        when(category.getId()).thenReturn(categoryId);
        when(ticketCategoryRepository.findAllTicketCategories(1)).thenReturn(Collections.singletonList(category));

        Ticket ticket = mock(Ticket.class);
        when(ticket.getLockedAssignment()).thenReturn(false);
        when(ticketRepository.findById(ticketId, categoryId)).thenReturn(ticket);
        when(ticketRepository.toggleTicketLocking(ticketId, categoryId, true)).thenReturn(1);

        assertTrue(eventManager.toggleTicketLocking(eventName, categoryId, ticketId, username));
        verify(ticketRepository).toggleTicketLocking(ticketId, categoryId, true);
    }

    @Test
    void testCreateEvent() {
        EventModification em = mock(EventModification.class);
        when(em.getId()).thenReturn(null);
        when(em.getOrganizationId()).thenReturn(1);
        when(em.getShortName()).thenReturn("short-name");
        when(em.getAvailableSeats()).thenReturn(100);
        when(em.getBegin()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now()));
        when(em.getEnd()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().plusHours(1)));
        when(em.getZoneId()).thenReturn("UTC");
        
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(1);
        when(organizationRepository.findAllForUser("user")).thenReturn(Collections.singletonList(org));
        
        MigrationInfoService migrationInfoService = mock(MigrationInfoService.class);
        MigrationInfo migrationInfo = mock(MigrationInfo.class);
        when(migrationInfo.getVersion()).thenReturn(MigrationVersion.fromVersion("1.0"));
        when(migrationInfoService.current()).thenReturn(migrationInfo);
        when(flyway.info()).thenReturn(migrationInfoService);
        
        AffectedRowCountAndKey<Integer> arcak = mock(AffectedRowCountAndKey.class);
        when(arcak.getKey()).thenReturn(123);
        when(eventRepository.insert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyBoolean(), any(), any(), any(), anyInt(), anyInt(), any(), anyInt(), any(), any(), any())).thenReturn(arcak);
        
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(123);
        when(event.getTimeZone()).thenReturn("UTC");
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now());
        when(eventRepository.findById(123)).thenReturn(event);

        eventManager.createEvent(em, "user");
        
        verify(eventRepository).insert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyBoolean(), any(), any(), any(), anyInt(), anyInt(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void testUpdateEventHeader() {
        Event original = mock(Event.class);
        when(original.getId()).thenReturn(1);
        when(original.getOrganizationId()).thenReturn(10);
        when(original.getFormat()).thenReturn(Event.EventFormat.IN_PERSON);
        ZonedDateTime now = ZonedDateTime.now();
        when(original.getBegin()).thenReturn(now);
        when(original.getEnd()).thenReturn(now.plusHours(1));

        EventModification em = mock(EventModification.class);
        when(em.getOrganizationId()).thenReturn(10);
        when(em.getDisplayName()).thenReturn("New Name");
        when(em.getBegin()).thenReturn(DateTimeModification.fromZonedDateTime(now));
        when(em.getEnd()).thenReturn(DateTimeModification.fromZonedDateTime(now.plusHours(1)));
        when(em.getZoneId()).thenReturn("UTC");
        when(em.getFormat()).thenReturn(Event.EventFormat.IN_PERSON);
        when(em.getShortName()).thenReturn("short-name");
        when(em.getGeolocation()).thenReturn(null);

        Organization org = mock(Organization.class);
        when(organizationRepository.findOrganizationForUser("admin", 10)).thenReturn(Optional.of(org));

        when(eventRepository.findById(1)).thenReturn(original);
        
        EventAndOrganizationId eaoi = mock(EventAndOrganizationId.class);
        when(eaoi.getOrganizationId()).thenReturn(10);
        when(eaoi.getId()).thenReturn(1);
        when(eventRepository.findEventAndOrganizationIdById(1)).thenReturn(eaoi);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short-name")).thenReturn(Optional.of(eaoi));

        eventManager.updateEventHeader(original, em, "admin");

        verify(eventRepository).updateHeader(anyInt(), eq("New Name"), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyString(), anyInt(), anyInt(), any());
    }

    @Test
    void testUpdateEventSeatsAndPrices() {
        Event original = mock(Event.class);
        when(original.getId()).thenReturn(1);
        when(original.getOrganizationId()).thenReturn(10);

        EventModification em = mock(EventModification.class);
        when(em.getAvailableSeats()).thenReturn(200);
        when(em.getOrganizationId()).thenReturn(10);
        when(em.getCurrency()).thenReturn("USD");
        when(em.isVatIncluded()).thenReturn(true);
        when(em.getVatPercentage()).thenReturn(java.math.BigDecimal.TEN);

        when(organizationRepository.findOrganizationForUser("admin", 10)).thenReturn(Optional.of(mock(Organization.class)));
        when(eventRepository.countExistingTickets(1)).thenReturn(100);
        
        Event modified = mock(Event.class);
        when(modified.getZoneId()).thenReturn(java.time.ZoneId.of("UTC"));
        when(eventRepository.findById(1)).thenReturn(modified);
        when(clockProvider.withZone(any())).thenReturn(java.time.Clock.systemUTC());

        eventManager.updateEventSeatsAndPrices(original, em, "admin");

        verify(eventRepository).updatePrices(anyString(), eq(200), anyBoolean(), any(), anyString(), eq(1), any(), anyInt());
        verify(ticketRepository).bulkTicketInitialization(any());
    }

    @Test
    void testCopyEvent() {
        EventModification em = mock(EventModification.class);
        when(em.getId()).thenReturn(null);
        when(em.getOrganizationId()).thenReturn(1);
        when(em.getShortName()).thenReturn("new-event");
        when(em.getAvailableSeats()).thenReturn(100);
        when(em.getBegin()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now()));
        when(em.getEnd()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().plusHours(1)));
        when(em.getZoneId()).thenReturn("UTC");
        
        alfio.model.metadata.AlfioMetadata metadata = mock(alfio.model.metadata.AlfioMetadata.class);
        when(metadata.getCopiedFrom()).thenReturn("old-event");
        when(em.getMetadata()).thenReturn(metadata);

        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(1);
        when(organizationRepository.findAllForUser("user")).thenReturn(Collections.singletonList(org));

        EventAndOrganizationId srcEvent = mock(EventAndOrganizationId.class);
        when(srcEvent.getId()).thenReturn(50);
        when(srcEvent.getOrganizationId()).thenReturn(1);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("old-event")).thenReturn(Optional.of(srcEvent));
        when(organizationRepository.findOrganizationForUser("user", 1)).thenReturn(Optional.of(org));

        MigrationInfoService migrationInfoService = mock(MigrationInfoService.class);
        MigrationInfo migrationInfo = mock(MigrationInfo.class);
        when(migrationInfo.getVersion()).thenReturn(MigrationVersion.fromVersion("1.0"));
        when(migrationInfoService.current()).thenReturn(migrationInfo);
        when(flyway.info()).thenReturn(migrationInfoService);

        AffectedRowCountAndKey<Integer> arcak = mock(AffectedRowCountAndKey.class);
        when(arcak.getKey()).thenReturn(123);
        when(eventRepository.insert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyBoolean(), any(), any(), any(), anyInt(), anyInt(), any(), anyInt(), any(), any(), any())).thenReturn(arcak);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(123);
        when(event.getOrganizationId()).thenReturn(1);
        when(event.getTimeZone()).thenReturn("UTC");
        when(event.now(any(ClockProvider.class))).thenReturn(ZonedDateTime.now());
        when(eventRepository.findById(123)).thenReturn(event);

        eventManager.createEvent(em, "user");

        verify(configurationRepository).copyEventConfiguration(eq(123), eq(1), eq(50), eq(1));
    }

    @Test
    void testSaveBadgeColorConfiguration() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getOrganizationId()).thenReturn(10);
        
        when(configurationManager.getFor(eq(alfio.model.system.ConfigurationKeys.CHECK_IN_COLOR_CONFIGURATION), any())).thenReturn(new ConfigurationManager.MaybeConfiguration(alfio.model.system.ConfigurationKeys.CHECK_IN_COLOR_CONFIGURATION));
        
        eventManager.saveBadgeColorConfiguration("blue", event, 100);
        
        verify(configurationRepository).insertEventLevel(eq(10), eq(1), eq(alfio.model.system.ConfigurationKeys.CHECK_IN_COLOR_CONFIGURATION.name()), contains("blue"), any());
    }

    @Test
    void testInsertCategory() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getOrganizationId()).thenReturn(10);
        when(event.getZoneId()).thenReturn(java.time.ZoneId.of("UTC"));
        when(event.getEnd()).thenReturn(ZonedDateTime.now().plusDays(1));
        when(event.getCurrency()).thenReturn("CHF");
        when(eventRepository.findById(1)).thenReturn(event);

        TicketCategoryModification tcm = mock(TicketCategoryModification.class);
        when(tcm.isBounded()).thenReturn(true);
        when(tcm.getMaxTickets()).thenReturn(10);
        when(tcm.getExpiration()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().plusHours(1)));
        when(tcm.getPrice()).thenReturn(java.math.BigDecimal.TEN);
        when(tcm.getInception()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().minusHours(1)));
        when(tcm.getValidCheckInFrom()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().minusHours(1)));
        when(tcm.getValidCheckInTo()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().plusHours(5)));
        when(tcm.getTicketValidityStart()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().minusHours(1)));
        when(tcm.getTicketValidityEnd()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().plusHours(5)));

        when(organizationRepository.findOrganizationForUser("admin", 10)).thenReturn(Optional.of(mock(Organization.class)));
        when(ticketCategoryRepository.getTicketAllocation(1)).thenReturn(50);
        when(ticketRepository.countNotAllocatedFreeAndReleasedTicket(1)).thenReturn(20);
        when(eventRepository.countExistingTickets(1)).thenReturn(100);
        
        AffectedRowCountAndKey<Integer> arcak = mock(AffectedRowCountAndKey.class);
        when(arcak.getKey()).thenReturn(1001);
        when(ticketCategoryRepository.insert(any(), any(), any(), anyInt(), anyBoolean(), anyInt(), anyBoolean(), anyInt(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any())).thenReturn(arcak);

        TicketCategory tc = mock(TicketCategory.class);
        when(tc.getId()).thenReturn(1001);
        when(tc.getMaxTickets()).thenReturn(10);
        when(ticketCategoryRepository.getByIdAndActive(1001, 1)).thenReturn(tc);

        eventManager.insertCategory(1, tcm, "admin");

        verify(ticketCategoryRepository).insert(any(), any(), any(), eq(10), anyBoolean(), eq(1), anyBoolean(), anyInt(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void testUpdateCategory() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getOrganizationId()).thenReturn(10);
        when(event.getZoneId()).thenReturn(java.time.ZoneId.of("UTC"));
        when(event.getEnd()).thenReturn(ZonedDateTime.now().plusDays(1));
        when(event.isFreeOfCharge()).thenReturn(false);
        when(event.getCurrency()).thenReturn("CHF");

        TicketCategoryModification tcm = mock(TicketCategoryModification.class);
        when(tcm.getId()).thenReturn(101);
        when(tcm.isBounded()).thenReturn(true);
        when(tcm.getMaxTickets()).thenReturn(20);
        when(tcm.getExpiration()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().plusHours(2)));
        when(tcm.isSkipWaitingList()).thenReturn(true);
        when(tcm.getPrice()).thenReturn(java.math.BigDecimal.TEN);
        when(tcm.getInception()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().minusHours(1)));
        when(tcm.getValidCheckInFrom()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().minusHours(1)));
        when(tcm.getValidCheckInTo()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().plusHours(5)));
        when(tcm.getTicketValidityStart()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().minusHours(1)));
        when(tcm.getTicketValidityEnd()).thenReturn(DateTimeModification.fromZonedDateTime(ZonedDateTime.now().plusHours(5)));

        TicketCategory existing = mock(TicketCategory.class);
        when(existing.getId()).thenReturn(101);
        when(existing.isBounded()).thenReturn(true);
        when(existing.getMaxTickets()).thenReturn(10);
        
        when(ticketCategoryRepository.getById(101)).thenReturn(existing);
        when(ticketCategoryRepository.getByIdAndActive(101, 1)).thenReturn(existing);
        when(organizationRepository.findOrganizationForUser("admin", 10)).thenReturn(Optional.of(mock(Organization.class)));
        when(eventRepository.countExistingTickets(1)).thenReturn(100);
        when(ticketRepository.countAllocatedTicketsForEvent(1)).thenReturn(50);
        when(ticketCategoryRepository.getByIdAndActive(101, 1)).thenReturn(existing);
        when(eventRepository.findById(1)).thenReturn(event);

        eventManager.updateCategory(101, 1, tcm, "admin");

        verify(ticketCategoryRepository).update(eq(101), any(), any(), any(), eq(20), anyBoolean(), anyInt(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testFixOutOfRangeCategories() {
        EventModification em = mock(EventModification.class);
        when(em.getShortName()).thenReturn("short-name");
        
        EventAndOrganizationId event = mock(EventAndOrganizationId.class);
        when(event.getId()).thenReturn(1);
        when(event.getOrganizationId()).thenReturn(10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short-name")).thenReturn(Optional.of(event));
        when(organizationRepository.findOrganizationForUser("admin", 10)).thenReturn(Optional.of(mock(Organization.class)));

        TicketCategory tc = mock(TicketCategory.class);
        when(tc.getId()).thenReturn(1001);
        when(tc.getName()).thenReturn("Category");
        when(tc.getInception(any())).thenReturn(ZonedDateTime.now().minusDays(1));
        when(tc.getExpiration(any())).thenReturn(ZonedDateTime.now().plusDays(2));
        
        when(ticketCategoryRepository.findAllTicketCategories(1)).thenReturn(Collections.singletonList(tc));
        
        ZonedDateTime newEnd = ZonedDateTime.now().plusDays(1);
        eventManager.fixOutOfRangeCategories(em, "admin", java.time.ZoneId.of("UTC"), newEnd);
        
        verify(ticketCategoryRepository).fixDates(eq(1001), any(), eq(newEnd));
    }

    @Test
    void testUpdateLinkedSubscriptions() {
        int eventId = 1;
        int organizationId = 10;
        alfio.model.subscription.LinkSubscriptionsToEventRequest request = mock(alfio.model.subscription.LinkSubscriptionsToEventRequest.class);
        java.util.UUID descriptorId = java.util.UUID.randomUUID();
        when(request.getDescriptorId()).thenReturn(descriptorId);
        
        when(jdbcTemplate.batchUpdate(anyString(), any(org.springframework.jdbc.core.namedparam.MapSqlParameterSource[].class))).thenReturn(new int[]{1});

        eventManager.updateLinkedSubscriptions(Collections.singletonList(request), eventId, organizationId);
        
        verify(subscriptionRepository).removeStaleSubscriptions(eq(eventId), eq(organizationId), anyList());
        verify(jdbcTemplate).batchUpdate(anyString(), any(org.springframework.jdbc.core.namedparam.MapSqlParameterSource[].class));
    }
}
