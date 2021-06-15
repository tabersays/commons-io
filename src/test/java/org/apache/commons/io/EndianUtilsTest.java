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
package org.apache.commons.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class EndianUtilsTest  {

    @Test
    public void testCtor() {
        new EndianUtils();
        // Constructor does not blow up.
    }

    @Test
    public void testEOFException() throws IOException {
        final ByteArrayInputStream input = new ByteArrayInputStream(new byte[] {});
        try {
            EndianUtils.readSwappedDouble(input);
            fail("Expected EOFException");
        } catch (final EOFException e) {
            // expected
        }
    }

    @Test
    public void testSwapShort() {
        assertThat(EndianUtils.swapShort((short) 0)).isEqualTo((short) 0);
        assertThat(EndianUtils.swapShort((short) 0x0102)).isEqualTo((short) 0x0201);
        assertThat(EndianUtils.swapShort((short) 0xffff)).isEqualTo((short) 0xffff);
        assertThat(EndianUtils.swapShort((short) 0x0201)).isEqualTo((short) 0x0102);
    }

    @Test
    public void testSwapInteger() {
        assertThat(EndianUtils.swapInteger(0)).isEqualTo(0);
        assertThat(EndianUtils.swapInteger(0x01020304)).isEqualTo(0x04030201);
        assertThat(EndianUtils.swapInteger(0x00000001)).isEqualTo(0x01000000);
        assertThat(EndianUtils.swapInteger(0x01000000)).isEqualTo(0x00000001);
        assertThat(EndianUtils.swapInteger(0x11111111)).isEqualTo(0x11111111);
        assertThat(EndianUtils.swapInteger(0x10efcdab)).isEqualTo(0xabcdef10);
        assertThat(EndianUtils.swapInteger(0xab000000)).isEqualTo(0xab);
    }

    @Test
    public void testSwapLong() {
        assertThat(EndianUtils.swapLong(0)).isEqualTo(0);
        assertThat(EndianUtils.swapLong(0x0102030405060708L)).isEqualTo(0x0807060504030201L);
        assertThat(EndianUtils.swapLong(0xffffffffffffffffL)).isEqualTo(0xffffffffffffffffL);
        assertThat(EndianUtils.swapLong(0xab00000000000000L)).isEqualTo(0xab);
    }

    @Test
    public void testSwapFloat() {
        assertThat(EndianUtils.swapFloat(0.0f)).isCloseTo(0.0f, within(0.0));
        final float f1 = Float.intBitsToFloat( 0x01020304 );
        final float f2 = Float.intBitsToFloat( 0x04030201 );
        assertThat(EndianUtils.swapFloat(f1)).isCloseTo(f2, within(0.0));
    }

    @Test
    public void testSwapDouble() {
        assertThat(EndianUtils.swapDouble(0.0)).isCloseTo(0.0, within(0.0));
        final double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        final double d2 = Double.longBitsToDouble( 0x0807060504030201L );
        assertThat(EndianUtils.swapDouble(d1)).isCloseTo(d2, within(0.0));
    }

    /**
     * Tests all swapXxxx methods for symmetry when going from one endian
     * to another and back again.
     */
    @Test
    public void testSymmetry() {
        assertThat(EndianUtils.swapShort(EndianUtils.swapShort((short) 0x0102))).isEqualTo((short) 0x0102);
        assertThat(EndianUtils.swapInteger(EndianUtils.swapInteger(0x01020304))).isEqualTo(0x01020304);
        assertThat(EndianUtils.swapLong(EndianUtils.swapLong(0x0102030405060708L))).isEqualTo(0x0102030405060708L);
        final float f1 = Float.intBitsToFloat( 0x01020304 );
        assertThat(EndianUtils.swapFloat(EndianUtils.swapFloat(f1))).isCloseTo(f1, within(0.0));
        final double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        assertThat(EndianUtils.swapDouble(EndianUtils.swapDouble(d1))).isCloseTo(d1, within(0.0));
    }

    @Test
    public void testReadSwappedShort() throws IOException {
        final byte[] bytes = { 0x02, 0x01 };
        assertThat(EndianUtils.readSwappedShort(bytes, 0)).isEqualTo(0x0102);

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertThat(EndianUtils.readSwappedShort(input)).isEqualTo(0x0102);
    }

    @Test
    public void testWriteSwappedShort() throws IOException {
        byte[] bytes = new byte[2];
        EndianUtils.writeSwappedShort( bytes, 0, (short) 0x0102 );
        assertThat(bytes[0]).isEqualTo(0x02);
        assertThat(bytes[1]).isEqualTo(0x01);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
        EndianUtils.writeSwappedShort( baos, (short) 0x0102 );
        bytes = baos.toByteArray();
        assertThat(bytes[0]).isEqualTo(0x02);
        assertThat(bytes[1]).isEqualTo(0x01);
    }

    @Test
    public void testReadSwappedUnsignedShort() throws IOException {
        final byte[] bytes = { 0x02, 0x01 };
        assertThat(EndianUtils.readSwappedUnsignedShort(bytes, 0)).isEqualTo(0x00000102);

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertThat(EndianUtils.readSwappedUnsignedShort(input)).isEqualTo(0x00000102);
    }

    @Test
    public void testReadSwappedInteger() throws IOException {
        final byte[] bytes = { 0x04, 0x03, 0x02, 0x01 };
        assertThat(EndianUtils.readSwappedInteger(bytes, 0)).isEqualTo(0x01020304);

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertThat(EndianUtils.readSwappedInteger(input)).isEqualTo(0x01020304);
    }

    @Test
    public void testWriteSwappedInteger() throws IOException {
        byte[] bytes = new byte[4];
        EndianUtils.writeSwappedInteger( bytes, 0, 0x01020304 );
        assertThat(bytes[0]).isEqualTo(0x04);
        assertThat(bytes[1]).isEqualTo(0x03);
        assertThat(bytes[2]).isEqualTo(0x02);
        assertThat(bytes[3]).isEqualTo(0x01);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        EndianUtils.writeSwappedInteger( baos, 0x01020304 );
        bytes = baos.toByteArray();
        assertThat(bytes[0]).isEqualTo(0x04);
        assertThat(bytes[1]).isEqualTo(0x03);
        assertThat(bytes[2]).isEqualTo(0x02);
        assertThat(bytes[3]).isEqualTo(0x01);
    }

    @Test
    public void testReadSwappedUnsignedInteger() throws IOException {
        final byte[] bytes = { 0x04, 0x03, 0x02, 0x01 };
        assertThat(EndianUtils.readSwappedUnsignedInteger(bytes, 0)).isEqualTo(0x0000000001020304L);

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertThat(EndianUtils.readSwappedUnsignedInteger(input)).isEqualTo(0x0000000001020304L);
    }

    @Test
    public void testReadSwappedLong() throws IOException {
        final byte[] bytes = { 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01 };
        assertThat(EndianUtils.readSwappedLong(bytes, 0)).isEqualTo(0x0102030405060708L);

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertThat(EndianUtils.readSwappedLong(input)).isEqualTo(0x0102030405060708L);
    }

    @Test
    public void testWriteSwappedLong() throws IOException {
        byte[] bytes = new byte[8];
        EndianUtils.writeSwappedLong( bytes, 0, 0x0102030405060708L );
        assertThat(bytes[0]).isEqualTo(0x08);
        assertThat(bytes[1]).isEqualTo(0x07);
        assertThat(bytes[2]).isEqualTo(0x06);
        assertThat(bytes[3]).isEqualTo(0x05);
        assertThat(bytes[4]).isEqualTo(0x04);
        assertThat(bytes[5]).isEqualTo(0x03);
        assertThat(bytes[6]).isEqualTo(0x02);
        assertThat(bytes[7]).isEqualTo(0x01);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
        EndianUtils.writeSwappedLong( baos, 0x0102030405060708L );
        bytes = baos.toByteArray();
        assertThat(bytes[0]).isEqualTo(0x08);
        assertThat(bytes[1]).isEqualTo(0x07);
        assertThat(bytes[2]).isEqualTo(0x06);
        assertThat(bytes[3]).isEqualTo(0x05);
        assertThat(bytes[4]).isEqualTo(0x04);
        assertThat(bytes[5]).isEqualTo(0x03);
        assertThat(bytes[6]).isEqualTo(0x02);
        assertThat(bytes[7]).isEqualTo(0x01);
    }

    @Test
    public void testReadSwappedFloat() throws IOException {
        final byte[] bytes = { 0x04, 0x03, 0x02, 0x01 };
        final float f1 = Float.intBitsToFloat( 0x01020304 );
        final float f2 = EndianUtils.readSwappedFloat( bytes, 0 );
        assertThat(f2).isCloseTo(f1, within(0.0));

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertThat(EndianUtils.readSwappedFloat(input)).isCloseTo(f1, within(0.0));
    }

    @Test
    public void testWriteSwappedFloat() throws IOException {
        byte[] bytes = new byte[4];
        final float f1 = Float.intBitsToFloat( 0x01020304 );
        EndianUtils.writeSwappedFloat( bytes, 0, f1 );
        assertThat(bytes[0]).isEqualTo(0x04);
        assertThat(bytes[1]).isEqualTo(0x03);
        assertThat(bytes[2]).isEqualTo(0x02);
        assertThat(bytes[3]).isEqualTo(0x01);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        EndianUtils.writeSwappedFloat( baos, f1 );
        bytes = baos.toByteArray();
        assertThat(bytes[0]).isEqualTo(0x04);
        assertThat(bytes[1]).isEqualTo(0x03);
        assertThat(bytes[2]).isEqualTo(0x02);
        assertThat(bytes[3]).isEqualTo(0x01);
    }

    @Test
    public void testReadSwappedDouble() throws IOException {
        final byte[] bytes = { 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01 };
        final double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        final double d2 = EndianUtils.readSwappedDouble( bytes, 0 );
        assertThat(d2).isCloseTo(d1, within(0.0));

        final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        assertThat(EndianUtils.readSwappedDouble(input)).isCloseTo(d1, within(0.0));
    }

    @Test
    public void testWriteSwappedDouble() throws IOException {
        byte[] bytes = new byte[8];
        final double d1 = Double.longBitsToDouble( 0x0102030405060708L );
        EndianUtils.writeSwappedDouble( bytes, 0, d1 );
        assertThat(bytes[0]).isEqualTo(0x08);
        assertThat(bytes[1]).isEqualTo(0x07);
        assertThat(bytes[2]).isEqualTo(0x06);
        assertThat(bytes[3]).isEqualTo(0x05);
        assertThat(bytes[4]).isEqualTo(0x04);
        assertThat(bytes[5]).isEqualTo(0x03);
        assertThat(bytes[6]).isEqualTo(0x02);
        assertThat(bytes[7]).isEqualTo(0x01);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
        EndianUtils.writeSwappedDouble( baos, d1 );
        bytes = baos.toByteArray();
        assertThat(bytes[0]).isEqualTo(0x08);
        assertThat(bytes[1]).isEqualTo(0x07);
        assertThat(bytes[2]).isEqualTo(0x06);
        assertThat(bytes[3]).isEqualTo(0x05);
        assertThat(bytes[4]).isEqualTo(0x04);
        assertThat(bytes[5]).isEqualTo(0x03);
        assertThat(bytes[6]).isEqualTo(0x02);
        assertThat(bytes[7]).isEqualTo(0x01);
    }

    // tests #IO-101
    @Test
    public void testSymmetryOfLong() {

        final double[] tests = {34.345, -345.5645, 545.12, 10.043, 7.123456789123};
        for (final double test : tests) {

            // testing the real problem
            byte[] buffer = new byte[8];
            final long ln1 = Double.doubleToLongBits( test );
            EndianUtils.writeSwappedLong(buffer, 0, ln1);
            final long ln2 = EndianUtils.readSwappedLong(buffer, 0);
            assertThat(ln2).isEqualTo(ln1);

            // testing the bug report
            buffer = new byte[8];
            EndianUtils.writeSwappedDouble(buffer, 0, test);
            final double val = EndianUtils.readSwappedDouble(buffer, 0);
            assertThat(val).withFailMessage(0).isEqualTo(test);
        }
    }

    // tests #IO-117
    @Test
    public void testUnsignedOverrun() throws Exception {
        final byte[] target = { 0, 0, 0, (byte)0x80 };
        final long expected = 0x80000000L;

        long actual = EndianUtils.readSwappedUnsignedInteger(target, 0);
        assertThat(actual).as("readSwappedUnsignedInteger(byte[], int) was incorrect").isEqualTo(expected);

        final ByteArrayInputStream in = new ByteArrayInputStream(target);
        actual = EndianUtils.readSwappedUnsignedInteger(in);
        assertThat(actual).as("readSwappedUnsignedInteger(InputStream) was incorrect").isEqualTo(expected);
    }

}
