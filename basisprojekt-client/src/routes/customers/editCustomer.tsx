import { useState } from 'react';
import { createRoute, useNavigate, Link } from '@tanstack/react-router';
import { useSuspenseQuery } from '@tanstack/react-query';
import type { Customer } from '../../domain/Customer';
import {
  customerQueryOptions,
  useUpdateCustomer,
} from '../../features/customers/api/customerQueries';
import { Route as rootRoute } from '../root';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/customers/$customerId',
  loader: ({ context: { queryClient }, params: { customerId } }) =>
    queryClient.ensureQueryData(customerQueryOptions(Number(customerId))),
  component: EditCustomerPage,
});

export function EditCustomerPage() {
  const { customerId } = Route.useParams();
  const id = Number(customerId);
  // tag::navigate-usage[]
  const navigate = useNavigate();
  // end::navigate-usage[]
  const { data: customer } = useSuspenseQuery(customerQueryOptions(id));
  const updateMutation = useUpdateCustomer(id);
  const [formData, setFormData] = useState<Customer>(() => ({ ...customer }));
  const [error, setError] = useState<string | null>(null);

  const handleChange = (field: keyof Customer, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    updateMutation.mutate(formData, {
      // tag::navigate-call[]
      onSuccess: () => navigate({ to: '/customers' }),
      // end::navigate-call[]
      onError: (err) => setError(err.message),
    });
  };

  return (
    <div>
      <div className="page-header">
        <h2>Kunde bearbeiten</h2>
        <Link to="/customers" className="btn">
          Zurück
        </Link>
      </div>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit} className="customer-form">
        <div className="form-field">
          <label htmlFor="firstName">Vorname *</label>
          <input
            id="firstName"
            value={formData.firstName}
            onChange={(e) => handleChange('firstName', e.target.value)}
            required
          />
        </div>
        <div className="form-field">
          <label htmlFor="lastName">Nachname *</label>
          <input
            id="lastName"
            value={formData.lastName}
            onChange={(e) => handleChange('lastName', e.target.value)}
            required
          />
        </div>
        <div className="form-field">
          <label htmlFor="email">E-Mail *</label>
          <input
            id="email"
            type="email"
            value={formData.email}
            onChange={(e) => handleChange('email', e.target.value)}
            required
          />
        </div>
        <div className="form-field">
          <label htmlFor="phoneNumber">Telefonnummer</label>
          <input
            id="phoneNumber"
            type="tel"
            value={formData.phoneNumber ?? ''}
            onChange={(e) => handleChange('phoneNumber', e.target.value)}
          />
        </div>
        <div className="form-field">
          <label htmlFor="birthDate">Geburtsdatum</label>
          <input
            id="birthDate"
            type="date"
            value={formData.birthDate ?? ''}
            onChange={(e) => handleChange('birthDate', e.target.value)}
          />
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={updateMutation.isPending}>
            {updateMutation.isPending ? 'Wird gespeichert...' : 'Speichern'}
          </button>
        </div>
      </form>
    </div>
  );
}
