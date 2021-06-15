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
package org.apache.commons.io.monitor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.jupiter.api.Test;

/**
 * {@link FileAlterationObserver} Test Case.
 */
public class FileAlterationObserverTestCase extends AbstractMonitorTestCase {

    /**
     * Construct a new test case.
     *
     */
    public FileAlterationObserverTestCase() {
        listener = new CollectionFileListener(true);
    }

    /**
     * Test add/remove listeners.
     */
    @Test
    public void testAddRemoveListeners() {
        final FileAlterationObserver observer = new FileAlterationObserver("/foo");
        // Null Listener
        observer.addListener(null);
        assertThat(observer.getListeners().iterator().hasNext()).as("Listeners[1]").isFalse();
        observer.removeListener(null);
        assertThat(observer.getListeners().iterator().hasNext()).as("Listeners[2]").isFalse();

        // Add Listener
        final FileAlterationListenerAdaptor listener = new FileAlterationListenerAdaptor();
        observer.addListener(listener);
        final Iterator<FileAlterationListener> it = observer.getListeners().iterator();
        assertThat(it.hasNext()).as("Listeners[3]").isTrue();
        assertThat(it.next()).as("Added").isEqualTo(listener);
        assertThat(it.hasNext()).as("Listeners[4]").isFalse();

        // Remove Listener
        observer.removeListener(listener);
        assertThat(observer.getListeners().iterator().hasNext()).as("Listeners[5]").isFalse();
    }

    /**
     * Test toString().
     */
    @Test
    public void testToString() {
        final File file = new File("/foo");

        FileAlterationObserver observer = new FileAlterationObserver(file);
        assertThat(observer.toString()).isEqualTo("FileAlterationObserver[file='" + file.getPath() +  "', listeners=0]");

        observer = new FileAlterationObserver(file, CanReadFileFilter.CAN_READ);
        assertThat(observer.toString()).isEqualTo("FileAlterationObserver[file='" + file.getPath() +  "', CanReadFileFilter, listeners=0]");

        assertThat(observer.getDirectory()).isEqualTo(file);
  }

    /**
     * Test checkAndNotify() method
     * @throws Exception
     */
    @Test
    public void testDirectory() throws Exception {
        checkAndNotify();
        checkCollectionsEmpty("A");
        final File testDirA = new File(testDir, "test-dir-A");
        final File testDirB = new File(testDir, "test-dir-B");
        final File testDirC = new File(testDir, "test-dir-C");
        testDirA.mkdir();
        testDirB.mkdir();
        testDirC.mkdir();
        final File testDirAFile1 = touch(new File(testDirA, "A-file1.java"));
        final File testDirAFile2 = touch(new File(testDirA, "A-file2.txt")); // filter should ignore this
        final File testDirAFile3 = touch(new File(testDirA, "A-file3.java"));
        File testDirAFile4 = touch(new File(testDirA, "A-file4.java"));
        final File testDirBFile1 = touch(new File(testDirB, "B-file1.java"));

        checkAndNotify();
        checkCollectionSizes("B", 3, 0, 0, 4, 0, 0);
        assertThat(listener.getCreatedDirectories().contains(testDirA)).as("B testDirA").isTrue();
        assertThat(listener.getCreatedDirectories().contains(testDirB)).as("B testDirB").isTrue();
        assertThat(listener.getCreatedDirectories().contains(testDirC)).as("B testDirC").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile1)).as("B testDirAFile1").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile2)).as("B testDirAFile2").isFalse();
        assertThat(listener.getCreatedFiles().contains(testDirAFile3)).as("B testDirAFile3").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile4)).as("B testDirAFile4").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirBFile1)).as("B testDirBFile1").isTrue();

        checkAndNotify();
        checkCollectionsEmpty("C");

        testDirAFile4 = touch(testDirAFile4);
        FileUtils.deleteDirectory(testDirB);
        checkAndNotify();
        checkCollectionSizes("D", 0, 0, 1, 0, 1, 1);
        assertThat(listener.getDeletedDirectories().contains(testDirB)).as("D testDirB").isTrue();
        assertThat(listener.getChangedFiles().contains(testDirAFile4)).as("D testDirAFile4").isTrue();
        assertThat(listener.getDeletedFiles().contains(testDirBFile1)).as("D testDirBFile1").isTrue();

        FileUtils.deleteDirectory(testDir);
        checkAndNotify();
        checkCollectionSizes("E", 0, 0, 2, 0, 0, 3);
        assertThat(listener.getDeletedDirectories().contains(testDirA)).as("E testDirA").isTrue();
        assertThat(listener.getDeletedFiles().contains(testDirAFile1)).as("E testDirAFile1").isTrue();
        assertThat(listener.getDeletedFiles().contains(testDirAFile2)).as("E testDirAFile2").isFalse();
        assertThat(listener.getDeletedFiles().contains(testDirAFile3)).as("E testDirAFile3").isTrue();
        assertThat(listener.getDeletedFiles().contains(testDirAFile4)).as("E testDirAFile4").isTrue();

        testDir.mkdir();
        checkAndNotify();
        checkCollectionsEmpty("F");

        checkAndNotify();
        checkCollectionsEmpty("G");
    }

    /**
     * Test checkAndNotify() creating
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testFileCreate() throws IOException {
        checkAndNotify();
        checkCollectionsEmpty("A");
        File testDirA = new File(testDir, "test-dir-A");
        testDirA.mkdir();
        testDir  = touch(testDir);
        testDirA = touch(testDirA);
        File testDirAFile1 =       new File(testDirA, "A-file1.java");
        final File testDirAFile2 = touch(new File(testDirA, "A-file2.java"));
        File testDirAFile3 =       new File(testDirA, "A-file3.java");
        final File testDirAFile4 = touch(new File(testDirA, "A-file4.java"));
        File testDirAFile5 =       new File(testDirA, "A-file5.java");

        checkAndNotify();
        checkCollectionSizes("B", 1, 0, 0, 2, 0, 0);
        assertThat(listener.getCreatedFiles().contains(testDirAFile1)).as("B testDirAFile1").isFalse();
        assertThat(listener.getCreatedFiles().contains(testDirAFile2)).as("B testDirAFile2").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile3)).as("B testDirAFile3").isFalse();
        assertThat(listener.getCreatedFiles().contains(testDirAFile4)).as("B testDirAFile4").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile5)).as("B testDirAFile5").isFalse();

        assertThat(testDirAFile1.exists()).as("B testDirAFile1 exists").isFalse();
        assertThat(testDirAFile2.exists()).as("B testDirAFile2 exists").isTrue();
        assertThat(testDirAFile3.exists()).as("B testDirAFile3 exists").isFalse();
        assertThat(testDirAFile4.exists()).as("B testDirAFile4 exists").isTrue();
        assertThat(testDirAFile5.exists()).as("B testDirAFile5 exists").isFalse();

        checkAndNotify();
        checkCollectionsEmpty("C");

        // Create file with name < first entry
        testDirAFile1 = touch(testDirAFile1);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("D", 0, 1, 0, 1, 0, 0);
        assertThat(testDirAFile1.exists()).as("D testDirAFile1 exists").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile1)).as("D testDirAFile1").isTrue();

        // Create file with name between 2 entries
        testDirAFile3 = touch(testDirAFile3);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("E", 0, 1, 0, 1, 0, 0);
        assertThat(testDirAFile3.exists()).as("E testDirAFile3 exists").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile3)).as("E testDirAFile3").isTrue();

        // Create file with name > last entry
        testDirAFile5 = touch(testDirAFile5);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("F", 0, 1, 0, 1, 0, 0);
        assertThat(testDirAFile5.exists()).as("F testDirAFile5 exists").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile5)).as("F testDirAFile5").isTrue();
    }

    /**
     * Test checkAndNotify() creating
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testFileUpdate() throws IOException {
        checkAndNotify();
        checkCollectionsEmpty("A");
        File testDirA = new File(testDir, "test-dir-A");
        testDirA.mkdir();
        testDir  = touch(testDir);
        testDirA = touch(testDirA);
        File testDirAFile1 = touch(new File(testDirA, "A-file1.java"));
        final File testDirAFile2 = touch(new File(testDirA, "A-file2.java"));
        File testDirAFile3 = touch(new File(testDirA, "A-file3.java"));
        final File testDirAFile4 = touch(new File(testDirA, "A-file4.java"));
        File testDirAFile5 = touch(new File(testDirA, "A-file5.java"));

        checkAndNotify();
        checkCollectionSizes("B", 1, 0, 0, 5, 0, 0);
        assertThat(listener.getCreatedFiles().contains(testDirAFile1)).as("B testDirAFile1").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile2)).as("B testDirAFile2").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile3)).as("B testDirAFile3").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile4)).as("B testDirAFile4").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile5)).as("B testDirAFile5").isTrue();

        assertThat(testDirAFile1.exists()).as("B testDirAFile1 exists").isTrue();
        assertThat(testDirAFile2.exists()).as("B testDirAFile2 exists").isTrue();
        assertThat(testDirAFile3.exists()).as("B testDirAFile3 exists").isTrue();
        assertThat(testDirAFile4.exists()).as("B testDirAFile4 exists").isTrue();
        assertThat(testDirAFile5.exists()).as("B testDirAFile5 exists").isTrue();

        checkAndNotify();
        checkCollectionsEmpty("C");

        // Update first entry
        testDirAFile1 = touch(testDirAFile1);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("D", 0, 1, 0, 0, 1, 0);
        assertThat(listener.getChangedFiles().contains(testDirAFile1)).as("D testDirAFile1").isTrue();

        // Update file with name between 2 entries
        testDirAFile3 = touch(testDirAFile3);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("E", 0, 1, 0, 0, 1, 0);
        assertThat(listener.getChangedFiles().contains(testDirAFile3)).as("E testDirAFile3").isTrue();

        // Update last entry
        testDirAFile5 = touch(testDirAFile5);
        testDirA      = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("F", 0, 1, 0, 0, 1, 0);
        assertThat(listener.getChangedFiles().contains(testDirAFile5)).as("F testDirAFile5").isTrue();
    }

    /**
     * Test checkAndNotify() deleting
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testFileDelete() throws IOException {
        checkAndNotify();
        checkCollectionsEmpty("A");
        File testDirA = new File(testDir, "test-dir-A");
        testDirA.mkdir();
        testDir  = touch(testDir);
        testDirA = touch(testDirA);
        final File testDirAFile1 = touch(new File(testDirA, "A-file1.java"));
        final File testDirAFile2 = touch(new File(testDirA, "A-file2.java"));
        final File testDirAFile3 = touch(new File(testDirA, "A-file3.java"));
        final File testDirAFile4 = touch(new File(testDirA, "A-file4.java"));
        final File testDirAFile5 = touch(new File(testDirA, "A-file5.java"));

        assertThat(testDirAFile1.exists()).as("B testDirAFile1 exists").isTrue();
        assertThat(testDirAFile2.exists()).as("B testDirAFile2 exists").isTrue();
        assertThat(testDirAFile3.exists()).as("B testDirAFile3 exists").isTrue();
        assertThat(testDirAFile4.exists()).as("B testDirAFile4 exists").isTrue();
        assertThat(testDirAFile5.exists()).as("B testDirAFile5 exists").isTrue();

        checkAndNotify();
        checkCollectionSizes("B", 1, 0, 0, 5, 0, 0);
        assertThat(listener.getCreatedFiles().contains(testDirAFile1)).as("B testDirAFile1").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile2)).as("B testDirAFile2").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile3)).as("B testDirAFile3").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile4)).as("B testDirAFile4").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile5)).as("B testDirAFile5").isTrue();

        checkAndNotify();
        checkCollectionsEmpty("C");

        // Delete first entry
        FileUtils.deleteQuietly(testDirAFile1);
        testDirA = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("D", 0, 1, 0, 0, 0, 1);
        assertThat(testDirAFile1.exists()).as("D testDirAFile1 exists").isFalse();
        assertThat(listener.getDeletedFiles().contains(testDirAFile1)).as("D testDirAFile1").isTrue();

        // Delete file with name between 2 entries
        FileUtils.deleteQuietly(testDirAFile3);
        testDirA = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("E", 0, 1, 0, 0, 0, 1);
        assertThat(testDirAFile3.exists()).as("E testDirAFile3 exists").isFalse();
        assertThat(listener.getDeletedFiles().contains(testDirAFile3)).as("E testDirAFile3").isTrue();

        // Delete last entry
        FileUtils.deleteQuietly(testDirAFile5);
        testDirA = touch(testDirA);
        checkAndNotify();
        checkCollectionSizes("F", 0, 1, 0, 0, 0, 1);
        assertThat(testDirAFile5.exists()).as("F testDirAFile5 exists").isFalse();
        assertThat(listener.getDeletedFiles().contains(testDirAFile5)).as("F testDirAFile5").isTrue();
    }

    /**
     * Test checkAndNotify() method
     * @throws IOException if an I/O error occurs.
     */
    @Test
    public void testObserveSingleFile() throws IOException {
        final File testDirA = new File(testDir, "test-dir-A");
        File testDirAFile1 = new File(testDirA, "A-file1.java");
        testDirA.mkdir();

        final FileFilter nameFilter = FileFilterUtils.nameFileFilter(testDirAFile1.getName());
        createObserver(testDirA, nameFilter);
        checkAndNotify();
        checkCollectionsEmpty("A");
        assertThat(testDirAFile1.exists()).as("A testDirAFile1 exists").isFalse();

        // Create
        testDirAFile1 = touch(testDirAFile1);
        File testDirAFile2 = touch(new File(testDirA, "A-file2.txt"));  /* filter should ignore */
        File testDirAFile3 = touch(new File(testDirA, "A-file3.java")); /* filter should ignore */
        assertThat(testDirAFile1.exists()).as("B testDirAFile1 exists").isTrue();
        assertThat(testDirAFile2.exists()).as("B testDirAFile2 exists").isTrue();
        assertThat(testDirAFile3.exists()).as("B testDirAFile3 exists").isTrue();
        checkAndNotify();
        checkCollectionSizes("C", 0, 0, 0, 1, 0, 0);
        assertThat(listener.getCreatedFiles().contains(testDirAFile1)).as("C created").isTrue();
        assertThat(listener.getCreatedFiles().contains(testDirAFile2)).as("C created").isFalse();
        assertThat(listener.getCreatedFiles().contains(testDirAFile3)).as("C created").isFalse();

        // Modify
        testDirAFile1 = touch(testDirAFile1);
        testDirAFile2 = touch(testDirAFile2);
        testDirAFile3 = touch(testDirAFile3);
        checkAndNotify();
        checkCollectionSizes("D", 0, 0, 0, 0, 1, 0);
        assertThat(listener.getChangedFiles().contains(testDirAFile1)).as("D changed").isTrue();
        assertThat(listener.getChangedFiles().contains(testDirAFile2)).as("D changed").isFalse();
        assertThat(listener.getChangedFiles().contains(testDirAFile3)).as("D changed").isFalse();

        // Delete
        FileUtils.deleteQuietly(testDirAFile1);
        FileUtils.deleteQuietly(testDirAFile2);
        FileUtils.deleteQuietly(testDirAFile3);
        assertThat(testDirAFile1.exists()).as("E testDirAFile1 exists").isFalse();
        assertThat(testDirAFile2.exists()).as("E testDirAFile2 exists").isFalse();
        assertThat(testDirAFile3.exists()).as("E testDirAFile3 exists").isFalse();
        checkAndNotify();
        checkCollectionSizes("E", 0, 0, 0, 0, 0, 1);
        assertThat(listener.getDeletedFiles().contains(testDirAFile1)).as("E deleted").isTrue();
        assertThat(listener.getDeletedFiles().contains(testDirAFile2)).as("E deleted").isFalse();
        assertThat(listener.getDeletedFiles().contains(testDirAFile3)).as("E deleted").isFalse();
    }

    /**
     * Call {@link FileAlterationObserver#checkAndNotify()}.
     */
    protected void checkAndNotify() {
        observer.checkAndNotify();
    }
}
