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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test cases for FileUtils.listFiles() methods.
 */
public class FileUtilsListFilesTestCase {

    @TempDir
    public File temporaryFolder;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
    public void setUp() throws Exception {
        File dir = temporaryFolder;
        File file = new File(dir, "dummy-build.xml");
        FileUtils.touch(file);
        file = new File(dir, "README");
        FileUtils.touch(file);

        dir = new File(dir, "subdir1");
        dir.mkdirs();
        file = new File(dir, "dummy-build.xml");
        FileUtils.touch(file);
        file = new File(dir, "dummy-readme.txt");
        FileUtils.touch(file);

        dir = new File(dir, "subsubdir1");
        dir.mkdirs();
        file = new File(dir, "dummy-file.txt");
        FileUtils.touch(file);
        file = new File(dir, "dummy-index.html");
        FileUtils.touch(file);

        dir = dir.getParentFile();
        dir = new File(dir, "CVS");
        dir.mkdirs();
        file = new File(dir, "Entries");
        FileUtils.touch(file);
        file = new File(dir, "Repository");
        FileUtils.touch(file);
    }

    private Collection<String> filesToFilenames(final Collection<File> files) {
        final Collection<String> filenames = new ArrayList<>(files.size());
        for (final File file : files) {
            filenames.add(file.getName());
        }
        return filenames;
    }

    private Collection<String> filesToFilenames(final Iterator<File> files) {
        final Collection<String> filenames = new ArrayList<>();
        while (files.hasNext()) {
            filenames.add(files.next().getName());
        }
        return filenames;
    }

    @Test
    public void testIterateFilesByExtension() {
        final String[] extensions = { "xml", "txt" };

        Iterator<File> files = FileUtils.iterateFiles(temporaryFolder, extensions, false);
        Collection<String> filenames = filesToFilenames(files);
        assertThat(filenames.size()).isEqualTo(1);
        assertThat(filenames.contains("dummy-build.xml")).isTrue();
        assertThat(filenames.contains("README")).isFalse();
        assertThat(filenames.contains("dummy-file.txt")).isFalse();

        files = FileUtils.iterateFiles(temporaryFolder, extensions, true);
        filenames = filesToFilenames(files);
        assertThat(filenames.size()).isEqualTo(4);
        assertThat(filenames.contains("dummy-file.txt")).isTrue();
        assertThat(filenames.contains("dummy-index.html")).isFalse();

        files = FileUtils.iterateFiles(temporaryFolder, null, false);
        filenames = filesToFilenames(files);
        assertThat(filenames.size()).isEqualTo(2);
        assertThat(filenames.contains("dummy-build.xml")).isTrue();
        assertThat(filenames.contains("README")).isTrue();
        assertThat(filenames.contains("dummy-file.txt")).isFalse();
    }

    @Test
    public void testListFilesByExtension() {
        final String[] extensions = {"xml", "txt"};

        Collection<File> files = FileUtils.listFiles(temporaryFolder, extensions, false);
        assertThat(files.size()).isEqualTo(1);
        Collection<String> filenames = filesToFilenames(files);
        assertThat(filenames.contains("dummy-build.xml")).isTrue();
        assertThat(filenames.contains("README")).isFalse();
        assertThat(filenames.contains("dummy-file.txt")).isFalse();

        files = FileUtils.listFiles(temporaryFolder, extensions, true);
        filenames = filesToFilenames(files);
        assertThat(filenames.size()).isEqualTo(4);
        assertThat(filenames.contains("dummy-file.txt")).isTrue();
        assertThat(filenames.contains("dummy-index.html")).isFalse();

        files = FileUtils.listFiles(temporaryFolder, null, false);
        assertThat(files.size()).isEqualTo(2);
        filenames = filesToFilenames(files);
        assertThat(filenames.contains("dummy-build.xml")).isTrue();
        assertThat(filenames.contains("README")).isTrue();
        assertThat(filenames.contains("dummy-file.txt")).isFalse();
    }

    @Test
    public void testListFiles() {
        Collection<File> files;
        Collection<String> filenames;
        IOFileFilter fileFilter;
        IOFileFilter dirFilter;

        // First, find non-recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        files = FileUtils.listFiles(temporaryFolder, fileFilter, null);
        filenames = filesToFilenames(files);
        assertThat(filenames.contains("dummy-build.xml")).as("'dummy-build.xml' is missing").isTrue();
        assertThat(filenames.contains("dummy-index.html")).as("'dummy-index.html' shouldn't be found").isFalse();
        assertThat(filenames.contains("Entries")).as("'Entries' shouldn't be found").isFalse();

        // Second, find recursively
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("CVS"));
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertThat(filenames.contains("dummy-build.xml")).as("'dummy-build.xml' is missing").isTrue();
        assertThat(filenames.contains("dummy-index.html")).as("'dummy-index.html' is missing").isTrue();
        assertThat(filenames.contains("Entries")).as("'Entries' shouldn't be found").isFalse();

        // Do the same as above but now with the filter coming from FileFilterUtils
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.makeCVSAware(null);
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertThat(filenames.contains("dummy-build.xml")).as("'dummy-build.xml' is missing").isTrue();
        assertThat(filenames.contains("dummy-index.html")).as("'dummy-index.html' is missing").isTrue();
        assertThat(filenames.contains("Entries")).as("'Entries' shouldn't be found").isFalse();

        // Again with the CVS filter but now with a non-null parameter
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.prefixFileFilter("sub");
        dirFilter = FileFilterUtils.makeCVSAware(dirFilter);
        files = FileUtils.listFiles(temporaryFolder, fileFilter, dirFilter);
        filenames = filesToFilenames(files);
        assertThat(filenames.contains("dummy-build.xml")).as("'dummy-build.xml' is missing").isTrue();
        assertThat(filenames.contains("dummy-index.html")).as("'dummy-index.html' is missing").isTrue();
        assertThat(filenames.contains("Entries")).as("'Entries' shouldn't be found").isFalse();

        try {
            FileUtils.listFiles(temporaryFolder, null, null);
            fail("Expected error about null parameter");
        } catch (final NullPointerException e) {
            // expected
        }
    }


}
