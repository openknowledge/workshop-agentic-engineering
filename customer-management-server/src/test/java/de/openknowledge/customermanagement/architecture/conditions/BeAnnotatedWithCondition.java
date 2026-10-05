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
package de.openknowledge.customermanagement.architecture.conditions;

import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import de.openknowledge.customermanagement.architecture.grammar.Types;
import java.lang.annotation.Annotation;

public class BeAnnotatedWithCondition extends ArchCondition<JavaParameter> {

    private final Class<? extends Annotation> annotationType;

    public BeAnnotatedWithCondition(Class<? extends Annotation> annotationType) {
        super("be annotated with @" + annotationType.getSimpleName());
        this.annotationType = annotationType;
    }

    public static ArchCondition<JavaParameter> beAnnotatedWith(
            Class<? extends Annotation> annotationType) {
        return new BeAnnotatedWithCondition(annotationType);
    }

    @Override
    public void check(JavaParameter parameter, ConditionEvents events) {
        if (!parameter.isAnnotatedWith(annotationType)
                && !Types.anyMatches(
                        Types.elementTypeAnnotations(Types.reflect(parameter).getAnnotatedType()),
                        annotationType::equals)) {
            JavaCodeUnit codeUnit = Types.codeUnitOf(parameter);
            events.add(
                    SimpleConditionEvent.violated(
                            parameter,
                            codeUnit.getOwner().getSimpleName()
                                    + "#"
                                    + codeUnit.getName()
                                    + " parameter '"
                                    + Types.reflect(parameter).getName()
                                    + "' of type "
                                    + Types.elementTypeOf(parameter).getSimpleName()
                                    + " is missing @"
                                    + annotationType.getSimpleName()));
        }
    }
}
