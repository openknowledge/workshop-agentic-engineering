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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import de.openknowledge.customermanagement.architecture.grammar.SentinelValues;
import de.openknowledge.customermanagement.infrastructure.validation.Validate;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import org.mockito.MockedStatic;
import org.mockito.exceptions.base.MockitoException;

public class ReturnValidatedObjectRule extends ArchCondition<JavaMethod> {

    public ReturnValidatedObjectRule() {
        super(
                "pass the record created by its canonical constructor from the method parameters"
                        + " to Validate.validate(<record>) and return that record");
    }

    public static ArchCondition<JavaMethod> returnValidatedObjectConstructedFromParameters() {
        return new ReturnValidatedObjectRule();
    }

    @Override
    public void check(JavaMethod javaMethod, ConditionEvents events) {
        String violation = checkFactoryMethod(javaMethod.reflect());
        if (violation != null) {
            events.add(SimpleConditionEvent.violated(javaMethod, violation));
        }
    }

    private static String checkFactoryMethod(Method method) {
        Class<?> recordClass = method.getDeclaringClass();
        String name = recordClass.getSimpleName() + "#" + method.getName() + ": ";
        List<Object> validated = new ArrayList<>();
        try (MockedStatic<Validate> mockedValidate = mockStatic(Validate.class)) {
            mockedValidate
                    .when(() -> Validate.validate((Object) any()))
                    .thenAnswer(
                            invocation -> {
                                validated.add(invocation.getArgument(0));
                                return invocation.getArgument(0);
                            });
            Object[] arguments = SentinelValues.forTypes(method.getParameterTypes());
            Object result = method.invoke(null, arguments);
            return verify(recordClass, validated, arguments, method, result, name);
        } catch (ReflectiveOperationException
                | IllegalArgumentException
                | SentinelValues.SentinelGenerationException e) {
            return name
                    + "could not be evaluated (unable to invoke with sentinel arguments) - "
                    + rootCauseMessage(e);
        } catch (MockitoException e) {
            return name + "mocking Validate failed - " + rootCauseMessage(e);
        }
    }

    private static String verify(
            Class<?> recordClass,
            List<Object> validated,
            Object[] arguments,
            Method method,
            Object result,
            String name)
            throws ReflectiveOperationException {
        List<Object> candidates = validated.stream().filter(recordClass::isInstance).toList();
        if (candidates.isEmpty()) {
            return name + "does not call Validate.validate with a " + recordClass.getSimpleName();
        }
        if (candidates.size() > 1) {
            return name
                    + "calls Validate.validate more than once with a "
                    + recordClass.getSimpleName();
        }
        Object candidate = candidates.get(0);
        String mismatch = componentMismatch(recordClass, candidate, arguments, method);
        if (mismatch != null) {
            return name
                    + "validated "
                    + recordClass.getSimpleName()
                    + " is not constructed from the parameters - "
                    + mismatch;
        }
        if (result != candidate) {
            return name + "does not return the object passed to Validate.validate";
        }
        return null;
    }

    private static String componentMismatch(
            Class<?> recordClass, Object candidate, Object[] arguments, Method method)
            throws ReflectiveOperationException {
        RecordComponent[] components = recordClass.getRecordComponents();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (components.length != parameterTypes.length) {
            return "parameter count differs from component count";
        }
        for (int i = 0; i < components.length; i++) {
            // parameters converted to another type (e.g. URI -> UriReference) cannot be compared
            // read the field, since accessors may be overridden (e.g. to wrap collections)
            if (components[i].getType() == parameterTypes[i]
                    && componentValue(recordClass, components[i], candidate) != arguments[i]) {
                return "component '" + components[i].getName() + "' differs from parameter " + i;
            }
        }
        return null;
    }

    private static Object componentValue(
            Class<?> recordClass, RecordComponent component, Object record)
            throws ReflectiveOperationException {
        Field field = recordClass.getDeclaredField(component.getName());
        field.setAccessible(true);
        return field.get(record);
    }

    private static String rootCauseMessage(Throwable throwable) {
        Throwable detail = throwable.getCause() != null ? throwable.getCause() : throwable;
        return detail.getMessage();
    }
}
