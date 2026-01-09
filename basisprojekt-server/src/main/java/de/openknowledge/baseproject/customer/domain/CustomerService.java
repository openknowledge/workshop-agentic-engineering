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
package de.openknowledge.baseproject.customer.domain;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CustomerService {

    @Autowired private CustomerRepository repository;

    public List<CustomerEntity> findAllCustomers() {
        return repository.findAll();
    }

    public Optional<CustomerEntity> findCustomer(Long id) {
        return repository.findById(id);
    }

    public CustomerEntity createCustomer(Customer customer) {
        repository
                .findByCustomerEmail(customer.email())
                .ifPresent(
                        existing -> {
                            throw new IllegalArgumentException("Email already in use");
                        });
        CustomerEntity entity = new CustomerEntity(customer);
        return repository.save(entity);
    }

    public void updateCustomer(Long id, Customer customer) {
        repository
                .findByCustomerEmail(customer.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(
                        conflicting -> {
                            throw new IllegalArgumentException("Email already in use");
                        });
        repository
                .findById(id)
                .ifPresentOrElse(
                        entity -> entity.update(customer),
                        () -> {
                            throw new EntityNotFoundException();
                        });
    }

    public void deleteCustomer(Long id) {
        repository
                .findById(id)
                .ifPresentOrElse(
                        repository::delete,
                        () -> {
                            throw new EntityNotFoundException();
                        });
    }
}
