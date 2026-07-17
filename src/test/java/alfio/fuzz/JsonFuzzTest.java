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

import alfio.util.Json;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;

public class JsonFuzzTest {

    @FuzzTest
    public void fuzzJsonDeserializeMap(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Json.fromJson(json, Map.class);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonDeserializeString(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Json.fromJson(json, String.class);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonDeserializeTypeReference(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Json.fromJson(json, new TypeReference<Map<String, Object>>() {});
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonDeserializeList(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Json.fromJson(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonSerializeDeserialize(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Map<String, Object> map = Json.fromJson(json, new TypeReference<>() {});
            if (map != null) {
                String serialized = Json.toJson(map);
                Json.fromJson(serialized, new TypeReference<Map<String, Object>>() {});
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
