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
package de.openknowledge.baseproject.architecture.predicates;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import java.util.Arrays;
import java.util.List;

public class CanonicalConstructorPredicate extends DescribedPredicate<JavaConstructor> {

    public CanonicalConstructorPredicate() {
        super("canonical constructors");
    }

    public static DescribedPredicate<JavaConstructor> areCanonical() {
        return new CanonicalConstructorPredicate();
    }

    @Override
    public boolean test(JavaConstructor constructor) {
        JavaClass owner = constructor.getOwner();
        if (!owner.isRecord()) {
            return false;
        }
        List<String> componentTypeNames =
                Arrays.stream(owner.reflect().getRecordComponents())
                        .map(component -> component.getType().getName())
                        .toList();
        List<String> parameterTypeNames =
                constructor.getRawParameterTypes().stream().map(JavaClass::getName).toList();
        return componentTypeNames.equals(parameterTypeNames);
    }
}
