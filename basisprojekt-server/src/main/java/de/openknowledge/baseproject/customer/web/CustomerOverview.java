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
package de.openknowledge.baseproject.customer.web;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.openknowledge.baseproject.customer.domain.Customer;
<<<<<<< HEAD
import java.net.URI;

public record CustomerOverview(UriReference self, @JsonUnwrapped Customer customer) {

    public static CustomerOverview of(URI self, Customer customer) {
        return new CustomerOverview(new UriReference(self), customer);
=======
import de.openknowledge.baseproject.infrastructure.validation.Validate;
import jakarta.validation.Valid;
import java.net.URI;

public record CustomerOverview(@Valid UriReference self, @JsonUnwrapped @Valid Customer customer) {

    public static CustomerOverview newCustomerOverview(URI self, Customer customer) {
        return newCustomerOverview(new UriReference(self), customer);
    }

    public static CustomerOverview newCustomerOverview(UriReference self, Customer customer) {
        return Validate.validate(new CustomerOverview(self, customer));
>>>>>>> 8740e8a (Initial setup)
    }
}
