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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.SortedMap;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Charsets}.
 *
 */
@SuppressWarnings("deprecation") // testing deprecated code
public class CharsetsTestCase {

    @Test
    public void testRequiredCharsets() {
        final SortedMap<String, Charset> requiredCharsets = Charsets.requiredCharsets();
        // test for what we expect to be there as of Java 6
        // Make sure the object at the given key is the right one
        assertThat("US-ASCII").isEqualTo(requiredCharsets.get("US-ASCII").name());
        assertThat("ISO-8859-1").isEqualTo(requiredCharsets.get("ISO-8859-1").name());
        assertThat("UTF-8").isEqualTo(requiredCharsets.get("UTF-8").name());
        assertThat("UTF-16").isEqualTo(requiredCharsets.get("UTF-16").name());
        assertThat("UTF-16BE").isEqualTo(requiredCharsets.get("UTF-16BE").name());
        assertThat("UTF-16LE").isEqualTo(requiredCharsets.get("UTF-16LE").name());
    }

    @Test
    public void testIso8859_1() {
        assertThat(Charsets.ISO_8859_1.name()).isEqualTo("ISO-8859-1");
    }

    @Test
    public void testToCharset() {
        assertThat(Charsets.toCharset((String) null)).isEqualTo(Charset.defaultCharset());
        assertThat(Charsets.toCharset((Charset) null)).isEqualTo(Charset.defaultCharset());
        assertThat(Charsets.toCharset(Charset.defaultCharset())).isEqualTo(Charset.defaultCharset());
        assertThat(Charsets.toCharset(StandardCharsets.UTF_8)).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    public void testUsAscii() {
        assertThat(Charsets.US_ASCII.name()).isEqualTo("US-ASCII");
    }

    @Test
    public void testUtf16() {
        assertThat(Charsets.UTF_16.name()).isEqualTo("UTF-16");
    }

    @Test
    public void testUtf16Be() {
        assertThat(Charsets.UTF_16BE.name()).isEqualTo("UTF-16BE");
    }

    @Test
    public void testUtf16Le() {
        assertThat(Charsets.UTF_16LE.name()).isEqualTo("UTF-16LE");
    }

    @Test
    public void testUtf8() {
        assertThat(Charsets.UTF_8.name()).isEqualTo("UTF-8");
    }

}
