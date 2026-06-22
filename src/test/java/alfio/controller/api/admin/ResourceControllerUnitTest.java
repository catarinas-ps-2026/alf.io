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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import alfio.manager.*;
import alfio.manager.i18n.MessageSourceManager;
import alfio.model.UploadedResource;
import alfio.model.modification.UploadBase64FileModification;
import alfio.repository.EventRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.util.ClockProvider;
import alfio.util.TemplateManager;
import alfio.util.TemplateResource;
import com.samskivert.mustache.MustacheException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResourceControllerUnitTest {

    // ---- Mocks ----
    private UploadedResourceManager uploadedResourceManager;
    private EventRepository eventRepository;
    private MessageSourceManager messageSourceManager;
    private TemplateManager templateManager;
    private OrganizationRepository organizationRepository;
    private FileUploadManager fileUploadManager;
    private ExtensionManager extensionManager;
    private ClockProvider clockProvider;
    private SubscriptionManager subscriptionManager;
    private AccessService accessService;

    // ---- Subject under test ----
    private ResourceController controller;

    // ---- Shared test fixtures ----
    private Principal principal;

    @BeforeEach
    void setUp() {
        uploadedResourceManager = mock(UploadedResourceManager.class);
        eventRepository = mock(EventRepository.class);
        messageSourceManager = mock(MessageSourceManager.class);
        templateManager = mock(TemplateManager.class);
        organizationRepository = mock(OrganizationRepository.class);
        fileUploadManager = mock(FileUploadManager.class);
        extensionManager = mock(ExtensionManager.class);
        clockProvider = mock(ClockProvider.class);
        subscriptionManager = mock(SubscriptionManager.class);
        accessService = mock(AccessService.class);

        controller = new ResourceController(
                uploadedResourceManager,
                eventRepository,
                messageSourceManager,
                templateManager,
                organizationRepository,
                fileUploadManager,
                extensionManager,
                clockProvider,
                subscriptionManager,
                accessService);

        principal = mock(Principal.class);
        when(principal.getName()).thenReturn("admin");
    }

    // =========================================================================
    // getOverridableTemplates
    // =========================================================================

    @Test
    void getOverridableTemplates_returnsOnlyOverridableTemplates() {
        List<TemplateResource> result = controller.getOverridableTemplates();

        assertNotNull(result);
        assertFalse(result.isEmpty(), "There should be at least one overridable template");
        assertTrue(
                result.stream().allMatch(TemplateResource::overridable),
                "Every returned template must have overridable=true");
    }

    // =========================================================================
    // findAll  — global scope (GET /admin/api/resource)
    // =========================================================================

    @Test
    void findAll_global_checksAdminAndDelegatesToManager() {
        List<UploadedResource> expected = List.of(mock(UploadedResource.class));
        when(uploadedResourceManager.findAll()).thenReturn(expected);

        List<UploadedResource> result = controller.findAll(principal);

        verify(accessService).ensureAdmin(principal);
        verify(uploadedResourceManager).findAll();
        assertEquals(expected, result);
    }

    // =========================================================================
    // findAllForOrganization  — org scope
    // =========================================================================

    @Test
    void findAllForOrganization_checksOwnershipAndDelegatesToManager() {
        int orgId = 42;
        List<UploadedResource> expected = List.of(mock(UploadedResource.class));
        when(uploadedResourceManager.findAll(orgId)).thenReturn(expected);

        List<UploadedResource> result = controller.findAllForOrganization(orgId, principal);

        verify(accessService).checkOrganizationOwnership(principal, orgId);
        verify(uploadedResourceManager).findAll(orgId);
        assertEquals(expected, result);
    }

    // =========================================================================
    // findAllForEvent  — event scope
    // =========================================================================

    @Test
    void findAllForEvent_checksOwnershipAndDelegatesToManager() {
        int orgId = 42;
        int eventId = 7;
        List<UploadedResource> expected = List.of(mock(UploadedResource.class));
        when(uploadedResourceManager.findAll(orgId, eventId)).thenReturn(expected);

        List<UploadedResource> result = controller.findAllForEvent(orgId, eventId, principal);

        verify(accessService).checkEventOwnership(principal, eventId, orgId);
        verify(uploadedResourceManager).findAll(orgId, eventId);
        assertEquals(expected, result);
    }

    // =========================================================================
    // getMetadata — global scope (GET /admin/api/resource/{name}/metadata)
    // =========================================================================

    @Test
    void getMetadata_global_resourceExists_returns200WithBody() {
        String name = "logo.png";
        UploadedResource resource = mock(UploadedResource.class);
        when(uploadedResourceManager.hasResource(name)).thenReturn(true);
        when(uploadedResourceManager.get(name)).thenReturn(resource);

        ResponseEntity<UploadedResource> response = controller.getMetadata(name, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resource, response.getBody());
        verify(accessService).ensureAdmin(principal);
    }

    @Test
    void getMetadata_global_resourceNotFound_returns404() {
        String name = "missing.png";
        when(uploadedResourceManager.hasResource(name)).thenReturn(false);

        ResponseEntity<UploadedResource> response = controller.getMetadata(name, principal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(accessService).ensureAdmin(principal);
        verify(uploadedResourceManager, never()).get(name);
    }

    // =========================================================================
    // getMetadata — org scope
    // =========================================================================

    @Test
    void getMetadata_org_resourceExists_returns200WithBody() {
        int orgId = 42;
        String name = "banner.png";
        UploadedResource resource = mock(UploadedResource.class);
        when(uploadedResourceManager.hasResource(orgId, name)).thenReturn(true);
        when(uploadedResourceManager.get(orgId, name)).thenReturn(resource);

        ResponseEntity<UploadedResource> response = controller.getMetadata(orgId, name, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resource, response.getBody());
        verify(accessService).checkOrganizationOwnership(principal, orgId);
    }

    @Test
    void getMetadata_org_resourceNotFound_returns404() {
        int orgId = 42;
        String name = "missing.png";
        when(uploadedResourceManager.hasResource(orgId, name)).thenReturn(false);

        ResponseEntity<UploadedResource> response = controller.getMetadata(orgId, name, principal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(accessService).checkOrganizationOwnership(principal, orgId);
    }

    // =========================================================================
    // getMetadata — event scope
    // =========================================================================

    @Test
    void getMetadata_event_resourceExists_returns200WithBody() {
        int orgId = 42;
        int eventId = 7;
        String name = "ticket-bg.png";
        UploadedResource resource = mock(UploadedResource.class);
        when(uploadedResourceManager.hasResource(orgId, eventId, name)).thenReturn(true);
        when(uploadedResourceManager.get(orgId, eventId, name)).thenReturn(resource);

        ResponseEntity<UploadedResource> response = controller.getMetadata(orgId, eventId, name, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resource, response.getBody());
        verify(accessService).checkEventOwnership(principal, eventId, orgId);
    }

    @Test
    void getMetadata_event_resourceNotFound_returns404() {
        int orgId = 42;
        int eventId = 7;
        String name = "missing.png";
        when(uploadedResourceManager.hasResource(orgId, eventId, name)).thenReturn(false);

        ResponseEntity<UploadedResource> response = controller.getMetadata(orgId, eventId, name, principal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // =========================================================================
    // uploadFile — global scope (POST /admin/api/resource)
    // =========================================================================

    @Test
    void uploadFile_global_success_delegatesToManager() {
        UploadBase64FileModification upload = new UploadBase64FileModification();
        upload.setName("logo.png");
        when(uploadedResourceManager.saveResource(upload)).thenReturn(Optional.of(1));

        assertDoesNotThrow(() -> controller.uploadFile(upload, principal));

        verify(accessService).ensureAdmin(principal);
        verify(uploadedResourceManager).saveResource(upload);
    }

    @Test
    void uploadFile_global_saveReturnsEmpty_throwsIllegalArgumentException() {
        UploadBase64FileModification upload = new UploadBase64FileModification();
        upload.setName("invalid.png");
        when(uploadedResourceManager.saveResource(upload)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.uploadFile(upload, principal));

        verify(accessService).ensureAdmin(principal);
    }

    // =========================================================================
    // uploadFile — org scope (POST /admin/api/resource-organization/{orgId})
    // =========================================================================

    @Test
    void uploadFile_org_success_delegatesToManager() {
        int orgId = 42;
        UploadBase64FileModification upload = new UploadBase64FileModification();
        upload.setName("org-logo.png");
        when(uploadedResourceManager.saveResource(orgId, upload)).thenReturn(Optional.of(2));

        assertDoesNotThrow(() -> controller.uploadFile(orgId, upload, principal));

        verify(accessService).checkOrganizationOwnership(principal, orgId);
        verify(uploadedResourceManager).saveResource(orgId, upload);
    }

    @Test
    void uploadFile_org_saveReturnsEmpty_throwsIllegalArgumentException() {
        int orgId = 42;
        UploadBase64FileModification upload = new UploadBase64FileModification();
        upload.setName("bad-file.png");
        when(uploadedResourceManager.saveResource(orgId, upload)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.uploadFile(orgId, upload, principal));
    }

    // =========================================================================
    // uploadFile — event scope (POST /admin/api/resource-event/{orgId}/{eventId})
    // =========================================================================

    @Test
    void uploadFile_event_success_delegatesToManager() {
        int orgId = 42;
        int eventId = 7;
        UploadBase64FileModification upload = new UploadBase64FileModification();
        upload.setName("event-banner.png");
        when(uploadedResourceManager.saveResource(orgId, eventId, upload)).thenReturn(Optional.of(3));

        assertDoesNotThrow(() -> controller.uploadFile(orgId, eventId, upload, principal));

        verify(accessService).checkEventOwnership(principal, eventId, orgId);
        verify(uploadedResourceManager).saveResource(orgId, eventId, upload);
    }

    @Test
    void uploadFile_event_saveReturnsEmpty_throwsIllegalArgumentException() {
        int orgId = 42;
        int eventId = 7;
        UploadBase64FileModification upload = new UploadBase64FileModification();
        upload.setName("bad.png");
        when(uploadedResourceManager.saveResource(orgId, eventId, upload)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.uploadFile(orgId, eventId, upload, principal));
    }

    // =========================================================================
    // delete — global scope (DELETE /admin/api/resource/{name})
    // =========================================================================

    @Test
    void delete_global_checksAdminAndDelegatesToManager() {
        String name = "logo.png";

        controller.delete(name, principal);

        verify(accessService).ensureAdmin(principal);
        verify(uploadedResourceManager).deleteResource(name);
    }

    // =========================================================================
    // delete — org scope (DELETE /admin/api/resource-organization/{orgId}/{name})
    // =========================================================================

    @Test
    void delete_org_checksOwnershipAndDelegatesToManager() {
        int orgId = 42;
        String name = "org-logo.png";

        controller.delete(orgId, name, principal);

        verify(accessService).checkOrganizationOwnership(principal, orgId);
        verify(uploadedResourceManager).deleteResource(orgId, name);
    }

    // =========================================================================
    // delete — event scope (DELETE /admin/api/resource-event/{orgId}/{eventId}/{name})
    // =========================================================================

    @Test
    void delete_event_checksOwnershipAndDelegatesToManager() {
        int orgId = 42;
        int eventId = 7;
        String name = "event-bg.png";

        controller.delete(orgId, eventId, name, principal);

        verify(accessService).checkEventOwnership(principal, eventId, orgId);
        verify(uploadedResourceManager).deleteResource(orgId, eventId, name);
    }

    // =========================================================================
    // handleSyntaxError — @ExceptionHandler
    // =========================================================================

    @Test
    void handleSyntaxError_withMustacheException_returnsExceptionMessage() {
        // MustacheException wraps a cause; the handler checks if cause is MustacheException
        MustacheException cause = new MustacheException("template syntax error");
        Exception wrapped = new Exception("outer", cause);

        String result = controller.handleSyntaxError(wrapped);

        assertEquals("template syntax error", result);
    }

    @Test
    void handleSyntaxError_withGenericException_returnsFallbackMessage() {
        Exception generic = new RuntimeException("unrelated error");

        String result = controller.handleSyntaxError(generic);

        assertEquals("Something went wrong. Please check the syntax and retry", result);
    }

    @Test
    void handleSyntaxError_withNullCause_returnsFallbackMessage() {
        // Exception with no cause should also fall back to the default message
        Exception noCause = new RuntimeException("no cause");

        String result = controller.handleSyntaxError(noCause);

        assertEquals("Something went wrong. Please check the syntax and retry", result);
    }
}
