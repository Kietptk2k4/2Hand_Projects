export function RoleListTableSkeleton({ rows = 6, showCreatedColumn = true }) {
  const columnCount = showCreatedColumn ? 5 : 4;

  return (
    <div className="space-y-3">
      <div
        className="hidden gap-3 md:grid"
        style={{
          gridTemplateColumns: `minmax(0,1fr) minmax(0,1.4fr)${showCreatedColumn ? " minmax(0,1fr)" : ""} minmax(0,1fr) 5.5rem`,
        }}
      >
        {Array.from({ length: columnCount }, (_, index) => (
          <div key={`head-${index}`} className="h-3 animate-pulse rounded bg-admin-surface-muted" />
        ))}
      </div>

      {Array.from({ length: rows }, (_, index) => (
        <div
          key={index}
          className="hidden animate-pulse items-center gap-3 rounded-lg bg-admin-surface-muted/60 px-3 py-3.5 md:flex"
        >
          <div className="h-6 w-[4.5rem] rounded-md bg-admin-surface-muted" />
          <div className="h-4 flex-1 rounded bg-admin-surface-muted" />
          {showCreatedColumn ? (
            <div className="h-8 w-24 rounded bg-admin-surface-muted" />
          ) : null}
          <div className="h-8 w-24 rounded bg-admin-surface-muted" />
          <div className="h-9 w-[5.5rem] rounded-lg bg-admin-surface-muted" />
        </div>
      ))}

      {Array.from({ length: Math.min(rows, 3) }, (_, index) => (
        <div
          key={`mobile-${index}`}
          className="animate-pulse rounded-xl border border-admin-border bg-admin-surface p-4 md:hidden"
        >
          <div className="flex items-start justify-between gap-3">
            <div className="h-4 w-32 rounded bg-admin-surface-muted" />
            <div className="h-6 w-16 rounded-md bg-admin-surface-muted" />
          </div>
          <div className="mt-3 h-3 w-40 rounded bg-admin-surface-muted" />
          <div className="mt-4 h-11 w-full rounded-lg bg-admin-surface-muted" />
        </div>
      ))}
    </div>
  );
}
