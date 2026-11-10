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
package de.openknowledge.customermanagement.todo.integration;

import static de.openknowledge.customermanagement.infrastructure.validation.Validate.validate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TodoUser(@Valid ExternalUserId id, @Valid DisplayName name, @Valid UserEmail email) {

    public static TodoUser newTodoUser(ExternalUserId id, DisplayName name, UserEmail email) {
        return validate(new TodoUser(id, name, email));
    }
}
