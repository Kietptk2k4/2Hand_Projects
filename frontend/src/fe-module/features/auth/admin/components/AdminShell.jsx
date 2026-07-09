import { AdminShellProvider } from "./AdminShellContext.jsx";

export function AdminShell({ children }) {
  return (
    <AdminShellProvider>
      <div className="admin-shell min-h-dvh">{children}</div>
    </AdminShellProvider>
  );
}
