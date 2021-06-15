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

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests {@link PathUtils}.
 */
public class PathUtilsTest extends TestArguments {

    private static final String TEST_JAR_NAME = "test.jar";

    private static final String TEST_JAR_PATH = "src/test/resources/org/apache/commons/io/test.jar";

    private static final String PATH_FIXTURE = "NOTICE.txt";

    /**
     * A temporary directory managed by JUnit.
     */
    @TempDir
    public Path tempDir;

    private FileSystem openArchive(final Path p, final boolean createNew) throws IOException {
        final FileSystem archive;
        if (createNew) {
            final Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            final URI fileUri = p.toAbsolutePath().toUri();
            final URI uri = URI.create("jar:" + fileUri.toASCIIString());
            archive = FileSystems.newFileSystem(uri, env, null);
        } else {
            archive = FileSystems.newFileSystem(p, (ClassLoader) null);
        }
        return archive;
    }

    @Test
    public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePath() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName()).toAbsolutePath();
        try {
            final Path archivePath = Paths.get(TEST_JAR_PATH);
            try (final FileSystem archive = openArchive(archivePath, false)) {
                // relative jar -> absolute dir
                Path sourceDir = archive.getPath("dir1");
                PathUtils.copyDirectory(sourceDir, tempDir);
                assertThat(Files.exists(tempDir.resolve("f1"))).isTrue();

                // absolute jar -> absolute dir
                sourceDir = archive.getPath("/next");
                PathUtils.copyDirectory(sourceDir, tempDir);
                assertThat(Files.exists(tempDir.resolve("dir"))).isTrue();
            }
        } finally {
            PathUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePathReverse() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            try (final FileSystem archive = openArchive(tempDir.resolve(TEST_JAR_NAME), true)) {
                // absolute dir -> relative jar
                Path targetDir = archive.getPath("target");
                Files.createDirectory(targetDir);
                final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2")
                        .toAbsolutePath();
                PathUtils.copyDirectory(sourceDir, targetDir);
                assertThat(Files.exists(targetDir.resolve("dirs-a-file-size-1"))).isTrue();

                // absolute dir -> absolute jar
                targetDir = archive.getPath("/");
                PathUtils.copyDirectory(sourceDir, targetDir);
                assertThat(Files.exists(targetDir.resolve("dirs-a-file-size-1"))).isTrue();
            }
        } finally {
            PathUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCopyDirectoryForDifferentFilesystemsWithRelativePath() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            final Path archivePath = Paths.get(TEST_JAR_PATH);
            try (final FileSystem archive = openArchive(archivePath, false);
                    final FileSystem targetArchive = openArchive(tempDir.resolve(TEST_JAR_NAME), true)) {
                final Path targetDir = targetArchive.getPath("targetDir");
                Files.createDirectory(targetDir);
                // relative jar -> relative dir
                Path sourceDir = archive.getPath("next");
                PathUtils.copyDirectory(sourceDir, targetDir);
                assertThat(Files.exists(targetDir.resolve("dir"))).isTrue();

                // absolute jar -> relative dir
                sourceDir = archive.getPath("/dir1");
                PathUtils.copyDirectory(sourceDir, targetDir);
                assertThat(Files.exists(targetDir.resolve("f1"))).isTrue();
            }
        } finally {
            PathUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCopyDirectoryForDifferentFilesystemsWithRelativePathReverse() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            try (final FileSystem archive = openArchive(tempDir.resolve(TEST_JAR_NAME), true)) {
                // relative dir -> relative jar
                Path targetDir = archive.getPath("target");
                Files.createDirectory(targetDir);
                final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2");
                PathUtils.copyDirectory(sourceDir, targetDir);
                assertThat(Files.exists(targetDir.resolve("dirs-a-file-size-1"))).isTrue();

                // relative dir -> absolute jar
                targetDir = archive.getPath("/");
                PathUtils.copyDirectory(sourceDir, targetDir);
                assertThat(Files.exists(targetDir.resolve("dirs-a-file-size-1"))).isTrue();
            }
        } finally {
            PathUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCopyFile() throws IOException {
        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        try {
            final Path sourceFile = Paths
                .get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
            final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDir);
            assertThat(Files.exists(targetFile)).isTrue();
            assertThat(Files.size(targetFile)).isEqualTo(Files.size(sourceFile));
        } finally {
            PathUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testCreateDirectoriesAlreadyExists() throws IOException {
        assertThat(PathUtils.createParentDirectories(tempDir)).isEqualTo(tempDir.getParent());
    }

    @Test
    public void testCreateDirectoriesNew() throws IOException {
        assertThat(PathUtils.createParentDirectories(tempDir.resolve("child"))).isEqualTo(tempDir);
    }

    @Test
    public void testIsDirectory() throws IOException {
        assertThat(PathUtils.isDirectory(null)).isFalse();

        assertThat(PathUtils.isDirectory(tempDir)).isTrue();
        final Path testFile1 = Files.createTempFile(tempDir, "prefix", null);
        assertThat(PathUtils.isDirectory(testFile1)).isFalse();

        final Path tempDir = Files.createTempDirectory(getClass().getCanonicalName());
        Files.delete(tempDir);
        assertThat(PathUtils.isDirectory(tempDir)).isFalse();
    }

    @Test
    public void testIsRegularFile() throws IOException {
        assertThat(PathUtils.isRegularFile(null)).isFalse();

        assertThat(PathUtils.isRegularFile(tempDir)).isFalse();
        final Path testFile1 = Files.createTempFile(tempDir, "prefix", null);
        assertThat(PathUtils.isRegularFile(testFile1)).isTrue();

        Files.delete(testFile1);
        assertThat(PathUtils.isRegularFile(testFile1)).isFalse();
    }

    @Test
    public void testNewDirectoryStream() throws Exception {
        final PathFilter pathFilter = new NameFileFilter(PATH_FIXTURE);
        try (final DirectoryStream<Path> stream = PathUtils.newDirectoryStream(PathUtils.current(), pathFilter)) {
            final Iterator<Path> iterator = stream.iterator();
            final Path path = iterator.next();
            assertThat(path.getFileName().toString()).isEqualTo(PATH_FIXTURE);
            assertThat(iterator.hasNext()).isFalse();
        }
    }

}
