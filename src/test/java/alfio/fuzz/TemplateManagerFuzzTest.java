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

import alfio.util.TemplateManager;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.util.Locale;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.StaticMessageSource;

public class TemplateManagerFuzzTest {

    private static final StaticMessageSource MESSAGE_SOURCE = new StaticMessageSource();

    @FuzzTest
    public void fuzzTranslate(FuzzedDataProvider data) {
        try {
            String template = data.consumeString(2000);
            TemplateManager.translate(template, Locale.ENGLISH, MESSAGE_SOURCE);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException | NoSuchMessageException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzTranslateWithLocales(FuzzedDataProvider data) {
        try {
            String template = data.consumeString(2000);
            Locale[] locales = {
                Locale.ITALIAN,
                Locale.GERMAN,
                Locale.FRENCH,
                Locale.forLanguageTag("pt-BR"),
                Locale.forLanguageTag("nl-NL"),
                Locale.forLanguageTag("ro-RO"),
                Locale.forLanguageTag("tr-TR"),
                Locale.forLanguageTag("pl-PL"),
                Locale.forLanguageTag("da-DK"),
                Locale.forLanguageTag("bg-BG"),
                Locale.forLanguageTag("sv-SE"),
                Locale.forLanguageTag("cs-CZ"),
                Locale.forLanguageTag("es-ES")
            };
            Locale locale = locales[data.consumeInt(0, locales.length - 1)];
            TemplateManager.translate(template, locale, MESSAGE_SOURCE);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException | NoSuchMessageException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzTranslateNestedI18n(FuzzedDataProvider data) {
        try {
            // Construct templates with nested i18n tags to stress the AST parser
            String inner = data.consumeString(200);
            String outer = data.consumeString(200);
            String template = "{{#i18n}}" + inner + "{{/i18n}}" + outer + "{{#i18n}}" + inner + "{{/i18n}}";
            TemplateManager.translate(template, Locale.ENGLISH, MESSAGE_SOURCE);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException | NoSuchMessageException e) {
            // catch exceptions from invalid inputs
        }
    }
}
