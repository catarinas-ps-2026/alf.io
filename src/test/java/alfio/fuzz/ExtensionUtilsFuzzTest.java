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

import alfio.extension.ExtensionUtils;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class ExtensionUtilsFuzzTest {

    @FuzzTest
    public void fuzzMd5(FuzzedDataProvider data) {
        try {
            String input = data.consumeString(500);
            ExtensionUtils.md5(input);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzComputeHMAC(FuzzedDataProvider data) {
        try {
            String secret = data.consumeString(100);
            int numParts = data.consumeInt(0, 10);
            String[] parts = new String[numParts];
            for (int i = 0; i < numParts; i++) {
                parts[i] = data.consumeString(50);
            }
            ExtensionUtils.computeHMAC(secret, parts);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzFormat(FuzzedDataProvider data) {
        try {
            String str = data.consumeString(200);
            int numParams = data.consumeInt(0, 10);
            String[] params = new String[numParams];
            for (int i = 0; i < numParams; i++) {
                params[i] = data.consumeString(30);
            }
            ExtensionUtils.format(str, params);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzBase64UrlSafe(FuzzedDataProvider data) {
        try {
            String input = data.consumeString(500);
            ExtensionUtils.base64UrlSafe(input);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzFormatDateTime(FuzzedDataProvider data) {
        try {
            String dateTimeAsString = data.consumeString(50);
            String formatPattern = data.consumeString(50);
            boolean utc = data.consumeBoolean();
            ExtensionUtils.formatDateTime(dateTimeAsString, formatPattern, utc);
        } catch (Exception e) {
            // catch exceptions from invalid inputs (DateTimeParseException, etc.)
        }
    }
}
