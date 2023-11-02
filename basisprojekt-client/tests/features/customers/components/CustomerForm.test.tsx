import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CustomerForm } from '../../../../src/features/customers/components/CustomerForm';

// tag::validation-test[]
test('shows error message if required input field is empty', async () => {
  const user = userEvent.setup();
  render(<CustomerForm onSubmit={vi.fn()} isPending={false} submitLabel="Speichern" />);

  const firstName = screen.getByLabelText(/Vorname/);
  await user.type(firstName, 'A');
  await user.clear(firstName);
  await user.tab();

  expect(await screen.findByText(/Vorname ist erforderlich/i)).toBeInTheDocument();
});
// end::validation-test[]

test('show pending state and deactivates button on submission', () => {
  render(<CustomerForm onSubmit={vi.fn()} isPending={true} submitLabel="Speichern" />);

  const button = screen.getByRole('button', { name: 'Wird gespeichert...' });
  expect(button).toBeDisabled();
});

async function fillRequiredFields(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText(/Vorname/), 'Max');
  await user.type(screen.getByLabelText(/Nachname/), 'Mustermann');
  await user.type(screen.getByLabelText(/E-Mail/), 'max@example.com');
}

test('emits null for cleared optional fields', async () => {
  const user = userEvent.setup();
  const onSubmit = vi.fn();
  render(<CustomerForm onSubmit={onSubmit} isPending={false} submitLabel="Speichern" />);

  await fillRequiredFields(user);
  await user.click(screen.getByRole('button', { name: 'Speichern' }));

  expect(onSubmit).toHaveBeenCalledWith(
    expect.objectContaining({ phoneNumber: null, birthDate: null })
  );
});

test('emits exactly the known form fields', async () => {
  const user = userEvent.setup();
  const onSubmit = vi.fn();
  render(<CustomerForm onSubmit={onSubmit} isPending={false} submitLabel="Speichern" />);

  await fillRequiredFields(user);
  await user.click(screen.getByRole('button', { name: 'Speichern' }));

  expect(Object.keys(onSubmit.mock.calls[0][0]).sort()).toEqual([
    'birthDate',
    'email',
    'name',
    'phoneNumber',
  ]);
});
