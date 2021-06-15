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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.TestResources;
import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Tailer}.
 *
 */
public class TailerTest {

    @TempDir
    public static File temporaryFolder;

    private Tailer tailer;

    @AfterEach
    public void tearDown() {
        if (tailer != null) {
            tailer.stop();
        }
    }

    @Test
    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    public void testLongFile() throws Exception {
        final long delay = 50;

        final File file = new File(temporaryFolder, "testLongFile.txt");
        createFile(file, 0);
        try (final Writer writer = new FileWriter(file, true)) {
            for (int i = 0; i < 100000; i++) {
                writer.write("LineLineLineLineLineLineLineLineLineLine\n");
            }
            writer.write("SBTOURIST\n");
        }

        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delay, false);

        // final long start = System.currentTimeMillis();

        final Thread thread = new Thread(tailer);
        thread.start();

        List<String> lines = listener.getLines();
        while (lines.isEmpty() || !lines.get(lines.size() - 1).equals("SBTOURIST")) {
            lines = listener.getLines();
        }
        // System.out.println("Elapsed: " + (System.currentTimeMillis() - start));

        listener.clear();
    }

    @Test
    @SuppressWarnings("squid:S2699") // Suppress "Add at least one assertion to this test case"
    public void testBufferBreak() throws Exception {
        final long delay = 50;

        final File file = new File(temporaryFolder, "testBufferBreak.txt");
        createFile(file, 0);
        writeString(file, "SBTOURIST\n");

        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delay, false, 1);

        final Thread thread = new Thread(tailer);
        thread.start();

        List<String> lines = listener.getLines();
        while (lines.isEmpty() || !lines.get(lines.size() - 1).equals("SBTOURIST")) {
            lines = listener.getLines();
        }

        listener.clear();
    }

    @Test
    public void testMultiByteBreak() throws Exception {
        // System.out.println("testMultiByteBreak() Default charset: " + Charset.defaultCharset().displayName());
        final long delay = 50;
        final File origin = TestResources.getFile("test-file-utf8.bin");
        final File file = new File(temporaryFolder, "testMultiByteBreak.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        final String osname = System.getProperty("os.name");
        final boolean isWindows = osname.startsWith("Windows");
        // Need to use UTF-8 to read & write the file otherwise it can be corrupted (depending on the default charset)
        final Charset charsetUTF8 = StandardCharsets.UTF_8;
        tailer = new Tailer(file, charsetUTF8, listener, delay, false, isWindows, IOUtils.DEFAULT_BUFFER_SIZE);
        final Thread thread = new Thread(tailer);
        thread.start();

        try (Writer out = new OutputStreamWriter(new FileOutputStream(file), charsetUTF8);
             BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(origin), charsetUTF8))) {
            final List<String> lines = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null){
                out.write(line);
                out.write("\n");
                lines.add(line);
            }
            out.close(); // ensure data is written

           final long testDelayMillis = delay * 10;
           TestUtils.sleep(testDelayMillis);
           final List<String> tailerlines = listener.getLines();
            assertThat(tailerlines.size()).as("line count").isEqualTo(lines.size());
           for(int i = 0,len = lines.size();i<len;i++){
               final String expected = lines.get(i);
               final String actual = tailerlines.get(i);
               if (!expected.equals(actual)) {
                   fail("Line: " + i
                           + "\nExp: (" + expected.length() + ") " + expected
                           + "\nAct: (" + actual.length() + ") "+ actual);
               }
           }
        }
    }

    @Test
    public void testTailerEof() throws Exception {
        // Create & start the Tailer
        final long delay = 50;
        final File file = new File(temporaryFolder, "tailer2-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delay, false);
        final Thread thread = new Thread(tailer);
        thread.start();

        // Write some lines to the file
        writeString(file, "Line");

        TestUtils.sleep(delay * 2);
        List<String> lines = listener.getLines();
        assertThat(lines.size()).as("1 line count").isEqualTo(0);

        writeString(file, " one\n");
        TestUtils.sleep(delay * 2);
        lines = listener.getLines();

        assertThat(lines.size()).as("1 line count").isEqualTo(1);
        assertThat(lines.get(0)).as("1 line 1").isEqualTo("Line one");

        listener.clear();
    }

    @Test
    public void testTailer() throws Exception {

        // Create & start the Tailer
        final long delayMillis = 50;
        final File file = new File(temporaryFolder, "tailer1-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        final String osname = System.getProperty("os.name");
        final boolean isWindows = osname.startsWith("Windows");
        tailer = new Tailer(file, listener, delayMillis, false, isWindows);
        final Thread thread = new Thread(tailer);
        thread.start();

        // Write some lines to the file
        write(file, "Line one", "Line two");
        final long testDelayMillis = delayMillis * 10;
        TestUtils.sleep(testDelayMillis);
        List<String> lines = listener.getLines();
        assertThat(lines.size()).as("1 line count").isEqualTo(2);
        assertThat(lines.get(0)).as("1 line 1").isEqualTo("Line one");
        assertThat(lines.get(1)).as("1 line 2").isEqualTo("Line two");
        listener.clear();

        // Write another line to the file
        write(file, "Line three");
        TestUtils.sleep(testDelayMillis);
        lines = listener.getLines();
        assertThat(lines.size()).as("2 line count").isEqualTo(1);
        assertThat(lines.get(0)).as("2 line 3").isEqualTo("Line three");
        listener.clear();

        // Check file does actually have all the lines
        lines = FileUtils.readLines(file, "UTF-8");
        assertThat(lines.size()).as("3 line count").isEqualTo(3);
        assertThat(lines.get(0)).as("3 line 1").isEqualTo("Line one");
        assertThat(lines.get(1)).as("3 line 2").isEqualTo("Line two");
        assertThat(lines.get(2)).as("3 line 3").isEqualTo("Line three");

        // Delete & re-create
        file.delete();
        assertThat(file.exists()).as("File should not exist").isFalse();
        createFile(file, 0);
        assertThat(file.exists()).as("File should now exist").isTrue();
        TestUtils.sleep(testDelayMillis);

        // Write another line
        write(file, "Line four");
        TestUtils.sleep(testDelayMillis);
        lines = listener.getLines();
        assertThat(lines.size()).as("4 line count").isEqualTo(1);
        assertThat(lines.get(0)).as("4 line 3").isEqualTo("Line four");
        listener.clear();

        // Stop
        thread.interrupt();
        TestUtils.sleep(testDelayMillis * 4);
        write(file, "Line five");
        assertThat(listener.getLines().size()).as("4 line count").isEqualTo(0);
        assertThat(listener.exception).as("Missing InterruptedException").isNotNull();
        assertThat(listener.exception instanceof InterruptedException).as("Unexpected Exception: " + listener.exception).isTrue();
        assertThat(listener.initialized).as("Expected init to be called").isEqualTo(1);
        // assertEquals(0 , listener.notFound, "fileNotFound should not be called"); // there is a window when it might be called
        assertThat(listener.rotated).as("fileRotated should be be called").isEqualTo(1);
    }

    @Test
    public void testTailerEndOfFileReached() throws Exception {
        // Create & start the Tailer
        final long delayMillis = 50;
        final long testDelayMillis = delayMillis * 10;
        final File file = new File(temporaryFolder, "tailer-eof-test.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        final String osname = System.getProperty("os.name");
        final boolean isWindows = osname.startsWith("Windows");
        tailer = new Tailer(file, listener, delayMillis, false, isWindows);
        final Thread thread = new Thread(tailer);
        thread.start();

        // write a few lines
        write(file, "line1", "line2", "line3");
        TestUtils.sleep(testDelayMillis);

        // write a few lines
        write(file, "line4", "line5", "line6");
        TestUtils.sleep(testDelayMillis);

        // write a few lines
        write(file, "line7", "line8", "line9");
        TestUtils.sleep(testDelayMillis);

        // May be > 3 times due to underlying OS behavior wrt streams
        assertThat(listener.reachedEndOfFile >= 3).as("end of file reached at least 3 times").isTrue();
    }

    protected void createFile(final File file, final long size)
        throws IOException {
        if (!file.getParentFile().exists()) {
            throw new IOException("Cannot create file " + file
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(file))) {
            TestUtils.generateTestData(output, size);
        }

        // try to make sure file is found
        // (to stop continuum occasionally failing)
        RandomAccessFile reader = null;
        try {
            while (reader == null) {
                try {
                    reader = new RandomAccessFile(file.getPath(), "r");
                } catch (final FileNotFoundException ignore) {
                }
                try {
                    TestUtils.sleep(200L);
                } catch (final InterruptedException ignore) {
                    // ignore
                }
            }
        } finally {
            try {
                IOUtils.close(reader);
            } catch (final IOException ignored) {
                // ignored
            }
        }
    }

    /** Append some lines to a file */
    private void write(final File file, final String... lines) throws Exception {
        try (FileWriter writer = new FileWriter(file, true)) {
            for (final String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    /** Append a string to a file */
    private void writeString(final File file, final String ... strings) throws Exception {
        try (FileWriter writer = new FileWriter(file, true)) {
            for (final String string : strings) {
                writer.write(string);
            }
        }
    }

    @Test
    public void testStopWithNoFile() throws Exception {
        final File file = new File(temporaryFolder,"nosuchfile");
        assertThat(file.exists()).as("nosuchfile should not exist").isFalse();
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        tailer = Tailer.create(file, listener, delay, false);
        TestUtils.sleep(idle);
        tailer.stop();
        TestUtils.sleep(delay+idle);
        assertThat(listener.exception).as("Should not generate Exception").isNull();
        assertThat(listener.initialized).as("Expected init to be called").isEqualTo(1);
        assertThat(listener.notFound > 0).as("fileNotFound should be called").isTrue();
        assertThat(listener.rotated).as("fileRotated should be not be called").isEqualTo(0);
        assertThat(listener.reachedEndOfFile).as("end of file never reached").isEqualTo(0);
    }

    /*
     * Tests [IO-357][Tailer] InterruptedException while the thead is sleeping is silently ignored.
     */
    @Test
    public void testInterrupt() throws Exception {
        final File file = new File(temporaryFolder, "nosuchfile");
        assertThat(file.exists()).as("nosuchfile should not exist").isFalse();
        final TestTailerListener listener = new TestTailerListener();
        // Use a long delay to try to make sure the test thread calls interrupt() while the tailer thread is sleeping.
        final int delay = 1000;
        final int idle = 50; // allow time for thread to work
        tailer = new Tailer(file, listener, delay, false, IOUtils.DEFAULT_BUFFER_SIZE);
        final Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
        TestUtils.sleep(idle);
        thread.interrupt();
        TestUtils.sleep(delay + idle);
        assertThat(listener.exception).as("Missing InterruptedException").isNotNull();
        assertThat(listener.exception instanceof InterruptedException).as("Unexpected Exception: " + listener.exception).isTrue();
        assertThat(listener.initialized).as("Expected init to be called").isEqualTo(1);
        assertThat(listener.notFound > 0).as("fileNotFound should be called").isTrue();
        assertThat(listener.rotated).as("fileRotated should be not be called").isEqualTo(0);
        assertThat(listener.reachedEndOfFile).as("end of file never reached").isEqualTo(0);
    }

    @Test
    public void testStopWithNoFileUsingExecutor() throws Exception {
        final File file = new File(temporaryFolder,"nosuchfile");
        assertThat(file.exists()).as("nosuchfile should not exist").isFalse();
        final TestTailerListener listener = new TestTailerListener();
        final int delay = 100;
        final int idle = 50; // allow time for thread to work
        tailer = new Tailer(file, listener, delay, false);
        final Executor exec = new ScheduledThreadPoolExecutor(1);
        exec.execute(tailer);
        TestUtils.sleep(idle);
        tailer.stop();
        TestUtils.sleep(delay+idle);
        assertThat(listener.exception).as("Should not generate Exception").isNull();
        assertThat(listener.initialized).as("Expected init to be called").isEqualTo(1);
        assertThat(listener.notFound > 0).as("fileNotFound should be called").isTrue();
        assertThat(listener.rotated).as("fileRotated should be not be called").isEqualTo(0);
        assertThat(listener.reachedEndOfFile).as("end of file never reached").isEqualTo(0);
    }

    @Test
    public void testIO335() throws Exception { // test CR behavior
        // Create & start the Tailer
        final long delayMillis = 50;
        final File file = new File(temporaryFolder, "tailer-testio334.txt");
        createFile(file, 0);
        final TestTailerListener listener = new TestTailerListener();
        tailer = new Tailer(file, listener, delayMillis, false);
        final Thread thread = new Thread(tailer);
        thread.start();

        // Write some lines to the file
        writeString(file, "CRLF\r\n", "LF\n", "CR\r", "CRCR\r\r", "trail");
        final long testDelayMillis = delayMillis * 10;
        TestUtils.sleep(testDelayMillis);
        final List<String> lines = listener.getLines();
        assertThat(lines.size()).as("line count").isEqualTo(4);
        assertThat(lines.get(0)).as("line 1").isEqualTo("CRLF");
        assertThat(lines.get(1)).as("line 2").isEqualTo("LF");
        assertThat(lines.get(2)).as("line 3").isEqualTo("CR");
        assertThat(lines.get(3)).as("line 4").isEqualTo("CRCR\r");
    }

    /**
     * Test {@link TailerListener} implementation.
     */
    private static class TestTailerListener extends TailerListenerAdapter {

        // Must be synchronized because it is written by one thread and read by another
        private final List<String> lines = Collections.synchronizedList(new ArrayList<String>());

        volatile Exception exception;

        volatile int notFound;

        volatile int rotated;

        volatile int initialized;

        volatile int reachedEndOfFile;

        @Override
        public void handle(final String line) {
            lines.add(line);
        }

        public List<String> getLines() {
            return lines;
        }

        public void clear() {
            lines.clear();
        }

        @Override
        public void handle(final Exception e) {
            exception = e;
        }

        @Override
        public void init(final Tailer tailer) {
            initialized++; // not atomic, but OK because only updated here.
        }

        @Override
        public void fileNotFound() {
            notFound++; // not atomic, but OK because only updated here.
        }

        @Override
        public void fileRotated() {
            rotated++; // not atomic, but OK because only updated here.
        }

        @Override
        public void endOfFileReached() {
            reachedEndOfFile++; // not atomic, but OK because only updated here.
        }
    }
}
