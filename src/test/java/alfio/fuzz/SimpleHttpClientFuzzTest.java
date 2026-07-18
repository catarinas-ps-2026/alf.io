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
package alfio.fuzz;

import alfio.extension.SimpleHttpClient;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class SimpleHttpClientFuzzTest {

    private static final SimpleHttpClient CLIENT =
        new SimpleHttpClient(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build());

    @FuzzTest
    public void fuzzUriCreation(FuzzedDataProvider data) {
        try {
            String url = data.consumeString(500);
            URI.create(url);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid URIs
        }
    }

    @FuzzTest
    public void fuzzGetWithUrl(FuzzedDataProvider data) {
        try {
            String url = data.consumeString(500);
            CLIENT.get(url);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        } catch (Exception e) {
            // IOException, InterruptedException, SecurityException from actual HTTP calls
            // We only care about crashes from URL parsing/construction
        }
    }

    @FuzzTest
    public void fuzzHeadWithUrl(FuzzedDataProvider data) {
        try {
            String url = data.consumeString(500);
            CLIENT.head(url);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        } catch (Exception e) {
            // IOException, InterruptedException from actual HTTP calls
        }
    }

    @FuzzTest
    public void fuzzBasicCredentials(FuzzedDataProvider data) {
        try {
            String username = data.consumeString(200);
            String password = data.consumeString(200);
            String result = CLIENT.basicCredentials(username, password);
            // basicAuth should always return a "Basic <base64>" string
            if (result != null && !result.startsWith("Basic ")) {
                throw new AssertionError("basicCredentials did not return Basic auth header");
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
