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

import alfio.model.modification.UploadBase64FileModification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileUploadRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final JdbcOperations jdbcOperations = mock(JdbcOperations.class);
    private final FileUploadRepository fileUploadRepository = mock(FileUploadRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testUpload() {
        doReturn(jdbcTemplate).when(fileUploadRepository).getNamedParameterJdbcTemplate();
        when(jdbcTemplate.getJdbcOperations()).thenReturn(jdbcOperations);

        UploadBase64FileModification fileMod = mock(UploadBase64FileModification.class);
        when(fileMod.getFile()).thenReturn(new byte[]{1, 2, 3});
        when(fileMod.getName()).thenReturn("test.txt");
        when(fileMod.getType()).thenReturn("text/plain");

        fileUploadRepository.upload(fileMod, "digest", Map.of("key", "value"));

        verify(jdbcOperations).execute(anyString(), any(PreparedStatementCallback.class));
    }

    @Test
    void testFile() throws SQLException, IOException {
        doReturn(jdbcTemplate).when(fileUploadRepository).getNamedParameterJdbcTemplate();
        
        ResultSet rs = mock(ResultSet.class);
        byte[] content = "hello world".getBytes();
        when(rs.getBinaryStream("content")).thenReturn(new ByteArrayInputStream(content));
        
        when(jdbcTemplate.query(anyString(), any(SqlParameterSource.class), any(ResultSetExtractor.class)))
            .thenAnswer(invocation -> {
                ResultSetExtractor<?> extractor = invocation.getArgument(2);
                return extractor.extractData(rs);
            });

        File result = fileUploadRepository.file("some-id");
        assertNotNull(result);
        if (result != null) {
            result.delete();
        }
    }
}
