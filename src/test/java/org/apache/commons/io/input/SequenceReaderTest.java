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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SequenceReader}.
 */
public class SequenceReaderTest {

    private static final char NUL = 0;

    private void checkArray(final char[] expected, final char[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertThat(actual[i]).as("Compare[" + i + "]").isEqualTo(expected[i]);
        }
    }

    private void checkRead(final Reader reader, final String expected) throws IOException {
        for (int i = 0; i < expected.length(); i++) {
            assertThat((char) reader.read()).as("Read[" + i + "] of '" + expected + "'").isEqualTo(expected.charAt(i));
        }
    }

    private void checkReadEof(final Reader reader) throws IOException {
        for (int i = 0; i < 10; i++) {
            assertThat(reader.read()).isEqualTo(-1);
        }
    }

    @Test
    public void testClose() throws IOException {
        try (final Reader reader = new SequenceReader(new CharSequenceReader("FooBar"))) {
            checkRead(reader, "Foo");
            reader.close();
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadClosedReader() throws IOException {
        @SuppressWarnings("resource")
        final Reader reader = new SequenceReader(new CharSequenceReader("FooBar"));
        reader.close();
        checkReadEof(reader);
    }

    @Test
    public void testMarkSupported() throws Exception {
        try (final Reader reader = new SequenceReader()) {
            assertThat(reader.markSupported()).isFalse();
        }
    }

    @Test
    public void testRead() throws IOException {
        try (final Reader reader = new SequenceReader(new StringReader("Foo"), new StringReader("Bar"))) {
            assertThat(reader.read()).isEqualTo('F');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('B');
            assertThat(reader.read()).isEqualTo('a');
            assertThat(reader.read()).isEqualTo('r');
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadCharArray() throws IOException {
        try (final Reader reader = new SequenceReader(new StringReader("Foo"), new StringReader("Bar"))) {
            char[] chars = new char[2];
            assertThat(reader.read(chars)).isEqualTo(2);
            checkArray(new char[] { 'F', 'o' }, chars);
            chars = new char[3];
            assertThat(reader.read(chars)).isEqualTo(3);
            checkArray(new char[] { 'o', 'B', 'a' }, chars);
            chars = new char[3];
            assertThat(reader.read(chars)).isEqualTo(1);
            checkArray(new char[] { 'r', NUL, NUL }, chars);
            assertThat(reader.read(chars)).isEqualTo(-1);
        }
    }

    @Test
    public void testReadCharArrayPortion() throws IOException {
        final char[] chars = new char[10];
        try (final Reader reader = new SequenceReader(new StringReader("Foo"), new StringReader("Bar"))) {
            assertThat(reader.read(chars, 3, 3)).isEqualTo(3);
            checkArray(new char[] { NUL, NUL, NUL, 'F', 'o', 'o' }, chars);
            assertThat(reader.read(chars, 0, 3)).isEqualTo(3);
            checkArray(new char[] { 'B', 'a', 'r', 'F', 'o', 'o', NUL }, chars);
            assertThat(reader.read(chars)).isEqualTo(-1);
            assertThrows(IndexOutOfBoundsException.class, () -> reader.read(chars, 10, 10));
            assertThrows(NullPointerException.class, () -> reader.read(null, 0, 10));
        }
    }

    @Test
    public void testReadCollection() throws IOException {
        final Collection<Reader> readers = new ArrayList<>();
        readers.add(new StringReader("F"));
        readers.add(new StringReader("B"));
        try (final Reader reader = new SequenceReader(readers)) {
            assertThat(reader.read()).isEqualTo('F');
            assertThat(reader.read()).isEqualTo('B');
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadIterable() throws IOException {
        final Collection<Reader> readers = new ArrayList<>();
        readers.add(new StringReader("F"));
        readers.add(new StringReader("B"));
        final Iterable<Reader> iterable = readers;
        try (final Reader reader = new SequenceReader(iterable)) {
            assertThat(reader.read()).isEqualTo('F');
            assertThat(reader.read()).isEqualTo('B');
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadLength0Readers() throws IOException {
        try (final Reader reader = new SequenceReader(new StringReader(StringUtils.EMPTY),
            new StringReader(StringUtils.EMPTY), new StringReader(StringUtils.EMPTY))) {
            checkReadEof(reader);
        }
    }

    @Test
    public void testReadLength1Readers() throws IOException {
        try (final Reader reader = new SequenceReader(
        // @formatter:off
            new StringReader("0"),
            new StringReader("1"),
            new StringReader("2"),
            new StringReader("3"))) {
            // @formatter:on
            assertThat(reader.read()).isEqualTo('0');
            assertThat(reader.read()).isEqualTo('1');
            assertThat(reader.read()).isEqualTo('2');
            assertThat(reader.read()).isEqualTo('3');
        }
    }

    @Test
    public void testReadList() throws IOException {
        final List<Reader> readers = new ArrayList<>();
        readers.add(new StringReader("F"));
        readers.add(new StringReader("B"));
        try (final Reader reader = new SequenceReader(readers)) {
            assertThat(reader.read()).isEqualTo('F');
            assertThat(reader.read()).isEqualTo('B');
            checkReadEof(reader);
        }
    }

    @Test
    public void testSkip() throws IOException {
        try (final Reader reader = new SequenceReader(new StringReader("Foo"), new StringReader("Bar"))) {
            assertThat(reader.skip(3)).isEqualTo(3);
            checkRead(reader, "Bar");
            assertThat(reader.skip(3)).isEqualTo(0);
        }
    }
}