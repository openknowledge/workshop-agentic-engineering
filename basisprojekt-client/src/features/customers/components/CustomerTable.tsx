import { useState } from 'react';
import {
<<<<<<< HEAD
  createColumnHelper,
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  flexRender,
=======
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
>>>>>>> 8740e8a (Initial setup)
  type SortingState,
} from '@tanstack/react-table';
import { Link } from '@tanstack/react-router';
import type { CustomerOverview } from '../../../domain/CustomerOverview';
import { useDeleteCustomer } from '../api/customerQueries';

<<<<<<< HEAD
const columnHelper = createColumnHelper<CustomerOverview>();
=======
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
>>>>>>> 8740e8a (Initial setup)

function ActionButtons({ customerId }: { customerId: string }) {
  const deleteMutation = useDeleteCustomer();

  const handleDelete = () => {
    if (window.confirm('Kunde wirklich löschen?')) {
      deleteMutation.mutate(Number(customerId));
    }
  };

  return (
    <div className="action-buttons">
      <Link to="/customers/$customerId" params={{ customerId }} className="btn btn-edit">
        Bearbeiten
      </Link>
      <button
        type="button"
        onClick={handleDelete}
        disabled={deleteMutation.isPending}
        className="btn btn-delete"
      >
        {deleteMutation.isPending ? 'Wird gelöscht...' : 'Löschen'}
      </button>
    </div>
  );
}

<<<<<<< HEAD
const columns = [
=======
const columns = columnHelper.columns([
>>>>>>> 8740e8a (Initial setup)
  columnHelper.accessor('name.firstName', { id: 'firstName', header: 'Vorname' }),
  columnHelper.accessor('name.lastName', { id: 'lastName', header: 'Nachname' }),
  columnHelper.accessor('email', { header: 'E-Mail' }),
  columnHelper.accessor('phoneNumber', {
    header: 'Telefon',
    cell: (info) => info.getValue() ?? '-',
  }),
  columnHelper.accessor('birthDate', {
    header: 'Geburtsdatum',
    cell: (info) => info.getValue() ?? '-',
  }),
  columnHelper.display({
    id: 'actions',
    header: 'Aktionen',
    cell: ({ row }) => {
      const customerId = row.original.self.href.split('/').pop()!;
      return <ActionButtons customerId={customerId} />;
    },
  }),
<<<<<<< HEAD
];
=======
]);
>>>>>>> 8740e8a (Initial setup)

interface CustomerTableProps {
  customers: CustomerOverview[];
}

export function CustomerTable({ customers }: CustomerTableProps) {
  const [sorting, setSorting] = useState<SortingState>([]);
  const [globalFilter, setGlobalFilter] = useState('');

<<<<<<< HEAD
  const table = useReactTable({
=======
  const table = useTable({
    features,
>>>>>>> 8740e8a (Initial setup)
    data: customers,
    columns,
    state: {
      sorting,
      globalFilter,
    },
    onSortingChange: setSorting,
    onGlobalFilterChange: setGlobalFilter,
<<<<<<< HEAD
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
=======
>>>>>>> 8740e8a (Initial setup)
  });

  return (
    <div className="table-container">
      <div className="table-toolbar">
        <input
          value={globalFilter}
          onChange={(e) => setGlobalFilter(e.target.value)}
          placeholder="Suchen..."
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
<<<<<<< HEAD
                  {flexRender(header.column.columnDef.header, header.getContext())}
=======
                  <table.FlexRender header={header} />
>>>>>>> 8740e8a (Initial setup)
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
                Keine Kunden vorhanden
              </td>
            </tr>
          ) : (
            table.getRowModel().rows.map((row) => (
              <tr key={row.id}>
<<<<<<< HEAD
                {row.getVisibleCells().map((cell) => (
                  <td key={cell.id}>{flexRender(cell.column.columnDef.cell, cell.getContext())}</td>
=======
                {row.getAllCells().map((cell) => (
                  <td key={cell.id}>
                    <table.FlexRender cell={cell} />
                  </td>
>>>>>>> 8740e8a (Initial setup)
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
          Zurück
        </button>
        <span>
<<<<<<< HEAD
          Seite {table.getState().pagination.pageIndex + 1} von {table.getPageCount()}
=======
          Seite {table.state.pagination.pageIndex + 1} von {table.getPageCount()}
>>>>>>> 8740e8a (Initial setup)
        </span>
        <button type="button" onClick={() => table.nextPage()} disabled={!table.getCanNextPage()}>
          Weiter
        </button>
      </div>
    </div>
  );
}
