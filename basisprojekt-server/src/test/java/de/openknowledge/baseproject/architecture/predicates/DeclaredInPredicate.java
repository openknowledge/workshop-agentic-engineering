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
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaParameter;
import de.openknowledge.baseproject.architecture.grammar.Types;

public class DeclaredInPredicate extends DescribedPredicate<JavaParameter> {

    private final DescribedPredicate<? super JavaCodeUnit> codeUnits;

    public DeclaredInPredicate(DescribedPredicate<? super JavaCodeUnit> codeUnits) {
        super("are declared in code units that " + codeUnits.getDescription());
        this.codeUnits = codeUnits;
    }

    public static DescribedPredicate<JavaParameter> areDeclaredIn(
            DescribedPredicate<? super JavaCodeUnit> codeUnits) {
        return new DeclaredInPredicate(codeUnits);
    }

    @Override
    public boolean test(JavaParameter parameter) {
        return codeUnits.test(Types.codeUnitOf(parameter));
    }
}
