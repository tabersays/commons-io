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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.junit.jupiter.api.Test;

/**
 * Test {@link ProxyWriter}.
 *
 */
public class ProxyWriterTest {

    @Test
    public void appendCharSequence() throws Exception {
        try (final StringBuilderWriter writer = new StringBuilderWriter();
                final ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.append("ABC");
            assertThat(writer.toString()).isEqualTo("ABC");
        }
    }

    @Test
    public void appendCharSequence_with_offset() throws Exception {
        try (final StringBuilderWriter writer = new StringBuilderWriter();
                final ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.append("ABC", 1, 3);
            proxy.flush();
            assertThat(writer.toString()).isEqualTo("BC");
        }
    }

    @Test
    public void appendChar() throws Exception {
        try (final StringBuilderWriter writer = new StringBuilderWriter();
                final ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.append('c');
            assertThat(writer.toString()).isEqualTo("c");
        }
    }

    @Test
    public void writeString() throws Exception {
        try (final StringBuilderWriter writer = new StringBuilderWriter();
                final ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write("ABC");
            assertThat(writer.toString()).isEqualTo("ABC");
        }
    }

    @Test
    public void writeStringPartial() throws Exception {
        try (final StringBuilderWriter writer = new StringBuilderWriter();
                final ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write("ABC", 1, 2);
            assertThat(writer.toString()).isEqualTo("BC");
        }
    }

    @Test
    public void writeCharArray() throws Exception {
        try (final StringBuilderWriter writer = new StringBuilderWriter();
                final ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write(new char[] { 'A', 'B', 'C' });
            assertThat(writer.toString()).isEqualTo("ABC");
        }
    }

    @Test
    public void writeInt() throws Exception {
        try (final StringBuilderWriter writer = new StringBuilderWriter();
                final ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write(65);
            assertThat(writer.toString()).isEqualTo("A");
        }
    }

    @Test
    public void writeCharArrayPartial() throws Exception {
        try (final StringBuilderWriter writer = new StringBuilderWriter();
                final ProxyWriter proxy = new ProxyWriter(writer)) {
            proxy.write(new char[] { 'A', 'B', 'C' }, 1, 2);
            assertThat(writer.toString()).isEqualTo("BC");
        }
    }

    @Test
    public void nullString() throws Exception {
        try (final ProxyWriter proxy = new ProxyWriter(NullWriter.NULL_WRITER)) {
            proxy.write((String) null);
            proxy.write((String) null, 0, 0);
        }
    }

    @Test
    public void nullCharArray() throws Exception {
        try (final ProxyWriter proxy = new ProxyWriter(NullWriter.NULL_WRITER)) {
            proxy.write((char[]) null);
            proxy.write((char[]) null, 0, 0);
        }
    }

    @Test
    public void nullCharSequence() throws Exception {
        try (final ProxyWriter proxy = new ProxyWriter(NullWriter.NULL_WRITER)) {
            proxy.append(null);
        }
    }

    @Test
    public void exceptions_in_append_char() throws IOException {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final OutputStreamWriter osw = new OutputStreamWriter(baos) {
                    @Override
                    public void write(final int c) throws IOException {
                        throw new UnsupportedEncodingException("Bah");
                    }
                }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.append('c'));
            }
        }
    }

    @Test
    public void exceptions_in_append_charSequence() throws IOException {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public Writer append(final CharSequence csq) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.append("ABCE"));
            }
        }
    }

    @Test
    public void exceptions_in_append_charSequence_offset() throws IOException {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.append("ABCE", 1, 2));
            }
        }
    }

    @Test
    public void exceptions_in_write_int() throws IOException {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final int c) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write('a'));
            }
        }
    }

    @Test
    public void exceptions_in_write_char_array() throws IOException {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final char[] cbuf) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write("ABCE".toCharArray()));
            }
        }
    }

    @Test
    public void exceptions_in_write_offset_char_array() throws IOException {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final char[] cbuf, final int off, final int len) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write("ABCE".toCharArray(), 2, 3));
            }
        }
    }

    @Test
    public void exceptions_in_write_string() throws IOException {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final String str) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write("ABCE"));
            }
        }
    }

    @Test
    public void exceptions_in_write_string_offset() throws IOException {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void write(final String str, final int off, final int len) throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, () -> proxy.write("ABCE", 1, 3));
            }
        }
    }

    @Test
    public void exceptions_in_flush() throws IOException {
        try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
            @Override
            public void flush() throws IOException {
                throw new UnsupportedEncodingException("Bah");
            }
        }) {
            try (ProxyWriter proxy = new ProxyWriter(osw)) {
                assertThrows(UnsupportedEncodingException.class, proxy::flush);
            }
        }
    }

    @Test
    public void exceptions_in_close() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            try (final OutputStreamWriter osw = new OutputStreamWriter(new ByteArrayOutputStream()) {
                @Override
                public void close() throws IOException {
                    throw new UnsupportedEncodingException("Bah");
                }
            }) {
                try (final ProxyWriter proxy = new ProxyWriter(osw)) {
                    // noop
                }
            }
        });
    }
}
