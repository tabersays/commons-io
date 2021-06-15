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

import java.io.EOFException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link IOExceptionList}.
 */
public class IOExceptionListTestCase {

    @Test
    public void testCause() {
        final EOFException cause = new EOFException();
        final List<EOFException> list = Collections.singletonList(cause);
        final IOExceptionList sqlExceptionList = new IOExceptionList(list);
        assertThat(sqlExceptionList.getCause()).isEqualTo(cause);
        assertThat(sqlExceptionList.getCause(0)).isEqualTo(cause);
        assertThat(sqlExceptionList.getCauseList()).isEqualTo(list);
        assertThat(sqlExceptionList.getCauseList(EOFException.class)).isEqualTo(list);
        assertThat(sqlExceptionList.getCause(0, EOFException.class)).isEqualTo(cause);
        // No CCE:
        final List<EOFException> causeList = sqlExceptionList.getCauseList();
        assertThat(causeList).isEqualTo(list);
    }

    @Test
    public void testMessageCause() {
        final EOFException cause = new EOFException();
        final List<EOFException> list = Collections.singletonList(cause);
        final IOExceptionList sqlExceptionList = new IOExceptionList("Hello", list);
        assertThat(sqlExceptionList.getMessage()).isEqualTo("Hello");
        //
        assertThat(sqlExceptionList.getCause()).isEqualTo(cause);
        assertThat(sqlExceptionList.getCause(0)).isEqualTo(cause);
        assertThat(sqlExceptionList.getCauseList()).isEqualTo(list);
        assertThat(sqlExceptionList.getCauseList(EOFException.class)).isEqualTo(list);
        assertThat(sqlExceptionList.getCause(0, EOFException.class)).isEqualTo(cause);
        // No CCE:
        final List<EOFException> causeList = sqlExceptionList.getCauseList();
        assertThat(causeList).isEqualTo(list);
    }

    @Test
    public void testNullCause() {
        final IOExceptionList sqlExceptionList = new IOExceptionList(null);
        assertThat(sqlExceptionList.getCause()).isNull();
        assertThat(sqlExceptionList.getCauseList().isEmpty()).isTrue();
    }

    @Test
    public void testPrintStackTrace() {
        final EOFException cause = new EOFException();
        final List<EOFException> list = Collections.singletonList(cause);
        final IOExceptionList sqlExceptionList = new IOExceptionList(list);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        sqlExceptionList.printStackTrace(pw);
        final String st = sw.toString();
        assertThat(st.startsWith("org.apache.commons.io.IOExceptionList: 1 exceptions: [java.io.EOFException]")).isTrue();
        assertThat(st.contains("Caused by: java.io.EOFException")).isTrue();
    }
}
