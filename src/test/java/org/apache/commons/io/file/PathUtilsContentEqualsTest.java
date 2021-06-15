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

package org.apache.commons.io.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PathUtilsContentEqualsTest {

    @TempDir
    public File temporaryFolder;

    private String getName() {
        return this.getClass().getSimpleName();
    }

    @Test
    public void testFileContentEquals() throws Exception {
        // Non-existent files
        final Path path1 = new File(temporaryFolder, getName()).toPath();
        final Path path2 = new File(temporaryFolder, getName() + "2").toPath();
        assertThat(PathUtils.fileContentEquals(null, null)).isTrue();
        assertThat(PathUtils.fileContentEquals(null, path1)).isFalse();
        assertThat(PathUtils.fileContentEquals(path1, null)).isFalse();
        // both don't exist
        assertThat(PathUtils.fileContentEquals(path1, path1)).isTrue();
        assertThat(PathUtils.fileContentEquals(path1, path2)).isTrue();
        assertThat(PathUtils.fileContentEquals(path2, path2)).isTrue();
        assertThat(PathUtils.fileContentEquals(path2, path1)).isTrue();

        // Directories
        try {
            PathUtils.fileContentEquals(temporaryFolder.toPath(), temporaryFolder.toPath());
            fail("Comparing directories should fail with an IOException");
        } catch (final IOException ioe) {
            // expected
        }

        // Different files
        final Path objFile1 = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".object");
        objFile1.toFile().deleteOnExit();
        PathUtils.copyFile(getClass().getResource("/java/lang/Object.class"), objFile1);

        final Path objFile1b = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".object2");
        objFile1b.toFile().deleteOnExit();
        PathUtils.copyFile(getClass().getResource("/java/lang/Object.class"), objFile1b);

        final Path objFile2 = Paths.get(temporaryFolder.getAbsolutePath(), getName() + ".collection");
        objFile2.toFile().deleteOnExit();
        PathUtils.copyFile(getClass().getResource("/java/util/Collection.class"), objFile2);

        assertThat(PathUtils.fileContentEquals(objFile1, objFile2)).isFalse();
        assertThat(PathUtils.fileContentEquals(objFile1b, objFile2)).isFalse();
        assertThat(PathUtils.fileContentEquals(objFile1, objFile1b)).isTrue();

        assertThat(PathUtils.fileContentEquals(objFile1, objFile1)).isTrue();
        assertThat(PathUtils.fileContentEquals(objFile1b, objFile1b)).isTrue();
        assertThat(PathUtils.fileContentEquals(objFile2, objFile2)).isTrue();

        // Equal files
        Files.createFile(path1);
        Files.createFile(path2);
        assertThat(PathUtils.fileContentEquals(path1, path1)).isTrue();
        assertThat(PathUtils.fileContentEquals(path1, path2)).isTrue();
    }

    @Test
    public void testDirectoryContentEquals() throws Exception {
        // Non-existent files
        final Path path1 = new File(temporaryFolder, getName()).toPath();
        final Path path2 = new File(temporaryFolder, getName() + "2").toPath();
        assertThat(PathUtils.directoryContentEquals(null, null)).isTrue();
        assertThat(PathUtils.directoryContentEquals(null, path1)).isFalse();
        assertThat(PathUtils.directoryContentEquals(path1, null)).isFalse();
        // both don't exist
        assertThat(PathUtils.directoryContentEquals(path1, path1)).isTrue();
        assertThat(PathUtils.directoryContentEquals(path1, path2)).isTrue();
        assertThat(PathUtils.directoryContentEquals(path2, path2)).isTrue();
        assertThat(PathUtils.directoryContentEquals(path2, path1)).isTrue();
        // Tree equals true tests
        {
            // Trees of files only that contain the same files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only2");
            assertThat(PathUtils.directoryContentEquals(dir1, dir2)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir2, dir2)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir1, dir1)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir2, dir2)).isTrue();
        }
        {
            // Trees of directories containing other directories.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir2");
            assertThat(PathUtils.directoryContentEquals(dir1, dir2)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir2, dir2)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir1, dir1)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir2, dir2)).isTrue();
        }
        {
            // Trees of directories containing other directories and files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            assertThat(PathUtils.directoryContentEquals(dir1, dir2)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir2, dir2)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir1, dir1)).isTrue();
            assertThat(PathUtils.directoryContentEquals(dir2, dir2)).isTrue();
        }
        // Tree equals false tests
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/");
            assertThat(PathUtils.directoryContentEquals(dir1, dir2)).isFalse();
            assertThat(PathUtils.directoryContentEquals(dir2, dir1)).isFalse();
        }
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files");
            assertThat(PathUtils.directoryContentEquals(dir1, dir2)).isFalse();
            assertThat(PathUtils.directoryContentEquals(dir2, dir1)).isFalse();
        }
    }

    @Test
    public void testDirectoryAndFileContentEquals() throws Exception {
        // Non-existent files
        final Path path1 = new File(temporaryFolder, getName()).toPath();
        final Path path2 = new File(temporaryFolder, getName() + "2").toPath();
        assertThat(PathUtils.directoryAndFileContentEquals(null, null)).isTrue();
        assertThat(PathUtils.directoryAndFileContentEquals(null, path1)).isFalse();
        assertThat(PathUtils.directoryAndFileContentEquals(path1, null)).isFalse();
        // both don't exist
        assertThat(PathUtils.directoryAndFileContentEquals(path1, path1)).isTrue();
        assertThat(PathUtils.directoryAndFileContentEquals(path1, path2)).isTrue();
        assertThat(PathUtils.directoryAndFileContentEquals(path2, path2)).isTrue();
        assertThat(PathUtils.directoryAndFileContentEquals(path2, path1)).isTrue();
        // Tree equals true tests
        {
            // Trees of files only that contain the same files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-files-only/directory-files-only2");
            assertThat(PathUtils.directoryAndFileContentEquals(dir1, dir2)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir2, dir2)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir1, dir1)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir2, dir2)).isTrue();
        }
        {
            // Trees of directories containing other directories.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files/dir2");
            assertThat(PathUtils.directoryAndFileContentEquals(dir1, dir2)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir2, dir2)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir1, dir1)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir2, dir2)).isTrue();
        }
        {
            // Trees of directories containing other directories and files.
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1");
            assertThat(PathUtils.directoryAndFileContentEquals(dir1, dir2)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir2, dir2)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir1, dir1)).isTrue();
            assertThat(PathUtils.directoryAndFileContentEquals(dir2, dir2)).isTrue();
        }
        // Tree equals false tests
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/directory-files-only1");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files/dirs-and-files1/");
            assertThat(PathUtils.directoryAndFileContentEquals(dir1, dir2)).isFalse();
            assertThat(PathUtils.directoryAndFileContentEquals(dir2, dir1)).isFalse();
        }
        {
            final Path dir1 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-and-files");
            final Path dir2 = Paths.get("src/test/resources/dir-equals-tests/dir-equals-dirs-then-files");
            assertThat(PathUtils.directoryAndFileContentEquals(dir1, dir2)).isFalse();
            assertThat(PathUtils.directoryAndFileContentEquals(dir2, dir1)).isFalse();
        }
    }

}
