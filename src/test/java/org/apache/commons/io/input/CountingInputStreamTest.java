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
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests the CountingInputStream.
 *
 */
public class CountingInputStreamTest {

    @Test
    public void testCounting() throws Exception {
        final String text = "A piece of text";
        final byte[] bytes = text.getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            // have to declare this larger as we're going to read
            // off the end of the stream and input stream seems
            // to do bounds checking
            final byte[] result = new byte[21];

            final byte[] ba = new byte[5];
            int found = cis.read(ba);
            System.arraycopy(ba, 0, result, 0, 5);
            assertThat(cis.getCount()).isEqualTo(found);

            final int value = cis.read();
            found++;
            result[5] = (byte) value;
            assertThat(cis.getCount()).isEqualTo(found);

            found += cis.read(result, 6, 5);
            assertThat(cis.getCount()).isEqualTo(found);

            found += cis.read(result, 11, 10); // off the end
            assertThat(cis.getCount()).isEqualTo(found);

            // trim to get rid of the 6 empty values
            final String textResult = new String(result).trim();
            assertThat(text).isEqualTo(textResult);
        }
    }


    /*
     * Test for files > 2GB in size - see issue IO-84
     */
    @Test
    public void testLargeFiles_IO84() throws Exception {
        final long size = (long) Integer.MAX_VALUE + (long) 1;
        final NullInputStream mock = new NullInputStream(size);
        final CountingInputStream cis = new CountingInputStream(mock);

        // Test integer methods
        IOUtils.consume(cis);
        try {
            cis.getCount();
            fail("Expected getCount() to throw an ArithmeticException");
        } catch (final ArithmeticException ae) {
            // expected result
        }
        try {
            cis.resetCount();
            fail("Expected resetCount() to throw an ArithmeticException");
        } catch (final ArithmeticException ae) {
            // expected result
        }

        mock.close();

        // Test long methods
        IOUtils.consume(cis);
        assertThat(cis.getByteCount()).as("getByteCount()").isEqualTo(size);
        assertThat(cis.resetByteCount()).as("resetByteCount()").isEqualTo(size);
    }

    @Test
    public void testResetting() throws Exception {
        final String text = "A piece of text";
        final byte[] bytes = text.getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            final byte[] result = new byte[bytes.length];

            int found = cis.read(result, 0, 5);
            assertThat(cis.getCount()).isEqualTo(found);

            final int count = cis.resetCount();
            found = cis.read(result, 6, 5);
            assertThat(count).isEqualTo(found);
        }
    }

    @Test
    public void testZeroLength1() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            final int found = cis.read();
            assertThat(found).isEqualTo(-1);
            assertThat(cis.getCount()).isEqualTo(0);
        }
    }

    @Test
    public void testZeroLength2() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            final byte[] result = new byte[10];

            final int found = cis.read(result);
            assertThat(found).isEqualTo(-1);
            assertThat(cis.getCount()).isEqualTo(0);
        }
    }

    @Test
    public void testZeroLength3() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.EMPTY_BYTE_ARRAY);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            final byte[] result = new byte[10];

            final int found = cis.read(result, 0, 5);
            assertThat(found).isEqualTo(-1);
            assertThat(cis.getCount()).isEqualTo(0);
        }
    }

    @Test
    public void testEOF1() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[2]);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            int found = cis.read();
            assertThat(found).isEqualTo(0);
            assertThat(cis.getCount()).isEqualTo(1);
            found = cis.read();
            assertThat(found).isEqualTo(0);
            assertThat(cis.getCount()).isEqualTo(2);
            found = cis.read();
            assertThat(found).isEqualTo(-1);
            assertThat(cis.getCount()).isEqualTo(2);
        }
    }

    @Test
    public void testEOF2() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[2]);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            final byte[] result = new byte[10];

            final int found = cis.read(result);
            assertThat(found).isEqualTo(2);
            assertThat(cis.getCount()).isEqualTo(2);
        }
    }

    @Test
    public void testEOF3() throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[2]);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            final byte[] result = new byte[10];

            final int found = cis.read(result, 0, 5);
            assertThat(found).isEqualTo(2);
            assertThat(cis.getCount()).isEqualTo(2);
        }
    }

    @Test
    public void testSkipping() throws IOException {
        final String text = "Hello World!";
        final byte[] bytes = text.getBytes();
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try (final CountingInputStream cis = new CountingInputStream(bais)) {

            assertThat(cis.skip(6)).isEqualTo(6);
            assertThat(cis.getCount()).isEqualTo(6);
            final byte[] result = new byte[6];
            cis.read(result);

            assertThat(new String(result)).isEqualTo("World!");
            assertThat(cis.getCount()).isEqualTo(12);
        }
    }

}
