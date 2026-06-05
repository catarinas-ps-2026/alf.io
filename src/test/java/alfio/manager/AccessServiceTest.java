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

import alfio.config.authentication.support.APITokenAuthentication;
import alfio.controller.form.ReservationCreate;
import alfio.manager.support.AccessDeniedException;
import alfio.model.EventAndOrganizationId;
import alfio.model.PromoCodeDiscount;
import alfio.model.PurchaseContext;
import alfio.model.ReservationIdAndEventId;
import alfio.model.Ticket;
import alfio.model.modification.AdditionalServiceReservationModification;
import alfio.model.modification.GroupModification;
import alfio.model.modification.ReservationRequest;
import alfio.model.subscription.LinkEventsToSubscriptionRequest;
import alfio.model.subscription.LinkSubscriptionsToEventRequest;
import alfio.model.subscription.SubscriptionDescriptor;
import alfio.model.user.Organization;
import alfio.model.user.Role;
import alfio.model.user.User;
import alfio.repository.*;
import alfio.repository.user.AuthorityRepository;
import alfio.repository.user.OrganizationRepository;
import alfio.repository.user.UserRepository;
import alfio.repository.user.join.UserOrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.util.*;

import static alfio.config.authentication.support.AuthenticationConstants.SYSTEM_API_CLIENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccessServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuthorityRepository authorityRepository;
    @Mock private UserOrganizationRepository userOrganizationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private TicketReservationRepository reservationRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private BillingDocumentRepository billingDocumentRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private TicketCategoryRepository ticketCategoryRepository;
    @Mock private PromoCodeDiscountRepository promoCodeDiscountRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private AdditionalServiceRepository additionalServiceRepository;
    @Mock private WaitingQueueRepository waitingQueueRepository;
    @Mock private PurchaseContextFieldRepository purchaseContextFieldRepository;

    private AccessService accessService;
    private final Map<String, Set<Role>> userRolesMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accessService = new AccessService(
            userRepository,
            authorityRepository,
            userOrganizationRepository,
            eventRepository,
            subscriptionRepository,
            reservationRepository,
            ticketRepository,
            billingDocumentRepository,
            groupRepository,
            ticketCategoryRepository,
            promoCodeDiscountRepository,
            organizationRepository,
            additionalServiceRepository,
            waitingQueueRepository,
            purchaseContextFieldRepository
        );

        userRolesMap.clear();
        when(authorityRepository.checkRole(anyString(), anySet())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            Set<String> checkedRoleNames = invocation.getArgument(1);
            Set<Role> userRoles = userRolesMap.getOrDefault(username, Collections.emptySet());
            return userRoles.stream()
                .map(Role::getRoleName)
                .anyMatch(checkedRoleNames::contains);
        });

        // Set up default users
        when(userRepository.findIdByUserName("user")).thenReturn(Optional.of(2));
        when(userRepository.findIdByUserName("admin")).thenReturn(Optional.of(1));
        when(userRepository.findIdByUserName("currentUser")).thenReturn(Optional.of(3));
        
        mockUserRoles("user", Role.ADMIN);
        mockUserRoles("admin", Role.ADMIN);
        mockUserRoles("currentUser", Role.ADMIN);
    }

    private void mockUserRoles(String username, Role... roles) {
        userRolesMap.put(username, new HashSet<>(Arrays.asList(roles)));
    }

    private Principal mockPrincipal(String name) {
        Principal p = mock(Principal.class);
        when(p.getName()).thenReturn(name);
        return p;
    }

    private APITokenAuthentication mockSystemApiPrincipal() {
        APITokenAuthentication p = mock(APITokenAuthentication.class);
        when(p.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + SYSTEM_API_CLIENT)));
        return p;
    }

    // 1. checkAccessToUser
    @Test
    void testCheckAccessToUser_NullUserId() {
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToUser(mockPrincipal("user"), null));
    }

    @Test
    void testCheckAccessToUser_NullPrincipal() {
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToUser(null, 123));
    }

    @Test
    void testCheckAccessToUser_AdminPrincipal() {
        Principal admin = mockPrincipal("admin");
        mockUserRoles("admin", Role.ADMIN);
        assertDoesNotThrow(() -> accessService.checkAccessToUser(admin, 123));
    }

    @Test
    void testCheckAccessToUser_SystemApiPrincipal() {
        APITokenAuthentication sysApi = mockSystemApiPrincipal();
        assertDoesNotThrow(() -> accessService.checkAccessToUser(sysApi, 123));
    }

    @Test
    void testCheckAccessToUser_TargetUserNotFound() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user");
        when(userRepository.findOptionalById(123)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToUser(user, 123));
    }

    @Test
    void testCheckAccessToUser_TargetUserIsAdminByName() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user");
        User targetUser = mock(User.class);
        when(targetUser.getUsername()).thenReturn("admin");
        when(userRepository.findOptionalById(123)).thenReturn(Optional.of(targetUser));
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToUser(user, 123));
    }

    @Test
    void testCheckAccessToUser_TargetUserIsAdminByRole() {
        Principal user = mockPrincipal("user");
        User targetUser = mock(User.class);
        when(targetUser.getUsername()).thenReturn("targetUser");
        when(userRepository.findOptionalById(123)).thenReturn(Optional.of(targetUser));
        mockUserRoles("targetUser", Role.ADMIN);
        mockUserRoles("user"); // current user is not admin
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToUser(user, 123));
    }

    @Test
    void testCheckAccessToUser_TargetUserNoOrgs() {
        Principal user = mockPrincipal("user");
        User targetUser = mock(User.class);
        when(targetUser.getUsername()).thenReturn("targetUser");
        when(userRepository.findOptionalById(123)).thenReturn(Optional.of(targetUser));
        mockUserRoles("targetUser");
        mockUserRoles("user");
        when(organizationRepository.findAllForUser("targetUser")).thenReturn(Collections.emptyList());
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToUser(user, 123));
    }

    @Test
    void testCheckAccessToUser_TargetUserMultipleOrgs() {
        Principal user = mockPrincipal("user");
        User targetUser = mock(User.class);
        when(targetUser.getUsername()).thenReturn("targetUser");
        when(userRepository.findOptionalById(123)).thenReturn(Optional.of(targetUser));
        mockUserRoles("targetUser");
        mockUserRoles("user");
        
        Organization org1 = mock(Organization.class);
        Organization org2 = mock(Organization.class);
        when(organizationRepository.findAllForUser("targetUser")).thenReturn(List.of(org1, org2));
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToUser(user, 123));
    }

    @Test
    void testCheckAccessToUser_TargetUserOneOrg_NotOwner() {
        Principal user = mockPrincipal("currentUser");
        User targetUser = mock(User.class);
        when(targetUser.getUsername()).thenReturn("targetUser");
        when(userRepository.findOptionalById(123)).thenReturn(Optional.of(targetUser));
        mockUserRoles("targetUser");
        mockUserRoles("currentUser", Role.OWNER);
        
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(100);
        when(organizationRepository.findAllForUser("targetUser")).thenReturn(List.of(org));
        
        when(userOrganizationRepository.userIsInOrganization(3, 100)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToUser(user, 123));
    }

    @Test
    void testCheckAccessToUser_TargetUserOneOrg_IsOwner() {
        Principal user = mockPrincipal("currentUser");
        User targetUser = mock(User.class);
        when(targetUser.getUsername()).thenReturn("targetUser");
        when(userRepository.findOptionalById(123)).thenReturn(Optional.of(targetUser));
        mockUserRoles("targetUser");
        mockUserRoles("currentUser", Role.OWNER);
        
        Organization org = mock(Organization.class);
        when(org.getId()).thenReturn(100);
        when(organizationRepository.findAllForUser("targetUser")).thenReturn(List.of(org));
        
        when(userOrganizationRepository.userIsInOrganization(3, 100)).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkAccessToUser(user, 123));
    }

    // 2. checkOrganizationMembership
    @Test
    void testCheckOrganizationMembership_NullPrincipal() {
        assertDoesNotThrow(() -> accessService.checkOrganizationMembership(null, 100, Set.of(Role.OWNER)));
    }

    @Test
    void testCheckOrganizationMembership_SystemApiPrincipal() {
        APITokenAuthentication sysApi = mockSystemApiPrincipal();
        assertDoesNotThrow(() -> accessService.checkOrganizationMembership(sysApi, 100, Set.of(Role.OWNER)));
    }

    @Test
    void testCheckOrganizationMembership_AdminPrincipal() {
        Principal admin = mockPrincipal("admin");
        mockUserRoles("admin", Role.ADMIN);
        assertDoesNotThrow(() -> accessService.checkOrganizationMembership(admin, 100, Set.of(Role.OWNER)));
    }

    @Test
    void testCheckOrganizationMembership_UserHasRoleAndIsInOrg() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user", Role.OWNER);
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkOrganizationMembership(user, 100, Set.of(Role.OWNER)));
    }

    @Test
    void testCheckOrganizationMembership_UserHasRoleButNotInOrg() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user", Role.OWNER);
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkOrganizationMembership(user, 100, Set.of(Role.OWNER)));
    }

    @Test
    void testCheckOrganizationMembership_UserDoesNotHaveRole() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user");
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkOrganizationMembership(user, 100, Set.of(Role.OWNER)));
    }

    // 3. checkOrganizationOwnership
    @Test
    void testCheckOrganizationOwnership_NullPrincipal() {
        assertDoesNotThrow(() -> accessService.checkOrganizationOwnership(null, 100));
    }

    @Test
    void testCheckOrganizationOwnership_SystemApiPrincipal() {
        APITokenAuthentication sysApi = mockSystemApiPrincipal();
        assertDoesNotThrow(() -> accessService.checkOrganizationOwnership(sysApi, 100));
    }

    @Test
    void testCheckOrganizationOwnership_NullOrgAndAdmin() {
        Principal admin = mockPrincipal("admin");
        mockUserRoles("admin", Role.ADMIN);
        assertDoesNotThrow(() -> accessService.checkOrganizationOwnership(admin, null));
    }

    @Test
    void testCheckOrganizationOwnership_NullOrgAndNotAdmin() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user");
        assertThrows(AccessDeniedException.class, () -> accessService.checkOrganizationOwnership(user, null));
    }

    @Test
    void testCheckOrganizationOwnership_IsOwner() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user", Role.OWNER);
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(true);
        assertDoesNotThrow(() -> accessService.checkOrganizationOwnership(user, 100));
    }

    @Test
    void testCheckOrganizationOwnership_IsNotOwner() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user", Role.OWNER);
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> accessService.checkOrganizationOwnership(user, 100));
    }

    // 4. ensureAdmin & ensureSystemApiKey
    @Test
    void testEnsureAdmin_Success() {
        Principal admin = mockPrincipal("admin");
        mockUserRoles("admin", Role.ADMIN);
        assertDoesNotThrow(() -> accessService.ensureAdmin(admin));
    }

    @Test
    void testEnsureAdmin_Failure() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user");
        assertThrows(AccessDeniedException.class, () -> accessService.ensureAdmin(user));
    }

    @Test
    void testEnsureSystemApiKey_Success() {
        APITokenAuthentication sysApi = mockSystemApiPrincipal();
        assertDoesNotThrow(() -> accessService.ensureSystemApiKey(sysApi));
    }

    @Test
    void testEnsureSystemApiKey_Failure() {
        Principal user = mockPrincipal("user");
        assertThrows(AccessDeniedException.class, () -> accessService.ensureSystemApiKey(user));
    }

    // 5. checkEventOwnership
    @Test
    void testCheckEventOwnership_ById_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        EventAndOrganizationId result = accessService.checkEventOwnership(user, 10);
        assertNotNull(result);
        assertEquals(10, result.getId());
    }

    @Test
    void testCheckEventOwnership_ByIdAndOrgId_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        assertDoesNotThrow(() -> accessService.checkEventOwnership(user, 10, 100));
    }

    @Test
    void testCheckEventOwnership_ByIdAndOrgId_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventOwnership(user, 10, 200));
    }

    @Test
    void testCheckEventOwnership_ByShortName_NotFound() {
        Principal user = mockPrincipal("user");
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventOwnership(user, "short"));
    }

    @Test
    void testCheckEventOwnership_ByShortName_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        EventAndOrganizationId result = accessService.checkEventOwnership(user, "short");
        assertNotNull(result);
        assertEquals(10, result.getId());
    }

    @Test
    void testCheckEventOwnership_ByShortNameAndOrgId_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        assertDoesNotThrow(() -> accessService.checkEventOwnership(user, "short", 100));
    }

    @Test
    void testCheckEventOwnership_ByShortNameAndOrgId_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventOwnership(user, "short", 200));
    }

    // 6. checkEventMembership
    @Test
    void testCheckEventMembership_ByShortName_NotFound() {
        Principal user = mockPrincipal("user");
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventMembership(user, "short", Set.of(Role.OWNER)));
    }

    @Test
    void testCheckEventMembership_ByShortName_Success() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user", Role.OWNER);
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(true);
        
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        EventAndOrganizationId result = accessService.checkEventMembership(user, "short", Set.of(Role.OWNER));
        assertNotNull(result);
        assertEquals(10, result.getId());
    }

    @Test
    void testCheckEventMembership_ById_Success() {
        Principal user = mockPrincipal("user");
        mockUserRoles("user", Role.OWNER);
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(true);
        
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        EventAndOrganizationId result = accessService.checkEventMembership(user, 10, Set.of(Role.OWNER));
        assertNotNull(result);
        assertEquals(10, result.getId());
    }

    // 7. checkCategoryOwnership
    @Test
    void testCheckCategoryOwnership_ById_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(1), 10)).thenReturn(1);
        
        EventAndOrganizationId result = accessService.checkCategoryOwnership(user, 10, 1);
        assertNotNull(result);
    }

    @Test
    void testCheckCategoryOwnership_ById_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(1, 2), 10)).thenReturn(1);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkCategoryOwnership(user, 10, Set.of(1, 2)));
    }

    @Test
    void testCheckCategoryOwnership_ByShortName_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(1), 10)).thenReturn(1);
        
        EventAndOrganizationId result = accessService.checkCategoryOwnership(user, "short", 1);
        assertNotNull(result);
    }

    @Test
    void testCheckCategoryOwnership_ByShortName_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(1, 2), 10)).thenReturn(1);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkCategoryOwnership(user, "short", Set.of(1, 2)));
    }

    // 8. checkEventReservationCreationRequest
    @Test
    void testCheckEventReservationCreationRequest_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        ReservationCreate<ReservationRequest> createRequest = mock(ReservationCreate.class);
        ReservationRequest req = mock(ReservationRequest.class);
        when(req.getTicketCategoryId()).thenReturn(1);
        when(createRequest.getTickets()).thenReturn(List.of(req));
        
        when(ticketCategoryRepository.countCategoriesBelongingToEvent(10, Set.of(1))).thenReturn(1);
        
        assertDoesNotThrow(() -> accessService.checkEventReservationCreationRequest(user, "short", createRequest));
    }

    @Test
    void testCheckEventReservationCreationRequest_CategoryMismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        ReservationCreate<ReservationRequest> createRequest = mock(ReservationCreate.class);
        ReservationRequest req = mock(ReservationRequest.class);
        when(req.getTicketCategoryId()).thenReturn(1);
        when(createRequest.getTickets()).thenReturn(List.of(req));
        
        when(ticketCategoryRepository.countCategoriesBelongingToEvent(10, Set.of(1))).thenReturn(0);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventReservationCreationRequest(user, "short", createRequest));
    }

    @Test
    void testCheckEventReservationCreationRequest_WithAdditionalServices_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        ReservationCreate<ReservationRequest> createRequest = mock(ReservationCreate.class);
        ReservationRequest req = mock(ReservationRequest.class);
        when(req.getTicketCategoryId()).thenReturn(1);
        when(createRequest.getTickets()).thenReturn(List.of(req));
        
        AdditionalServiceReservationModification addMod = mock(AdditionalServiceReservationModification.class);
        when(addMod.getAdditionalServiceId()).thenReturn(20);
        when(createRequest.getAdditionalServices()).thenReturn(List.of(addMod));
        
        when(ticketCategoryRepository.countCategoriesBelongingToEvent(10, Set.of(1))).thenReturn(1);
        when(additionalServiceRepository.countAdditionalServicesBelongingToEvent(10, Set.of(20))).thenReturn(1);
        
        assertDoesNotThrow(() -> accessService.checkEventReservationCreationRequest(user, "short", List.of(createRequest)));
    }

    @Test
    void testCheckEventReservationCreationRequest_WithAdditionalServices_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        ReservationCreate<ReservationRequest> createRequest = mock(ReservationCreate.class);
        ReservationRequest req = mock(ReservationRequest.class);
        when(req.getTicketCategoryId()).thenReturn(1);
        when(createRequest.getTickets()).thenReturn(List.of(req));
        
        AdditionalServiceReservationModification addMod = mock(AdditionalServiceReservationModification.class);
        when(addMod.getAdditionalServiceId()).thenReturn(20);
        when(createRequest.getAdditionalServices()).thenReturn(List.of(addMod));
        
        when(ticketCategoryRepository.countCategoriesBelongingToEvent(10, Set.of(1))).thenReturn(1);
        when(additionalServiceRepository.countAdditionalServicesBelongingToEvent(10, Set.of(20))).thenReturn(0);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventReservationCreationRequest(user, "short", List.of(createRequest)));
    }

    // 9. checkReservationOwnership & checkReservationMembership
    @Test
    void testCheckReservationOwnership_Event_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        ReservationIdAndEventId res = new ReservationIdAndEventId("resId", 10);
        when(reservationRepository.getReservationIdAndEventId(List.of("resId"))).thenReturn(List.of(res));
        
        assertDoesNotThrow(() -> accessService.checkReservationOwnership(user, PurchaseContext.PurchaseContextType.event, "short", "resId"));
    }

    @Test
    void testCheckReservationOwnership_Event_FailReservationNotFound() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(reservationRepository.getReservationIdAndEventId(List.of("resId"))).thenReturn(Collections.emptyList());
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkReservationOwnership(user, PurchaseContext.PurchaseContextType.event, "short", "resId"));
    }

    @Test
    void testCheckReservationOwnership_Subscription_Success() {
        Principal user = mockPrincipal("user");
        SubscriptionDescriptor desc = mock(SubscriptionDescriptor.class);
        when(desc.getOrganizationId()).thenReturn(100);
        when(desc.getPublicIdentifier()).thenReturn("pubId");
        
        when(subscriptionRepository.findDescriptorByReservationId("resId")).thenReturn(Optional.of(desc));
        
        assertDoesNotThrow(() -> accessService.checkReservationOwnership(user, PurchaseContext.PurchaseContextType.subscription, "pubId", "resId"));
    }

    @Test
    void testCheckReservationOwnership_Subscription_MismatchPublicId() {
        Principal user = mockPrincipal("user");
        SubscriptionDescriptor desc = mock(SubscriptionDescriptor.class);
        when(desc.getOrganizationId()).thenReturn(100);
        when(desc.getPublicIdentifier()).thenReturn("otherPubId");
        
        when(subscriptionRepository.findDescriptorByReservationId("resId")).thenReturn(Optional.of(desc));
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkReservationOwnership(user, PurchaseContext.PurchaseContextType.subscription, "pubId", "resId"));
    }

    @Test
    void testCheckReservationMembership_Event_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        ReservationIdAndEventId res = new ReservationIdAndEventId("resId", 10);
        when(reservationRepository.getReservationIdAndEventId(List.of("resId"))).thenReturn(List.of(res));
        
        assertDoesNotThrow(() -> accessService.checkReservationMembership(user, PurchaseContext.PurchaseContextType.event, "short", "resId"));
    }

    @Test
    void testCheckReservationMembership_Subscription_Success() {
        Principal user = mockPrincipal("user");
        SubscriptionDescriptor desc = mock(SubscriptionDescriptor.class);
        when(desc.getOrganizationId()).thenReturn(100);
        when(desc.getPublicIdentifier()).thenReturn("pubId");
        
        when(subscriptionRepository.findDescriptorByReservationId("resId")).thenReturn(Optional.of(desc));
        
        assertDoesNotThrow(() -> accessService.checkReservationMembership(user, PurchaseContext.PurchaseContextType.subscription, "pubId", "resId"));
    }

    // 10. checkPurchaseContextOwnership (with type)
    @Test
    void testCheckPurchaseContextOwnership_Event_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        assertDoesNotThrow(() -> accessService.checkPurchaseContextOwnership(user, PurchaseContext.PurchaseContextType.event, "short"));
    }

    @Test
    void testCheckPurchaseContextOwnership_Subscription_Success() {
        Principal user = mockPrincipal("user");
        UUID uuid = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(uuid)).thenReturn(Optional.of(100));
        
        assertDoesNotThrow(() -> accessService.checkPurchaseContextOwnership(user, PurchaseContext.PurchaseContextType.subscription, uuid.toString()));
    }

    // 11. checkPurchaseContextOwnership (with eventId / subscriptionDescriptorId)
    @Test
    void testCheckPurchaseContextOwnership_WithEventId_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        assertDoesNotThrow(() -> accessService.checkPurchaseContextOwnership(user, 100, 10, null));
    }

    @Test
    void testCheckPurchaseContextOwnership_WithSubscriptionId_Success() {
        Principal user = mockPrincipal("user");
        UUID uuid = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(uuid)).thenReturn(Optional.of(100));
        
        assertDoesNotThrow(() -> accessService.checkPurchaseContextOwnership(user, 100, null, uuid));
    }

    @Test
    void testCheckPurchaseContextOwnership_WithSubscriptionId_Mismatch() {
        Principal user = mockPrincipal("user");
        UUID uuid = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(uuid)).thenReturn(Optional.of(200));
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkPurchaseContextOwnership(user, 100, null, uuid));
    }

    @Test
    void testCheckPurchaseContextOwnership_WithSubscriptionId_NotFound() {
        Principal user = mockPrincipal("user");
        UUID uuid = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(uuid)).thenReturn(Optional.empty());
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkPurchaseContextOwnership(user, 100, null, uuid));
    }

    // 12. checkDescriptorsLinkRequest
    @Test
    void testCheckDescriptorsLinkRequest_EmptyDescriptors() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        EventAndOrganizationId result = accessService.checkDescriptorsLinkRequest(user, "short", Collections.emptyList());
        assertNotNull(result);
        assertEquals(10, result.getId());
    }

    @Test
    void testCheckDescriptorsLinkRequest_CountMismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        LinkSubscriptionsToEventRequest req = mock(LinkSubscriptionsToEventRequest.class);
        UUID descId = UUID.randomUUID();
        when(req.getDescriptorId()).thenReturn(descId);
        
        when(subscriptionRepository.countDescriptorsBelongingToOrganization(List.of(descId), 100)).thenReturn(0);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkDescriptorsLinkRequest(user, "short", List.of(req)));
    }

    @Test
    void testCheckDescriptorsLinkRequest_CategoriesSuccess() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        LinkSubscriptionsToEventRequest req = mock(LinkSubscriptionsToEventRequest.class);
        UUID descId = UUID.randomUUID();
        when(req.getDescriptorId()).thenReturn(descId);
        when(req.getCategories()).thenReturn(List.of(1, 2));
        
        when(subscriptionRepository.countDescriptorsBelongingToOrganization(List.of(descId), 100)).thenReturn(1);
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(1, 2), 10)).thenReturn(2);
        
        EventAndOrganizationId result = accessService.checkDescriptorsLinkRequest(user, "short", List.of(req));
        assertNotNull(result);
    }

    @Test
    void testCheckDescriptorsLinkRequest_CategoriesMismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        LinkSubscriptionsToEventRequest req = mock(LinkSubscriptionsToEventRequest.class);
        UUID descId = UUID.randomUUID();
        when(req.getDescriptorId()).thenReturn(descId);
        when(req.getCategories()).thenReturn(List.of(1, 2));
        
        when(subscriptionRepository.countDescriptorsBelongingToOrganization(List.of(descId), 100)).thenReturn(1);
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(1, 2), 10)).thenReturn(1);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkDescriptorsLinkRequest(user, "short", List.of(req)));
    }

    // 13. checkBillingDocumentOwnership
    @Test
    void testCheckBillingDocumentOwnership_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        ReservationIdAndEventId res = new ReservationIdAndEventId("resId", 10);
        when(reservationRepository.getReservationIdAndEventId(List.of("resId"))).thenReturn(List.of(res));
        when(billingDocumentRepository.checkBillingDocumentExistsForReservation(999L, "resId")).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkBillingDocumentOwnership(user, PurchaseContext.PurchaseContextType.event, "short", "resId", 999L));
    }

    @Test
    void testCheckBillingDocumentOwnership_NotExist() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        ReservationIdAndEventId res = new ReservationIdAndEventId("resId", 10);
        when(reservationRepository.getReservationIdAndEventId(List.of("resId"))).thenReturn(List.of(res));
        when(billingDocumentRepository.checkBillingDocumentExistsForReservation(999L, "resId")).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkBillingDocumentOwnership(user, PurchaseContext.PurchaseContextType.event, "short", "resId", 999L));
    }

    // 14. checkGroupLinkOwnership & checkGroupOwnership
    @Test
    void testCheckGroupLinkOwnership_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        when(groupRepository.checkGroupLinkExists(50, 100, 10, 5)).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkGroupLinkOwnership(user, 50, 100, 10, 5));
    }

    @Test
    void testCheckGroupLinkOwnership_OrgIdMismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkGroupLinkOwnership(user, 50, 200, 10, 5));
    }

    @Test
    void testCheckGroupLinkOwnership_LinkNotExists() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        when(groupRepository.checkGroupLinkExists(50, 100, 10, 5)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkGroupLinkOwnership(user, 50, 100, 10, 5));
    }

    @Test
    void testCheckGroupOwnership_Success() {
        Principal user = mockPrincipal("user");
        when(groupRepository.checkGroupExists(30, 100)).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkGroupOwnership(user, 30, 100));
    }

    @Test
    void testCheckGroupOwnership_NotExist() {
        Principal user = mockPrincipal("user");
        when(groupRepository.checkGroupExists(30, 100)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkGroupOwnership(user, 30, 100));
    }

    // 15. checkGroupUpdateRequest & checkGroupCreateRequest
    @Test
    void testCheckGroupUpdateRequest_Success() {
        Principal user = mockPrincipal("user");
        when(groupRepository.checkGroupExists(30, 100)).thenReturn(true);
        
        GroupModification groupMod = mock(GroupModification.class);
        when(groupMod.getOrganizationId()).thenReturn(100);
        when(groupMod.getId()).thenReturn(30);
        
        assertDoesNotThrow(() -> accessService.checkGroupUpdateRequest(user, 30, 100, groupMod));
    }

    @Test
    void testCheckGroupUpdateRequest_OrgIdMismatch() {
        Principal user = mockPrincipal("user");
        GroupModification groupMod = mock(GroupModification.class);
        when(groupMod.getOrganizationId()).thenReturn(200);
        when(groupMod.getId()).thenReturn(30);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkGroupUpdateRequest(user, 30, 100, groupMod));
    }

    @Test
    void testCheckGroupUpdateRequest_GroupIdMismatch() {
        Principal user = mockPrincipal("user");
        GroupModification groupMod = mock(GroupModification.class);
        when(groupMod.getOrganizationId()).thenReturn(100);
        when(groupMod.getId()).thenReturn(40);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkGroupUpdateRequest(user, 30, 100, groupMod));
    }

    @Test
    void testCheckGroupCreateRequest_Success() {
        Principal user = mockPrincipal("user");
        GroupModification groupMod = mock(GroupModification.class);
        when(groupMod.getOrganizationId()).thenReturn(100);
        
        assertDoesNotThrow(() -> accessService.checkGroupCreateRequest(user, 100, groupMod));
    }

    @Test
    void testCheckGroupCreateRequest_OrgIdMismatch() {
        Principal user = mockPrincipal("user");
        GroupModification groupMod = mock(GroupModification.class);
        when(groupMod.getOrganizationId()).thenReturn(200);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkGroupCreateRequest(user, 100, groupMod));
    }

    // 16. checkAccessToPromoCode & checkAccessToPromoCodeEventOrganization
    @Test
    void testCheckAccessToPromoCode_NotFound() {
        Principal user = mockPrincipal("user");
        when(promoCodeDiscountRepository.findOptionalById(40)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToPromoCode(user, 40));
    }

    @Test
    void testCheckAccessToPromoCode_WithEvent_Success() {
        Principal user = mockPrincipal("user");
        PromoCodeDiscount pc = mock(PromoCodeDiscount.class);
        when(pc.getEventId()).thenReturn(10);
        when(pc.getOrganizationId()).thenReturn(100);
        when(promoCodeDiscountRepository.findOptionalById(40)).thenReturn(Optional.of(pc));
        
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        assertDoesNotThrow(() -> accessService.checkAccessToPromoCode(user, 40));
    }

    @Test
    void testCheckAccessToPromoCode_WithoutEvent_Success() {
        Principal user = mockPrincipal("user");
        PromoCodeDiscount pc = mock(PromoCodeDiscount.class);
        when(pc.getEventId()).thenReturn(null);
        when(pc.getOrganizationId()).thenReturn(100);
        when(promoCodeDiscountRepository.findOptionalById(40)).thenReturn(Optional.of(pc));
        
        assertDoesNotThrow(() -> accessService.checkAccessToPromoCode(user, 40));
    }

    @Test
    void testCheckAccessToPromoCodeEventOrganization_BothNull() {
        Principal user = mockPrincipal("user");
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToPromoCodeEventOrganization(user, null, null));
    }

    @Test
    void testCheckAccessToPromoCodeEventOrganization_BothNotNull() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        int result = accessService.checkAccessToPromoCodeEventOrganization(user, 10, 100);
        assertEquals(100, result);
    }

    @Test
    void testCheckAccessToPromoCodeEventOrganization_OnlyEvent() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        int result = accessService.checkAccessToPromoCodeEventOrganization(user, 10, null);
        assertEquals(100, result);
    }

    @Test
    void testCheckAccessToPromoCodeEventOrganization_OnlyOrg() {
        Principal user = mockPrincipal("user");
        int result = accessService.checkAccessToPromoCodeEventOrganization(user, null, 100);
        assertEquals(100, result);
    }

    // 17. checkEventLinkRequest
    @Test
    void testCheckEventLinkRequest_SubscriptionNotFound() {
        Principal user = mockPrincipal("user");
        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(subId)).thenReturn(Optional.empty());
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventLinkRequest(user, subId.toString(), Collections.emptyList()));
    }

    @Test
    void testCheckEventLinkRequest_SlugsMismatch() {
        Principal user = mockPrincipal("user");
        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(subId)).thenReturn(Optional.of(100));
        
        LinkEventsToSubscriptionRequest request = new LinkEventsToSubscriptionRequest("slug1", Collections.emptyList());
        when(eventRepository.countEventsInOrganization(100, Set.of("slug1"))).thenReturn(0);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventLinkRequest(user, subId.toString(), List.of(request)));
    }

    @Test
    void testCheckEventLinkRequest_CategoriesSuccess() {
        Principal user = mockPrincipal("user");
        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(subId)).thenReturn(Optional.of(100));
        
        LinkEventsToSubscriptionRequest request = new LinkEventsToSubscriptionRequest("slug1", List.of(1, 2));
        when(eventRepository.countEventsInOrganization(100, Set.of("slug1"))).thenReturn(1);
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(1, 2), "slug1")).thenReturn(2);
        
        assertDoesNotThrow(() -> accessService.checkEventLinkRequest(user, subId.toString(), List.of(request)));
    }

    @Test
    void testCheckEventLinkRequest_CategoriesMismatch() {
        Principal user = mockPrincipal("user");
        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(subId)).thenReturn(Optional.of(100));
        
        LinkEventsToSubscriptionRequest request = new LinkEventsToSubscriptionRequest("slug1", List.of(1, 2));
        when(eventRepository.countEventsInOrganization(100, Set.of("slug1"))).thenReturn(1);
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(1, 2), "slug1")).thenReturn(1);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventLinkRequest(user, subId.toString(), List.of(request)));
    }

    // 18. canAccessEvent & canAccessTicket
    @Test
    void testCanAccessEvent_NotFound() {
        Principal user = mockPrincipal("user");
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.empty());
        
        assertThrows(AccessDeniedException.class, () -> accessService.canAccessEvent(user, "short"));
    }

    @Test
    void testCanAccessEvent_NotInOrg() {
        Principal user = mockPrincipal("user");
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(2);
        when(userRepository.getByUsername("user")).thenReturn(mockUser);
        
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.canAccessEvent(user, "short"));
    }

    @Test
    void testCanAccessEvent_Success() {
        Principal user = mockPrincipal("user");
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(2);
        when(userRepository.getByUsername("user")).thenReturn(mockUser);
        
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(true);
        
        EventAndOrganizationId result = accessService.canAccessEvent(user, "short");
        assertNotNull(result);
        assertEquals(10, result.getId());
    }

    @Test
    void testCanAccessTicket_Success() {
        Principal user = mockPrincipal("user");
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(2);
        when(userRepository.getByUsername("user")).thenReturn(mockUser);
        
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(true);
        
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(10);
        when(ticketRepository.findByUUID("ticket-uuid")).thenReturn(ticket);
        
        assertDoesNotThrow(() -> accessService.canAccessTicket(user, "short", "ticket-uuid"));
    }

    @Test
    void testCanAccessTicket_MismatchEventId() {
        Principal user = mockPrincipal("user");
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(2);
        when(userRepository.getByUsername("user")).thenReturn(mockUser);
        
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(userOrganizationRepository.userIsInOrganization(2, 100)).thenReturn(true);
        
        Ticket ticket = mock(Ticket.class);
        when(ticket.getEventId()).thenReturn(20);
        when(ticketRepository.findByUUID("ticket-uuid")).thenReturn(ticket);
        
        assertThrows(AccessDeniedException.class, () -> accessService.canAccessTicket(user, "short", "ticket-uuid"));
    }

    // 19. checkWaitingQueueSubscriberInEvent
    @Test
    void testCheckWaitingQueueSubscriberInEvent_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(waitingQueueRepository.exists(500, 10)).thenReturn(true);
        
        EventAndOrganizationId result = accessService.checkWaitingQueueSubscriberInEvent(user, 500, "short");
        assertNotNull(result);
    }

    @Test
    void testCheckWaitingQueueSubscriberInEvent_NotExist() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(waitingQueueRepository.exists(500, 10)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkWaitingQueueSubscriberInEvent(user, 500, "short"));
    }

    // 20. checkBillingDocumentsOwnership & checkPurchaseContextOwnershipAndTicketAdditionalFieldIds
    @Test
    void testCheckBillingDocumentsOwnership_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        when(billingDocumentRepository.findByIdsAndEvent(List.of(1L, 2L), 10)).thenReturn(List.of(1L, 2L));
        
        assertDoesNotThrow(() -> accessService.checkBillingDocumentsOwnership(user, 10, List.of(1L, 2L)));
    }

    @Test
    void testCheckBillingDocumentsOwnership_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        when(billingDocumentRepository.findByIdsAndEvent(List.of(1L, 2L), 10)).thenReturn(List.of(1L));
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkBillingDocumentsOwnership(user, 10, List.of(1L, 2L)));
    }

    @Test
    void testCheckPurchaseContextOwnershipAndTicketAdditionalFieldIds_Event_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(purchaseContextFieldRepository.countMatchingAdditionalFieldsForPurchaseContext(10, null, Set.of(1L, 2L))).thenReturn(2);
        
        assertDoesNotThrow(() -> accessService.checkPurchaseContextOwnershipAndTicketAdditionalFieldIds(
            user, PurchaseContext.PurchaseContextType.event, "short", Set.of(1L, 2L)));
    }

    @Test
    void testCheckPurchaseContextOwnershipAndTicketAdditionalFieldIds_Event_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(purchaseContextFieldRepository.countMatchingAdditionalFieldsForPurchaseContext(10, null, Set.of(1L, 2L))).thenReturn(1);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkPurchaseContextOwnershipAndTicketAdditionalFieldIds(
            user, PurchaseContext.PurchaseContextType.event, "short", Set.of(1L, 2L)));
    }

    @Test
    void testCheckPurchaseContextOwnershipAndTicketAdditionalFieldIds_Subscription_Success() {
        Principal user = mockPrincipal("user");
        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(subId)).thenReturn(Optional.of(100));
        when(purchaseContextFieldRepository.countMatchingAdditionalFieldsForPurchaseContext(null, subId, Set.of(1L))).thenReturn(1);
        
        assertDoesNotThrow(() -> accessService.checkPurchaseContextOwnershipAndTicketAdditionalFieldIds(
            user, PurchaseContext.PurchaseContextType.subscription, subId.toString(), Set.of(1L)));
    }

    // 21. checkCategoryOwnershipAndTicket
    @Test
    void testCheckCategoryOwnershipAndTicket_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(5), 10)).thenReturn(1);
        
        when(ticketRepository.isInCategory(500, 5)).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkCategoryOwnershipAndTicket(user, "short", 5, 500));
    }

    @Test
    void testCheckCategoryOwnershipAndTicket_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(ticketCategoryRepository.countCategoryForEvent(Set.of(5), 10)).thenReturn(1);
        
        when(ticketRepository.isInCategory(500, 5)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkCategoryOwnershipAndTicket(user, "short", 5, 500));
    }

    // 22. checkEventAndReservationOwnership
    @Test
    void testCheckEventAndReservationOwnership_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(reservationRepository.countReservationsWithEventId(Set.of("r1", "r2"), 10)).thenReturn(2);
        
        assertDoesNotThrow(() -> accessService.checkEventAndReservationOwnership(user, "short", Set.of("r1", "r2")));
    }

    @Test
    void testCheckEventAndReservationOwnership_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(reservationRepository.countReservationsWithEventId(Set.of("r1", "r2"), 10)).thenReturn(1);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventAndReservationOwnership(user, "short", Set.of("r1", "r2")));
    }

    @Test
    void testCheckEventAndReservationOwnership_PartialIds_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(reservationRepository.countReservationWithShortIdsForEvent(anyList(), eq(10))).thenReturn(2);
        
        assertDoesNotThrow(() -> accessService.checkEventAndReservationOwnership(user, "short", Set.of("R1", "R2"), true));
    }

    @Test
    void testCheckEventAndReservationOwnership_PartialIds_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(reservationRepository.countReservationWithShortIdsForEvent(anyList(), eq(10))).thenReturn(1);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventAndReservationOwnership(user, "short", Set.of("R1", "R2"), true));
    }

    // 23. checkEventAndReservationAndTransactionOwnership
    @Test
    void testCheckEventAndReservationAndTransactionOwnership_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(reservationRepository.countReservationsWithEventId(Set.of("r1"), 10)).thenReturn(1);
        when(reservationRepository.hasReservationWithTransactionId("r1", 999)).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkEventAndReservationAndTransactionOwnership(user, "short", "r1", 999));
    }

    @Test
    void testCheckEventAndReservationAndTransactionOwnership_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        
        when(reservationRepository.countReservationsWithEventId(Set.of("r1"), 10)).thenReturn(1);
        when(reservationRepository.hasReservationWithTransactionId("r1", 999)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventAndReservationAndTransactionOwnership(user, "short", "r1", 999));
    }

    // 24. checkTicketMembership
    @Test
    void testCheckTicketMembership_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        ReservationIdAndEventId res = new ReservationIdAndEventId("resId", 10);
        when(reservationRepository.getReservationIdAndEventId(List.of("resId"))).thenReturn(List.of(res));
        
        Ticket ticket = mock(Ticket.class);
        when(ticket.getTicketsReservationId()).thenReturn("resId");
        when(ticketRepository.findByIds(List.of(500))).thenReturn(List.of(ticket));
        
        assertDoesNotThrow(() -> accessService.checkTicketMembership(user, "short", "resId", 500));
    }

    @Test
    void testCheckTicketMembership_TicketCountMismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        ReservationIdAndEventId res = new ReservationIdAndEventId("resId", 10);
        when(reservationRepository.getReservationIdAndEventId(List.of("resId"))).thenReturn(List.of(res));
        
        when(ticketRepository.findByIds(List.of(500))).thenReturn(Collections.emptyList());
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkTicketMembership(user, "short", "resId", 500));
    }

    @Test
    void testCheckTicketMembership_ReservationIdMismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        
        ReservationIdAndEventId res = new ReservationIdAndEventId("resId", 10);
        when(reservationRepository.getReservationIdAndEventId(List.of("resId"))).thenReturn(List.of(res));
        
        Ticket ticket = mock(Ticket.class);
        when(ticket.getTicketsReservationId()).thenReturn("otherResId");
        when(ticketRepository.findByIds(List.of(500))).thenReturn(List.of(ticket));
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkTicketMembership(user, "short", "resId", 500));
    }

    // 25. checkEventTicketIdentifierMembership (two overloads)
    @Test
    void testCheckEventTicketIdentifierMembership_WithEventId_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        when(ticketRepository.isTicketInEvent(10, "t1")).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkEventTicketIdentifierMembership(user, 10, "t1", Set.of(Role.ADMIN)));
    }

    @Test
    void testCheckEventTicketIdentifierMembership_WithEventId_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        when(ticketRepository.isTicketInEvent(10, "t1")).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventTicketIdentifierMembership(user, 10, "t1", Set.of(Role.ADMIN)));
    }

    @Test
    void testCheckEventTicketIdentifierMembership_WithEventName_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(ticketRepository.isTicketInEvent(10, "t1")).thenReturn(true);
        
        EventAndOrganizationId result = accessService.checkEventTicketIdentifierMembership(user, "short", "t1", Set.of(Role.ADMIN));
        assertNotNull(result);
        assertEquals(10, result.getId());
    }

    @Test
    void testCheckEventTicketIdentifierMembership_WithEventName_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(ticketRepository.isTicketInEvent(10, "t1")).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkEventTicketIdentifierMembership(user, "short", "t1", Set.of(Role.ADMIN)));
    }

    // 26. checkAdditionalServiceOwnership
    @Test
    void testCheckAdditionalServiceOwnership_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        when(additionalServiceRepository.additionalServiceExistsForEvent(50, 10)).thenReturn(true);
        
        assertDoesNotThrow(() -> accessService.checkAdditionalServiceOwnership(user, 10, 50));
    }

    @Test
    void testCheckAdditionalServiceOwnership_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findEventAndOrganizationIdById(10)).thenReturn(event);
        when(additionalServiceRepository.additionalServiceExistsForEvent(50, 10)).thenReturn(false);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkAdditionalServiceOwnership(user, 10, 50));
    }

    // 27. checkAccessToAdditionalField
    @Test
    void testCheckAccessToAdditionalField_Event_Success() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(purchaseContextFieldRepository.countMatchingAdditionalFieldsForPurchaseContext(10, null, Set.of(5L))).thenReturn(1);
        
        assertDoesNotThrow(() -> accessService.checkAccessToAdditionalField(user, PurchaseContext.PurchaseContextType.event, "short", 5L));
    }

    @Test
    void testCheckAccessToAdditionalField_Event_Mismatch() {
        Principal user = mockPrincipal("user");
        EventAndOrganizationId event = new EventAndOrganizationId(10, 100);
        when(eventRepository.findOptionalEventAndOrganizationIdByShortName("short")).thenReturn(Optional.of(event));
        when(purchaseContextFieldRepository.countMatchingAdditionalFieldsForPurchaseContext(10, null, Set.of(5L))).thenReturn(0);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToAdditionalField(user, PurchaseContext.PurchaseContextType.event, "short", 5L));
    }

    @Test
    void testCheckAccessToAdditionalField_Subscription_Success() {
        Principal user = mockPrincipal("user");
        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(subId)).thenReturn(Optional.of(100));
        when(purchaseContextFieldRepository.countMatchingAdditionalFieldsForPurchaseContext(null, subId, Set.of(5L))).thenReturn(1);
        
        assertDoesNotThrow(() -> accessService.checkAccessToAdditionalField(user, PurchaseContext.PurchaseContextType.subscription, subId.toString(), 5L));
    }

    @Test
    void testCheckAccessToAdditionalField_Subscription_Mismatch() {
        Principal user = mockPrincipal("user");
        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(subId)).thenReturn(Optional.of(100));
        when(purchaseContextFieldRepository.countMatchingAdditionalFieldsForPurchaseContext(null, subId, Set.of(5L))).thenReturn(0);
        
        assertThrows(AccessDeniedException.class, () -> accessService.checkAccessToAdditionalField(user, PurchaseContext.PurchaseContextType.subscription, subId.toString(), 5L));
    }

    @Test
    void testCheckSubscriptionDescriptorOwnership_NotFound() {
        Principal user = mockPrincipal("user");
        UUID subId = UUID.randomUUID();
        when(subscriptionRepository.findOrganizationIdForDescriptor(subId)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> accessService.checkSubscriptionDescriptorOwnership(user, subId.toString()));
    }
}
