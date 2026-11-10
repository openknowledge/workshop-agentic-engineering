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
package de.openknowledge.customermanagement.infrastructure.openapi;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

/**
 * Describes parameters of a record value object type (a record with a {@code @JsonValue} component)
 * with the schema of that component, including its constraints. Without this, springdoc describes
 * such parameters as plain strings as soon as a {@code Converter<String, ...>} exists for them.
 */
@Component
public class JsonValueParameterCustomizer implements ParameterCustomizer {

    @Autowired private SpringDocConfigProperties properties;

    @Override
    public Parameter customize(Parameter parameterModel, MethodParameter methodParameter) {
        Class<?> type = methodParameter.getParameterType();
        if (parameterModel != null && isJsonValueRecord(type)) {
            ResolvedSchema resolved =
                    ModelConverters.getInstance(properties.isOpenapi31())
                            .resolveAsResolvedSchema(new AnnotatedType(type).resolveAsRef(false));
            if (resolved != null && resolved.schema != null) {
                parameterModel.setSchema(resolved.schema);
            }
        }
        return parameterModel;
    }

    private boolean isJsonValueRecord(Class<?> type) {
        return type.isRecord()
                && Arrays.stream(type.getRecordComponents()).anyMatch(this::isJsonValue);
    }

    private boolean isJsonValue(RecordComponent component) {
        return component.isAnnotationPresent(JsonValue.class)
                || component.getAccessor().isAnnotationPresent(JsonValue.class);
    }
}
