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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class MarkShieldInputStreamTest {

    @Test
    public void markIsNoOpWhenUnderlyingDoesNotSupport() throws IOException {
        try (final MarkTestableInputStream in = new MarkTestableInputStream(new NullInputStream(64, false, false));
             final MarkShieldInputStream msis = new MarkShieldInputStream(in)) {

            msis.mark(1024);

            assertThat(in.markcount).isEqualTo(0);
            assertThat(in.readlimit).isEqualTo(0);
        }
    }

    @Test
    public void markIsNoOpWhenUnderlyingSupports() throws IOException {
        try (final MarkTestableInputStream in = new MarkTestableInputStream(new NullInputStream(64, true, false));
             final MarkShieldInputStream msis = new MarkShieldInputStream(in)) {

            msis.mark(1024);

            assertThat(in.markcount).isEqualTo(0);
            assertThat(in.readlimit).isEqualTo(0);
        }
    }

    @Test
    public void markSupportedIsFalseWhenUnderlyingFalse() throws IOException {
        // test wrapping an underlying stream which does NOT support marking
        try (final InputStream is = new NullInputStream(64, false, false)) {
            assertThat(is.markSupported()).isFalse();

            try (final MarkShieldInputStream msis = new MarkShieldInputStream(is)) {
                assertThat(msis.markSupported()).isFalse();
            }
        }
    }

    @Test
    public void markSupportedIsFalseWhenUnderlyingTrue() throws IOException {
        // test wrapping an underlying stream which supports marking
        try (final InputStream is = new NullInputStream(64, true, false)) {
            assertThat(is.markSupported()).isTrue();

            try (final MarkShieldInputStream msis = new MarkShieldInputStream(is)) {
                assertThat(msis.markSupported()).isFalse();
            }
        }
    }

    @Test
    public void resetThrowsExceptionWhenUnderylingDoesNotSupport() throws IOException {
        // test wrapping an underlying stream which does NOT support marking
        try (final MarkShieldInputStream msis = new MarkShieldInputStream(
                new NullInputStream(64, false, false))) {
            assertThrows(UnsupportedOperationException.class, () -> msis.reset());
        }
    }

    @Test
    public void resetThrowsExceptionWhenUnderylingSupports() throws IOException {
        // test wrapping an underlying stream which supports marking
        try (final MarkShieldInputStream msis = new MarkShieldInputStream(
                new NullInputStream(64, true, false))) {
            assertThrows(UnsupportedOperationException.class, () -> msis.reset());
        }
    }

    private static class MarkTestableInputStream extends ProxyInputStream {
        int markcount;
        int readlimit;

        public MarkTestableInputStream(final InputStream in) {
            super(in);
        }

        @SuppressWarnings("sync-override")
        @Override
        public void mark(final int readlimit) {
            // record that `mark` was called
            this.markcount++;
            this.readlimit = readlimit;

            // invoke on super
            super.mark(readlimit);
        }
    }
}
