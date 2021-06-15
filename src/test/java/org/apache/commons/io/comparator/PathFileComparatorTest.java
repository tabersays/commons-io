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
import java.util.Comparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link PathFileComparator}.
 */
public class PathFileComparatorTest extends ComparatorAbstractTestCase {


    @BeforeEach
    public void setUp() {
        comparator = (AbstractFileComparator) PathFileComparator.PATH_COMPARATOR;
        reverse = PathFileComparator.PATH_REVERSE;
        equalFile1 = new File("foo/file.txt");
        equalFile2 = new File("foo/file.txt");
        lessFile   = new File("abc/file.txt");
        moreFile   = new File("xyz/file.txt");
    }

    /** Test case sensitivity */
    @Test
    public void testCaseSensitivity() {
        final File file3 = new File("FOO/file.txt");
        final Comparator<File> sensitive = new PathFileComparator(null); /* test null as well */
        assertThat(sensitive.compare(equalFile1, equalFile2)).as("sensitive file1 & file2 = 0").isEqualTo(0);
        assertThat(sensitive.compare(equalFile1, file3) > 0).as("sensitive file1 & file3 > 0").isTrue();
        assertThat(sensitive.compare(equalFile1, lessFile) > 0).as("sensitive file1 & less  > 0").isTrue();

        final Comparator<File> insensitive = PathFileComparator.PATH_INSENSITIVE_COMPARATOR;
        assertThat(insensitive.compare(equalFile1, equalFile2)).as("insensitive file1 & file2 = 0").isEqualTo(0);
        assertThat(insensitive.compare(equalFile1, file3)).as("insensitive file1 & file3 = 0").isEqualTo(0);
        assertThat(insensitive.compare(equalFile1, lessFile) > 0).as("insensitive file1 & file4 > 0").isTrue();
        assertThat(insensitive.compare(file3, lessFile) > 0).as("insensitive file3 & less  > 0").isTrue();
    }
}
