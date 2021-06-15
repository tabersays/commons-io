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

import java.io.File;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class FilenameUtilsWildcardTestCase {

    private static final boolean WINDOWS = File.separatorChar == '\\';

    //-----------------------------------------------------------------------
    // Testing:
    //   FilenameUtils.wildcardMatch(String,String)

    @Test
    public void testMatch() {
        assertThat(FilenameUtils.wildcardMatch(null, "Foo")).isFalse();
        assertThat(FilenameUtils.wildcardMatch("Foo", null)).isFalse();
        assertThat(FilenameUtils.wildcardMatch(null, null)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Foo")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("", "")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("", "*")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("", "?")).isFalse();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Fo*")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Fo?")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo Bar and Catflap", "Fo*")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("New Bookmarks", "N?w ?o?k??r?s")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Bar")).isFalse();
        assertThat(FilenameUtils.wildcardMatch("Foo Bar Foo", "F*o Bar*")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Adobe Acrobat Installer", "Ad*er")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "*Foo")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("BarFoo", "*Foo")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Foo*")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("FooBar", "Foo*")).isTrue();
        assertThat(FilenameUtils.wildcardMatch("FOO", "*Foo")).isFalse();
        assertThat(FilenameUtils.wildcardMatch("BARFOO", "*Foo")).isFalse();
        assertThat(FilenameUtils.wildcardMatch("FOO", "Foo*")).isFalse();
        assertThat(FilenameUtils.wildcardMatch("FOOBAR", "Foo*")).isFalse();
    }

    @Test
    public void testMatchOnSystem() {
        assertThat(FilenameUtils.wildcardMatchOnSystem(null, "Foo")).isFalse();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo", null)).isFalse();
        assertThat(FilenameUtils.wildcardMatchOnSystem(null, null)).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo", "Foo")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("", "")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo", "Fo*")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo", "Fo?")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo Bar and Catflap", "Fo*")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("New Bookmarks", "N?w ?o?k??r?s")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo", "Bar")).isFalse();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo Bar Foo", "F*o Bar*")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Adobe Acrobat Installer", "Ad*er")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo", "*Foo")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("BarFoo", "*Foo")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("Foo", "Foo*")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("FooBar", "Foo*")).isTrue();
        assertThat(FilenameUtils.wildcardMatchOnSystem("FOO", "*Foo")).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.wildcardMatchOnSystem("BARFOO", "*Foo")).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.wildcardMatchOnSystem("FOO", "Foo*")).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.wildcardMatchOnSystem("FOOBAR", "Foo*")).isEqualTo(WINDOWS);
    }

    @Test
    public void testMatchCaseSpecified() {
        assertThat(FilenameUtils.wildcardMatch(null, "Foo", IOCase.SENSITIVE)).isFalse();
        assertThat(FilenameUtils.wildcardMatch("Foo", null, IOCase.SENSITIVE)).isFalse();
        assertThat(FilenameUtils.wildcardMatch(null, null, IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Foo", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("", "", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Fo*", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Fo?", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo Bar and Catflap", "Fo*", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("New Bookmarks", "N?w ?o?k??r?s", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Bar", IOCase.SENSITIVE)).isFalse();
        assertThat(FilenameUtils.wildcardMatch("Foo Bar Foo", "F*o Bar*", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Adobe Acrobat Installer", "Ad*er", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "*Foo", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Foo*", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "*Foo", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("BarFoo", "*Foo", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("Foo", "Foo*", IOCase.SENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("FooBar", "Foo*", IOCase.SENSITIVE)).isTrue();

        assertThat(FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.SENSITIVE)).isFalse();
        assertThat(FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.SENSITIVE)).isFalse();
        assertThat(FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.SENSITIVE)).isFalse();
        assertThat(FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.SENSITIVE)).isFalse();
        assertThat(FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.INSENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.INSENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.INSENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.INSENSITIVE)).isTrue();
        assertThat(FilenameUtils.wildcardMatch("FOO", "*Foo", IOCase.SYSTEM)).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.wildcardMatch("BARFOO", "*Foo", IOCase.SYSTEM)).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.wildcardMatch("FOO", "Foo*", IOCase.SYSTEM)).isEqualTo(WINDOWS);
        assertThat(FilenameUtils.wildcardMatch("FOOBAR", "Foo*", IOCase.SYSTEM)).isEqualTo(WINDOWS);
    }

    @Test
    public void testSplitOnTokens() {
        assertThat(FilenameUtils.splitOnTokens("Ad*er")).containsExactly(new String[]{"Ad", "*", "er"});
        assertThat(FilenameUtils.splitOnTokens("Ad?er")).containsExactly(new String[]{"Ad", "?", "er"});
        assertThat(FilenameUtils.splitOnTokens("Test*?One")).containsExactly(new String[]{"Test", "*", "?", "One"});
        assertThat(FilenameUtils.splitOnTokens("Test?*One")).containsExactly(new String[]{"Test", "?", "*", "One"});
        assertThat(FilenameUtils.splitOnTokens("****")).containsExactly(new String[]{"*"});
        assertThat(FilenameUtils.splitOnTokens("*??*")).containsExactly(new String[]{"*", "?", "?", "*"});
        assertThat(FilenameUtils.splitOnTokens("*?**?*")).containsExactly(new String[]{"*", "?", "*", "?", "*"});
        assertThat(FilenameUtils.splitOnTokens("*?***?*")).containsExactly(new String[]{"*", "?", "*", "?", "*"});
        assertThat(FilenameUtils.splitOnTokens("h??*")).containsExactly(new String[]{"h", "?", "?", "*"});
        assertThat(FilenameUtils.splitOnTokens("")).containsExactly(new String[]{""});
    }

    private void assertMatch(final String text, final String wildcard, final boolean expected) {
        assertThat(FilenameUtils.wildcardMatch(text, wildcard)).as(text + " " + wildcard).isEqualTo(expected);
    }

    // A separate set of tests, added to this batch
    @Test
    public void testMatch2() {
        assertMatch("log.txt", "log.txt", true);
        assertMatch("log.txt1", "log.txt", false);

        assertMatch("log.txt", "log.txt*", true);
        assertMatch("log.txt", "log.txt*1", false);
        assertMatch("log.txt", "*log.txt*", true);

        assertMatch("log.txt", "*.txt", true);
        assertMatch("txt.log", "*.txt", false);
        assertMatch("config.ini", "*.ini", true);

        assertMatch("config.txt.bak", "con*.txt", false);

        assertMatch("log.txt9", "*.txt?", true);
        assertMatch("log.txt", "*.txt?", false);

        assertMatch("progtestcase.java~5~", "*test*.java~*~", true);
        assertMatch("progtestcase.java;5~", "*test*.java~*~", false);
        assertMatch("progtestcase.java~5", "*test*.java~*~", false);

        assertMatch("log.txt", "log.*", true);

        assertMatch("log.txt", "log?*", true);

        assertMatch("log.txt12", "log.txt??", true);

        assertMatch("log.log", "log**log", true);
        assertMatch("log.log", "log**", true);
        assertMatch("log.log", "log.**", true);
        assertMatch("log.log", "**.log", true);
        assertMatch("log.log", "**log", true);

        assertMatch("log.log", "log*log", true);
        assertMatch("log.log", "log*", true);
        assertMatch("log.log", "log.*", true);
        assertMatch("log.log", "*.log", true);
        assertMatch("log.log", "*log", true);

        assertMatch("log.log", "*log?", false);
        assertMatch("log.log", "*log?*", true);
        assertMatch("log.log.abc", "*log?abc", true);
        assertMatch("log.log.abc.log.abc", "*log?abc", true);
        assertMatch("log.log.abc.log.abc.d", "*log?abc?d", true);
    }

    /**
     * See https://issues.apache.org/jira/browse/IO-246
     */
    @Test
    public void test_IO_246() {

        // Tests for "*?"
        assertMatch("aaa", "aa*?", true);
        // these ought to work as well, but "*?" does not work properly at present
//      assertMatch("aaa", "a*?", true);
//      assertMatch("aaa", "*?", true);

        // Tests for "?*"
        assertMatch("",    "?*",   false);
        assertMatch("a",   "a?*",  false);
        assertMatch("aa",  "aa?*", false);
        assertMatch("a",   "?*",   true);
        assertMatch("aa",  "?*",   true);
        assertMatch("aaa", "?*",   true);

        // Test ending on "?"
        assertMatch("",    "?", false);
        assertMatch("a",   "a?", false);
        assertMatch("aa",  "aa?", false);
        assertMatch("aab", "aa?", true);
        assertMatch("aaa", "*a", true);
    }

    @Test
    public void testLocaleIndependence() {
        final Locale orig = Locale.getDefault();

        final Locale[] locales = Locale.getAvailableLocales();

        final String[][] data = {
            { "I", "i"},
            { "i", "I"},
            { "i", "\u0130"},
            { "i", "\u0131"},
            { "\u03A3", "\u03C2"},
            { "\u03A3", "\u03C3"},
            { "\u03C2", "\u03C3"}
        };

        try {
            for (int i = 0; i < data.length; i++) {
                for (final Locale locale : locales) {
                    Locale.setDefault(locale);
                    assertThat(data[i][0].equalsIgnoreCase(data[i][1])).as("Test data corrupt: " + i).isTrue();
                    final boolean match = FilenameUtils.wildcardMatch(data[i][0], data[i][1], IOCase.INSENSITIVE);
                    assertThat(match).as(Locale.getDefault().toString() + ": " + i).isTrue();
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
    }

}
