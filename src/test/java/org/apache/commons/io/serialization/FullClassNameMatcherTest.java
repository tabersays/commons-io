/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.io.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class FullClassNameMatcherTest {

    private static final String [] NAMES_ARRAY = { Integer.class.getName(), Long.class.getName() };

    @Test
    public void noNames() {
        final FullClassNameMatcher m = new FullClassNameMatcher();
        assertThat(m.matches(Integer.class.getName())).isFalse();
    }

    @Test
    public void withNames() {
        final FullClassNameMatcher m = new FullClassNameMatcher(NAMES_ARRAY);
        assertThat(m.matches(Integer.class.getName())).isTrue();
        assertThat(m.matches(String.class.getName())).isFalse();
    }
}