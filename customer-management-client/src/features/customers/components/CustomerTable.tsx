import { useState } from 'react';
import {
  columnFilteringFeature,
  createColumnHelper,
  createFilteredRowModel,
  createPaginatedRowModel,
  createSortedRowModel,
  filterFn_includesString,
  globalFilteringFeature,
  rowPaginationFeature,
  rowSortingFeature,
  sortFn_alphanumeric,
  sortFn_text,
  tableFeatures,
  useTable,
  type SortingState,
} from '@tanstack/react-table';
import { Link } from '@tanstack/react-router';
import type { CustomerOverview } from '../../../domain/CustomerOverview';
import { useDeleteCustomer } from '../api/customerQueries';

const features = tableFeatures({
  columnFilteringFeature,
  globalFilteringFeature,
  rowSortingFeature,
  rowPaginationFeature,
  filteredRowModel: createFilteredRowModel(),
  sortedRowModel: createSortedRowModel(),
  paginatedRowModel: createPaginatedRowModel(),
  filterFns: { includesString: filterFn_includesString },
  sortFns: { alphanumeric: sortFn_alphanumeric, text: sortFn_text },
});

const columnHelper = createColumnHelper<typeof features, CustomerOverview>();

function ActionButtons({ customerId }: { customerId: string }) {
  const deleteMutation = useDeleteCustomer();

  const handleDelete = () => {
    if (window.confirm('Really delete customer?')) {
      deleteMutation.mutate(Number(customerId));
    }
  };

  return (
    <div className="action-buttons">
      <Link to="/customers/$customerId" params={{ customerId }} className="btn btn-edit">
        Edit
      </Link>
      <button
        type="button"
        onClick={handleDelete}
        disabled={deleteMutation.isPending}
        className="btn btn-delete"
      >
        {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
      </button>
    </div>
  );
}

const columns = columnHelper.columns([
  columnHelper.accessor('name.firstName', { id: 'firstName', header: 'First name' }),
  columnHelper.accessor('name.lastName', { id: 'lastName', header: 'Last name' }),
  columnHelper.accessor('email', { header: 'Email' }),
  columnHelper.accessor('phoneNumber', {
    header: 'Phone',
    cell: (info) => info.getValue() ?? '-',
  }),
  columnHelper.accessor('birthDate', {
    header: 'Date of birth',
    cell: (info) => info.getValue() ?? '-',
  }),
  columnHelper.display({
    id: 'actions',
    header: 'Actions',
    cell: ({ row }) => {
      const customerId = row.original.self.href.split('/').pop()!;
      return <ActionButtons customerId={customerId} />;
    },
  }),
]);

interface CustomerTableProps {
  customers: CustomerOverview[];
}

export function CustomerTable({ customers }: CustomerTableProps) {
  const [sorting, setSorting] = useState<SortingState>([]);
  const [globalFilter, setGlobalFilter] = useState('');

  const table = useTable({
    features,
    data: customers,
    columns,
    state: {
      sorting,
      globalFilter,
    },
    onSortingChange: setSorting,
    onGlobalFilterChange: setGlobalFilter,
  });

  return (
    <div className="table-container">
      <div className="table-toolbar">
        <input
          value={globalFilter}
          onChange={(e) => setGlobalFilter(e.target.value)}
          placeholder="Search..."
          className="search-input"
        />
      </div>

      <table className="data-table">
        <thead>
          {table.getHeaderGroups().map((headerGroup) => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map((header) => (
                <th
                  key={header.id}
                  onClick={header.column.getToggleSortingHandler()}
                  className={header.column.getCanSort() ? 'sortable' : ''}
                >
                  <table.FlexRender header={header} />
                  {header.column.getIsSorted() === 'asc' ? ' ▲' : ''}
                  {header.column.getIsSorted() === 'desc' ? ' ▼' : ''}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody>
          {table.getRowModel().rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="empty-row">
                No customers available
              </td>
            </tr>
          ) : (
            table.getRowModel().rows.map((row) => (
              <tr key={row.id}>
                {row.getAllCells().map((cell) => (
                  <td key={cell.id}>
                    <table.FlexRender cell={cell} />
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>

      <div className="pagination">
        <button
          type="button"
          onClick={() => table.previousPage()}
          disabled={!table.getCanPreviousPage()}
        >
          Previous
        </button>
        <span>
          Page {table.state.pagination.pageIndex + 1} of {table.getPageCount()}
        </span>
        <button type="button" onClick={() => table.nextPage()} disabled={!table.getCanNextPage()}>
          Next
        </button>
      </div>
    </div>
  );
}
