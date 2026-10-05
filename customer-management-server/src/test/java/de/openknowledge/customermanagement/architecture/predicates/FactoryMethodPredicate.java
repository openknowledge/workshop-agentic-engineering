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
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;

public class FactoryMethodPredicate extends DescribedPredicate<JavaCodeUnit> {

    public FactoryMethodPredicate() {
        super("static factory methods new<RecordName>");
    }

    public static DescribedPredicate<JavaCodeUnit> areFactoryMethods() {
        return new FactoryMethodPredicate();
    }

    @Override
    public boolean test(JavaCodeUnit codeUnit) {
        if (!(codeUnit instanceof JavaMethod method)) {
            return false;
        }
        JavaClass owner = method.getOwner();
        return owner.isRecord()
                && method.getModifiers().contains(JavaModifier.STATIC)
                && method.getName().equals("new" + owner.getSimpleName())
                && method.getRawReturnType().equals(owner);
    }
}
