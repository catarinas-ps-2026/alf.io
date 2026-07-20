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

import alfio.util.ItalianTaxIdValidator;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class ItalianTaxIdValidatorFuzzTest {

    @FuzzTest
    public void fuzzValidator(FuzzedDataProvider data) {
        try {
            String fiscalCode = data.consumeString(50);
            boolean companyRegistration = data.consumeBoolean();
            ItalianTaxIdValidator.validateFiscalCode(fiscalCode, companyRegistration);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid/special inputs
        }

        try {
            String firstName = data.consumeString(50);
            String lastName = data.consumeString(50);
            String fiscalCode = data.consumeString(50);
            boolean companyRegistration = data.consumeBoolean();
            ItalianTaxIdValidator.fiscalCodeMatchesWithName(firstName, lastName, fiscalCode, companyRegistration);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions
        }

        try {
            String vatId = data.consumeString(50);
            ItalianTaxIdValidator.validateVatId(vatId);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions
        }
    }
}
