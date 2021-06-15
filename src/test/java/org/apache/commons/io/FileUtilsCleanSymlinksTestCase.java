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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test cases for FileUtils.cleanDirectory() method that involve symlinks.
 * &amp; FileUtils.isSymlink(File file)
 */
public class FileUtilsCleanSymlinksTestCase {

    @TempDir
    public File top;

    @Test
    public void testCleanDirWithSymlinkFile() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // Can't use "ln" for symlinks on the command line in Windows.
            return;
        }

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

        // assert contents of the real directory were removed including the symlink
        FileUtils.cleanDirectory(realOuter);
        assertThat(realOuter.list().length).isEqualTo(0);

        // ensure that the contents of the symlink were NOT removed.
        assertThat(randomFile.exists()).isTrue();
        assertThat(symlinkFile.exists()).isFalse();
    }


    @Test
    public void testCleanDirWithASymlinkDir() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // Can't use "ln" for symlinks on the command line in Windows.
            return;
        }

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
        FileUtils.cleanDirectory(realOuter);
        assertThat(realOuter.list().length).isEqualTo(0);

        // ensure that the contents of the symlink were NOT removed.
        assertThat(randomDirectory.list().length).as("Contents of sym link should not have been removed").isEqualTo(1);
    }

    @Test
    public void testCleanDirWithParentSymlinks() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // Can't use "ln" for symlinks on the command line in Windows.
            return;
        }

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

        // assert contents of the real directory were removed including the symlink
        FileUtils.cleanDirectory(symlinkParentDirectory);// should clean the contents of this but not recurse into other links
        assertThat(symlinkParentDirectory.list().length).isEqualTo(0);
        assertThat(realParent.list().length).isEqualTo(0);

        // ensure that the contents of the symlink were NOT removed.
        assertThat(randomDirectory.list().length).as("Contents of sym link should not have been removed").isEqualTo(1);
    }

    @Test
    public void testStillClearsIfGivenDirectoryIsASymlink() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // Can't use "ln" for symlinks on the command line in Windows.
            return;
        }

        final File randomDirectory = new File(top, "randomDir");
        assertThat(randomDirectory.mkdirs()).isTrue();

        FileUtils.touch(new File(randomDirectory, "randomfile"));
        assertThat(randomDirectory.list().length).isEqualTo(1);

        final File symlinkDirectory = new File(top, "fakeDir");
        assertThat(setupSymlink(randomDirectory, symlinkDirectory)).isTrue();

        FileUtils.cleanDirectory(symlinkDirectory);
        assertThat(symlinkDirectory.list().length).isEqualTo(0);
        assertThat(randomDirectory.list().length).isEqualTo(0);
    }


    @Test
    public void testIdentifiesSymlinkDir() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // Can't use "ln" for symlinks on the command line in Windows.
            return;
        }

        final File randomDirectory = new File(top, "randomDir");
        assertThat(randomDirectory.mkdirs()).isTrue();

        final File symlinkDirectory = new File(top, "fakeDir");
        assertThat(setupSymlink(randomDirectory, symlinkDirectory)).isTrue();

        assertThat(FileUtils.isSymlink(symlinkDirectory)).isTrue();
        assertThat(FileUtils.isSymlink(randomDirectory)).isFalse();
    }

    @Test
    public void testIdentifiesSymlinkFile() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // Can't use "ln" for symlinks on the command line in Windows.
            return;
        }

        final File randomFile = new File(top, "randomfile");
        FileUtils.touch(randomFile);

        final File symlinkFile = new File(top, "fakeinner");
        assertThat(setupSymlink(randomFile, symlinkFile)).isTrue();

        assertThat(FileUtils.isSymlink(symlinkFile)).isTrue();
        assertThat(FileUtils.isSymlink(randomFile)).isFalse();
    }

    @Test
    public void testIdentifiesBrokenSymlinkFile() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // Can't use "ln" for symlinks on the command line in Windows.
            return;
        }

        final File noexistFile = new File(top, "noexist");
        final File symlinkFile = new File(top, "fakeinner");
        final File badSymlinkInPathFile = new File(symlinkFile, "fakeinner");
        final File noexistParentFile = new File("noexist", "file");

        assertThat(setupSymlink(noexistFile, symlinkFile)).isTrue();

        assertThat(FileUtils.isSymlink(symlinkFile)).isTrue();
        assertThat(FileUtils.isSymlink(noexistFile)).isFalse();
        assertThat(FileUtils.isSymlink(noexistParentFile)).isFalse();
        assertThat(FileUtils.isSymlink(badSymlinkInPathFile)).isFalse();
    }

    @Test
    public void testCorrectlyIdentifySymlinkWithParentSymLink() throws Exception {
        if (System.getProperty("os.name").startsWith("Win")) {
            // Can't use "ln" for symlinks on the command line in Windows.
            return;
        }

        final File realParent = new File(top, "realparent");
        assertThat(realParent.mkdirs()).isTrue();

        final File symlinkParentDirectory = new File(top, "fakeparent");
        assertThat(setupSymlink(realParent, symlinkParentDirectory)).isTrue();

        final File realChild = new File(symlinkParentDirectory, "realChild");
        assertThat(realChild.mkdirs()).isTrue();

        final File symlinkChild = new File(symlinkParentDirectory, "fakeChild");
        assertThat(setupSymlink(realChild, symlinkChild)).isTrue();

        assertThat(FileUtils.isSymlink(symlinkChild)).isTrue();
        assertThat(FileUtils.isSymlink(realChild)).isFalse();
    }

    private boolean setupSymlink(final File res, final File link) throws Exception {
        // create symlink
        final List<String> args = new ArrayList<>();
        args.add("ln");
        args.add("-s");

        args.add(res.getAbsolutePath());
        args.add(link.getAbsolutePath());

        final Process proc;

        proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
        return proc.waitFor() == 0;
    }

}
