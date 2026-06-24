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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HttpUtilsTest {

    @Test
    void statusCodeIsSuccessfulOnlyFor2xxResponses() {
        assertFalse(HttpUtils.statusCodeIsSuccessful(199));
        assertTrue(HttpUtils.statusCodeIsSuccessful(200));
        assertTrue(HttpUtils.statusCodeIsSuccessful(299));
        assertFalse(HttpUtils.statusCodeIsSuccessful(300));
    }

    @Test
    void callSuccessfulUsesResponseStatusCode() {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(204);

        assertTrue(HttpUtils.callSuccessful(response));
    }

    @Test
    void basicAuthEncodesUsernameAndPasswordAsUtf8() {
        assertEquals("Basic dXNlcjpwQHNz", HttpUtils.basicAuth("user", "p@ss"));
    }

    @Test
    void formUrlEncodedBodyEscapesKeysAndValues() throws Exception {
        var body = readBody(HttpUtils.ofFormUrlEncodedBody(Map.of(
                "a b", "c+d",
                "symbol", "€")));

        assertTrue(body.contains("a+b=c%2Bd"));
        assertTrue(body.contains("symbol=%E2%82%AC"));
        assertEquals(1, body.chars().filter(c -> c == '&').count());
    }

    @Test
    void multipartBodyPublisherRequiresAtLeastOnePart() {
        var publisher = new HttpUtils.MultiPartBodyPublisher();

        assertThrows(IllegalStateException.class, publisher::build);
    }

    @Test
    void multipartBodyPublisherBuildsStringAndStreamPartsWithFinalBoundary() throws Exception {
        var publisher = new HttpUtils.MultiPartBodyPublisher()
                .addPart("field", "value")
                .addPart(
                        "file",
                        () -> new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)),
                        "file.txt",
                        null);
        var boundary = publisher.getBoundary();

        var body = readBody(publisher.build());

        assertTrue(body.contains("--" + boundary));
        assertTrue(body.contains("Content-Disposition: form-data; name=field"));
        assertTrue(body.contains("value"));
        assertTrue(body.contains("Content-Disposition: form-data; name=file; filename=file.txt"));
        assertTrue(body.contains("Content-Type: application/octet-stream"));
        assertTrue(body.contains("content"));
        assertTrue(body.endsWith("--" + boundary + "--"));
    }

    private static String readBody(HttpRequest.BodyPublisher publisher) throws Exception {
        var subscriber = new BodySubscriber();
        publisher.subscribe(subscriber);
        return subscriber.awaitBody();
    }

    private static class BodySubscriber implements Flow.Subscriber<ByteBuffer> {
        private final CountDownLatch done = new CountDownLatch(1);
        private final List<byte[]> chunks = new ArrayList<>();
        private Throwable failure;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            var bytes = new byte[item.remaining()];
            item.get(bytes);
            chunks.add(bytes);
        }

        @Override
        public void onError(Throwable throwable) {
            failure = throwable;
            done.countDown();
        }

        @Override
        public void onComplete() {
            done.countDown();
        }

        String awaitBody() throws Exception {
            assertTrue(done.await(5, TimeUnit.SECONDS));
            if (failure != null) {
                throw new AssertionError(failure);
            }
            var size = chunks.stream().mapToInt(c -> c.length).sum();
            var all = new byte[size];
            var offset = 0;
            for (var chunk : chunks) {
                System.arraycopy(chunk, 0, all, offset, chunk.length);
                offset += chunk.length;
            }
            return new String(all, StandardCharsets.UTF_8);
        }
    }
}
