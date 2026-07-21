import { AdminMainHeader } from "./AdminMainHeader.jsx";

/**
 * Full-width admin shell (guide Method 1):
 * w-full + padding only — no mx-auto / max-w container.
 */
export function AdminPageLayout({ nav, children }) {
  return (
    <div className="flex min-h-dvh w-full bg-admin-canvas">
      <div className="w-full shrink-0 lg:fixed lg:inset-y-0 lg:left-0 lg:z-30 lg:w-64">{nav}</div>

      <div className="flex min-w-0 flex-1 flex-col lg:pl-64">
        <AdminMainHeader />
        <main className="flex-1">
          <div className="w-full px-4 py-6 sm:px-6 lg:px-8">{children}</div>
        </main>
      </div>
    </div>
  );
}
