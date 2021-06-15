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

import static org.apache.commons.io.StandardLineSeparator.CR;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import static org.apache.commons.io.StandardLineSeparator.LF;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link StandardLineSeparator}.
 */
public class StandardLineSeparatorTest {

    @Test
    public void testCR() {
        assertThat(CR.getString()).isEqualTo("\r");
    }

    @Test
    public void testCR_getBytes() {
        assertThat(CR.getBytes(StandardCharsets.ISO_8859_1)).containsExactly("\r".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testCRLF() {
        assertThat(CRLF.getString()).isEqualTo("\r\n");
    }

    @Test
    public void testCRLF_getBytes() {
        assertThat(CRLF.getBytes(StandardCharsets.ISO_8859_1)).containsExactly("\r\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testLF() {
        assertThat(LF.getString()).isEqualTo("\n");
    }

    @Test
    public void testLF_getBytes() {
        assertThat(LF.getBytes(StandardCharsets.ISO_8859_1)).containsExactly("\n".getBytes(StandardCharsets.ISO_8859_1));
    }

}
