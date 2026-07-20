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

import alfio.extension.ScriptValidation;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class ScriptValidationFuzzTest {

    @FuzzTest
    public void fuzzScriptValidation(FuzzedDataProvider data) {
        try {
            String script = data.consumeString(2000);
            ScriptValidation scriptValidation = new ScriptValidation(script);
            scriptValidation.validate();
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        } catch (Exception e) {
            // ScriptValidation can throw ScriptNotValidException which is expected
        }
    }
}
