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

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import de.openknowledge.baseproject.customer.domain.Customer;
import de.openknowledge.baseproject.customer.domain.CustomerEntity;
<<<<<<< HEAD
=======
import de.openknowledge.baseproject.customer.domain.CustomerId;
>>>>>>> 8740e8a (Initial setup)
import de.openknowledge.baseproject.customer.domain.CustomerService;
import de.openknowledge.baseproject.todo.integration.TodoException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@RequestMapping(path = "/api/customers")
public class CustomerController {

    @Autowired private CustomerService customerService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public List<CustomerOverview> getCustomers() {
        return customerService.findAllCustomers().stream().map(toOverview()).toList();
    }

<<<<<<< HEAD
    @GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Customer getCustomer(@PathVariable("id") Long id) {
=======
    // tag::path-validation[]
    @GetMapping(path = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Customer getCustomer(@Valid @PathVariable("id") CustomerId id) {
>>>>>>> 8740e8a (Initial setup)
        return customerService
                .findCustomer(id)
                .map(CustomerEntity::getCustomer)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
    }

<<<<<<< HEAD
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createCustomer(@Valid @RequestBody Customer customer) {
        try {
            long customerId = customerService.registerCustomer(customer).getId();
=======
    // end::path-validation[]

    // tag::body-validation[]
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createCustomer(@Valid @RequestBody Customer customer) {
        try {
            CustomerId customerId = customerService.registerCustomer(customer).getId();
>>>>>>> 8740e8a (Initial setup)
            return ResponseEntity.created(createCustomerUri(customerId)).build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        } catch (TodoException e) {
            throw new ResponseStatusException(BAD_GATEWAY, "External user service unavailable");
        }
    }

<<<<<<< HEAD
    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public void updateCustomer(@PathVariable("id") Long id, @Valid @RequestBody Customer customer) {
=======
    // end::body-validation[]

    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public void updateCustomer(
            @Valid @PathVariable("id") CustomerId id, @Valid @RequestBody Customer customer) {
>>>>>>> 8740e8a (Initial setup)
        try {
            customerService.updateCustomer(id, customer);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Customer not found");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(NO_CONTENT)
<<<<<<< HEAD
    public void deleteCustomer(@PathVariable("id") Long id) {
=======
    public void deleteCustomer(@Valid @PathVariable("id") CustomerId id) {
>>>>>>> 8740e8a (Initial setup)
        try {
            customerService.deleteCustomer(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Customer not found");
        }
    }

    private Function<CustomerEntity, CustomerOverview> toOverview() {
        return customer ->
<<<<<<< HEAD
                CustomerOverview.of(createCustomerUri(customer.getId()), customer.getCustomer());
    }

    private URI createCustomerUri(long customerId) {
        return URI.create("/api/customers/" + customerId);
=======
                CustomerOverview.newCustomerOverview(
                        createCustomerUri(customer.getId()), customer.getCustomer());
    }

    private URI createCustomerUri(CustomerId customerId) {
        return URI.create("/api/customers/" + customerId.customerId());
>>>>>>> 8740e8a (Initial setup)
    }
}
