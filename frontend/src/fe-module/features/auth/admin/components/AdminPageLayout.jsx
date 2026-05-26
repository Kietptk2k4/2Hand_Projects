export function AdminPageLayout({ nav, children }) {
  return (
    <div className="flex w-full flex-col gap-6 lg:flex-row lg:items-start">
      {nav}
      <div className="min-w-0 flex-1 space-y-6">{children}</div>
    </div>
  );
}
