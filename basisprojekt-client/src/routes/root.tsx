import { createRootRoute, Link, Outlet } from '@tanstack/react-router';

export const Route = createRootRoute({
  component: RootLayout,
});

export function RootLayout() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>Kundenverwaltung</h1>
        <nav>
          <Link to="/customers">Kunden</Link>
        </nav>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
