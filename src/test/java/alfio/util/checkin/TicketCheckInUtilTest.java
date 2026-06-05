package alfio.util.checkin;

import alfio.model.TicketCategory;
import alfio.manager.ExtensionManager;
import alfio.manager.system.ConfigurationManager;
import alfio.model.Event;
import alfio.model.Ticket;
import alfio.model.metadata.JoinLink;
import alfio.model.metadata.TicketMetadata;
import alfio.repository.EventRepository;
import alfio.repository.TicketCategoryRepository;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TicketCheckInUtilTest {

    @Test
    void shouldGenerateOnlineCheckInUrl() {

        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);

        UUID uuid = UUID.randomUUID();

        when(event.getPrivateKey()).thenReturn("private-key");
        when(event.supportsQRCodeCaseInsensitive()).thenReturn(false);
        when(event.getShortName()).thenReturn("my-event");

        when(ticket.getPublicUuid()).thenReturn(uuid);
        when(ticket.ticketCode("private-key", false))
            .thenReturn("ticket-code");

        String expectedHash =
            DigestUtils.sha256Hex("ticket-code");

        String result = TicketCheckInUtil.ticketOnlineCheckInUrl(
            event,
            ticket,
            "https://alf.io/"
        );

        assertEquals(
            "https://alf.io/event/my-event/ticket/"
                + uuid
                + "/check-in/"
                + expectedHash,
            result
        );
    }

    @Test
    void shouldReturnCustomOnlineCheckInInfo() {

        ExtensionManager extensionManager = mock(ExtensionManager.class);
        EventRepository eventRepository = mock(EventRepository.class);
        TicketCategoryRepository ticketCategoryRepository = mock(TicketCategoryRepository.class);
        ConfigurationManager configurationManager = mock(ConfigurationManager.class);

        Event event = mock(Event.class);
        Ticket ticket = mock(Ticket.class);
        TicketCategory ticketCategory = mock(TicketCategory.class);

        JoinLink joinLink = new JoinLink(
            "https://meet.example.com",
            null,
            null,
            Map.of("en", "Join meeting")
        );

        TicketMetadata ticketMetadata = new TicketMetadata(
            joinLink,
            Map.of("en", "Meeting description"),
            Map.of()
        );

        when(extensionManager.handleCustomOnlineJoinUrl(
            eq(event),
            eq(ticket),
            anyMap()
        )).thenReturn(Optional.of(ticketMetadata));

        Map<String, String> result =
            TicketCheckInUtil.getOnlineCheckInInfo(
                extensionManager,
                eventRepository,
                ticketCategoryRepository,
                configurationManager,
                event,
                Locale.ENGLISH,
                ticket,
                ticketCategory,
                Map.of()
            );

        assertEquals(
            "true",
            result.get(TicketCheckInUtil.CUSTOM_CHECK_IN_URL)
        );

        assertEquals(
            "https://meet.example.com",
            result.get(TicketCheckInUtil.ONLINE_CHECK_IN_URL)
        );

        assertEquals(
            "Join meeting",
            result.get(TicketCheckInUtil.CUSTOM_CHECK_IN_URL_TEXT)
        );

        assertEquals(
            "Meeting description",
            result.get(TicketCheckInUtil.CUSTOM_CHECK_IN_URL_DESCRIPTION)
        );

        assertEquals(
            "",
            result.get("prerequisites")
        );
    }
}