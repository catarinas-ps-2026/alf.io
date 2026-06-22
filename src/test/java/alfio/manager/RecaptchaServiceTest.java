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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

public class RecaptchaServiceTest {

    @Mock
    private HttpClient client;

    @Mock
    private ConfigurationManager configurationManager;

    @Mock
    private HttpServletRequest request;

    private RecaptchaService service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RecaptchaService(client, configurationManager);
    }

    @Test
    public void testCheckRecaptchaSecretNotPresent() {
        MaybeConfiguration secretConfig = mock(MaybeConfiguration.class);
        when(secretConfig.getValue()).thenReturn(Optional.empty());
        when(configurationManager.getForSystem(any())).thenReturn(secretConfig);

        assertTrue(service.checkRecaptcha("response-token", request));
    }

    @Test
    public void testCheckRecaptchaNullResponse() {
        MaybeConfiguration secretConfig = mock(MaybeConfiguration.class);
        when(secretConfig.getValue()).thenReturn(Optional.of("my-secret"));
        when(configurationManager.getForSystem(any())).thenReturn(secretConfig);

        when(request.getParameter("g-recaptcha-response")).thenReturn(null);

        assertFalse(service.checkRecaptcha(null, request));
    }

    @Test
    public void testCheckRecaptchaSuccess() throws Exception {
        MaybeConfiguration secretConfig = mock(MaybeConfiguration.class);
        when(secretConfig.getValue()).thenReturn(Optional.of("my-secret"));
        when(configurationManager.getForSystem(any())).thenReturn(secretConfig);

        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            when(mockResponse.body()).thenReturn("{\"success\":true}");
            httpUtilsMockedStatic
                    .when(() -> HttpUtils.postForm(eq(client), anyString(), anyMap()))
                    .thenReturn(mockResponse);

            assertTrue(service.checkRecaptcha("response-token", request));
        }
    }

    @Test
    public void testCheckRecaptchaFailedResponse() throws Exception {
        MaybeConfiguration secretConfig = mock(MaybeConfiguration.class);
        when(secretConfig.getValue()).thenReturn(Optional.of("my-secret"));
        when(configurationManager.getForSystem(any())).thenReturn(secretConfig);

        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            when(mockResponse.body()).thenReturn("{\"success\":false}");
            httpUtilsMockedStatic
                    .when(() -> HttpUtils.postForm(eq(client), anyString(), anyMap()))
                    .thenReturn(mockResponse);

            assertFalse(service.checkRecaptcha("response-token", request));
        }
    }
}
