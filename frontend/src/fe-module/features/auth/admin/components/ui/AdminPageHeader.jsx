export function AdminPageHeader({ eyebrow, title, subtitle, actions, className = "" }) {
  return (
    <header
      className={[
        "mb-6 flex flex-col gap-4 sm:mb-7 sm:flex-row sm:items-start sm:justify-between",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <div className="min-w-0 flex-1">
        {eyebrow ? (
          <p className="mb-1 text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">{eyebrow}</p>
        ) : null}
        <h1 className="text-balance text-xl font-semibold tracking-tight text-admin-text sm:text-2xl">{title}</h1>
        {subtitle ? (
          <p className="mt-1.5 break-words text-sm text-admin-text-secondary">{subtitle}</p>
        ) : null}
      </div>
      {actions ? (
        <div className="flex w-full shrink-0 flex-wrap items-center gap-2 sm:w-auto sm:justify-end">{actions}</div>
      ) : null}
    </header>
  );
}
