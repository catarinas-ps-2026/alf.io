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
package alfio.controller.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import alfio.TestConfiguration;
import alfio.config.DataSourceConfiguration;
import alfio.config.Initializer;
import alfio.util.Json;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.output.HtmlRender;
import org.openapitools.openapidiff.core.output.MarkdownRender;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.utils.Constants;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(
    classes = {
        DataSourceConfiguration.class,
        TestConfiguration.class,
        ControllerConfiguration.class,
        CheckRestApiStabilityIntegrationTest.DisableSecurity.class,
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class,
    }
)
@ActiveProfiles({
    Initializer.PROFILE_DEV,
    Initializer.PROFILE_DISABLE_JOBS,
    Initializer.PROFILE_INTEGRATION_TEST,
})
class CheckRestApiStabilityIntegrationTest {

    private static final String DESCRIPTOR_JSON_PATH =
        "src/test/resources/api/descriptor.json";

    @Autowired
    private MockMvc mockMvc;

    private final boolean updateDescriptor = false; // change to true to regenerate the file

    @Test
    void checkRestApiStability() throws Exception {
        var mvcResult = this.mockMvc
            .perform(get(Constants.DEFAULT_API_DOCS_URL))
            .andExpect(status().isOk())
            .andReturn();

        var response = mvcResult.getResponse();
        var content = response.getContentAsString();
        // for some reason we get a quoted base64 JSON: "ey..."
        var descriptor = Base64.getDecoder().decode(
            content.substring(1, content.length() - 1)
        );

        // for generating the result
        if (updateDescriptor) {
            try (
                var writer = Files.newBufferedWriter(
                    Paths.get(DESCRIPTOR_JSON_PATH),
                    StandardCharsets.UTF_8
                )
            ) {
                var formattedDescriptor = Json.OBJECT_MAPPER.readTree(
                    descriptor
                ).toPrettyString();
                writer.write(formattedDescriptor);
            }
        }

        var apiDocsDir = Paths.get("build/api-docs");
        Files.createDirectories(apiDocsDir);
        try (
            var writer = Files.newBufferedWriter(
                apiDocsDir.resolve("openapi.json"),
                StandardCharsets.UTF_8
            )
        ) {
            var formattedDescriptor = Json.OBJECT_MAPPER.readTree(
                descriptor
            ).toPrettyString();
            writer.write(formattedDescriptor);
        }

        var redocHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <title>API Contract Portal</title>\n" +
                "  <meta charset=\"utf-8\"/>\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "  <link href=\"https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700\" rel=\"stylesheet\">\n" +
                "  <style>body { margin: 0; padding: 0; }</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <redoc spec-url='openapi.json'></redoc>\n" +
                "  <script src=\"https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js\"> </script>\n" +
                "</body>\n" +
                "</html>";
        Files.writeString(apiDocsDir.resolve("index.html"), redocHtml, StandardCharsets.UTF_8);

        var referenceDescriptor = IOUtils.toString(
            new FileReader(DESCRIPTOR_JSON_PATH)
        );
        var currentDescriptor = IOUtils.toString(
            descriptor,
            StandardCharsets.UTF_8.toString()
        );
        var compareResult = OpenApiCompare.fromContents(
            referenceDescriptor,
            currentDescriptor
        );

        var diffOut = new ByteArrayOutputStream();
        new HtmlRender().render(compareResult, new OutputStreamWriter(diffOut));
        var htmlContent = diffOut.toString(StandardCharsets.UTF_8);
        if (htmlContent.isEmpty() || !compareResult.isDifferent()) {
            htmlContent = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head><title>API Contract Diff</title></head>\n" +
                    "<body>\n" +
                    "  <h1>API Contract Diff Report</h1>\n" +
                    "  <p style=\"color: green; font-weight: bold;\">No backward-incompatible changes detected! API contract is stable.</p>\n" +
                    "</body>\n" +
                    "</html>";
        }
        Files.writeString(apiDocsDir.resolve("openapi-diff.html"), htmlContent, StandardCharsets.UTF_8);

        if (compareResult.isDifferent()) {
            var out = new ByteArrayOutputStream();
            new MarkdownRender().render(
                compareResult,
                new OutputStreamWriter(out)
            );
            Assertions.fail(out.toString(StandardCharsets.UTF_8));
        }
    }

    @EnableWebSecurity
    @Configuration
    public static class DisableSecurity {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
            return http
                .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/**").permitAll()
                )
                .build();
        }
    }
}
