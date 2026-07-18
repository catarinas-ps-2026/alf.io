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

import alfio.util.PinGenerator;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class PinGeneratorFuzzTest {

    @FuzzTest
    public void fuzzIsPinValid(FuzzedDataProvider data) {
        try {
            String pin = data.consumeString(20);
            int pinLength = data.consumeInt(1, 20);
            PinGenerator.isPinValid(pin, pinLength);
        } catch (Exception e) {
            // catch exceptions from invalid inputs (StringIndexOutOfBoundsException, etc.)
        }
    }

    @FuzzTest
    public void fuzzPinToPartialUuid(FuzzedDataProvider data) {
        try {
            String pin = data.consumeString(20);
            int pinLength = data.consumeInt(1, 20);
            PinGenerator.pinToPartialUuid(pin, pinLength);
        } catch (Exception e) {
            // catch exceptions from invalid inputs (StringIndexOutOfBoundsException, etc.)
        }
    }

    @FuzzTest
    public void fuzzUuidToPin(FuzzedDataProvider data) {
        try {
            String uuid = data.consumeString(40);
            int pinLength = data.consumeInt(1, 20);
            PinGenerator.uuidToPin(uuid, pinLength);
        } catch (Exception e) {
            // catch exceptions from invalid inputs (StringIndexOutOfBoundsException, etc.)
        }
    }

    @FuzzTest
    public void fuzzUuidToPinRoundTrip(FuzzedDataProvider data) {
        try {
            String uuid = data.consumeString(40);
            int pinLength = data.consumeInt(1, 20);
            String pin = PinGenerator.uuidToPin(uuid, pinLength);
            if (pin != null && !pin.isEmpty()) {
                String partialUuid = PinGenerator.pinToPartialUuid(pin, pinLength);
                // partialUuid should be non-null and shorter than the original uuid
                if (partialUuid == null) {
                    throw new AssertionError("pinToPartialUuid returned null");
                }
            }
        } catch (Exception e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzIsPinValidWithGeneratedPin(FuzzedDataProvider data) {
        try {
            String uuid = data.consumeString(40);
            int pinLength = data.consumeInt(1, 20);
            String pin = PinGenerator.uuidToPin(uuid, pinLength);
            if (pin != null && !pin.isEmpty()) {
                boolean valid = PinGenerator.isPinValid(pin, pinLength);
                if (!valid) {
                    throw new AssertionError("Generated pin should be valid");
                }
            }
        } catch (Exception e) {
            // catch exceptions from invalid inputs
        }
    }
}
