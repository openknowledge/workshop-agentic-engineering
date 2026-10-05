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

import com.fasterxml.jackson.annotation.JsonValue;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HaveSchemaMatchingConstraintsCondition extends ArchCondition<JavaClass> {

    public HaveSchemaMatchingConstraintsCondition() {
        super("have a @Schema that matches the @Min, @Max and @Size of its @JsonValue component");
    }

    public static ArchCondition<JavaClass> schemaMatchingConstraints() {
        return new HaveSchemaMatchingConstraintsCondition();
    }

    @Override
    public void check(JavaClass javaClass, ConditionEvents events) {
        Class<?> type = javaClass.reflect();
        Schema schema = type.getAnnotation(Schema.class);
        RecordComponent component = findValueComponent(type);
        if (schema == null || component == null) {
            return;
        }
        List<String> violations = new ArrayList<>();
        Min min = findAnnotation(Min.class, component);
        Max max = findAnnotation(Max.class, component);
        Size size = findAnnotation(Size.class, component);
        checkNumber("minimum", schema.minimum(), min == null ? null : min.value(), violations);
        checkNumber("maximum", schema.maximum(), max == null ? null : max.value(), violations);
        checkInt("minLength", schema.minLength(), size == null ? 0 : size.min(), violations);
        checkInt(
                "maxLength",
                schema.maxLength(),
                size == null ? Integer.MAX_VALUE : size.max(),
                violations);
        for (String violation : violations) {
            events.add(
                    SimpleConditionEvent.violated(
                            javaClass, type.getSimpleName() + ": " + violation));
        }
    }

    private static void checkNumber(
            String name, String schemaValue, Long constraintValue, List<String> violations) {
        boolean schemaSet = !schemaValue.isEmpty();
        boolean matches =
                schemaSet == (constraintValue != null)
                        && (!schemaSet
                                || new BigDecimal(schemaValue)
                                                .compareTo(BigDecimal.valueOf(constraintValue))
                                        == 0);
        if (!matches) {
            violations.add(
                    String.format(
                            "@Schema(%s=\"%s\") does not match the bean validation constraint (%s)",
                            name, schemaValue, constraintValue));
        }
    }

    private static void checkInt(
            String name, int schemaValue, int constraintValue, List<String> violations) {
        if (schemaValue != constraintValue) {
            violations.add(
                    String.format(
                            "@Schema(%s=%d) does not match the bean validation constraint (%d)",
                            name, schemaValue, constraintValue));
        }
    }

    private static RecordComponent findValueComponent(Class<?> type) {
        if (!type.isRecord()) {
            return null;
        }
        RecordComponent[] components = type.getRecordComponents();
        for (RecordComponent component : components) {
            if (findAnnotation(JsonValue.class, component) != null) {
                return component;
            }
        }
        return components.length == 1 ? components[0] : null;
    }

    private static <A extends Annotation> A findAnnotation(
            Class<A> annotationType, RecordComponent component) {
        A annotation = component.getAccessor().getAnnotation(annotationType);
        if (annotation == null) {
            try {
                Field field = component.getDeclaringRecord().getDeclaredField(component.getName());
                annotation = field.getAnnotation(annotationType);
            } catch (NoSuchFieldException e) {
                // no backing field, nothing to look at
            }
        }
        return annotation;
    }
}
