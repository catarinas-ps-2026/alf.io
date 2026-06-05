package alfio.repository;

import alfio.model.modification.GroupMemberModification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final GroupRepository groupRepository = mock(GroupRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testInsertBatch() {
        when(groupRepository.getNamedParameterJdbcTemplate()).thenReturn(jdbcTemplate);
        
        List<GroupMemberModification> members = List.of(
            new GroupMemberModification(null, "m1", "desc1"),
            new GroupMemberModification(null, "M2", "desc2")
        );
        
        int[] expectedResult = new int[]{1, 1};
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class))).thenReturn(expectedResult);
        
        int[] result = groupRepository.insert(123, members);
        assertArrayEquals(expectedResult, result);
        
        verify(jdbcTemplate).batchUpdate(eq("insert into group_member(a_group_id_fk, value, description) values(:groupId, :value, :description)"), any(MapSqlParameterSource[].class));
    }

    @Test
    void testDeactivateGroupMember() {
        when(groupRepository.getNamedParameterJdbcTemplate()).thenReturn(jdbcTemplate);
        
        List<Integer> memberIds = List.of(1, 2);
        groupRepository.deactivateGroupMember(memberIds, 123);
        
        verify(jdbcTemplate).batchUpdate(eq("update group_member set active = false, value = 'DISABLED-' || :disabledPlaceholder where id = :memberId and a_group_id_fk = :groupId"), any(MapSqlParameterSource[].class));
    }
}
