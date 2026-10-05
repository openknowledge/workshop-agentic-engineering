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

import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.List;

public class HaveTypeCondition extends ArchCondition<JavaParameter> {

    private final ArchCondition<JavaType> condition;

    public HaveTypeCondition(ArchCondition<JavaType> condition) {
        super("have a type that " + condition.getDescription());
        this.condition = condition;
    }

    public static ArchCondition<JavaParameter> haveType(ArchCondition<JavaType> condition) {
        return new HaveTypeCondition(condition);
    }

    @Override
    public void init(Collection<JavaParameter> parameters) {
        condition.init(List.of());
    }

    @Override
    public void check(JavaParameter parameter, ConditionEvents events) {
        ConditionEvents typeEvents = ConditionEvents.Factory.create();
        condition.check(parameter.getType(), typeEvents);
        for (ConditionEvent violation : typeEvents.getViolating()) {
            events.add(
                    SimpleConditionEvent.violated(
                            parameter, String.join(", ", violation.getDescriptionLines())));
        }
    }
}
