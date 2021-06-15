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
package org.apache.commons.io.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class WindowsLineEndingInputStreamTest {
    @Test
    public void simpleString() throws Exception {
        assertThat(roundtrip("abc")).isEqualTo("abc\r\n");
    }

    @Test
    public void inTheMiddleOfTheLine() throws Exception {
        assertThat(roundtrip("a\r\nbc")).isEqualTo("a\r\nbc\r\n");
    }

    @Test
    public void multipleBlankLines() throws Exception {
        assertThat(roundtrip("a\r\n\r\nbc")).isEqualTo("a\r\n\r\nbc\r\n");
    }

    @Test
    public void twoLinesAtEnd() throws Exception {
        assertThat(roundtrip("a\r\n\r\n")).isEqualTo("a\r\n\r\n");
    }

    @Test
    public void linuxLinefeeds() throws Exception {
        final String roundtrip = roundtrip( "ab\nc", false );
        assertThat(roundtrip).isEqualTo("ab\r\nc");
    }


    @Test
    public void malformed() throws Exception {
        assertThat(roundtrip("a\rbc", false)).isEqualTo("a\rbc");
    }

    @Test
    public void retainLineFeed() throws Exception {
        assertThat(roundtrip("a\r\n\r\n", false)).isEqualTo("a\r\n\r\n");
        assertThat(roundtrip("a", false)).isEqualTo("a");
    }

    private String roundtrip( final String msg ) throws IOException {
        return roundtrip( msg, true );
    }

    private String roundtrip( final String msg, final boolean ensure ) throws IOException {
        final ByteArrayInputStream baos = new ByteArrayInputStream( msg.getBytes(StandardCharsets.UTF_8) );
        final WindowsLineEndingInputStream lf = new WindowsLineEndingInputStream( baos, ensure );
        final byte[] buf = new byte[100];
        final int read = lf.read( buf );
        lf.close();
        return new String( buf, 0, read, StandardCharsets.UTF_8);
    }
}
