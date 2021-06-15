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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.apache.commons.io.test.TestUtils;
import org.junit.jupiter.api.Test;

/**
 * {@link FileAlterationMonitor} Test Case.
 */
public class FileAlterationMonitorTestCase extends AbstractMonitorTestCase {

    /**
     * Construct a new test case.
     *
     */
    public FileAlterationMonitorTestCase() {
        listener = new CollectionFileListener(false);
    }

    /**
     * Test default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        final FileAlterationMonitor monitor = new FileAlterationMonitor();
        assertThat(monitor.getInterval()).as("Interval").isEqualTo(10000);
    }

    @Test
    public void testCollectionConstructorShouldDoNothingWithNullCollection() {
        final Collection<FileAlterationObserver> observers = null;
        final FileAlterationMonitor monitor = new FileAlterationMonitor(0, observers);
        assertThat(monitor.getObservers().iterator().hasNext()).isFalse();
    }

    @Test
    public void testCollectionConstructorShouldDoNothingWithNullObservers() {
        final Collection<FileAlterationObserver> observers = new ArrayList<>(5);
        final FileAlterationMonitor monitor = new FileAlterationMonitor(0, observers);
        assertThat(monitor.getObservers().iterator().hasNext()).isFalse();
    }

    @Test
    public void testCollectionConstructor() {
        observer = new FileAlterationObserver("foo");
        final Collection<FileAlterationObserver> observers = Arrays.asList(observer);
        final FileAlterationMonitor monitor = new FileAlterationMonitor(0, observers);
        final Iterator<FileAlterationObserver> iterator = monitor.getObservers().iterator();
        assertThat(iterator.next()).isEqualTo(observer);
    }

    /**
     * Test add/remove observers.
     */
    @Test
    public void testAddRemoveObservers() {
        FileAlterationObserver[] observers = null;

        // Null Observers
        FileAlterationMonitor monitor = new FileAlterationMonitor(123, observers);
        assertThat(monitor.getInterval()).as("Interval").isEqualTo(123);
        assertThat(monitor.getObservers().iterator().hasNext()).as("Observers[1]").isFalse();

        // Null Observer
        observers = new FileAlterationObserver[1]; // observer is null
        monitor = new FileAlterationMonitor(456, observers);
        assertThat(monitor.getObservers().iterator().hasNext()).as("Observers[2]").isFalse();

        // Null Observer
        monitor.addObserver(null);
        assertThat(monitor.getObservers().iterator().hasNext()).as("Observers[3]").isFalse();
        monitor.removeObserver(null);

        // Add Observer
        final FileAlterationObserver observer = new FileAlterationObserver("foo");
        monitor.addObserver(observer);
        final Iterator<FileAlterationObserver> it = monitor.getObservers().iterator();
        assertThat(it.hasNext()).as("Observers[4]").isTrue();
        assertThat(it.next()).as("Added").isEqualTo(observer);
        assertThat(it.hasNext()).as("Observers[5]").isFalse();

        // Remove Observer
        monitor.removeObserver(observer);
        assertThat(monitor.getObservers().iterator().hasNext()).as("Observers[6]").isFalse();
    }

    /**
     * Test checkAndNotify() method
     * @throws Exception
     */
    @Test
    public void testMonitor() throws Exception {
        final long interval = 100;
        listener.clear();
        final FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        assertThat(monitor.getInterval()).as("Interval").isEqualTo(interval);
        monitor.start();

        try {
            monitor.start(); // try and start again
            fail("Expected IllegalStateException");
        } catch (final IllegalStateException e) {
            // expected result, monitor already running
        }

        // Create a File
        checkCollectionsEmpty("A");
        File file1 = touch(new File(testDir, "file1.java"));
        checkFile("Create", file1, listener.getCreatedFiles());
        listener.clear();

        // Update a file
        checkCollectionsEmpty("B");
        file1 = touch(file1);
        checkFile("Update", file1, listener.getChangedFiles());
        listener.clear();

        // Delete a file
        checkCollectionsEmpty("C");
        file1.delete();
        checkFile("Delete", file1, listener.getDeletedFiles());
        listener.clear();

        // Stop monitoring
        monitor.stop();

        try {
            monitor.stop(); // try and stop again
            fail("Expected IllegalStateException");
        } catch (final IllegalStateException e) {
            // expected result, monitor already stopped
        }
    }

    /**
     * Test using a thread factory.
     * @throws Exception
     */
    @Test
    public void testThreadFactory() throws Exception {
        final long interval = 100;
        listener.clear();
        final FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        monitor.setThreadFactory(Executors.defaultThreadFactory());
        assertThat(monitor.getInterval()).as("Interval").isEqualTo(interval);
        monitor.start();

        // Create a File
        checkCollectionsEmpty("A");
        final File file2 = touch(new File(testDir, "file2.java"));
        checkFile("Create", file2, listener.getCreatedFiles());
        listener.clear();

        // Delete a file
        checkCollectionsEmpty("B");
        file2.delete();
        checkFile("Delete", file2, listener.getDeletedFiles());
        listener.clear();

        // Stop monitoring
        monitor.stop();
    }

    /**
     * Check all the File Collections have the expected sizes.
     */
    private void checkFile(final String label, final File file, final Collection<File> files) {
        for (int i = 0; i < 20; i++) {
            if (files.contains(file)) {
                return; // found, test passes
            }
            TestUtils.sleepQuietly(pauseTime);
        }
        fail(label + " " + file + " not found");
    }

    /**
     * Test case for IO-535
     *
     * Verify that {@link FileAlterationMonitor#stop()} stops the created thread
     */
    @Test
    public void testStopWhileWaitingForNextInterval() throws Exception {
        final Collection<Thread> createdThreads = new ArrayList<>(1);
        final ThreadFactory threadFactory = new ThreadFactory() {
            private final ThreadFactory delegate = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = delegate.newThread(r);
                thread.setDaemon(true); //do not leak threads if the test fails
                createdThreads.add(thread);
                return thread;
            }
        };

        final FileAlterationMonitor monitor = new FileAlterationMonitor(1_000);
        monitor.setThreadFactory(threadFactory);

        monitor.start();
        assertThat(createdThreads.isEmpty()).isFalse();

        Thread.sleep(10); // wait until the watcher thread enters Thread.sleep()
        monitor.stop(100);

        for (final Thread thread : createdThreads) {
            assertThat(thread.isAlive()).as("The FileAlterationMonitor did not stop the threads it created.").isFalse();
        }
    }
}
