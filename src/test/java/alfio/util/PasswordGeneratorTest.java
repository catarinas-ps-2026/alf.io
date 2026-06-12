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
package alfio.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordGeneratorTest {

    private static final Pattern VALIDATION_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\p{Punct})(?=\\S+$).{10,}$"
    );

    @Test
    void generateRandomPasswordReturnsNonNull() {
        String password = PasswordGenerator.generateRandomPassword();
        assertNotNull(password);
    }

    @Test
    void generateRandomPasswordLengthWithinRange() {
        String password = PasswordGenerator.generateRandomPassword();
        assertTrue(password.length() >= 10 && password.length() <= 20,
            "Generated password length should be between 10 and 20, but was: " + password.length());
    }

    @Test
    void generateRandomPasswordDifferentCallsProduceDifferentPasswords() {
        Set<String> generatedPasswords = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            generatedPasswords.add(PasswordGenerator.generateRandomPassword());
        }
        assertTrue(generatedPasswords.size() > 1,
            "Multiple calls to generateRandomPassword should produce different values");
    }

    @Test
    void generateRandomPasswordContainsOnlyValidCharacters() {
        String password = PasswordGenerator.generateRandomPassword();
        String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#~!-_/^&+%()=";
        for (char c : password.toCharArray()) {
            assertTrue(validChars.indexOf(c) >= 0,
                "Generated password contains invalid character: " + c);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",
        "   ",
        "\t",
        "\n"
    })
    void isValidReturnsFalseForBlankOrNullPassword(String password) {
        assertFalse(PasswordGenerator.isValid(password),
            "isValid should return false for blank/null password: " + password);
    }

    @Test
    void isValidReturnsFalseForPasswordMissingDigit() {
        String passwordWithoutDigit = "Abcd!~!@#";
        assertFalse(PasswordGenerator.isValid(passwordWithoutDigit),
            "Password without digit should be invalid");
    }

    @Test
    void isValidReturnsFalseForPasswordMissingLowercase() {
        String passwordWithoutLowercase = "ABCD123!~";
        assertFalse(PasswordGenerator.isValid(passwordWithoutLowercase),
            "Password without lowercase letter should be invalid");
    }

    @Test
    void isValidReturnsFalseForPasswordMissingUppercase() {
        String passwordWithoutUppercase = "abcd123!~";
        assertFalse(PasswordGenerator.isValid(passwordWithoutUppercase),
            "Password without uppercase letter should be invalid");
    }

    @Test
    void isValidReturnsFalseForPasswordMissingSpecialCharacter() {
        String passwordWithoutSpecialChar = "Abcd1234ef";
        assertFalse(PasswordGenerator.isValid(passwordWithoutSpecialChar),
            "Password without special character should be invalid");
    }

    @Test
    void isValidReturnsFalseForPasswordWithSpace() {
        String passwordWithSpace = "Abcd123 !~";
        assertFalse(PasswordGenerator.isValid(passwordWithSpace),
            "Password with space should be invalid");
    }

    @Test
    void isValidReturnsFalseForPasswordShorterThanMinLength() {
        String shortPassword = "Abc1!";
        assertTrue(shortPassword.length() < 10, "Test setup: password should be shorter than 10");
        assertFalse(PasswordGenerator.isValid(shortPassword),
            "Password shorter than 10 characters should be invalid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Abcdef123!",
        "MyPassword1#",
        "Test@Pass99",
        "Complex!Pwd2024",
        "Secure~Pass123",
        "Valid_Pass99!",
        "Another^Pass01",
        "Example&Pass12"
    })
    void isValidReturnsTrueForValidPassword(String password) {
        assertTrue(PasswordGenerator.isValid(password),
            "Valid password should pass validation: " + password);
    }

    @Test
    void isValidWithMultipleSpecialCharactersValid() {
        String passwordMultipleSpecialChars = "Abc123!~#A";
        assertTrue(PasswordGenerator.isValid(passwordMultipleSpecialChars),
            "Password with multiple special characters should be valid");
    }

    @Test
    void isValidWithAllRequiredCharacterTypes() {
        String completePassword = "P@ssw0rd12";
        assertTrue(PasswordGenerator.isValid(completePassword),
            "Password with all required character types should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Abcd1!@#$%",
        "Test1!~@#A",
        "Pass(word)1",
        "Valid_Pass=99",
        "Secure/Pwd^001"
    })
    void isValidWithVariousSpecialCharactersValid(String password) {
        assertTrue(PasswordGenerator.isValid(password),
            "Password with various special characters should be valid: " + password);
    }
}
