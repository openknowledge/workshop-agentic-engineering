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
package de.openknowledge.customermanagement.customer.domain;

import static de.openknowledge.customermanagement.infrastructure.validation.Validate.validate;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Embeddable
public record CustomerId(@JsonValue @NotNull @Min(1) Long customerId) {

    public CustomerId(String customerId) {
        this(ofNullable(customerId).map(Long::valueOf).orElse(null));
    }

    public static CustomerId newCustomerId(Long customerId) {
        return validate(new CustomerId(customerId));
    }
}
