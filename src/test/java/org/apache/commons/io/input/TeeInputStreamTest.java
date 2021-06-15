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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.test.ThrowOnCloseInputStream;
import org.apache.commons.io.test.ThrowOnCloseOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TeeInputStream}.
 */
public class TeeInputStreamTest  {

    private final String ASCII = "US-ASCII";

    private InputStream tee;

    private ByteArrayOutputStream output;

    @BeforeEach
    public void setUp() throws Exception {
        final InputStream input = new ByteArrayInputStream("abc".getBytes(ASCII));
        output = new ByteArrayOutputStream();
        tee = new TeeInputStream(input, output);
    }

    @Test
    public void testReadNothing() throws Exception {
        assertThat(new String(output.toString(ASCII))).isEqualTo("");
    }

    @Test
    public void testReadOneByte() throws Exception {
        assertThat(tee.read()).isEqualTo('a');
        assertThat(new String(output.toString(ASCII))).isEqualTo("a");
    }

    @Test
    public void testReadEverything() throws Exception {
        assertThat(tee.read()).isEqualTo('a');
        assertThat(tee.read()).isEqualTo('b');
        assertThat(tee.read()).isEqualTo('c');
        assertThat(tee.read()).isEqualTo(-1);
        assertThat(new String(output.toString(ASCII))).isEqualTo("abc");
    }

    @Test
    public void testReadToArray() throws Exception {
        final byte[] buffer = new byte[8];
        assertThat(tee.read(buffer)).isEqualTo(3);
        assertThat(buffer[0]).isEqualTo('a');
        assertThat(buffer[1]).isEqualTo('b');
        assertThat(buffer[2]).isEqualTo('c');
        assertThat(tee.read(buffer)).isEqualTo(-1);
        assertThat(new String(output.toString(ASCII))).isEqualTo("abc");
    }

    @Test
    public void testReadToArrayWithOffset() throws Exception {
        final byte[] buffer = new byte[8];
        assertThat(tee.read(buffer, 4, 4)).isEqualTo(3);
        assertThat(buffer[4]).isEqualTo('a');
        assertThat(buffer[5]).isEqualTo('b');
        assertThat(buffer[6]).isEqualTo('c');
        assertThat(tee.read(buffer, 4, 4)).isEqualTo(-1);
        assertThat(new String(output.toString(ASCII))).isEqualTo("abc");
    }

    @Test
    public void testSkip() throws Exception {
        assertThat(tee.read()).isEqualTo('a');
        assertThat(tee.skip(1)).isEqualTo(1);
        assertThat(tee.read()).isEqualTo('c');
        assertThat(tee.read()).isEqualTo(-1);
        assertThat(new String(output.toString(ASCII))).isEqualTo("ac");
    }

    @Test
    public void testMarkReset() throws Exception {
        assertThat(tee.read()).isEqualTo('a');
        tee.mark(1);
        assertThat(tee.read()).isEqualTo('b');
        tee.reset();
        assertThat(tee.read()).isEqualTo('b');
        assertThat(tee.read()).isEqualTo('c');
        assertThat(tee.read()).isEqualTo(-1);
        assertThat(new String(output.toString(ASCII))).isEqualTo("abbc");
    }

    /**
     * Tests that the main {@code InputStream} is closed when closing the branch {@code OutputStream} throws an
     * exception on {@link TeeInputStream#close()}, if specified to do so.
     */
    @Test
    public void testCloseBranchIOException() throws Exception {
        final ByteArrayInputStream goodIs = mock(ByteArrayInputStream.class);
        final OutputStream badOs = new ThrowOnCloseOutputStream();

        final TeeInputStream nonClosingTis = new TeeInputStream(goodIs, badOs, false);
        nonClosingTis.close();
        verify(goodIs).close();

        final TeeInputStream closingTis = new TeeInputStream(goodIs, badOs, true);
        try {
            closingTis.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodIs, times(2)).close();
        }
    }

    /**
     * Tests that the branch {@code OutputStream} is closed when closing the main {@code InputStream} throws an
     * exception on {@link TeeInputStream#close()}, if specified to do so.
     */
    @Test
    public void testCloseMainIOException() throws IOException {
        final InputStream badIs = new ThrowOnCloseInputStream();
        final ByteArrayOutputStream goodOs = mock(ByteArrayOutputStream.class);

        final TeeInputStream nonClosingTis = new TeeInputStream(badIs, goodOs, false);
        try {
            nonClosingTis.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodOs, never()).close();
        }

        final TeeInputStream closingTis = new TeeInputStream(badIs, goodOs, true);
        try {
            closingTis.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodOs).close();
        }
    }

}
