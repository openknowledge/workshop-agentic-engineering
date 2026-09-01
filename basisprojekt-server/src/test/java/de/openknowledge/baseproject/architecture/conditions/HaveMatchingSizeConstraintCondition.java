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

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

public class HaveMatchingSizeConstraintCondition extends ArchCondition<JavaClass> {

    public HaveMatchingSizeConstraintCondition() {
        super("have a matching @Size(max=length) for every @Column(length) on a String path");
    }

    public static ArchCondition<JavaClass> sizeConstraintMatchingColumnLength() {
        return new HaveMatchingSizeConstraintCondition();
    }

    @Override
    public void check(JavaClass javaClass, ConditionEvents events) {
        List<String> violations = new ArrayList<>();
        inspectEntity(javaClass.reflect(), violations);
        for (String violation : violations) {
            events.add(SimpleConditionEvent.violated(javaClass, violation));
        }
    }

    private static void inspectEntity(Class<?> entityClass, List<String> violations) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getAnnotation(Embedded.class) == null) {
                continue;
            }
            AttributeOverride[] overrides = field.getAnnotationsByType(AttributeOverride.class);
            for (AttributeOverride override : overrides) {
                checkOverride(entityClass, field, override, violations);
            }
        }
    }

    private static void checkOverride(
            Class<?> entityClass,
            Field field,
            AttributeOverride override,
            List<String> violations) {
        String[] parts = override.name().split("\\.");
        if (parts.length != 2) {
            return;
        }
        Class<?> intermediateType = resolveComponentType(field.getType(), parts[0]);
        if (intermediateType == null || !intermediateType.isRecord()) {
            return;
        }
        RecordComponent leaf = findRecordComponent(intermediateType, parts[1]);
        if (leaf == null || leaf.getType() != String.class) {
            return;
        }
        Lob lob = leaf.getAnnotation(Lob.class);
        if (lob == null) {
            lob = leaf.getAccessor().getAnnotation(Lob.class);
        }
        if (lob != null) {
            return;
        }
        Column column = override.column();
        int length = column.length();
        Size size = leaf.getAnnotation(Size.class);
        if (size == null) {
            size = leaf.getAccessor().getAnnotation(Size.class);
        }
        if (size == null || size.max() != length) {
            int foundMax = size != null ? size.max() : -1;
            violations.add(
                    String.format(
                            "%s#%s '%s': column length=%d but @Size(max=%d)",
                            entityClass.getSimpleName(),
                            field.getName(),
                            override.name(),
                            length,
                            foundMax));
        }
    }

    private static Class<?> resolveComponentType(Class<?> type, String componentName) {
        if (!type.isRecord()) {
            return null;
        }
        for (RecordComponent rc : type.getRecordComponents()) {
            if (rc.getName().equals(componentName)) {
                return rc.getType();
            }
        }
        return null;
    }

    private static RecordComponent findRecordComponent(Class<?> type, String name) {
        for (RecordComponent rc : type.getRecordComponents()) {
            if (rc.getName().equals(name)) {
                return rc;
            }
        }
        return null;
    }
}
