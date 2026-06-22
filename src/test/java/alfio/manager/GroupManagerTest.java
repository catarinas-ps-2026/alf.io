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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.model.Audit;
import alfio.model.Ticket;
import alfio.model.group.Group;
import alfio.model.group.GroupMember;
import alfio.model.group.LinkedGroup;
import alfio.model.modification.GroupMemberModification;
import alfio.model.modification.GroupModification;
import alfio.model.modification.LinkedGroupModification;
import alfio.model.result.Result;
import alfio.repository.AuditingRepository;
import alfio.repository.GroupRepository;
import alfio.repository.TicketRepository;
import ch.digitalfondue.npjt.AffectedRowCountAndKey;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

public class GroupManagerTest {

    private GroupRepository groupRepository;
    private TicketRepository ticketRepository;
    private AuditingRepository auditingRepository;
    private PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    private GroupManager groupManager;

    @BeforeEach
    public void setUp() {
        groupRepository = mock(GroupRepository.class);
        ticketRepository = mock(TicketRepository.class);
        auditingRepository = mock(AuditingRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        transactionStatus = mock(TransactionStatus.class);

        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        groupManager = new GroupManager(groupRepository, ticketRepository, auditingRepository, transactionManager);
    }

    @Test
    public void testCreateNewSuccess() {
        GroupModification input = new GroupModification(
                null,
                "Test Group",
                "Test Description",
                1,
                Collections.singletonList(new GroupMemberModification(null, "member1@test.com", "Member 1")));

        Group mockGroup = new Group(42, "Test Group", "Test Description", 1, true);
        when(groupRepository.insert(anyString(), anyString(), anyInt()))
                .thenReturn(new AffectedRowCountAndKey<>(1, 42));
        when(groupRepository.getById(42)).thenReturn(mockGroup);
        when(groupRepository.insert(eq(42), anyList())).thenReturn(new int[] {1});

        Result<Integer> result = groupManager.createNew(input);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData());
        verify(transactionStatus, never()).setRollbackOnly();
    }

    @Test
    public void testCreateNewDuplicateMembers() {
        List<GroupMemberModification> members = Arrays.asList(
                new GroupMemberModification(null, "dup@test.com", "Member 1"),
                new GroupMemberModification(null, "dup@test.com", "Member 2"));
        GroupModification input = new GroupModification(null, "Test Group", "Test Description", 1, members);

        Group mockGroup = new Group(42, "Test Group", "Test Description", 1, true);
        when(groupRepository.insert(anyString(), anyString(), anyInt()))
                .thenReturn(new AffectedRowCountAndKey<>(1, 42));
        when(groupRepository.getById(42)).thenReturn(mockGroup);

        Result<Integer> result = groupManager.createNew(input);
        assertFalse(result.isSuccess());
        verify(transactionStatus, times(1)).setRollbackOnly();
    }

    @Test
    public void testCreateLinkGroupNotFound() {
        LinkedGroupModification modification = new LinkedGroupModification(
                null, 1, 10, null, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        when(groupRepository.getById(1)).thenReturn(null);

        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            groupManager.createLink(1, 10, modification);
        });
        assertEquals("Group not found", exception.getMessage());
    }

    @Test
    public void testCreateLinkMissingMaxAllocation() {
        LinkedGroupModification modification = new LinkedGroupModification(
                null, 1, 10, null, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, null);
        Group mockGroup = new Group(1, "Group Name", "Description", 1, true);
        when(groupRepository.getById(1)).thenReturn(mockGroup);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            groupManager.createLink(1, 10, modification);
        });
        assertEquals("Missing max allocation", exception.getMessage());
    }

    @Test
    public void testCreateLinkSuccess() {
        LinkedGroupModification modification = new LinkedGroupModification(
                null, 1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 5);
        Group mockGroup = new Group(1, "Group Name", "Description", 1, true);
        when(groupRepository.getById(1)).thenReturn(mockGroup);

        when(groupRepository.createConfiguration(
                        1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 5))
                .thenReturn(new AffectedRowCountAndKey<>(1, 100));

        LinkedGroup mockLinkedGroup =
                new LinkedGroup(100, 1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 5);
        when(groupRepository.getConfiguration(100)).thenReturn(mockLinkedGroup);

        LinkedGroup result = groupManager.createLink(1, 10, modification);
        assertNotNull(result);
        assertEquals(100, result.getId());
    }

    @Test
    public void testUpdateLinkCleanStateNotRequired() {
        LinkedGroupModification modification = new LinkedGroupModification(
                100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        LinkedGroup original =
                new LinkedGroup(100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);

        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(original);
        when(groupRepository.updateConfiguration(
                        100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null))
                .thenReturn(1);
        when(groupRepository.getConfiguration(100)).thenReturn(original);

        LinkedGroup result = groupManager.updateLink(100, modification);
        assertNotNull(result);
    }

    @Test
    public void testUpdateLinkCleanStateRequiredAndNoConfirmedTickets() {
        // Original has UNLIMITED, modification has LIMITED_QUANTITY -> requires clean state
        LinkedGroupModification modification = new LinkedGroupModification(
                100, 1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 5);
        LinkedGroup original =
                new LinkedGroup(100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);

        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(original);
        when(groupRepository.countWhitelistedTicketsForConfiguration(100)).thenReturn(0);
        when(groupRepository.updateConfiguration(
                        100, 1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 5))
                .thenReturn(1);
        when(groupRepository.getConfiguration(100)).thenReturn(original);

        LinkedGroup result = groupManager.updateLink(100, modification);
        assertNotNull(result);
    }

    @Test
    public void testUpdateLinkCleanStateRequiredAndHasConfirmedTickets() {
        // Original has UNLIMITED, modification has LIMITED_QUANTITY -> requires clean state
        LinkedGroupModification modification = new LinkedGroupModification(
                100, 1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 5);
        LinkedGroup original =
                new LinkedGroup(100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);

        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(original);
        when(groupRepository.countWhitelistedTicketsForConfiguration(100)).thenReturn(3);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            groupManager.updateLink(100, modification);
        });
        assertEquals("Cannot update as there are already confirmed tickets.", exception.getMessage());
    }

    @Test
    public void testUpdateLinkCleanStateRequiredDueToGroupIdChange() {
        LinkedGroupModification modification = new LinkedGroupModification(
                100, 2, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        LinkedGroup original =
                new LinkedGroup(100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);

        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(original);
        when(groupRepository.countWhitelistedTicketsForConfiguration(100)).thenReturn(0);
        when(groupRepository.updateConfiguration(
                        100, 2, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null))
                .thenReturn(1);
        when(groupRepository.getConfiguration(100)).thenReturn(original);

        LinkedGroup result = groupManager.updateLink(100, modification);
        assertNotNull(result);
    }

    @Test
    public void testUpdateLinkCleanStateRequiredDueToMaxAllocationDecrease() {
        LinkedGroupModification modification = new LinkedGroupModification(
                100, 1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 5);
        LinkedGroup original =
                new LinkedGroup(100, 1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 10);

        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(original);
        when(groupRepository.countWhitelistedTicketsForConfiguration(100)).thenReturn(0);
        when(groupRepository.updateConfiguration(
                        100, 1, 10, 20, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 5))
                .thenReturn(1);
        when(groupRepository.getConfiguration(100)).thenReturn(original);

        LinkedGroup result = groupManager.updateLink(100, modification);
        assertNotNull(result);
    }

    @Test
    public void testUpdateLinkFailureOnUpdateCount() {
        LinkedGroupModification modification = new LinkedGroupModification(
                100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        LinkedGroup original =
                new LinkedGroup(100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);

        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(original);
        when(groupRepository.updateConfiguration(
                        100, 1, 10, 20, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null))
                .thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            groupManager.updateLink(100, modification);
        });
    }

    @Test
    public void testIsGroupLinked() {
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.emptyList());
        assertFalse(groupManager.isGroupLinked(1, 2));

        LinkedGroup mockLinkedGroup =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(mockLinkedGroup));
        assertTrue(groupManager.isGroupLinked(1, 2));
    }

    @Test
    public void testGetAllActiveForOrganization() {
        List<Group> expected = Collections.singletonList(mock(Group.class));
        when(groupRepository.getAllActiveForOrganization(1)).thenReturn(expected);
        assertEquals(expected, groupManager.getAllActiveForOrganization(1));
    }

    @Test
    public void testGetAllForOrganization() {
        List<Group> expected = Collections.singletonList(mock(Group.class));
        when(groupRepository.getAllForOrganization(1)).thenReturn(expected);
        assertEquals(expected, groupManager.getAllForOrganization(1));
    }

    @Test
    public void testLoadCompleteEmpty() {
        when(groupRepository.getOptionalById(1)).thenReturn(Optional.empty());
        assertFalse(groupManager.loadComplete(1).isPresent());
    }

    @Test
    public void testLoadCompleteSuccess() {
        Group group = new Group(1, "Group Name", "Description", 10, true);
        when(groupRepository.getOptionalById(1)).thenReturn(Optional.of(group));

        List<GroupMember> members = Collections.singletonList(new GroupMember(2, 1, "member@test.com", "Member Name"));
        when(groupRepository.getItems(1)).thenReturn(members);

        Optional<GroupModification> result = groupManager.loadComplete(1);
        assertTrue(result.isPresent());
        GroupModification modification = result.get();
        assertEquals(1, modification.getId());
        assertEquals("Group Name", modification.getName());
        assertEquals("Description", modification.getDescription());
        assertEquals(10, modification.getOrganizationId());
        assertEquals(1, modification.getItems().size());
        assertEquals("member@test.com", modification.getItems().get(0).getValue());
    }

    @Test
    public void testFindById() {
        Group group = new Group(1, "Group Name", "Description", 10, true);
        when(groupRepository.getOptionalById(1)).thenReturn(Optional.of(group));

        assertTrue(groupManager.findById(1, 10).isPresent());
        assertFalse(groupManager.findById(1, 20).isPresent());

        when(groupRepository.getOptionalById(2)).thenReturn(Optional.empty());
        assertFalse(groupManager.findById(2, 10).isPresent());
    }

    @Test
    public void testIsAllowed() {
        // No links -> allowed
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.emptyList());
        assertTrue(groupManager.isAllowed("member@test.com", 1, 2));

        // Link exists but matching member not found
        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));
        when(groupRepository.findItemByValueExactMatch(1, "member@test.com")).thenReturn(Optional.empty());
        assertFalse(groupManager.isAllowed("member@test.com", 1, 2));

        // Link exists and matching member found
        GroupMember member = new GroupMember(10, 1, "member@test.com", "Member");
        when(groupRepository.findItemByValueExactMatch(1, "member@test.com")).thenReturn(Optional.of(member));
        assertTrue(groupManager.isAllowed("member@test.com", 1, 2));
    }

    @Test
    public void testGetLinksForEvent() {
        List<LinkedGroup> expected = Collections.singletonList(mock(LinkedGroup.class));
        when(groupRepository.findActiveConfigurationsForEvent(1)).thenReturn(expected);
        assertEquals(expected, groupManager.getLinksForEvent(1));
    }

    @Test
    public void testAcquireMemberForTicketNoLinks() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.emptyList());

        assertTrue(groupManager.acquireMemberForTicket(ticket));
    }

    @Test
    public void testAcquireMemberForTicketMemberNotFound() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(ticket.getEmail()).thenReturn("nonexistent@test.com");

        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));
        when(groupRepository.findItemByValueExactMatch(1, "nonexistent@test.com"))
                .thenReturn(Optional.empty());

        assertFalse(groupManager.acquireMemberForTicket(ticket));
    }

    @Test
    public void testAcquireMemberForTicketOncePerValueValidationFails() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(ticket.getEmail()).thenReturn("member@test.com");

        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.ONCE_PER_VALUE, LinkedGroup.MatchType.FULL, null);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));

        GroupMember member = new GroupMember(10, 1, "member@test.com", "Member");
        when(groupRepository.findItemByValueExactMatch(1, "member@test.com")).thenReturn(Optional.of(member));
        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(configuration);
        when(groupRepository.countExistingWhitelistedTickets(10, 100)).thenReturn(1);

        assertFalse(groupManager.acquireMemberForTicket(ticket));
    }

    @Test
    public void testAcquireMemberForTicketLimitedQuantityValidationFails() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(ticket.getEmail()).thenReturn("member@test.com");

        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.FULL, 3);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));

        GroupMember member = new GroupMember(10, 1, "member@test.com", "Member");
        when(groupRepository.findItemByValueExactMatch(1, "member@test.com")).thenReturn(Optional.of(member));
        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(configuration);
        when(groupRepository.countExistingWhitelistedTickets(10, 100)).thenReturn(3);

        assertFalse(groupManager.acquireMemberForTicket(ticket));
    }

    @Test
    public void testAcquireMemberForTicketPartialMatchValidationFails() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(ticket.getEmail()).thenReturn("user@example.com");

        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.EMAIL_DOMAIN, 3);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));

        // Exact match not found, but ends with matches
        when(groupRepository.findItemByValueExactMatch(1, "user@example.com")).thenReturn(Optional.empty());
        GroupMember member = new GroupMember(10, 1, "example.com", "Domain Member");
        when(groupRepository.findItemEndsWith(100, 1, "%@example.com")).thenReturn(Optional.of(member));

        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(configuration);
        when(ticketRepository.countByEmailAddressAndCategory("user@example.com", 2))
                .thenReturn(4);

        assertFalse(groupManager.acquireMemberForTicket(ticket));
    }

    @Test
    public void testAcquireMemberForTicketPartialMatchValidationSuccess() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(500);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(ticket.getEmail()).thenReturn("user@example.com");
        when(ticket.getTicketsReservationId()).thenReturn("resId");

        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.LIMITED_QUANTITY, LinkedGroup.MatchType.EMAIL_DOMAIN, 3);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));

        when(groupRepository.findItemByValueExactMatch(1, "user@example.com")).thenReturn(Optional.empty());
        GroupMember member = new GroupMember(10, 1, "example.com", "Domain Member");
        when(groupRepository.findItemEndsWith(100, 1, "%@example.com")).thenReturn(Optional.of(member));

        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(configuration);
        when(ticketRepository.countByEmailAddressAndCategory("user@example.com", 2))
                .thenReturn(2);

        assertTrue(groupManager.acquireMemberForTicket(ticket));
        verify(groupRepository).insertWhitelistedTicket(10, 100, 500, null);
        verify(auditingRepository)
                .insert(
                        eq("resId"),
                        isNull(),
                        eq(1),
                        eq(Audit.EventType.GROUP_MEMBER_ACQUIRED),
                        any(Date.class),
                        eq(Audit.EntityType.TICKET),
                        eq("500"),
                        anyList());
    }

    @Test
    public void testAcquireMemberForTicketOncePerValueSuccess() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(500);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(ticket.getEmail()).thenReturn("member@test.com");
        when(ticket.getTicketsReservationId()).thenReturn("resId");

        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.ONCE_PER_VALUE, LinkedGroup.MatchType.FULL, null);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));

        GroupMember member = new GroupMember(10, 1, "member@test.com", "Member");
        when(groupRepository.findItemByValueExactMatch(1, "member@test.com")).thenReturn(Optional.of(member));
        when(groupRepository.getConfigurationForUpdate(100)).thenReturn(configuration);
        when(groupRepository.countExistingWhitelistedTickets(10, 100)).thenReturn(0);

        assertTrue(groupManager.acquireMemberForTicket(ticket));
        verify(groupRepository).insertWhitelistedTicket(10, 100, 500, Boolean.TRUE);
        verify(auditingRepository)
                .insert(
                        eq("resId"),
                        isNull(),
                        eq(1),
                        eq(Audit.EventType.GROUP_MEMBER_ACQUIRED),
                        any(Date.class),
                        eq(Audit.EntityType.TICKET),
                        eq("500"),
                        anyList());
    }

    @Test
    public void testAcquireMemberForTicketUnlimitedSuccess() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(500);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(ticket.getEmail()).thenReturn("member@test.com");
        when(ticket.getTicketsReservationId()).thenReturn("resId");

        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.FULL, null);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));

        GroupMember member = new GroupMember(10, 1, "member@test.com", "Member");
        when(groupRepository.findItemByValueExactMatch(1, "member@test.com")).thenReturn(Optional.of(member));

        assertTrue(groupManager.acquireMemberForTicket(ticket));
        verify(groupRepository).insertWhitelistedTicket(10, 100, 500, null);
        verify(auditingRepository)
                .insert(
                        eq("resId"),
                        isNull(),
                        eq(1),
                        eq(Audit.EventType.GROUP_MEMBER_ACQUIRED),
                        any(Date.class),
                        eq(Audit.EntityType.TICKET),
                        eq("500"),
                        anyList());
    }

    @Test
    public void testAcquireMemberForTicketInvalidEmailDomainMatch() {
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(1);
        when(ticket.getCategoryId()).thenReturn(2);
        when(ticket.getEmail()).thenReturn("invalidemail");

        LinkedGroup configuration =
                new LinkedGroup(100, 1, 1, 2, LinkedGroup.Type.UNLIMITED, LinkedGroup.MatchType.EMAIL_DOMAIN, null);
        when(groupRepository.findActiveConfigurationsFor(1, 2)).thenReturn(Collections.singletonList(configuration));

        when(groupRepository.findItemByValueExactMatch(1, "invalidemail")).thenReturn(Optional.empty());

        assertFalse(groupManager.acquireMemberForTicket(ticket));
    }

    @Test
    public void testDeleteWhitelistedTicketsForReservation() {
        // Empty tickets
        when(ticketRepository.findTicketsInReservation("resEmpty")).thenReturn(Collections.emptyList());
        groupManager.deleteWhitelistedTicketsForReservation("resEmpty");
        verify(groupRepository, never()).deleteExistingWhitelistedTickets(anyList());

        // With tickets
        Ticket t1 = mock(Ticket.class);
        when(t1.getId()).thenReturn(101);
        Ticket t2 = mock(Ticket.class);
        when(t2.getId()).thenReturn(102);

        when(ticketRepository.findTicketsInReservation("resWith")).thenReturn(Arrays.asList(t1, t2));
        groupManager.deleteWhitelistedTicketsForReservation("resWith");
        verify(groupRepository).deleteExistingWhitelistedTickets(Arrays.asList(101, 102));
    }

    @Test
    public void testDisableLink() {
        when(groupRepository.disableLink(100)).thenReturn(1);
        groupManager.disableLink(100);

        when(groupRepository.disableLink(200)).thenReturn(0);
        assertThrows(IllegalArgumentException.class, () -> {
            groupManager.disableLink(200);
        });
    }

    @Test
    public void testUpdateEmpty() {
        // Group not found
        when(groupRepository.getOptionalById(1)).thenReturn(Optional.empty());
        Optional<GroupModification> res1 = groupManager.update(
                1,
                new GroupModification(
                        1, "Name", "Desc", 10, Collections.singletonList(mock(GroupMemberModification.class))));
        assertFalse(res1.isPresent());

        // Modification items empty
        Group group = new Group(1, "Name", "Desc", 10, true);
        when(groupRepository.getOptionalById(1)).thenReturn(Optional.of(group));
        Optional<GroupModification> res2 =
                groupManager.update(1, new GroupModification(1, "Name", "Desc", 10, Collections.emptyList()));
        assertFalse(res2.isPresent());
    }

    @Test
    public void testUpdateSuccessNoNewItems() {
        Group group = new Group(1, "Name", "Desc", 10, true);
        when(groupRepository.getOptionalById(1)).thenReturn(Optional.of(group));

        List<String> existingValues = Collections.singletonList("member@test.com");
        when(groupRepository.getAllValuesIncludingNotActive(1)).thenReturn(existingValues);

        GroupMemberModification item = new GroupMemberModification(10, "member@test.com", "Member");
        GroupModification modification =
                new GroupModification(1, "New Name", "New Desc", 10, Collections.singletonList(item));

        when(groupRepository.update(1, "New Name", "New Desc")).thenReturn(1);
        when(groupRepository.getItems(1)).thenReturn(Collections.emptyList());

        Optional<GroupModification> res = groupManager.update(1, modification);
        assertTrue(res.isPresent());
        verify(groupRepository, never()).insert(eq(1), anyList());
    }

    @Test
    public void testUpdateSuccessWithNewItems() {
        Group group = new Group(1, "Name", "Desc", 10, true);
        when(groupRepository.getOptionalById(1)).thenReturn(Optional.of(group));

        when(groupRepository.getAllValuesIncludingNotActive(1)).thenReturn(Collections.emptyList());

        GroupMemberModification item = new GroupMemberModification(null, "newmember@test.com", "New Member");
        GroupModification modification =
                new GroupModification(1, "New Name", "New Desc", 10, Collections.singletonList(item));

        when(groupRepository.insert(eq(1), anyList())).thenReturn(new int[] {1});
        when(groupRepository.update(1, "New Name", "New Desc")).thenReturn(1);
        when(groupRepository.getItems(1)).thenReturn(Collections.emptyList());

        Optional<GroupModification> res = groupManager.update(1, modification);
        assertTrue(res.isPresent());
    }

    @Test
    public void testUpdateWithDuplicateNewItemsThrowsException() {
        Group group = new Group(1, "Name", "Desc", 10, true);
        when(groupRepository.getOptionalById(1)).thenReturn(Optional.of(group));

        when(groupRepository.getAllValuesIncludingNotActive(1)).thenReturn(Collections.emptyList());

        // Subclass overrides equals/hashCode to bypass distinct() filtering but keeps duplicate value
        GroupMemberModification item1 = new CustomGroupMemberModification(null, "dup@test.com", "Desc 1", 1);
        GroupMemberModification item2 = new CustomGroupMemberModification(null, "dup@test.com", "Desc 2", 2);

        GroupModification modification =
                new GroupModification(1, "New Name", "New Desc", 10, Arrays.asList(item1, item2));

        assertThrows(GroupManager.DuplicateGroupItemException.class, () -> {
            groupManager.update(1, modification);
        });
    }

    @Test
    public void testDeactivateMembers() {
        assertFalse(groupManager.deactivateMembers(Collections.emptyList(), 1));

        assertTrue(groupManager.deactivateMembers(Collections.singletonList(10), 1));
        verify(groupRepository).deactivateGroupMember(Collections.singletonList(10), 1);
    }

    @Test
    public void testDeactivateGroupNoMembers() {
        when(groupRepository.getItems(1)).thenReturn(Collections.emptyList());
        when(groupRepository.deactivateGroup(1)).thenReturn(1);

        assertTrue(groupManager.deactivateGroup(1));
        verify(groupRepository, never()).deactivateGroupMember(anyList(), anyInt());
        verify(groupRepository).disableAllLinks(1);
        verify(groupRepository).deactivateGroup(1);
    }

    @Test
    public void testDeactivateGroupWithMembers() {
        GroupMember m1 = new GroupMember(10, 1, "val1", "desc1");
        when(groupRepository.getItems(1)).thenReturn(Collections.singletonList(m1));
        when(groupRepository.deactivateGroup(1)).thenReturn(1);

        assertTrue(groupManager.deactivateGroup(1));
        verify(groupRepository).deactivateGroupMember(Collections.singletonList(10), 1);
        verify(groupRepository).disableAllLinks(1);
        verify(groupRepository).deactivateGroup(1);
    }

    @Test
    public void testDeactivateGroupFailureOnDeactivateCount() {
        when(groupRepository.getItems(1)).thenReturn(Collections.emptyList());
        when(groupRepository.deactivateGroup(1)).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () -> {
            groupManager.deactivateGroup(1);
        });
    }

    @Test
    public void testWhitelistValidator() {
        GroupManager mockManager = mock(GroupManager.class);
        GroupManager.WhitelistValidator validator = new GroupManager.WhitelistValidator(100, mockManager);
        GroupManager.WhitelistValidationItem item = new GroupManager.WhitelistValidationItem(200, "val");

        when(mockManager.isAllowed("val", 100, 200)).thenReturn(true);
        assertTrue(validator.test(item));

        when(mockManager.isAllowed("val", 100, 200)).thenReturn(false);
        assertFalse(validator.test(item));
    }

    private static class CustomGroupMemberModification extends GroupMemberModification {
        private final int uniqueId;

        CustomGroupMemberModification(Integer id, String value, String description, int uniqueId) {
            super(id, value, description);
            this.uniqueId = uniqueId;
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }
}
