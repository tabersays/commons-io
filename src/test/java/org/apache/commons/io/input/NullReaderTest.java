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
import java.io.Reader;
import org.junit.jupiter.api.Test;

/**
 * JUnit Test Case for {@link NullReader}.
 *
 */
public class NullReaderTest {

    // Use the same message as in java.io.InputStream.reset() in OpenJDK 8.0.275-1.
    private static final String MARK_RESET_NOT_SUPPORTED = "mark/reset not supported";

    @Test
    public void testRead() throws Exception {
        final int size = 5;
        final TestNullReader reader = new TestNullReader(size);
        for (int i = 0; i < size; i++) {
            assertThat(reader.read()).as("Check Value [" + i + "]").isEqualTo(i);
        }

        // Check End of File
        assertThat(reader.read()).as("End of File").isEqualTo(-1);

        // Test reading after the end of file
        try {
            final int result = reader.read();
            fail("Should have thrown an IOException, value=[" + result + "]");
        } catch (final IOException e) {
            assertThat(e.getMessage()).isEqualTo("Read after end of file");
        }

        // Close - should reset
        reader.close();
        assertThat(reader.getPosition()).as("Available after close").isEqualTo(0);
    }

    @Test
    public void testReadCharArray() throws Exception {
        final char[] chars = new char[10];
        final Reader reader = new TestNullReader(15);

        // Read into array
        final int count1 = reader.read(chars);
        assertThat(count1).as("Read 1").isEqualTo(chars.length);
        for (int i = 0; i < count1; i++) {
            assertThat(chars[i]).as("Check Chars 1").isEqualTo(i);
        }

        // Read into array
        final int count2 = reader.read(chars);
        assertThat(count2).as("Read 2").isEqualTo(5);
        for (int i = 0; i < count2; i++) {
            assertThat(chars[i]).as("Check Chars 2").isEqualTo(count1 + i);
        }

        // End of File
        final int count3 = reader.read(chars);
        assertThat(count3).as("Read 3 (EOF)").isEqualTo(-1);

        // Test reading after the end of file
        try {
            final int count4 = reader.read(chars);
            fail("Should have thrown an IOException, value=[" + count4 + "]");
        } catch (final IOException e) {
            assertThat(e.getMessage()).isEqualTo("Read after end of file");
        }

        // reset by closing
        reader.close();

        // Read into array using offset & length
        final int offset = 2;
        final int lth    = 4;
        final int count5 = reader.read(chars, offset, lth);
        assertThat(count5).as("Read 5").isEqualTo(lth);
        for (int i = offset; i < lth; i++) {
            assertThat(chars[i]).as("Check Chars 3").isEqualTo(i);
        }
    }

    @Test
    public void testEOFException() throws Exception {
        final Reader reader = new TestNullReader(2, false, true);
        assertThat(reader.read()).as("Read 1").isEqualTo(0);
        assertThat(reader.read()).as("Read 2").isEqualTo(1);
        try {
            final int result = reader.read();
            fail("Should have thrown an EOFException, value=[" + result + "]");
        } catch (final EOFException e) {
            // expected
        }
        reader.close();
    }

    @Test
    public void testMarkAndReset() throws Exception {
        int position = 0;
        final int readlimit = 10;
        @SuppressWarnings("resource") // this is actually closed
        final Reader reader = new TestNullReader(100, true, false);

        assertThat(reader.markSupported()).as("Mark Should be Supported").isTrue();

        // No Mark
        try {
            reader.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (final IOException e) {
            assertThat(e.getMessage()).as("No Mark IOException message").isEqualTo("No position has been marked");
        }

        for (; position < 3; position++) {
            assertThat(reader.read()).as("Read Before Mark [" + position + "]").isEqualTo(position);
        }

        // Mark
        reader.mark(readlimit);

        // Read further
        for (int i = 0; i < 3; i++) {
            assertThat(reader.read()).as("Read After Mark [" + i + "]").isEqualTo(position + i);
        }

        // Reset
        reader.reset();

        // Read From marked position
        for (int i = 0; i < readlimit + 1; i++) {
            assertThat(reader.read()).as("Read After Reset [" + i + "]").isEqualTo(position + i);
        }

        // Reset after read limit passed
        try {
            reader.reset();
            fail("Read limit exceeded, expected IOException ");
        } catch (final IOException e) {
            assertThat(e.getMessage()).as("Read limit IOException message").isEqualTo("Marked position [" + position
                    + "] is no longer valid - passed the read limit ["
                    + readlimit + "]");
        }
        reader.close();
    }

    @Test
    public void testMarkNotSupported() throws Exception {
        final Reader reader = new TestNullReader(100, false, true);
        assertThat(reader.markSupported()).as("Mark Should NOT be Supported").isFalse();

        try {
            reader.mark(5);
            fail("mark() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertThat(e.getMessage()).as("mark() error message").isEqualTo(MARK_RESET_NOT_SUPPORTED);
        }

        try {
            reader.reset();
            fail("reset() should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            assertThat(e.getMessage()).as("reset() error message").isEqualTo(MARK_RESET_NOT_SUPPORTED);
        }
        reader.close();
    }

    @Test
    public void testSkip() throws Exception {
        final Reader reader = new TestNullReader(10, true, false);
        assertThat(reader.read()).as("Read 1").isEqualTo(0);
        assertThat(reader.read()).as("Read 2").isEqualTo(1);
        assertThat(reader.skip(5)).as("Skip 1").isEqualTo(5);
        assertThat(reader.read()).as("Read 3").isEqualTo(7);
        assertThat(reader.skip(5)).as("Skip 2").isEqualTo(2); // only 2 left to skip
        assertThat(reader.skip(5)).as("Skip 3 (EOF)").isEqualTo(-1); // End of file
        try {
            reader.skip(5); //
            fail("Expected IOException for skipping after end of file");
        } catch (final IOException e) {
            assertThat(e.getMessage()).as("Skip after EOF IOException message").isEqualTo("Skip after end of file");
        }
        reader.close();
    }


    // ------------- Test NullReader implementation -------------

    private static final class TestNullReader extends NullReader {
        public TestNullReader(final int size) {
            super(size);
        }
        public TestNullReader(final int size, final boolean markSupported, final boolean throwEofException) {
            super(size, markSupported, throwEofException);
        }
        @Override
        protected int processChar() {
            return (int)getPosition() - 1;
        }
        @Override
        protected void processChars(final char[] chars, final int offset, final int length) {
            final int startPos = (int)getPosition() - length;
            for (int i = offset; i < length; i++) {
                chars[i] = (char)(startPos + i);
            }
        }

    }
}
