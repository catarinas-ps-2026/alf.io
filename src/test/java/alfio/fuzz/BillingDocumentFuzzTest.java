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

import alfio.model.BillingDocument;
import alfio.util.Json;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

public class BillingDocumentFuzzTest {

    @FuzzTest
    public void fuzzBillingDocumentModelDeserialization(FuzzedDataProvider data) {
        try {
            String modelJson = data.consumeString(2000);
            Json.fromJson(modelJson, new TypeReference<Map<String, Object>>() {});
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzBillingDocumentRoundTrip(FuzzedDataProvider data) {
        try {
            String modelJson = data.consumeString(2000);
            Map<String, Object> model = Json.fromJson(modelJson, new TypeReference<>() {});
            if (model != null) {
                String serialized = Json.toJson(model);
                Json.fromJson(serialized, new TypeReference<Map<String, Object>>() {});
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzBillingDocumentTypeParsing(FuzzedDataProvider data) {
        try {
            String typeStr = data.consumeString(20);
            BillingDocument.Type.valueOf(typeStr);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid enum values
        }
    }

    @FuzzTest
    public void fuzzBillingDocumentStatusParsing(FuzzedDataProvider data) {
        try {
            String statusStr = data.consumeString(20);
            BillingDocument.Status.valueOf(statusStr);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid enum values
        }
    }
}
