import { useState, useEffect } from 'react';
import { createRoute, useNavigate, Link } from '@tanstack/react-router';
import type { Customer } from '../../domain/Customer';
import { Route as rootRoute } from '../root';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/customers/$customerId',
  component: EditCustomerPage,
});

export function EditCustomerPage() {
  const { customerId } = Route.useParams();
  // tag::navigate-usage[]
  const navigate = useNavigate();
  // end::navigate-usage[]
  const [customer, setCustomer] = useState<Customer | undefined>();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch(`/api/customers/${customerId}`)
      .then((res) => {
        if (!res.ok) throw new Error('Kunde nicht gefunden');
        return res.json();
      })
      .then(setCustomer)
      .catch((err: Error) => setError(err.message));
  }, [customerId]);

  const handleChange = (field: keyof Customer, value: string) => {
    setCustomer((prev) => prev && { ...prev, [field]: value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!customer) return;
    setError(null);
    fetch(`/api/customers/${customerId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customer),
    })
      .then((res) => {
        if (!res.ok) throw new Error('Kunde konnte nicht gespeichert werden');
        // tag::navigate-call[]
        navigate({ to: '/customers' });
        // end::navigate-call[]
      })
      .catch((err: Error) => setError(err.message));
  };

  if (customer === undefined && error === null) {
    return <p>Laden...</p>;
  }

  return (
    <div>
      <div className="page-header">
        <h2>Kunde bearbeiten</h2>
        <Link to="/customers" className="btn">
          Zurück
        </Link>
      </div>
      {error && <div className="error-message">{error}</div>}
      {customer && (
        <form onSubmit={handleSubmit} className="customer-form">
          <div className="form-field">
            <label htmlFor="firstName">Vorname *</label>
            <input
              id="firstName"
              value={customer.firstName}
              onChange={(e) => handleChange('firstName', e.target.value)}
              required
            />
          </div>
          <div className="form-field">
            <label htmlFor="lastName">Nachname *</label>
            <input
              id="lastName"
              value={customer.lastName}
              onChange={(e) => handleChange('lastName', e.target.value)}
              required
            />
          </div>
          <div className="form-field">
            <label htmlFor="email">E-Mail *</label>
            <input
              id="email"
              type="email"
              value={customer.email}
              onChange={(e) => handleChange('email', e.target.value)}
              required
            />
          </div>
          <div className="form-field">
            <label htmlFor="phoneNumber">Telefonnummer</label>
            <input
              id="phoneNumber"
              type="tel"
              value={customer.phoneNumber ?? ''}
              onChange={(e) => handleChange('phoneNumber', e.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="birthDate">Geburtsdatum</label>
            <input
              id="birthDate"
              type="date"
              value={customer.birthDate ?? ''}
              onChange={(e) => handleChange('birthDate', e.target.value)}
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              Speichern
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
