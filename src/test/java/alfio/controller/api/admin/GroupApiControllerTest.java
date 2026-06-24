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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import alfio.manager.AccessService;
import alfio.manager.EventManager;
import alfio.manager.GroupManager;
import alfio.manager.GroupManager.DuplicateGroupItemException;
import alfio.model.EventAndOrganizationId;
import alfio.model.group.Group;
import alfio.model.group.LinkedGroup;
import alfio.model.modification.GroupModification;
import alfio.model.modification.LinkedGroupModification;
import alfio.model.result.ErrorCode;
import alfio.model.result.Result;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupApiControllerTest {

    @Mock
    private GroupManager groupManager;

    @Mock
    private EventManager eventManager;

    @Mock
    private AccessService accessService;

    @Mock
    private Principal principal;

    private GroupApiController controller;

    @BeforeEach
    void setUp() {
        controller = new GroupApiController(groupManager, eventManager, accessService);
    }

    @Test
    void loadAllGroupsForOrganization_showAll_true_returnsAllGroups() {
        List<Group> allGroups = new ArrayList<>();
        when(groupManager.getAllForOrganization(1)).thenReturn(allGroups);

        var result = controller.loadAllGroupsForOrganization(1, true, principal);

        verify(accessService).checkOrganizationOwnership(principal, 1);
        verify(groupManager).getAllForOrganization(1);
        assertTrue(result.hasBody());
    }

    @Test
    void loadAllGroupsForOrganization_showAll_false_returnsActiveGroups() {
        List<Group> activeGroups = new ArrayList<>();
        when(groupManager.getAllActiveForOrganization(1)).thenReturn(activeGroups);

        var result = controller.loadAllGroupsForOrganization(1, false, principal);

        verify(accessService).checkOrganizationOwnership(principal, 1);
        verify(groupManager).getAllActiveForOrganization(1);
        assertTrue(result.hasBody());
    }

    @Test
    void loadAllGroupsForOrganization_defaultShowAll_returnActiveGroups() {
        List<Group> activeGroups = new ArrayList<>();
        when(groupManager.getAllActiveForOrganization(1)).thenReturn(activeGroups);

        var result = controller.loadAllGroupsForOrganization(1, false, principal);

        verify(groupManager).getAllActiveForOrganization(1);
        verify(groupManager, never()).getAllForOrganization(1);
    }

    @Test
    void loadDetail_withValidListId_returnsGroupModification() {
        GroupModification modification = new GroupModification(1, "Test Group", "Test Description", 1, List.of());
        when(groupManager.loadComplete(1)).thenReturn(Optional.of(modification));

        var result = controller.loadDetail(1, 1, principal);

        verify(accessService).checkGroupOwnership(principal, 1, 1);
        verify(groupManager).loadComplete(1);
        assertTrue(result.hasBody());
    }

    @Test
    void loadDetail_withNonExistentListId_returnsNotFound() {
        when(groupManager.loadComplete(1)).thenReturn(Optional.empty());

        var result = controller.loadDetail(1, 1, principal);

        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void updateGroup_withValidData_updatesAndReturns() {
        GroupModification modification = new GroupModification(1, "Test Group", "Test Description", 1, List.of());
        when(groupManager.update(1, modification)).thenReturn(Optional.of(modification));

        var result = controller.updateGroup(1, 1, modification, principal);

        verify(accessService).checkGroupUpdateRequest(principal, 1, 1, modification);
        verify(groupManager).update(1, modification);
        assertTrue(result.hasBody());
    }

    @Test
    void updateGroup_withNonExistentGroup_returnsNotFound() {
        GroupModification modification = new GroupModification(1, "Test Group", "Test Description", 1, List.of());
        when(groupManager.update(1, modification)).thenReturn(Optional.empty());

        var result = controller.updateGroup(1, 1, modification, principal);

        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void createNew_withMatchingOrganizationId_succeeds() {
        GroupModification request = new GroupModification(1, null, null, 1, List.of());
        Result<Integer> successResult = Result.success(1);
        when(groupManager.createNew(request)).thenReturn(successResult);

        var result = controller.createNew(1, request, principal);

        verify(accessService).checkGroupCreateRequest(principal, 1, request);
        verify(groupManager).createNew(request);
        assertTrue(result.hasBody());
        assertEquals("1", result.getBody());
    }

    @Test
    void createNew_withMismatchingOrganizationId_returnsBadRequest() {
        GroupModification request = new GroupModification(1, null, null, 2, List.of());

        var result = controller.createNew(1, request, principal);

        assertTrue(result.getStatusCode().is4xxClientError());
        verify(groupManager, never()).createNew(request);
    }

    @Test
    void createNew_withDuplicateError_returnsBadRequestWithMessage() {
        GroupModification request = new GroupModification(1, null, null, 1, List.of());

        ErrorCode error = ErrorCode.custom("value.duplicate", "Duplicate value");
        Result<Integer> failResult = Result.error(error);
        when(groupManager.createNew(request)).thenReturn(failResult);

        var result = controller.createNew(1, request, principal);

        assertTrue(result.getStatusCode().is4xxClientError());
        assertEquals("Duplicate value", result.getBody());
    }

    @Test
    void createNew_withOtherError_returnsBadRequest() {
        GroupModification request = new GroupModification(1, null, null, 1, List.of());

        ErrorCode error = ErrorCode.custom("other.error", "Some error");
        Result<Integer> failResult = Result.error(error);
        when(groupManager.createNew(request)).thenReturn(failResult);

        var result = controller.createNew(1, request, principal);

        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void findLinked_withValidEvent_returnsLinkedGroups() {
        when(principal.getName()).thenReturn("user");
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.of(event));
        List<LinkedGroup> linkedGroups = new ArrayList<>();
        when(groupManager.getLinksForEvent(1)).thenReturn(linkedGroups);

        var result = controller.findLinked("event-name", principal);

        verify(accessService).checkEventMembership(principal, "event-name", AccessService.MEMBERSHIP_ROLES);
        verify(groupManager).getLinksForEvent(1);
        assertTrue(result.hasBody());
    }

    @Test
    void findLinked_withNonExistentEvent_returnsNotFound() {
        when(principal.getName()).thenReturn("user");
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.empty());

        var result = controller.findLinked("event-name", principal);

        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void findActiveGroup_withNoCategory_returnsActiveGroup() {
        when(principal.getName()).thenReturn("user");
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.of(event));
        LinkedGroup linkedGroup =
                new LinkedGroup(1, 1, 1, null, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        when(groupManager.getLinksForEvent(1)).thenReturn(List.of(linkedGroup));

        var result = controller.findActiveGroup("event-name", principal);

        verify(accessService).checkEventOwnership(principal, "event-name");
        assertTrue(result.hasBody());
    }

    @Test
    void findActiveGroup_withNoCategoryAndNoLink_returnsNoContent() {
        when(principal.getName()).thenReturn("user");
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.of(event));
        when(groupManager.getLinksForEvent(1)).thenReturn(new ArrayList<>());

        var result = controller.findActiveGroup("event-name", principal);

        assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void findActiveGroup_withNonExistentEvent_returnsNotFound() {
        when(principal.getName()).thenReturn("user");
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.empty());

        var result = controller.findActiveGroup("event-name", principal);

        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void findActiveGroup_withCategory_returnsActiveGroupForCategory() {
        when(principal.getName()).thenReturn("user");
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.of(event));
        LinkedGroup linkedGroup =
                new LinkedGroup(1, 1, 1, 5, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        when(groupManager.findLinks(1, 5)).thenReturn(List.of(linkedGroup));

        var result = controller.findActiveGroup("event-name", 5, principal);

        verify(accessService).checkCategoryOwnership(principal, "event-name", 5);
        assertTrue(result.hasBody());
    }

    @Test
    void findActiveGroup_withCategoryAndNoLink_returnsNoContent() {
        when(principal.getName()).thenReturn("user");
        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.of(event));
        when(groupManager.findLinks(1, 5)).thenReturn(new ArrayList<>());

        var result = controller.findActiveGroup("event-name", 5, principal);

        assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    @Test
    void findActiveGroup_withCategoryAndNonExistentEvent_returnsNotFound() {
        when(principal.getName()).thenReturn("user");
        when(eventManager.getOptionalEventAndOrganizationIdByName("event-name", "user"))
                .thenReturn(Optional.empty());

        var result = controller.findActiveGroup("event-name", 5, principal);

        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void linkGroup_withMatchingGroupId_createsNewLink() {
        LinkedGroupModification body = new LinkedGroupModification(null, 1, 1, null, null, null, null);

        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(accessService.checkEventOwnership(principal, 1)).thenReturn(event);
        when(groupManager.getLinksForEvent(1)).thenReturn(new ArrayList<>());
        LinkedGroup linkedGroup =
                new LinkedGroup(1, 1, 1, null, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        when(groupManager.createLink(1, 1, body)).thenReturn(linkedGroup);

        var result = controller.linkGroup(1, body, principal);

        verify(groupManager).createLink(1, 1, body);
        assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    void linkGroup_withMismatchingGroupId_returnsBadRequest() {
        LinkedGroupModification body = new LinkedGroupModification(null, 2, 0, null, null, null, null);

        var result = controller.linkGroup(1, body, principal);

        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void linkGroup_withNullBody_returnsBadRequest() {

        var result = controller.linkGroup(1, null, principal);

        assertTrue(result.getStatusCode().is4xxClientError());
    }

    @Test
    void linkGroup_withExistingLink_updatesLink() {
        LinkedGroupModification body = new LinkedGroupModification(null, 1, 1, 5, null, null, null);

        EventAndOrganizationId event = new EventAndOrganizationId(1, 1);
        when(accessService.checkCategoryOwnership(principal, 1, 5)).thenReturn(event);
        LinkedGroup existingLink =
                new LinkedGroup(10, 1, 1, 5, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);

        when(groupManager.getLinksForEvent(1)).thenReturn(List.of(existingLink));
        when(groupManager.updateLink(10, body)).thenReturn(existingLink);

        var result = controller.linkGroup(1, body, principal);

        verify(groupManager).updateLink(10, body);
    }

    @Test
    void unlinkGroup_withValidData_disablesLink() {

        var result = controller.unlinkGroup(1, 1, 1, 5, principal);

        verify(accessService).checkGroupLinkOwnership(principal, 1, 1, 1, 5);
        verify(groupManager).disableLink(1);
        assertEquals("OK", result.getBody());
    }

    @Test
    void unlinkGroup_withoutCategory_disablesLink() {

        var result = controller.unlinkGroup(1, 1, 1, null, principal);

        verify(accessService).checkGroupLinkOwnership(principal, 1, 1, 1, null);
        verify(groupManager).disableLink(1);
    }

    @Test
    void deactivateMember_withValidMemberId_deactivates() {
        when(groupManager.deactivateMembers(List.of(1), 1)).thenReturn(true);

        var result = controller.deactivateMember(1, 1, 1, principal);

        verify(accessService).checkGroupOwnership(principal, 1, 1);
        verify(groupManager).deactivateMembers(List.of(1), 1);
        assertTrue(result.hasBody());
        assertTrue(result.getBody());
    }

    @Test
    void deactivateGroup_withValidGroupId_deactivates() {
        when(groupManager.deactivateGroup(1)).thenReturn(true);

        var result = controller.deactivateGroup(1, 1, principal);

        verify(accessService).checkGroupOwnership(principal, 1, 1);
        verify(groupManager).deactivateGroup(1);
        assertTrue(result.hasBody());
        assertTrue(result.getBody());
    }

    @Test
    void exceptionHandler_handlesDuplicateGroupItemException() {
        DuplicateGroupItemException exception = new DuplicateGroupItemException("Duplicate item");

        String result = controller.handleDuplicateGroupItemException(exception);

        assertEquals("Duplicate item", result);
    }
}
