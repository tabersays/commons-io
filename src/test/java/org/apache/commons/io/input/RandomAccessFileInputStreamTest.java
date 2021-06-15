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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class RandomAccessFileInputStreamTest {

    private static final String DATA_FILE = "src/test/resources/org/apache/commons/io/test-file-iso8859-1.bin";
    private static final int DATA_FILE_LEN = 1430;

    private RandomAccessFile createRandomAccessFile() throws FileNotFoundException {
        return new RandomAccessFile(DATA_FILE, "r");
    }

    @Test
    public void testAvailable() throws IOException {
        try (final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(createRandomAccessFile(),
            true)) {
            assertThat(inputStream.available()).isEqualTo(DATA_FILE_LEN);
        }
    }

    @Test
    public void testAvailableLong() throws IOException {
        try (final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(createRandomAccessFile(),
            true)) {
            assertThat(inputStream.availableLong()).isEqualTo(DATA_FILE_LEN);
        }
    }

    @Test
    public void testCtorCloseOnCloseFalse() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(file, false)) {
                assertThat(inputStream.isCloseOnClose()).isFalse();
            }
            file.read();
        }
    }

    @Test
    public void testCtorCloseOnCloseTrue() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(file, true)) {
                assertThat(inputStream.isCloseOnClose()).isTrue();
            }
            assertThrows(IOException.class, () -> file.read());
        }
    }

    @Test
    public void testCtorNullFile() {
        assertThrows(NullPointerException.class, () -> new RandomAccessFileInputStream(null));
    }

    @Test
    public void testGetters() throws IOException {
        try (RandomAccessFile file = createRandomAccessFile()) {
            try (final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(file, true)) {
                assertThat(inputStream.getRandomAccessFile()).isEqualTo(file);
                assertThat(inputStream.isCloseOnClose()).isTrue();
            }
        }
    }

    @Test
    public void testRead() throws IOException {
        try (final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(createRandomAccessFile(),
            true)) {
            // A Test Line.
            assertThat(inputStream.read()).isEqualTo('A');
            assertThat(inputStream.read()).isEqualTo(' ');
            assertThat(inputStream.read()).isEqualTo('T');
            assertThat(inputStream.read()).isEqualTo('e');
            assertThat(inputStream.read()).isEqualTo('s');
            assertThat(inputStream.read()).isEqualTo('t');
            assertThat(inputStream.read()).isEqualTo(' ');
            assertThat(inputStream.read()).isEqualTo('L');
            assertThat(inputStream.read()).isEqualTo('i');
            assertThat(inputStream.read()).isEqualTo('n');
            assertThat(inputStream.read()).isEqualTo('e');
            assertThat(inputStream.read()).isEqualTo('.');
            assertThat(inputStream.available()).isEqualTo(DATA_FILE_LEN - 12);
            assertThat(inputStream.availableLong()).isEqualTo(DATA_FILE_LEN - 12);
        }
    }

    @Test
    public void testSkip() throws IOException {

        try (final RandomAccessFile file = createRandomAccessFile();
            final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(file, false)) {
            assertThat(inputStream.skip(-1)).isEqualTo(0);
            assertThat(inputStream.skip(Integer.MIN_VALUE)).isEqualTo(0);
            assertThat(inputStream.skip(0)).isEqualTo(0);
            // A Test Line.
            assertThat(inputStream.read()).isEqualTo('A');
            assertThat(inputStream.skip(1)).isEqualTo(1);
            assertThat(inputStream.read()).isEqualTo('T');
            assertThat(inputStream.skip(1)).isEqualTo(1);
            assertThat(inputStream.read()).isEqualTo('s');
            assertThat(inputStream.skip(1)).isEqualTo(1);
            assertThat(inputStream.read()).isEqualTo(' ');
            assertThat(inputStream.skip(1)).isEqualTo(1);
            assertThat(inputStream.read()).isEqualTo('i');
            assertThat(inputStream.skip(1)).isEqualTo(1);
            assertThat(inputStream.read()).isEqualTo('e');
            assertThat(inputStream.skip(1)).isEqualTo(1);
            //
            assertThat(inputStream.available()).isEqualTo(DATA_FILE_LEN - 12);
            assertThat(inputStream.availableLong()).isEqualTo(DATA_FILE_LEN - 12);
            assertThat(inputStream.skip(10)).isEqualTo(10);
            assertThat(inputStream.availableLong()).isEqualTo(DATA_FILE_LEN - 22);
            //
            final long avail = inputStream.availableLong();
            assertThat(inputStream.skip(inputStream.availableLong())).isEqualTo(avail);
            // At EOF
            assertThat(file.length()).isEqualTo(DATA_FILE_LEN);
            assertThat(file.getFilePointer()).isEqualTo(DATA_FILE_LEN);
            //
            assertThat(inputStream.skip(1)).isEqualTo(0);
            assertThat(inputStream.skip(1000000000000L)).isEqualTo(0);
        }
    }

    @Test
    public void testReadByteArray() throws IOException {
        try (final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(createRandomAccessFile(),
            true)) {
            // A Test Line.
            final int dataLen = 12;
            final byte[] buffer = new byte[dataLen];
            assertThat(inputStream.read(buffer)).isEqualTo(dataLen);
            assertThat(buffer).containsExactly("A Test Line.".getBytes(StandardCharsets.ISO_8859_1));
            //
            assertThat(inputStream.available()).isEqualTo(DATA_FILE_LEN - dataLen);
            assertThat(inputStream.availableLong()).isEqualTo(DATA_FILE_LEN - dataLen);
        }
    }

    @Test
    public void testReadByteArrayBounds() throws IOException {
        try (final RandomAccessFileInputStream inputStream = new RandomAccessFileInputStream(createRandomAccessFile(),
            true)) {
            // A Test Line.
            final int dataLen = 12;
            final byte[] buffer = new byte[dataLen];
            assertThat(inputStream.read(buffer, 0, dataLen)).isEqualTo(dataLen);
            assertThat(buffer).containsExactly("A Test Line.".getBytes(StandardCharsets.ISO_8859_1));
            //
            assertThat(inputStream.available()).isEqualTo(DATA_FILE_LEN - dataLen);
            assertThat(inputStream.availableLong()).isEqualTo(DATA_FILE_LEN - dataLen);
        }
    }
}
