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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This is used to test FilenameUtils for correctness.
 *
 * @see FilenameUtils
 */
public class FilenameUtilsTestCase {

    @TempDir
    public File temporaryFolder;

    private static final String SEP = "" + File.separatorChar;
    private static final boolean WINDOWS = File.separatorChar == '\\';

    private File testFile1;
    private File testFile2;

    private int testFile1Size;
    private int testFile2Size;

    @BeforeEach
    public void setUp() throws Exception {
        testFile1 = File.createTempFile("test", "1", temporaryFolder);
        testFile2 = File.createTempFile("test", "2", temporaryFolder);

        testFile1Size = (int) testFile1.length();
        testFile2Size = (int) testFile2.length();
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output3 =
                new BufferedOutputStream(new FileOutputStream(testFile1))) {
            TestUtils.generateTestData(output3, testFile1Size);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output2 =
                new BufferedOutputStream(new FileOutputStream(testFile2))) {
            TestUtils.generateTestData(output2, testFile2Size);
        }
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1))) {
            TestUtils.generateTestData(output1, testFile1Size);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2))) {
            TestUtils.generateTestData(output, testFile2Size);
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testNormalize() {
        assertThat(FilenameUtils.normalize(null)).isNull();
        assertThat(FilenameUtils.normalize(":")).isNull();
        assertThat(FilenameUtils.normalize("1:\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("1:")).isNull();
        assertThat(FilenameUtils.normalize("1:a")).isNull();
        assertThat(FilenameUtils.normalize("\\\\\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\a")).isNull();

        assertThat(FilenameUtils.normalize("a\\b/c.txt")).isEqualTo("a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\a\\b/c.txt")).isEqualTo("" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("C:\\a\\b/c.txt")).isEqualTo("C:" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\\\server\\a\\b/c.txt")).isEqualTo("" + SEP + "" + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("~\\a\\b/c.txt")).isEqualTo("~" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("~user\\a\\b/c.txt")).isEqualTo("~user" + SEP + "a" + SEP + "b" + SEP + "c.txt");

        assertThat(FilenameUtils.normalize("a/b/../c")).isEqualTo("a" + SEP + "c");
        assertThat(FilenameUtils.normalize("a/b/../../c")).isEqualTo("c");
        assertThat(FilenameUtils.normalize("a/b/../../c/")).isEqualTo("c" + SEP);
        assertThat(FilenameUtils.normalize("a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalize("a/b/..")).isEqualTo("a" + SEP);
        assertThat(FilenameUtils.normalize("a/b/../")).isEqualTo("a" + SEP);
        assertThat(FilenameUtils.normalize("a/b/../..")).isEqualTo("");
        assertThat(FilenameUtils.normalize("a/b/../../")).isEqualTo("");
        assertThat(FilenameUtils.normalize("a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalize("a/b/../c/../d")).isEqualTo("a" + SEP + "d");
        assertThat(FilenameUtils.normalize("a/b/../c/../d/")).isEqualTo("a" + SEP + "d" + SEP);
        assertThat(FilenameUtils.normalize("a/b//d")).isEqualTo("a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalize("a/b/././.")).isEqualTo("a" + SEP + "b" + SEP);
        assertThat(FilenameUtils.normalize("a/b/./././")).isEqualTo("a" + SEP + "b" + SEP);
        assertThat(FilenameUtils.normalize("./a/")).isEqualTo("a" + SEP);
        assertThat(FilenameUtils.normalize("./a")).isEqualTo("a");
        assertThat(FilenameUtils.normalize("./")).isEqualTo("");
        assertThat(FilenameUtils.normalize(".")).isEqualTo("");
        assertThat(FilenameUtils.normalize("../a")).isNull();
        assertThat(FilenameUtils.normalize("..")).isNull();
        assertThat(FilenameUtils.normalize("")).isEqualTo("");

        assertThat(FilenameUtils.normalize("/a")).isEqualTo(SEP + "a");
        assertThat(FilenameUtils.normalize("/a/")).isEqualTo(SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("/a/b/../c")).isEqualTo(SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalize("/a/b/../../c")).isEqualTo(SEP + "c");
        assertThat(FilenameUtils.normalize("/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalize("/a/b/..")).isEqualTo(SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("/a/b/../..")).isEqualTo(SEP + "");
        assertThat(FilenameUtils.normalize("/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalize("/a/b/../c/../d")).isEqualTo(SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalize("/a/b//d")).isEqualTo(SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalize("/a/b/././.")).isEqualTo(SEP + "a" + SEP + "b" + SEP);
        assertThat(FilenameUtils.normalize("/./a")).isEqualTo(SEP + "a");
        assertThat(FilenameUtils.normalize("/./")).isEqualTo(SEP + "");
        assertThat(FilenameUtils.normalize("/.")).isEqualTo(SEP + "");
        assertThat(FilenameUtils.normalize("/../a")).isNull();
        assertThat(FilenameUtils.normalize("/..")).isNull();
        assertThat(FilenameUtils.normalize("/")).isEqualTo(SEP + "");

        assertThat(FilenameUtils.normalize("~/a")).isEqualTo("~" + SEP + "a");
        assertThat(FilenameUtils.normalize("~/a/")).isEqualTo("~" + SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("~/a/b/../c")).isEqualTo("~" + SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalize("~/a/b/../../c")).isEqualTo("~" + SEP + "c");
        assertThat(FilenameUtils.normalize("~/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalize("~/a/b/..")).isEqualTo("~" + SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("~/a/b/../..")).isEqualTo("~" + SEP + "");
        assertThat(FilenameUtils.normalize("~/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalize("~/a/b/../c/../d")).isEqualTo("~" + SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalize("~/a/b//d")).isEqualTo("~" + SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalize("~/a/b/././.")).isEqualTo("~" + SEP + "a" + SEP + "b" + SEP);
        assertThat(FilenameUtils.normalize("~/./a")).isEqualTo("~" + SEP + "a");
        assertThat(FilenameUtils.normalize("~/./")).isEqualTo("~" + SEP);
        assertThat(FilenameUtils.normalize("~/.")).isEqualTo("~" + SEP);
        assertThat(FilenameUtils.normalize("~/../a")).isNull();
        assertThat(FilenameUtils.normalize("~/..")).isNull();
        assertThat(FilenameUtils.normalize("~/")).isEqualTo("~" + SEP);
        assertThat(FilenameUtils.normalize("~")).isEqualTo("~" + SEP);

        assertThat(FilenameUtils.normalize("~user/a")).isEqualTo("~user" + SEP + "a");
        assertThat(FilenameUtils.normalize("~user/a/")).isEqualTo("~user" + SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("~user/a/b/../c")).isEqualTo("~user" + SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalize("~user/a/b/../../c")).isEqualTo("~user" + SEP + "c");
        assertThat(FilenameUtils.normalize("~user/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalize("~user/a/b/..")).isEqualTo("~user" + SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("~user/a/b/../..")).isEqualTo("~user" + SEP + "");
        assertThat(FilenameUtils.normalize("~user/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalize("~user/a/b/../c/../d")).isEqualTo("~user" + SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalize("~user/a/b//d")).isEqualTo("~user" + SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalize("~user/a/b/././.")).isEqualTo("~user" + SEP + "a" + SEP + "b" + SEP);
        assertThat(FilenameUtils.normalize("~user/./a")).isEqualTo("~user" + SEP + "a");
        assertThat(FilenameUtils.normalize("~user/./")).isEqualTo("~user" + SEP + "");
        assertThat(FilenameUtils.normalize("~user/.")).isEqualTo("~user" + SEP + "");
        assertThat(FilenameUtils.normalize("~user/../a")).isNull();
        assertThat(FilenameUtils.normalize("~user/..")).isNull();
        assertThat(FilenameUtils.normalize("~user/")).isEqualTo("~user" + SEP);
        assertThat(FilenameUtils.normalize("~user")).isEqualTo("~user" + SEP);

        assertThat(FilenameUtils.normalize("C:/a")).isEqualTo("C:" + SEP + "a");
        assertThat(FilenameUtils.normalize("C:/a/")).isEqualTo("C:" + SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("C:/a/b/../c")).isEqualTo("C:" + SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalize("C:/a/b/../../c")).isEqualTo("C:" + SEP + "c");
        assertThat(FilenameUtils.normalize("C:/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalize("C:/a/b/..")).isEqualTo("C:" + SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("C:/a/b/../..")).isEqualTo("C:" + SEP + "");
        assertThat(FilenameUtils.normalize("C:/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalize("C:/a/b/../c/../d")).isEqualTo("C:" + SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalize("C:/a/b//d")).isEqualTo("C:" + SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalize("C:/a/b/././.")).isEqualTo("C:" + SEP + "a" + SEP + "b" + SEP);
        assertThat(FilenameUtils.normalize("C:/./a")).isEqualTo("C:" + SEP + "a");
        assertThat(FilenameUtils.normalize("C:/./")).isEqualTo("C:" + SEP + "");
        assertThat(FilenameUtils.normalize("C:/.")).isEqualTo("C:" + SEP + "");
        assertThat(FilenameUtils.normalize("C:/../a")).isNull();
        assertThat(FilenameUtils.normalize("C:/..")).isNull();
        assertThat(FilenameUtils.normalize("C:/")).isEqualTo("C:" + SEP + "");

        assertThat(FilenameUtils.normalize("C:a")).isEqualTo("C:" + "a");
        assertThat(FilenameUtils.normalize("C:a/")).isEqualTo("C:" + "a" + SEP);
        assertThat(FilenameUtils.normalize("C:a/b/../c")).isEqualTo("C:" + "a" + SEP + "c");
        assertThat(FilenameUtils.normalize("C:a/b/../../c")).isEqualTo("C:" + "c");
        assertThat(FilenameUtils.normalize("C:a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalize("C:a/b/..")).isEqualTo("C:" + "a" + SEP);
        assertThat(FilenameUtils.normalize("C:a/b/../..")).isEqualTo("C:" + "");
        assertThat(FilenameUtils.normalize("C:a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalize("C:a/b/../c/../d")).isEqualTo("C:" + "a" + SEP + "d");
        assertThat(FilenameUtils.normalize("C:a/b//d")).isEqualTo("C:" + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalize("C:a/b/././.")).isEqualTo("C:" + "a" + SEP + "b" + SEP);
        assertThat(FilenameUtils.normalize("C:./a")).isEqualTo("C:" + "a");
        assertThat(FilenameUtils.normalize("C:./")).isEqualTo("C:" + "");
        assertThat(FilenameUtils.normalize("C:.")).isEqualTo("C:" + "");
        assertThat(FilenameUtils.normalize("C:../a")).isNull();
        assertThat(FilenameUtils.normalize("C:..")).isNull();
        assertThat(FilenameUtils.normalize("C:")).isEqualTo("C:" + "");

        assertThat(FilenameUtils.normalize("//server/a")).isEqualTo(SEP + SEP + "server" + SEP + "a");
        assertThat(FilenameUtils.normalize("//server/a/")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("//server/a/b/../c")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalize("//server/a/b/../../c")).isEqualTo(SEP + SEP + "server" + SEP + "c");
        assertThat(FilenameUtils.normalize("//server/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalize("//server/a/b/..")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP);
        assertThat(FilenameUtils.normalize("//server/a/b/../..")).isEqualTo(SEP + SEP + "server" + SEP + "");
        assertThat(FilenameUtils.normalize("//server/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalize("//server/a/b/../c/../d")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalize("//server/a/b//d")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalize("//server/a/b/././.")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP);
        assertThat(FilenameUtils.normalize("//server/./a")).isEqualTo(SEP + SEP + "server" + SEP + "a");
        assertThat(FilenameUtils.normalize("//server/./")).isEqualTo(SEP + SEP + "server" + SEP + "");
        assertThat(FilenameUtils.normalize("//server/.")).isEqualTo(SEP + SEP + "server" + SEP + "");
        assertThat(FilenameUtils.normalize("//server/../a")).isNull();
        assertThat(FilenameUtils.normalize("//server/..")).isNull();
        assertThat(FilenameUtils.normalize("//server/")).isEqualTo(SEP + SEP + "server" + SEP + "");

        assertThat(FilenameUtils.normalize("\\\\127.0.0.1\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "127.0.0.1" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\\\::1\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "::1" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\\\1::\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "1::" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\\\server.example.org\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "server.example.org" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\\\server.sub.example.org\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "server.sub.example.org" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\\\server.\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "server." + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\\\1::127.0.0.1\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "1::127.0.0.1" + SEP + "a" + SEP + "b" + SEP + "c.txt");

        // not valid IPv4 addresses but technically a valid "reg-name"s according to RFC1034
        assertThat(FilenameUtils.normalize("\\\\127.0.0.256\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "127.0.0.256" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalize("\\\\127.0.0.01\\a\\b\\c.txt")).isEqualTo(SEP + SEP + "127.0.0.01" + SEP + "a" + SEP + "b" + SEP + "c.txt");

        assertThat(FilenameUtils.normalize("\\\\-server\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\.\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\..\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\127.0..1\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\::1::2\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\:1\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\1:\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\1:2:3:4:5:6:7:8:9\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\g:2:3:4:5:6:7:8\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\1ffff:2:3:4:5:6:7:8\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalize("\\\\1:2\\a\\b\\c.txt")).isNull();
        // IO-556
        assertThat(FilenameUtils.normalize("//../foo")).isNull();
        assertThat(FilenameUtils.normalize("\\\\..\\foo")).isNull();
    }

    /**
     */
    @Test
    public void testNormalize_with_nullbytes() {
        try {
            assertThat(FilenameUtils.normalize("a\\b/c\u0000.txt")).isEqualTo("a" + SEP + "b" + SEP + "c.txt");
        } catch (final IllegalArgumentException ignore) {
        }

        try {
            assertThat(FilenameUtils.normalize("\u0000a\\b/c.txt")).isEqualTo("a" + SEP + "b" + SEP + "c.txt");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testNormalizeUnixWin() {

        // Normalize (Unix Separator)
        assertThat(FilenameUtils.normalize("/a/b/../c/", true)).isEqualTo("/a/c/");
        assertThat(FilenameUtils.normalize("\\a\\b\\..\\c\\", true)).isEqualTo("/a/c/");

        // Normalize (Windows Separator)
        assertThat(FilenameUtils.normalize("/a/b/../c/", false)).isEqualTo("\\a\\c\\");
        assertThat(FilenameUtils.normalize("\\a\\b\\..\\c\\", false)).isEqualTo("\\a\\c\\");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testNormalizeNoEndSeparator() {
        assertThat(FilenameUtils.normalizeNoEndSeparator(null)).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator(":")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("1:\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("1:")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("1:a")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("\\\\\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("\\\\a")).isNull();

        assertThat(FilenameUtils.normalizeNoEndSeparator("a\\b/c.txt")).isEqualTo("a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalizeNoEndSeparator("\\a\\b/c.txt")).isEqualTo("" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:\\a\\b/c.txt")).isEqualTo("C:" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalizeNoEndSeparator("\\\\server\\a\\b/c.txt")).isEqualTo("" + SEP + "" + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~\\a\\b/c.txt")).isEqualTo("~" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user\\a\\b/c.txt")).isEqualTo("~user" + SEP + "a" + SEP + "b" + SEP + "c.txt");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:\\\\a\\\\b\\\\c.txt")).isEqualTo("C:" + SEP + "a" + SEP + "b" + SEP + "c.txt");

        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../c")).isEqualTo("a" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../../c")).isEqualTo("c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../../c/")).isEqualTo("c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/..")).isEqualTo("a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../")).isEqualTo("a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../..")).isEqualTo("");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../../")).isEqualTo("");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../c/../d")).isEqualTo("a" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/../c/../d/")).isEqualTo("a" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b//d")).isEqualTo("a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/././.")).isEqualTo("a" + SEP + "b");
        assertThat(FilenameUtils.normalizeNoEndSeparator("a/b/./././")).isEqualTo("a" + SEP + "b");
        assertThat(FilenameUtils.normalizeNoEndSeparator("./a/")).isEqualTo("a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("./a")).isEqualTo("a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("./")).isEqualTo("");
        assertThat(FilenameUtils.normalizeNoEndSeparator(".")).isEqualTo("");
        assertThat(FilenameUtils.normalizeNoEndSeparator("../a")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("")).isEqualTo("");

        assertThat(FilenameUtils.normalizeNoEndSeparator("/a")).isEqualTo(SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/")).isEqualTo(SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/../c")).isEqualTo(SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/../../c")).isEqualTo(SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/..")).isEqualTo(SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/../..")).isEqualTo(SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/../c/../d")).isEqualTo(SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b//d")).isEqualTo(SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/././.")).isEqualTo(SEP + "a" + SEP + "b");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/./a")).isEqualTo(SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/./")).isEqualTo(SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/.")).isEqualTo(SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("/../a")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("/..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("/")).isEqualTo(SEP + "");

        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a")).isEqualTo("~" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/")).isEqualTo("~" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b/../c")).isEqualTo("~" + SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b/../../c")).isEqualTo("~" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b/..")).isEqualTo("~" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b/../..")).isEqualTo("~" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b/../c/../d")).isEqualTo("~" + SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b//d")).isEqualTo("~" + SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/a/b/././.")).isEqualTo("~" + SEP + "a" + SEP + "b");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/./a")).isEqualTo("~" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/./")).isEqualTo("~" + SEP);
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/.")).isEqualTo("~" + SEP);
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/../a")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("~/")).isEqualTo("~" + SEP);
        assertThat(FilenameUtils.normalizeNoEndSeparator("~")).isEqualTo("~" + SEP);

        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a")).isEqualTo("~user" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/")).isEqualTo("~user" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b/../c")).isEqualTo("~user" + SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b/../../c")).isEqualTo("~user" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b/..")).isEqualTo("~user" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b/../..")).isEqualTo("~user" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b/../c/../d")).isEqualTo("~user" + SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b//d")).isEqualTo("~user" + SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/a/b/././.")).isEqualTo("~user" + SEP + "a" + SEP + "b");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/./a")).isEqualTo("~user" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/./")).isEqualTo("~user" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/.")).isEqualTo("~user" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/../a")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user/")).isEqualTo("~user" + SEP);
        assertThat(FilenameUtils.normalizeNoEndSeparator("~user")).isEqualTo("~user" + SEP);

        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a")).isEqualTo("C:" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/")).isEqualTo("C:" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b/../c")).isEqualTo("C:" + SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b/../../c")).isEqualTo("C:" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b/..")).isEqualTo("C:" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b/../..")).isEqualTo("C:" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b/../c/../d")).isEqualTo("C:" + SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b//d")).isEqualTo("C:" + SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/a/b/././.")).isEqualTo("C:" + SEP + "a" + SEP + "b");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/./a")).isEqualTo("C:" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/./")).isEqualTo("C:" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/.")).isEqualTo("C:" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/../a")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:/")).isEqualTo("C:" + SEP + "");

        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a")).isEqualTo("C:" + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/")).isEqualTo("C:" + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b/../c")).isEqualTo("C:" + "a" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b/../../c")).isEqualTo("C:" + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b/..")).isEqualTo("C:" + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b/../..")).isEqualTo("C:" + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b/../c/../d")).isEqualTo("C:" + "a" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b//d")).isEqualTo("C:" + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:a/b/././.")).isEqualTo("C:" + "a" + SEP + "b");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:./a")).isEqualTo("C:" + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:./")).isEqualTo("C:" + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:.")).isEqualTo("C:" + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:../a")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("C:")).isEqualTo("C:" + "");

        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a")).isEqualTo(SEP + SEP + "server" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/")).isEqualTo(SEP + SEP + "server" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b/../c")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b/../../c")).isEqualTo(SEP + SEP + "server" + SEP + "c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b/../../../c")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b/..")).isEqualTo(SEP + SEP + "server" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b/../..")).isEqualTo(SEP + SEP + "server" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b/../../..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b/../c/../d")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b//d")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "d");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/a/b/././.")).isEqualTo(SEP + SEP + "server" + SEP + "a" + SEP + "b");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/./a")).isEqualTo(SEP + SEP + "server" + SEP + "a");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/./")).isEqualTo(SEP + SEP + "server" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/.")).isEqualTo(SEP + SEP + "server" + SEP + "");
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/../a")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/..")).isNull();
        assertThat(FilenameUtils.normalizeNoEndSeparator("//server/")).isEqualTo(SEP + SEP + "server" + SEP + "");
    }

    @Test
    public void testNormalizeNoEndSeparatorUnixWin() {

        // Normalize (Unix Separator)
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/../c/", true)).isEqualTo("/a/c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("\\a\\b\\..\\c\\", true)).isEqualTo("/a/c");

        // Normalize (Windows Separator)
        assertThat(FilenameUtils.normalizeNoEndSeparator("/a/b/../c/", false)).isEqualTo("\\a\\c");
        assertThat(FilenameUtils.normalizeNoEndSeparator("\\a\\b\\..\\c\\", false)).isEqualTo("\\a\\c");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testConcat() {
        assertThat(FilenameUtils.concat("", null)).isNull();
        assertThat(FilenameUtils.concat(null, null)).isNull();
        assertThat(FilenameUtils.concat(null, "")).isNull();
        assertThat(FilenameUtils.concat(null, "a")).isNull();
        assertThat(FilenameUtils.concat(null, "/a")).isEqualTo(SEP + "a");

        assertThat(FilenameUtils.concat("", ":")).isNull(); // invalid prefix
        assertThat(FilenameUtils.concat(":", "")).isNull(); // invalid prefix

        assertThat(FilenameUtils.concat("", "f/")).isEqualTo("f" + SEP);
        assertThat(FilenameUtils.concat("", "f")).isEqualTo("f");
        assertThat(FilenameUtils.concat("a/", "f/")).isEqualTo("a" + SEP + "f" + SEP);
        assertThat(FilenameUtils.concat("a", "f")).isEqualTo("a" + SEP + "f");
        assertThat(FilenameUtils.concat("a/b/", "f/")).isEqualTo("a" + SEP + "b" + SEP + "f" + SEP);
        assertThat(FilenameUtils.concat("a/b", "f")).isEqualTo("a" + SEP + "b" + SEP + "f");

        assertThat(FilenameUtils.concat("a/b/", "../f/")).isEqualTo("a" + SEP + "f" + SEP);
        assertThat(FilenameUtils.concat("a/b", "../f")).isEqualTo("a" + SEP + "f");
        assertThat(FilenameUtils.concat("a/b/../c/", "f/../g/")).isEqualTo("a" + SEP + "c" + SEP + "g" + SEP);
        assertThat(FilenameUtils.concat("a/b/../c", "f/../g")).isEqualTo("a" + SEP + "c" + SEP + "g");

        assertThat(FilenameUtils.concat("a/c.txt", "f")).isEqualTo("a" + SEP + "c.txt" + SEP + "f");

        assertThat(FilenameUtils.concat("", "/f/")).isEqualTo(SEP + "f" + SEP);
        assertThat(FilenameUtils.concat("", "/f")).isEqualTo(SEP + "f");
        assertThat(FilenameUtils.concat("a/", "/f/")).isEqualTo(SEP + "f" + SEP);
        assertThat(FilenameUtils.concat("a", "/f")).isEqualTo(SEP + "f");

        assertThat(FilenameUtils.concat("a/b/", "/c/d")).isEqualTo(SEP + "c" + SEP + "d");
        assertThat(FilenameUtils.concat("a/b/", "C:c/d")).isEqualTo("C:c" + SEP + "d");
        assertThat(FilenameUtils.concat("a/b/", "C:/c/d")).isEqualTo("C:" + SEP + "c" + SEP + "d");
        assertThat(FilenameUtils.concat("a/b/", "~/c/d")).isEqualTo("~" + SEP + "c" + SEP + "d");
        assertThat(FilenameUtils.concat("a/b/", "~user/c/d")).isEqualTo("~user" + SEP + "c" + SEP + "d");
        assertThat(FilenameUtils.concat("a/b/", "~")).isEqualTo("~" + SEP);
        assertThat(FilenameUtils.concat("a/b/", "~user")).isEqualTo("~user" + SEP);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testSeparatorsToUnix() {
        assertThat(FilenameUtils.separatorsToUnix(null)).isNull();
        assertThat(FilenameUtils.separatorsToUnix("/a/b/c")).isEqualTo("/a/b/c");
        assertThat(FilenameUtils.separatorsToUnix("/a/b/c.txt")).isEqualTo("/a/b/c.txt");
        assertThat(FilenameUtils.separatorsToUnix("/a/b\\c")).isEqualTo("/a/b/c");
        assertThat(FilenameUtils.separatorsToUnix("\\a\\b\\c")).isEqualTo("/a/b/c");
        assertThat(FilenameUtils.separatorsToUnix("D:\\a\\b\\c")).isEqualTo("D:/a/b/c");
    }

    @Test
    public void testSeparatorsToWindows() {
        assertThat(FilenameUtils.separatorsToWindows(null)).isNull();
        assertThat(FilenameUtils.separatorsToWindows("\\a\\b\\c")).isEqualTo("\\a\\b\\c");
        assertThat(FilenameUtils.separatorsToWindows("\\a\\b\\c.txt")).isEqualTo("\\a\\b\\c.txt");
        assertThat(FilenameUtils.separatorsToWindows("\\a\\b/c")).isEqualTo("\\a\\b\\c");
        assertThat(FilenameUtils.separatorsToWindows("/a/b/c")).isEqualTo("\\a\\b\\c");
        assertThat(FilenameUtils.separatorsToWindows("D:/a/b/c")).isEqualTo("D:\\a\\b\\c");
    }

    @Test
    public void testSeparatorsToSystem() {
        if (WINDOWS) {
            assertThat(FilenameUtils.separatorsToSystem(null)).isNull();
            assertThat(FilenameUtils.separatorsToSystem("\\a\\b\\c")).isEqualTo("\\a\\b\\c");
            assertThat(FilenameUtils.separatorsToSystem("\\a\\b\\c.txt")).isEqualTo("\\a\\b\\c.txt");
            assertThat(FilenameUtils.separatorsToSystem("\\a\\b/c")).isEqualTo("\\a\\b\\c");
            assertThat(FilenameUtils.separatorsToSystem("/a/b/c")).isEqualTo("\\a\\b\\c");
            assertThat(FilenameUtils.separatorsToSystem("D:/a/b/c")).isEqualTo("D:\\a\\b\\c");
        } else {
            assertThat(FilenameUtils.separatorsToSystem(null)).isNull();
            assertThat(FilenameUtils.separatorsToSystem("/a/b/c")).isEqualTo("/a/b/c");
            assertThat(FilenameUtils.separatorsToSystem("/a/b/c.txt")).isEqualTo("/a/b/c.txt");
            assertThat(FilenameUtils.separatorsToSystem("/a/b\\c")).isEqualTo("/a/b/c");
            assertThat(FilenameUtils.separatorsToSystem("\\a\\b\\c")).isEqualTo("/a/b/c");
            assertThat(FilenameUtils.separatorsToSystem("D:\\a\\b\\c")).isEqualTo("D:/a/b/c");
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void testGetPrefixLength() {
        assertThat(FilenameUtils.getPrefixLength(null)).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength(":")).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength("1:\\a\\b\\c.txt")).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength("1:")).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength("1:a")).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength("\\\\\\a\\b\\c.txt")).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength("\\\\a")).isEqualTo(-1);

        assertThat(FilenameUtils.getPrefixLength("")).isEqualTo(0);
        assertThat(FilenameUtils.getPrefixLength("\\")).isEqualTo(1);

        if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(FilenameUtils.getPrefixLength("C:")).isEqualTo(2);
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(FilenameUtils.getPrefixLength("C:")).isEqualTo(0);
        }

        assertThat(FilenameUtils.getPrefixLength("C:\\")).isEqualTo(3);
        assertThat(FilenameUtils.getPrefixLength("//server/")).isEqualTo(9);
        assertThat(FilenameUtils.getPrefixLength("~")).isEqualTo(2);
        assertThat(FilenameUtils.getPrefixLength("~/")).isEqualTo(2);
        assertThat(FilenameUtils.getPrefixLength("~user")).isEqualTo(6);
        assertThat(FilenameUtils.getPrefixLength("~user/")).isEqualTo(6);

        assertThat(FilenameUtils.getPrefixLength("a\\b\\c.txt")).isEqualTo(0);
        assertThat(FilenameUtils.getPrefixLength("\\a\\b\\c.txt")).isEqualTo(1);
        assertThat(FilenameUtils.getPrefixLength("C:a\\b\\c.txt")).isEqualTo(2);
        assertThat(FilenameUtils.getPrefixLength("C:\\a\\b\\c.txt")).isEqualTo(3);
        assertThat(FilenameUtils.getPrefixLength("\\\\server\\a\\b\\c.txt")).isEqualTo(9);

        assertThat(FilenameUtils.getPrefixLength("a/b/c.txt")).isEqualTo(0);
        assertThat(FilenameUtils.getPrefixLength("/a/b/c.txt")).isEqualTo(1);
        assertThat(FilenameUtils.getPrefixLength("C:/a/b/c.txt")).isEqualTo(3);
        assertThat(FilenameUtils.getPrefixLength("//server/a/b/c.txt")).isEqualTo(9);
        assertThat(FilenameUtils.getPrefixLength("~/a/b/c.txt")).isEqualTo(2);
        assertThat(FilenameUtils.getPrefixLength("~user/a/b/c.txt")).isEqualTo(6);

        assertThat(FilenameUtils.getPrefixLength("a\\b\\c.txt")).isEqualTo(0);
        assertThat(FilenameUtils.getPrefixLength("\\a\\b\\c.txt")).isEqualTo(1);
        assertThat(FilenameUtils.getPrefixLength("~\\a\\b\\c.txt")).isEqualTo(2);
        assertThat(FilenameUtils.getPrefixLength("~user\\a\\b\\c.txt")).isEqualTo(6);

        assertThat(FilenameUtils.getPrefixLength("//server/a/b/c.txt")).isEqualTo(9);
        assertThat(FilenameUtils.getPrefixLength("\\\\\\a\\b\\c.txt")).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength("///a/b/c.txt")).isEqualTo(-1);

        assertThat(FilenameUtils.getPrefixLength("/:foo")).isEqualTo(1);
        assertThat(FilenameUtils.getPrefixLength("/:/")).isEqualTo(1);
        assertThat(FilenameUtils.getPrefixLength("/:::::::.txt")).isEqualTo(1);

        assertThat(FilenameUtils.getPrefixLength("\\\\127.0.0.1\\a\\b\\c.txt")).isEqualTo(12);
        assertThat(FilenameUtils.getPrefixLength("\\\\::1\\a\\b\\c.txt")).isEqualTo(6);
        assertThat(FilenameUtils.getPrefixLength("\\\\server.example.org\\a\\b\\c.txt")).isEqualTo(21);
        assertThat(FilenameUtils.getPrefixLength("\\\\server.\\a\\b\\c.txt")).isEqualTo(10);

        assertThat(FilenameUtils.getPrefixLength("\\\\-server\\a\\b\\c.txt")).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength("\\\\.\\a\\b\\c.txt")).isEqualTo(-1);
        assertThat(FilenameUtils.getPrefixLength("\\\\..\\a\\b\\c.txt")).isEqualTo(-1);
    }

    @Test
    public void testIndexOfLastSeparator() {
        assertThat(FilenameUtils.indexOfLastSeparator(null)).isEqualTo(-1);
        assertThat(FilenameUtils.indexOfLastSeparator("noseperator.inthispath")).isEqualTo(-1);
        assertThat(FilenameUtils.indexOfLastSeparator("a/b/c")).isEqualTo(3);
        assertThat(FilenameUtils.indexOfLastSeparator("a\\b\\c")).isEqualTo(3);
    }

    @Test
    public void testIndexOfExtension() {
        assertThat(FilenameUtils.indexOfExtension(null)).isEqualTo(-1);
        assertThat(FilenameUtils.indexOfExtension("file")).isEqualTo(-1);
        assertThat(FilenameUtils.indexOfExtension("file.txt")).isEqualTo(4);
        assertThat(FilenameUtils.indexOfExtension("a.txt/b.txt/c.txt")).isEqualTo(13);
        assertThat(FilenameUtils.indexOfExtension("a/b/c")).isEqualTo(-1);
        assertThat(FilenameUtils.indexOfExtension("a\\b\\c")).isEqualTo(-1);
        assertThat(FilenameUtils.indexOfExtension("a/b.notextension/c")).isEqualTo(-1);
        assertThat(FilenameUtils.indexOfExtension("a\\b.notextension\\c")).isEqualTo(-1);

        if (FilenameUtils.isSystemWindows()) {
            // Special case handling for NTFS ADS names
        	try {
        		FilenameUtils.indexOfExtension("foo.exe:bar.txt");
        		throw new AssertionError("Expected Exception");
        	} catch (final IllegalArgumentException e) {
             assertThat(e.getMessage()).isEqualTo("NTFS ADS separator (':') in file name is forbidden.");
        	}
        } else {
            // Upwards compatibility on other systems
        	   assertThat(FilenameUtils.indexOfExtension("foo.exe:bar.txt")).isEqualTo(11);
        }

    }

    //-----------------------------------------------------------------------
    @Test
    public void testGetPrefix() {
        assertThat(FilenameUtils.getPrefix(null)).isNull();
        assertThat(FilenameUtils.getPrefix(":")).isNull();
        assertThat(FilenameUtils.getPrefix("1:\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.getPrefix("1:")).isNull();
        assertThat(FilenameUtils.getPrefix("1:a")).isNull();
        assertThat(FilenameUtils.getPrefix("\\\\\\a\\b\\c.txt")).isNull();
        assertThat(FilenameUtils.getPrefix("\\\\a")).isNull();

        assertThat(FilenameUtils.getPrefix("")).isEqualTo("");
        assertThat(FilenameUtils.getPrefix("\\")).isEqualTo("\\");

        if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(FilenameUtils.getPrefix("C:")).isEqualTo("C:");
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(FilenameUtils.getPrefix("C:")).isEqualTo("");
        }

        assertThat(FilenameUtils.getPrefix("C:\\")).isEqualTo("C:\\");
        assertThat(FilenameUtils.getPrefix("//server/")).isEqualTo("//server/");
        assertThat(FilenameUtils.getPrefix("~")).isEqualTo("~/");
        assertThat(FilenameUtils.getPrefix("~/")).isEqualTo("~/");
        assertThat(FilenameUtils.getPrefix("~user")).isEqualTo("~user/");
        assertThat(FilenameUtils.getPrefix("~user/")).isEqualTo("~user/");

        assertThat(FilenameUtils.getPrefix("a\\b\\c.txt")).isEqualTo("");
        assertThat(FilenameUtils.getPrefix("\\a\\b\\c.txt")).isEqualTo("\\");
        assertThat(FilenameUtils.getPrefix("C:\\a\\b\\c.txt")).isEqualTo("C:\\");
        assertThat(FilenameUtils.getPrefix("\\\\server\\a\\b\\c.txt")).isEqualTo("\\\\server\\");

        assertThat(FilenameUtils.getPrefix("a/b/c.txt")).isEqualTo("");
        assertThat(FilenameUtils.getPrefix("/a/b/c.txt")).isEqualTo("/");
        assertThat(FilenameUtils.getPrefix("C:/a/b/c.txt")).isEqualTo("C:/");
        assertThat(FilenameUtils.getPrefix("//server/a/b/c.txt")).isEqualTo("//server/");
        assertThat(FilenameUtils.getPrefix("~/a/b/c.txt")).isEqualTo("~/");
        assertThat(FilenameUtils.getPrefix("~user/a/b/c.txt")).isEqualTo("~user/");

        assertThat(FilenameUtils.getPrefix("a\\b\\c.txt")).isEqualTo("");
        assertThat(FilenameUtils.getPrefix("\\a\\b\\c.txt")).isEqualTo("\\");
        assertThat(FilenameUtils.getPrefix("~\\a\\b\\c.txt")).isEqualTo("~\\");
        assertThat(FilenameUtils.getPrefix("~user\\a\\b\\c.txt")).isEqualTo("~user\\");
    }

    @Test
    public void testGetPrefix_with_nullbyte() {
        try {
            assertThat(FilenameUtils.getPrefix("~u\u0000ser\\a\\b\\c.txt")).isEqualTo("~user\\");
        } catch (final IllegalArgumentException ignore) {

        }
    }

    @Test
    public void testGetPath() {
        assertThat(FilenameUtils.getPath(null)).isNull();
        assertThat(FilenameUtils.getPath("noseperator.inthispath")).isEqualTo("");
        assertThat(FilenameUtils.getPath("/noseperator.inthispath")).isEqualTo("");
        assertThat(FilenameUtils.getPath("\\noseperator.inthispath")).isEqualTo("");
        assertThat(FilenameUtils.getPath("a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getPath("a/b/c")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getPath("a/b/c/")).isEqualTo("a/b/c/");
        assertThat(FilenameUtils.getPath("a\\b\\c")).isEqualTo("a\\b\\");

        assertThat(FilenameUtils.getPath(":")).isNull();
        assertThat(FilenameUtils.getPath("1:/a/b/c.txt")).isNull();
        assertThat(FilenameUtils.getPath("1:")).isNull();
        assertThat(FilenameUtils.getPath("1:a")).isNull();
        assertThat(FilenameUtils.getPath("///a/b/c.txt")).isNull();
        assertThat(FilenameUtils.getPath("//a")).isNull();

        assertThat(FilenameUtils.getPath("")).isEqualTo("");
        assertThat(FilenameUtils.getPath("C:")).isEqualTo("");
        assertThat(FilenameUtils.getPath("C:/")).isEqualTo("");
        assertThat(FilenameUtils.getPath("//server/")).isEqualTo("");
        assertThat(FilenameUtils.getPath("~")).isEqualTo("");
        assertThat(FilenameUtils.getPath("~/")).isEqualTo("");
        assertThat(FilenameUtils.getPath("~user")).isEqualTo("");
        assertThat(FilenameUtils.getPath("~user/")).isEqualTo("");

        assertThat(FilenameUtils.getPath("a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getPath("/a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getPath("C:a")).isEqualTo("");
        assertThat(FilenameUtils.getPath("C:a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getPath("C:/a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getPath("//server/a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getPath("~/a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getPath("~user/a/b/c.txt")).isEqualTo("a/b/");
    }

    @Test
    public void testGetPath_with_nullbyte() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.getPath("~user/a/\u0000b/c.txt"));
    }


    @Test
    public void testGetPathNoEndSeparator() {
        assertThat(FilenameUtils.getPath(null)).isNull();
        assertThat(FilenameUtils.getPath("noseperator.inthispath")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("/noseperator.inthispath")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("\\noseperator.inthispath")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getPathNoEndSeparator("a/b/c")).isEqualTo("a/b");
        assertThat(FilenameUtils.getPathNoEndSeparator("a/b/c/")).isEqualTo("a/b/c");
        assertThat(FilenameUtils.getPathNoEndSeparator("a\\b\\c")).isEqualTo("a\\b");

        assertThat(FilenameUtils.getPathNoEndSeparator(":")).isNull();
        assertThat(FilenameUtils.getPathNoEndSeparator("1:/a/b/c.txt")).isNull();
        assertThat(FilenameUtils.getPathNoEndSeparator("1:")).isNull();
        assertThat(FilenameUtils.getPathNoEndSeparator("1:a")).isNull();
        assertThat(FilenameUtils.getPathNoEndSeparator("///a/b/c.txt")).isNull();
        assertThat(FilenameUtils.getPathNoEndSeparator("//a")).isNull();

        assertThat(FilenameUtils.getPathNoEndSeparator("")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("C:")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("C:/")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("//server/")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("~")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("~/")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("~user")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("~user/")).isEqualTo("");

        assertThat(FilenameUtils.getPathNoEndSeparator("a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getPathNoEndSeparator("/a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getPathNoEndSeparator("C:a")).isEqualTo("");
        assertThat(FilenameUtils.getPathNoEndSeparator("C:a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getPathNoEndSeparator("C:/a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getPathNoEndSeparator("//server/a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getPathNoEndSeparator("~/a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getPathNoEndSeparator("~user/a/b/c.txt")).isEqualTo("a/b");
    }

    @Test
    public void testGetPathNoEndSeparator_with_null_byte() {
        try {
            assertThat(FilenameUtils.getPathNoEndSeparator("~user/a\u0000/b/c.txt")).isEqualTo("a/b");
        } catch (final IllegalArgumentException ignore) {

        }
    }

    @Test
    public void testGetFullPath() {
        assertThat(FilenameUtils.getFullPath(null)).isNull();
        assertThat(FilenameUtils.getFullPath("noseperator.inthispath")).isEqualTo("");
        assertThat(FilenameUtils.getFullPath("a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getFullPath("a/b/c")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getFullPath("a/b/c/")).isEqualTo("a/b/c/");
        assertThat(FilenameUtils.getFullPath("a\\b\\c")).isEqualTo("a\\b\\");

        assertThat(FilenameUtils.getFullPath(":")).isNull();
        assertThat(FilenameUtils.getFullPath("1:/a/b/c.txt")).isNull();
        assertThat(FilenameUtils.getFullPath("1:")).isNull();
        assertThat(FilenameUtils.getFullPath("1:a")).isNull();
        assertThat(FilenameUtils.getFullPath("///a/b/c.txt")).isNull();
        assertThat(FilenameUtils.getFullPath("//a")).isNull();

        assertThat(FilenameUtils.getFullPath("")).isEqualTo("");

        if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(FilenameUtils.getFullPath("C:")).isEqualTo("C:");
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(FilenameUtils.getFullPath("C:")).isEqualTo("");
        }

        assertThat(FilenameUtils.getFullPath("C:/")).isEqualTo("C:/");
        assertThat(FilenameUtils.getFullPath("//server/")).isEqualTo("//server/");
        assertThat(FilenameUtils.getFullPath("~")).isEqualTo("~/");
        assertThat(FilenameUtils.getFullPath("~/")).isEqualTo("~/");
        assertThat(FilenameUtils.getFullPath("~user")).isEqualTo("~user/");
        assertThat(FilenameUtils.getFullPath("~user/")).isEqualTo("~user/");

        assertThat(FilenameUtils.getFullPath("a/b/c.txt")).isEqualTo("a/b/");
        assertThat(FilenameUtils.getFullPath("/a/b/c.txt")).isEqualTo("/a/b/");
        assertThat(FilenameUtils.getFullPath("C:a")).isEqualTo("C:");
        assertThat(FilenameUtils.getFullPath("C:a/b/c.txt")).isEqualTo("C:a/b/");
        assertThat(FilenameUtils.getFullPath("C:/a/b/c.txt")).isEqualTo("C:/a/b/");
        assertThat(FilenameUtils.getFullPath("//server/a/b/c.txt")).isEqualTo("//server/a/b/");
        assertThat(FilenameUtils.getFullPath("~/a/b/c.txt")).isEqualTo("~/a/b/");
        assertThat(FilenameUtils.getFullPath("~user/a/b/c.txt")).isEqualTo("~user/a/b/");
    }

    @Test
    public void testGetFullPathNoEndSeparator() {
        assertThat(FilenameUtils.getFullPathNoEndSeparator(null)).isNull();
        assertThat(FilenameUtils.getFullPathNoEndSeparator("noseperator.inthispath")).isEqualTo("");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("a/b/c")).isEqualTo("a/b");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("a/b/c/")).isEqualTo("a/b/c");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("a\\b\\c")).isEqualTo("a\\b");

        assertThat(FilenameUtils.getFullPathNoEndSeparator(":")).isNull();
        assertThat(FilenameUtils.getFullPathNoEndSeparator("1:/a/b/c.txt")).isNull();
        assertThat(FilenameUtils.getFullPathNoEndSeparator("1:")).isNull();
        assertThat(FilenameUtils.getFullPathNoEndSeparator("1:a")).isNull();
        assertThat(FilenameUtils.getFullPathNoEndSeparator("///a/b/c.txt")).isNull();
        assertThat(FilenameUtils.getFullPathNoEndSeparator("//a")).isNull();

        assertThat(FilenameUtils.getFullPathNoEndSeparator("")).isEqualTo("");

        if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(FilenameUtils.getFullPathNoEndSeparator("C:")).isEqualTo("C:");
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(FilenameUtils.getFullPathNoEndSeparator("C:")).isEqualTo("");
        }

        assertThat(FilenameUtils.getFullPathNoEndSeparator("C:/")).isEqualTo("C:/");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("//server/")).isEqualTo("//server/");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("~")).isEqualTo("~");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("~/")).isEqualTo("~/");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("~user")).isEqualTo("~user");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("~user/")).isEqualTo("~user/");

        assertThat(FilenameUtils.getFullPathNoEndSeparator("a/b/c.txt")).isEqualTo("a/b");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("/a/b/c.txt")).isEqualTo("/a/b");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("C:a")).isEqualTo("C:");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("C:a/b/c.txt")).isEqualTo("C:a/b");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("C:/a/b/c.txt")).isEqualTo("C:/a/b");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("//server/a/b/c.txt")).isEqualTo("//server/a/b");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("~/a/b/c.txt")).isEqualTo("~/a/b");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("~user/a/b/c.txt")).isEqualTo("~user/a/b");
    }

    /**
     * Test for https://issues.apache.org/jira/browse/IO-248
     */
    @Test
    public void testGetFullPathNoEndSeparator_IO_248() {

        // Test single separator
        assertThat(FilenameUtils.getFullPathNoEndSeparator("/")).isEqualTo("/");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("\\")).isEqualTo("\\");

        // Test one level directory
        assertThat(FilenameUtils.getFullPathNoEndSeparator("/abc")).isEqualTo("/");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("\\abc")).isEqualTo("\\");

        // Test one level directory
        assertThat(FilenameUtils.getFullPathNoEndSeparator("/abc/xyz")).isEqualTo("/abc");
        assertThat(FilenameUtils.getFullPathNoEndSeparator("\\abc\\xyz")).isEqualTo("\\abc");
    }

    @Test
    public void testGetName() {
        assertThat(FilenameUtils.getName(null)).isNull();
        assertThat(FilenameUtils.getName("noseperator.inthispath")).isEqualTo("noseperator.inthispath");
        assertThat(FilenameUtils.getName("a/b/c.txt")).isEqualTo("c.txt");
        assertThat(FilenameUtils.getName("a/b/c")).isEqualTo("c");
        assertThat(FilenameUtils.getName("a/b/c/")).isEqualTo("");
        assertThat(FilenameUtils.getName("a\\b\\c")).isEqualTo("c");
    }

    @Test
    public void testInjectionFailure() {
        try {
            assertThat(FilenameUtils.getName("a\\b\\\u0000c")).isEqualTo("c");
        } catch (final IllegalArgumentException ignore) {

        }
    }

    @Test
    public void testGetBaseName() {
        assertThat(FilenameUtils.getBaseName(null)).isNull();
        assertThat(FilenameUtils.getBaseName("noseperator.inthispath")).isEqualTo("noseperator");
        assertThat(FilenameUtils.getBaseName("a/b/c.txt")).isEqualTo("c");
        assertThat(FilenameUtils.getBaseName("a/b/c")).isEqualTo("c");
        assertThat(FilenameUtils.getBaseName("a/b/c/")).isEqualTo("");
        assertThat(FilenameUtils.getBaseName("a\\b\\c")).isEqualTo("c");
        assertThat(FilenameUtils.getBaseName("file.txt.bak")).isEqualTo("file.txt");
    }

    @Test
    public void testGetBaseName_with_nullByte() {
        try {
            assertThat(FilenameUtils.getBaseName("fil\u0000e.txt.bak")).isEqualTo("file.txt");
        } catch (final IllegalArgumentException ignore) {

        }
    }

    @Test
    public void testGetExtension() {
        assertThat(FilenameUtils.getExtension(null)).isNull();
        assertThat(FilenameUtils.getExtension("file.ext")).isEqualTo("ext");
        assertThat(FilenameUtils.getExtension("README")).isEqualTo("");
        assertThat(FilenameUtils.getExtension("domain.dot.com")).isEqualTo("com");
        assertThat(FilenameUtils.getExtension("image.jpeg")).isEqualTo("jpeg");
        assertThat(FilenameUtils.getExtension("a.b/c")).isEqualTo("");
        assertThat(FilenameUtils.getExtension("a.b/c.txt")).isEqualTo("txt");
        assertThat(FilenameUtils.getExtension("a/b/c")).isEqualTo("");
        assertThat(FilenameUtils.getExtension("a.b\\c")).isEqualTo("");
        assertThat(FilenameUtils.getExtension("a.b\\c.txt")).isEqualTo("txt");
        assertThat(FilenameUtils.getExtension("a\\b\\c")).isEqualTo("");
        assertThat(FilenameUtils.getExtension("C:\\temp\\foo.bar\\README")).isEqualTo("");
        assertThat(FilenameUtils.getExtension("../filename.ext")).isEqualTo("ext");

        if (FilenameUtils.isSystemWindows()) {
            // Special case handling for NTFS ADS names
        	try {
        		FilenameUtils.getExtension("foo.exe:bar.txt");
        		throw new AssertionError("Expected Exception");
        	} catch (final IllegalArgumentException e) {
             assertThat(e.getMessage()).isEqualTo("NTFS ADS separator (':') in file name is forbidden.");
        	}
        } else {
            // Upwards compatibility:
        	   assertThat(FilenameUtils.getExtension("foo.exe:bar.txt")).isEqualTo("txt");
        }
    }

    @Test
    public void testRemoveExtension() {
        assertThat(FilenameUtils.removeExtension(null)).isNull();
        assertThat(FilenameUtils.removeExtension("file.ext")).isEqualTo("file");
        assertThat(FilenameUtils.removeExtension("README")).isEqualTo("README");
        assertThat(FilenameUtils.removeExtension("domain.dot.com")).isEqualTo("domain.dot");
        assertThat(FilenameUtils.removeExtension("image.jpeg")).isEqualTo("image");
        assertThat(FilenameUtils.removeExtension("a.b/c")).isEqualTo("a.b/c");
        assertThat(FilenameUtils.removeExtension("a.b/c.txt")).isEqualTo("a.b/c");
        assertThat(FilenameUtils.removeExtension("a/b/c")).isEqualTo("a/b/c");
        assertThat(FilenameUtils.removeExtension("a.b\\c")).isEqualTo("a.b\\c");
        assertThat(FilenameUtils.removeExtension("a.b\\c.txt")).isEqualTo("a.b\\c");
        assertThat(FilenameUtils.removeExtension("a\\b\\c")).isEqualTo("a\\b\\c");
        assertThat(FilenameUtils.removeExtension("C:\\temp\\foo.bar\\README")).isEqualTo("C:\\temp\\foo.bar\\README");
        assertThat(FilenameUtils.removeExtension("../filename.ext")).isEqualTo("../filename");
    }

    //-----------------------------------------------------------------------
    @Test
    public void testEquals() {
        assertThat(FilenameUtils.equals(null, null)).isTrue();
        assertThat(FilenameUtils.equals(null, "")).isFalse();
        assertThat(FilenameUtils.equals("", null)).isFalse();
        assertThat(FilenameUtils.equals("", "")).isTrue();
        assertThat(FilenameUtils.equals("file.txt", "file.txt")).isTrue();
        assertThat(FilenameUtils.equals("file.txt", "FILE.TXT")).isFalse();
        assertThat(FilenameUtils.equals("a\\b\\file.txt", "a/b/file.txt")).isFalse();
    }

    @Test
    public void testEqualsOnSystem() {
        assertThat(FilenameUtils.equalsOnSystem(null, null)).isTrue();
        assertThat(FilenameUtils.equalsOnSystem(null, "")).isFalse();
        assertThat(FilenameUtils.equalsOnSystem("", null)).isFalse();
        assertThat(FilenameUtils.equalsOnSystem("", "")).isTrue();
        assertThat(FilenameUtils.equalsOnSystem("file.txt", "file.txt")).isTrue();
        assertThat(FilenameUtils.equalsOnSystem("file.txt", "FILE.TXT")).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.equalsOnSystem("a\\b\\file.txt", "a/b/file.txt")).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test
    public void testEqualsNormalized() {
        assertThat(FilenameUtils.equalsNormalized(null, null)).isTrue();
        assertThat(FilenameUtils.equalsNormalized(null, "")).isFalse();
        assertThat(FilenameUtils.equalsNormalized("", null)).isFalse();
        assertThat(FilenameUtils.equalsNormalized("", "")).isTrue();
        assertThat(FilenameUtils.equalsNormalized("file.txt", "file.txt")).isTrue();
        assertThat(FilenameUtils.equalsNormalized("file.txt", "FILE.TXT")).isFalse();
        assertThat(FilenameUtils.equalsNormalized("a\\b\\file.txt", "a/b/file.txt")).isTrue();
        assertThat(FilenameUtils.equalsNormalized("a/b/", "a/b")).isFalse();
    }

    @Test
    public void testEqualsNormalizedOnSystem() {
        assertThat(FilenameUtils.equalsNormalizedOnSystem(null, null)).isTrue();
        assertThat(FilenameUtils.equalsNormalizedOnSystem(null, "")).isFalse();
        assertThat(FilenameUtils.equalsNormalizedOnSystem("", null)).isFalse();
        assertThat(FilenameUtils.equalsNormalizedOnSystem("", "")).isTrue();
        assertThat(FilenameUtils.equalsNormalizedOnSystem("file.txt", "file.txt")).isTrue();
        assertThat(FilenameUtils.equalsNormalizedOnSystem("file.txt", "FILE.TXT")).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.equalsNormalizedOnSystem("a\\b\\file.txt", "a/b/file.txt")).isTrue();
        assertThat(FilenameUtils.equalsNormalizedOnSystem("a/b/", "a/b")).isFalse();
    }

    /**
     * Test for https://issues.apache.org/jira/browse/IO-128
     */
    @Test
    public void testEqualsNormalizedError_IO_128() {
        assertThat(FilenameUtils.equalsNormalizedOnSystem("//file.txt", "file.txt")).isFalse();
        assertThat(FilenameUtils.equalsNormalizedOnSystem("file.txt", "//file.txt")).isFalse();
        assertThat(FilenameUtils.equalsNormalizedOnSystem("//file.txt", "//file.txt")).isFalse();
    }

    @Test
    public void testEquals_fullControl() {
        assertThat(FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.SENSITIVE)).isFalse();
        assertThat(FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.INSENSITIVE)).isTrue();
        assertThat(FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.SYSTEM)).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.equals("file.txt", "FILE.TXT", true, null)).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test
    public void testIsExtension() {
        assertThat(FilenameUtils.isExtension(null, (String) null)).isFalse();
        assertThat(FilenameUtils.isExtension("file.txt", (String) null)).isFalse();
        assertThat(FilenameUtils.isExtension("file", (String) null)).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", "")).isFalse();
        assertThat(FilenameUtils.isExtension("file", "")).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", "rtf")).isFalse();

        assertThat(FilenameUtils.isExtension("a/b/file.txt", (String) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", "")).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", "rtf")).isFalse();

        assertThat(FilenameUtils.isExtension("a.b/file.txt", (String) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", "")).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", "rtf")).isFalse();

        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", (String) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", "")).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", "rtf")).isFalse();

        assertThat(FilenameUtils.isExtension("a.b\\file.txt", (String) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "")).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "rtf")).isFalse();

        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "TXT")).isFalse();
    }

    @Test
    public void testIsExtension_injection() {
        try {
            FilenameUtils.isExtension("a.b\\fi\u0000le.txt", "TXT");
            fail("Should throw IAE");
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testIsExtensionArray() {
        assertThat(FilenameUtils.isExtension(null, (String[]) null)).isFalse();
        assertThat(FilenameUtils.isExtension("file.txt", (String[]) null)).isFalse();
        assertThat(FilenameUtils.isExtension("file", (String[]) null)).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt")).isFalse();
        assertThat(FilenameUtils.isExtension("file.txt", new String[]{"txt"})).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", new String[]{"rtf"})).isFalse();
        assertThat(FilenameUtils.isExtension("file", "rtf", "")).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a/b/file.txt", (String[]) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt")).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", new String[]{"txt"})).isTrue();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", new String[]{"rtf"})).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a.b/file.txt", (String[]) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt")).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", new String[]{"txt"})).isTrue();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", new String[]{"rtf"})).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", (String[]) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt")).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", new String[]{"txt"})).isTrue();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", new String[]{"rtf"})).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a.b\\file.txt", (String[]) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt")).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new String[]{"txt"})).isTrue();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new String[]{"rtf"})).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new String[]{"TXT"})).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "TXT", "RTF")).isFalse();
    }

    @Test
    public void testIsExtensionVarArgs() {
        assertThat(FilenameUtils.isExtension("file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", "rtf")).isFalse();
        assertThat(FilenameUtils.isExtension("file", "rtf", "")).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a/b/file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", "rtf")).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a.b/file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", "rtf")).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", "rtf")).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "txt")).isTrue();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "rtf")).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "rtf", "txt")).isTrue();

        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "TXT")).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", "TXT", "RTF")).isFalse();
    }

    @Test
    public void testIsExtensionCollection() {
        assertThat(FilenameUtils.isExtension(null, (Collection<String>) null)).isFalse();
        assertThat(FilenameUtils.isExtension("file.txt", (Collection<String>) null)).isFalse();
        assertThat(FilenameUtils.isExtension("file", (Collection<String>) null)).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", new ArrayList<String>())).isFalse();
        assertThat(FilenameUtils.isExtension("file.txt", new ArrayList<>(Arrays.asList("txt")))).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", new ArrayList<>(Arrays.asList("rtf")))).isFalse();
        assertThat(FilenameUtils.isExtension("file", new ArrayList<>(Arrays.asList("rtf", "")))).isTrue();
        assertThat(FilenameUtils.isExtension("file.txt", new ArrayList<>(Arrays.asList("rtf", "txt")))).isTrue();

        assertThat(FilenameUtils.isExtension("a/b/file.txt", (Collection<String>) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", new ArrayList<String>())).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", new ArrayList<>(Arrays.asList("txt")))).isTrue();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", new ArrayList<>(Arrays.asList("rtf")))).isFalse();
        assertThat(FilenameUtils.isExtension("a/b/file.txt", new ArrayList<>(Arrays.asList("rtf", "txt")))).isTrue();

        assertThat(FilenameUtils.isExtension("a.b/file.txt", (Collection<String>) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", new ArrayList<String>())).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", new ArrayList<>(Arrays.asList("txt")))).isTrue();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", new ArrayList<>(Arrays.asList("rtf")))).isFalse();
        assertThat(FilenameUtils.isExtension("a.b/file.txt", new ArrayList<>(Arrays.asList("rtf", "txt")))).isTrue();

        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", (Collection<String>) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList<String>())).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList<>(Arrays.asList("txt")))).isTrue();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList<>(Arrays.asList("rtf")))).isFalse();
        assertThat(FilenameUtils.isExtension("a\\b\\file.txt", new ArrayList<>(Arrays.asList("rtf", "txt")))).isTrue();

        assertThat(FilenameUtils.isExtension("a.b\\file.txt", (Collection<String>) null)).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<String>())).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("txt")))).isTrue();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("rtf")))).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("rtf", "txt")))).isTrue();

        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("TXT")))).isFalse();
        assertThat(FilenameUtils.isExtension("a.b\\file.txt", new ArrayList<>(Arrays.asList("TXT", "RTF")))).isFalse();
    }
}
