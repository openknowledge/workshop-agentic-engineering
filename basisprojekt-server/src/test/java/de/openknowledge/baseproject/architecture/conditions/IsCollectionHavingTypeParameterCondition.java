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
package de.openknowledge.baseproject.architecture.conditions;

import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.List;

public class IsCollectionHavingTypeParameterCondition extends ArchCondition<JavaType> {

    private final ArchCondition<JavaType> condition;

    public IsCollectionHavingTypeParameterCondition(ArchCondition<JavaType> condition) {
        super("are collections having a type parameter that " + condition.getDescription());
        this.condition = condition;
    }

    public static ArchCondition<JavaType> isCollectionHavingTypeParameter(
            ArchCondition<JavaType> condition) {
        return new IsCollectionHavingTypeParameterCondition(condition);
    }

    @Override
    public void init(Collection<JavaType> types) {
        condition.init(List.of());
    }

    @Override
    public void check(JavaType type, ConditionEvents events) {
        if (!type.toErasure().isAssignableTo(Collection.class)) {
            events.add(
                    SimpleConditionEvent.violated(type, type.getName() + " is not a collection"));
            return;
        }
        if (!(type instanceof JavaParameterizedType parameterized)) {
            events.add(
                    SimpleConditionEvent.violated(type, type.getName() + " has no type parameter"));
            return;
        }
        for (JavaType typeArgument : parameterized.getActualTypeArguments()) {
            ConditionEvents argumentEvents = ConditionEvents.Factory.create();
            condition.check(typeArgument, argumentEvents);
            for (ConditionEvent violation : argumentEvents.getViolating()) {
                events.add(
                        SimpleConditionEvent.violated(
                                type, String.join(", ", violation.getDescriptionLines())));
            }
        }
    }
}
