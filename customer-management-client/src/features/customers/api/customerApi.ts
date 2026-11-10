import type { Customer } from '../../../domain/Customer';
import type { CustomerOverview } from '../../../domain/CustomerOverview';

const BASE_URL = '/api/customers';

export async function fetchCustomers(): Promise<CustomerOverview[]> {
  const response = await fetch(BASE_URL);
  if (!response.ok) {
    throw new Error('Customers could not be loaded');
  }
  return response.json();
}

export async function fetchCustomer(id: number): Promise<Customer> {
  const response = await fetch(`${BASE_URL}/${id}`);
  if (!response.ok) {
    throw new Error('Customer not found');
  }
  return response.json();
}

export async function deleteCustomer(id: number): Promise<void> {
  const response = await fetch(`${BASE_URL}/${id}`, { method: 'DELETE' });
  if (!response.ok) {
    throw new Error('Customer could not be deleted');
  }
}

export async function updateCustomer(id: number, data: Customer): Promise<void> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!response.ok) {
    throw new Error('Customer could not be updated');
  }
}

export async function createCustomer(data: Customer): Promise<void> {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!response.ok) {
    throw new Error('Customer could not be created');
  }
}
