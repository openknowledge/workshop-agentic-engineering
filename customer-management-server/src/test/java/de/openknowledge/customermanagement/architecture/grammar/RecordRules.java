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
package de.openknowledge.customermanagement.architecture.grammar;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

public class RecordRules {

    public static Constructor<?> canonicalConstructor(Class<?> recordClass) {
        Class<?>[] parameterTypes =
                Arrays.stream(recordClass.getRecordComponents())
                        .map(RecordComponent::getType)
                        .toArray(Class<?>[]::new);
        try {
            return recordClass.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Canonical constructor not found for " + recordClass, e);
        }
    }
}
