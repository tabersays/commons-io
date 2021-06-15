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
package org.apache.commons.io.filefilter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.file.CounterAssertions;
import org.apache.commons.io.file.Counters;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DirectoryFileFilter}.
 */
public class DirectoryFileFilterTest {

    /**
     * Javadoc example.
     *
     * System.out calls are commented out here but not in the Javadoc.
     */
    @Test
    public void testJavadocExampleUsingIo() {
        final File dir = new File(".");
        final String[] files = dir.list(DirectoryFileFilter.INSTANCE);
        for (final String file : files) {
            // System.out.println(files[i]);
        }
        // End of Javadoc example
        assertThat(files.length > 0).isTrue();
    }

    /**
     * Javadoc example.
     *
     * System.out calls are commented out here but not in the Javadoc.
     */
    @Test
    public void testJavadocExampleUsingNio() throws IOException {
        final Path dir = Paths.get("");
        final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(DirectoryFileFilter.INSTANCE,
            TrueFileFilter.INSTANCE);
        //
        // Walk one dir
        Files.walkFileTree(dir, Collections.emptySet(), 1, visitor);
        // System.out.println(visitor.getPathCounters());
        // System.out.println(visitor.getFileList());
        //
        visitor.getPathCounters().reset();
        //
        // Walk dir tree
        Files.walkFileTree(dir, visitor);
        // System.out.println(visitor.getPathCounters());
        // System.out.println(visitor.getDirList());
        // System.out.println(visitor.getFileList());
        //
        // End of Javadoc example
        assertThat(visitor.getPathCounters().getFileCounter().get()).isEqualTo(0);
        assertThat(visitor.getPathCounters().getDirectoryCounter().get() > 0).isTrue();
        assertThat(visitor.getPathCounters().getByteCounter().get()).isEqualTo(0);
        // We counted and accumulated
        assertThat(visitor.getDirList().isEmpty()).isFalse();
        assertThat(visitor.getFileList().isEmpty()).isFalse();
        //
        assertThat(visitor.getPathCounters()).isNotEqualTo(Counters.noopPathCounters());
        visitor.getPathCounters().reset();
        CounterAssertions.assertZeroCounters(visitor.getPathCounters());
    }

}
