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
import java.util.List;

public class HaveSizePredicate<T> extends DescribedPredicate<List<T>> {

    private final int size;

    public HaveSizePredicate(int size) {
        super("have size " + size);
        this.size = size;
    }

    public static <T> DescribedPredicate<List<T>> haveSize(int size) {
        return new HaveSizePredicate<T>(size);
    }

    @Override
    public boolean test(List<T> parameters) {
        return parameters.size() == size;
    }
}
