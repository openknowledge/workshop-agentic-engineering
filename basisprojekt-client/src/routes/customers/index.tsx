import { createRoute } from '@tanstack/react-router';
import { useSuspenseQuery } from '@tanstack/react-query';
import { customersQueryOptions } from '../../features/customers/api/customerQueries';
import { Route as rootRoute } from '../root';
import { CustomerTable } from '../../features/customers/components/CustomerTable';

export const Route = createRoute({
  getParentRoute: () => rootRoute,
  path: '/customers',
  loader: ({ context: { queryClient } }) => queryClient.ensureQueryData(customersQueryOptions),
  component: CustomerListPage,
});

export function CustomerListPage() {
  const { data: customers } = useSuspenseQuery(customersQueryOptions);

  return (
    <div>
      <div className="page-header">
        <h2>Kunden</h2>
      </div>
      <CustomerTable customers={customers} />
    </div>
  );
}
