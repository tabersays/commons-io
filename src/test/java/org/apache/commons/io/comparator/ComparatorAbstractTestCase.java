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
package org.apache.commons.io.comparator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Base Test case for Comparator implementations.
 */
public abstract class ComparatorAbstractTestCase {

    @TempDir
    public File dir;

    /** comparator instance */
    protected AbstractFileComparator comparator;

    /** reverse comparator instance */
    protected Comparator<File> reverse;

    /** File which compares equal to  "equalFile2" */
    protected File equalFile1;

    /** File which compares equal to  "equalFile1" */
    protected File equalFile2;

    /** File which is less than the "moreFile" */
    protected File lessFile;

    /** File which is more than the "lessFile" */
    protected File moreFile;

    /**
     * Test the comparator.
     */
    @Test
    public void testComparator() {
        assertThat(comparator.compare(equalFile1, equalFile2)).as("equal").isEqualTo(0);
        assertThat(comparator.compare(lessFile, moreFile) < 0).as("less").isTrue();
        assertThat(comparator.compare(moreFile, lessFile) > 0).as("more").isTrue();
    }

    /**
     * Test the comparator reversed.
     */
    @Test
    public void testReverseComparator() {
        assertThat(reverse.compare(equalFile1, equalFile2)).as("equal").isEqualTo(0);
        assertThat(reverse.compare(moreFile, lessFile) < 0).as("less").isTrue();
        assertThat(reverse.compare(lessFile, moreFile) > 0).as("more").isTrue();
    }

    /**
     * Test comparator array sort is null safe.
     */
    @Test
    public void testSortArrayNull() {
        assertThat(comparator.sort((File[]) null)).isNull();
    }

    /**
     * Test the comparator array sort.
     */
    @Test
    public void testSortArray() {
        final File[] files = new File[3];
        files[0] = equalFile1;
        files[1] = moreFile;
        files[2] = lessFile;
        comparator.sort(files);
        assertThat(files[0]).as("equal").isSameAs(lessFile);
        assertThat(files[1]).as("less").isSameAs(equalFile1);
        assertThat(files[2]).as("more").isSameAs(moreFile);
    }

    /**
     * Test the comparator array sort.
     */
    @Test
    public void testSortList() {
        final List<File> files = new ArrayList<>();
        files.add(equalFile1);
        files.add(moreFile);
        files.add(lessFile);
        comparator.sort(files);
        assertThat(files.get(0)).as("equal").isSameAs(lessFile);
        assertThat(files.get(1)).as("less").isSameAs(equalFile1);
        assertThat(files.get(2)).as("more").isSameAs(moreFile);
    }

    /**
     * Test comparator list sort is null safe.
     */
    @Test
    public void testSortListNull() {
        assertThat(comparator.sort((List<File>) null)).isNull();
    }

    /**
     * Test comparator toString.
     */
    @Test
    public void testToString() {
        assertThat(comparator.toString()).as("comparator").isNotNull();
        assertThat(reverse.toString().startsWith("ReverseFileComparator[")).as("reverse").isTrue();
    }
}
