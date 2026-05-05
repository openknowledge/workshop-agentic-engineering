import { useEffect, useState } from 'react';
import { createRoute, Link } from '@tanstack/react-router';
import type { CustomerOverview } from '../../domain/CustomerOverview';
import { Route as rootRoute } from '../root';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/customers',
  component: CustomerListPage,
});

export function CustomerListPage() {
  const [customers, setCustomers] = useState<CustomerOverview[] | undefined>();

  useEffect(() => {
    fetch('/api/customers')
      .then((res) => res.json())
      .then(setCustomers)
      .catch(console.error);
  }, []);

  if (customers === undefined) {
    return <p>Laden...</p>;
  }

  return (
    <div>
      <div className="page-header">
        <h2>Kunden</h2>
      </div>
      <table className="data-table">
        <thead>
          <tr>
            <th>Vorname</th>
            <th>Nachname</th>
            <th>E-Mail</th>
            <th>Telefon</th>
            <th>Geburtsdatum</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          {customers.length === 0 ? (
            <tr>
              <td colSpan={6} className="empty-row">
                Keine Kunden vorhanden
              </td>
            </tr>
          ) : (
            customers.map((customer) => {
              const customerId = customer.self.href.split('/').pop()!;
              return (
                <tr key={customer.self.href}>
                  <td>{customer.firstName}</td>
                  <td>{customer.lastName}</td>
                  <td>{customer.email}</td>
                  <td>{customer.phoneNumber ?? '-'}</td>
                  <td>{customer.birthDate ?? '-'}</td>
                  <td>
                    // tag::link-navigation[]
                    <Link
                      to="/customers/$customerId"
                      params={{ customerId }}
                      className="btn btn-edit"
                    >
                      Bearbeiten
                    </Link>
                    // end::link-navigation[]
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}
