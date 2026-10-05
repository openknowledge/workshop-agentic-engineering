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
import com.tngtech.archunit.core.domain.JavaParameter;
import de.openknowledge.customermanagement.architecture.grammar.Types;

public class RecordTypeParameterPredicate extends DescribedPredicate<JavaParameter> {

    public RecordTypeParameterPredicate() {
        super("record types");
    }

    public static DescribedPredicate<JavaParameter> recordTypes() {
        return new RecordTypeParameterPredicate();
    }

    @Override
    public boolean test(JavaParameter parameter) {
        return Types.elementTypeOf(parameter).isRecord();
    }
}
