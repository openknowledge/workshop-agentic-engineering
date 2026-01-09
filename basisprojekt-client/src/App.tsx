import { useEffect, useState } from 'react';
import type { Customer } from './domain/Customer';
import type { CustomerOverview } from './domain/CustomerOverview';

function App() {
  const [customers, setCustomers] = useState<CustomerOverview[] | undefined>();
  const [selected, setSelected] = useState<{ href: string; customer: Customer } | undefined>();

  const loadCustomers = () => {
    fetch('api/customers')
      .then((response) => response.json())
      .then(setCustomers)
      .catch(console.error);
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const handleSelect = (overview: CustomerOverview) => {
    const { self, ...customer } = overview;
    setSelected({ href: self.href, customer });
  };

  const handleChange = (field: keyof Customer, value: string) => {
    setSelected((prev) => prev && { ...prev, customer: { ...prev.customer, [field]: value } });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selected) return;
    fetch(selected.href, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(selected.customer),
    })
      .then(() => {
        setSelected(undefined);
        loadCustomers();
      })
      .catch(console.error);
  };

  return (
    <div className="app">
      <h1>Customers</h1>
      {customers === undefined ? (
        <p>Loading...</p>
      ) : (
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
              customers.map((customer) => (
                <tr key={customer.self.href}>
                  <td>{customer.firstName}</td>
                  <td>{customer.lastName}</td>
                  <td>{customer.email}</td>
                  <td>{customer.phoneNumber ?? '-'}</td>
                  <td>{customer.birthDate ?? '-'}</td>
                  <td>
                    <a href="#edit-form" onClick={() => handleSelect(customer)} className="btn">
                      Bearbeiten
                    </a>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
      {selected && (
        <form id="edit-form" onSubmit={handleSubmit} className="customer-form">
          <h2>Kunde bearbeiten</h2>
          <div className="form-field">
            <label htmlFor="firstName">Vorname</label>
            <input
              id="firstName"
              value={selected.customer.firstName}
              onChange={(e) => handleChange('firstName', e.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="lastName">Nachname</label>
            <input
              id="lastName"
              value={selected.customer.lastName}
              onChange={(e) => handleChange('lastName', e.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="email">E-Mail</label>
            <input
              id="email"
              value={selected.customer.email}
              onChange={(e) => handleChange('email', e.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="phoneNumber">Telefonnummer</label>
            <input
              id="phoneNumber"
              value={selected.customer.phoneNumber ?? ''}
              onChange={(e) => handleChange('phoneNumber', e.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="birthDate">Geburtsdatum</label>
            <input
              id="birthDate"
              type="date"
              value={selected.customer.birthDate ?? ''}
              onChange={(e) => handleChange('birthDate', e.target.value)}
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              Speichern
            </button>
            <button type="button" className="btn" onClick={() => setSelected(undefined)}>
              Abbrechen
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

export default App;
