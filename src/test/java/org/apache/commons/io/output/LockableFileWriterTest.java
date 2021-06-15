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
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that files really lock, although no writing is done as
 * the locking is tested only on construction.
 *
 */
public class LockableFileWriterTest {

    @TempDir
    public File temporaryFolder;

    private File file;
    private File lockDir;
    private File lockFile;
    private File altLockDir;
    private File altLockFile;

    @BeforeEach
    public void setUp() {
        file = new File(temporaryFolder, "testlockfile");
        lockDir = new File(System.getProperty("java.io.tmpdir"));
        lockFile = new File(lockDir, file.getName() + ".lck");
        altLockDir = temporaryFolder;
        altLockFile = new File(altLockDir, file.getName() + ".lck");
    }

    //-----------------------------------------------------------------------
    @Test public void testFileLocked() throws IOException {

        // open a valid lockable writer
        try (LockableFileWriter lfw1 = new LockableFileWriter(file)) {
            assertThat(file.exists()).isTrue();
            assertThat(lockFile.exists()).isTrue();

            // try to open a second writer
            try (LockableFileWriter lfw2 = new LockableFileWriter(file)) {
                fail("Somehow able to open a locked file. ");
            } catch(final IOException ioe) {
                final String msg = ioe.getMessage();
                assertThat(msg.startsWith("Can't write file, lock ")).as("Exception message does not start correctly. ").isTrue();
                assertThat(file.exists()).isTrue();
                assertThat(lockFile.exists()).isTrue();
            }

            // try to open a third writer
            try (LockableFileWriter lfw3 = new LockableFileWriter(file)) {
                fail("Somehow able to open a locked file. ");
            } catch(final IOException ioe) {
                final String msg = ioe.getMessage();
                assertThat(msg.startsWith("Can't write file, lock ")).as("Exception message does not start correctly. ").isTrue();
                assertThat(file.exists()).isTrue();
                assertThat(lockFile.exists()).isTrue();
            }
        }
        assertThat(file.exists()).isTrue();
        assertThat(lockFile.exists()).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test public void testAlternateLockDir() throws IOException {
        // open a valid lockable writer
        try (LockableFileWriter lfw1 = new LockableFileWriter(file, "UTF-8" ,true, altLockDir.getAbsolutePath())){
            assertThat(file.exists()).isTrue();
            assertThat(altLockFile.exists()).isTrue();

            // try to open a second writer
            try (LockableFileWriter lfw2 = new LockableFileWriter(file, StandardCharsets.UTF_8, true, altLockDir.getAbsolutePath())){
                fail("Somehow able to open a locked file. ");
            } catch(final IOException ioe) {
                final String msg = ioe.getMessage();
                assertThat(msg.startsWith("Can't write file, lock ")).as("Exception message does not start correctly. ").isTrue();
                assertThat(file.exists()).isTrue();
                assertThat(altLockFile.exists()).isTrue();
            }
        }
        assertThat(file.exists()).isTrue();
        assertThat(altLockFile.exists()).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test public void testFileNotLocked() throws IOException {
        // open a valid lockable writer
        try (LockableFileWriter lfw1 = new LockableFileWriter(file)) {
            assertThat(file.exists()).isTrue();
            assertThat(lockFile.exists()).isTrue();
        }
        assertThat(file.exists()).isTrue();
        assertThat(lockFile.exists()).isFalse();

        // open a second valid writer on the same file
        try (LockableFileWriter lfw2 = new LockableFileWriter(file)) {
            assertThat(file.exists()).isTrue();
            assertThat(lockFile.exists()).isTrue();
        }
        assertThat(file.exists()).isTrue();
        assertThat(lockFile.exists()).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test public void testConstructor_File_encoding_badEncoding() throws IOException {
        try (Writer writer = new LockableFileWriter(file, "BAD-ENCODE")) {
            fail();
        } catch (final UnsupportedCharsetException ex) {
            // expected
            assertThat(file.exists()).isFalse();
            assertThat(lockFile.exists()).isFalse();
        }
        assertThat(file.exists()).isFalse();
        assertThat(lockFile.exists()).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test public void testConstructor_File_directory() {
        try (Writer writer = new LockableFileWriter(temporaryFolder)) {
            fail();
        } catch (final IOException ex) {
            // expected
            assertThat(file.exists()).isFalse();
            assertThat(lockFile.exists()).isFalse();
        }
        assertThat(file.exists()).isFalse();
        assertThat(lockFile.exists()).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test public void testConstructor_File_nullFile() throws IOException {
        try (Writer writer = new LockableFileWriter((File) null)) {
            fail();
        } catch (final NullPointerException ex) {
            // expected
            assertThat(file.exists()).isFalse();
            assertThat(lockFile.exists()).isFalse();
        }
        assertThat(file.exists()).isFalse();
        assertThat(lockFile.exists()).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test public void testConstructor_fileName_nullFile() throws IOException {
        try (Writer writer = new LockableFileWriter((String) null)) {
            fail();
        } catch (final NullPointerException ex) {
            // expected
            assertThat(file.exists()).isFalse();
            assertThat(lockFile.exists()).isFalse();
        }
        assertThat(file.exists()).isFalse();
        assertThat(lockFile.exists()).isFalse();
    }

}
