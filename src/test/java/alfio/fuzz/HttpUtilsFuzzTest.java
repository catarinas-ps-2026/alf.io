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

import alfio.util.HttpUtils;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class HttpUtilsFuzzTest {

    @FuzzTest
    public void fuzzBasicAuth(FuzzedDataProvider data) {
        try {
            String username = data.consumeString(200);
            String password = data.consumeString(200);
            String result = HttpUtils.basicAuth(username, password);
            if (result != null && !result.startsWith("Basic ")) {
                throw new AssertionError("basicAuth did not return Basic auth header");
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzOfFormUrlEncodedBody(FuzzedDataProvider data) {
        try {
            int numParams = data.consumeInt(0, 20);
            Map<String, String> params = new HashMap<>();
            for (int i = 0; i < numParams; i++) {
                String key = data.consumeString(50);
                String value = data.consumeString(200);
                params.put(key, value);
            }
            HttpUtils.ofFormUrlEncodedBody(params);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzCallSuccessful(FuzzedDataProvider data) {
        try {
            int statusCode = data.consumeInt(-1, 600);
            // Test the static method with a mock-like response
            // callSuccessful checks the status code range
            boolean result = HttpUtils.statusCodeIsSuccessful(statusCode);
            // Status codes 200-299 should be successful
            if (statusCode >= 200 && statusCode <= 299) {
                if (!result) {
                    throw new AssertionError("Status code " + statusCode + " should be successful");
                }
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
