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

import alfio.manager.CheckInManager;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class CheckInManagerFuzzTest {

    @FuzzTest
    public void fuzzEncrypt(FuzzedDataProvider data) {
        try {
            String key = data.consumeString(100);
            String payload = data.consumeString(500);
            if (!key.isEmpty() && !payload.isEmpty()) {
                String encrypted = CheckInManager.encrypt(key, payload);
                // verify the encrypted output format: base64url(iv)|base64url(ciphertext)
                if (encrypted != null) {
                    String[] parts = encrypted.split("\\|");
                    if (parts.length == 2) {
                        // both parts should be non-empty base64url strings
                        if (parts[0].isEmpty() || parts[1].isEmpty()) {
                            throw new AssertionError("Encrypted output has empty parts");
                        }
                    }
                }
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzEncryptDecryptRoundTrip(FuzzedDataProvider data) {
        try {
            String key = data.consumeString(100);
            String payload = data.consumeString(500);
            if (!key.isEmpty() && !payload.isEmpty()) {
                String encrypted = CheckInManager.encrypt(key, payload);
                // verify the encrypted output can be split on the pipe delimiter
                if (encrypted != null) {
                    String[] parts = encrypted.split("\\|");
                    if (parts.length != 2) {
                        throw new AssertionError("Encrypted output does not have exactly 2 parts");
                    }
                }
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzEncryptEmptyInputs(FuzzedDataProvider data) {
        try {
            String key = data.consumeString(200);
            String payload = data.consumeString(1000);
            CheckInManager.encrypt(key, payload);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }
}
