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

import alfio.model.UploadedResource;
import alfio.model.modification.UploadBase64FileModification;
import alfio.repository.UploadedResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UploadedResourceManagerTest {

    @Mock
    private UploadedResourceRepository repository;

    private UploadedResourceManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new UploadedResourceManager(repository);
    }

    @Test
    public void testHasResource() {
        when(repository.hasResource("res1")).thenReturn(true);
        assertTrue(manager.hasResource("res1"));

        when(repository.hasResource(1, "res2")).thenReturn(true);
        assertTrue(manager.hasResource(1, "res2"));

        when(repository.hasResource(1, 2, "res3")).thenReturn(true);
        assertTrue(manager.hasResource(1, 2, "res3"));
    }

    @Test
    public void testGetResource() {
        UploadedResource mockResource = mock(UploadedResource.class);
        when(repository.get("res1")).thenReturn(mockResource);
        assertEquals(mockResource, manager.get("res1"));

        when(repository.get(1, "res2")).thenReturn(mockResource);
        assertEquals(mockResource, manager.get(1, "res2"));

        when(repository.get(1, 2, "res3")).thenReturn(mockResource);
        assertEquals(mockResource, manager.get(1, 2, "res3"));
    }

    @Test
    public void testOutputResource() {
        OutputStream out = new ByteArrayOutputStream();
        manager.outputResource("res1", out);
        verify(repository).fileContent("res1", out);

        manager.outputResource(1, "res2", out);
        verify(repository).fileContent(1, "res2", out);

        manager.outputResource(1, 2, "res3", out);
        verify(repository).fileContent(1, 2, "res3", out);
    }

    @Test
    public void testSaveResourceNotAnImage() {
        UploadBase64FileModification file = new UploadBase64FileModification();
        file.setName("test.txt");
        file.setType("text/plain");
        file.setFile("hello".getBytes());
        file.setAttributes(new HashMap<>());

        when(repository.hasResource("test.txt")).thenReturn(false);
        when(repository.upload(eq(null), eq(null), eq(file), anyMap())).thenReturn(42);

        Optional<Integer> result = manager.saveResource(file);
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
        verify(repository, never()).delete(anyString());
    }

    @Test
    public void testSaveResourceIsImage() {
        // 1x1 transparent PNG
        byte[] pngBytes = new byte[] {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00, 0x00, 0x00, 0x0d,
            0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08,
            0x06, 0x00, 0x00, 0x00, 0x1f, 0x15, (byte) 0xc4, (byte) 0x89, 0x00, 0x00, 0x00,
            0x0d, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9c, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0d, 0x0a, 0x2d, (byte) 0xb4, 0x00, 0x00, 0x00, 0x00, 0x49,
            0x45, 0x4e, 0x44, (byte) 0xae, 0x42, 0x60, (byte) 0x82
        };

        UploadBase64FileModification file = new UploadBase64FileModification();
        file.setName("test.png");
        file.setType("image/png");
        file.setFile(pngBytes);
        file.setAttributes(new HashMap<>());

        when(repository.hasResource(1, 2, "test.png")).thenReturn(true);
        when(repository.upload(eq(1), eq(2), eq(file), anyMap())).thenReturn(42);

        Optional<Integer> result = manager.saveResource(1, 2, file);
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
        verify(repository).delete(1, 2, "test.png");
        
        // check that attributes map contains width and height
        verify(repository).upload(eq(1), eq(2), eq(file), argThat(map -> 
            "1".equals(map.get("width")) && "1".equals(map.get("height"))
        ));
    }

    @Test
    public void testDeleteResource() {
        manager.deleteResource("res1");
        verify(repository).delete("res1");

        manager.deleteResource(1, "res2");
        verify(repository).delete(1, "res2");

        manager.deleteResource(1, 2, "res3");
        verify(repository).delete(1, 2, "res3");
    }

    @Test
    public void testFindAll() {
        List<UploadedResource> list = Collections.singletonList(mock(UploadedResource.class));
        when(repository.findAll()).thenReturn(list);
        assertEquals(list, manager.findAll());

        when(repository.findAll(1)).thenReturn(list);
        assertEquals(list, manager.findAll(1));

        when(repository.findAll(1, 2)).thenReturn(list);
        assertEquals(list, manager.findAll(1, 2));
    }

    @Test
    public void testFindCascading() {
        // Cascading event level
        when(repository.hasResource(1, 2, "logo.png")).thenReturn(true);
        doAnswer(inv -> {
            OutputStream os = inv.getArgument(3);
            os.write("event-logo".getBytes());
            return null;
        }).when(repository).fileContent(eq(1), eq(2), eq("logo.png"), any());

        Optional<byte[]> result = manager.findCascading(1, 2, "logo.png");
        assertTrue(result.isPresent());
        assertEquals("event-logo", new String(result.get()));

        // Cascading org level
        when(repository.hasResource(1, 2, "logo.png")).thenReturn(false);
        when(repository.hasResource(1, "logo.png")).thenReturn(true);
        doAnswer(inv -> {
            OutputStream os = inv.getArgument(2);
            os.write("org-logo".getBytes());
            return null;
        }).when(repository).fileContent(eq(1), eq("logo.png"), any());

        Optional<byte[]> result2 = manager.findCascading(1, 2, "logo.png");
        assertTrue(result2.isPresent());
        assertEquals("org-logo", new String(result2.get()));

        // Cascading system level
        when(repository.hasResource(1, 2, "logo.png")).thenReturn(false);
        when(repository.hasResource(1, "logo.png")).thenReturn(false);
        when(repository.hasResource("logo.png")).thenReturn(true);
        doAnswer(inv -> {
            OutputStream os = inv.getArgument(1);
            os.write("system-logo".getBytes());
            return null;
        }).when(repository).fileContent(eq("logo.png"), any());

        Optional<byte[]> result3 = manager.findCascading(1, 2, "logo.png");
        assertTrue(result3.isPresent());
        assertEquals("system-logo", new String(result3.get()));

        // None found
        when(repository.hasResource(1, 2, "logo.png")).thenReturn(false);
        when(repository.hasResource(1, "logo.png")).thenReturn(false);
        when(repository.hasResource("logo.png")).thenReturn(false);
        Optional<byte[]> result4 = manager.findCascading(1, 2, "logo.png");
        assertFalse(result4.isPresent());
    }
}
