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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.PathUtilsIsEmptyTest;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.test.TestUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This is used to test FileUtils for correctness.
 *
 * @see FileUtils
 */
@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"}) // unit tests include tests of many deprecated methods
public class FileUtilsTestCase {

    /**
     * DirectoryWalker implementation that recursively lists all files and directories.
     */
    static class ListDirectoryWalker extends DirectoryWalker<File> {

        ListDirectoryWalker() {
        }

        @Override
        protected void handleDirectoryStart(final File directory, final int depth, final Collection<File> results) throws IOException {
            // Add all directories except the starting directory
            if (depth > 0) {
                results.add(directory);
            }
        }

        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> results) throws IOException {
            results.add(file);
        }

        List<File> list(final File startDirectory) throws IOException {
            final ArrayList<File> files = new ArrayList<>();
            walk(startDirectory, files);
            return files;
        }
    }

    // Test helper class to pretend a file is shorter than it is
    private static class ShorterFile extends File {
        private static final long serialVersionUID = 1L;

        public ShorterFile(final String pathname) {
            super(pathname);
        }

        @Override
        public long length() {
            return super.length() - 1;
        }
    }

    /** Test data. */
    private static final long DATE3 = 1000000002000L;

    /** Test data. */
    private static final long DATE2 = 1000000001000L;

    /** Test data. */
    private static final long DATE1 = 1000000000000L;

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;

    /**
     * Size of test directory.
     */
    private static final BigInteger TEST_DIRECTORY_SIZE_BI = BigInteger.ZERO;

    /**
     * Size (greater of zero) of test file.
     */
    private static final BigInteger TEST_DIRECTORY_SIZE_GT_ZERO_BI = BigInteger.valueOf(100);

    /**
     * List files recursively
     */
    private static final ListDirectoryWalker LIST_WALKER = new ListDirectoryWalker();
    @TempDir
    public File temporaryFolder;

    /**
     * Delay in milliseconds to make sure test for "last modified date" are accurate
     */
    //private static final int LAST_MODIFIED_DELAY = 600;

    private File testFile1;
    private File testFile2;

    private long testFile1Size;

    private long testFile2Size;

    private void backDateFile10Minutes(final File testFile) throws IOException {
        final long mins10 = 1000 * 60 * 10;
        final long lastModified1 = getLastModifiedMillis(testFile);
        assertThat(setLastModifiedMillis(testFile, lastModified1 - mins10)).isTrue();
        // ensure it was changed
        assertThat(lastModified1).as("Should have changed source date").isNotEqualTo(getLastModifiedMillis(testFile));
    }

    private void consumeRemaining(final Iterator<File> iterator) {
        if (iterator != null) {
            iterator.forEachRemaining(e -> {});
        }
    }

    private void createCircularSymLink(final File file) throws IOException {
        if (!FilenameUtils.isSystemWindows()) {
            Runtime.getRuntime()
                    .exec("ln -s " + file + "/.. " + file + "/cycle");
        } else {
            try {
                Runtime.getRuntime()
                        .exec("mklink /D " + file + "/cycle" + file + "/.. ");
            } catch (final IOException ioe) { // So that tests run in FAT filesystems
                //don't fail
            }
        }
    }

    private void createFilesForTestCopyDirectory(final File grandParentDir, final File parentDir, final File childDir) throws Exception {
        final File childDir2 = new File(parentDir, "child2");
        final File grandChildDir = new File(childDir, "grandChild");
        final File grandChild2Dir = new File(childDir2, "grandChild2");
        final File file1 = new File(grandParentDir, "file1.txt");
        final File file2 = new File(parentDir, "file2.txt");
        final File file3 = new File(childDir, "file3.txt");
        final File file4 = new File(childDir2, "file4.txt");
        final File file5 = new File(grandChildDir, "file5.txt");
        final File file6 = new File(grandChild2Dir, "file6.txt");
        FileUtils.deleteDirectory(grandParentDir);
        grandChildDir.mkdirs();
        grandChild2Dir.mkdirs();
        FileUtils.writeStringToFile(file1, "File 1 in grandparent", "UTF8");
        FileUtils.writeStringToFile(file2, "File 2 in parent", "UTF8");
        FileUtils.writeStringToFile(file3, "File 3 in child", "UTF8");
        FileUtils.writeStringToFile(file4, "File 4 in child2", "UTF8");
        FileUtils.writeStringToFile(file5, "File 5 in grandChild", "UTF8");
        FileUtils.writeStringToFile(file6, "File 6 in grandChild2", "UTF8");
    }

    private long getLastModifiedMillis(final File file) throws IOException {
        return FileUtils.lastModified(file);
    }

    private String getName() {
        return this.getClass().getSimpleName();
    }

    private void iterateFilesAndDirs(final File dir, final IOFileFilter fileFilter,
        final IOFileFilter dirFilter, final Collection<File> expectedFilesAndDirs) {
        final Iterator<File> iterator;
        int filesCount = 0;
        iterator = FileUtils.iterateFilesAndDirs(dir, fileFilter, dirFilter);
        try {
            final List<File> actualFiles = new ArrayList<>();
            while (iterator.hasNext()) {
                filesCount++;
                final File file = iterator.next();
                actualFiles.add(file);
                assertThat(expectedFilesAndDirs.contains(file)).withFailMessage(() -> "Unexpected directory/file " + file + ", expected one of " + expectedFilesAndDirs).isTrue();
            }
            assertThat(filesCount).withFailMessage(() -> actualFiles.toString()).isEqualTo(expectedFilesAndDirs.size());
        } finally {
            // MUST consume until the end in order to close the underlying stream.
            consumeRemaining(iterator);
        }
    }

    //-----------------------------------------------------------------------
    void openOutputStream_noParent(final boolean createFile) throws Exception {
        final File file = new File("test.txt");
        assertThat(file.getParentFile()).isNull();
        try {
            if (createFile) {
                TestUtils.createLineBasedFile(file, new String[]{"Hello"});
            }
            try (FileOutputStream out = FileUtils.openOutputStream(file)) {
                out.write(0);
            }
            assertThat(file.exists()).isTrue();
        } finally {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    private boolean setLastModifiedMillis(final File testFile, final long millis) {
        return testFile.setLastModified(millis);
//        try {
//            Files.setLastModifiedTime(testFile.toPath(), FileTime.fromMillis(millis));
//        } catch (IOException e) {
//            return false;
//        }
//        return true;
    }

    @BeforeEach
    public void setUp() throws Exception {
        testFile1 = new File(temporaryFolder, "file1-test.txt");
        testFile2 = new File(temporaryFolder, "file1a-test.txt");

        testFile1Size = testFile1.length();
        testFile2Size = testFile2.length();
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
        FileUtils.deleteDirectory(temporaryFolder);
        temporaryFolder.mkdirs();
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
    public void test_openInputStream_exists() throws Exception {
        final File file = new File(temporaryFolder, "test.txt");
        TestUtils.createLineBasedFile(file, new String[]{"Hello"});
        try (FileInputStream in = FileUtils.openInputStream(file)) {
            assertThat(in.read()).isEqualTo('H');
        }
    }

    @Test
    public void test_openInputStream_existsButIsDirectory() {
        final File directory = new File(temporaryFolder, "subdir");
        directory.mkdirs();
        assertThrows(IOException.class, () -> FileUtils.openInputStream(directory));
    }

    @Test
    public void test_openInputStream_notExists() {
        final File directory = new File(temporaryFolder, "test.txt");
        try (FileInputStream in = FileUtils.openInputStream(directory)) {
            fail();
        } catch (final IOException ioe) {
            // expected
        }
    }

    @Test
    public void test_openOutputStream_exists() throws Exception {
        final File file = new File(temporaryFolder, "test.txt");
        TestUtils.createLineBasedFile(file, new String[]{"Hello"});
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            out.write(0);
        }
        assertThat(file.exists()).isTrue();
    }

    @Test
    public void test_openOutputStream_existsButIsDirectory() {
        final File directory = new File(temporaryFolder, "subdir");
        directory.mkdirs();
        assertThrows(IllegalArgumentException.class, () -> FileUtils.openOutputStream(directory));
    }

    @Test
    public void test_openOutputStream_noParentCreateFile() throws Exception {
        openOutputStream_noParent(true);
    }

    @Test
    public void test_openOutputStream_noParentNoFile() throws Exception {
        openOutputStream_noParent(false);
    }

    @Test
    public void test_openOutputStream_notExists() throws Exception {
        final File file = new File(temporaryFolder, "a/test.txt");
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            out.write(0);
        }
        assertThat(file.exists()).isTrue();
    }

    @Test
    public void test_openOutputStream_notExistsCannotCreate() {
        // according to Wikipedia, most filing systems have a 256 limit on filename
        final String longStr =
                "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
                        "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz";  // 300 chars
        final File file = new File(temporaryFolder, "a/" + longStr + "/test.txt");
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            fail();
        } catch (final IOException ioe) {
            // expected
        }
    }

    //-----------------------------------------------------------------------
    // byteCountToDisplaySize
    @Test
    public void testByteCountToDisplaySizeBigInteger() {
        final BigInteger b1023 = BigInteger.valueOf(1023);
        final BigInteger b1025 = BigInteger.valueOf(1025);
        final BigInteger KB1 = BigInteger.valueOf(1024);
        final BigInteger MB1 = KB1.multiply(KB1);
        final BigInteger GB1 = MB1.multiply(KB1);
        final BigInteger GB2 = GB1.add(GB1);
        final BigInteger TB1 = GB1.multiply(KB1);
        final BigInteger PB1 = TB1.multiply(KB1);
        final BigInteger EB1 = PB1.multiply(KB1);
        assertThat(FileUtils.byteCountToDisplaySize(BigInteger.ZERO)).isEqualTo("0 bytes");
        assertThat(FileUtils.byteCountToDisplaySize(BigInteger.ONE)).isEqualTo("1 bytes");
        assertThat(FileUtils.byteCountToDisplaySize(b1023)).isEqualTo("1023 bytes");
        assertThat(FileUtils.byteCountToDisplaySize(KB1)).isEqualTo("1 KB");
        assertThat(FileUtils.byteCountToDisplaySize(b1025)).isEqualTo("1 KB");
        assertThat(FileUtils.byteCountToDisplaySize(MB1.subtract(BigInteger.ONE))).isEqualTo("1023 KB");
        assertThat(FileUtils.byteCountToDisplaySize(MB1)).isEqualTo("1 MB");
        assertThat(FileUtils.byteCountToDisplaySize(MB1.add(BigInteger.ONE))).isEqualTo("1 MB");
        assertThat(FileUtils.byteCountToDisplaySize(GB1.subtract(BigInteger.ONE))).isEqualTo("1023 MB");
        assertThat(FileUtils.byteCountToDisplaySize(GB1)).isEqualTo("1 GB");
        assertThat(FileUtils.byteCountToDisplaySize(GB1.add(BigInteger.ONE))).isEqualTo("1 GB");
        assertThat(FileUtils.byteCountToDisplaySize(GB2)).isEqualTo("2 GB");
        assertThat(FileUtils.byteCountToDisplaySize(GB2.subtract(BigInteger.ONE))).isEqualTo("1 GB");
        assertThat(FileUtils.byteCountToDisplaySize(TB1)).isEqualTo("1 TB");
        assertThat(FileUtils.byteCountToDisplaySize(PB1)).isEqualTo("1 PB");
        assertThat(FileUtils.byteCountToDisplaySize(EB1)).isEqualTo("1 EB");
        assertThat(FileUtils.byteCountToDisplaySize(Long.MAX_VALUE)).isEqualTo("7 EB");
        // Other MAX_VALUEs
        assertThat(FileUtils.byteCountToDisplaySize(BigInteger.valueOf(Character.MAX_VALUE))).isEqualTo("63 KB");
        assertThat(FileUtils.byteCountToDisplaySize(BigInteger.valueOf(Short.MAX_VALUE))).isEqualTo("31 KB");
        assertThat(FileUtils.byteCountToDisplaySize(BigInteger.valueOf(Integer.MAX_VALUE))).isEqualTo("1 GB");
    }

    @SuppressWarnings("NumericOverflow")
    @Test
    public void testByteCountToDisplaySizeLong() {
        assertThat(FileUtils.byteCountToDisplaySize(0)).isEqualTo("0 bytes");
        assertThat(FileUtils.byteCountToDisplaySize(1)).isEqualTo("1 bytes");
        assertThat(FileUtils.byteCountToDisplaySize(1023)).isEqualTo("1023 bytes");
        assertThat(FileUtils.byteCountToDisplaySize(1024)).isEqualTo("1 KB");
        assertThat(FileUtils.byteCountToDisplaySize(1025)).isEqualTo("1 KB");
        assertThat(FileUtils.byteCountToDisplaySize(1024 * 1023)).isEqualTo("1023 KB");
        assertThat(FileUtils.byteCountToDisplaySize(1024 * 1024)).isEqualTo("1 MB");
        assertThat(FileUtils.byteCountToDisplaySize(1024 * 1025)).isEqualTo("1 MB");
        assertThat(FileUtils.byteCountToDisplaySize(1024 * 1024 * 1023)).isEqualTo("1023 MB");
        assertThat(FileUtils.byteCountToDisplaySize(1024 * 1024 * 1024)).isEqualTo("1 GB");
        assertThat(FileUtils.byteCountToDisplaySize(1024 * 1024 * 1025)).isEqualTo("1 GB");
        assertThat(FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 2)).isEqualTo("2 GB");
        assertThat(FileUtils.byteCountToDisplaySize(1024 * 1024 * 1024 * 2 - 1)).isEqualTo("1 GB");
        assertThat(FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 1024)).isEqualTo("1 TB");
        assertThat(FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 1024 * 1024)).isEqualTo("1 PB");
        assertThat(FileUtils.byteCountToDisplaySize(1024L * 1024 * 1024 * 1024 * 1024 * 1024)).isEqualTo("1 EB");
        assertThat(FileUtils.byteCountToDisplaySize(Long.MAX_VALUE)).isEqualTo("7 EB");
        // Other MAX_VALUEs
        assertThat(FileUtils.byteCountToDisplaySize(Character.MAX_VALUE)).isEqualTo("63 KB");
        assertThat(FileUtils.byteCountToDisplaySize(Short.MAX_VALUE)).isEqualTo("31 KB");
        assertThat(FileUtils.byteCountToDisplaySize(Integer.MAX_VALUE)).isEqualTo("1 GB");
    }

    @Test
    public void testChecksum() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(temporaryFolder, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");

        // compute the expected checksum
        final Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text.getBytes(StandardCharsets.US_ASCII), 0, text.length());
        final long expectedValue = expectedChecksum.getValue();

        // compute the checksum of the file
        final Checksum testChecksum = new CRC32();
        final Checksum resultChecksum = FileUtils.checksum(file, testChecksum);
        final long resultValue = resultChecksum.getValue();

        assertThat(resultChecksum).isSameAs(testChecksum);
        assertThat(resultValue).isEqualTo(expectedValue);
    }

    //-----------------------------------------------------------------------
    @Test
    public void testChecksumCRC32() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(temporaryFolder, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");

        // compute the expected checksum
        final Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text.getBytes(StandardCharsets.US_ASCII), 0, text.length());
        final long expectedValue = expectedChecksum.getValue();

        // compute the checksum of the file
        final long resultValue = FileUtils.checksumCRC32(file);

        assertThat(resultValue).isEqualTo(expectedValue);
    }

    @Test
    public void testChecksumDouble() throws Exception {
        // create a test file
        final String text1 = "Imagination is more important than knowledge - Einstein";
        final File file1 = new File(temporaryFolder, "checksum-test.txt");
        FileUtils.writeStringToFile(file1, text1, "US-ASCII");

        // create a second test file
        final String text2 = "To be or not to be - Shakespeare";
        final File file2 = new File(temporaryFolder, "checksum-test2.txt");
        FileUtils.writeStringToFile(file2, text2, "US-ASCII");

        // compute the expected checksum
        final Checksum expectedChecksum = new CRC32();
        expectedChecksum.update(text1.getBytes(StandardCharsets.US_ASCII), 0, text1.length());
        expectedChecksum.update(text2.getBytes(StandardCharsets.US_ASCII), 0, text2.length());
        final long expectedValue = expectedChecksum.getValue();

        // compute the checksum of the file
        final Checksum testChecksum = new CRC32();
        FileUtils.checksum(file1, testChecksum);
        FileUtils.checksum(file2, testChecksum);
        final long resultValue = testChecksum.getValue();

        assertThat(resultValue).isEqualTo(expectedValue);
    }

    @Test
    public void testChecksumOnDirectory() throws Exception {
        try {
            FileUtils.checksum(new File("."), new CRC32());
            fail();
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testChecksumOnNullChecksum() throws Exception {
        // create a test file
        final String text = "Imagination is more important than knowledge - Einstein";
        final File file = new File(temporaryFolder, "checksum-test.txt");
        FileUtils.writeStringToFile(file, text, "US-ASCII");
        try {
            FileUtils.checksum(file, null);
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testChecksumOnNullFile() throws Exception {
        try {
            FileUtils.checksum(null, new CRC32());
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
    }

    // Compare sizes of a directory tree using long and BigInteger methods
    @Test
    public void testCompareSizeOf() {
        final File start = new File("src/test/java");
        final long sizeLong1 = FileUtils.sizeOf(start);
        final BigInteger sizeBig = FileUtils.sizeOfAsBigInteger(start);
        final long sizeLong2 = FileUtils.sizeOf(start);
        assertThat(sizeLong2).as("Size should not change").isEqualTo(sizeLong1);
        assertThat(sizeBig.longValue()).as("longSize should equal BigSize").isEqualTo(sizeLong1);
    }

    @Test
    public void testContentEquals() throws Exception {
        // Non-existent files
        final File file = new File(temporaryFolder, getName());
        final File file2 = new File(temporaryFolder, getName() + "2");
        assertThat(FileUtils.contentEquals(null, null)).isTrue();
        assertThat(FileUtils.contentEquals(null, file)).isFalse();
        assertThat(FileUtils.contentEquals(file, null)).isFalse();
        // both don't  exist
        assertThat(FileUtils.contentEquals(file, file)).isTrue();
        assertThat(FileUtils.contentEquals(file, file2)).isTrue();
        assertThat(FileUtils.contentEquals(file2, file2)).isTrue();
        assertThat(FileUtils.contentEquals(file2, file)).isTrue();

        // Directories
        assertThrows(IllegalArgumentException.class, () -> FileUtils.contentEquals(temporaryFolder, temporaryFolder));

        // Different files
        final File objFile1 =
                new File(temporaryFolder, getName() + ".object");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(
                getClass().getResource("/java/lang/Object.class"),
                objFile1);

        final File objFile1b =
                new File(temporaryFolder, getName() + ".object2");
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile(
                getClass().getResource("/java/lang/Object.class"),
                objFile1b);

        final File objFile2 =
                new File(temporaryFolder, getName() + ".collection");
        objFile2.deleteOnExit();
        FileUtils.copyURLToFile(
                getClass().getResource("/java/util/Collection.class"),
                objFile2);

        assertThat(FileUtils.contentEquals(objFile1, objFile2)).isFalse();
        assertThat(FileUtils.contentEquals(objFile1b, objFile2)).isFalse();
        assertThat(FileUtils.contentEquals(objFile1, objFile1b)).isTrue();

        assertThat(FileUtils.contentEquals(objFile1, objFile1)).isTrue();
        assertThat(FileUtils.contentEquals(objFile1b, objFile1b)).isTrue();
        assertThat(FileUtils.contentEquals(objFile2, objFile2)).isTrue();

        // Equal files
        file.createNewFile();
        file2.createNewFile();
        assertThat(FileUtils.contentEquals(file, file)).isTrue();
        assertThat(FileUtils.contentEquals(file, file2)).isTrue();
    }

    // toFiles

    @Test
    public void testContentEqualsIgnoreEOL() throws Exception {
        // Non-existent files
        final File file1 = new File(temporaryFolder, getName());
        final File file2 = new File(temporaryFolder, getName() + "2");
        assertThat(FileUtils.contentEqualsIgnoreEOL(null, null, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(null, file1, null)).isFalse();
        assertThat(FileUtils.contentEqualsIgnoreEOL(file1, null, null)).isFalse();
        // both don't  exist
        assertThat(FileUtils.contentEqualsIgnoreEOL(file1, file1, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(file1, file2, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(file2, file2, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(file2, file1, null)).isTrue();

        // Directories
        assertThrows(IllegalArgumentException.class,
            () -> FileUtils.contentEqualsIgnoreEOL(temporaryFolder, temporaryFolder, null));

        // Different files
        final File tfile1 = new File(temporaryFolder, getName() + ".txt1");
        tfile1.deleteOnExit();
        FileUtils.write(tfile1, "123\r");

        final File tfile2 = new File(temporaryFolder, getName() + ".txt2");
        tfile1.deleteOnExit();
        FileUtils.write(tfile2, "123\n");

        final File tfile3 = new File(temporaryFolder, getName() + ".collection");
        tfile3.deleteOnExit();
        FileUtils.write(tfile3, "123\r\n2");

        assertThat(FileUtils.contentEqualsIgnoreEOL(tfile1, tfile1, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(tfile2, tfile2, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(tfile3, tfile3, null)).isTrue();

        assertThat(FileUtils.contentEqualsIgnoreEOL(tfile1, tfile2, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(tfile1, tfile3, null)).isFalse();
        assertThat(FileUtils.contentEqualsIgnoreEOL(tfile2, tfile3, null)).isFalse();

        final URL urlCR = getClass().getResource("FileUtilsTestDataCR.dat");
        assertThat(urlCR).isNotNull();
        final File cr = new File(urlCR.toURI());
        assertThat(cr.exists()).isTrue();

        final URL urlCRLF = getClass().getResource("FileUtilsTestDataCRLF.dat");
        assertThat(urlCRLF).isNotNull();
        final File crlf = new File(urlCRLF.toURI());
        assertThat(crlf.exists()).isTrue();

        final URL urlLF = getClass().getResource("FileUtilsTestDataLF.dat");
        assertThat(urlLF).isNotNull();
        final File lf = new File(urlLF.toURI());
        assertThat(lf.exists()).isTrue();

        assertThat(FileUtils.contentEqualsIgnoreEOL(cr, cr, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(crlf, crlf, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(lf, lf, null)).isTrue();

        assertThat(FileUtils.contentEqualsIgnoreEOL(cr, crlf, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(cr, lf, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(crlf, lf, null)).isTrue();

        // Check the files behave OK when EOL is not ignored
        assertThat(FileUtils.contentEquals(cr, cr)).isTrue();
        assertThat(FileUtils.contentEquals(crlf, crlf)).isTrue();
        assertThat(FileUtils.contentEquals(lf, lf)).isTrue();

        assertThat(FileUtils.contentEquals(cr, crlf)).isFalse();
        assertThat(FileUtils.contentEquals(cr, lf)).isFalse();
        assertThat(FileUtils.contentEquals(crlf, lf)).isFalse();

        // Equal files
        file1.createNewFile();
        file2.createNewFile();
        assertThat(FileUtils.contentEqualsIgnoreEOL(file1, file1, null)).isTrue();
        assertThat(FileUtils.contentEqualsIgnoreEOL(file1, file2, null)).isTrue();
    }

    @Test
    public void testCopyDirectoryExceptions() {
        //
        // NullPointerException
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, null));
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, testFile1));
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(testFile1, null));
        assertThrows(NullPointerException.class, () -> FileUtils.copyDirectory(null, new File("a")));
        //
        // IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(testFile1, new File("a")));
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(testFile1, new File("a")));
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(temporaryFolder, temporaryFolder));
        //
        // IOException
        assertThrows(IOException.class, () -> FileUtils.copyDirectory(new File("doesnt-exist"), new File("a")));
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(temporaryFolder, testFile1));
    }

    @Test
    public void testCopyDirectoryFiltered() throws Exception {
        final File grandParentDir = new File(temporaryFolder, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        final NameFileFilter filter = new NameFileFilter("parent", "child", "file3.txt");
        final File destDir = new File(temporaryFolder, "copydest");

        FileUtils.copyDirectory(grandParentDir, destDir, filter);
        final List<File> files = LIST_WALKER.list(destDir);
        assertThat(files.size()).isEqualTo(3);
        assertThat(files.get(0).getName()).isEqualTo("parent");
        assertThat(files.get(1).getName()).isEqualTo("child");
        assertThat(files.get(2).getName()).isEqualTo("file3.txt");
    }

    @Test
    public void testCopyDirectoryPreserveDates() throws Exception {
        final File source = new File(temporaryFolder, "source");
        final File sourceDirectory = new File(source, "directory");
        final File sourceFile = new File(sourceDirectory, "hello.txt");

        // Prepare source data
        source.mkdirs();
        sourceDirectory.mkdir();
        FileUtils.writeStringToFile(sourceFile, "HELLO WORLD", "UTF8");
        // Set dates in reverse order to avoid overwriting previous values
        // Also, use full seconds (arguments are in ms) close to today
        // but still highly unlikely to occur in the real world
        assertThat(setLastModifiedMillis(sourceFile, DATE3)).isTrue();
        assertThat(setLastModifiedMillis(sourceDirectory, DATE2)).isTrue();
        assertThat(setLastModifiedMillis(source, DATE1)).isTrue();

        final File target = new File(temporaryFolder, "target");
        final File targetDirectory = new File(target, "directory");
        final File targetFile = new File(targetDirectory, "hello.txt");

        // Test with preserveFileDate disabled
        // On Windows, the last modified time is copied by default.
        FileUtils.copyDirectory(source, target, false);
        assertThat(getLastModifiedMillis(target)).isNotEqualTo(DATE1);
        assertThat(getLastModifiedMillis(targetDirectory)).isNotEqualTo(DATE2);
        if (!SystemUtils.IS_OS_WINDOWS) {
            assertThat(getLastModifiedMillis(targetFile)).isNotEqualTo(DATE3);
        }
        FileUtils.deleteDirectory(target);

        // Test with preserveFileDate enabled
        FileUtils.copyDirectory(source, target, true);
        assertThat(getLastModifiedMillis(target)).isEqualTo(DATE1);
        assertThat(getLastModifiedMillis(targetDirectory)).isEqualTo(DATE2);
        assertThat(getLastModifiedMillis(targetFile)).isEqualTo(DATE3);
        FileUtils.deleteDirectory(target);

        // also if the target directory already exists (IO-190)
        target.mkdirs();
        FileUtils.copyDirectory(source, target, true);
        assertThat(getLastModifiedMillis(target)).isEqualTo(DATE1);
        assertThat(getLastModifiedMillis(targetDirectory)).isEqualTo(DATE2);
        assertThat(getLastModifiedMillis(targetFile)).isEqualTo(DATE3);
        FileUtils.deleteDirectory(target);

        // also if the target subdirectory already exists (IO-190)
        targetDirectory.mkdirs();
        FileUtils.copyDirectory(source, target, true);
        assertThat(getLastModifiedMillis(target)).isEqualTo(DATE1);
        assertThat(getLastModifiedMillis(targetDirectory)).isEqualTo(DATE2);
        assertThat(getLastModifiedMillis(targetFile)).isEqualTo(DATE3);
        FileUtils.deleteDirectory(target);
    }

    /* Test for IO-141 */
    @Test
    public void testCopyDirectoryToChild() throws Exception {
        final File grandParentDir = new File(temporaryFolder, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        final long expectedCount = LIST_WALKER.list(grandParentDir).size() +
                LIST_WALKER.list(parentDir).size();
        final long expectedSize = FileUtils.sizeOfDirectory(grandParentDir) +
                FileUtils.sizeOfDirectory(parentDir);
        FileUtils.copyDirectory(parentDir, childDir);
        assertThat(LIST_WALKER.list(grandParentDir).size()).isEqualTo(expectedCount);
        assertThat(FileUtils.sizeOfDirectory(grandParentDir)).isEqualTo(expectedSize);
        assertThat(expectedCount > 0).as("Count > 0").isTrue();
        assertThat(expectedSize > 0).as("Size > 0").isTrue();
    }

    // toURLs

    @Test
    public void testCopyDirectoryToDirectory_NonExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 1234);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 4321);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        final File actualDestDir = new File(destDir, srcDir.getName());

        FileUtils.copyDirectoryToDirectory(srcDir, destDir);

        assertThat(destDir.exists()).as("Check exists").isTrue();
        assertThat(actualDestDir.exists()).as("Check exists").isTrue();
        final long srcSize = FileUtils.sizeOfDirectory(srcDir);
        assertThat(srcSize > 0).as("Size > 0").isTrue();
        assertThat(FileUtils.sizeOfDirectory(actualDestDir)).as("Check size").isEqualTo(srcSize);
        assertThat(new File(actualDestDir, "sub/A.txt").exists()).isTrue();
        FileUtils.deleteDirectory(destDir);
    }

//   @Test public void testToURLs2() throws Exception {
//        File[] files = new File[] {
//            new File(temporaryFolder, "file1.txt"),
//            null,
//        };
//        URL[] urls = FileUtils.toURLs(files);
//
//        assertEquals(files.length, urls.length);
//        assertTrue(urls[0].toExternalForm().startsWith("file:"));
//        assertTrue(urls[0].toExternalForm().indexOf("file1.txt") > 0);
//        assertEquals(null, urls[1]);
//    }
//
//   @Test public void testToURLs3() throws Exception {
//        File[] files = null;
//        URL[] urls = FileUtils.toURLs(files);
//
//        assertEquals(0, urls.length);
//    }

    @Test
    public void testCopyDirectoryToExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 1234);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 4321);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);
        destDir.mkdirs();

        FileUtils.copyDirectory(srcDir, destDir);

        final long srcSize = FileUtils.sizeOfDirectory(srcDir);
        assertThat(srcSize > 0).as("Size > 0").isTrue();
        assertThat(FileUtils.sizeOfDirectory(destDir)).isEqualTo(srcSize);
        assertThat(new File(destDir, "sub/A.txt").exists()).isTrue();
    }

    // contentEquals

    /* Test for IO-141 */
    @Test
    public void testCopyDirectoryToGrandChild() throws Exception {
        final File grandParentDir = new File(temporaryFolder, "grandparent");
        final File parentDir = new File(grandParentDir, "parent");
        final File childDir = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        final long expectedCount = LIST_WALKER.list(grandParentDir).size() * 2;
        final long expectedSize = FileUtils.sizeOfDirectory(grandParentDir) * 2;
        FileUtils.copyDirectory(grandParentDir, childDir);
        assertThat(LIST_WALKER.list(grandParentDir).size()).isEqualTo(expectedCount);
        assertThat(FileUtils.sizeOfDirectory(grandParentDir)).isEqualTo(expectedSize);
        assertThat(expectedSize > 0).as("Size > 0").isTrue();
    }

    /* Test for IO-217 FileUtils.copyDirectoryToDirectory makes infinite loops */
    @Test
    public void testCopyDirectoryToItself() throws Exception {
        final File dir = new File(temporaryFolder, "itself");
        dir.mkdirs();
        FileUtils.copyDirectoryToDirectory(dir, dir);
        assertThat(LIST_WALKER.list(dir).size()).isEqualTo(1);
    }

    // copyURLToFile

    @Test
    public void testCopyDirectoryToNonExistingDest() throws Exception {
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 1234);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 4321);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "sub");
        subDir.mkdir();
        final File subFile = new File(subDir, "A.txt");
        FileUtils.writeStringToFile(subFile, "HELLO WORLD", "UTF8");
        final File destDir = new File(System.getProperty("java.io.tmpdir"), "tmp-FileUtilsTestCase");
        FileUtils.deleteDirectory(destDir);

        FileUtils.copyDirectory(srcDir, destDir);

        assertThat(destDir.exists()).as("Check exists").isTrue();
        final long sizeOfSrcDirectory = FileUtils.sizeOfDirectory(srcDir);
        assertThat(sizeOfSrcDirectory > 0).as("Size > 0").isTrue();
        assertThat(FileUtils.sizeOfDirectory(destDir)).as("Check size").isEqualTo(sizeOfSrcDirectory);
        assertThat(new File(destDir, "sub/A.txt").exists()).isTrue();
        FileUtils.deleteDirectory(destDir);
    }

    @Test
    public void testCopyFile1() throws Exception {
        final File destination = new File(temporaryFolder, "copy1.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        FileUtils.copyFile(testFile1, destination);
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(destination.length()).as("Check Full copy").isEqualTo(testFile1Size);
        assertThat(getLastModifiedMillis(destination)).as("Check last modified date preserved").isEqualTo(getLastModifiedMillis(testFile1));
    }

    // forceMkdir

    @Test
    public void testCopyFile1ToDir() throws Exception {
        final File directory = new File(temporaryFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        backDateFile10Minutes(testFile1);

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(destination.length()).as("Check Full copy").isEqualTo(testFile1Size);
        assertThat(FileUtils.lastModified(destination)).as("Check last modified date preserved").isEqualTo(FileUtils.lastModified(testFile1));

        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyFileToDirectory(destination, directory),
            "Should not be able to copy a file into the same directory as itself");
    }

    @Test
    public void testCopyFile2() throws Exception {
        final File destination = new File(temporaryFolder, "copy2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        FileUtils.copyFile(testFile1, destination);
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(destination.length()).as("Check Full copy").isEqualTo(testFile2Size);
        assertThat(getLastModifiedMillis(destination)).as("Check last modified date preserved").isEqualTo(getLastModifiedMillis(testFile1));
    }

    // sizeOfDirectory

    @Test
    public void testCopyFile2ToDir() throws Exception {
        final File directory = new File(temporaryFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        backDateFile10Minutes(testFile1);

        FileUtils.copyFileToDirectory(testFile1, directory);
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(destination.length()).as("Check Full copy").isEqualTo(testFile2Size);
        assertThat(FileUtils.lastModified(destination)).as("Check last modified date preserved").isEqualTo(FileUtils.lastModified(testFile1));
    }

    @Test
    public void testCopyFile2WithoutFileDatePreservation() throws Exception {
        final File destFile = new File(temporaryFolder, "copy2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        // destination file time should not be less than this (allowing for granularity)
        final long nowMillis = System.currentTimeMillis() - 1000L;
        // On Windows, the last modified time is copied by default.
        FileUtils.copyFile(testFile1, destFile, false);
        assertThat(destFile.exists()).as("Check Exist").isTrue();
        assertThat(destFile.length()).as("Check Full copy").isEqualTo(testFile1Size);
        final long destLastModMillis = getLastModifiedMillis(destFile);
        final long unexpectedMillis = getLastModifiedMillis(testFile1);
        if (!SystemUtils.IS_OS_WINDOWS) {
            final long deltaMillis = destLastModMillis - unexpectedMillis;
            assertThat(destLastModMillis).as("Check last modified date not same as input, delta " + deltaMillis).isNotEqualTo(unexpectedMillis);
            assertThat(destLastModMillis > nowMillis).as(destLastModMillis + " > " + nowMillis + " (delta " + deltaMillis + ")").isTrue();
        }
    }

    @Test
    @Disabled
    public void testCopyFileLarge() throws Exception {

        final File largeFile = new File(temporaryFolder, "large.txt");
        final File destination = new File(temporaryFolder, "copylarge.txt");

        System.out.println("START:   " + new java.util.Date());
        if (!largeFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + largeFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(largeFile));
        try {
            TestUtils.generateTestData(output, FileUtils.ONE_GB);
        } finally {
            IOUtils.closeQuietly(output);
        }
        System.out.println("CREATED: " + new java.util.Date());
        FileUtils.copyFile(largeFile, destination);
        System.out.println("COPIED:  " + new java.util.Date());

        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(destination.length()).as("Check Full copy").isEqualTo(largeFile.length());
    }

    @Test
    public void testCopyFileToOutputStream() throws Exception {
        final ByteArrayOutputStream destination = new ByteArrayOutputStream();
        FileUtils.copyFile(testFile1, destination);
        assertThat(destination.size()).as("Check Full copy size").isEqualTo(testFile1Size);
        final byte[] expected = FileUtils.readFileToByteArray(testFile1);
        assertThat(destination.toByteArray()).as("Check Full copy").containsExactly(expected);
    }

    @Test
    public void testCopyToDirectoryWithDirectory() throws IOException {
        final File destDirectory = new File(temporaryFolder, "destination");
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }

        // Create a test directory
        final File inputDirectory = new File(temporaryFolder, "input");
        if (!inputDirectory.exists()) {
            inputDirectory.mkdirs();
        }
        final File outputDirDestination = new File(destDirectory, inputDirectory.getName());
        FileUtils.copyToDirectory(testFile1, inputDirectory);
        final File destFile1 = new File(outputDirDestination, testFile1.getName());
        FileUtils.copyToDirectory(testFile2, inputDirectory);
        final File destFile2 = new File(outputDirDestination, testFile2.getName());

        FileUtils.copyToDirectory(inputDirectory, destDirectory);

        // Check the directory was created
        assertThat(outputDirDestination.exists()).as("Check Exists").isTrue();
        assertThat(outputDirDestination.isDirectory()).as("Check Directory").isTrue();

        // Check each file
        assertThat(destFile1.exists()).as("Check Exists").isTrue();
        assertThat(destFile1.length()).as("Check Full Copy").isEqualTo(testFile1Size);
        assertThat(destFile2.exists()).as("Check Exists").isTrue();
        assertThat(destFile2.length()).as("Check Full Copy").isEqualTo(testFile2Size);
    }

    @Test
    public void testCopyToDirectoryWithFile() throws IOException {
        final File directory = new File(temporaryFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        final File destination = new File(directory, testFile1.getName());

        FileUtils.copyToDirectory(testFile1, directory);
        assertThat(destination.exists()).as("Check Exists").isTrue();
        assertThat(destination.length()).as("Check Full Copy").isEqualTo(testFile1Size);
    }

    @Test
    public void testCopyToDirectoryWithFileSourceDoesNotExist() {
        assertThrows(IOException.class,
                () -> FileUtils.copyToDirectory(new File(temporaryFolder, "doesNotExists"), temporaryFolder));
    }

    // copyFile

    @Test
    public void testCopyToDirectoryWithFileSourceIsNull() {
        assertThrows(NullPointerException.class, () -> FileUtils.copyToDirectory((File) null, temporaryFolder));
    }

    @Test
    public void testCopyToDirectoryWithIterable() throws IOException {
        final File directory = new File(temporaryFolder, "subdir");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        final List<File> input = new ArrayList<>();
        input.add(testFile1);
        input.add(testFile2);

        final File destFile1 = new File(directory, testFile1.getName());
        final File destFile2 = new File(directory, testFile2.getName());

        FileUtils.copyToDirectory(input, directory);
        // Check each file
        assertThat(destFile1.exists()).as("Check Exists").isTrue();
        assertThat(destFile1.length()).as("Check Full Copy").isEqualTo(testFile1Size);
        assertThat(destFile2.exists()).as("Check Exists").isTrue();
        assertThat(destFile2.length()).as("Check Full Copy").isEqualTo(testFile2Size);
    }

    @Test
    public void testCopyToDirectoryWithIterableSourceDoesNotExist() {
        assertThrows(IOException.class,
                () -> FileUtils.copyToDirectory(Collections.singleton(new File(temporaryFolder, "doesNotExists")),
                        temporaryFolder));
    }

    @Test
    public void testCopyToDirectoryWithIterableSourceIsNull() {
        assertThrows(NullPointerException.class, () -> FileUtils.copyToDirectory((List<File>) null, temporaryFolder));
    }

    @Test
    public void testCopyToSelf() throws Exception {
        final File destination = new File(temporaryFolder, "copy3.txt");
        //Prepare a test file
        FileUtils.copyFile(testFile1, destination);
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyFile(destination, destination));
    }

    @Test
    public void testCopyURLToFile() throws Exception {
        // Creates file
        final File file = new File(temporaryFolder, getName());
        file.deleteOnExit();

        // Loads resource
        final String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile(getClass().getResource(resourceName), file);

        // Tests that resuorce was copied correctly
        try (FileInputStream fis = new FileInputStream(file)) {
            assertThat(IOUtils.contentEquals(getClass().getResourceAsStream(resourceName), fis)).as("Content is not equal.").isTrue();
        }
        //TODO Maybe test copy to itself like for copyFile()
    }

    @Test
    public void testCopyURLToFileWithTimeout() throws Exception {
        // Creates file
        final File file = new File(temporaryFolder, "testCopyURLToFileWithTimeout");
        file.deleteOnExit();

        // Loads resource
        final String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile(getClass().getResource(resourceName), file, 500, 500);

        // Tests that resuorce was copied correctly
        try (FileInputStream fis = new FileInputStream(file)) {
            assertThat(IOUtils.contentEquals(getClass().getResourceAsStream(resourceName), fis)).as("Content is not equal.").isTrue();
        }
        //TODO Maybe test copy to itself like for copyFile()
    }

    @Test
    public void testDecodeUrl() {
        assertThat(FileUtils.decodeUrl("")).isEqualTo("");
        assertThat(FileUtils.decodeUrl("foo")).isEqualTo("foo");
        assertThat(FileUtils.decodeUrl("+")).isEqualTo("+");
        assertThat(FileUtils.decodeUrl("%25%20")).isEqualTo("% ");
        assertThat(FileUtils.decodeUrl("%2520")).isEqualTo("%20");
        assertThat(FileUtils
                .decodeUrl("jar:file:/C:/dir/sub%20dir/1.0/foo-1.0.jar!/org/Bar.class")).isEqualTo("jar:file:/C:/dir/sub dir/1.0/foo-1.0.jar!/org/Bar.class");
    }

    @Test
    public void testDecodeUrlEncodingUtf8() {
        assertThat(FileUtils.decodeUrl("%C3%A4%C3%B6%C3%BC%C3%9F")).isEqualTo("\u00E4\u00F6\u00FC\u00DF");
    }

    @Test
    public void testDecodeUrlLenient() {
        assertThat(FileUtils.decodeUrl(" ")).isEqualTo(" ");
        assertThat(FileUtils.decodeUrl("\u00E4\u00F6\u00FC\u00DF")).isEqualTo("\u00E4\u00F6\u00FC\u00DF");
        assertThat(FileUtils.decodeUrl("%")).isEqualTo("%");
        assertThat(FileUtils.decodeUrl("%%20")).isEqualTo("% ");
        assertThat(FileUtils.decodeUrl("%2")).isEqualTo("%2");
        assertThat(FileUtils.decodeUrl("%2G")).isEqualTo("%2G");
    }

    @Test
    public void testDecodeUrlNullSafe() {
        assertThat(FileUtils.decodeUrl(null)).isNull();
    }

    @Test
    public void testDelete() throws Exception {
        assertThat(FileUtils.delete(testFile1)).isEqualTo(testFile1);
        assertThrows(IOException.class, () -> FileUtils.delete(new File("does not exist.nope")));
    }

    @Test
    public void testDeleteDirectoryWithNonDirectory() throws Exception {
        try {
            FileUtils.deleteDirectory(testFile1);
            fail();
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testDeleteQuietlyDir() throws IOException {
        final File testDirectory = new File(temporaryFolder, "testDeleteQuietlyDir");
        final File testFile = new File(testDirectory, "testDeleteQuietlyFile");
        testDirectory.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }

        assertThat(testDirectory.exists()).isTrue();
        assertThat(testFile.exists()).isTrue();
        FileUtils.deleteQuietly(testDirectory);
        assertThat(testDirectory.exists()).as("Check No Exist").isFalse();
        assertThat(testFile.exists()).as("Check No Exist").isFalse();
    }

    @Test
    public void testDeleteQuietlyFile() throws IOException {
        final File testFile = new File(temporaryFolder, "testDeleteQuietlyFile");
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }

        assertThat(testFile.exists()).isTrue();
        FileUtils.deleteQuietly(testFile);
        assertThat(testFile.exists()).as("Check No Exist").isFalse();
    }

    @Test
    public void testDeleteQuietlyForNull() {
        try {
            FileUtils.deleteQuietly(null);
        } catch (final Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testDeleteQuietlyNonExistent() {
        final File testFile = new File("testDeleteQuietlyNonExistent");
        assertThat(testFile.exists()).isFalse();

        try {
            FileUtils.deleteQuietly(testFile);
        } catch (final Exception ex) {
            fail(ex.getMessage());
        }
    }

    /*
     *  Test the FileUtils implementation.
     */
    @Test
    public void testFileUtils() throws Exception {
        // Loads file from classpath
        final File file1 = new File(temporaryFolder, "test.txt");
        final String filename = file1.getAbsolutePath();

        //Create test file on-the-fly (used to be in CVS)
        try (OutputStream out = new FileOutputStream(file1)) {
            out.write("This is a test".getBytes(StandardCharsets.UTF_8));
        }

        final File file2 = new File(temporaryFolder, "test2.txt");

        FileUtils.writeStringToFile(file2, filename, "UTF-8");
        assertThat(file2.exists()).isTrue();
        assertThat(file2.length() > 0).isTrue();

        final String file2contents = FileUtils.readFileToString(file2, "UTF-8");
        assertThat(file2contents).as("Second file's contents correct").isEqualTo(filename);

        assertThat(file2.delete()).isTrue();

        final String contents = FileUtils.readFileToString(new File(filename), "UTF-8");
        assertThat(contents).as("FileUtils.fileRead()").isEqualTo("This is a test");

    }

    // copyToDirectory

    @Test
    public void testForceDeleteAFile1() throws Exception {
        final File destination = new File(temporaryFolder, "copy1.txt");
        destination.createNewFile();
        assertThat(destination.exists()).as("Copy1.txt doesn't exist to delete").isTrue();
        FileUtils.forceDelete(destination);
        assertThat(destination.exists()).as("Check No Exist").isFalse();
    }

    @Test
    public void testForceDeleteAFile2() throws Exception {
        final File destination = new File(temporaryFolder, "copy2.txt");
        destination.createNewFile();
        assertThat(destination.exists()).as("Copy2.txt doesn't exist to delete").isTrue();
        FileUtils.forceDelete(destination);
        assertThat(destination.exists()).as("Check No Exist").isFalse();
    }

    @Test
    public void testForceDeleteAFile3() throws Exception {
        final File destination = new File(temporaryFolder, "no_such_file");
        assertThat(destination.exists()).as("Check No Exist").isFalse();
        try {
            FileUtils.forceDelete(destination);
            fail("Should generate FileNotFoundException");
        } catch (final FileNotFoundException ignored) {
        }
    }

    @Test
    public void testForceDeleteDir() throws Exception {
        final File testDirectory = temporaryFolder;
        assertThat(testDirectory.exists()).as("TestDirectory must exist").isTrue();
        FileUtils.forceDelete(testDirectory);
        assertThat(testDirectory.exists()).as("TestDirectory must not exist").isFalse();
    }

    @Test
    public void testForceDeleteReadOnlyFile() throws Exception {
        File destination = File.createTempFile("test-", ".txt");
        assertThat(destination.setReadOnly()).isTrue();
        assertThat(destination.canRead()).isTrue();
        assertThat(destination.canWrite()).isFalse();
        // sanity check that File.delete() in deletes read-only files.
        assertThat(destination.delete()).isTrue();
        destination = File.createTempFile("test-", ".txt");
        // real test
        assertThat(destination.setReadOnly()).isTrue();
        assertThat(destination.canRead()).isTrue();
        assertThat(destination.canWrite()).isFalse();
        assertThat(destination.exists()).as("File doesn't exist to delete").isTrue();
        FileUtils.forceDelete(destination);
        assertThat(destination.exists()).as("Check deletion").isFalse();
    }

    @Test
    public void testForceMkdir() throws Exception {
        // Tests with existing directory
        FileUtils.forceMkdir(temporaryFolder);

        // Creates test file
        final File testFile = new File(temporaryFolder, getName());
        testFile.deleteOnExit();
        testFile.createNewFile();
        assertThat(testFile.exists()).as("Test file does not exist.").isTrue();

        // Tests with existing file
        assertThrows(IOException.class, () -> FileUtils.forceMkdir(testFile));

        testFile.delete();

        // Tests with non-existent directory
        FileUtils.forceMkdir(testFile);
        assertThat(testFile.exists()).as("Directory was not created.").isTrue();
    }

    @Test
    public void testForceMkdirParent() throws Exception {
        // Tests with existing directory
        assertThat(temporaryFolder.exists()).isTrue();
        final File testParentDir = new File(temporaryFolder, "testForceMkdirParent");
        testParentDir.delete();
        assertThat(testParentDir.exists()).isFalse();
        final File testFile = new File(testParentDir, "test.txt");
        assertThat(testParentDir.exists()).isFalse();
        assertThat(testFile.exists()).isFalse();
        // Create
        FileUtils.forceMkdirParent(testFile);
        assertThat(testParentDir.exists()).isTrue();
        assertThat(testFile.exists()).isFalse();
        // Again
        FileUtils.forceMkdirParent(testFile);
        assertThat(testParentDir.exists()).isTrue();
        assertThat(testFile.exists()).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test
    public void testGetFile() {
        final File expected_A = new File("src");
        final File expected_B = new File(expected_A, "main");
        final File expected_C = new File(expected_B, "java");
        assertThat(FileUtils.getFile("src")).as("A").isEqualTo(expected_A);
        assertThat(FileUtils.getFile("src", "main")).as("B").isEqualTo(expected_B);
        assertThat(FileUtils.getFile("src", "main", "java")).as("C").isEqualTo(expected_C);
        try {
            FileUtils.getFile((String[]) null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException e) {
            // expected
        }
    }

    // forceDelete

    @Test
    public void testGetFile_Parent() {
        final File parent = new File("parent");
        final File expected_A = new File(parent, "src");
        final File expected_B = new File(expected_A, "main");
        final File expected_C = new File(expected_B, "java");
        assertThat(FileUtils.getFile(parent, "src")).as("A").isEqualTo(expected_A);
        assertThat(FileUtils.getFile(parent, "src", "main")).as("B").isEqualTo(expected_B);
        assertThat(FileUtils.getFile(parent, "src", "main", "java")).as("C").isEqualTo(expected_C);
        try {
            FileUtils.getFile(parent, (String[]) null);
            fail("Expected NullPointerException");
        } catch (final NullPointerException e) {
            // expected
        }
        try {
            FileUtils.getFile((File) null, "src");
            fail("Expected NullPointerException");
        } catch (final NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetTempDirectory() {
        final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
        assertThat(FileUtils.getTempDirectory()).isEqualTo(tempDirectory);
    }

    @Test
    public void testGetTempDirectoryPath() {
        assertThat(FileUtils.getTempDirectoryPath()).isEqualTo(System.getProperty("java.io.tmpdir"));
    }

    // copyFileToDirectory

    @Test
    public void testGetUserDirectory() {
        final File userDirectory = new File(System.getProperty("user.home"));
        assertThat(FileUtils.getUserDirectory()).isEqualTo(userDirectory);
    }

    @Test
    public void testGetUserDirectoryPath() {
        assertThat(FileUtils.getUserDirectoryPath()).isEqualTo(System.getProperty("user.home"));
    }

    // forceDelete

    // This test relies on FileUtils.copyFile using File.length to check the output size
    @Test
    public void testIncorrectOutputSize() {
        final File inFile = new File("pom.xml");
        final File outFile = new ShorterFile("target/pom.tmp"); // it will report a shorter file
        try {
            FileUtils.copyFile(inFile, outFile);
            fail("Expected IOException");
        } catch (final Exception e) {
            final String msg = e.toString();
            assertThat(msg.contains("Failed to copy full contents")).as(msg).isTrue();
        } finally {
            outFile.delete(); // tidy up
        }
    }

    @Test
    public void testIO276() throws Exception {
        final File dir = new File("target", "IO276");
        assertThat(dir.mkdirs()).as(dir + " should not be present").isTrue();
        final File file = new File(dir, "IO276.txt");
        assertThat(file.createNewFile()).as(file + " should not be present").isTrue();
        FileUtils.forceDeleteOnExit(dir);
        // If this does not work, test will fail next time (assuming target is not cleaned)
    }

    @Test
    public void testIO300() {
        final File testDirectory = temporaryFolder;
        final File src = new File(testDirectory, "dir1");
        final File dest = new File(src, "dir2");
        assertThat(dest.mkdirs()).isTrue();
        assertThat(src.exists()).isTrue();
        try {
            FileUtils.moveDirectoryToDirectory(src, dest, false);
            fail("expected IOException");
        } catch (final IOException ioe) {
            // expected
        }
        assertThat(src.exists()).isTrue();
    }

    @Test
    public void testIsEmptyDirectory() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        final File tempDirAsFile = tempDir.toFile();
        try {
            assertThat(FileUtils.isEmptyDirectory(tempDirAsFile)).isTrue();
        } finally {
            Files.delete(tempDir);
        }
        assertThat(FileUtils.isEmptyDirectory(PathUtilsIsEmptyTest.DIR_SIZE_1.toFile())).isFalse();
    }

    // isFileNewer / isFileOlder
    @Test
    public void testIsFileNewerOlder() throws Exception {
        final File reference = new File(temporaryFolder, "FileUtils-reference.txt");
        final File oldFile = new File(temporaryFolder, "FileUtils-old.txt");
        final File newFile = new File(temporaryFolder, "FileUtils-new.txt");
        final File invalidFile = new File(temporaryFolder, "FileUtils-invalid-file.txt");

        // Create Files
        if (!oldFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + oldFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(oldFile));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }

        do {
            try {
                TestUtils.sleep(1000);
            } catch (final InterruptedException ie) {
                // ignore
            }
            if (!reference.getParentFile().exists()) {
                throw new IOException("Cannot create file " + reference
                        + " as the parent directory does not exist");
            }
            final BufferedOutputStream output =
                    new BufferedOutputStream(new FileOutputStream(reference));
            try {
                TestUtils.generateTestData(output, 0);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } while (getLastModifiedMillis(oldFile) == getLastModifiedMillis(reference));

        final Date date = new Date();
        final long now = date.getTime();
        final Instant instant = date.toInstant();
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        final LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        final LocalDate localDate = zonedDateTime.toLocalDate();
        final LocalDate localDatePlusDay = localDate.plusDays(1);
        final LocalTime localTime = LocalTime.ofSecondOfDay(0);

        do {
            try {
                TestUtils.sleep(1000);
            } catch (final InterruptedException ie) {
                // ignore
            }
            if (!newFile.getParentFile().exists()) {
                throw new IOException("Cannot create file " + newFile
                        + " as the parent directory does not exist");
            }
            final BufferedOutputStream output =
                    new BufferedOutputStream(new FileOutputStream(newFile));
            try {
                TestUtils.generateTestData(output, 0);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } while (getLastModifiedMillis(reference) == getLastModifiedMillis(newFile));

        // Test isFileNewer()
        assertThat(FileUtils.isFileNewer(oldFile, reference)).as("Old File - Newer - File").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, date)).as("Old File - Newer - Date").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, now)).as("Old File - Newer - Mili").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, instant)).as("Old File - Newer - Instant").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, zonedDateTime)).as("Old File - Newer - ZonedDateTime").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, localDateTime)).as("Old File - Newer - LocalDateTime").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, localDateTime, ZoneId.systemDefault())).as("Old File - Newer - LocalDateTime,ZoneId").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, localDate)).as("Old File - Newer - LocalDate").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, localDate, localTime)).as("Old File - Newer - LocalDate,ZoneId").isTrue();
        assertThat(FileUtils.isFileNewer(oldFile, localDatePlusDay)).as("Old File - Newer - LocalDate plus one day").isFalse();
        assertThat(FileUtils.isFileNewer(oldFile, localDatePlusDay, localTime)).as("Old File - Newer - LocalDate plus one day,ZoneId").isFalse();

        assertThat(FileUtils.isFileNewer(newFile, reference)).as("New File - Newer - File").isTrue();
        assertThat(FileUtils.isFileNewer(newFile, date)).as("New File - Newer - Date").isTrue();
        assertThat(FileUtils.isFileNewer(newFile, now)).as("New File - Newer - Mili").isTrue();
        assertThat(FileUtils.isFileNewer(newFile, instant)).as("New File - Newer - Instant").isTrue();
        assertThat(FileUtils.isFileNewer(newFile, zonedDateTime)).as("New File - Newer - ZonedDateTime").isTrue();
        assertThat(FileUtils.isFileNewer(newFile, localDateTime)).as("New File - Newer - LocalDateTime").isTrue();
        assertThat(FileUtils.isFileNewer(newFile, localDateTime, ZoneId.systemDefault())).as("New File - Newer - LocalDateTime,ZoneId").isTrue();
        assertThat(FileUtils.isFileNewer(newFile, localDate)).as("New File - Newer - LocalDate").isFalse();
        assertThat(FileUtils.isFileNewer(newFile, localDate, localTime)).as("New File - Newer - LocalDate,ZoneId").isTrue();
        assertThat(FileUtils.isFileNewer(newFile, localDatePlusDay)).as("New File - Newer - LocalDate plus one day").isFalse();
        assertThat(FileUtils.isFileNewer(newFile, localDatePlusDay, localTime)).as("New File - Newer - LocalDate plus one day,ZoneId").isFalse();
        assertThat(FileUtils.isFileNewer(invalidFile, reference)).as("Invalid - Newer - File").isFalse();
        final String invalidFileName = invalidFile.getName();
        try {
            FileUtils.isFileNewer(newFile, invalidFile);
            fail("Should have cause IllegalArgumentException");
        } catch (final IllegalArgumentException iae) {
            final String message = iae.getMessage();
            assertThat(message.contains(invalidFileName)).as("Message should contain: " + invalidFileName + " but was: " + message).isTrue();
        }

        // Test isFileOlder()
        assertThat(FileUtils.isFileOlder(oldFile, reference)).as("Old File - Older - File").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, date)).as("Old File - Older - Date").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, now)).as("Old File - Older - Mili").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, instant)).as("Old File - Older - Instant").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, zonedDateTime)).as("Old File - Older - ZonedDateTime").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, localDateTime)).as("Old File - Older - LocalDateTime").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, localDateTime, ZoneId.systemDefault())).as("Old File - Older - LocalDateTime,LocalTime").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, localDate)).as("Old File - Older - LocalDate").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, localDate, localTime)).as("Old File - Older - LocalDate,ZoneId").isFalse();
        assertThat(FileUtils.isFileOlder(oldFile, localDatePlusDay)).as("Old File - Older - LocalDate plus one day").isTrue();
        assertThat(FileUtils.isFileOlder(oldFile, localDatePlusDay, localTime)).as("Old File - Older - LocalDate plus one day,LocalTime").isTrue();

        assertThat(FileUtils.isFileOlder(newFile, reference)).as("New File - Older - File").isFalse();
        assertThat(FileUtils.isFileOlder(newFile, date)).as("New File - Older - Date").isFalse();
        assertThat(FileUtils.isFileOlder(newFile, now)).as("New File - Older - Mili").isFalse();
        assertThat(FileUtils.isFileOlder(newFile, instant)).as("New File - Older - Instant").isFalse();
        assertThat(FileUtils.isFileOlder(newFile, zonedDateTime)).as("New File - Older - ZonedDateTime").isFalse();
        assertThat(FileUtils.isFileOlder(newFile, localDateTime)).as("New File - Older - LocalDateTime").isFalse();
        assertThat(FileUtils.isFileOlder(newFile, localDateTime, ZoneId.systemDefault())).as("New File - Older - LocalDateTime,ZoneId").isFalse();
        assertThat(FileUtils.isFileOlder(newFile, localDate)).as("New File - Older - LocalDate").isTrue();
        assertThat(FileUtils.isFileOlder(newFile, localDate, localTime)).as("New File - Older - LocalDate,LocalTime").isFalse();
        assertThat(FileUtils.isFileOlder(newFile, localDatePlusDay)).as("New File - Older - LocalDate plus one day").isTrue();
        assertThat(FileUtils.isFileOlder(newFile, localDatePlusDay, localTime)).as("New File - Older - LocalDate plus one day,LocalTime").isTrue();

        assertThat(FileUtils.isFileOlder(invalidFile, reference)).as("Invalid - Older - File").isFalse();
        assertThrows(IllegalArgumentException.class, () -> FileUtils.isFileOlder(newFile, invalidFile));
        try {
            FileUtils.isFileOlder(newFile, invalidFile);
            fail("Should have cause IllegalArgumentException");
        } catch (final IllegalArgumentException iae) {
            final String message = iae.getMessage();
            assertThat(message.contains(invalidFileName)).as("Message should contain: " + invalidFileName + " but was: " + message).isTrue();
        }


        // ----- Test isFileNewer() exceptions -----
        // Null File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(null, now));

        // Null reference File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(oldFile, (File) null));

        // Invalid reference File
        assertThrows(IllegalArgumentException.class, () -> FileUtils.isFileNewer(oldFile, invalidFile));

        // Null reference Date
        assertThrows(NullPointerException.class, () -> FileUtils.isFileNewer(oldFile, (Date) null));

        // ----- Test isFileOlder() exceptions -----
        // Null File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(null, now));

        // Null reference File
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(oldFile, (File) null));

        // Null reference Date
        assertThrows(NullPointerException.class, () -> FileUtils.isFileOlder(oldFile, (Date) null));

        // Invalid reference File
        assertThrows(IllegalArgumentException.class, () -> FileUtils.isFileOlder(oldFile, invalidFile));
    }

    @Test
    public void testIsDirectory() throws IOException {
        assertThat(FileUtils.isDirectory(null)).isFalse();

        assertThat(FileUtils.isDirectory(temporaryFolder)).isTrue();
        assertThat(FileUtils.isDirectory(testFile1)).isFalse();

        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        final File tempDirAsFile = tempDir.toFile();
        Files.delete(tempDir);
        assertThat(FileUtils.isDirectory(tempDirAsFile)).isFalse();
    }

    @Test
    public void testIsRegularFile() throws IOException {
        assertThat(FileUtils.isRegularFile(null)).isFalse();

        assertThat(FileUtils.isRegularFile(temporaryFolder)).isFalse();
        assertThat(FileUtils.isRegularFile(testFile1)).isTrue();

        Files.delete(testFile1.toPath());
        assertThat(FileUtils.isRegularFile(testFile1)).isFalse();
    }

    @Test
    public void testIterateFiles() throws Exception {
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "list_test");
        final File subSubDir = new File(subDir, "subSubDir");
        final File notSubSubDir = new File(subDir, "notSubSubDir");
        assertThat(subDir.mkdir()).isTrue();
        assertThat(subSubDir.mkdir()).isTrue();
        assertThat(notSubSubDir.mkdir()).isTrue();
        Iterator<File> iterator = null;
        try {
            // Need list to be appendable
            final List<String> expectedFileNames = new ArrayList<>(
                Arrays.asList("a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt"));
            final int[] fileSizes = {123, 234, 345, 456, 678, 789};
            assertThat(fileSizes.length).isEqualTo(expectedFileNames.size());
            Collections.sort(expectedFileNames);
            Arrays.sort(fileSizes);
            for (int i = 0; i < fileSizes.length; ++i) {
                TestUtils.generateTestData(new File(subDir, expectedFileNames.get(i)), fileSizes[i]);
            }
            //
            final String subSubFileName = "z.txt";
            TestUtils.generateTestData(new File(subSubDir, subSubFileName), 1);
            expectedFileNames.add(subSubFileName);
            //
            final String notSubSubFileName = "not.txt";
            TestUtils.generateTestData(new File(notSubSubDir, notSubSubFileName), 1);

            final WildcardFileFilter allFilesFileFilter = new WildcardFileFilter("*.*");
            final NameFileFilter dirFilter = new NameFileFilter("subSubDir");
            iterator = FileUtils.iterateFiles(subDir, allFilesFileFilter, dirFilter);

            final Map<String, String> matchedFileNames = new HashMap<>();
            final List<String> actualFileNames = new ArrayList<>();

            while (iterator.hasNext()) {
                boolean found = false;
                final String fileName = iterator.next().getName();
                actualFileNames.add(fileName);

                for (int j = 0; !found && j < expectedFileNames.size(); ++j) {
                    final String expectedFileName = expectedFileNames.get(j);
                    if (expectedFileName.equals(fileName)) {
                        matchedFileNames.put(expectedFileName, expectedFileName);
                        found = true;
                    }
                }
            }
            assertThat(matchedFileNames.size()).isEqualTo(expectedFileNames.size());
            Collections.sort(actualFileNames);
            assertThat(actualFileNames).isEqualTo(expectedFileNames);
        } finally {
            consumeRemaining(iterator);
            notSubSubDir.delete();
            subSubDir.delete();
            subDir.delete();
        }
    }

    @Test
    public void testIterateFilesAndDirs() throws IOException {
        final File srcDir = temporaryFolder;
        // temporaryFolder/srcDir
        // - subdir1
        // -- subdir2
        // --- a.txt
        // --- subdir3
        // --- subdir4
        final File subDir1 = new File(srcDir, "subdir1");
        final File subDir2 = new File(subDir1, "subdir2");
        final File subDir3 = new File(subDir2, "subdir3");
        final File subDir4 = new File(subDir2, "subdir4");
        assertThat(subDir1.mkdir()).isTrue();
        assertThat(subDir2.mkdir()).isTrue();
        assertThat(subDir3.mkdir()).isTrue();
        assertThat(subDir4.mkdir()).isTrue();
        final File someFile = new File(subDir2, "a.txt");
        final WildcardFileFilter fileFilterAllFiles = new WildcardFileFilter("*.*");
        final WildcardFileFilter fileFilterAllDirs = new WildcardFileFilter("*");
        final WildcardFileFilter fileFilterExtTxt = new WildcardFileFilter("*.txt");
        try {
            try (final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(someFile))) {
                TestUtils.generateTestData(output, 100);
            }
            //
            // "*.*" and "*"
            Collection<File> expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile, subDir3, subDir4);
            iterateFilesAndDirs(subDir1, fileFilterAllFiles, fileFilterAllDirs, expectedFilesAndDirs);
            //
            // "*.txt" and "*"
            final int filesCount;
            expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile, subDir3, subDir4);
            iterateFilesAndDirs(subDir1, fileFilterExtTxt, fileFilterAllDirs, expectedFilesAndDirs);
            //
            // "*.*" and "subdir2"
            expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile);
            iterateFilesAndDirs(subDir1, fileFilterAllFiles, new NameFileFilter("subdir2"), expectedFilesAndDirs);
            //
            // "*.txt" and "subdir2"
            expectedFilesAndDirs = Arrays.asList(subDir1, subDir2, someFile);
            iterateFilesAndDirs(subDir1, fileFilterExtTxt, new NameFileFilter("subdir2"), expectedFilesAndDirs);
        } finally {
            someFile.delete();
            subDir4.delete();
            subDir3.delete();
            subDir2.delete();
            subDir1.delete();
        }
    }

    @Test
    public void testListFiles() throws Exception {
        final File srcDir = temporaryFolder;
        final File subDir = new File(srcDir, "list_test");
        final File subDir2 = new File(subDir, "subdir");
        subDir.mkdir();
        subDir2.mkdir();
        try {

            final String[] expectedFileNames = {"a.txt", "b.txt", "c.txt", "d.txt", "e.txt", "f.txt"};
            final int[] fileSizes = {123, 234, 345, 456, 678, 789};

            for (int i = 0; i < expectedFileNames.length; ++i) {
                final File theFile = new File(subDir, expectedFileNames[i]);
                if (!theFile.getParentFile().exists()) {
                    throw new IOException("Cannot create file " + theFile + " as the parent directory does not exist");
                }
                final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(theFile));
                try {
                    TestUtils.generateTestData(output, fileSizes[i]);
                } finally {
                    IOUtils.closeQuietly(output);
                }
            }

            final Collection<File> actualFiles = FileUtils.listFiles(subDir, new WildcardFileFilter("*.*"),
                new WildcardFileFilter("*"));

            final int count = actualFiles.size();
            final Object[] fileObjs = actualFiles.toArray();

            assertThat(actualFiles.size()).withFailMessage(() -> actualFiles.toString()).isEqualTo(expectedFileNames.length);

            final Map<String, String> foundFileNames = new HashMap<>();

            for (int i = 0; i < count; ++i) {
                boolean found = false;
                for (int j = 0; !found && j < expectedFileNames.length; ++j) {
                    if (expectedFileNames[j].equals(((File) fileObjs[i]).getName())) {
                        foundFileNames.put(expectedFileNames[j], expectedFileNames[j]);
                        found = true;
                    }
                }
            }

            assertThat(expectedFileNames.length).withFailMessage(() -> foundFileNames.toString()).isEqualTo(foundFileNames.size());
        } finally {
            subDir.delete();
        }
    }

    @Test
    public void testListFilesWithDirs() throws IOException {
        final File srcDir = temporaryFolder;

        final File subDir1 = new File(srcDir, "subdir");
        final File subDir2 = new File(subDir1, "subdir2");
        subDir1.mkdir();
        subDir2.mkdir();
        try {
            final File someFile = new File(subDir2, "a.txt");
            if (!someFile.getParentFile().exists()) {
                throw new IOException("Cannot create file " + someFile + " as the parent directory does not exist");
            }
            final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(someFile));
            try {
                TestUtils.generateTestData(output, 100);
            } finally {
                IOUtils.closeQuietly(output);
            }

            final File subDir3 = new File(subDir2, "subdir3");
            subDir3.mkdir();

            final Collection<File> files = FileUtils.listFilesAndDirs(subDir1, new WildcardFileFilter("*.*"),
                new WildcardFileFilter("*"));

            assertThat(files.size()).isEqualTo(4);
            assertThat(files.contains(subDir1)).as("Should contain the directory.").isTrue();
            assertThat(files.contains(subDir2)).as("Should contain the directory.").isTrue();
            assertThat(files.contains(someFile)).as("Should contain the file.").isTrue();
            assertThat(files.contains(subDir3)).as("Should contain the directory.").isTrue();
        } finally {
            subDir1.delete();
        }
    }

    @Test
    public void testMoveDirectory_CopyDelete() throws Exception {

        final File dir = temporaryFolder;
        final File src = new File(dir, "testMoveDirectory2Source") {
            private static final long serialVersionUID = 1L;

            // Force renameTo to fail
            @Override
            public boolean renameTo(final File dest) {
                return false;
            }
        };
        final File testDir = new File(src, "foo");
        final File testFile = new File(testDir, "bar");
        testDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File destination = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destination);

        // Move the directory
        FileUtils.moveDirectory(src, destination);

        // Check results
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(src.exists()).as("Original deleted").isFalse();
        final File movedDir = new File(destination, testDir.getName());
        final File movedFile = new File(movedDir, testFile.getName());
        assertThat(movedDir.exists()).as("Check dir moved").isTrue();
        assertThat(movedFile.exists()).as("Check file moved").isTrue();
    }

    @Test
    public void testMoveDirectory_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectory(null, new File("foo")));
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectory(new File("foo"), null));
        try {
            FileUtils.moveDirectory(new File("nonexistant"), new File("foo"));
            fail("Expected FileNotFoundException for source");
        } catch (final FileNotFoundException e) {
            // expected
        }
        final File testFile = new File(temporaryFolder, "testMoveDirectoryFile");
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveDirectory(testFile, new File("foo")));
        final File testSrcFile = new File(temporaryFolder, "testMoveDirectorySource");
        final File testDestFile = new File(temporaryFolder, "testMoveDirectoryDest");
        testSrcFile.mkdir();
        testDestFile.mkdir();
        try {
            FileUtils.moveDirectory(testSrcFile, testDestFile);
            fail("Expected FileExistsException when dest already exists");
        } catch (final FileExistsException e) {
            // expected
        }
    }

    @Test
    public void testMoveDirectory_Rename() throws Exception {
        final File dir = temporaryFolder;
        final File src = new File(dir, "testMoveDirectory1Source");
        final File testDir = new File(src, "foo");
        final File testFile = new File(testDir, "bar");
        testDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File destination = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destination);

        // Move the directory
        FileUtils.moveDirectory(src, destination);

        // Check results
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(src.exists()).as("Original deleted").isFalse();
        final File movedDir = new File(destination, testDir.getName());
        final File movedFile = new File(movedDir, testFile.getName());
        assertThat(movedDir.exists()).as("Check dir moved").isTrue();
        assertThat(movedFile.exists()).as("Check file moved").isTrue();
    }

    @Test
    public void testMoveDirectoryToDirectory() throws Exception {
        final File dir = temporaryFolder;
        final File src = new File(dir, "testMoveDirectory1Source");
        final File testChildDir = new File(src, "foo");
        final File testFile = new File(testChildDir, "bar");
        testChildDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File destDir = new File(dir, "testMoveDirectory1Dest");
        FileUtils.deleteDirectory(destDir);
        assertThat(destDir.exists()).as("Check Exist before").isFalse();

        // Move the directory
        FileUtils.moveDirectoryToDirectory(src, destDir, true);

        // Check results
        assertThat(destDir.exists()).as("Check Exist after").isTrue();
        assertThat(src.exists()).as("Original deleted").isFalse();
        final File movedDir = new File(destDir, src.getName());
        final File movedChildDir = new File(movedDir, testChildDir.getName());
        final File movedFile = new File(movedChildDir, testFile.getName());
        assertThat(movedDir.exists()).as("Check dir moved").isTrue();
        assertThat(movedChildDir.exists()).as("Check child dir moved").isTrue();
        assertThat(movedFile.exists()).as("Check file moved").isTrue();
    }

    @Test
    public void testMoveDirectoryToDirectory_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectoryToDirectory(null, new File("foo"), true));
        assertThrows(NullPointerException.class, () -> FileUtils.moveDirectoryToDirectory(new File("foo"), null, true));
        final File testFile1 = new File(temporaryFolder, "testMoveFileFile1");
        final File testFile2 = new File(temporaryFolder, "testMoveFileFile2");
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1 + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 = new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2 + " as the parent directory does not exist");
        }
        final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        try {
            FileUtils.moveDirectoryToDirectory(testFile1, testFile2, true);
            fail("Expected IOException when dest not a directory");
        } catch (final IOException e) {
            // expected
        }

        final File nonexistant = new File(temporaryFolder, "testMoveFileNonExistant");
        try {
            FileUtils.moveDirectoryToDirectory(testFile1, nonexistant, false);
            fail("Expected IOException when dest does not exist and create=false");
        } catch (final IOException e) {
            // expected
        }
    }

    @Test
    public void testMoveFile_CopyDelete() throws Exception {
        final File destination = new File(temporaryFolder, "move2.txt");
        final File src = new File(testFile1.getAbsolutePath()) {
            private static final long serialVersionUID = 1L;

            // Force renameTo to fail, as if destination is on another
            // filesystem
            @Override
            public boolean renameTo(final File f) {
                return false;
            }
        };
        FileUtils.moveFile(src, destination);
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(src.exists()).as("Original deleted").isFalse();
    }

    @Test
    public void testMoveFile_CopyDelete_Failed() throws Exception {
        final File destination = new File(temporaryFolder, "move3.txt");
        final File src = new File(testFile1.getAbsolutePath()) {
            private static final long serialVersionUID = 1L;

            // Force delete failure
            @Override
            public boolean delete() {
                return false;
            }

            // Force renameTo to fail, as if destination is on another
            // filesystem
            @Override
            public boolean renameTo(final File f) {
                return false;
            }

        };
        assertThrows(IOException.class, () -> FileUtils.moveFile(src, destination));
        // expected
        assertThat(destination.exists()).as("Check Rollback").isFalse();
        assertThat(src.exists()).as("Original exists").isTrue();
    }

    @Test
    public void testMoveFile_CopyDelete_WithFileDatePreservation() throws Exception {
        final File destination = new File(temporaryFolder, "move2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        final File src = new File(testFile1.getAbsolutePath()) {
            private static final long serialVersionUID = 1L;

            // Force renameTo to fail, as if destination is on another
            // filesystem
            @Override
            public boolean renameTo(final File f) {
                return false;
            }
        };
        final long expected = getLastModifiedMillis(testFile1);

        FileUtils.moveFile(src, destination, StandardCopyOption.COPY_ATTRIBUTES);
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(src.exists()).as("Original deleted").isFalse();

        final long destLastMod = getLastModifiedMillis(destination);
        final long delta = destLastMod - expected;
        assertThat(destLastMod).as("Check last modified date same as input, delta " + delta).isEqualTo(expected);
    }

    @Test
    public void testMoveFile_CopyDelete_WithoutFileDatePreservation() throws Exception {
        final File destination = new File(temporaryFolder, "move2.txt");

        backDateFile10Minutes(testFile1); // set test file back 10 minutes

        // destination file time should not be less than this (allowing for granularity)
        final long nowMillis = System.currentTimeMillis() - 1000L;

        final File src = new File(testFile1.getAbsolutePath()) {
            private static final long serialVersionUID = 1L;

            // Force renameTo to fail, as if destination is on another
            // filesystem
            @Override
            public boolean renameTo(final File f) {
                return false;
            }
        };
        final long unexpectedMillis = getLastModifiedMillis(testFile1);

        FileUtils.moveFile(src, destination, PathUtils.EMPTY_COPY_OPTIONS);
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(src.exists()).as("Original deleted").isFalse();

        // On Windows, the last modified time is copied by default.
        if (!SystemUtils.IS_OS_WINDOWS) {
            final long destLastModMillis = getLastModifiedMillis(destination);
            final long deltaMillis = destLastModMillis - unexpectedMillis;
            assertThat(destLastModMillis).as("Check last modified date not same as input, delta " + deltaMillis).isNotEqualTo(unexpectedMillis);
            assertThat(destLastModMillis > nowMillis).as(destLastModMillis + " > " + nowMillis + " (delta " + deltaMillis + ")").isTrue();
        }
    }

    @Test
    public void testMoveFile_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveFile(null, new File("foo")));
        assertThrows(NullPointerException.class, () -> FileUtils.moveFile(new File("foo"), null));
        try {
            FileUtils.moveFile(new File("nonexistant"), new File("foo"));
            fail("Expected FileNotFoundException for source");
        } catch (final FileNotFoundException e) {
            // expected
        }
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveFile(temporaryFolder, new File("foo")));
        final File testSourceFile = new File(temporaryFolder, "testMoveFileSource");
        final File testDestFile = new File(temporaryFolder, "testMoveFileSource");
        if (!testSourceFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testSourceFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testSourceFile));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testDestFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testDestFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testDestFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        try {
            FileUtils.moveFile(testSourceFile, testDestFile);
            fail("Expected FileExistsException when dest already exists");
        } catch (final FileExistsException e) {
            // expected
        }
    }

    @Test
    public void testMoveFile_Rename() throws Exception {
        final File destination = new File(temporaryFolder, "move1.txt");

        FileUtils.moveFile(testFile1, destination);
        assertThat(destination.exists()).as("Check Exist").isTrue();
        assertThat(testFile1.exists()).as("Original deleted").isFalse();
    }

    @Test
    public void testMoveFileToDirectory() throws Exception {
        final File destDir = new File(temporaryFolder, "moveFileDestDir");
        final File movedFile = new File(destDir, testFile1.getName());
        assertThat(destDir.exists()).as("Check Exist before").isFalse();
        assertThat(movedFile.exists()).as("Check Exist before").isFalse();

        FileUtils.moveFileToDirectory(testFile1, destDir, true);
        assertThat(movedFile.exists()).as("Check Exist after").isTrue();
        assertThat(testFile1.exists()).as("Original deleted").isFalse();
    }

    @Test
    public void testMoveFileToDirectory_Errors() throws Exception {
        assertThrows(NullPointerException.class, () -> FileUtils.moveFileToDirectory(null, new File("foo"), true));
        assertThrows(NullPointerException.class, () -> FileUtils.moveFileToDirectory(new File("foo"), null, true));
        final File testFile1 = new File(temporaryFolder, "testMoveFileFile1");
        final File testFile2 = new File(temporaryFolder, "testMoveFileFile2");
        if (!testFile1.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile1
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output1 =
                new BufferedOutputStream(new FileOutputStream(testFile1));
        try {
            TestUtils.generateTestData(output1, 0);
        } finally {
            IOUtils.closeQuietly(output1);
        }
        if (!testFile2.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile2
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile2));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveFileToDirectory(testFile1, testFile2, true));

        final File nonexistant = new File(temporaryFolder, "testMoveFileNonExistant");
        try {
            FileUtils.moveFileToDirectory(testFile1, nonexistant, false);
            fail("Expected IOException when dest does not exist and create=false");
        } catch (final IOException e) {
            // expected
        }
    }

    @Test
    public void testMoveToDirectory() throws Exception {
        final File destDir = new File(temporaryFolder, "testMoveToDirectoryDestDir");
        final File testDir = new File(temporaryFolder, "testMoveToDirectoryTestDir");
        final File testFile = new File(temporaryFolder, "testMoveToDirectoryTestFile");
        testDir.mkdirs();
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile));
        try {
            TestUtils.generateTestData(output, 0);
        } finally {
            IOUtils.closeQuietly(output);
        }
        final File movedFile = new File(destDir, testFile.getName());
        final File movedDir = new File(destDir, testFile.getName());

        assertThat(movedFile.exists()).as("Check File Doesnt exist").isFalse();
        assertThat(movedDir.exists()).as("Check Dir Doesnt exist").isFalse();

        // Test moving a file
        FileUtils.moveToDirectory(testFile, destDir, true);
        assertThat(movedFile.exists()).as("Check File exists").isTrue();
        assertThat(testFile.exists()).as("Check Original File doesn't exist").isFalse();

        // Test moving a directory
        FileUtils.moveToDirectory(testDir, destDir, true);
        assertThat(movedDir.exists()).as("Check Dir exists").isTrue();
        assertThat(testDir.exists()).as("Check Original Dir doesn't exist").isFalse();
    }

    @Test
    public void testMoveToDirectory_Errors() throws Exception {
        try {
            FileUtils.moveDirectoryToDirectory(null, new File("foo"), true);
            fail("Expected NullPointerException when source is null");
        } catch (final NullPointerException e) {
            // expected
        }
        try {
            FileUtils.moveDirectoryToDirectory(new File("foo"), null, true);
            fail("Expected NullPointerException when destination is null");
        } catch (final NullPointerException e) {
            // expected
        }
        final File nonexistant = new File(temporaryFolder, "nonexistant");
        final File destDir = new File(temporaryFolder, "MoveToDirectoryDestDir");
        try {
            FileUtils.moveToDirectory(nonexistant, destDir, true);
            fail("Expected IOException when source does not exist");
        } catch (final IOException e) {
            // expected
        }
    }

    @Test
    public void testReadFileToByteArray() throws Exception {
        final File file = new File(temporaryFolder, "read.txt");
        final FileOutputStream out = new FileOutputStream(file);
        out.write(11);
        out.write(21);
        out.write(31);
        out.close();

        final byte[] data = FileUtils.readFileToByteArray(file);
        assertThat(data.length).isEqualTo(3);
        assertThat(data[0]).isEqualTo(11);
        assertThat(data[1]).isEqualTo(21);
        assertThat(data[2]).isEqualTo(31);
    }

    @Test
    public void testReadFileToStringWithDefaultEncoding() throws Exception {
        final File file = new File(temporaryFolder, "read.obj");
        final FileOutputStream out = new FileOutputStream(file);
        final byte[] text = "Hello /u1234".getBytes();
        out.write(text);
        out.close();

        final String data = FileUtils.readFileToString(file);
        assertThat(data).isEqualTo("Hello /u1234");
    }

    @Test
    public void testReadFileToStringWithEncoding() throws Exception {
        final File file = new File(temporaryFolder, "read.obj");
        final FileOutputStream out = new FileOutputStream(file);
        final byte[] text = "Hello /u1234".getBytes(StandardCharsets.UTF_8);
        out.write(text);
        out.close();

        final String data = FileUtils.readFileToString(file, "UTF8");
        assertThat(data).isEqualTo("Hello /u1234");
    }

    @Test
    public void testReadLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        try {
            final String[] data = {"hello", "/u1234", "", "this is", "some text"};
            TestUtils.createLineBasedFile(file, data);

            final List<String> lines = FileUtils.readLines(file, "UTF-8");
            assertThat(lines).isEqualTo(Arrays.asList(data));
        } finally {
            TestUtils.deleteFile(file);
        }
    }

    @Test
    public void testSizeOf() throws Exception {
        final File file = new File(temporaryFolder, getName());

        // Null argument
        try {
            FileUtils.sizeOf(null);
            fail("Exception expected.");
        } catch (final NullPointerException ignore) {
        }

        // Non-existent file
        try {
            FileUtils.sizeOf(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // New file
        assertThat(FileUtils.sizeOf(file)).isEqualTo(0);
        file.delete();

        // Existing file
        assertThat(FileUtils.sizeOf(testFile1)).as("Unexpected files size").isEqualTo(testFile1Size);

        // Existing directory
        assertThat(FileUtils.sizeOf(temporaryFolder)).as("Unexpected directory size").isEqualTo(TEST_DIRECTORY_SIZE);
    }

    @Test
    public void testSizeOfAsBigInteger() throws Exception {
        final File file = new File(temporaryFolder, getName());

        // Null argument
        try {
            FileUtils.sizeOfAsBigInteger(null);
            fail("Exception expected.");
        } catch (final NullPointerException ignore) {
        }

        // Non-existent file
        try {
            FileUtils.sizeOfAsBigInteger(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // New file
        assertThat(FileUtils.sizeOfAsBigInteger(file)).isEqualTo(BigInteger.ZERO);
        file.delete();

        // Existing file
        assertThat(FileUtils.sizeOfAsBigInteger(testFile1)).as("Unexpected files size").isEqualTo(BigInteger.valueOf(testFile1Size));

        // Existing directory
        assertThat(FileUtils.sizeOfAsBigInteger(temporaryFolder)).as("Unexpected directory size").isEqualTo(TEST_DIRECTORY_SIZE_BI);
    }


    @Test
    public void testSizeOfDirectory() throws Exception {
        final File file = new File(temporaryFolder, getName());

        // Non-existent file
        try {
            FileUtils.sizeOfDirectory(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Creates file
        file.createNewFile();

        // Existing file
        try {
            FileUtils.sizeOfDirectory(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Existing directory
        file.delete();
        file.mkdir();

        // Create a cyclic symlink
        this.createCircularSymLink(file);

        assertThat(FileUtils.sizeOfDirectory(file)).as("Unexpected directory size").isEqualTo(TEST_DIRECTORY_SIZE);
    }

    @Test
    public void testSizeOfDirectoryAsBigInteger() throws Exception {
        final File file = new File(temporaryFolder, getName());

        // Non-existent file
        try {
            FileUtils.sizeOfDirectoryAsBigInteger(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // Existing file
        try {
            FileUtils.sizeOfDirectoryAsBigInteger(file);
            fail("Exception expected.");
        } catch (final IllegalArgumentException ignore) {
        }

        // Existing directory
        file.delete();
        file.mkdir();

        this.createCircularSymLink(file);

        assertThat(FileUtils.sizeOfDirectoryAsBigInteger(file)).as("Unexpected directory size").isEqualTo(TEST_DIRECTORY_SIZE_BI);

        // Existing directory which size is greater than zero
        file.delete();
        file.mkdir();

        final File nonEmptyFile = new File(file, "nonEmptyFile" + System.nanoTime());
        if (!nonEmptyFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + nonEmptyFile
                    + " as the parent directory does not exist");
        }
        final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(nonEmptyFile));
        try {
            TestUtils.generateTestData(output, TEST_DIRECTORY_SIZE_GT_ZERO_BI.longValue());
        } finally {
            IOUtils.closeQuietly(output);
        }
        nonEmptyFile.deleteOnExit();

        assertThat(FileUtils.sizeOfDirectoryAsBigInteger(file)).as("Unexpected directory size").isEqualTo(TEST_DIRECTORY_SIZE_GT_ZERO_BI);

        nonEmptyFile.delete();
        file.delete();
    }

    //-----------------------------------------------------------------------
    @Test
    public void testToFile1() throws Exception {
        final URL url = new URL("file", null, "a/b/c/file.txt");
        final File file = FileUtils.toFile(url);
        assertThat(file.toString().contains("file.txt")).isTrue();
    }

    @Test
    public void testToFile2() throws Exception {
        final URL url = new URL("file", null, "a/b/c/file%20n%61me%2520.tx%74");
        final File file = FileUtils.toFile(url);
        assertThat(file.toString().contains("file name%20.txt")).isTrue();
    }

    @Test
    public void testToFile3() throws Exception {
        assertThat(FileUtils.toFile(null)).isNull();
        assertThat(FileUtils.toFile(new URL("http://jakarta.apache.org"))).isNull();
    }

    @Test
    public void testToFile4() throws Exception {
        final URL url = new URL("file", null, "a/b/c/file%%20%me.txt%");
        final File file = FileUtils.toFile(url);
        assertThat(file.toString().contains("file% %me.txt%")).isTrue();
    }

    /* IO-252 */
    @Test
    public void testToFile5() throws Exception {
        final URL url = new URL("file", null, "both%20are%20100%20%25%20true");
        final File file = FileUtils.toFile(url);
        assertThat(file.toString()).isEqualTo("both are 100 % true");
    }

    @Test
    public void testToFiles1() throws Exception {
        final URL[] urls = {
                new URL("file", null, "file1.txt"),
                new URL("file", null, "file2.txt")
        };
        final File[] files = FileUtils.toFiles(urls);

        assertThat(files.length).isEqualTo(urls.length);
        assertThat(files[0].toString().contains("file1.txt")).as("File: " + files[0]).isTrue();
        assertThat(files[1].toString().contains("file2.txt")).as("File: " + files[1]).isTrue();
    }

    @Test
    public void testToFiles2() throws Exception {
        final URL[] urls = {
                new URL("file", null, "file1.txt"),
                null
        };
        final File[] files = FileUtils.toFiles(urls);

        assertThat(files.length).isEqualTo(urls.length);
        assertThat(files[0].toString().contains("file1.txt")).as("File: " + files[0]).isTrue();
        assertThat(files[1]).as("File: " + files[1]).isNull();
    }

    @Test
    public void testToFiles3() throws Exception {
        final URL[] urls = null;
        final File[] files = FileUtils.toFiles(urls);

        assertThat(files.length).isEqualTo(0);
    }

    @Test
    public void testToFiles3a() throws Exception {
        final URL[] urls = {}; // empty array
        final File[] files = FileUtils.toFiles(urls);

        assertThat(files.length).isEqualTo(0);
    }

    @Test
    public void testToFiles4() throws Exception {
        final URL[] urls = {
                new URL("file", null, "file1.txt"),
                new URL("http", "jakarta.apache.org", "file1.txt")
        };
        try {
            FileUtils.toFiles(urls);
            fail();
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testToFileUtf8() throws Exception {
        final URL url = new URL("file", null, "/home/%C3%A4%C3%B6%C3%BC%C3%9F");
        final File file = FileUtils.toFile(url);
        assertThat(file.toString().contains("\u00E4\u00F6\u00FC\u00DF")).isTrue();
    }

    @Test
    public void testTouch() throws IOException {
        final File file = new File(temporaryFolder, "touch.txt");
        if (file.exists()) {
            file.delete();
        }
        assertThat(file.exists()).as("Bad test: test file still exists").isFalse();
        FileUtils.touch(file);
        assertThat(file.exists()).as("FileUtils.touch() created file").isTrue();
        final FileOutputStream out = new FileOutputStream(file);
        assertThat(file.length()).as("Created empty file.").isEqualTo(0);
        out.write(0);
        out.close();
        assertThat(file.length()).as("Wrote one byte to file").isEqualTo(1);
        final long y2k = new GregorianCalendar(2000, 0, 1).getTime().getTime();
        final boolean res = setLastModifiedMillis(file, y2k);  // 0L fails on Win98
        assertThat(res).as("Bad test: set lastModified failed").isTrue();
        assertThat(getLastModifiedMillis(file)).as("Bad test: set lastModified set incorrect value").isEqualTo(y2k);
        final long nowMillis = System.currentTimeMillis();
        FileUtils.touch(file);
        assertThat(file.length()).as("FileUtils.touch() didn't empty the file.").isEqualTo(1);
        assertThat(getLastModifiedMillis(file)).as("FileUtils.touch() changed lastModified").isNotEqualTo(y2k);
        final int delta = 3000;
        assertThat(getLastModifiedMillis(file) >= nowMillis - delta).as("FileUtils.touch() changed lastModified to more than now-3s").isTrue();
        assertThat(getLastModifiedMillis(file) <= nowMillis + delta).as("FileUtils.touch() changed lastModified to less than now+3s").isTrue();
    }

    @Test
    public void testToURLs1() throws Exception {
        final File[] files = {
                new File(temporaryFolder, "file1.txt"),
                new File(temporaryFolder, "file2.txt"),
                new File(temporaryFolder, "test file.txt")
        };
        final URL[] urls = FileUtils.toURLs(files);

        assertThat(urls.length).isEqualTo(files.length);
        assertThat(urls[0].toExternalForm().startsWith("file:")).isTrue();
        assertThat(urls[0].toExternalForm().contains("file1.txt")).isTrue();
        assertThat(urls[1].toExternalForm().startsWith("file:")).isTrue();
        assertThat(urls[1].toExternalForm().contains("file2.txt")).isTrue();

        // Test escaped char
        assertThat(urls[2].toExternalForm().startsWith("file:")).isTrue();
        assertThat(urls[2].toExternalForm().contains("test%20file.txt")).isTrue();
    }

    @Test
    public void testToURLs3a() throws Exception {
        final File[] files = {}; // empty array
        final URL[] urls = FileUtils.toURLs(files);

        assertThat(urls.length).isEqualTo(0);
    }

    @Test
    public void testWrite_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWrite_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteByteArrayToFile() throws Exception {
        final File file = new File(temporaryFolder, "write.obj");
        final byte[] data = {11, 21, 31};
        FileUtils.writeByteArrayToFile(file, data);
        TestUtils.assertEqualContent(data, file);
    }

    @Test
    public void testWriteByteArrayToFile_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeByteArrayToFile(file, "this is brand new data".getBytes(), false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteByteArrayToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeByteArrayToFile(file, "this is brand new data".getBytes(), true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength() throws Exception {
        final File file = new File(temporaryFolder, "write.obj");
        final byte[] data = {11, 21, 32, 41, 51};
        final byte[] writtenData = new byte[3];
        System.arraycopy(data, 1, writtenData, 0, 3);
        FileUtils.writeByteArrayToFile(file, data, 1, 3);
        TestUtils.assertEqualContent(writtenData, file);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength_WithAppendOptionTrue_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final byte[] data = "SKIP_THIS_this is brand new data_AND_SKIP_THIS".getBytes(StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(file, data, 10, 22, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteByteArrayToFile_WithOffsetAndLength_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final byte[] data = "SKIP_THIS_this is brand new data_AND_SKIP_THIS".getBytes(StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(file, data, 10, 22, true);

        final String expected = "This line was there before you..." + "this is brand new data";
        final String actual = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteCharSequence1() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.write(file, "Hello /u1234", "UTF8");
        final byte[] text = "Hello /u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteCharSequence2() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.write(file, "Hello /u1234", (String) null);
        final byte[] text = "Hello /u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteLines_3arg_nullSeparator() throws Exception {
        final Object[] data = {
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list);

        final String expected = "hello" + System.lineSeparator() + "world" + System.lineSeparator() +
                System.lineSeparator() + "this is" + System.lineSeparator() +
                System.lineSeparator() + "some text" + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLines_3argsWithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, linesToAppend, false);

        final String expected = "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLines_3argsWithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, linesToAppend, true);

        final String expected = "This line was there before you..."
                + "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLines_4arg() throws Exception {
        final Object[] data = {
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list, "*");

        final String expected = "hello*world**this is**some text*";
        final String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLines_4arg_nullSeparator() throws Exception {
        final Object[] data = {
                "hello", new StringBuffer("world"), "", "this is", null, "some text"};
        final List<Object> list = Arrays.asList(data);

        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeLines(file, "US-ASCII", list, null);

        final String expected = "hello" + System.lineSeparator() + "world" + System.lineSeparator() +
                System.lineSeparator() + "this is" + System.lineSeparator() +
                System.lineSeparator() + "some text" + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file, "US-ASCII");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLines_4arg_Writer_nullData() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeLines(file, "US-ASCII", null, "*");

        assertThat(file.length()).as("Sizes differ").isEqualTo(0);
    }

    @Test
    public void testWriteLines_4argsWithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, linesToAppend, null, false);

        final String expected = "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    public void testWriteLines_4argsWithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, linesToAppend, null, true);

        final String expected = "This line was there before you..."
                + "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLines_5argsWithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, null, linesToAppend, null, false);

        final String expected = "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLines_5argsWithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, null, linesToAppend, null, true);

        final String expected = "This line was there before you..."
                + "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLinesEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, null, linesToAppend, false);

        final String expected = "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteLinesEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        final List<String> linesToAppend = Arrays.asList("my first line", "The second Line");
        FileUtils.writeLines(file, null, linesToAppend, true);

        final String expected = "This line was there before you..."
                + "my first line"
                + System.lineSeparator() + "The second Line"
                + System.lineSeparator();
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteStringToFile_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteStringToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteStringToFile1() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.writeStringToFile(file, "Hello /u1234", "UTF8");
        final byte[] text = "Hello /u1234".getBytes(StandardCharsets.UTF_8);
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFile2() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.writeStringToFile(file, "Hello /u1234", (String) null);
        final byte[] text = "Hello /u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFile3() throws Exception {
        final File file = new File(temporaryFolder, "write.txt");
        FileUtils.writeStringToFile(file, "Hello /u1234", (Charset) null);
        final byte[] text = "Hello /u1234".getBytes();
        TestUtils.assertEqualContent(text, file);
    }

    @Test
    public void testWriteStringToFileWithEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", (String) null, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteStringToFileWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.writeStringToFile(file, "this is brand new data", (String) null, true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteWithEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", (String) null, false);

        final String expected = "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWriteWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        final File file = TestUtils.newFile(temporaryFolder, "lines.txt");
        FileUtils.writeStringToFile(file, "This line was there before you...");

        FileUtils.write(file, "this is brand new data", (String) null, true);

        final String expected = "This line was there before you..."
                + "this is brand new data";
        final String actual = FileUtils.readFileToString(file);
        assertThat(actual).isEqualTo(expected);
    }

}
