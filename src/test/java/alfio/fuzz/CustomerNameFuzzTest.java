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

import alfio.model.CustomerName;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class CustomerNameFuzzTest {

    @FuzzTest
    public void fuzzCustomerNameWithFullName(FuzzedDataProvider data) {
        try {
            String fullName = data.consumeString(100);
            String firstName = data.consumeString(100);
            String lastName = data.consumeString(100);
            boolean mustUseFirstAndLastName = false;
            boolean validate = data.consumeBoolean();
            new CustomerName(fullName, firstName, lastName, mustUseFirstAndLastName, validate);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzCustomerNameWithFirstAndLastName(FuzzedDataProvider data) {
        try {
            String fullName = data.consumeString(100);
            String firstName = data.consumeString(100);
            String lastName = data.consumeString(100);
            boolean mustUseFirstAndLastName = true;
            boolean validate = data.consumeBoolean();
            new CustomerName(fullName, firstName, lastName, mustUseFirstAndLastName, validate);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
