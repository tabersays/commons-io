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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.test.ThrowOnCloseReader;
import org.apache.commons.io.test.ThrowOnCloseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link TeeReader}.
 */
public class TeeReaderTest  {

    private StringBuilderWriter output;

    private Reader tee;

    @BeforeEach
    public void setUp() {
        final Reader input = new CharSequenceReader("abc");
        output = new StringBuilderWriter();
        tee = new TeeReader(input, output);
    }

    /**
     * Tests that the main {@code Reader} is closed when closing the branch {@code Writer} throws an
     * exception on {@link TeeReader#close()}, if specified to do so.
     */
    @Test
    public void testCloseBranchIOException() throws Exception {
        final StringReader goodR = mock(StringReader.class);
        final Writer badW = new ThrowOnCloseWriter();

        final TeeReader nonClosingTr = new TeeReader(goodR, badW, false);
        nonClosingTr.close();
        verify(goodR).close();

        final TeeReader closingTr = new TeeReader(goodR, badW, true);
        try {
            closingTr.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodR, times(2)).close();
        }
    }

    /**
     * Tests that the branch {@code Writer} is closed when closing the main {@code Reader} throws an
     * exception on {@link TeeReader#close()}, if specified to do so.
     */
    @Test
    public void testCloseMainIOException() throws IOException {
        final Reader badR = new ThrowOnCloseReader();
        final StringWriter goodW = mock(StringWriter.class);

        final TeeReader nonClosingTr = new TeeReader(badR, goodW, false);
        try {
            nonClosingTr.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            verify(goodW, never()).close();
        }

        final TeeReader closingTr = new TeeReader(badR, goodW, true);
        try {
            closingTr.close();
            fail("Expected " + IOException.class.getName());
        } catch (final IOException e) {
            //Assert.assertTrue(goodW.closed);
            verify(goodW).close();
        }
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
        assertThat(output.toString()).isEqualTo("abbc");
    }

    @Test
    public void testReadEverything() throws Exception {
        assertThat(tee.read()).isEqualTo('a');
        assertThat(tee.read()).isEqualTo('b');
        assertThat(tee.read()).isEqualTo('c');
        assertThat(tee.read()).isEqualTo(-1);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    public void testReadNothing() {
        assertThat(output.toString()).isEqualTo("");
    }

    @Test
    public void testReadOneChar() throws Exception {
        assertThat(tee.read()).isEqualTo('a');
        assertThat(output.toString()).isEqualTo("a");
    }

    @Test
    public void testReadToArray() throws Exception {
        final char[] buffer = new char[8];
        assertThat(tee.read(buffer)).isEqualTo(3);
        assertThat(buffer[0]).isEqualTo('a');
        assertThat(buffer[1]).isEqualTo('b');
        assertThat(buffer[2]).isEqualTo('c');
        assertThat(tee.read(buffer)).isEqualTo(-1);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    public void testReadToArrayWithOffset() throws Exception {
        final char[] buffer = new char[8];
        assertThat(tee.read(buffer, 4, 4)).isEqualTo(3);
        assertThat(buffer[4]).isEqualTo('a');
        assertThat(buffer[5]).isEqualTo('b');
        assertThat(buffer[6]).isEqualTo('c');
        assertThat(tee.read(buffer, 4, 4)).isEqualTo(-1);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    public void testReadToCharBuffer() throws Exception {
        final CharBuffer buffer = CharBuffer.allocate(8);
        buffer.position(1);
        assertThat(tee.read(buffer)).isEqualTo(3);
        assertThat(buffer.position()).isEqualTo(4);
        buffer.flip();
        buffer.position(1);
        assertThat(buffer.charAt(0)).isEqualTo('a');
        assertThat(buffer.charAt(1)).isEqualTo('b');
        assertThat(buffer.charAt(2)).isEqualTo('c');
        assertThat(tee.read(buffer)).isEqualTo(-1);
        assertThat(output.toString()).isEqualTo("abc");
    }

    @Test
    public void testSkip() throws Exception {
        assertThat(tee.read()).isEqualTo('a');
        assertThat(tee.skip(1)).isEqualTo(1);
        assertThat(tee.read()).isEqualTo('c');
        assertThat(tee.read()).isEqualTo(-1);
        assertThat(output.toString()).isEqualTo("ac");
    }

}