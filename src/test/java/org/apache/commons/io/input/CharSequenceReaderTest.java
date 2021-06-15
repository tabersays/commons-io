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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Arrays;
import org.apache.commons.io.TestResources;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CharSequenceReader}.
 *
 */
public class CharSequenceReaderTest {
    private static final char NONE = (new char[1])[0];

    @Test
    public void testClose() throws IOException {
        final Reader reader = new CharSequenceReader("FooBar");
        checkRead(reader, "Foo");
        reader.close();
        checkRead(reader, "Foo");

        final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7);
        checkRead(subReader, "Foo");
        subReader.close();
        checkRead(subReader, "Foo");
    }

    @Test
    public void testReady() throws IOException {
        final Reader reader = new CharSequenceReader("FooBar");
        assertThat(reader.ready()).isTrue();
        reader.skip(3);
        assertThat(reader.ready()).isTrue();
        checkRead(reader, "Bar");
        assertThat(reader.ready()).isFalse();
        reader.reset();
        assertThat(reader.ready()).isTrue();
        reader.skip(2);
        assertThat(reader.ready()).isTrue();
        reader.skip(10);
        assertThat(reader.ready()).isFalse();
        reader.close();
        assertThat(reader.ready()).isTrue();
        reader.skip(20);
        assertThat(reader.ready()).isFalse();

        final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7);
        assertThat(subReader.ready()).isTrue();
        subReader.skip(3);
        assertThat(subReader.ready()).isTrue();
        checkRead(subReader, "Bar");
        assertThat(subReader.ready()).isFalse();
        subReader.reset();
        assertThat(subReader.ready()).isTrue();
        subReader.skip(2);
        assertThat(subReader.ready()).isTrue();
        subReader.skip(10);
        assertThat(subReader.ready()).isFalse();
        subReader.close();
        assertThat(subReader.ready()).isTrue();
        subReader.skip(20);
        assertThat(subReader.ready()).isFalse();
    }

    @Test
    public void testMarkSupported() throws Exception {
        try (final Reader reader = new CharSequenceReader("FooBar")) {
            assertThat(reader.markSupported()).isTrue();
        }
    }

    @Test
    public void testMark() throws IOException {
        try (final Reader reader = new CharSequenceReader("FooBar")) {
            checkRead(reader, "Foo");
            reader.mark(0);
            checkRead(reader, "Bar");
            reader.reset();
            checkRead(reader, "Bar");
            reader.close();
            checkRead(reader, "Foo");
            reader.reset();
            checkRead(reader, "Foo");
        }
        try (final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7)) {
            checkRead(subReader, "Foo");
            subReader.mark(0);
            checkRead(subReader, "Bar");
            subReader.reset();
            checkRead(subReader, "Bar");
            subReader.close();
            checkRead(subReader, "Foo");
            subReader.reset();
            checkRead(subReader, "Foo");
        }
    }

    @Test
    public void testSkip() throws IOException {
        final Reader reader = new CharSequenceReader("FooBar");
        assertThat(reader.skip(3)).isEqualTo(3);
        checkRead(reader, "Bar");
        assertThat(reader.skip(3)).isEqualTo(0);
        reader.reset();
        assertThat(reader.skip(2)).isEqualTo(2);
        assertThat(reader.skip(10)).isEqualTo(4);
        assertThat(reader.skip(1)).isEqualTo(0);
        reader.close();
        assertThat(reader.skip(20)).isEqualTo(6);
        assertThat(reader.read()).isEqualTo(-1);

        final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7);
        assertThat(subReader.skip(3)).isEqualTo(3);
        checkRead(subReader, "Bar");
        assertThat(subReader.skip(3)).isEqualTo(0);
        subReader.reset();
        assertThat(subReader.skip(2)).isEqualTo(2);
        assertThat(subReader.skip(10)).isEqualTo(4);
        assertThat(subReader.skip(1)).isEqualTo(0);
        subReader.close();
        assertThat(subReader.skip(20)).isEqualTo(6);
        assertThat(subReader.read()).isEqualTo(-1);
    }

    @Test
    public void testRead() throws IOException {
        final String value = "Foo";
        testRead(value);
        testRead(new StringBuilder(value));
        testRead(new StringBuffer(value));
        testRead(CharBuffer.wrap(value));
    }

    private void testRead(final CharSequence charSequence) throws IOException {
        try (final Reader reader = new CharSequenceReader(charSequence)) {
            assertThat(reader.read()).isEqualTo('F');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo(-1);
            assertThat(reader.read()).isEqualTo(-1);
        }
        try (final Reader reader = new CharSequenceReader(charSequence, 1, 5)) {
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo(-1);
            assertThat(reader.read()).isEqualTo(-1);
        }
    }

    @Test
    public void testReadCharArray() throws IOException {
        final String value = "FooBar";
        testReadCharArray(value);
        testReadCharArray(new StringBuilder(value));
        testReadCharArray(new StringBuffer(value));
        testReadCharArray(CharBuffer.wrap(value));
    }

    private void testReadCharArray(final CharSequence charSequence) throws IOException {
        try (final Reader reader = new CharSequenceReader(charSequence)) {
            char[] chars = new char[2];
            assertThat(reader.read(chars)).isEqualTo(2);
            checkArray(new char[] { 'F', 'o' }, chars);
            chars = new char[3];
            assertThat(reader.read(chars)).isEqualTo(3);
            checkArray(new char[] { 'o', 'B', 'a' }, chars);
            chars = new char[3];
            assertThat(reader.read(chars)).isEqualTo(1);
            checkArray(new char[] { 'r', NONE, NONE }, chars);
            assertThat(reader.read(chars)).isEqualTo(-1);
        }
        try (final Reader reader = new CharSequenceReader(charSequence, 1, 5)) {
            char[] chars = new char[2];
            assertThat(reader.read(chars)).isEqualTo(2);
            checkArray(new char[] { 'o', 'o' }, chars);
            chars = new char[3];
            assertThat(reader.read(chars)).isEqualTo(2);
            checkArray(new char[] { 'B', 'a', NONE }, chars);
            chars = new char[3];
            assertThat(reader.read(chars)).isEqualTo(-1);
            checkArray(new char[] { NONE, NONE, NONE }, chars);
            assertThat(reader.read(chars)).isEqualTo(-1);
        }
    }

    @Test
    public void testReadCharArrayPortion() throws IOException {
        final String value = "FooBar";
        testReadCharArrayPortion(value);
        testReadCharArrayPortion(new StringBuilder(value));
        testReadCharArrayPortion(new StringBuffer(value));
        testReadCharArrayPortion(CharBuffer.wrap(value));
    }

    private void testReadCharArrayPortion(final CharSequence charSequence) throws IOException {
        final char[] chars = new char[10];
        try (final Reader reader = new CharSequenceReader(charSequence)) {
            assertThat(reader.read(chars, 3, 3)).isEqualTo(3);
            checkArray(new char[] { NONE, NONE, NONE, 'F', 'o', 'o' }, chars);
            assertThat(reader.read(chars, 0, 3)).isEqualTo(3);
            checkArray(new char[] { 'B', 'a', 'r', 'F', 'o', 'o', NONE }, chars);
            assertThat(reader.read(chars)).isEqualTo(-1);
        }
        Arrays.fill(chars, NONE);
        try (final Reader reader = new CharSequenceReader(charSequence, 1, 5)) {
            assertThat(reader.read(chars, 3, 2)).isEqualTo(2);
            checkArray(new char[] { NONE, NONE, NONE, 'o', 'o', NONE }, chars);
            assertThat(reader.read(chars, 0, 3)).isEqualTo(2);
            checkArray(new char[] { 'B', 'a', NONE, 'o', 'o', NONE }, chars);
            assertThat(reader.read(chars)).isEqualTo(-1);
        }
    }

    private void checkRead(final Reader reader, final String expected) throws IOException {
        for (int i = 0; i < expected.length(); i++) {
            assertThat((char) reader.read()).as("Read[" + i + "] of '" + expected + "'").isEqualTo(expected.charAt(i));
        }
    }

    private void checkArray(final char[] expected, final char[] actual) {
        for (int i = 0; i < expected.length; i++) {
            assertThat(actual[i]).as("Compare[" + i + "]").isEqualTo(expected[i]);
        }
    }

    @Test
    public void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new CharSequenceReader("FooBar", -1, 6),
                "Expected exception not thrown for negative start.");
        assertThrows(IllegalArgumentException.class, () -> new CharSequenceReader("FooBar", 1, 0),
                "Expected exception not thrown for end before start.");
    }

    @Test
    @SuppressWarnings("resource") // don't really need to close CharSequenceReader here
    public void testToString() {
        assertThat(new CharSequenceReader("FooBar").toString()).isEqualTo("FooBar");
        assertThat(new CharSequenceReader("xFooBarx", 1, 7).toString()).isEqualTo("FooBar");
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        /*
         * File CharSequenceReader.bin contains a CharSequenceReader that was serialized before
         * the start and end fields were added. Its CharSequence is "FooBar".
         * This part of the test will test that adding the fields does not break any existing
         * serialized CharSequenceReaders.
         */
        try (ObjectInputStream ois = new ObjectInputStream(TestResources.getInputStream("CharSequenceReader.bin"))) {
            final CharSequenceReader reader = (CharSequenceReader) ois.readObject();
            assertThat(reader.read()).isEqualTo('F');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('B');
            assertThat(reader.read()).isEqualTo('a');
            assertThat(reader.read()).isEqualTo('r');
            assertThat(reader.read()).isEqualTo(-1);
            assertThat(reader.read()).isEqualTo(-1);
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            final CharSequenceReader reader = new CharSequenceReader("xFooBarx", 1, 7);
            oos.writeObject(reader);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            final CharSequenceReader reader = (CharSequenceReader) ois.readObject();
            assertThat(reader.read()).isEqualTo('F');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('B');
            assertThat(reader.read()).isEqualTo('a');
            assertThat(reader.read()).isEqualTo('r');
            assertThat(reader.read()).isEqualTo(-1);
            assertThat(reader.read()).isEqualTo(-1);
            reader.reset();
            assertThat(reader.read()).isEqualTo('F');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('o');
            assertThat(reader.read()).isEqualTo('B');
            assertThat(reader.read()).isEqualTo('a');
            assertThat(reader.read()).isEqualTo('r');
            assertThat(reader.read()).isEqualTo(-1);
            assertThat(reader.read()).isEqualTo(-1);
        }
    }
}
