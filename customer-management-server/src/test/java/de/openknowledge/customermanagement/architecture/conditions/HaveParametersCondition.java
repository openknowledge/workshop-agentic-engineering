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

import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.List;

public class HaveParametersCondition extends ArchCondition<JavaConstructor> {

    private final ArchCondition<List<JavaParameter>> condition;

    public HaveParametersCondition(ArchCondition<List<JavaParameter>> condition) {
        super("have parameters that " + condition.getDescription());
        this.condition = condition;
    }

    public static ArchCondition<JavaConstructor> haveParameters(
            ArchCondition<List<JavaParameter>> condition) {
        return new HaveParametersCondition(condition);
    }

    @Override
    public void init(Collection<JavaConstructor> constructors) {
        condition.init(List.of());
    }

    @Override
    public void check(JavaConstructor constructor, ConditionEvents events) {
        ConditionEvents parametersEvents = ConditionEvents.Factory.create();
        condition.check(constructor.getParameters(), parametersEvents);
        for (ConditionEvent violation : parametersEvents.getViolating()) {
            events.add(
                    SimpleConditionEvent.violated(
                            constructor,
                            constructor.getOwner().getSimpleName()
                                    + ": "
                                    + String.join(", ", violation.getDescriptionLines())));
        }
    }
}
