/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io.input;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.apache.commons.io.TaggedIOException;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TaggedInputStream}.
 */
public class TaggedInputStreamTest  {

    @Test
    public void testEmptyStream() throws IOException {
        final InputStream stream = new TaggedInputStream(ClosedInputStream.CLOSED_INPUT_STREAM);
        assertThat(stream.available()).isEqualTo(0);
        assertThat(stream.read()).isEqualTo(-1);
        assertThat(stream.read(new byte[1])).isEqualTo(-1);
        assertThat(stream.read(new byte[1], 0, 1)).isEqualTo(-1);
        stream.close();
    }

    @Test
    public void testNormalStream() throws IOException {
        final InputStream stream = new TaggedInputStream(
                new ByteArrayInputStream(new byte[] { 'a', 'b', 'c' }));
        assertThat(stream.available()).isEqualTo(3);
        assertThat(stream.read()).isEqualTo('a');
        final byte[] buffer = new byte[1];
        assertThat(stream.read(buffer)).isEqualTo(1);
        assertThat(buffer[0]).isEqualTo('b');
        assertThat(stream.read(buffer, 0, 1)).isEqualTo(1);
        assertThat(buffer[0]).isEqualTo('c');
        assertThat(stream.read()).isEqualTo(-1);
        stream.close();
    }

    @Test
    public void testBrokenStream() {
        final IOException exception = new IOException("test exception");
        final TaggedInputStream stream =
            new TaggedInputStream(new BrokenInputStream(exception));

        // Test the available() method
        try {
            stream.available();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertThat(stream.isCauseOf(e)).isTrue();
            try {
                stream.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertThat(e2).isEqualTo(exception);
            }
        }

        // Test the read() method
        try {
            stream.read();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertThat(stream.isCauseOf(e)).isTrue();
            try {
                stream.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertThat(e2).isEqualTo(exception);
            }
        }

        // Test the close() method
        try {
            stream.close();
            fail("Expected exception not thrown.");
        } catch (final IOException e) {
            assertThat(stream.isCauseOf(e)).isTrue();
            try {
                stream.throwIfCauseOf(e);
                fail("Expected exception not thrown.");
            } catch (final IOException e2) {
                assertThat(e2).isEqualTo(exception);
            }
        }
    }

    @Test
    public void testOtherException() throws Exception {
        final IOException exception = new IOException("test exception");
        final TaggedInputStream stream = new TaggedInputStream(ClosedInputStream.CLOSED_INPUT_STREAM);

        assertThat(stream.isCauseOf(exception)).isFalse();
        assertThat(stream.isCauseOf(
                new TaggedIOException(exception, UUID.randomUUID()))).isFalse();

        stream.throwIfCauseOf(exception);

        stream.throwIfCauseOf(
                    new TaggedIOException(exception, UUID.randomUUID()));
        stream.close();
    }

}
