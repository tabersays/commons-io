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

package org.apache.commons.io.function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class IOFunctionTest {

    private static class Holder<T> {
        T value;
    }

    @Test
    public void testAndThenConsumer() throws IOException {
        final Holder<Integer> holder = new Holder<>();
        final IOFunction<InputStream, Integer> readByte = InputStream::read;
        final Consumer<Integer> sinkInteger = i -> {
            holder.value = i * i;
        };
        final IOConsumer<InputStream> productFunction = readByte.andThen(sinkInteger);

        final InputStream is = new ByteArrayInputStream(new byte[] {2, 3});
        productFunction.accept(is);
        assertThat(holder.value).isEqualTo(4);
        productFunction.accept(is);
        assertThat(holder.value).isEqualTo(9);
    }

    @Test
    public void testAndThenFunction() throws IOException {
        final IOFunction<InputStream, Integer> readByte = InputStream::read;
        final Function<Integer, Integer> squareInteger = i -> i * i;
        final IOFunction<InputStream, Integer> productFunction = readByte.andThen(squareInteger);

        final InputStream is = new ByteArrayInputStream(new byte[] {2, 3});
        assertThat(productFunction.apply(is)).isEqualTo(4);
        assertThat(productFunction.apply(is)).isEqualTo(9);
    }

    @Test
    public void testAndThenIOConsumer() throws IOException {
        final Holder<Integer> holder = new Holder<>();
        final IOFunction<InputStream, Integer> readByte = InputStream::read;
        final IOConsumer<Integer> sinkInteger = i -> {
            holder.value = i * i;
        };
        final IOConsumer<InputStream> productFunction = readByte.andThen(sinkInteger);

        final InputStream is = new ByteArrayInputStream(new byte[] {2, 3});
        productFunction.accept(is);
        assertThat(holder.value).isEqualTo(4);
        productFunction.accept(is);
        assertThat(holder.value).isEqualTo(9);
    }

    @Test
    public void testAndThenIOFunction() throws IOException {
        final IOFunction<InputStream, Integer> readByte = InputStream::read;
        final IOFunction<Integer, Integer> squareInteger = i -> i * i;
        final IOFunction<InputStream, Integer> productFunction = readByte.andThen(squareInteger);

        final InputStream is = new ByteArrayInputStream(new byte[] {2, 3});
        assertThat(productFunction.apply(is)).isEqualTo(4);
        assertThat(productFunction.apply(is)).isEqualTo(9);
    }

    @Test
    public void testApply() throws IOException {
        final IOFunction<InputStream, Integer> readByte = InputStream::read;
        final InputStream is = new ByteArrayInputStream(new byte[] { (byte)0xa, (byte)0xb, (byte)0xc});
        assertThat(readByte.apply(is)).isEqualTo(0xa);
        assertThat(readByte.apply(is)).isEqualTo(0xb);
        assertThat(readByte.apply(is)).isEqualTo(0xc);
        assertThat(readByte.apply(is)).isEqualTo(-1);
    }

    @Test
    public void testApplyRaisesException() {
        final IOFunction<InputStream, Integer> raiseException = is -> {
            throw new IOException("Boom!");
        };
        final InputStream is = new ByteArrayInputStream(new byte[] { (byte)0xa, (byte)0xb, (byte)0xc});

        assertThrows(IOException.class, () -> {
            raiseException.apply(is);
        });
    }

    @Test
    public void testComposeFunction() throws IOException {
        final Function<InputStream, Integer> alwaysSeven = is -> 7;
        final IOFunction<Integer, Integer> squareInteger = i -> i * i;
        final IOFunction<InputStream, Integer> productFunction = squareInteger.compose(alwaysSeven);

        final InputStream is = new ByteArrayInputStream(new byte[] {2, 3});
        assertThat(productFunction.apply(is)).isEqualTo(49);
        assertThat(productFunction.apply(is)).isEqualTo(49);
    }

    @Test
    public void testComposeIOFunction() throws IOException {
        final IOFunction<InputStream, Integer> readByte = InputStream::read;
        final IOFunction<Integer, Integer> squareInteger = i -> i * i;
        final IOFunction<InputStream, Integer> productFunction = squareInteger.compose(readByte);

        final InputStream is = new ByteArrayInputStream(new byte[] {2, 3});
        assertThat(productFunction.apply(is)).isEqualTo(4);
        assertThat(productFunction.apply(is)).isEqualTo(9);
    }

    @Test
    public void testComposeIOSupplier() throws IOException {
        final InputStream is = new ByteArrayInputStream(new byte[] {2, 3});

        final IOSupplier<Integer> readByte = () -> is.read();
        final IOFunction<Integer, Integer> squareInteger = i -> i * i;
        final IOSupplier<Integer> productFunction = squareInteger.compose(readByte);

        assertThat(productFunction.get()).isEqualTo(4);
        assertThat(productFunction.get()).isEqualTo(9);
    }

    @Test
    public void testComposeSupplier() throws IOException {
        final Supplier<Integer> alwaysNine = () -> 9;
        final IOFunction<Integer, Integer> squareInteger = i -> i * i;
        final IOSupplier<Integer> productFunction = squareInteger.compose(alwaysNine);

        assertThat(productFunction.get()).isEqualTo(81);
        assertThat(productFunction.get()).isEqualTo(81);
    }

    @Test
    public void testIdentity() throws IOException {
        final IOFunction<InputStream, InputStream> identityFunction = IOFunction.identity();
        try (final InputStream is = new ByteArrayInputStream(new byte[] { (byte) 0xa, (byte) 0xb, (byte) 0xc })) {
            assertThat(identityFunction.apply(is)).isEqualTo(is);
        }
    }
}
