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
package alfio.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import alfio.model.modification.GroupMemberModification;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@ExtendWith(MockitoExtension.class)
class GroupRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final GroupRepository groupRepository =
            mock(GroupRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testInsertBatch() {
        when(groupRepository.getNamedParameterJdbcTemplate()).thenReturn(jdbcTemplate);

        List<GroupMemberModification> members = List.of(
                new GroupMemberModification(null, "m1", "desc1"), new GroupMemberModification(null, "M2", "desc2"));

        int[] expectedResult = new int[] {1, 1};
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class)))
                .thenReturn(expectedResult);

        int[] result = groupRepository.insert(123, members);
        assertArrayEquals(expectedResult, result);

        verify(jdbcTemplate)
                .batchUpdate(
                        eq(
                                "insert into group_member(a_group_id_fk, value, description) values(:groupId, :value, :description)"),
                        any(MapSqlParameterSource[].class));
    }

    @Test
    void testDeactivateGroupMember() {
        when(groupRepository.getNamedParameterJdbcTemplate()).thenReturn(jdbcTemplate);

        List<Integer> memberIds = List.of(1, 2);
        groupRepository.deactivateGroupMember(memberIds, 123);

        verify(jdbcTemplate)
                .batchUpdate(
                        eq(
                                "update group_member set active = false, value = 'DISABLED-' || :disabledPlaceholder where id = :memberId and a_group_id_fk = :groupId"),
                        any(MapSqlParameterSource[].class));
    }
}
