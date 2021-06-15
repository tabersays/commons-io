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

import java.io.IOException;
import java.io.StringReader;
import java.util.function.IntPredicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link CharacterFilterReader} with an {@link IntPredicate}.
 */
public class CharacterFilterReaderIntPredicateTest {

    @Test
    public void testInputSize0FilterAll() throws IOException {
        final StringReader input = new StringReader(StringUtils.EMPTY);
        try (CharacterFilterReader reader = new CharacterFilterReader(input, ch -> true)) {
            assertThat(reader.read()).isEqualTo(-1);
        }
    }

    @Test
    public void testInputSize1FilterAll() throws IOException {
        try (StringReader input = new StringReader("a");
                CharacterFilterReader reader = new CharacterFilterReader(input, ch -> true)) {
            assertThat(reader.read()).isEqualTo(-1);
        }
    }

    @Test
    public void testInputSize2FilterAll() throws IOException {
        final StringReader input = new StringReader("aa");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, ch -> true)) {
            assertThat(reader.read()).isEqualTo(-1);
        }
    }

    @Test
    public void testInputSize2FilterFirst() throws IOException {
        final StringReader input = new StringReader("ab");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, ch -> ch == 'a')) {
            assertThat(reader.read()).isEqualTo('b');
            assertThat(reader.read()).isEqualTo(-1);
        }
    }

    @Test
    public void testInputSize2FilterLast() throws IOException {
        final StringReader input = new StringReader("ab");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, ch -> ch == 'b')) {
            assertThat(reader.read()).isEqualTo('a');
            assertThat(reader.read()).isEqualTo(-1);
        }
    }

    @Test
    public void testInputSize5FilterWhitespace() throws IOException {
        final StringReader input = new StringReader(" a b ");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, Character::isWhitespace)) {
            assertThat(reader.read()).isEqualTo('a');
            assertThat(reader.read()).isEqualTo('b');
            assertThat(reader.read()).isEqualTo(-1);
        }
    }

    @Test
    public void testReadIntoBuffer() throws IOException {
        final StringReader input = new StringReader("ababcabcd");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, ch -> ch == 'b')) {
            final char[] buff = new char[9];
            final int charCount = reader.read(buff);
            assertThat(charCount).isEqualTo(6);
            assertThat(new String(buff, 0, charCount)).isEqualTo("aacacd");
        }
    }

    @Test
    public void testReadIntoBufferFilterWhitespace() throws IOException {
        final StringReader input = new StringReader(" a b a b c a b c d ");
        try (CharacterFilterReader reader = new CharacterFilterReader(input, Character::isWhitespace)) {
            final char[] buff = new char[19];
            final int charCount = reader.read(buff);
            assertThat(charCount).isEqualTo(9);
            assertThat(new String(buff, 0, charCount)).isEqualTo("ababcabcd");
        }
    }

    @Test
    public void testReadUsingReader() throws IOException {
        final StringReader input = new StringReader("ababcabcd");
        try (StringBuilderWriter output = new StringBuilderWriter();
                CharacterFilterReader reader = new CharacterFilterReader(input, ch -> ch == 'b')) {
            IOUtils.copy(reader, output);
            assertThat(output.toString()).isEqualTo("aacacd");
        }
    }

    @Test
    public void testReadUsingReaderFilterWhitespace() throws IOException {
        final StringReader input = new StringReader(" a b a b c a b c d ");
        try (StringBuilderWriter output = new StringBuilderWriter();
                CharacterFilterReader reader = new CharacterFilterReader(input, Character::isWhitespace)) {
            IOUtils.copy(reader, output);
            assertThat(output.toString()).isEqualTo("ababcabcd");
        }
    }

}
