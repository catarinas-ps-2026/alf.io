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

import alfio.model.ContentLanguage;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class ContentLanguageFuzzTest {

    @FuzzTest
    public void fuzzFindAllFor(FuzzedDataProvider data) {
        try {
            int bitMask = data.consumeInt();
            ContentLanguage.findAllFor(bitMask);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzFindAllForVariousBitMasks(FuzzedDataProvider data) {
        try {
            // Test with powers of 2 and common bitmask patterns
            int bitMask = data.consumeInt(0, ContentLanguage.ALL_LANGUAGES_IDENTIFIER);
            var result = ContentLanguage.findAllFor(bitMask);
            // Verify result properties
            if (!result.isEmpty()) {
                result.forEach(cl -> {
                    cl.getLanguage();
                    cl.getDisplayLanguage();
                    cl.value();
                    cl.locale();
                });
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
