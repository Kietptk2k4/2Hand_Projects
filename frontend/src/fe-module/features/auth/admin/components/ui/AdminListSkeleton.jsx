export function AdminListSkeleton({ rows = 6, className = "" }) {
  return (
    <div className={["space-y-3", className].filter(Boolean).join(" ")}>
      <div className="hidden gap-3 md:grid md:grid-cols-[minmax(0,1.2fr)_minmax(0,1.5fr)_minmax(0,1fr)_minmax(0,1fr)_5rem]">
        {Array.from({ length: 5 }, (_, index) => (
          <div key={`head-${index}`} className="h-3 animate-pulse rounded bg-admin-surface-muted" />
        ))}
      </div>
      {Array.from({ length: rows }, (_, index) => (
        <div
          key={index}
          className="flex animate-pulse items-center gap-3 rounded-lg bg-admin-surface-muted/60 px-3 py-3.5"
        >
          <div className="h-6 w-20 rounded-md bg-admin-surface-muted" />
          <div className="hidden h-4 flex-1 rounded bg-admin-surface-muted md:block" />
          <div className="hidden h-4 w-24 rounded bg-admin-surface-muted md:block" />
          <div className="hidden h-4 w-24 rounded bg-admin-surface-muted md:block" />
          <div className="ml-auto h-8 w-16 rounded-lg bg-admin-surface-muted" />
        </div>
      ))}
    </div>
  );
}
