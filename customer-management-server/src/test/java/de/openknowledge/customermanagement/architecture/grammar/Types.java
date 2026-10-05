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

import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaParameter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class Types {

    private Types() {}

    public static Class<?> elementType(Class<?> rawType, Type genericType) {
        if (rawType.isArray()) {
            return rawType.getComponentType();
        }
        if (genericType instanceof ParameterizedType parameterized
                && (Collection.class.isAssignableFrom(rawType)
                        || Optional.class.isAssignableFrom(rawType))) {
            Class<?> argumentClass = rawClassOf(parameterized.getActualTypeArguments()[0]);
            if (argumentClass != null) {
                return argumentClass;
            }
        }
        return rawType;
    }

    public static Class<?> rawClassOf(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterized
                && parameterized.getRawType() instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof WildcardType wildcard && wildcard.getUpperBounds().length > 0) {
            return rawClassOf(wildcard.getUpperBounds()[0]);
        }
        return null;
    }

    public static List<Annotation> elementTypeAnnotations(AnnotatedType annotatedType) {
        if (annotatedType instanceof AnnotatedParameterizedType parameterized) {
            List<Annotation> annotations = new ArrayList<>();
            for (AnnotatedType argument : parameterized.getAnnotatedActualTypeArguments()) {
                annotations.addAll(List.of(argument.getAnnotations()));
            }
            return annotations;
        }
        return List.of();
    }

    public static boolean anyMatches(
            List<Annotation> annotations, Predicate<Class<? extends Annotation>> predicate) {
        for (Annotation annotation : annotations) {
            if (predicate.test(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    public static JavaCodeUnit codeUnitOf(JavaParameter parameter) {
        return parameter.getOwner();
    }

    public static Parameter reflect(JavaParameter parameter) {
        return ((Executable) codeUnitOf(parameter).reflect()).getParameters()[parameter.getIndex()];
    }

    public static Class<?> elementTypeOf(JavaParameter parameter) {
        return elementTypeOf(reflect(parameter));
    }

    public static Class<?> elementTypeOf(Parameter parameter) {
        return elementType(parameter.getType(), parameter.getParameterizedType());
    }
}
