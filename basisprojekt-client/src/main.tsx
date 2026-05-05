import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider, createRouter } from '@tanstack/react-router';
import { Route as rootRoute } from './routes/root';
import { Route as indexRoute } from './routes/index';
import { Route as customersRoute } from './routes/customers/index';
import { Route as customerDetailRoute } from './routes/customers/editCustomer';
import './index.css';

const routeTree = rootRoute.addChildren([indexRoute, customersRoute, customerDetailRoute]);

const router = createRouter({ routeTree });

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
