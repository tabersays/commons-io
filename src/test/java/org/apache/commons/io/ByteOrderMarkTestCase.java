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
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;


/**
 * Test for {@link ByteOrderMark}.
 *
 */
public class ByteOrderMarkTestCase  {

    private static final ByteOrderMark TEST_BOM_1 = new ByteOrderMark("test1", 1);
    private static final ByteOrderMark TEST_BOM_2 = new ByteOrderMark("test2", 1, 2);
    private static final ByteOrderMark TEST_BOM_3 = new ByteOrderMark("test3", 1, 2, 3);

    /** Test {@link ByteOrderMark#getCharsetName()} */
    @Test
    public void charsetName() {
        assertThat(TEST_BOM_1.getCharsetName()).as("test1 name").isEqualTo("test1");
        assertThat(TEST_BOM_2.getCharsetName()).as("test2 name").isEqualTo("test2");
        assertThat(TEST_BOM_3.getCharsetName()).as("test3 name").isEqualTo("test3");
    }

    /** Tests that {@link ByteOrderMark#getCharsetName()} can be loaded as a {@link java.nio.charset.Charset} as advertised. */
    @Test
    public void constantCharsetNames() {
        assertThat(Charset.forName(ByteOrderMark.UTF_8.getCharsetName())).isNotNull();
        assertThat(Charset.forName(ByteOrderMark.UTF_16BE.getCharsetName())).isNotNull();
        assertThat(Charset.forName(ByteOrderMark.UTF_16LE.getCharsetName())).isNotNull();
        assertThat(Charset.forName(ByteOrderMark.UTF_32BE.getCharsetName())).isNotNull();
        assertThat(Charset.forName(ByteOrderMark.UTF_32LE.getCharsetName())).isNotNull();
    }

    /** Test {@link ByteOrderMark#length()} */
    @Test
    public void testLength() {
        assertThat(TEST_BOM_1.length()).as("test1 length").isEqualTo(1);
        assertThat(TEST_BOM_2.length()).as("test2 length").isEqualTo(2);
        assertThat(TEST_BOM_3.length()).as("test3 length").isEqualTo(3);
    }

    /** Test {@link ByteOrderMark#get(int)} */
    @Test
    public void get() {
        assertThat(TEST_BOM_1.get(0)).as("test1 get(0)").isEqualTo(1);
        assertThat(TEST_BOM_2.get(0)).as("test2 get(0)").isEqualTo(1);
        assertThat(TEST_BOM_2.get(1)).as("test2 get(1)").isEqualTo(2);
        assertThat(TEST_BOM_3.get(0)).as("test3 get(0)").isEqualTo(1);
        assertThat(TEST_BOM_3.get(1)).as("test3 get(1)").isEqualTo(2);
        assertThat(TEST_BOM_3.get(2)).as("test3 get(2)").isEqualTo(3);
    }

    /** Test {@link ByteOrderMark#getBytes()} */
    @Test
    public void getBytes() {
        assertThat(new byte[]{(byte) 1}).as("test1 bytes").containsExactly(TEST_BOM_1.getBytes());
        TEST_BOM_1.getBytes()[0] = 2;
        assertThat(new byte[]{(byte) 1}).as("test1 bytes").containsExactly(TEST_BOM_1.getBytes());
        assertThat(new byte[]{(byte) 1, (byte) 2}).as("test1 bytes").containsExactly(TEST_BOM_2.getBytes());
        assertThat(new byte[]{(byte) 1, (byte) 2, (byte) 3}).as("test1 bytes").containsExactly(TEST_BOM_3.getBytes());
    }

    /** Test {@link ByteOrderMark#equals(Object)} */
    @SuppressWarnings("EqualsWithItself")
    @Test
    public void testEquals() {
        assertThat(TEST_BOM_1).as("test1 equals").isEqualTo(TEST_BOM_1);
        assertThat(TEST_BOM_2).as("test2 equals").isEqualTo(TEST_BOM_2);
        assertThat(TEST_BOM_3).as("test3 equals").isEqualTo(TEST_BOM_3);

        assertThat(new Object()).as("Object not equal").isNotEqualTo(TEST_BOM_1);
        assertThat(new ByteOrderMark("1a", 2)).as("test1-1 not equal").isNotEqualTo(TEST_BOM_1);
        assertThat(new ByteOrderMark("1b", 1, 2)).as("test1-2 not test2").isNotEqualTo(TEST_BOM_1);
        assertThat(new ByteOrderMark("2", 1, 1)).as("test2 not equal").isNotEqualTo(TEST_BOM_2);
        assertThat(new ByteOrderMark("3", 1, 2, 4)).as("test3 not equal").isNotEqualTo(TEST_BOM_3);
    }

    /** Test {@link ByteOrderMark#hashCode()} */
    @Test
    public void testHashCode() {
        final int bomClassHash = ByteOrderMark.class.hashCode();
        assertThat(TEST_BOM_1.hashCode()).as("hash test1 ").isEqualTo(bomClassHash + 1);
        assertThat(TEST_BOM_2.hashCode()).as("hash test2 ").isEqualTo(bomClassHash + 3);
        assertThat(TEST_BOM_3.hashCode()).as("hash test3 ").isEqualTo(bomClassHash + 6);
    }

    /** Test Errors */
    @Test
    public void errors() {
        try {
            new ByteOrderMark(null, 1,2,3);
            fail("null charset name, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
        try {
            new ByteOrderMark("", 1,2,3);
            fail("no charset name, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
        try {
            new ByteOrderMark("a", (int[])null);
            fail("null bytes, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
        try {
            new ByteOrderMark("b");
            fail("empty bytes, expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    /** Test {@link ByteOrderMark#toString()} */
    @Test
    public void testToString() {
        assertThat(TEST_BOM_1.toString()).as("test1 ").isEqualTo("ByteOrderMark[test1: 0x1]");
        assertThat(TEST_BOM_2.toString()).as("test2 ").isEqualTo("ByteOrderMark[test2: 0x1,0x2]");
        assertThat(TEST_BOM_3.toString()).as("test3 ").isEqualTo("ByteOrderMark[test3: 0x1,0x2,0x3]");
    }
}
