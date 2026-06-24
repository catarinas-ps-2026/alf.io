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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import alfio.manager.system.ConfigurationLevel;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.Mailer;
import alfio.model.*;
import alfio.model.system.ConfigurationKeys;
import alfio.model.user.Organization;
import alfio.repository.EventDescriptionRepository;
import alfio.repository.EventRepository;
import alfio.repository.TicketCategoryRepository;
import alfio.repository.TicketRepository;
import alfio.repository.user.OrganizationRepository;
import com.ryantenney.passkit4j.Pass;
import com.ryantenney.passkit4j.PassSerializer;
import com.ryantenney.passkit4j.sign.PassSigner;
import com.ryantenney.passkit4j.sign.PassSignerImpl;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class PassKitManagerTest {
    private EventRepository eventRepository;
    private OrganizationRepository organizationRepository;
    private ConfigurationManager configurationManager;
    private FileUploadManager fileUploadManager;
    private EventDescriptionRepository eventDescriptionRepository;
    private TicketCategoryRepository ticketCategoryRepository;
    private TicketRepository ticketRepository;
    private FileBlobCacheManager fileBlobCacheManager;

    private PassKitManager passKitManager;
    private List<File> tempFilesToDelete;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        configurationManager = mock(ConfigurationManager.class);
        fileUploadManager = mock(FileUploadManager.class);
        eventDescriptionRepository = mock(EventDescriptionRepository.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        ticketRepository = mock(TicketRepository.class);
        fileBlobCacheManager = mock(FileBlobCacheManager.class);
        tempFilesToDelete = new ArrayList<>();

        passKitManager = new PassKitManager(
                eventRepository,
                organizationRepository,
                configurationManager,
                fileUploadManager,
                eventDescriptionRepository,
                ticketCategoryRepository,
                ticketRepository,
                fileBlobCacheManager);
    }

    @AfterEach
    void tearDown() {
        for (File f : tempFilesToDelete) {
            if (f.exists()) {
                f.delete();
            }
        }
    }

    private ConfigurationManager.MaybeConfiguration mockMaybeConfig(
            ConfigurationKeys key, String value, boolean present) {
        ConfigurationManager.MaybeConfiguration maybeConfig = mock(ConfigurationManager.MaybeConfiguration.class);
        when(maybeConfig.isPresent()).thenReturn(present);
        when(maybeConfig.isEmpty()).thenReturn(!present);
        when(maybeConfig.getValue()).thenReturn(present ? Optional.of(value) : Optional.empty());
        when(maybeConfig.getValueOrNull()).thenReturn(present ? value : null);
        when(maybeConfig.getValueOrDefault(anyString())).thenAnswer(invocation -> {
            String def = invocation.getArgument(0);
            return present ? value : def;
        });
        when(maybeConfig.getValueAsBooleanOrDefault()).thenReturn(present ? Boolean.parseBoolean(value) : false);
        return maybeConfig;
    }

    private void setupConfiguration(
            boolean enablePass,
            String typeId,
            String keystore,
            String keystorePwd,
            String teamId,
            String privateKeyAlias) {

        ConfigurationManager.MaybeConfiguration enablePassConf =
                mockMaybeConfig(ConfigurationKeys.ENABLE_PASS, String.valueOf(enablePass), true);
        ConfigurationManager.MaybeConfiguration typeIdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER, typeId, typeId != null);
        ConfigurationManager.MaybeConfiguration keystoreConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_KEYSTORE, keystore, keystore != null);
        ConfigurationManager.MaybeConfiguration keystorePwdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_KEYSTORE_PASSWORD, keystorePwd, keystorePwd != null);
        ConfigurationManager.MaybeConfiguration teamIdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_TEAM_IDENTIFIER, teamId, teamId != null);
        ConfigurationManager.MaybeConfiguration privateKeyAliasConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_PRIVATE_KEY_ALIAS, privateKeyAlias, privateKeyAlias != null);

        Map<ConfigurationKeys, ConfigurationManager.MaybeConfiguration> confMap = new HashMap<>();
        confMap.put(ConfigurationKeys.ENABLE_PASS, enablePassConf);
        confMap.put(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER, typeIdConf);
        confMap.put(ConfigurationKeys.PASSBOOK_KEYSTORE, keystoreConf);
        confMap.put(ConfigurationKeys.PASSBOOK_KEYSTORE_PASSWORD, keystorePwdConf);
        confMap.put(ConfigurationKeys.PASSBOOK_TEAM_IDENTIFIER, teamIdConf);
        confMap.put(ConfigurationKeys.PASSBOOK_PRIVATE_KEY_ALIAS, privateKeyAliasConf);

        when(configurationManager.getFor(anySet(), any(ConfigurationLevel.class)))
                .thenReturn(confMap);

        when(configurationManager.getFor(eq(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER), any(ConfigurationLevel.class)))
                .thenReturn(typeIdConf);
    }

    @Test
    void testRetrieveTicketDetails_EventNotFound() {
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.empty());

        Optional<Pair<EventAndOrganizationId, Ticket>> result = passKitManager.retrieveTicketDetails(
                "event-name", UUID.randomUUID().toString());
        assertFalse(result.isPresent());
    }

    @Test
    void testRetrieveTicketDetails_TicketNotFound() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        UUID ticketUuid = UUID.randomUUID();
        when(ticketRepository.findOptionalByPublicUUID(ticketUuid)).thenReturn(Optional.empty());

        Optional<Pair<EventAndOrganizationId, Ticket>> result =
                passKitManager.retrieveTicketDetails("event-name", ticketUuid.toString());
        assertFalse(result.isPresent());
    }

    @Test
    void testRetrieveTicketDetails_EventIdMismatch() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        UUID ticketUuid = UUID.randomUUID();
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(2);

        when(ticketRepository.findOptionalByPublicUUID(ticketUuid)).thenReturn(Optional.of(ticket));

        Optional<Pair<EventAndOrganizationId, Ticket>> result =
                passKitManager.retrieveTicketDetails("event-name", ticketUuid.toString());
        assertFalse(result.isPresent());
    }

    @Test
    void testRetrieveTicketDetails_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        UUID ticketUuid = UUID.randomUUID();
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);

        when(ticketRepository.findOptionalByPublicUUID(ticketUuid)).thenReturn(Optional.of(ticket));

        Optional<Pair<EventAndOrganizationId, Ticket>> result =
                passKitManager.retrieveTicketDetails("event-name", ticketUuid.toString());
        assertTrue(result.isPresent());
        assertEquals(event, result.get().getLeft());
        assertEquals(ticket, result.get().getRight());
    }

    @Test
    void testValidateToken_HeaderDoesNotStartWithApplePass() {
        Optional<Pair<EventAndOrganizationId, Ticket>> result = passKitManager.validateToken(
                "event-name", "type-id", UUID.randomUUID().toString(), "Bearer token");
        assertFalse(result.isPresent());
    }

    @Test
    void testValidateToken_EventNotFound() {
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.empty());

        Optional<Pair<EventAndOrganizationId, Ticket>> result = passKitManager.validateToken(
                "event-name", "type-id", UUID.randomUUID().toString(), "ApplePass token");
        assertFalse(result.isPresent());
    }

    @Test
    void testValidateToken_TypeIdentifierNotPresent() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        ConfigurationManager.MaybeConfiguration typeIdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER, null, false);
        when(configurationManager.getFor(eq(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER), any(ConfigurationLevel.class)))
                .thenReturn(typeIdConf);

        Optional<Pair<EventAndOrganizationId, Ticket>> result = passKitManager.validateToken(
                "event-name", "type-id", UUID.randomUUID().toString(), "ApplePass token");
        assertFalse(result.isPresent());
    }

    @Test
    void testValidateToken_TypeIdentifierMismatch() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        ConfigurationManager.MaybeConfiguration typeIdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER, "expected-type-id", true);
        when(configurationManager.getFor(eq(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER), any(ConfigurationLevel.class)))
                .thenReturn(typeIdConf);

        Optional<Pair<EventAndOrganizationId, Ticket>> result = passKitManager.validateToken(
                "event-name", "mismatched-type-id", UUID.randomUUID().toString(), "ApplePass token");
        assertFalse(result.isPresent());
    }

    @Test
    void testValidateToken_TicketNotFound() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        ConfigurationManager.MaybeConfiguration typeIdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER, "type-id", true);
        when(configurationManager.getFor(eq(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER), any(ConfigurationLevel.class)))
                .thenReturn(typeIdConf);

        String ticketUuid = UUID.randomUUID().toString();
        when(ticketRepository.findOptionalByUUID(ticketUuid)).thenReturn(Optional.empty());

        Optional<Pair<EventAndOrganizationId, Ticket>> result =
                passKitManager.validateToken("event-name", "type-id", ticketUuid, "ApplePass token");
        assertFalse(result.isPresent());
    }

    @Test
    void testValidateToken_EventIdMismatch() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        ConfigurationManager.MaybeConfiguration typeIdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER, "type-id", true);
        when(configurationManager.getFor(eq(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER), any(ConfigurationLevel.class)))
                .thenReturn(typeIdConf);

        String ticketUuid = UUID.randomUUID().toString();
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(2); // different event id

        when(ticketRepository.findOptionalByUUID(ticketUuid)).thenReturn(Optional.of(ticket));

        Optional<Pair<EventAndOrganizationId, Ticket>> result =
                passKitManager.validateToken("event-name", "type-id", ticketUuid, "ApplePass token");
        assertFalse(result.isPresent());
    }

    @Test
    void testValidateToken_TokenMismatch() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        ConfigurationManager.MaybeConfiguration typeIdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER, "type-id", true);
        when(configurationManager.getFor(eq(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER), any(ConfigurationLevel.class)))
                .thenReturn(typeIdConf);

        String ticketUuid = UUID.randomUUID().toString();
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getUuid()).thenReturn(ticketUuid);
        when(ticket.getTicketsReservationId()).thenReturn("res-id");

        when(ticketRepository.findOptionalByUUID(ticketUuid)).thenReturn(Optional.of(ticket));
        when(eventRepository.getPrivateKey(1)).thenReturn("private-key");

        Optional<Pair<EventAndOrganizationId, Ticket>> result =
                passKitManager.validateToken("event-name", "type-id", ticketUuid, "ApplePass mismatched-token");
        assertFalse(result.isPresent());
    }

    @Test
    void testValidateToken_Success() {
        EventAndOrganizationId event = new EventAndOrganizationId(1, 10);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("event-name"))
                .thenReturn(Optional.of(event));

        ConfigurationManager.MaybeConfiguration typeIdConf =
                mockMaybeConfig(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER, "type-id", true);
        when(configurationManager.getFor(eq(ConfigurationKeys.PASSBOOK_TYPE_IDENTIFIER), any(ConfigurationLevel.class)))
                .thenReturn(typeIdConf);

        String ticketUuid = UUID.randomUUID().toString();
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getUuid()).thenReturn(ticketUuid);
        when(ticket.getTicketsReservationId()).thenReturn("res-id");

        when(ticketRepository.findOptionalByUUID(ticketUuid)).thenReturn(Optional.of(ticket));
        when(eventRepository.getPrivateKey(1)).thenReturn("private-key");

        String correctToken = Ticket.hmacSHA256Base64("private-key", "1/res-id/" + ticketUuid);

        Optional<Pair<EventAndOrganizationId, Ticket>> result =
                passKitManager.validateToken("event-name", "type-id", ticketUuid, "ApplePass " + correctToken);
        assertTrue(result.isPresent());
        assertEquals(event, result.get().getLeft());
        assertEquals(ticket, result.get().getRight());
    }

    @Test
    void testWritePass_MissingConfiguration() throws Exception {
        EventAndOrganizationId eventAndOrg = new EventAndOrganizationId(1, 10);
        setupConfiguration(false, null, null, null, null, null);

        Ticket ticket = mock(Ticket.class);
        Organization organization = mock(Organization.class);
        when(organizationRepository.getById(10)).thenReturn(organization);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean success = passKitManager.writePass(ticket, eventAndOrg, out);
        assertFalse(success);
    }

    @Test
    void testWritePass_SuccessWithScaleLogoAndCustomValidity() throws Exception {
        EventAndOrganizationId eventAndOrg = new EventAndOrganizationId(1, 10);
        String keystoreBase64 = Base64.getEncoder().encodeToString("dummy-keystore".getBytes());
        setupConfiguration(true, "type-id", keystoreBase64, "password", "team-id", "alias");

        Ticket ticket = mock(Ticket.class);
        when(ticket.getUserLanguage()).thenReturn("en");
        when(ticket.getCategoryId()).thenReturn(100);
        when(ticket.getUuid()).thenReturn(UUID.randomUUID().toString());
        when(ticket.ticketCode(any(), anyBoolean())).thenReturn("ticket-code");

        Organization organization = mock(Organization.class);
        when(organization.getName()).thenReturn("Org Name");
        when(organization.getEmail()).thenReturn("org@example.com");
        when(organizationRepository.getById(10)).thenReturn(organization);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Zurich"));
        when(event.getBegin()).thenReturn(ZonedDateTime.now());
        when(event.getEnd()).thenReturn(ZonedDateTime.now().plusHours(2));
        when(event.getDisplayName()).thenReturn("Event Display Name");
        when(event.getLocation()).thenReturn("Event Location");
        when(event.getPrivateKey()).thenReturn("private-key");
        when(event.supportsQRCodeCaseInsensitive()).thenReturn(true);
        when(event.getLatitude()).thenReturn("47.3769");
        when(event.getLongitude()).thenReturn("8.5417");
        when(event.getFileBlobId()).thenReturn("logo-blob-id");
        when(event.getConfigurationLevel()).thenReturn(mock(ConfigurationLevel.class));

        when(eventRepository.findById(1)).thenReturn(event);

        when(eventDescriptionRepository.findDescriptionByEventIdTypeAndLocale(anyInt(), any(), anyString()))
                .thenReturn(Optional.of("Event Description Content"));

        TicketCategory category = mock(TicketCategory.class);
        when(category.getName()).thenReturn("Category Name");
        ZonedDateTime valStart = ZonedDateTime.now().plusMinutes(5);
        ZonedDateTime valEnd = ZonedDateTime.now().plusHours(1);
        when(category.getTicketValidityStart(any())).thenReturn(valStart);
        when(category.getTicketValidityEnd(any())).thenReturn(valEnd);
        when(ticketCategoryRepository.getById(100)).thenReturn(category);

        // Setup image metadata
        FileBlobMetadata metadata = mock(FileBlobMetadata.class);
        when(metadata.getContentType()).thenReturn("image/png");
        when(fileUploadManager.findMetadata("logo-blob-id")).thenReturn(Optional.of(metadata));

        // Setup dummy full size logo file
        File dummyLogoFile = File.createTempFile("dummy-logo", ".png");
        tempFilesToDelete.add(dummyLogoFile);
        BufferedImage image = new BufferedImage(160, 50, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(image, "png", dummyLogoFile);
        when(fileUploadManager.getFile("logo-blob-id")).thenReturn(dummyLogoFile);

        // Stub fileBlobCacheManager to return the result of executing supplier
        when(fileBlobCacheManager.getFile(anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<File> supplier = invocation.getArgument(2);
                    File scaled = supplier.get();
                    if (scaled != null) {
                        tempFilesToDelete.add(scaled);
                    }
                    return scaled;
                });

        try (MockedStatic<PassSerializer> passSerializerMock = mockStatic(PassSerializer.class);
                MockedStatic<PassSignerImpl> passSignerImplMock = mockStatic(PassSignerImpl.class)) {

            PassSignerImpl.Builder builderMock = mock(PassSignerImpl.Builder.class);
            when(builderMock.keystore(any(InputStream.class), anyString())).thenReturn(builderMock);
            when(builderMock.alias(anyString())).thenReturn(builderMock);
            when(builderMock.intermediateCertificate(any(InputStream.class))).thenReturn(builderMock);
            PassSigner signerMock = mock(PassSigner.class);
            when(builderMock.build()).thenReturn(signerMock);

            passSignerImplMock.when(PassSignerImpl::builder).thenReturn(builderMock);

            passSerializerMock
                    .when(() -> PassSerializer.writePkPassArchive(
                            any(Pass.class), any(PassSigner.class), any(OutputStream.class)))
                    .thenAnswer(invocation -> {
                        OutputStream os = invocation.getArgument(2);
                        os.write("mocked-pkpass-bytes".getBytes(StandardCharsets.UTF_8));
                        return null;
                    });

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean success = passKitManager.writePass(ticket, eventAndOrg, out);

            assertTrue(success);
            assertEquals("mocked-pkpass-bytes", out.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    void testWritePass_SuccessWithLogoScaleExceptionAndNullLocationsAndDefaultValidity() throws Exception {
        EventAndOrganizationId eventAndOrg = new EventAndOrganizationId(1, 10);
        String keystoreBase64 = Base64.getEncoder().encodeToString("dummy-keystore".getBytes());
        setupConfiguration(true, "type-id", keystoreBase64, "password", "team-id", "alias");

        Ticket ticket = mock(Ticket.class);
        when(ticket.getUserLanguage()).thenReturn("en");
        when(ticket.getCategoryId()).thenReturn(100);
        when(ticket.getUuid()).thenReturn(UUID.randomUUID().toString());
        when(ticket.ticketCode(any(), anyBoolean())).thenReturn("ticket-code");

        Organization organization = mock(Organization.class);
        when(organization.getName()).thenReturn("Org Name");
        when(organization.getEmail()).thenReturn("org@example.com");
        when(organizationRepository.getById(10)).thenReturn(organization);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Zurich"));
        when(event.getBegin()).thenReturn(ZonedDateTime.now());
        when(event.getEnd()).thenReturn(ZonedDateTime.now().plusHours(2));
        when(event.getDisplayName()).thenReturn("Event Display Name");
        when(event.getLocation()).thenReturn("Event Location");
        when(event.getPrivateKey()).thenReturn("private-key");
        when(event.supportsQRCodeCaseInsensitive()).thenReturn(true);
        when(event.getLatitude()).thenReturn(null); // null latitude
        when(event.getLongitude()).thenReturn(null); // null longitude
        when(event.getFileBlobId()).thenReturn("logo-blob-id");
        when(event.getConfigurationLevel()).thenReturn(mock(ConfigurationLevel.class));

        when(eventRepository.findById(1)).thenReturn(event);

        when(eventDescriptionRepository.findDescriptionByEventIdTypeAndLocale(anyInt(), any(), anyString()))
                .thenReturn(Optional.empty()); // empty description

        TicketCategory category = mock(TicketCategory.class);
        when(category.getName()).thenReturn("Category Name");
        when(category.getTicketValidityStart(any())).thenReturn(null); // triggers default validity
        when(category.getTicketValidityEnd(any())).thenReturn(null);
        when(ticketCategoryRepository.getById(100)).thenReturn(category);

        // Setup image metadata
        FileBlobMetadata metadata = mock(FileBlobMetadata.class);
        when(metadata.getContentType()).thenReturn("image/jpeg");
        when(fileUploadManager.findMetadata("logo-blob-id")).thenReturn(Optional.of(metadata));

        // Return a non-existent file, which will cause ImageIO.read to throw IOException
        File nonExistentFile =
                new File(System.getProperty("java.io.tmpdir"), "non-existent-logo-" + UUID.randomUUID() + ".png");
        when(fileUploadManager.getFile("logo-blob-id")).thenReturn(nonExistentFile);

        // Stub fileBlobCacheManager to execute and propagate the exception
        when(fileBlobCacheManager.getFile(anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<File> supplier = invocation.getArgument(2);
                    return supplier.get(); // this will throw IllegalStateException (wrapping IOException)
                });

        try (MockedStatic<PassSerializer> passSerializerMock = mockStatic(PassSerializer.class);
                MockedStatic<PassSignerImpl> passSignerImplMock = mockStatic(PassSignerImpl.class)) {

            PassSignerImpl.Builder builderMock = mock(PassSignerImpl.Builder.class);
            when(builderMock.keystore(any(InputStream.class), anyString())).thenReturn(builderMock);
            when(builderMock.alias(anyString())).thenReturn(builderMock);
            when(builderMock.intermediateCertificate(any(InputStream.class))).thenReturn(builderMock);
            PassSigner signerMock = mock(PassSigner.class);
            when(builderMock.build()).thenReturn(signerMock);

            passSignerImplMock.when(PassSignerImpl::builder).thenReturn(builderMock);

            passSerializerMock
                    .when(() -> PassSerializer.writePkPassArchive(
                            any(Pass.class), any(PassSigner.class), any(OutputStream.class)))
                    .thenAnswer(invocation -> {
                        OutputStream os = invocation.getArgument(2);
                        os.write("mocked-pkpass-bytes".getBytes(StandardCharsets.UTF_8));
                        return null;
                    });

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean success = passKitManager.writePass(ticket, eventAndOrg, out);

            assertTrue(success);
            assertEquals("mocked-pkpass-bytes", out.toString(StandardCharsets.UTF_8));
        }
    }

    @Test
    void testGetPass_SkipPassbook() {
        Map<String, String> model = new HashMap<>();
        model.put(Mailer.SKIP_PASSBOOK, "true");
        byte[] result = passKitManager.getPass(model);
        assertNull(result);
    }

    @Test
    void testGetPass_ExceptionCatchAll() {
        Map<String, String> model = new HashMap<>();
        byte[] result = passKitManager.getPass(model);
        assertNull(result);
    }

    @Test
    void testGetPass_MissingConfigKeys() {
        Map<String, String> model = new HashMap<>();
        model.put(
                "ticket",
                "{\"id\":1,\"uuid\":\"ticket-uuid\",\"categoryId\":100,\"eventId\":1,\"status\":\"ACQUIRED\",\"ticketsReservationId\":\"res-id\",\"userLanguage\":\"en\",\"email\":\"test@example.com\",\"fullName\":\"Test User\"}");
        model.put("organizationId", "10");

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getConfigurationLevel()).thenReturn(mock(ConfigurationLevel.class));
        when(eventRepository.findById(1)).thenReturn(event);

        Organization organization = mock(Organization.class);
        when(organizationRepository.getById(10)).thenReturn(organization);

        setupConfiguration(false, null, null, null, null, null);

        byte[] result = passKitManager.getPass(model);
        assertNull(result);
    }

    @Test
    void testGetPass_Success() throws Exception {
        Map<String, String> model = new HashMap<>();
        model.put(
                "ticket",
                "{\"id\":1,\"uuid\":\"ticket-uuid\",\"categoryId\":100,\"eventId\":1,\"status\":\"ACQUIRED\",\"ticketsReservationId\":\"res-id\",\"userLanguage\":\"en\",\"email\":\"test@example.com\",\"fullName\":\"Test User\"}");
        model.put("organizationId", "10");

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(1);
        when(event.getZoneId()).thenReturn(ZoneId.of("Europe/Zurich"));
        when(event.getBegin()).thenReturn(ZonedDateTime.now());
        when(event.getEnd()).thenReturn(ZonedDateTime.now().plusHours(2));
        when(event.getDisplayName()).thenReturn("Event Display Name");
        when(event.getLocation()).thenReturn("Event Location");
        when(event.getPrivateKey()).thenReturn("private-key");
        when(event.supportsQRCodeCaseInsensitive()).thenReturn(true);
        when(event.getLatitude()).thenReturn(null);
        when(event.getLongitude()).thenReturn(null);
        when(event.getFileBlobId()).thenReturn(null);
        when(event.getConfigurationLevel()).thenReturn(mock(ConfigurationLevel.class));

        when(eventRepository.findById(1)).thenReturn(event);

        Organization organization = mock(Organization.class);
        when(organization.getName()).thenReturn("Org Name");
        when(organization.getEmail()).thenReturn("org@example.com");
        when(organizationRepository.getById(10)).thenReturn(organization);

        String keystoreBase64 = Base64.getEncoder().encodeToString("dummy-keystore".getBytes());
        setupConfiguration(true, "type-id", keystoreBase64, "password", "team-id", "alias");

        when(eventDescriptionRepository.findDescriptionByEventIdTypeAndLocale(anyInt(), any(), anyString()))
                .thenReturn(Optional.empty());

        TicketCategory category = mock(TicketCategory.class);
        when(category.getName()).thenReturn("Category Name");
        when(category.getTicketValidityStart(any())).thenReturn(null);
        when(category.getTicketValidityEnd(any())).thenReturn(null);
        when(ticketCategoryRepository.getById(100)).thenReturn(category);

        try (MockedStatic<PassSerializer> passSerializerMock = mockStatic(PassSerializer.class);
                MockedStatic<PassSignerImpl> passSignerImplMock = mockStatic(PassSignerImpl.class)) {

            PassSignerImpl.Builder builderMock = mock(PassSignerImpl.Builder.class);
            when(builderMock.keystore(any(InputStream.class), anyString())).thenReturn(builderMock);
            when(builderMock.alias(anyString())).thenReturn(builderMock);
            when(builderMock.intermediateCertificate(any(InputStream.class))).thenReturn(builderMock);
            PassSigner signerMock = mock(PassSigner.class);
            when(builderMock.build()).thenReturn(signerMock);

            passSignerImplMock.when(PassSignerImpl::builder).thenReturn(builderMock);

            passSerializerMock
                    .when(() -> PassSerializer.writePkPassArchive(
                            any(Pass.class), any(PassSigner.class), any(OutputStream.class)))
                    .thenAnswer(invocation -> {
                        OutputStream os = invocation.getArgument(2);
                        os.write("mocked-pkpass-bytes-from-get-pass".getBytes(StandardCharsets.UTF_8));
                        return null;
                    });

            byte[] result = passKitManager.getPass(model);
            assertNotNull(result);
            assertEquals("mocked-pkpass-bytes-from-get-pass", new String(result, StandardCharsets.UTF_8));
        }
    }
}
