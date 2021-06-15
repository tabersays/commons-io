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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.junit.jupiter.api.Test;

/**
 * This is used to test DirectoryWalker for correctness.
 *
 * @see DirectoryWalker
 *
 */
public class DirectoryWalkerTestCase {

    // Directories
    private static final File current      = new File(".");
    private static final File javaDir      = new File("src/main/java");
    private static final File orgDir       = new File(javaDir, "org");
    private static final File apacheDir    = new File(orgDir, "apache");
    private static final File commonsDir   = new File(apacheDir, "commons");
    private static final File ioDir        = new File(commonsDir, "io");
    private static final File outputDir    = new File(ioDir, "output");
    private static final File[] dirs       = {orgDir, apacheDir, commonsDir, ioDir, outputDir};

    // Files
    private static final File filenameUtils = new File(ioDir, "FilenameUtils.java");
    private static final File ioUtils       = new File(ioDir, "IOUtils.java");
    private static final File proxyWriter   = new File(outputDir, "ProxyWriter.java");
    private static final File nullStream    = new File(outputDir, "NullOutputStream.java");
    private static final File[] ioFiles     = {filenameUtils, ioUtils};
    private static final File[] outputFiles = {proxyWriter, nullStream};

    // Filters
    private static final IOFileFilter dirsFilter        = createNameFilter(dirs);
    private static final IOFileFilter iofilesFilter     = createNameFilter(ioFiles);
    private static final IOFileFilter outputFilesFilter = createNameFilter(outputFiles);
    private static final IOFileFilter ioDirAndFilesFilter = dirsFilter.or(iofilesFilter);
    private static final IOFileFilter dirsAndFilesFilter = ioDirAndFilesFilter.or(outputFilesFilter);

    // Filter to exclude SVN files
    private static final IOFileFilter NOT_SVN = FileFilterUtils.makeSVNAware(null);

    //-----------------------------------------------------------------------

    /**
     * Test Filtering
     */
    @Test
    public void testFilter() {
        final List<File> results = new TestFileFinder(dirsAndFilesFilter, -1).find(javaDir);
        assertThat(results.size()).as("Result Size").isEqualTo(1 + dirs.length + ioFiles.length + outputFiles.length);
        assertThat(results.contains(javaDir)).as("Start Dir").isTrue();
        checkContainsFiles("Dir", dirs, results);
        checkContainsFiles("IO File", ioFiles, results);
        checkContainsFiles("Output File", outputFiles, results);
    }

    /**
     * Test Filtering and limit to depth 0
     */
    @Test
    public void testFilterAndLimitA() {
        final List<File> results = new TestFileFinder(NOT_SVN, 0).find(javaDir);
        assertThat(results.size()).as("[A] Result Size").isEqualTo(1);
        assertThat(results.contains(javaDir)).as("[A] Start Dir").isTrue();
    }

    /**
     * Test Filtering and limit to depth 1
     */
    @Test
    public void testFilterAndLimitB() {
        final List<File> results = new TestFileFinder(NOT_SVN, 1).find(javaDir);
        assertThat(results.size()).as("[B] Result Size").isEqualTo(2);
        assertThat(results.contains(javaDir)).as("[B] Start Dir").isTrue();
        assertThat(results.contains(orgDir)).as("[B] Org Dir").isTrue();
    }

    /**
     * Test Filtering and limit to depth 3
     */
    @Test
    public void testFilterAndLimitC() {
        final List<File> results = new TestFileFinder(NOT_SVN, 3).find(javaDir);
        assertThat(results.size()).as("[C] Result Size").isEqualTo(4);
        assertThat(results.contains(javaDir)).as("[C] Start Dir").isTrue();
        assertThat(results.contains(orgDir)).as("[C] Org Dir").isTrue();
        assertThat(results.contains(apacheDir)).as("[C] Apache Dir").isTrue();
        assertThat(results.contains(commonsDir)).as("[C] Commons Dir").isTrue();
    }

    /**
     * Test Filtering and limit to depth 5
     */
    @Test
    public void testFilterAndLimitD() {
        final List<File> results = new TestFileFinder(dirsAndFilesFilter, 5).find(javaDir);
        assertThat(results.size()).as("[D] Result Size").isEqualTo(1 + dirs.length + ioFiles.length);
        assertThat(results.contains(javaDir)).as("[D] Start Dir").isTrue();
        checkContainsFiles("[D] Dir", dirs, results);
        checkContainsFiles("[D] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile1() {
        final List<File> results = new TestFileFinder(dirsFilter, iofilesFilter, -1).find(javaDir);
        assertThat(results.size()).as("[DirAndFile1] Result Size").isEqualTo(1 + dirs.length + ioFiles.length);
        assertThat(results.contains(javaDir)).as("[DirAndFile1] Start Dir").isTrue();
        checkContainsFiles("[DirAndFile1] Dir", dirs, results);
        checkContainsFiles("[DirAndFile1] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile2() {
        final List<File> results = new TestFileFinder(null, null, -1).find(javaDir);
        assertThat(results.size() > 1 + dirs.length + ioFiles.length).as("[DirAndFile2] Result Size").isTrue();
        assertThat(results.contains(javaDir)).as("[DirAndFile2] Start Dir").isTrue();
        checkContainsFiles("[DirAndFile2] Dir", dirs, results);
        checkContainsFiles("[DirAndFile2] File", ioFiles, results);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile3() {
        final List<File> results = new TestFileFinder(dirsFilter, null, -1).find(javaDir);
        final List<File> resultDirs = directoriesOnly(results);
        assertThat(resultDirs.size()).as("[DirAndFile3] Result Size").isEqualTo(1 + dirs.length);
        assertThat(results.contains(javaDir)).as("[DirAndFile3] Start Dir").isTrue();
        checkContainsFiles("[DirAndFile3] Dir", dirs, resultDirs);
    }

    /**
     * Test separate dir and file filters
     */
    @Test
    public void testFilterDirAndFile4() {
        final List<File> results = new TestFileFinder(null, iofilesFilter, -1).find(javaDir);
        final List<File> resultFiles = filesOnly(results);
        assertThat(resultFiles.size()).as("[DirAndFile4] Result Size").isEqualTo(ioFiles.length);
        assertThat(results.contains(javaDir)).as("[DirAndFile4] Start Dir").isTrue();
        checkContainsFiles("[DirAndFile4] File", ioFiles, resultFiles);
    }

    /**
     * Test Limiting to current directory
     */
    @Test
    public void testLimitToCurrent() {
        final List<File> results = new TestFileFinder(null, 0).find(current);
        assertThat(results.size()).as("Result Size").isEqualTo(1);
        assertThat(results.contains(new File("."))).as("Current Dir").isTrue();
    }

    /**
     * test an invalid start directory
     */
    @Test
    public void testMissingStartDirectory() {

        // TODO is this what we want with invalid directory?
        final File invalidDir = new File("invalid-dir");
        final List<File> results = new TestFileFinder(null, -1).find(invalidDir);
        assertThat(results.size()).as("Result Size").isEqualTo(1);
        assertThat(results.contains(invalidDir)).as("Current Dir").isTrue();

        try {
            new TestFileFinder(null, -1).find(null);
            fail("Null start directory didn't throw Exception");
        } catch (final NullPointerException ignore) {
            // expected result
        }
    }

    /**
     * test an invalid start directory
     */
    @Test
    public void testHandleStartDirectoryFalse() {

        final List<File> results = new TestFalseFileFinder(null, -1).find(current);
        assertThat(results.size()).as("Result Size").isEqualTo(0);

    }

    // ------------ Convenience Test Methods ------------------------------------

    /**
     * Check the files in the array are in the results list.
     */
    private void checkContainsFiles(final String prefix, final File[] files, final Collection<File> results) {
        for (int i = 0; i < files.length; i++) {
            assertThat(results.contains(files[i])).as(prefix + "[" + i + "] " + files[i]).isTrue();
        }
    }

    private void checkContainsString(final String prefix, final File[] files, final Collection<String> results) {
        for (int i = 0; i < files.length; i++) {
            assertThat(results.contains(files[i].toString())).as(prefix + "[" + i + "] " + files[i]).isTrue();
        }
    }

    /**
     * Extract the directories.
     */
    private List<File> directoriesOnly(final Collection<File> results) {
        final List<File> list = new ArrayList<>(results.size());
        for (final File file : results) {
            if (file.isDirectory()) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * Extract the files.
     */
    private List<File> filesOnly(final Collection<File> results) {
        final List<File> list = new ArrayList<>(results.size());
        for (final File file : results) {
            if (file.isFile()) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * Create a name filter containing the names of the files
     * in the array.
     */
    private static IOFileFilter createNameFilter(final File[] files) {
        final String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        return new NameFileFilter(names);
    }

    /**
     * Test Cancel
     */
    @Test
    public void testCancel() {
        String cancelName = null;

        // Cancel on a file
        try {
            cancelName = "DirectoryWalker.java";
            new TestCancelWalker(cancelName, false).find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertThat(cancel.getFile().getName()).as("File:  " + cancelName).isEqualTo(cancelName);
            assertThat(cancel.getDepth()).as("Depth: " + cancelName).isEqualTo(5);
        } catch(final IOException ex) {
            fail("IOException: " + cancelName + " " + ex);
        }

        // Cancel on a directory
        try {
            cancelName = "commons";
            new TestCancelWalker(cancelName, false).find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertThat(cancel.getFile().getName()).as("File:  " + cancelName).isEqualTo(cancelName);
            assertThat(cancel.getDepth()).as("Depth: " + cancelName).isEqualTo(3);
        } catch(final IOException ex) {
            fail("IOException: " + cancelName + " " + ex);
        }

        // Suppress CancelException (use same file name as preceding test)
        try {
            final List<File> results = new TestCancelWalker(cancelName, true).find(javaDir);
            final File lastFile = results.get(results.size() - 1);
            assertThat(lastFile.getName()).as("Suppress:  " + cancelName).isEqualTo(cancelName);
        } catch(final IOException ex) {
            fail("Suppress threw " + ex);
        }

    }

    /**
     * Test Cancel
     */
    @Test
    public void testMultiThreadCancel() {
        String cancelName = "DirectoryWalker.java";
        TestMultiThreadCancelWalker walker = new TestMultiThreadCancelWalker(cancelName, false);
        // Cancel on a file
        try {
            walker.find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            final File last = walker.results.get(walker.results.size() - 1);
            assertThat(last.getName()).isEqualTo(cancelName);
            assertThat(cancel.getDepth()).as("Depth: " + cancelName).isEqualTo(5);
        } catch(final IOException ex) {
            fail("IOException: " + cancelName + " " + ex);
        }

        // Cancel on a directory
        try {
            cancelName = "commons";
            walker = new TestMultiThreadCancelWalker(cancelName, false);
            walker.find(javaDir);
            fail("CancelException not thrown for '" + cancelName + "'");
        } catch (final DirectoryWalker.CancelException cancel) {
            assertThat(cancel.getFile().getName()).as("File:  " + cancelName).isEqualTo(cancelName);
            assertThat(cancel.getDepth()).as("Depth: " + cancelName).isEqualTo(3);
        } catch(final IOException ex) {
            fail("IOException: " + cancelName + " " + ex);
        }

        // Suppress CancelException (use same file name as preceding test)
        try {
            walker = new TestMultiThreadCancelWalker(cancelName, true);
            final List<File> results = walker.find(javaDir);
            final File lastFile = results.get(results.size() - 1);
            assertThat(lastFile.getName()).as("Suppress:  " + cancelName).isEqualTo(cancelName);
        } catch(final IOException ex) {
            fail("Suppress threw " + ex);
        }

    }

    /**
     * Test Filtering
     */
    @Test
    public void testFilterString() {
        final List<String> results = new TestFileFinderString(dirsAndFilesFilter, -1).find(javaDir);
        assertThat(outputFiles.length + ioFiles.length).as("Result Size").isEqualTo(results.size());
        checkContainsString("IO File", ioFiles, results);
        checkContainsString("Output File", outputFiles, results);
    }

    // ------------ Test DirectoryWalker implementation --------------------------

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    private static class TestFileFinder extends DirectoryWalker<File> {

        protected TestFileFinder(final FileFilter filter, final int depthLimit) {
            super(filter, depthLimit);
        }

        protected TestFileFinder(final IOFileFilter dirFilter, final IOFileFilter fileFilter, final int depthLimit) {
            super(dirFilter, fileFilter, depthLimit);
        }

        /** find files. */
        protected List<File> find(final File startDirectory) {
           final List<File> results = new ArrayList<>();
           try {
               walk(startDirectory, results);
           } catch(final IOException ex) {
               fail(ex.toString());
           }
           return results;
        }

        /** Handles a directory end by adding the File to the result set. */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection<File> results) {
            results.add(directory);
        }

        /** Handles a file by adding the File to the result set. */
        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> results) {
            results.add(file);
        }
    }

    // ------------ Test DirectoryWalker implementation --------------------------

    /**
     * Test DirectoryWalker implementation that always returns false
     * from handleDirectoryStart()
     */
    private static class TestFalseFileFinder extends TestFileFinder {

        protected TestFalseFileFinder(final FileFilter filter, final int depthLimit) {
            super(filter, depthLimit);
        }

        /** Always returns false. */
        @Override
        protected boolean handleDirectory(final File directory, final int depth, final Collection<File> results) {
            return false;
        }
    }

    // ------------ Test DirectoryWalker implementation --------------------------

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    static class TestCancelWalker extends DirectoryWalker<File> {
        private final String cancelFileName;
        private final boolean suppressCancel;

        TestCancelWalker(final String cancelFileName,final boolean suppressCancel) {
            this.cancelFileName = cancelFileName;
            this.suppressCancel = suppressCancel;
        }

        /** find files. */
        protected List<File> find(final File startDirectory) throws IOException {
           final List<File> results = new ArrayList<>();
           walk(startDirectory, results);
           return results;
        }

        /** Handles a directory end by adding the File to the result set. */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection<File> results) throws IOException {
            results.add(directory);
            if (cancelFileName.equals(directory.getName())) {
                throw new CancelException(directory, depth);
            }
        }

        /** Handles a file by adding the File to the result set. */
        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> results) throws IOException {
            results.add(file);
            if (cancelFileName.equals(file.getName())) {
                throw new CancelException(file, depth);
            }
        }

        /** Handles Cancel. */
        @Override
        protected void handleCancelled(final File startDirectory, final Collection<File> results,
                       final CancelException cancel) throws IOException {
            if (!suppressCancel) {
                super.handleCancelled(startDirectory, results, cancel);
            }
        }
    }

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    static class TestMultiThreadCancelWalker extends DirectoryWalker<File> {
        private final String cancelFileName;
        private final boolean suppressCancel;
        private boolean cancelled;
        public List<File> results;

        TestMultiThreadCancelWalker(final String cancelFileName, final boolean suppressCancel) {
            this.cancelFileName = cancelFileName;
            this.suppressCancel = suppressCancel;
        }

        /** find files. */
        protected List<File> find(final File startDirectory) throws IOException {
           results = new ArrayList<>();
           walk(startDirectory, results);
           return results;
        }

        /** Handles a directory end by adding the File to the result set. */
        @Override
        protected void handleDirectoryEnd(final File directory, final int depth, final Collection<File> results) throws IOException {
            results.add(directory);
            assertThat(cancelled).isFalse();
            if (cancelFileName.equals(directory.getName())) {
                cancelled = true;
            }
        }

        /** Handles a file by adding the File to the result set. */
        @Override
        protected void handleFile(final File file, final int depth, final Collection<File> results) throws IOException {
            results.add(file);
            assertThat(cancelled).isFalse();
            if (cancelFileName.equals(file.getName())) {
                cancelled = true;
            }
        }

        /** Handles Cancelled. */
        @Override
        protected boolean handleIsCancelled(final File file, final int depth, final Collection<File> results) throws IOException {
            return cancelled;
        }

        /** Handles Cancel. */
        @Override
        protected void handleCancelled(final File startDirectory, final Collection<File> results,
                       final CancelException cancel) throws IOException {
            if (!suppressCancel) {
                super.handleCancelled(startDirectory, results, cancel);
            }
        }
    }

    /**
     * Test DirectoryWalker implementation that finds files in a directory hierarchy
     * applying a file filter.
     */
    private static class TestFileFinderString extends DirectoryWalker<String> {

        protected TestFileFinderString(final FileFilter filter, final int depthLimit) {
            super(filter, depthLimit);
        }

        /** find files. */
        protected List<String> find(final File startDirectory) {
           final List<String> results = new ArrayList<>();
           try {
               walk(startDirectory, results);
           } catch(final IOException ex) {
               fail(ex.toString());
           }
           return results;
        }

        /** Handles a file by adding the File to the result set. */
        @Override
        protected void handleFile(final File file, final int depth, final Collection<String> results) {
            results.add(file.toString());
        }
    }

}
