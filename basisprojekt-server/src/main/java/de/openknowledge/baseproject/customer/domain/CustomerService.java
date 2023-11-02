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

<<<<<<< HEAD
import de.openknowledge.baseproject.todo.integration.DisplayName;
import de.openknowledge.baseproject.todo.integration.Title;
import de.openknowledge.baseproject.todo.integration.Todo;
import de.openknowledge.baseproject.todo.integration.TodoRepository;
import de.openknowledge.baseproject.todo.integration.TodoUser;
=======
import static de.openknowledge.baseproject.todo.integration.CompletionState.newCompletionState;
import static de.openknowledge.baseproject.todo.integration.DisplayName.newDisplayName;
import static de.openknowledge.baseproject.todo.integration.Title.newTitle;
import static de.openknowledge.baseproject.todo.integration.Todo.newTodo;
import static de.openknowledge.baseproject.todo.integration.TodoUser.newTodoUser;
import static de.openknowledge.baseproject.todo.integration.UserEmail.newUserEmail;

import de.openknowledge.baseproject.todo.integration.DisplayName;
import de.openknowledge.baseproject.todo.integration.Title;
import de.openknowledge.baseproject.todo.integration.TodoRepository;
import de.openknowledge.baseproject.todo.integration.TodoUser;
import de.openknowledge.baseproject.todo.integration.UserEmail;
>>>>>>> 8740e8a (Initial setup)
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CustomerService {

<<<<<<< HEAD
    private static final Title WELCOME_TODO_TITLE = new Title("Send welcome email");
=======
    private static final Title WELCOME_TODO_TITLE = newTitle("Send welcome email");
>>>>>>> 8740e8a (Initial setup)

    @Autowired private CustomerRepository repository;

    @Autowired private TodoRepository todoRepository;

    public List<CustomerEntity> findAllCustomers() {
        return repository.findAll();
    }

<<<<<<< HEAD
    public Optional<CustomerEntity> findCustomer(Long id) {
        return repository.findById(id);
=======
    public Optional<CustomerEntity> findCustomer(CustomerId id) {
        return repository.findByCustomerId(id);
>>>>>>> 8740e8a (Initial setup)
    }

    public CustomerEntity registerCustomer(Customer customer) {
        repository
                .findByCustomerEmail(customer.email())
                .ifPresent(
                        existing -> {
                            throw new IllegalArgumentException("Email already in use");
                        });
        TodoUser externalUser = lookupOrCreateExternalUser(customer);
<<<<<<< HEAD
        todoRepository.createTodo(new Todo(externalUser.id(), WELCOME_TODO_TITLE, false));
=======
        todoRepository.createTodo(
                newTodo(externalUser.id(), WELCOME_TODO_TITLE, newCompletionState(false)));
>>>>>>> 8740e8a (Initial setup)
        Customer updated = customer.withExternalUserId(externalUser.id());
        return repository.save(new CustomerEntity(updated));
    }

    // Intentional full overwrite, including externalUserId: the client is responsible for
    // round-tripping fields it doesn't edit itself. Do not reintroduce a server-side preserve
    // call for externalUserId here; that would silently break the client's generic merge.
<<<<<<< HEAD
    public void updateCustomer(Long id, Customer customer) {
=======
    public void updateCustomer(CustomerId id, Customer customer) {
>>>>>>> 8740e8a (Initial setup)
        repository
                .findByCustomerEmail(customer.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(
                        conflicting -> {
                            throw new IllegalArgumentException("Email already in use");
                        });
        repository
<<<<<<< HEAD
                .findById(id)
=======
                .findByCustomerId(id)
>>>>>>> 8740e8a (Initial setup)
                .ifPresentOrElse(
                        entity -> entity.update(customer),
                        () -> {
                            throw new EntityNotFoundException();
                        });
    }

<<<<<<< HEAD
    public void deleteCustomer(Long id) {
        repository
                .findById(id)
=======
    public void deleteCustomer(CustomerId id) {
        repository
                .findByCustomerId(id)
>>>>>>> 8740e8a (Initial setup)
                .ifPresentOrElse(
                        repository::delete,
                        () -> {
                            throw new EntityNotFoundException();
                        });
    }

    private TodoUser lookupOrCreateExternalUser(Customer customer) {
<<<<<<< HEAD
        return todoRepository
                .findUserByEmail(customer.email())
                .orElseGet(
                        () ->
                                todoRepository.createUser(
                                        new TodoUser(
                                                null,
                                                buildDisplayName(customer),
                                                customer.email())));
=======
        UserEmail email = newUserEmail(customer.email().email());
        return todoRepository
                .findUserByEmail(email)
                .orElseGet(
                        () ->
                                todoRepository.createUser(
                                        newTodoUser(null, buildDisplayName(customer), email)));
>>>>>>> 8740e8a (Initial setup)
    }

    private DisplayName buildDisplayName(Customer customer) {
        Name name = customer.name();
<<<<<<< HEAD
        return new DisplayName(name.firstName().firstName() + " " + name.lastName().lastName());
=======
        return newDisplayName(name.firstName().firstName() + " " + name.lastName().lastName());
>>>>>>> 8740e8a (Initial setup)
    }
}
