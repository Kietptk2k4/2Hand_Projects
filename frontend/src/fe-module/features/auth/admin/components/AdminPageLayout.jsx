import { AdminMainHeader } from "./AdminMainHeader.jsx";

export function AdminPageLayout({ nav, children }) {
  return (
    <div className="flex min-h-dvh w-full">
      <div className="w-full shrink-0 lg:fixed lg:inset-y-0 lg:left-0 lg:z-30 lg:w-64">{nav}</div>

      <div className="flex min-w-0 flex-1 flex-col lg:pl-64">
        <AdminMainHeader />
        <main className="flex-1 px-4 py-5 sm:px-6 lg:px-8">
          <div className="mx-auto w-full max-w-[1440px] space-y-6">{children}</div>
        </main>
      </div>
    </div>
  );
}
