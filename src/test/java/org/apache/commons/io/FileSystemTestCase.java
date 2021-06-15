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

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

public class FileSystemTestCase {


    @Test
    public void testGetCurrent() {
        if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(FileSystem.getCurrent()).isEqualTo(FileSystem.WINDOWS);
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(FileSystem.getCurrent()).isEqualTo(FileSystem.LINUX);
        }
        if (SystemUtils.IS_OS_MAC_OSX) {
            assertThat(FileSystem.getCurrent()).isEqualTo(FileSystem.MAC_OSX);
        }
    }

    @Test
    public void testIsLegalName() {
        for (final FileSystem fs : FileSystem.values()) {
            assertThat(fs.isLegalFileName("")).withFailMessage(fs.name()).isFalse(); // Empty is always illegal
            assertThat(fs.isLegalFileName(null)).withFailMessage(fs.name()).isFalse(); // null is always illegal
            assertThat(fs.isLegalFileName("\0")).withFailMessage(fs.name()).isFalse(); // Assume NUL is always illegal
            assertThat(fs.isLegalFileName("0")).withFailMessage(fs.name()).isTrue(); // Assume simple name always legal
            for (final String candidate : fs.getReservedFileNames()) {
                // Reserved file names are not legal
                assertThat(fs.isLegalFileName(candidate)).isFalse();
            }
        }
    }

    @Test
    public void testIsReservedFileName() {
        for (final FileSystem fs : FileSystem.values()) {
            for (final String candidate : fs.getReservedFileNames()) {
                assertThat(fs.isReservedFileName(candidate)).isTrue();
            }
        }
    }

    @Test
    public void testReplacementWithNUL() {
        for (final FileSystem fs : FileSystem.values()) {
            try {
                fs.toLegalFileName("Test", '\0'); // Assume NUL is always illegal
            } catch (final IllegalArgumentException iae) {
                assertThat(iae.getMessage().startsWith("The replacement character '\\0'")).withFailMessage(iae.getMessage()).isTrue();
            }
        }
    }

    @Test
    public void testSorted() {
        for (final FileSystem fs : FileSystem.values()) {
            final char[] chars = fs.getIllegalFileNameChars();
            for (int i = 0; i < chars.length - 1; i++) {
                assertThat(chars[i] < chars[i + 1]).withFailMessage(fs.name()).isTrue();
            }
        }
    }

    @Test
    public void testSupportsDriveLetter() {
        assertThat(FileSystem.WINDOWS.supportsDriveLetter()).isTrue();
        assertThat(FileSystem.GENERIC.supportsDriveLetter()).isFalse();
        assertThat(FileSystem.LINUX.supportsDriveLetter()).isFalse();
        assertThat(FileSystem.MAC_OSX.supportsDriveLetter()).isFalse();
    }

    @Test
    public void testToLegalFileNameWindows() {
        final FileSystem fs = FileSystem.WINDOWS;
        final char replacement = '-';
        for (char i = 0; i < 32; i++) {
            assertThat(fs.toLegalFileName(String.valueOf(i), replacement).charAt(0)).isEqualTo(replacement);
        }
        final char[] illegal = { '<', '>', ':', '"', '/', '\\', '|', '?', '*' };
        for (char i = 0; i < illegal.length; i++) {
            assertThat(fs.toLegalFileName(String.valueOf(i), replacement).charAt(0)).isEqualTo(replacement);
        }
        for (char i = 'a'; i < 'z'; i++) {
            assertThat(fs.toLegalFileName(String.valueOf(i), replacement).charAt(0)).isEqualTo(i);
        }
        for (char i = 'A'; i < 'Z'; i++) {
            assertThat(fs.toLegalFileName(String.valueOf(i), replacement).charAt(0)).isEqualTo(i);
        }
        for (char i = '0'; i < '9'; i++) {
            assertThat(fs.toLegalFileName(String.valueOf(i), replacement).charAt(0)).isEqualTo(i);
        }
    }
}
