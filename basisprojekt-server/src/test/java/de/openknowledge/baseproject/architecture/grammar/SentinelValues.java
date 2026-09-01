/*
 * Copyright (C) open knowledge GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.openknowledge.baseproject.architecture.grammar;

import static de.openknowledge.baseproject.architecture.grammar.RecordRules.canonicalConstructor;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class SentinelValues {

    private static final int MAX_SENTINEL_DEPTH = 20;

    private SentinelValues() {}

    public static Object[] forComponentsOf(Class<?> recordClass) {
        return sentinelArguments(recordClass, 0, 0);
    }

    public static Object[] forTypes(Class<?>... types) {
        Object[] arguments = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            arguments[i] = sentinelValue(types[i], i, 0);
        }
        return arguments;
    }

    private static Object sentinelValue(Class<?> type, int seed, int depth) {
        if (type == String.class) {
            return "sentinel-" + seed;
        }
        if (type == boolean.class || type == Boolean.class) {
            return seed % 2 == 0;
        }
        if (type == long.class || type == Long.class) {
            return (long) seed;
        }
        if (type == int.class || type == Integer.class) {
            return seed;
        }
        if (type == short.class || type == Short.class) {
            return (short) seed;
        }
        if (type == byte.class || type == Byte.class) {
            return (byte) seed;
        }
        if (type == double.class || type == Double.class) {
            return (double) seed;
        }
        if (type == float.class || type == Float.class) {
            return (float) seed;
        }
        if (type == char.class || type == Character.class) {
            return (char) ('a' + (seed % 26));
        }
        if (type == LocalDate.class) {
            return LocalDate.of(2000, 1, 1).plusDays(seed);
        }
        if (type == URI.class) {
            return URI.create("urn:sentinel:" + seed);
        }
        if (type == UUID.class) {
            return new UUID(0, seed);
        }
        if (type == BigDecimal.class) {
            return BigDecimal.valueOf(seed);
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length == 0 ? null : constants[seed % constants.length];
        }
        if (Set.class.isAssignableFrom(type)) {
            return new LinkedHashSet<>(Set.of("sentinel-element-" + seed));
        }
        if (Collection.class.isAssignableFrom(type)) {
            return new ArrayList<>(List.of("sentinel-element-" + seed));
        }
        if (Optional.class.isAssignableFrom(type)) {
            return Optional.of("sentinel-optional-" + seed);
        }
        if (type.isRecord()) {
            return sentinelRecord(type, seed, depth);
        }
        throw new SentinelGenerationException(
                "no sentinel value generator for type " + type.getName());
    }

    private static Object sentinelRecord(Class<?> type, int seed, int depth) {
        if (depth > MAX_SENTINEL_DEPTH) {
            throw new SentinelGenerationException(
                    "sentinel construction exceeded max depth ("
                            + MAX_SENTINEL_DEPTH
                            + ") for type "
                            + type.getName()
                            + "; possible record cycle");
        }
        try {
            return canonicalConstructor(type).newInstance(sentinelArguments(type, seed, depth));
        } catch (ReflectiveOperationException e) {
            throw new SentinelGenerationException(
                    "failed to construct sentinel instance of " + type.getName(), e);
        }
    }

    private static Object[] sentinelArguments(Class<?> recordClass, int seed, int depth) {
        RecordComponent[] components = recordClass.getRecordComponents();
        Object[] arguments = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            arguments[i] = sentinelValue(components[i].getType(), (seed * 31) + i, depth + 1);
        }
        return arguments;
    }

    public static final class SentinelGenerationException extends RuntimeException {

        SentinelGenerationException(String message) {
            super(message);
        }

        SentinelGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
