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

import static org.apache.commons.io.input.UnsynchronizedByteArrayInputStream.END_OF_STREAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Basic unit tests for the alternative ByteArrayInputStream implementation.
 */
public class UnsynchronizedByteArrayInputStreamTest {

    @Test
    public void testConstructor1() throws IOException {
        final byte[] empty = IOUtils.EMPTY_BYTE_ARRAY;
        final byte[] one = new byte[1];
        final byte[] some = new byte[25];

        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(empty);
        assertThat(is.available()).isEqualTo(empty.length);

        is.close();
        is = new UnsynchronizedByteArrayInputStream(one);
        assertThat(is.available()).isEqualTo(one.length);

        is.close();
        is = new UnsynchronizedByteArrayInputStream(some);
        assertThat(is.available()).isEqualTo(some.length);
        is.close();
    }

    @Test
    @SuppressWarnings("resource") // not necessary to close these resources
    public void testConstructor2() {
        final byte[] empty = IOUtils.EMPTY_BYTE_ARRAY;
        final byte[] one = new byte[1];
        final byte[] some = new byte[25];

        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(empty, 0);
        assertThat(is.available()).isEqualTo(empty.length);
        is = new UnsynchronizedByteArrayInputStream(empty, 1);
        assertThat(is.available()).isEqualTo(0);

        is = new UnsynchronizedByteArrayInputStream(one, 0);
        assertThat(is.available()).isEqualTo(one.length);
        is = new UnsynchronizedByteArrayInputStream(one, 1);
        assertThat(is.available()).isEqualTo(0);
        is = new UnsynchronizedByteArrayInputStream(one, 2);
        assertThat(is.available()).isEqualTo(0);

        is = new UnsynchronizedByteArrayInputStream(some, 0);
        assertThat(is.available()).isEqualTo(some.length);
        is = new UnsynchronizedByteArrayInputStream(some, 1);
        assertThat(is.available()).isEqualTo(some.length - 1);
        is = new UnsynchronizedByteArrayInputStream(some, 10);
        assertThat(is.available()).isEqualTo(some.length - 10);
        is = new UnsynchronizedByteArrayInputStream(some, some.length);
        assertThat(is.available()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("resource") // not necessary to close these resources
    public void testConstructor3() {
        final byte[] empty = IOUtils.EMPTY_BYTE_ARRAY;
        final byte[] one = new byte[1];
        final byte[] some = new byte[25];

        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(empty, 0);
        assertThat(is.available()).isEqualTo(empty.length);
        is = new UnsynchronizedByteArrayInputStream(empty, 1);
        assertThat(is.available()).isEqualTo(0);
        is = new UnsynchronizedByteArrayInputStream(empty, 0,1);
        assertThat(is.available()).isEqualTo(0);
        is = new UnsynchronizedByteArrayInputStream(empty, 1,1);
        assertThat(is.available()).isEqualTo(0);

        is = new UnsynchronizedByteArrayInputStream(one, 0);
        assertThat(is.available()).isEqualTo(one.length);
        is = new UnsynchronizedByteArrayInputStream(one, 1);
        assertThat(is.available()).isEqualTo(one.length - 1);
        is = new UnsynchronizedByteArrayInputStream(one, 2);
        assertThat(is.available()).isEqualTo(0);
        is = new UnsynchronizedByteArrayInputStream(one, 0, 1);
        assertThat(is.available()).isEqualTo(1);
        is = new UnsynchronizedByteArrayInputStream(one, 1, 1);
        assertThat(is.available()).isEqualTo(0);
        is = new UnsynchronizedByteArrayInputStream(one, 0, 2);
        assertThat(is.available()).isEqualTo(1);
        is = new UnsynchronizedByteArrayInputStream(one, 2, 1);
        assertThat(is.available()).isEqualTo(0);
        is = new UnsynchronizedByteArrayInputStream(one, 2, 2);
        assertThat(is.available()).isEqualTo(0);

        is = new UnsynchronizedByteArrayInputStream(some, 0);
        assertThat(is.available()).isEqualTo(some.length);
        is = new UnsynchronizedByteArrayInputStream(some, 1);
        assertThat(is.available()).isEqualTo(some.length - 1);
        is = new UnsynchronizedByteArrayInputStream(some, 10);
        assertThat(is.available()).isEqualTo(some.length - 10);
        is = new UnsynchronizedByteArrayInputStream(some, some.length);
        assertThat(is.available()).isEqualTo(0);
        is = new UnsynchronizedByteArrayInputStream(some, some.length, some.length);
        assertThat(is.available()).isEqualTo(0);
        is = new UnsynchronizedByteArrayInputStream(some, some.length - 1, some.length);
        assertThat(is.available()).isEqualTo(1);
        is = new UnsynchronizedByteArrayInputStream(some, 0, 7);
        assertThat(is.available()).isEqualTo(7);
        is = new UnsynchronizedByteArrayInputStream(some, 7, 7);
        assertThat(is.available()).isEqualTo(7);
        is = new UnsynchronizedByteArrayInputStream(some, 0, some.length * 2);
        assertThat(is.available()).isEqualTo(some.length);
        is = new UnsynchronizedByteArrayInputStream(some, some.length - 1, 7);
        assertThat(is.available()).isEqualTo(1);
    }

    @Test
    public void testInvalidConstructor2OffsetUnder() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnsynchronizedByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY, -1);
        });
    }

    @Test
    public void testInvalidConstructor3LengthUnder() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnsynchronizedByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY, 0, -1);
        });
    }

    @Test
    public void testInvalidConstructor3OffsetUnder() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnsynchronizedByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY, -1, 1);
        });
    }

    @Test
    @SuppressWarnings("resource") // not necessary to close these resources
    public void testInvalidReadArrayExplicitLenUnder() {
        final byte[] buf = IOUtils.EMPTY_BYTE_ARRAY;
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(IndexOutOfBoundsException.class, () -> {
            is.read(buf, 0, -1);
        });
    }

    @Test
    public void testInvalidReadArrayExplicitOffsetUnder() {
        final byte[] buf = IOUtils.EMPTY_BYTE_ARRAY;
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(IndexOutOfBoundsException.class, () -> {
            is.read(buf, -1, 1);
        });
    }

    @Test
    public void testInvalidReadArrayExplicitRangeOver() {
        final byte[] buf = IOUtils.EMPTY_BYTE_ARRAY;
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(IndexOutOfBoundsException.class, () -> {
            is.read(buf, 0, 1);
        });
    }

    @Test
    public void testInvalidReadArrayNull() {
        final byte[] buf = null;
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(NullPointerException.class, () -> {
            is.read(buf);
        });
    }

    @Test
    public void testInvalidSkipNUnder() {
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThrows(IllegalArgumentException.class, () -> {
            is.skip(-1);
        });
    }

    @Test
    public void testMarkReset() {
        @SuppressWarnings("resource") // not necessary to close these resources
        final UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThat(is.markSupported()).isTrue();
        assertThat(is.read()).isEqualTo(0xa);
        assertThat(is.markSupported()).isTrue();

        is.mark(10);

        assertThat(is.read()).isEqualTo(0xb);
        assertThat(is.read()).isEqualTo(0xc);

        is.reset();

        assertThat(is.read()).isEqualTo(0xb);
        assertThat(is.read()).isEqualTo(0xc);
        assertThat(is.read()).isEqualTo(END_OF_STREAM);

        is.reset();

        assertThat(is.read()).isEqualTo(0xb);
        assertThat(is.read()).isEqualTo(0xc);
        assertThat(is.read()).isEqualTo(END_OF_STREAM);
    }

    @Test
    public void testReadArray() {
        byte[] buf = new byte[10];
        @SuppressWarnings("resource") // not necessary to close these resources
        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY);
        int read = is.read(buf);
        assertThat(read).isEqualTo(END_OF_STREAM);
        assertThat(buf).containsExactly(new byte[10]);

        buf = IOUtils.EMPTY_BYTE_ARRAY;
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf);
        assertThat(read).isEqualTo(0);

        buf = new byte[10];
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf);
        assertThat(read).isEqualTo(3);
        assertThat(buf[0]).isEqualTo(0xa);
        assertThat(buf[1]).isEqualTo(0xb);
        assertThat(buf[2]).isEqualTo(0xc);
        assertThat(buf[3]).isEqualTo(0);

        buf = new byte[2];
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf);
        assertThat(read).isEqualTo(2);
        assertThat(buf[0]).isEqualTo(0xa);
        assertThat(buf[1]).isEqualTo(0xb);
        read = is.read(buf);
        assertThat(read).isEqualTo(1);
        assertThat(buf[0]).isEqualTo(0xc);
    }

    @Test
    public void testReadArrayExplicit() {
        byte[] buf = new byte[10];
        @SuppressWarnings("resource") // not necessary to close these resources
        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY);
        int read = is.read(buf, 0, 10);
        assertThat(read).isEqualTo(END_OF_STREAM);
        assertThat(buf).containsExactly(new byte[10]);

        buf = new byte[10];
        is = new UnsynchronizedByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY);
        read = is.read(buf, 4, 2);
        assertThat(read).isEqualTo(END_OF_STREAM);
        assertThat(buf).containsExactly(new byte[10]);

        buf = new byte[10];
        is = new UnsynchronizedByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY);
        read = is.read(buf, 4, 6);
        assertThat(read).isEqualTo(END_OF_STREAM);
        assertThat(buf).containsExactly(new byte[10]);

        buf = IOUtils.EMPTY_BYTE_ARRAY;
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf, 0,0);
        assertThat(read).isEqualTo(0);

        buf = new byte[10];
        is = new UnsynchronizedByteArrayInputStream(new byte[]{(byte) 0xa, (byte) 0xb, (byte) 0xc});
        read = is.read(buf, 0, 2);
        assertThat(read).isEqualTo(2);
        assertThat(buf[0]).isEqualTo(0xa);
        assertThat(buf[1]).isEqualTo(0xb);
        assertThat(buf[2]).isEqualTo(0);
        read = is.read(buf, 0, 10);
        assertThat(read).isEqualTo(1);
        assertThat(buf[0]).isEqualTo(0xc);
    }

    @Test
    public void testReadSingle() {
        @SuppressWarnings("resource") // not necessary to close these resources
        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY);
        assertThat(is.read()).isEqualTo(END_OF_STREAM);

        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThat(is.read()).isEqualTo(0xa);
        assertThat(is.read()).isEqualTo(0xb);
        assertThat(is.read()).isEqualTo(0xc);
        assertThat(is.read()).isEqualTo(END_OF_STREAM);
    }

    @Test
    public void testSkip() {
        @SuppressWarnings("resource") // not necessary to close these resources
        UnsynchronizedByteArrayInputStream is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThat(is.available()).isEqualTo(3);

        is.skip(1);
        assertThat(is.available()).isEqualTo(2);
        assertThat(is.read()).isEqualTo(0xb);

        is.skip(1);
        assertThat(is.available()).isEqualTo(0);
        assertThat(is.read()).isEqualTo(END_OF_STREAM);


        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThat(is.available()).isEqualTo(3);
        is.skip(0);
        assertThat(is.available()).isEqualTo(3);
        assertThat(is.read()).isEqualTo(0xa);


        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThat(is.available()).isEqualTo(3);
        is.skip(2);
        assertThat(is.available()).isEqualTo(1);
        assertThat(is.read()).isEqualTo(0xc);
        assertThat(is.read()).isEqualTo(END_OF_STREAM);


        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThat(is.available()).isEqualTo(3);
        is.skip(3);
        assertThat(is.available()).isEqualTo(0);
        assertThat(is.read()).isEqualTo(END_OF_STREAM);


        is = new UnsynchronizedByteArrayInputStream(new byte[] {(byte)0xa, (byte)0xb, (byte)0xc});
        assertThat(is.available()).isEqualTo(3);
        is.skip(999);
        assertThat(is.available()).isEqualTo(0);
        assertThat(is.read()).isEqualTo(END_OF_STREAM);
    }
}
