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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link NullInputStream}.
 *
 */
public class NullInputStreamTest {

    // Use the same message as in java.io.InputStream.reset() in OpenJDK 8.0.275-1.
    private static final String MARK_RESET_NOT_SUPPORTED = "mark/reset not supported";

    @Test
    public void testRead() throws Exception {
        final int size = 5;
        final InputStream input = new TestNullInputStream(size);
        for (int i = 0; i < size; i++) {
            assertThat(input.available()).as("Check Size [" + i + "]").isEqualTo(size - i);
            assertThat(input.read()).as("Check Value [" + i + "]").isEqualTo(i);
        }
        assertThat(input.available()).as("Available after contents all read").isEqualTo(0);

        // Check available is zero after End of file
        assertThat(input.read()).as("End of File").isEqualTo(-1);
        assertThat(input.available()).as("Available after End of File").isEqualTo(0);

        // Test reading after the end of file
        try {
            final int result = input.read();
            fail("Should have thrown an IOException, byte=[" + result + "]");
        } catch (final IOException e) {
            assertThat(e.getMessage()).isEqualTo("Read after end of file");
        }

        // Close - should reset
        input.close();
        assertThat(input.available()).as("Available after close").isEqualTo(size);
    }

    @Test
    public void testReadByteArray() throws Exception {
        final byte[] bytes = new byte[10];
        final InputStream input = new TestNullInputStream(15);

        // Read into array
        final int count1 = input.read(bytes);
        assertThat(count1).as("Read 1").isEqualTo(bytes.length);
        for (int i = 0; i < count1; i++) {
            assertThat(bytes[i]).as("Check Bytes 1").isEqualTo(i);
        }

        // Read into array
        final int count2 = input.read(bytes);
        assertThat(count2).as("Read 2").isEqualTo(5);
        for (int i = 0; i < count2; i++) {
            assertThat(bytes[i]).as("Check Bytes 2").isEqualTo(count1 + i);
        }

        // End of File
        final int count3 = input.read(bytes);
        assertThat(count3).as("Read 3 (EOF)").isEqualTo(-1);

        // Test reading after the end of file
        try {
            final int count4 = input.read(bytes);
            fail("Should have thrown an IOException, byte=[" + count4 + "]");
        } catch (final IOException e) {
            assertThat(e.getMessage()).isEqualTo("Read after end of file");
        }

        // reset by closing
        input.close();

        // Read into array using offset & length
        final int offset = 2;
        final int lth    = 4;
        final int count5 = input.read(bytes, offset, lth);
        assertThat(count5).as("Read 5").isEqualTo(lth);
        for (int i = offset; i < lth; i++) {
            assertThat(bytes[i]).as("Check Bytes 2").isEqualTo(i);
        }
    }

    @Test
    public void testEOFException() throws Exception {
        final InputStream input = new TestNullInputStream(2, false, true);
        assertThat(input.read()).as("Read 1").isEqualTo(0);
        assertThat(input.read()).as("Read 2").isEqualTo(1);
        try {
            final int result = input.read();
            fail("Should have thrown an EOFException, byte=[" + result + "]");
        } catch (final EOFException e) {
            // expected
        }
        input.close();
    }

    @Test
    public void testMarkAndReset() throws Exception {
        int position = 0;
        final int readlimit = 10;
        @SuppressWarnings("resource") // this is actually closed
        final InputStream input = new TestNullInputStream(100, true, false);

        assertThat(input.markSupported()).as("Mark Should be Supported").isTrue();

        // No Mark
        try {
            input.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (final IOException e) {
            assertThat(e.getMessage()).as("No Mark IOException message").isEqualTo("No position has been marked");
        }

        for (; position < 3; position++) {
            assertThat(input.read()).as("Read Before Mark [" + position + "]").isEqualTo(position);
        }

        // Mark
        input.mark(readlimit);

        // Read further
        for (int i = 0; i < 3; i++) {
            assertThat(input.read()).as("Read After Mark [" + i + "]").isEqualTo(position + i);
        }

        // Reset
        input.reset();

        // Read From marked position
        for (int i = 0; i < readlimit + 1; i++) {
            assertThat(input.read()).as("Read After Reset [" + i + "]").isEqualTo(position + i);
        }

        // Reset after read limit passed
        try {
            input.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (final IOException e) {
            assertThat(e.getMessage()).as("Read limit IOException message").isEqualTo("Marked position [" + position
                    + "] is no longer valid - passed the read limit ["
                    + readlimit + "]");
        }
        input.close();
    }

    @Test
    public void testMarkNotSupported() throws Exception {
        final InputStream input = new TestNullInputStream(100, false, true);
        assertThat(input.markSupported()).as("Mark Should NOT be Supported").isFalse();

        try {
            input.mark(5);
            fail("mark() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertThat(e.getMessage()).as("mark() error message").isEqualTo(MARK_RESET_NOT_SUPPORTED);
        }

        try {
            input.reset();
            fail("reset() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertThat(e.getMessage()).as("reset() error message").isEqualTo(MARK_RESET_NOT_SUPPORTED);
        }
        input.close();
    }

    @Test
    public void testSkip() throws Exception {
        final InputStream input = new TestNullInputStream(10, true, false);
        assertThat(input.read()).as("Read 1").isEqualTo(0);
        assertThat(input.read()).as("Read 2").isEqualTo(1);
        assertThat(input.skip(5)).as("Skip 1").isEqualTo(5);
        assertThat(input.read()).as("Read 3").isEqualTo(7);
        assertThat(input.skip(5)).as("Skip 2").isEqualTo(2); // only 2 left to skip
        assertThat(input.skip(5)).as("Skip 3 (EOF)").isEqualTo(-1); // End of file
        try {
            input.skip(5); //
            fail("Expected IOException for skipping after end of file");
        } catch (final IOException e) {
            assertThat(e.getMessage()).as("Skip after EOF IOException message").isEqualTo("Skip after end of file");
        }
        input.close();
    }


    // ------------- Test NullInputStream implementation -------------

    private static final class TestNullInputStream extends NullInputStream {
        public TestNullInputStream(final int size) {
            super(size);
        }
        public TestNullInputStream(final int size, final boolean markSupported, final boolean throwEofException) {
            super(size, markSupported, throwEofException);
        }
        @Override
        protected int processByte() {
            return (int)getPosition() - 1;
        }
        @Override
        protected void processBytes(final byte[] bytes, final int offset, final int length) {
            final int startPos = (int)getPosition() - length;
            for (int i = offset; i < length; i++) {
                bytes[i] = (byte)(startPos + i);
            }
        }

    }
}
