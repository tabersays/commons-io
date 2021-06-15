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

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test cases for FileUtils.deleteDirectory() method.
 *
 */
public abstract class FileUtilsDeleteDirectoryBaseTestCase {
    @TempDir
    public File top;

    protected abstract boolean setupSymlink(final File res, final File link) throws Exception;

    @Test
    public void testDeleteDirWithASymlinkDir() throws Exception {

        final File realOuter = new File(top, "realouter");
        assertThat(realOuter.mkdirs()).isTrue();

        final File realInner = new File(realOuter, "realinner");
        assertThat(realInner.mkdirs()).isTrue();

        FileUtils.touch(new File(realInner, "file1"));
        assertThat(realInner.list().length).isEqualTo(1);

        final File randomDirectory = new File(top, "randomDir");
        assertThat(randomDirectory.mkdirs()).isTrue();

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertThat(randomDirectory.list().length).isEqualTo(1);

        final File symlinkDirectory = new File(realOuter, "fakeinner");
        assertThat(setupSymlink(randomDirectory, symlinkDirectory)).isTrue();

        assertThat(symlinkDirectory.list().length).isEqualTo(1);

        // assert contents of the real directory were removed including the symlink
        FileUtils.deleteDirectory(realOuter);
        assertThat(top.list().length).isEqualTo(1);

        // ensure that the contents of the symlink were NOT removed.
        assertThat(randomDirectory.list().length).as("Contents of sym link should not have been removed").isEqualTo(1);
    }

    @Test
    public void testDeleteDirWithASymlinkDir2() throws Exception {

        final File realOuter = new File(top, "realouter");
        assertThat(realOuter.mkdirs()).isTrue();

        final File realInner = new File(realOuter, "realinner");
        assertThat(realInner.mkdirs()).isTrue();

        FileUtils.touch(new File(realInner, "file1"));
        assertThat(realInner.list().length).isEqualTo(1);

        final File randomDirectory = new File(top, "randomDir");
        assertThat(randomDirectory.mkdirs()).isTrue();

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertThat(randomDirectory.list().length).isEqualTo(1);

        final File symlinkDirectory = new File(realOuter, "fakeinner");
        Files.createSymbolicLink(symlinkDirectory.toPath(), randomDirectory.toPath());

        assertThat(symlinkDirectory.list().length).isEqualTo(1);

        // assert contents of the real directory were removed including the symlink
        FileUtils.deleteDirectory(realOuter);
        assertThat(top.list().length).isEqualTo(1);

        // ensure that the contents of the symlink were NOT removed.
        assertThat(randomDirectory.list().length).as("Contents of sym link should not have been removed").isEqualTo(1);
    }

    @Test
    public void testDeleteDirWithSymlinkFile() throws Exception {
        final File realOuter = new File(top, "realouter");
        assertThat(realOuter.mkdirs()).isTrue();

        final File realInner = new File(realOuter, "realinner");
        assertThat(realInner.mkdirs()).isTrue();

        final File realFile = new File(realInner, "file1");
        FileUtils.touch(realFile);

        assertThat(realInner.list().length).isEqualTo(1);

        final File randomFile = new File(top, "randomfile");
        FileUtils.touch(randomFile);

        final File symlinkFile = new File(realInner, "fakeinner");
        assertThat(setupSymlink(randomFile, symlinkFile)).isTrue();

        assertThat(realInner.list().length).isEqualTo(2);
        assertThat(top.list().length).isEqualTo(2);

        // assert the real directory were removed including the symlink
        FileUtils.deleteDirectory(realOuter);
        assertThat(top.list().length).isEqualTo(1);

        // ensure that the contents of the symlink were NOT removed.
        assertThat(randomFile.exists()).isTrue();
        assertThat(symlinkFile.exists()).isFalse();
    }

    @Test
    public void testDeleteInvalidLinks() throws Exception {
        final File aFile = new File(top, "realParentDirA");
        assertThat(aFile.mkdir()).isTrue();
        final File bFile = new File(aFile, "realChildDirB");
        assertThat(bFile.mkdir()).isTrue();

        final File cFile = new File(top, "realParentDirC");
        assertThat(cFile.mkdir()).isTrue();
        final File dFile = new File(cFile, "realChildDirD");
        assertThat(dFile.mkdir()).isTrue();

        final File linkToC = new File(bFile, "linkToC");
        Files.createSymbolicLink(linkToC.toPath(), cFile.toPath());

        final File linkToB = new File(dFile, "linkToB");
        Files.createSymbolicLink(linkToB.toPath(), bFile.toPath());

        FileUtils.deleteDirectory(aFile);
        FileUtils.deleteDirectory(cFile);
        assertThat(top.list().length).isEqualTo(0);
    }

    @Test
    public void testDeleteParentSymlink() throws Exception {
        final File realParent = new File(top, "realparent");
        assertThat(realParent.mkdirs()).isTrue();

        final File realInner = new File(realParent, "realinner");
        assertThat(realInner.mkdirs()).isTrue();

        FileUtils.touch(new File(realInner, "file1"));
        assertThat(realInner.list().length).isEqualTo(1);

        final File randomDirectory = new File(top, "randomDir");
        assertThat(randomDirectory.mkdirs()).isTrue();

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertThat(randomDirectory.list().length).isEqualTo(1);

        final File symlinkDirectory = new File(realParent, "fakeinner");
        assertThat(setupSymlink(randomDirectory, symlinkDirectory)).isTrue();

        assertThat(symlinkDirectory.list().length).isEqualTo(1);

        final File symlinkParentDirectory = new File(top, "fakeouter");
        assertThat(setupSymlink(realParent, symlinkParentDirectory)).isTrue();

        // assert only the symlink is deleted, but not followed
        FileUtils.deleteDirectory(symlinkParentDirectory);
        assertThat(top.list().length).isEqualTo(2);

        // ensure that the contents of the symlink were NOT removed.
        assertThat(randomDirectory.list().length).as("Contents of sym link should not have been removed").isEqualTo(1);
    }

    @Test
    public void testDeleteParentSymlink2() throws Exception {
        final File realParent = new File(top, "realparent");
        assertThat(realParent.mkdirs()).isTrue();

        final File realInner = new File(realParent, "realinner");
        assertThat(realInner.mkdirs()).isTrue();

        FileUtils.touch(new File(realInner, "file1"));
        assertThat(realInner.list().length).isEqualTo(1);

        final File randomDirectory = new File(top, "randomDir");
        assertThat(randomDirectory.mkdirs()).isTrue();

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertThat(randomDirectory.list().length).isEqualTo(1);

        final File symlinkDirectory = new File(realParent, "fakeinner");
        Files.createSymbolicLink(symlinkDirectory.toPath(), randomDirectory.toPath());

        assertThat(symlinkDirectory.list().length).isEqualTo(1);

        final File symlinkParentDirectory = new File(top, "fakeouter");
        Files.createSymbolicLink(symlinkParentDirectory.toPath(), realParent.toPath());

        // assert only the symlink is deleted, but not followed
        FileUtils.deleteDirectory(symlinkParentDirectory);
        assertThat(top.list().length).isEqualTo(2);

        // ensure that the contents of the symlink were NOT removed.
        assertThat(randomDirectory.list().length).as("Contents of sym link should not have been removed").isEqualTo(1);
    }

    @Test
    public void testDeletesNested() throws Exception {
        final File nested = new File(top, "nested");
        assertThat(nested.mkdirs()).isTrue();

        assertThat(top.list().length).isEqualTo(1);

        FileUtils.touch(new File(nested, "regular"));
        FileUtils.touch(new File(nested, ".hidden"));

        assertThat(nested.list().length).isEqualTo(2);

        FileUtils.deleteDirectory(nested);

        assertThat(top.list().length).isEqualTo(0);
    }

    @Test
    public void testDeletesRegular() throws Exception {
        final File nested = new File(top, "nested");
        assertThat(nested.mkdirs()).isTrue();

        assertThat(top.list().length).isEqualTo(1);

        assertThat(nested.list().length).isEqualTo(0);

        FileUtils.deleteDirectory(nested);

        assertThat(top.list().length).isEqualTo(0);
    }

}
