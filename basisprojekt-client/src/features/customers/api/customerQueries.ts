import { queryOptions, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchCustomers, fetchCustomer, updateCustomer } from './customerApi';
import type { Customer } from '../../../domain/Customer';

export const customerKeys = {
  all: ['customers'] as const,
  detail: (id: number) => ['customers', id] as const,
};

export const customersQueryOptions = queryOptions({
  queryKey: customerKeys.all,
  queryFn: fetchCustomers,
});

export const customerQueryOptions = (id: number) =>
  queryOptions({
    queryKey: customerKeys.detail(id),
    queryFn: () => fetchCustomer(id),
  });

export function useUpdateCustomer(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: Customer) => updateCustomer(id, data),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: customerKeys.all });
      await queryClient.invalidateQueries({ queryKey: customerKeys.detail(id) });
    },
  });
}
