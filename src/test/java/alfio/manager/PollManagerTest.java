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

import alfio.model.EventAndOrganizationId;
import alfio.model.EventStatisticView;
import alfio.model.Ticket;
import alfio.model.modification.PollModification;
import alfio.model.modification.PollOptionModification;
import alfio.model.poll.*;
import alfio.model.result.ErrorCode;
import alfio.model.result.Result;
import alfio.repository.*;
import alfio.util.PinGenerator;
import ch.digitalfondue.npjt.AffectedRowCountAndKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PollManagerTest {

    private PollRepository pollRepository;
    private EventRepository eventRepository;
    private TicketRepository ticketRepository;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private TicketSearchRepository ticketSearchRepository;
    private AuditingRepository auditingRepository;
    private PollManager pollManager;

    private MockedStatic<PinGenerator> mockedPinGenerator;

    @BeforeEach
    public void setUp() {
        pollRepository = mock(PollRepository.class);
        eventRepository = mock(EventRepository.class);
        ticketRepository = mock(TicketRepository.class);
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        ticketSearchRepository = mock(TicketSearchRepository.class);
        auditingRepository = mock(AuditingRepository.class);
        pollManager = new PollManager(pollRepository, eventRepository, ticketRepository, jdbcTemplate, ticketSearchRepository, auditingRepository);

        mockedPinGenerator = mockStatic(PinGenerator.class);
    }

    @AfterEach
    public void tearDown() {
        if (mockedPinGenerator != null) {
            mockedPinGenerator.close();
        }
    }

    private void mockPinValid(String pin, boolean valid) {
        mockedPinGenerator.when(() -> PinGenerator.isPinValid(pin)).thenReturn(valid);
    }

    private void mockPinToPartial(String pin, String partial) {
        mockedPinGenerator.when(() -> PinGenerator.pinToPartialUuid(pin)).thenReturn(partial);
    }

    // --- getActiveForEvent ---

    @Test
    public void testGetActiveForEvent_EventNotFound() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.empty());
        mockPinValid(pin, true);

        Result<List<Poll>> result = pollManager.getActiveForEvent(eventName, pin);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.EventError.NOT_FOUND, result.getFirstErrorOrNull());
    }

    @Test
    public void testGetActiveForEvent_PinInvalid() {
        String eventName = "test-event";
        String pin = "INVALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, false);

        Result<List<Poll>> result = pollManager.getActiveForEvent(eventName, pin);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("pin.invalid", result.getFirstErrorOrNull().getCode());
    }

    @Test
    public void testGetActiveForEvent_TicketNotFound() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, true);
        mockPinToPartial(pin, "abcde");
        when(ticketRepository.findByEventIdAndPartialUUIDForUpdate(1, "abcde%", Ticket.TicketStatus.CHECKED_IN))
                .thenReturn(Collections.emptyList());

        Result<List<Poll>> result = pollManager.getActiveForEvent(eventName, pin);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("pin.invalid", result.getFirstErrorOrNull().getCode());
    }

    @Test
    public void testGetActiveForEvent_DuplicateTicket() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, true);
        mockPinToPartial(pin, "abcde");
        Ticket ticket1 = mock(Ticket.class);
        Ticket ticket2 = mock(Ticket.class);
        when(ticketRepository.findByEventIdAndPartialUUIDForUpdate(1, "abcde%", Ticket.TicketStatus.CHECKED_IN))
                .thenReturn(List.of(ticket1, ticket2));

        Result<List<Poll>> result = pollManager.getActiveForEvent(eventName, pin);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("pin.duplicate", result.getFirstErrorOrNull().getCode());
    }

    @Test
    public void testGetActiveForEvent_Success() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, true);
        mockPinToPartial(pin, "abcde");
        Ticket ticket = mock(Ticket.class);
        when(ticketRepository.findByEventIdAndPartialUUIDForUpdate(1, "abcde%", Ticket.TicketStatus.CHECKED_IN))
                .thenReturn(List.of(ticket));
        Poll poll = new Poll(200L, Poll.PollStatus.OPEN, Map.of("en", "Title"), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findActiveForEvent(1)).thenReturn(List.of(poll));

        Result<List<Poll>> result = pollManager.getActiveForEvent(eventName, pin);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals(200L, result.getData().get(0).id());
    }

    // --- getSingleActiveForEvent ---

    @Test
    public void testGetSingleActiveForEvent_ValidationFail() {
        String eventName = "test-event";
        String pin = "INVALIDPIN";
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.empty());
        mockPinValid(pin, true);

        Result<PollWithOptions> result = pollManager.getSingleActiveForEvent(eventName, 200L, pin);
        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testGetSingleActiveForEvent_PollNotFound() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, true);
        mockPinToPartial(pin, "abcde");
        Ticket ticket = mock(Ticket.class);
        when(ticketRepository.findByEventIdAndPartialUUIDForUpdate(1, "abcde%", Ticket.TicketStatus.CHECKED_IN))
                .thenReturn(List.of(ticket));
        when(pollRepository.findSingleActiveForEvent(1, 200L)).thenReturn(Optional.empty());

        Result<PollWithOptions> result = pollManager.getSingleActiveForEvent(eventName, 200L, pin);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("not_found", result.getFirstErrorOrNull().getCode());
    }

    @Test
    public void testGetSingleActiveForEvent_Success() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, true);
        mockPinToPartial(pin, "abcde");
        Ticket ticket = mock(Ticket.class);
        when(ticketRepository.findByEventIdAndPartialUUIDForUpdate(1, "abcde%", Ticket.TicketStatus.CHECKED_IN))
                .thenReturn(List.of(ticket));
        Poll poll = new Poll(200L, Poll.PollStatus.OPEN, Map.of("en", "Title"), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findSingleActiveForEvent(1, 200L)).thenReturn(Optional.of(poll));

        PollOption option = new PollOption(300L, 200L, Map.of("en", "Opt"), Map.of());
        when(pollRepository.getOptionsForPoll(200L)).thenReturn(List.of(option));

        Result<PollWithOptions> result = pollManager.getSingleActiveForEvent(eventName, 200L, pin);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(200L, result.getData().getPoll().id());
        assertEquals(1, result.getData().getOptions().size());
        assertEquals(300L, result.getData().getOptions().get(0).getId());
    }

    // --- registerAnswer ---

    @Test
    public void testRegisterAnswer_NullParams() {
        Result<Boolean> r1 = pollManager.registerAnswer("eventName", null, 1L, "pin");
        assertFalse(r1.isSuccess());
        assertEquals("not_found", r1.getFirstErrorOrNull().getCode());

        Result<Boolean> r2 = pollManager.registerAnswer("eventName", 1L, null, "pin");
        assertFalse(r2.isSuccess());
        assertEquals("not_found", r2.getFirstErrorOrNull().getCode());
    }

    @Test
    public void testRegisterAnswer_ValidationFail() {
        String eventName = "test-event";
        String pin = "INVALIDPIN";
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.empty());
        mockPinValid(pin, true);

        Result<Boolean> result = pollManager.registerAnswer(eventName, 1L, 2L, pin);
        assertFalse(result.isSuccess());
    }

    @Test
    public void testRegisterAnswer_InvalidSelection() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, true);
        mockPinToPartial(pin, "abcde");
        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(50);
        when(ticketRepository.findByEventIdAndPartialUUIDForUpdate(1, "abcde%", Ticket.TicketStatus.CHECKED_IN))
                .thenReturn(List.of(ticket));

        when(pollRepository.checkPollOption(2L, 1L, 1)).thenReturn(0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pollManager.registerAnswer(eventName, 1L, 2L, pin);
        });
        assertEquals("Invalid selection", exception.getMessage());
    }

    @Test
    public void testRegisterAnswer_UnexpectedInsertError() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, true);
        mockPinToPartial(pin, "abcde");
        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(50);
        when(ticketRepository.findByEventIdAndPartialUUIDForUpdate(1, "abcde%", Ticket.TicketStatus.CHECKED_IN))
                .thenReturn(List.of(ticket));

        when(pollRepository.checkPollOption(2L, 1L, 1)).thenReturn(1);
        when(pollRepository.registerAnswer(1L, 2L, 50, 10)).thenReturn(0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pollManager.registerAnswer(eventName, 1L, 2L, pin);
        });
        assertEquals("Unexpected error while inserting answer", exception.getMessage());
    }

    @Test
    public void testRegisterAnswer_Success() {
        String eventName = "test-event";
        String pin = "VALIDPIN";
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName(eventName)).thenReturn(Optional.of(event));
        mockPinValid(pin, true);
        mockPinToPartial(pin, "abcde");
        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(50);
        when(ticketRepository.findByEventIdAndPartialUUIDForUpdate(1, "abcde%", Ticket.TicketStatus.CHECKED_IN))
                .thenReturn(List.of(ticket));

        when(pollRepository.checkPollOption(2L, 1L, 1)).thenReturn(1);
        when(pollRepository.registerAnswer(1L, 2L, 50, 10)).thenReturn(1);

        Result<Boolean> result = pollManager.registerAnswer(eventName, 1L, 2L, pin);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
    }

    // --- getAllForEvent ---

    @Test
    public void testGetAllForEvent_EventNotFound() {
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.empty());
        List<Poll> result = pollManager.getAllForEvent("event");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllForEvent_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));
        Poll poll = new Poll(200L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findAllForEvent(1)).thenReturn(List.of(poll));

        List<Poll> result = pollManager.getAllForEvent("event");
        assertEquals(1, result.size());
        assertEquals(200L, result.get(0).id());
    }

    // --- getSingleForEvent ---

    @Test
    public void testGetSingleForEvent_EventNotFound() {
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.empty());
        Optional<PollWithOptions> result = pollManager.getSingleForEvent(1L, "event");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetSingleForEvent_PollNotFound() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.empty());

        Optional<PollWithOptions> result = pollManager.getSingleForEvent(100L, "event");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetSingleForEvent_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));
        PollOption option = new PollOption(2L, 100L, Map.of(), Map.of());
        when(pollRepository.getOptionsForPoll(100L)).thenReturn(List.of(option));

        Optional<PollWithOptions> result = pollManager.getSingleForEvent(100L, "event");
        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getPoll().id());
        assertEquals(1, result.get().getOptions().size());
    }

    @Test
    public void testGetSingleForEvent_PollIdNull() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));
        assertThrows(NullPointerException.class, () -> {
            pollManager.getSingleForEvent(null, "event");
        });
    }

    // --- createNewPoll ---

    @Test
    public void testCreateNewPoll_InvalidForm() {
        PollModification form = mock(PollModification.class);
        when(form.isValid()).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.createNewPoll("event", form);
        });
    }

    @Test
    public void testCreateNewPoll_EventNotFound() {
        PollModification form = mock(PollModification.class);
        when(form.isValid()).thenReturn(true);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.empty());

        Optional<Long> result = pollManager.createNewPoll("event", form);
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateNewPoll_SuccessSingleOption() {
        PollModification form = mock(PollModification.class);
        when(form.isValid()).thenReturn(true);
        when(form.getTitle()).thenReturn(Map.of("en", "Poll Title"));
        when(form.getDescription()).thenReturn(Map.of("en", "Desc"));
        when(form.getOrder()).thenReturn(5);
        when(form.isAccessRestricted()).thenReturn(false);

        PollOptionModification optMod = new PollOptionModification(null, Map.of("en", "Opt"), Map.of());
        when(form.getOptions()).thenReturn(List.of(optMod));

        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));

        AffectedRowCountAndKey<Long> pollKey = mock(AffectedRowCountAndKey.class);
        when(pollKey.getAffectedRowCount()).thenReturn(1);
        when(pollKey.getKey()).thenReturn(100L);
        when(pollRepository.insert(eq(Map.of("en", "Poll Title")), eq(Map.of("en", "Desc")), eq(List.of()), eq(5), eq(1), eq(10)))
                .thenReturn(pollKey);

        Optional<Long> result = pollManager.createNewPoll("event", form);
        assertTrue(result.isPresent());
        assertEquals(100L, result.get());

        verify(pollRepository).insertOption(eq(100L), eq(Map.of("en", "Opt")), anyMap(), eq(10));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateNewPoll_SuccessMultipleOptionsAccessRestricted() {
        PollModification form = mock(PollModification.class);
        when(form.isValid()).thenReturn(true);
        when(form.getTitle()).thenReturn(Map.of("en", "Poll Title"));
        when(form.getDescription()).thenReturn(null);
        when(form.getOrder()).thenReturn(2);
        when(form.isAccessRestricted()).thenReturn(true);

        PollOptionModification optMod1 = new PollOptionModification(null, Map.of("en", "Opt1"), Map.of());
        PollOptionModification optMod2 = new PollOptionModification(null, Map.of("en", "Opt2"), null);
        when(form.getOptions()).thenReturn(List.of(optMod1, optMod2));

        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));

        AffectedRowCountAndKey<Long> pollKey = mock(AffectedRowCountAndKey.class);
        when(pollKey.getAffectedRowCount()).thenReturn(1);
        when(pollKey.getKey()).thenReturn(100L);
        when(pollRepository.insert(eq(Map.of("en", "Poll Title")), eq(Map.of()), anyList(), eq(2), eq(1), eq(10)))
                .thenReturn(pollKey);

        when(pollRepository.bulkInsertOptions()).thenReturn("bulk-insert-query");
        when(jdbcTemplate.batchUpdate(eq("bulk-insert-query"), any(MapSqlParameterSource[].class))).thenReturn(new int[]{1, 1});

        Optional<Long> result = pollManager.createNewPoll("event", form);
        assertTrue(result.isPresent());
        assertEquals(100L, result.get());

        verify(jdbcTemplate).batchUpdate(eq("bulk-insert-query"), any(MapSqlParameterSource[].class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateNewPoll_BatchUpdateSizeMismatch() {
        PollModification form = mock(PollModification.class);
        when(form.isValid()).thenReturn(true);
        when(form.getTitle()).thenReturn(Map.of("en", "Poll Title"));
        when(form.getDescription()).thenReturn(null);
        when(form.getOrder()).thenReturn(2);
        when(form.isAccessRestricted()).thenReturn(true);

        PollOptionModification optMod1 = new PollOptionModification(null, Map.of("en", "Opt1"), Map.of());
        PollOptionModification optMod2 = new PollOptionModification(null, Map.of("en", "Opt2"), null);
        when(form.getOptions()).thenReturn(List.of(optMod1, optMod2));

        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));

        AffectedRowCountAndKey<Long> pollKey = mock(AffectedRowCountAndKey.class);
        when(pollKey.getAffectedRowCount()).thenReturn(1);
        when(pollKey.getKey()).thenReturn(100L);
        when(pollRepository.insert(eq(Map.of("en", "Poll Title")), eq(Map.of()), anyList(), eq(2), eq(1), eq(10)))
                .thenReturn(pollKey);

        when(pollRepository.bulkInsertOptions()).thenReturn("bulk-insert-query");
        when(jdbcTemplate.batchUpdate(eq("bulk-insert-query"), any(MapSqlParameterSource[].class))).thenReturn(new int[]{1, 0});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pollManager.createNewPoll("event", form);
        });
        assertEquals("Unexpected result from update.", exception.getMessage());
    }

    // --- deletePoll ---

    @Test
    public void testDeletePoll_Fails() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.deletePoll(100L, 1, 10)).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.deletePoll(event, 100L);
        });
    }

    @Test
    public void testDeletePoll_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.deletePoll(100L, 1, 10)).thenReturn(1);

        assertTrue(pollManager.deletePoll(event, 100L));
    }

    @Test
    public void testDeletePoll_NullId() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.deletePoll(event, null);
        });
    }

    // --- updatePoll ---

    @Test
    public void testUpdatePoll_FormInvalid() {
        PollModification form = mock(PollModification.class);
        when(form.getId()).thenReturn(100L);
        when(form.isValid(100L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.updatePoll("event", form);
        });
    }

    @Test
    public void testUpdatePoll_EventNotFound() {
        PollModification form = mock(PollModification.class);
        when(form.getId()).thenReturn(100L);
        when(form.isValid(100L)).thenReturn(true);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.empty());

        Optional<PollWithOptions> result = pollManager.updatePoll("event", form);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testUpdatePoll_PollNotFound() {
        PollModification form = mock(PollModification.class);
        when(form.getId()).thenReturn(100L);
        when(form.isValid(100L)).thenReturn(true);
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            pollManager.updatePoll("event", form);
        });
    }

    @Test
    public void testUpdatePoll_Success() {
        PollModification form = mock(PollModification.class);
        when(form.getId()).thenReturn(100L);
        when(form.isValid(100L)).thenReturn(true);
        when(form.getTitle()).thenReturn(Map.of("en", "New Title"));
        when(form.getDescription()).thenReturn(Map.of("en", "New Desc"));
        when(form.getOrder()).thenReturn(3);

        PollOptionModification newOpt = new PollOptionModification(null, Map.of("en", "New Opt"), Map.of());
        PollOptionModification existOpt = new PollOptionModification(200L, Map.of("en", "Exist Opt"), Map.of());
        when(form.getOptions()).thenReturn(List.of(newOpt, existOpt));

        Poll origPoll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag-uuid"), 1, 1, 10);

        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event")).thenReturn(Optional.of(event));

        when(form.isAccessRestricted()).thenReturn(false);

        when(pollRepository.update(eq(Map.of("en", "New Title")), eq(Map.of("en", "New Desc")), eq(List.of()), eq(3), eq(100L), eq(1)))
                .thenReturn(1);

        when(pollRepository.bulkUpdateOptions()).thenReturn("bulk-update-query");
        when(jdbcTemplate.batchUpdate(eq("bulk-update-query"), any(MapSqlParameterSource[].class))).thenReturn(new int[]{1});

        Poll updatedPoll = new Poll(100L, Poll.PollStatus.OPEN, Map.of("en", "New Title"), Map.of("en", "New Desc"), List.of(), 3, 1, 10);
        PollOption updatedOption = new PollOption(200L, 100L, Map.of("en", "Exist Opt"), Map.of());
        PollOption newInsertedOption = new PollOption(201L, 100L, Map.of("en", "New Opt"), Map.of());
        when(pollRepository.getOptionsForPoll(100L)).thenReturn(List.of(updatedOption, newInsertedOption));

        when(pollRepository.findSingleForEvent(1, 100L))
                .thenReturn(Optional.of(origPoll))
                .thenReturn(Optional.of(updatedPoll));

        Optional<PollWithOptions> result = pollManager.updatePoll("event", form);
        assertTrue(result.isPresent());
        assertEquals("New Title", result.get().getPoll().title().get("en"));
        assertEquals(2, result.get().getOptions().size());
        verify(pollRepository).insertOption(eq(100L), eq(Map.of("en", "New Opt")), anyMap(), eq(10));
    }

    // --- updateStatus ---

    @Test
    public void testUpdateStatus_RevertToDraft() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.updateStatus(100L, event, Poll.PollStatus.DRAFT);
        });
    }

    @Test
    public void testUpdateStatus_Fails() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.updateStatus(Poll.PollStatus.OPEN, 100L, 1)).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.updateStatus(100L, event, Poll.PollStatus.OPEN);
        });
    }

    @Test
    public void testUpdateStatus_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.updateStatus(Poll.PollStatus.OPEN, 100L, 1)).thenReturn(1);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        Optional<PollWithOptions> result = pollManager.updateStatus(100L, event, Poll.PollStatus.OPEN);
        assertTrue(result.isPresent());
        assertEquals(Poll.PollStatus.OPEN, result.get().getPoll().status());
    }

    // --- searchTicketsToAllow ---

    @Test
    public void testSearchTicketsToAllow_FilterBlank() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.searchTicketsToAllow(event, 100L, "  ");
        });
    }

    @Test
    public void testSearchTicketsToAllow_PollNotFound() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.empty());

        Optional<List<PollParticipant>> result = pollManager.searchTicketsToAllow(event, 100L, "user");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSearchTicketsToAllow_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        PollParticipant participant = new PollParticipant(1, "First", "Last", "email", "category");
        when(ticketSearchRepository.filterConfirmedTicketsInEventForPoll(1, 20, "%user%", List.of("tag1")))
                .thenReturn(List.of(participant));

        Optional<List<PollParticipant>> result = pollManager.searchTicketsToAllow(event, 100L, "user");
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("First", result.get().get(0).getFirstName());
    }

    // --- allowTicketsToVote ---

    @Test
    public void testAllowTicketsToVote_IdsEmpty() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.allowTicketsToVote(event, List.of(), 100L);
        });
    }

    @Test
    public void testAllowTicketsToVote_NoAllowedTags() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.allowTicketsToVote(event, List.of(1, 2), 100L);
        });
    }

    @Test
    public void testAllowTicketsToVote_TaggingFails() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        when(ticketRepository.tagTickets(List.of(1, 2), 1, "tag1")).thenReturn(1);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.allowTicketsToVote(event, List.of(1, 2), 100L);
        });
    }

    @Test
    public void testAllowTicketsToVote_AuditingFails() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        when(ticketRepository.tagTickets(List.of(1, 2), 1, "tag1")).thenReturn(2);
        when(auditingRepository.registerTicketTag(eq(List.of(1, 2)), anyList())).thenReturn(1);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.allowTicketsToVote(event, List.of(1, 2), 100L);
        });
    }

    @Test
    public void testAllowTicketsToVote_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        when(ticketRepository.tagTickets(List.of(1, 2), 1, "tag1")).thenReturn(2);
        when(auditingRepository.registerTicketTag(eq(List.of(1, 2)), anyList())).thenReturn(2);

        assertTrue(pollManager.allowTicketsToVote(event, List.of(1, 2), 100L));
    }

    // --- removeParticipants ---

    @Test
    public void testRemoveParticipants_PollNotFound() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            pollManager.removeParticipants(event, List.of(1), 100L);
        });
    }

    @Test
    public void testRemoveParticipants_NoAllowedTags() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.removeParticipants(event, List.of(1), 100L);
        });
    }

    @Test
    public void testRemoveParticipants_UntagFails() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        when(ticketRepository.untagTickets(List.of(1), 1, "tag1")).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.removeParticipants(event, List.of(1), 100L);
        });
    }

    @Test
    public void testRemoveParticipants_AuditingFails() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        when(ticketRepository.untagTickets(List.of(1), 1, "tag1")).thenReturn(1);
        when(auditingRepository.registerTicketUntag(eq(List.of(1)), anyList())).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.removeParticipants(event, List.of(1), 100L);
        });
    }

    @Test
    public void testRemoveParticipants_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        when(ticketRepository.untagTickets(List.of(1), 1, "tag1")).thenReturn(1);
        when(auditingRepository.registerTicketUntag(eq(List.of(1)), anyList())).thenReturn(1);

        PollParticipant p = new PollParticipant(1, "First", "Last", "email", "cat");
        when(ticketRepository.getTicketsForEventByTags(1, List.of("tag1"))).thenReturn(List.of(p));

        List<PollParticipant> result = pollManager.removeParticipants(event, List.of(1), 100L);
        assertEquals(1, result.size());
        assertEquals("First", result.get(0).getFirstName());
    }

    // --- removeOption ---

    @Test
    public void testRemoveOption_PollNotFound() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            pollManager.removeOption(event, 100L, 5L);
        });
    }

    @Test
    public void testRemoveOption_DeleteFails() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));
        when(pollRepository.deleteOption(100L, 5L)).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            pollManager.removeOption(event, 100L, 5L);
        });
    }

    @Test
    public void testRemoveOption_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));
        when(pollRepository.deleteOption(100L, 5L)).thenReturn(1);

        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));
        when(pollRepository.getOptionsForPoll(100L)).thenReturn(List.of());

        Optional<PollWithOptions> result = pollManager.removeOption(event, 100L, 5L);
        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getPoll().id());
    }

    // --- fetchAllowedTickets ---

    @Test
    public void testFetchAllowedTickets_PollNotFound() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            pollManager.fetchAllowedTickets(event, 100L);
        });
    }

    @Test
    public void testFetchAllowedTickets_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        PollParticipant p = new PollParticipant(1, "First", "Last", "email", "cat");
        when(ticketRepository.getTicketsForEventByTags(1, List.of("tag1"))).thenReturn(List.of(p));

        List<PollParticipant> result = pollManager.fetchAllowedTickets(event, 100L);
        assertEquals(1, result.size());
        assertEquals("First", result.get(0).getFirstName());
    }

    // --- getStatisticsFor ---

    @Test
    public void testGetStatisticsFor_PollNotFound() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.empty());

        Optional<PollStatistics> result = pollManager.getStatisticsFor(event, 100L);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetStatisticsFor_AccessRestricted() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of("tag1"), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        when(ticketRepository.countTicketsMatchingTagsAndStatus(1, List.of("tag1"), List.of("CHECKED_IN")))
                .thenReturn(15);

        PollOptionStatistics optStats = new PollOptionStatistics(10, 500L);
        when(pollRepository.getStatisticsFor(100L, 1)).thenReturn(List.of(optStats));

        Optional<PollStatistics> result = pollManager.getStatisticsFor(event, 100L);
        assertTrue(result.isPresent());
        assertEquals(10, result.get().getTotalVotes());
        assertEquals(15, result.get().getAllowedParticipants());
        assertEquals(1, result.get().getOptionStatistics().size());
        assertEquals(500L, result.get().getOptionStatistics().get(0).getOptionId());
        assertEquals("66.70", result.get().getParticipationPercentage());
        assertEquals("100.00", result.get().getOptionStatistics().get(0).getPercentage());
    }

    @Test
    public void testGetStatisticsFor_Public() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        Poll poll = new Poll(100L, Poll.PollStatus.OPEN, Map.of(), Map.of(), List.of(), 1, 1, 10);
        when(pollRepository.findSingleForEvent(1, 100L)).thenReturn(Optional.of(poll));

        EventStatisticView eventStats = new EventStatisticView(false, false, 50, 20, 30, 25, 0, 0, 0, 0, 1);
        when(eventRepository.findStatisticsFor(1)).thenReturn(eventStats);

        PollOptionStatistics optStats = new PollOptionStatistics(8, 500L);
        PollOptionStatistics optStats2 = new PollOptionStatistics(0, 501L);
        when(pollRepository.getStatisticsFor(100L, 1)).thenReturn(List.of(optStats, optStats2));

        Optional<PollStatistics> result = pollManager.getStatisticsFor(event, 100L);
        assertTrue(result.isPresent());
        assertEquals(8, result.get().getTotalVotes());
        assertEquals(25, result.get().getAllowedParticipants());
        assertEquals(2, result.get().getOptionStatistics().size());
        assertEquals("32.00", result.get().getParticipationPercentage());
        assertEquals("0", result.get().getOptionStatistics().get(1).getPercentage());
    }
}
