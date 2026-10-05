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
package de.openknowledge.customermanagement.architecture.grammar;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.lang.ClassesTransformer;
import java.util.ArrayList;
import java.util.List;

public class ParametersTransformer extends AbstractClassesTransformer<JavaParameter> {

    public ParametersTransformer() {
        super("parameters");
    }

    public static ClassesTransformer<JavaParameter> parameters() {
        return new ParametersTransformer();
    }

    @Override
    public Iterable<JavaParameter> doTransform(JavaClasses classes) {
        List<JavaParameter> parameters = new ArrayList<>();
        for (JavaClass javaClass : classes) {
            for (JavaCodeUnit codeUnit : javaClass.getCodeUnits()) {
                parameters.addAll(codeUnit.getParameters());
            }
        }
        return parameters;
    }
}
