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
package de.openknowledge.customermanagement.architecture.predicates;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;

public class ConstructorPredicate extends DescribedPredicate<JavaCodeUnit> {

    private final DescribedPredicate<? super JavaConstructor> predicate;

    public ConstructorPredicate(DescribedPredicate<? super JavaConstructor> predicate) {
        super("constructors that " + predicate.getDescription());
        this.predicate = predicate;
    }

    public static DescribedPredicate<JavaCodeUnit> constructorsThat(
            DescribedPredicate<? super JavaConstructor> predicate) {
        return new ConstructorPredicate(predicate);
    }

    @Override
    public boolean test(JavaCodeUnit codeUnit) {
        return codeUnit instanceof JavaConstructor constructor && predicate.test(constructor);
    }
}
