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

import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.List;

public class EveryCondition<T> extends ArchCondition<List<T>> {

    private final ArchCondition<T> condition;

    public EveryCondition(ArchCondition<T> condition) {
        super("all " + condition.getDescription());
        this.condition = condition;
    }

    public static <T> ArchCondition<List<T>> every(ArchCondition<T> condition) {
        return new EveryCondition<>(condition);
    }

    @Override
    public void init(Collection<List<T>> lists) {
        condition.init(List.of());
    }

    @Override
    public void check(List<T> items, ConditionEvents events) {
        for (T item : items) {
            ConditionEvents itemEvents = ConditionEvents.Factory.create();
            condition.check(item, itemEvents);
            for (ConditionEvent violation : itemEvents.getViolating()) {
                events.add(
                        SimpleConditionEvent.violated(
                                items, String.join(", ", violation.getDescriptionLines())));
            }
        }
    }
}
