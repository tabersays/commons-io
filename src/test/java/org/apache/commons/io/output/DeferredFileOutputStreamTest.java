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
package org.apache.commons.io.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.IntStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for the {@code DeferredFileOutputStream} class.
 *
 */
public class DeferredFileOutputStreamTest {

    public static IntStream data() {
        return IntStream.of(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096);
    }

    /**
     * The test data as a string (which is the simplest form).
     */
    private final String testString = "0123456789";

    /**
     * The test data as a byte array, derived from the string.
     */
    private final byte[] testBytes = testString.getBytes();

    /**
     * Tests the case where the amount of data exceeds the threshold, and is therefore written to disk. The actual data
     * written to disk is verified, as is the file itself.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testAboveThreshold(final int initialBufferSize) {
        final File testFile = new File("testAboveThreshold.dat");

        // Ensure that the test starts from a clean base.
        testFile.delete();

        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length - 5, initialBufferSize,
            testFile);
        try {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        } catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertThat(dfos.isInMemory()).isFalse();
        assertThat(dfos.getData()).isNull();

        verifyResultFile(testFile);

        // Ensure that the test starts from a clean base.
        testFile.delete();
    }

    /**
     * Tests the case where the amount of data exceeds the threshold, and is therefore written to disk. The actual data
     * written to disk is verified, as is the file itself.
     * Testing the getInputStream() method.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testAboveThresholdGetInputStream(final int initialBufferSize, final @TempDir Path tempDir) throws IOException {
        final File testFile = tempDir.resolve("testAboveThreshold.dat").toFile();

        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length - 5, initialBufferSize,
            testFile);
        dfos.write(testBytes, 0, testBytes.length);
        dfos.close();
        assertThat(dfos.isInMemory()).isFalse();

        try (InputStream is = dfos.toInputStream()) {
            assertThat(IOUtils.toByteArray(is)).containsExactly(testBytes);
        }

        verifyResultFile(testFile);
    }

    /**
     * Tests the case where the amount of data is exactly the same as the threshold. The behavior should be the same as
     * that for the amount of data being below (i.e. not exceeding) the threshold.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testAtThreshold(final int initialBufferSize) {
        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length, initialBufferSize, null);
        try {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        } catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertThat(dfos.isInMemory()).isTrue();

        final byte[] resultBytes = dfos.getData();
        assertThat(resultBytes.length).isEqualTo(testBytes.length);
        assertThat(testBytes).containsExactly(resultBytes);
    }

    /**
     * Tests the case where the amount of data falls below the threshold, and is therefore confined to memory.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testBelowThreshold(final int initialBufferSize) {
        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length + 42, initialBufferSize,
            null);
        try {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        } catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertThat(dfos.isInMemory()).isTrue();

        final byte[] resultBytes = dfos.getData();
        assertThat(resultBytes.length).isEqualTo(testBytes.length);
        assertThat(testBytes).containsExactly(resultBytes);
    }

    /**
     * Tests the case where the amount of data falls below the threshold, and is therefore confined to memory.
     * Testing the getInputStream() method.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testBelowThresholdGetInputStream(final int initialBufferSize) throws IOException {
        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length + 42, initialBufferSize,
            null);
        dfos.write(testBytes, 0, testBytes.length);
        dfos.close();
        assertThat(dfos.isInMemory()).isTrue();

        try (InputStream is = dfos.toInputStream()) {
            assertThat(IOUtils.toByteArray(is)).containsExactly(testBytes);
        }
    }

    /**
     * Test specifying a temporary file and the threshold is reached.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileAboveThreshold(final int initialBufferSize) {

        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final File tempDir = new File(".");
        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length - 5, initialBufferSize,
            prefix, suffix, tempDir);
        assertThat(dfos.getFile()).as("Check file is null-A").isNull();
        try {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        } catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertThat(dfos.isInMemory()).isFalse();
        assertThat(dfos.getData()).isNull();
        assertThat(dfos.getFile()).as("Check file not null").isNotNull();
        assertThat(dfos.getFile().exists()).as("Check file exists").isTrue();
        assertThat(dfos.getFile().getName().startsWith(prefix)).as("Check prefix").isTrue();
        assertThat(dfos.getFile().getName().endsWith(suffix)).as("Check suffix").isTrue();
        assertThat(dfos.getFile().getParent()).as("Check dir").isEqualTo(tempDir.getPath());

        verifyResultFile(dfos.getFile());

        // Delete the temporary file.
        dfos.getFile().delete();
    }

    /**
     * Test specifying a temporary file and the threshold is reached.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileAboveThresholdPrefixOnly(final int initialBufferSize) {

        final String prefix = "commons-io-test";
        final String suffix = null;
        final File tempDir = null;
        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length - 5, initialBufferSize,
            prefix, suffix, tempDir);
        assertThat(dfos.getFile()).as("Check file is null-A").isNull();
        try {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        } catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertThat(dfos.isInMemory()).isFalse();
        assertThat(dfos.getData()).isNull();
        assertThat(dfos.getFile()).as("Check file not null").isNotNull();
        assertThat(dfos.getFile().exists()).as("Check file exists").isTrue();
        assertThat(dfos.getFile().getName().startsWith(prefix)).as("Check prefix").isTrue();
        assertThat(dfos.getFile().getName().endsWith(".tmp")).as("Check suffix").isTrue(); // ".tmp" is default

        verifyResultFile(dfos.getFile());

        // Delete the temporary file.
        dfos.getFile().delete();
    }

    /**
     * Test specifying a temporary file and the threshold not reached.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testTempFileBelowThreshold(final int initialBufferSize) {

        final String prefix = "commons-io-test";
        final String suffix = ".out";
        final File tempDir = new File(".");
        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length + 42, initialBufferSize,
            prefix, suffix, tempDir);
        assertThat(dfos.getFile()).as("Check file is null-A").isNull();
        try {
            dfos.write(testBytes, 0, testBytes.length);
            dfos.close();
        } catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertThat(dfos.isInMemory()).isTrue();
        assertThat(dfos.getFile()).as("Check file is null-B").isNull();
    }

    /**
     * Test specifying a temporary file and the threshold is reached.
     *
     * @throws Exception
     */
    @Test
    public void testTempFileError() throws Exception {

        final String prefix = null;
        final String suffix = ".out";
        final File tempDir = new File(".");
        try {
            new DeferredFileOutputStream(testBytes.length - 5, prefix, suffix, tempDir).close();
            fail("Expected IllegalArgumentException ");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Tests the case where there are multiple writes beyond the threshold, to ensure that the
     * {@code thresholdReached()} method is only called once, as the threshold is crossed for the first time.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testThresholdReached(final int initialBufferSize) {
        final File testFile = new File("testThresholdReached.dat");

        // Ensure that the test starts from a clean base.
        testFile.delete();

        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length / 2, initialBufferSize,
            testFile);
        final int chunkSize = testBytes.length / 3;

        try {
            dfos.write(testBytes, 0, chunkSize);
            dfos.write(testBytes, chunkSize, chunkSize);
            dfos.write(testBytes, chunkSize * 2, testBytes.length - chunkSize * 2);
            dfos.close();
        } catch (final IOException e) {
            fail("Unexpected IOException");
        }
        assertThat(dfos.isInMemory()).isFalse();
        assertThat(dfos.getData()).isNull();

        verifyResultFile(testFile);

        // Ensure that the test starts from a clean base.
        testFile.delete();
    }

    /**
     * Test whether writeTo() properly writes large content.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testWriteToLarge(final int initialBufferSize) {
        final File testFile = new File("testWriteToFile.dat");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
        // Ensure that the test starts from a clean base.
        testFile.delete();

        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length / 2, testFile);
        try {
            dfos.write(testBytes);

            assertThat(testFile.exists()).isTrue();
            assertThat(dfos.isInMemory()).isFalse();

            try {
                dfos.writeTo(baos);
                fail("Should not have been able to write before closing");
            } catch (final IOException ioe) {
                // ok, as expected
            }

            dfos.close();
            dfos.writeTo(baos);
        } catch (final IOException ioe) {
            fail("Unexpected IOException");
        }
        final byte[] copiedBytes = baos.toByteArray();
        assertThat(copiedBytes).containsExactly(testBytes);
        verifyResultFile(testFile);
        testFile.delete();
    }

    /**
     * Test whether writeTo() properly writes small content.
     */
    @ParameterizedTest(name = "initialBufferSize = {0}")
    @MethodSource("data")
    public void testWriteToSmall(final int initialBufferSize) {
        final File testFile = new File("testWriteToMem.dat");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(initialBufferSize);
        // Ensure that the test starts from a clean base.
        testFile.delete();

        final DeferredFileOutputStream dfos = new DeferredFileOutputStream(testBytes.length * 2, initialBufferSize,
            testFile);
        try {
            dfos.write(testBytes);

            assertThat(testFile.exists()).isFalse();
            assertThat(dfos.isInMemory()).isTrue();

            try {
                dfos.writeTo(baos);
                fail("Should not have been able to write before closing");
            } catch (final IOException ioe) {
                // ok, as expected
            }

            dfos.close();
            dfos.writeTo(baos);
        } catch (final IOException ioe) {
            fail("Unexpected IOException");
        }
        final byte[] copiedBytes = baos.toByteArray();
        assertThat(copiedBytes).containsExactly(testBytes);

        testFile.delete();
    }

    /**
     * Verifies that the specified file contains the same data as the original test data.
     *
     * @param testFile The file containing the test output.
     */
    private void verifyResultFile(final File testFile) {
        try {
            final FileInputStream fis = new FileInputStream(testFile);
            assertThat(fis.available()).isEqualTo(testBytes.length);

            final byte[] resultBytes = new byte[testBytes.length];
            assertThat(fis.read(resultBytes)).isEqualTo(testBytes.length);

            assertThat(testBytes).containsExactly(resultBytes);
            assertThat(fis.read(resultBytes)).isEqualTo(-1);

            try {
                fis.close();
            } catch (final IOException e) {
                // Ignore an exception on close
            }
        } catch (final FileNotFoundException e) {
            fail("Unexpected FileNotFoundException");
        } catch (final IOException e) {
            fail("Unexpected IOException");
        }
    }
}
