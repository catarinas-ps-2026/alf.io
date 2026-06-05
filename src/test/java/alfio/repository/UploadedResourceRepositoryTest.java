package alfio.repository;

import alfio.model.modification.UploadBase64FileModification;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UploadedResourceRepositoryTest {

    private final NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
    private final JdbcOperations jdbcOperations = mock(JdbcOperations.class);
    private final UploadedResourceRepository repository = mock(UploadedResourceRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testUpload() {
        doReturn(jdbcTemplate).when(repository).getNamedParameterJdbcTemplate();
        when(jdbcTemplate.getJdbcOperations()).thenReturn(jdbcOperations);
        
        UploadBase64FileModification file = mock(UploadBase64FileModification.class);
        when(file.getFile()).thenReturn(new byte[]{1, 2});
        
        repository.upload(1, 2, file, Collections.emptyMap());
        verify(jdbcOperations).execute(anyString(), any(PreparedStatementCallback.class));
    }

    @Test
    void testFileContentGlobal() throws Exception {
        doReturn(jdbcTemplate).when(repository).getNamedParameterJdbcTemplate();
        java.io.OutputStream out = new java.io.ByteArrayOutputStream();
        
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.getBinaryStream("content")).thenReturn(new java.io.ByteArrayInputStream("hello".getBytes()));
        
        doAnswer(invocation -> {
            org.springframework.jdbc.core.RowCallbackHandler rch = invocation.getArgument(2);
            rch.processRow(rs);
            return null;
        }).when(jdbcTemplate).query(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), any(org.springframework.jdbc.core.RowCallbackHandler.class));
        
        repository.fileContent("test", out);
        assertEquals("hello", out.toString());
    }

    @Test
    void testFileContentOrganizer() throws Exception {
        doReturn(jdbcTemplate).when(repository).getNamedParameterJdbcTemplate();
        java.io.OutputStream out = new java.io.ByteArrayOutputStream();
        
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.getBinaryStream("content")).thenReturn(new java.io.ByteArrayInputStream("organizer".getBytes()));
        
        doAnswer(invocation -> {
            org.springframework.jdbc.core.RowCallbackHandler rch = invocation.getArgument(2);
            rch.processRow(rs);
            return null;
        }).when(jdbcTemplate).query(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), any(org.springframework.jdbc.core.RowCallbackHandler.class));
        
        repository.fileContent(1, "test", out);
        assertEquals("organizer", out.toString());
    }

    @Test
    void testFileContentEvent() throws Exception {
        doReturn(jdbcTemplate).when(repository).getNamedParameterJdbcTemplate();
        java.io.OutputStream out = new java.io.ByteArrayOutputStream();
        
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.getBinaryStream("content")).thenReturn(new java.io.ByteArrayInputStream("event".getBytes()));
        
        doAnswer(invocation -> {
            org.springframework.jdbc.core.RowCallbackHandler rch = invocation.getArgument(2);
            rch.processRow(rs);
            return null;
        }).when(jdbcTemplate).query(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class), any(org.springframework.jdbc.core.RowCallbackHandler.class));
        
        repository.fileContent(1, 2, "test", out);
        assertEquals("event", out.toString());
    }
}
