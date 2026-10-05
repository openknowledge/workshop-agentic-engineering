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

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.List;

public class HaveFieldsCondition extends ArchCondition<JavaClass> {

    private final DescribedPredicate<? super JavaField> fieldFilter;
    private final ArchCondition<List<Object>> condition;

    public HaveFieldsCondition(
            DescribedPredicate<? super JavaField> fieldFilter,
            ArchCondition<List<Object>> condition) {
        super(
                "have fields that "
                        + fieldFilter.getDescription()
                        + " and "
                        + condition.getDescription());
        this.fieldFilter = fieldFilter;
        this.condition = condition;
    }

    public static ArchCondition<JavaClass> haveFields(
            DescribedPredicate<CanBeAnnotated> fieldFilter, ArchCondition<List<Object>> condition) {
        return new HaveFieldsCondition(fieldFilter, condition);
    }

    @Override
    public void init(Collection<JavaClass> classes) {
        condition.init(List.of());
    }

    @Override
    public void check(JavaClass javaClass, ConditionEvents events) {
        List<Object> fields =
                javaClass.getFields().stream().filter(fieldFilter).map(Object.class::cast).toList();
        ConditionEvents fieldsEvents = ConditionEvents.Factory.create();
        condition.check(fields, fieldsEvents);
        for (ConditionEvent violation : fieldsEvents.getViolating()) {
            events.add(
                    SimpleConditionEvent.violated(
                            javaClass,
                            javaClass.getSimpleName()
                                    + ": "
                                    + String.join(", ", violation.getDescriptionLines())));
        }
    }
}
