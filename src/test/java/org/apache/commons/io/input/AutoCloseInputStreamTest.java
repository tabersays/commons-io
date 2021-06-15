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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link AutoCloseInputStream}.
 */
public class AutoCloseInputStreamTest {

    private byte[] data;

    private InputStream stream;

    private boolean closed;

    @BeforeEach
    public void setUp() {
        data = new byte[] {'x', 'y', 'z'};
        stream = new AutoCloseInputStream(new ByteArrayInputStream(data) {
            @Override
            public void close() {
                closed = true;
            }
        });
        closed = false;
    }

    @Test
    public void testClose() throws IOException {
        stream.close();
        assertThat(closed).as("closed").isTrue();
        assertThat(stream.read()).as("read()").isEqualTo(-1);
    }

    @Test
    public void testRead() throws IOException {
        for (final byte element : data) {
            assertThat(stream.read()).as("read()").isEqualTo(element);
            assertThat(closed).as("closed").isFalse();
        }
        assertThat(stream.read()).as("read()").isEqualTo(-1);
        assertThat(closed).as("closed").isTrue();
    }

    @Test
    public void testReadBuffer() throws IOException {
        final byte[] b = new byte[data.length * 2];
        int total = 0;
        for (int n = 0; n != -1; n = stream.read(b)) {
            assertThat(closed).as("closed").isFalse();
            for (int i = 0; i < n; i++) {
                assertThat(b[i]).as("read(b)").isEqualTo(data[total + i]);
            }
            total += n;
        }
        assertThat(total).as("read(b)").isEqualTo(data.length);
        assertThat(closed).as("closed").isTrue();
        assertThat(stream.read(b)).as("read(b)").isEqualTo(-1);
    }

    @Test
    public void testReadBufferOffsetLength() throws IOException {
        final byte[] b = new byte[data.length * 2];
        int total = 0;
        for (int n = 0; n != -1; n = stream.read(b, total, b.length - total)) {
            assertThat(closed).as("closed").isFalse();
            total += n;
        }
        assertThat(total).as("read(b, off, len)").isEqualTo(data.length);
        for (int i = 0; i < data.length; i++) {
            assertThat(b[i]).as("read(b, off, len)").isEqualTo(data[i]);
        }
        assertThat(closed).as("closed").isTrue();
        assertThat(stream.read(b, 0, b.length)).as("read(b, off, len)").isEqualTo(-1);
    }

    @Test
    public void testResetBeforeEnd() throws IOException {
        final String inputStr = "1234";
        final AutoCloseInputStream inputStream = new AutoCloseInputStream(new ByteArrayInputStream(inputStr.getBytes()));
        inputStream.mark(1);
        assertThat(inputStream.read()).isEqualTo('1');
        inputStream.reset();
        assertThat(inputStream.read()).isEqualTo('1');
        assertThat(inputStream.read()).isEqualTo('2');
        inputStream.reset();
        assertThat(inputStream.read()).isEqualTo('1');
        assertThat(inputStream.read()).isEqualTo('2');
        assertThat(inputStream.read()).isEqualTo('3');
        inputStream.reset();
        assertThat(inputStream.read()).isEqualTo('1');
        assertThat(inputStream.read()).isEqualTo('2');
        assertThat(inputStream.read()).isEqualTo('3');
        assertThat(inputStream.read()).isEqualTo('4');
        inputStream.reset();
        assertThat(inputStream.read()).isEqualTo('1');
    }

}
