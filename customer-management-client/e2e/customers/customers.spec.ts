import { test, expect } from '../fixtures';

test('displays the customer list', async ({ page }) => {
  await page.goto('/customers');
  await expect(page.getByRole('heading', { name: 'Customers', exact: true })).toBeVisible();
  await expect(page.getByRole('table')).toBeVisible();
});

test('navigates to the edit page of a customer', async ({ page }) => {
  await page.goto('/customers');
  await page.getByRole('link', { name: 'Edit' }).first().click();
  await expect(page).toHaveURL(/\/customers\/\d+$/);
  await expect(page.getByRole('heading', { name: 'Edit customer' })).toBeVisible();
});

test('saves changes and returns to the customer list', async ({ page }) => {
  await page.goto('/customers');
  await page.getByRole('link', { name: 'Edit' }).first().click();

  const firstNameInput = page.getByLabel(/First name/);
  await firstNameInput.clear();
  await firstNameInput.fill('Max');

  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page).toHaveURL('/customers');
});

test('filters the customer list via the search field', async ({ page }) => {
  await page.goto('/customers');
  await expect(page.locator('tbody')).toContainText('Mustermann');
  await expect(page.locator('tbody')).toContainText('Musterfrau');

  await page.getByPlaceholder('Search...').fill('Erika');

  await expect(page.locator('tbody')).toContainText('Musterfrau');
  await expect(page.locator('tbody')).not.toContainText('Mustermann');
});

test('sorts the table when clicking a column header', async ({ page }) => {
  await page.goto('/customers');
  await page.getByPlaceholder('Search...').fill('Muster');

  const lastNameHeader = page.getByRole('columnheader', { name: /Last name/ });

  await lastNameHeader.click();
  await expect(lastNameHeader).toContainText('▲');
  await expect(page.locator('tbody tr').first()).toContainText('Musterfrau');

  await lastNameHeader.click();
  await expect(lastNameHeader).toContainText('▼');
  await expect(page.locator('tbody tr').first()).toContainText('Mustermann');
});

test('shows a hint when the customer list is empty', async ({ page }) => {
  await page.route('**/api/customers', (route) => route.fulfill({ json: [] }));

  await page.goto('/customers');

  await expect(page.getByRole('cell', { name: 'No customers available' })).toBeVisible();
});

test('disables the pagination buttons when there is only one page', async ({ page }) => {
  await page.goto('/customers');

  await expect(page.getByRole('button', { name: 'Previous' })).toBeDisabled();
  await expect(page.getByRole('button', { name: 'Next' })).toBeDisabled();
});

test('cancels the deletion when the confirmation is declined', async ({ page }) => {
  await page.goto('/customers');
  await expect(page.locator('tbody')).toContainText('Mustermann');

  page.once('dialog', (dialog) => dialog.dismiss());
  await page.getByRole('row', { name: /Max/ }).getByRole('button', { name: 'Delete' }).click();

  await expect(page.locator('tbody')).toContainText('Mustermann');
});

test('creates a new customer and deletes them again', async ({ page }) => {
  const uniqueEmail = `e2e.create.delete+${Date.now()}-${Math.random().toString(36).slice(2)}@example.com`;

  await page.goto('/customers/new');
  await page.getByLabel(/First name/).fill('E-to-E');
  await page.getByLabel(/Last name/).fill('Tester');
  await page.getByLabel(/Email/).fill(uniqueEmail);
  await page.getByRole('button', { name: 'Create' }).click();

  await expect(page).toHaveURL(/\/customers$/);

  await page.getByPlaceholder('Search...').fill(uniqueEmail);
  const newRow = page.getByRole('row', { name: new RegExp(`E-to-E\\s+Tester`) });
  await expect(newRow).toBeVisible();

  page.once('dialog', (dialog) => dialog.accept());
  await newRow.getByRole('button', { name: 'Delete' }).click();

  await expect(page.locator('tbody')).not.toContainText(uniqueEmail);
});

test('prefills the edit form with the customer data', async ({ page }) => {
  await page.goto('/customers');
  await page.getByRole('row', { name: /Erika/ }).getByRole('link', { name: 'Edit' }).click();

  await expect(page.getByLabel(/First name/)).toHaveValue('Erika');
  await expect(page.getByLabel(/Last name/)).toHaveValue('Musterfrau');
  await expect(page.getByLabel(/Email/)).toHaveValue('erika.musterfrau@openknowledge.de');
  await expect(page.getByLabel(/Phone number/)).toHaveValue('+49 987 6543210');
  await expect(page.getByLabel(/Date of birth/)).toHaveValue('1985-11-23');
});

test('shows an error message when saving fails', async ({ page }) => {
  await page.goto('/customers');
  await page.getByRole('link', { name: 'Edit' }).first().click();

  await page.route('**/api/customers/*', (route) => {
    if (route.request().method() === 'PUT') {
      return route.fulfill({ status: 500, body: 'Internal Server Error' });
    }
    return route.fallback();
  });

  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page.getByText('Customer could not be updated')).toBeVisible();
  await expect(page).toHaveURL(/\/customers\/\d+$/);
});

test('shows an error message for an invalid email address', async ({ page }) => {
  await page.goto('/customers');
  await page.getByRole('link', { name: 'Edit' }).first().click();

  const emailInput = page.getByLabel(/Email/);
  await emailInput.clear();
  await emailInput.fill('not-an-email');
  await emailInput.blur();

  await expect(page.getByText('Invalid email address')).toBeVisible();
});

test('shows an error message for an empty last name', async ({ page }) => {
  await page.goto('/customers');
  await page.getByRole('link', { name: 'Edit' }).first().click();

  const lastNameInput = page.getByLabel(/Last name/);
  await lastNameInput.clear();
  await lastNameInput.blur();

  await expect(page.getByText('Last name is required')).toBeVisible();
});

test('disables the save button when the form is invalid', async ({ page }) => {
  await page.goto('/customers');
  await page.getByRole('link', { name: 'Edit' }).first().click();

  const emailInput = page.getByLabel(/Email/);
  await emailInput.clear();
  await emailInput.fill('not-an-email');
  await emailInput.blur();

  await expect(page.getByRole('button', { name: 'Save' })).toBeDisabled();
});

test('paginates to the next page', async ({ page }) => {
  const manyCustomers = Array.from({ length: 12 }, (_, index) => ({
    firstName: `FirstName${index + 1}`,
    lastName: `LastName${index + 1}`,
    email: `customer${index + 1}@example.com`,
    self: { href: `/api/customers/${index + 1}` },
  }));
  await page.route('**/api/customers', (route) => route.fulfill({ json: manyCustomers }));

  await page.goto('/customers');
  await expect(page.locator('tbody tr')).toHaveCount(10);
  await expect(page.getByText('Page 1 of 2')).toBeVisible();

  await page.getByRole('button', { name: 'Next' }).click();

  await expect(page.locator('tbody tr')).toHaveCount(2);
  await expect(page.getByText('Page 2 of 2')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Next' })).toBeDisabled();

  await page.getByRole('button', { name: 'Previous' }).click();

  await expect(page.locator('tbody tr')).toHaveCount(10);
  await expect(page.getByText('Page 1 of 2')).toBeVisible();
});

test('sorts the table by first name and by email', async ({ page }) => {
  await page.goto('/customers');
  await page.getByPlaceholder('Search...').fill('Muster');

  const firstNameHeader = page.getByRole('columnheader', { name: /First name/ });
  await firstNameHeader.click();
  await expect(firstNameHeader).toContainText('▲');
  await expect(page.locator('tbody tr').first()).toContainText('Erika');

  const emailHeader = page.getByRole('columnheader', { name: /Email/ });
  await emailHeader.click();
  await expect(emailHeader).toContainText('▲');
  await expect(page.locator('tbody tr').first()).toContainText('erika.musterfrau@openknowledge.de');

  await emailHeader.click();
  await expect(emailHeader).toContainText('▼');
  await expect(page.locator('tbody tr').first()).toContainText('max.mustermann@openknowledge.de');
});

test('shows an error screen when loading the customer list fails', async ({ page }) => {
  await page.route('**/api/customers', (route) =>
    route.fulfill({ status: 500, body: 'Internal Server Error' })
  );

  await page.goto('/customers');

  await expect(page.getByText('Something went wrong!')).toBeVisible();
  await expect(page.getByText('Customers could not be loaded')).toBeVisible();
  await expect(page.getByRole('table')).toHaveCount(0);
});

test('shows an error screen when a customer does not exist', async ({ page }) => {
  await page.goto('/customers/9999');

  await expect(page.getByText('Something went wrong!')).toBeVisible();
  await expect(page.getByText('Customer not found')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save' })).toHaveCount(0);
});

test('leaves the table unchanged when the deletion fails', async ({ page }) => {
  await page.route('**/api/customers/*', (route) => {
    if (route.request().method() === 'DELETE') {
      return route.fulfill({ status: 500, body: 'Internal Server Error' });
    }
    return route.fallback();
  });

  await page.goto('/customers');
  await expect(page.locator('tbody tr').first()).toBeVisible();
  const rowsBefore = await page.locator('tbody tr').count();

  page.once('dialog', (dialog) => dialog.accept());
  await page.getByRole('row', { name: /Max/ }).getByRole('button', { name: 'Delete' }).click();

  await expect(page.getByRole('button', { name: 'Delete' }).first()).toBeEnabled();
  await expect(page.locator('tbody tr')).toHaveCount(rowsBefore);
});

test('opens the form for a new customer via the link in the customer list', async ({ page }) => {
  await page.goto('/customers');
  await page.getByRole('link', { name: 'Create new customer' }).click();

  await expect(page).toHaveURL(/\/customers\/new$/);
  await expect(page.getByRole('heading', { name: 'New customer' })).toBeVisible();
  await expect(page.getByLabel(/First name/)).toHaveValue('');
  await expect(page.getByLabel(/Last name/)).toHaveValue('');
  await expect(page.getByLabel(/Email/)).toHaveValue('');
});

test('validates the email address in the new customer form', async ({ page }) => {
  await page.goto('/customers/new');

  const emailInput = page.getByLabel(/Email/);
  await emailInput.fill('not-an-email');
  await emailInput.blur();

  await expect(page.getByText('Invalid email address')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Create' })).toBeDisabled();
});

test('returns from the new customer form to the customer list without saving', async ({ page }) => {
  await page.goto('/customers/new');
  await page.getByRole('link', { name: 'Back' }).click();

  await expect(page).toHaveURL(/\/customers$/);
  await expect(page.getByRole('heading', { name: 'Customers', exact: true })).toBeVisible();
});

test('shows an error message when the creation fails', async ({ page }) => {
  await page.route('**/api/customers', (route) => {
    if (route.request().method() === 'POST') {
      return route.fulfill({ status: 500, body: 'Internal Server Error' });
    }
    return route.fallback();
  });

  await page.goto('/customers/new');
  await page.getByLabel(/First name/).fill('New');
  await page.getByLabel(/Last name/).fill('Customer');
  await page.getByLabel(/Email/).fill('new@example.com');

  await page.getByRole('button', { name: 'Create' }).click();

  await expect(page.getByText('Customer could not be created')).toBeVisible();
  await expect(page).toHaveURL(/\/customers\/new$/);
});
