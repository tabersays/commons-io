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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * This is used to test {@link FileCleaningTracker} for correctness.
 *
 * @see FileCleaningTracker
 */
public class FileCleaningTrackerTestCase {

    @TempDir
    public File temporaryFolder;

    protected FileCleaningTracker newInstance() {
        return new FileCleaningTracker();
    }

    private File testFile;
    private FileCleaningTracker theInstance;

    /**
     */
    @BeforeEach
    public void setUp() {
        testFile = new File(temporaryFolder, "file-test.txt");
        theInstance = newInstance();
    }

    @AfterEach
    public void tearDown() {

        // reset file cleaner class, so as not to break other tests

        /**
         * The following block of code can possibly be removed when the deprecated {@link FileCleaner} is gone. The
         * question is, whether we want to support reuse of {@link FileCleaningTracker} instances, which we should, IMO,
         * not.
         */
        {
            if (theInstance != null) {
                theInstance.q = new ReferenceQueue<>();
                theInstance.trackers.clear();
                theInstance.deleteFailures.clear();
                theInstance.exitWhenFinished = false;
                theInstance.reaper = null;
            }
        }

        theInstance = null;
    }

    //-----------------------------------------------------------------------
    @Test
    public void testFileCleanerFile() throws Exception {
        final String path = testFile.getPath();

        assertThat(testFile.exists()).isFalse();
        RandomAccessFile r = new RandomAccessFile(testFile, "rw");
        assertThat(testFile.exists()).isTrue();

        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        theInstance.track(path, r);
        assertThat(theInstance.getTrackCount()).isEqualTo(1);

        r.close();
        testFile = null;
        r = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        assertThat(new File(path).exists()).withFailMessage(showFailures()).isFalse();
    }

    @Test
    public void testFileCleanerDirectory() throws Exception {
        TestUtils.createFile(testFile, 100);
        assertThat(testFile.exists()).isTrue();
        assertThat(temporaryFolder.exists()).isTrue();

        Object obj = new Object();
        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        theInstance.track(temporaryFolder, obj);
        assertThat(theInstance.getTrackCount()).isEqualTo(1);

        obj = null;

        waitUntilTrackCount();

        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        assertThat(testFile.exists()).isTrue();  // not deleted, as dir not empty
        assertThat(testFile.getParentFile().exists()).isTrue();  // not deleted, as dir not empty
    }

    @Test
    public void testFileCleanerDirectory_NullStrategy() throws Exception {
        TestUtils.createFile(testFile, 100);
        assertThat(testFile.exists()).isTrue();
        assertThat(temporaryFolder.exists()).isTrue();

        Object obj = new Object();
        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        theInstance.track(temporaryFolder, obj, null);
        assertThat(theInstance.getTrackCount()).isEqualTo(1);

        obj = null;

        waitUntilTrackCount();

        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        assertThat(testFile.exists()).isTrue();  // not deleted, as dir not empty
        assertThat(testFile.getParentFile().exists()).isTrue();  // not deleted, as dir not empty
    }

    @Test
    public void testFileCleanerDirectory_ForceStrategy() throws Exception {
        if (!testFile.getParentFile().exists()) {
            throw new IOException("Cannot create file " + testFile
                    + " as the parent directory does not exist");
        }
        try (final BufferedOutputStream output =
                new BufferedOutputStream(new FileOutputStream(testFile))) {
            TestUtils.generateTestData(output, 100);
        }
        assertThat(testFile.exists()).isTrue();
        assertThat(temporaryFolder.exists()).isTrue();

        Object obj = new Object();
        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        theInstance.track(temporaryFolder, obj, FileDeleteStrategy.FORCE);
        assertThat(theInstance.getTrackCount()).isEqualTo(1);

        obj = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(testFile.getParentFile());

        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        assertThat(new File(testFile.getPath()).exists()).withFailMessage(showFailures()).isFalse();
        assertThat(testFile.getParentFile().exists()).withFailMessage(showFailures()).isFalse();
    }

    @Test
    public void testFileCleanerNull() {
        try {
            theInstance.track((File) null, new Object());
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
        try {
            theInstance.track((File) null, new Object(), FileDeleteStrategy.NORMAL);
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
        try {
            theInstance.track((String) null, new Object());
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
        try {
            theInstance.track((String) null, new Object(), FileDeleteStrategy.NORMAL);
            fail();
        } catch (final NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testFileCleanerExitWhenFinishedFirst() throws Exception {
        assertThat(theInstance.exitWhenFinished).isFalse();
        theInstance.exitWhenFinished();
        assertThat(theInstance.exitWhenFinished).isTrue();
        assertThat(theInstance.reaper).isNull();

        waitUntilTrackCount();

        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        assertThat(theInstance.exitWhenFinished).isTrue();
        assertThat(theInstance.reaper).isNull();
    }

    @Test
    public void testFileCleanerExitWhenFinished_NoTrackAfter() {
        assertThat(theInstance.exitWhenFinished).isFalse();
        theInstance.exitWhenFinished();
        assertThat(theInstance.exitWhenFinished).isTrue();
        assertThat(theInstance.reaper).isNull();

        final String path = testFile.getPath();
        final Object marker = new Object();
        try {
            theInstance.track(path, marker);
            fail();
        } catch (final IllegalStateException ex) {
            // expected
        }
        assertThat(theInstance.exitWhenFinished).isTrue();
        assertThat(theInstance.reaper).isNull();
    }

    @Test
    public void testFileCleanerExitWhenFinished1() throws Exception {
        final String path = testFile.getPath();

        assertThat(testFile.exists()).as("1-testFile exists: " + testFile).isFalse();
        RandomAccessFile r = new RandomAccessFile(testFile, "rw");
        assertThat(testFile.exists()).as("2-testFile exists").isTrue();

        assertThat(theInstance.getTrackCount()).as("3-Track Count").isEqualTo(0);
        theInstance.track(path, r);
        assertThat(theInstance.getTrackCount()).as("4-Track Count").isEqualTo(1);
        assertThat(theInstance.exitWhenFinished).as("5-exitWhenFinished").isFalse();
        assertThat(theInstance.reaper.isAlive()).as("6-reaper.isAlive").isTrue();

        assertThat(theInstance.exitWhenFinished).as("7-exitWhenFinished").isFalse();
        theInstance.exitWhenFinished();
        assertThat(theInstance.exitWhenFinished).as("8-exitWhenFinished").isTrue();
        assertThat(theInstance.reaper.isAlive()).as("9-reaper.isAlive").isTrue();

        r.close();
        testFile = null;
        r = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertThat(theInstance.getTrackCount()).as("10-Track Count").isEqualTo(0);
        assertThat(new File(path).exists()).as("11-testFile exists " + showFailures()).isFalse();
        assertThat(theInstance.exitWhenFinished).as("12-exitWhenFinished").isTrue();
        assertThat(theInstance.reaper.isAlive()).as("13-reaper.isAlive").isFalse();
    }

    @Test
    public void testFileCleanerExitWhenFinished2() throws Exception {
        final String path = testFile.getPath();

        assertThat(testFile.exists()).isFalse();
        RandomAccessFile r = new RandomAccessFile(testFile, "rw");
        assertThat(testFile.exists()).isTrue();

        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        theInstance.track(path, r);
        assertThat(theInstance.getTrackCount()).isEqualTo(1);
        assertThat(theInstance.exitWhenFinished).isFalse();
        assertThat(theInstance.reaper.isAlive()).isTrue();

        r.close();
        testFile = null;
        r = null;

        waitUntilTrackCount();
        pauseForDeleteToComplete(new File(path));

        assertThat(theInstance.getTrackCount()).isEqualTo(0);
        assertThat(new File(path).exists()).withFailMessage(showFailures()).isFalse();
        assertThat(theInstance.exitWhenFinished).isFalse();
        assertThat(theInstance.reaper.isAlive()).isTrue();

        assertThat(theInstance.exitWhenFinished).isFalse();
        theInstance.exitWhenFinished();
        for (int i = 0; i < 20 && theInstance.reaper.isAlive(); i++) {
            TestUtils.sleep(500L);  // allow reaper thread to die
        }
        assertThat(theInstance.exitWhenFinished).isTrue();
        assertThat(theInstance.reaper.isAlive()).isFalse();
    }

    //-----------------------------------------------------------------------
    private void pauseForDeleteToComplete(File file) {
        int count = 0;
        while(file.exists() && count++ < 40) {
            try {
                TestUtils.sleep(500L);
            } catch (final InterruptedException ignore) {
            }
            file = new File(file.getPath());
        }
    }
    private String showFailures() {
        if (theInstance.deleteFailures.size() == 1) {
            return "[Delete Failed: " + theInstance.deleteFailures.get(0) + "]";
        }
        return "[Delete Failures: " + theInstance.deleteFailures.size() + "]";
    }

    private void waitUntilTrackCount() throws Exception {
        System.gc();
        TestUtils.sleep(500);
        int count = 0;
        while(theInstance.getTrackCount() != 0 && count++ < 5) {
            List<String> list = new ArrayList<>();
            try {
                long i = 0;
                while (theInstance.getTrackCount() != 0) {
                    list.add("A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String A Big String " + (i++));
                }
            } catch (final Throwable ignored) {
            }
            list = null;
            System.gc();
            TestUtils.sleep(1000);
        }
        if (theInstance.getTrackCount() != 0) {
            throw new IllegalStateException("Your JVM is not releasing References, try running the testcase with less memory (-Xmx)");
        }

    }
}
