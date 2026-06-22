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
package alfio.job.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import alfio.extension.ExtensionService;
import alfio.extension.ScriptingExecutionService;
import alfio.manager.system.AdminJobExecutor;
import alfio.model.system.AdminJobSchedule;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RetryFailedExtensionJobExecutorTest {

    @Test
    void processRetriesFailedScriptWithMetadataPayload() {
        var extensionService = mock(ExtensionService.class);
        var executor = new RetryFailedExtensionJobExecutor(extensionService);
        var payload = Map.<String, Object>of("eventId", 42);

        assertEquals(
                "OK",
                executor.process(schedule(Map.of(
                        ScriptingExecutionService.EXTENSION_NAME, "extensionName",
                        ScriptingExecutionService.EXTENSION_PATH, "/path",
                        ScriptingExecutionService.EXTENSION_PARAMS, payload))));

        verify(extensionService).retryFailedAsyncScript("/path", "extensionName", payload);
        verifyNoMoreInteractions(extensionService);
    }

    @Test
    void processRethrowsRuntimeExceptionsFromExtensionService() {
        var extensionService = mock(ExtensionService.class);
        var executor = new RetryFailedExtensionJobExecutor(extensionService);
        var payload = Map.<String, Object>of();
        var failure = new IllegalStateException("boom");
        doThrow(failure).when(extensionService).retryFailedAsyncScript("/path", "extensionName", payload);

        var thrown = assertThrows(
                IllegalStateException.class,
                () -> executor.process(schedule(Map.of(
                        ScriptingExecutionService.EXTENSION_NAME, "extensionName",
                        ScriptingExecutionService.EXTENSION_PATH, "/path",
                        ScriptingExecutionService.EXTENSION_PARAMS, payload))));

        assertEquals(failure, thrown);
        verify(extensionService).retryFailedAsyncScript("/path", "extensionName", payload);
    }

    private static AdminJobSchedule schedule(Map<String, Object> metadata) {
        return new AdminJobSchedule(
                1L,
                AdminJobExecutor.JobName.EXECUTE_EXTENSION.name(),
                null,
                AdminJobSchedule.Status.SCHEDULED,
                null,
                metadata,
                0);
    }
}
