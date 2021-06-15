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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** This is more an example than a test - deserialize our {@link MoreComplexObject}
 *  to verify which settings it requires, as the object uses a number of primitive
 *  and java.* member objects.
 */
public class MoreComplexObjectTest extends AbstractCloseableListTest {

    private InputStream inputStream;
    private MoreComplexObject original;

    @BeforeEach
    public void setupMoreComplexObject() throws IOException {
        original = new MoreComplexObject();
        final ByteArrayOutputStream bos = closeAfterEachTest(new ByteArrayOutputStream());
        final ObjectOutputStream oos = closeAfterEachTest(new ObjectOutputStream(bos));
        oos.writeObject(original);
        inputStream = closeAfterEachTest(new ByteArrayInputStream(bos.toByteArray()));
    }

    private void assertSerialization(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
        final MoreComplexObject copy = (MoreComplexObject) (ois.readObject());
        assertThat(copy.toString()).as("Expecting same data after deserializing").isEqualTo(original.toString());
    }

    /** Trusting java.lang.* and the array variants of that means we have
     *  to define a number of accept classes explicitly. Quite safe but
     *  might become a bit verbose.
     */
    @Test
    public void trustJavaLang() throws IOException, ClassNotFoundException {
        assertSerialization(closeAfterEachTest(
                new ValidatingObjectInputStream(inputStream)
                .accept(MoreComplexObject.class, ArrayList.class, Random.class)
                .accept("java.lang.*","[Ljava.lang.*")
        ));
    }

    /** Trusting java.* is probably reasonable and avoids having to be too
     *  detailed in the accepts.
     */
    @Test
    public void trustJavaIncludingArrays() throws IOException, ClassNotFoundException {
        assertSerialization(closeAfterEachTest(
                new ValidatingObjectInputStream(inputStream)
                .accept(MoreComplexObject.class)
                .accept("java.*","[Ljava.*")
        ));
    }

    /** Here we accept everything but reject specific classes, using a pure
     *  blacklist mode.
     *
     *  That's not as safe as it's hard to get an exhaustive blacklist, but
     *  might be ok in controlled environments.
     */
    @Test
    public void useBlacklist() throws IOException, ClassNotFoundException {
        final String [] blacklist = {
                "org.apache.commons.collections.functors.InvokerTransformer",
                "org.codehaus.groovy.runtime.ConvertedClosure",
                "org.codehaus.groovy.runtime.MethodClosure",
                "org.springframework.beans.factory.ObjectFactory"
        };
        assertSerialization(closeAfterEachTest(
                new ValidatingObjectInputStream(inputStream)
                .accept("*")
                .reject(blacklist)
        ));
    }
}
