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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileDownloadManagerTest {

    @Mock
    private HttpClient httpClient;

    private FileDownloadManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new FileDownloadManager(httpClient);
    }

    @Test
    public void testDownloadFileSuccess() throws Exception {
        String url = "https://example.com/images/logo.png";
        byte[] body = "image-bytes".getBytes();
        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(body);

        HttpHeaders mockHeaders = HttpHeaders.of(
                Map.of("Content-Type", List.of("image/png")),
                (s1, s2) -> true
        );
        when(mockResponse.headers()).thenReturn(mockHeaders);

        when(httpClient.<byte[]>send(any(), any())).thenReturn(mockResponse);

        FileDownloadManager.DownloadedFile file = manager.downloadFile(url);

        assertNotNull(file);
        assertArrayEquals(body, file.getFile());
        assertEquals("logo.png", file.getName());
        assertEquals("image/png", file.getType());

        var uploadMod = file.toUploadBase64FileModification();
        assertArrayEquals(body, uploadMod.getFile());
        assertEquals("logo.png", uploadMod.getName());
        assertEquals("image/png", uploadMod.getType());
    }

    @Test
    public void testDownloadFileServerError() throws Exception {
        String url = "https://example.com/images/logo.png";
        HttpResponse<byte[]> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);

        when(httpClient.<byte[]>send(any(), any())).thenReturn(mockResponse);

        FileDownloadManager.DownloadedFile file = manager.downloadFile(url);

        assertNull(file);
    }

    @Test
    public void testDownloadFileIOException() throws Exception {
        String url = "https://example.com/images/logo.png";
        when(httpClient.<byte[]>send(any(), any())).thenThrow(new IOException("Network error"));

        FileDownloadManager.DownloadedFile file = manager.downloadFile(url);

        assertNull(file);
    }

    @Test
    public void testDownloadFileInterruptedException() throws Exception {
        String url = "https://example.com/images/logo.png";
        when(httpClient.<byte[]>send(any(), any())).thenThrow(new InterruptedException("Interrupted"));

        FileDownloadManager.DownloadedFile file = manager.downloadFile(url);

        assertNull(file);
        assertTrue(Thread.currentThread().isInterrupted());
    }
}
