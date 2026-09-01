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
package de.openknowledge.baseproject.infrastructure.http;

import static de.openknowledge.baseproject.infrastructure.validation.Validate.validate;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Creates the JSON name of an invalid parameter from a constraint violation. Records with a single
 * {@link JsonValue} component are serialized flat, so their component node is omitted. Method nodes
 * are omitted as well.
 */
public record InvalidParameterName(@NotNull String name) {

    public static InvalidParameterName newInvalidParameterName(String name) {
        return validate(new InvalidParameterName(name));
    }

    static InvalidParameterName of(ConstraintViolation<?> violation) {
        List<Node> nodes = new ArrayList<>();
        violation.getPropertyPath().forEach(nodes::add);
        boolean dropLast =
                !nodes.isEmpty()
                        && isJsonValueComponent(
                                violation.getLeafBean(), nodes.get(nodes.size() - 1));
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (isExecutable(node)) {
                continue;
            }
            if (node.isInIterable() && i > 0) {
                name.append('[').append(indexOf(node)).append(']');
            }
            if (dropLast && i == nodes.size() - 1) {
                break;
            }
            appendName(name, node.getName());
        }
        return newInvalidParameterName(name.toString());
    }

    // method validation prefixes the path with the method name, which is not part of the request
    private static boolean isExecutable(Node node) {
        return node.getKind() == ElementKind.METHOD || node.getKind() == ElementKind.CONSTRUCTOR;
    }

    private static Object indexOf(Node node) {
        return node.getIndex() != null ? node.getIndex() : node.getKey();
    }

    private static void appendName(StringBuilder name, String nodeName) {
        if (nodeName == null || nodeName.isEmpty() || nodeName.startsWith("<")) {
            return;
        }
        if (!name.isEmpty()) {
            name.append('.');
        }
        name.append(nodeName);
    }

    private static boolean isJsonValueComponent(Object leafBean, Path.Node node) {
        if (node.getKind() != ElementKind.PROPERTY || !(leafBean instanceof Record)) {
            return false;
        }
        return Stream.of(leafBean.getClass().getRecordComponents())
                .filter(component -> component.getName().equals(node.getName()))
                .anyMatch(InvalidParameterName::isJsonValue);
    }

    // @JsonValue is not applicable to record components, so it is propagated to accessor and field
    private static boolean isJsonValue(RecordComponent component) {
        if (component.getAccessor().isAnnotationPresent(JsonValue.class)) {
            return true;
        }
        try {
            return component
                    .getDeclaringRecord()
                    .getDeclaredField(component.getName())
                    .isAnnotationPresent(JsonValue.class);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
