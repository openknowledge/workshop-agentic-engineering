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
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public class SatisfyPredicateCondition<T> extends ArchCondition<T> {

    private final DescribedPredicate<? super T> predicate;

    public SatisfyPredicateCondition(DescribedPredicate<? super T> predicate) {
        super("satisfy " + predicate.getDescription());
        this.predicate = predicate;
    }

    public static <T> ArchCondition<T> satisfy(DescribedPredicate<? super T> predicate) {
        return new SatisfyPredicateCondition<>(predicate);
    }

    @Override
    public void check(T item, ConditionEvents events) {
        if (!predicate.test(item)) {
            events.add(
                    SimpleConditionEvent.violated(
                            item, item + " does not satisfy " + predicate.getDescription()));
        }
    }
}
