import { render, screen } from '@testing-library/react';
import type { AnyFieldApi } from '@tanstack/react-form';
import { FormField } from '../../src/components/FormField';

function makeField(errors: unknown[] = []): AnyFieldApi {
  return {
    name: 'testField',
    state: {
      value: '',
      meta: { errors, isTouched: false, isDirty: false, isValidating: false },
    },
    handleChange: vi.fn(),
    handleBlur: vi.fn(),
  } as unknown as AnyFieldApi;
}

test('displays string error message directly', () => {
  render(<FormField field={makeField(['Required field'])} label="Test field" />);

  expect(screen.getByText('Required field')).toBeInTheDocument();
});

test('extracts readable message from object error', () => {
  render(<FormField field={makeField([{ message: 'Invalid value' }])} label="Test field" />);

  expect(screen.getByText('Invalid value')).toBeInTheDocument();
});

test('concatenates multiple errors to one string', () => {
  render(
    <FormField field={makeField(['Required', { message: 'Too short' }])} label="Test field" />
  );

  expect(screen.getByText('Required, Too short')).toBeInTheDocument();
});

test('shows an asterisks at the required input fields', () => {
  render(<FormField field={makeField()} label="First name" required />);

  expect(screen.getByText(/First name \*/)).toBeInTheDocument();
});

test('does not show an asterisks at an optional field', () => {
  render(<FormField field={makeField()} label="Phone number" />);

  expect(screen.queryByText(/\*/)).not.toBeInTheDocument();
});
