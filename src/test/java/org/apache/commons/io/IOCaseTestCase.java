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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;

/**
 * This is used to test IOCase for correctness.
 *
 */
public class IOCaseTestCase {

    private static final boolean WINDOWS = File.separatorChar == '\\';

    //-----------------------------------------------------------------------
    @Test
    public void test_forName() {
        assertThat(IOCase.forName("Sensitive")).isEqualTo(IOCase.SENSITIVE);
        assertThat(IOCase.forName("Insensitive")).isEqualTo(IOCase.INSENSITIVE);
        assertThat(IOCase.forName("System")).isEqualTo(IOCase.SYSTEM);
        try {
            IOCase.forName("Blah");
            fail();
        } catch (final IllegalArgumentException ignore) {}
        try {
            IOCase.forName(null);
            fail();
        } catch (final IllegalArgumentException ignore) {}
    }

    @Test
    public void test_serialization() throws Exception {
        assertThat(serialize(IOCase.SENSITIVE)).isSameAs(IOCase.SENSITIVE);
        assertThat(serialize(IOCase.INSENSITIVE)).isSameAs(IOCase.INSENSITIVE);
        assertThat(serialize(IOCase.SYSTEM)).isSameAs(IOCase.SYSTEM);
    }

    @Test
    public void test_getName() {
        assertThat(IOCase.SENSITIVE.getName()).isEqualTo("Sensitive");
        assertThat(IOCase.INSENSITIVE.getName()).isEqualTo("Insensitive");
        assertThat(IOCase.SYSTEM.getName()).isEqualTo("System");
    }

    @Test
    public void test_toString() {
        assertThat(IOCase.SENSITIVE.toString()).isEqualTo("Sensitive");
        assertThat(IOCase.INSENSITIVE.toString()).isEqualTo("Insensitive");
        assertThat(IOCase.SYSTEM.toString()).isEqualTo("System");
    }

    @Test
    public void test_isCaseSensitive() {
        assertThat(IOCase.SENSITIVE.isCaseSensitive()).isTrue();
        assertThat(IOCase.INSENSITIVE.isCaseSensitive()).isFalse();
        assertThat(IOCase.SYSTEM.isCaseSensitive()).isEqualTo(!WINDOWS);
    }
    //-----------------------------------------------------------------------
    @Test
    public void test_checkCompare_functionality() {
        assertThat(IOCase.SENSITIVE.checkCompareTo("ABC", "") > 0).isTrue();
        assertThat(IOCase.SENSITIVE.checkCompareTo("", "ABC") < 0).isTrue();
        assertThat(IOCase.SENSITIVE.checkCompareTo("ABC", "DEF") < 0).isTrue();
        assertThat(IOCase.SENSITIVE.checkCompareTo("DEF", "ABC") > 0).isTrue();
        assertThat(IOCase.SENSITIVE.checkCompareTo("ABC", "ABC")).isEqualTo(0);
        assertThat(IOCase.SENSITIVE.checkCompareTo("", "")).isEqualTo(0);

        try {
            IOCase.SENSITIVE.checkCompareTo("ABC", null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkCompareTo(null, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkCompareTo(null, null);
            fail();
        } catch (final NullPointerException ignore) {}
    }

    @Test
    public void test_checkCompare_case() {
        assertThat(IOCase.SENSITIVE.checkCompareTo("ABC", "ABC")).isEqualTo(0);
        assertThat(IOCase.SENSITIVE.checkCompareTo("ABC", "abc") < 0).isTrue();
        assertThat(IOCase.SENSITIVE.checkCompareTo("abc", "ABC") > 0).isTrue();

        assertThat(IOCase.INSENSITIVE.checkCompareTo("ABC", "ABC")).isEqualTo(0);
        assertThat(IOCase.INSENSITIVE.checkCompareTo("ABC", "abc")).isEqualTo(0);
        assertThat(IOCase.INSENSITIVE.checkCompareTo("abc", "ABC")).isEqualTo(0);

        assertThat(IOCase.SYSTEM.checkCompareTo("ABC", "ABC")).isEqualTo(0);
        assertThat(IOCase.SYSTEM.checkCompareTo("ABC", "abc") == 0).isEqualTo(WINDOWS);
        assertThat(IOCase.SYSTEM.checkCompareTo("abc", "ABC") == 0).isEqualTo(WINDOWS);
    }


    //-----------------------------------------------------------------------
    @Test
    public void test_checkEquals_functionality() {
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "A")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "AB")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "ABC")).isTrue();
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "BC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "C")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "ABCD")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEquals("", "ABC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEquals("", "")).isTrue();

        try {
            IOCase.SENSITIVE.checkEquals("ABC", null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkEquals(null, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkEquals(null, null);
            fail();
        } catch (final NullPointerException ignore) {}
    }

    @Test
    public void test_checkEquals_case() {
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "ABC")).isTrue();
        assertThat(IOCase.SENSITIVE.checkEquals("ABC", "Abc")).isFalse();

        assertThat(IOCase.INSENSITIVE.checkEquals("ABC", "ABC")).isTrue();
        assertThat(IOCase.INSENSITIVE.checkEquals("ABC", "Abc")).isTrue();

        assertThat(IOCase.SYSTEM.checkEquals("ABC", "ABC")).isTrue();
        assertThat(IOCase.SYSTEM.checkEquals("ABC", "Abc")).isEqualTo(WINDOWS);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_checkStartsWith_functionality() {
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "")).isTrue();
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "A")).isTrue();
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "AB")).isTrue();
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "ABC")).isTrue();
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "BC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "C")).isFalse();
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "ABCD")).isFalse();
        assertThat(IOCase.SENSITIVE.checkStartsWith("", "ABC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkStartsWith("", "")).isTrue();

        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", null)).isFalse();
        assertThat(IOCase.SENSITIVE.checkStartsWith(null, "ABC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkStartsWith(null, null)).isFalse();
    }

    @Test
    public void test_checkStartsWith_case() {
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "AB")).isTrue();
        assertThat(IOCase.SENSITIVE.checkStartsWith("ABC", "Ab")).isFalse();

        assertThat(IOCase.INSENSITIVE.checkStartsWith("ABC", "AB")).isTrue();
        assertThat(IOCase.INSENSITIVE.checkStartsWith("ABC", "Ab")).isTrue();

        assertThat(IOCase.SYSTEM.checkStartsWith("ABC", "AB")).isTrue();
        assertThat(IOCase.SYSTEM.checkStartsWith("ABC", "Ab")).isEqualTo(WINDOWS);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_checkEndsWith_functionality() {
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "")).isTrue();
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "A")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "AB")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "ABC")).isTrue();
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "BC")).isTrue();
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "C")).isTrue();
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "ABCD")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEndsWith("", "ABC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEndsWith("", "")).isTrue();

        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", null)).isFalse();
        assertThat(IOCase.SENSITIVE.checkEndsWith(null, "ABC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkEndsWith(null, null)).isFalse();
    }

    @Test
    public void test_checkEndsWith_case() {
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "BC")).isTrue();
        assertThat(IOCase.SENSITIVE.checkEndsWith("ABC", "Bc")).isFalse();

        assertThat(IOCase.INSENSITIVE.checkEndsWith("ABC", "BC")).isTrue();
        assertThat(IOCase.INSENSITIVE.checkEndsWith("ABC", "Bc")).isTrue();

        assertThat(IOCase.SYSTEM.checkEndsWith("ABC", "BC")).isTrue();
        assertThat(IOCase.SYSTEM.checkEndsWith("ABC", "Bc")).isEqualTo(WINDOWS);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_checkIndexOf_functionality() {

        // start
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "A")).isEqualTo(0);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 1, "A")).isEqualTo(-1);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "AB")).isEqualTo(0);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 1, "AB")).isEqualTo(-1);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "ABC")).isEqualTo(0);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 1, "ABC")).isEqualTo(-1);

        // middle
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "D")).isEqualTo(3);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 3, "D")).isEqualTo(3);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 4, "D")).isEqualTo(-1);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "DE")).isEqualTo(3);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 3, "DE")).isEqualTo(3);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 4, "DE")).isEqualTo(-1);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "DEF")).isEqualTo(3);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 3, "DEF")).isEqualTo(3);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 4, "DEF")).isEqualTo(-1);

        // end
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "J")).isEqualTo(9);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 8, "J")).isEqualTo(9);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 9, "J")).isEqualTo(9);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "IJ")).isEqualTo(8);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 8, "IJ")).isEqualTo(8);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 9, "IJ")).isEqualTo(-1);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 6, "HIJ")).isEqualTo(7);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 7, "HIJ")).isEqualTo(7);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 8, "HIJ")).isEqualTo(-1);

        // not found
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABCDEFGHIJ", 0, "DED")).isEqualTo(-1);

        // too long
        assertThat(IOCase.SENSITIVE.checkIndexOf("DEF", 0, "ABCDEFGHIJ")).isEqualTo(-1);

        try {
            IOCase.SENSITIVE.checkIndexOf("ABC", 0, null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkIndexOf(null, 0, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkIndexOf(null, 0, null);
            fail();
        } catch (final NullPointerException ignore) {}
    }

    @Test
    public void test_checkIndexOf_case() {
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABC", 0, "BC")).isEqualTo(1);
        assertThat(IOCase.SENSITIVE.checkIndexOf("ABC", 0, "Bc")).isEqualTo(-1);

        assertThat(IOCase.INSENSITIVE.checkIndexOf("ABC", 0, "BC")).isEqualTo(1);
        assertThat(IOCase.INSENSITIVE.checkIndexOf("ABC", 0, "Bc")).isEqualTo(1);

        assertThat(IOCase.SYSTEM.checkIndexOf("ABC", 0, "BC")).isEqualTo(1);
        assertThat(IOCase.SYSTEM.checkIndexOf("ABC", 0, "Bc")).isEqualTo(WINDOWS ? 1 : -1);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_checkRegionMatches_functionality() {
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "")).isTrue();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "A")).isTrue();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "AB")).isTrue();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "ABC")).isTrue();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "BC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "C")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "ABCD")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("", 0, "ABC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("", 0, "")).isTrue();

        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "")).isTrue();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "A")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "AB")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "ABC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "BC")).isTrue();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "C")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 1, "ABCD")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("", 1, "ABC")).isFalse();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("", 1, "")).isFalse();

        try {
            IOCase.SENSITIVE.checkRegionMatches("ABC", 0, null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 0, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 0, null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches("ABC", 1, null);
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 1, "ABC");
            fail();
        } catch (final NullPointerException ignore) {}
        try {
            IOCase.SENSITIVE.checkRegionMatches(null, 1, null);
            fail();
        } catch (final NullPointerException ignore) {}
    }

    @Test
    public void test_checkRegionMatches_case() {
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "AB")).isTrue();
        assertThat(IOCase.SENSITIVE.checkRegionMatches("ABC", 0, "Ab")).isFalse();

        assertThat(IOCase.INSENSITIVE.checkRegionMatches("ABC", 0, "AB")).isTrue();
        assertThat(IOCase.INSENSITIVE.checkRegionMatches("ABC", 0, "Ab")).isTrue();

        assertThat(IOCase.SYSTEM.checkRegionMatches("ABC", 0, "AB")).isTrue();
        assertThat(IOCase.SYSTEM.checkRegionMatches("ABC", 0, "Ab")).isEqualTo(WINDOWS);
    }

    //-----------------------------------------------------------------------
    private IOCase serialize(final IOCase value) throws Exception {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buf);
        out.writeObject(value);
        out.flush();
        out.close();

        final ByteArrayInputStream bufin = new ByteArrayInputStream(buf.toByteArray());
        final ObjectInputStream in = new ObjectInputStream(bufin);
        return (IOCase) in.readObject();
    }

}
