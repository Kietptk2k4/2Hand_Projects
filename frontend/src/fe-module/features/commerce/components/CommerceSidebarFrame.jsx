export function CommerceSidebarFrame({ icon, title, subtitle, children }) {
  return (
    <aside className="flex h-full w-64 flex-col overflow-y-auto overscroll-contain border-r border-outline-variant bg-surface-container-lowest px-3 py-6">
      <div className="mb-8 px-3">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-lg bg-surface-container-high">
            <span className="material-symbols-outlined text-primary" aria-hidden="true">
              {icon}
            </span>
          </div>
          <div className="min-w-0">
            <h2 className="truncate text-headline-sm font-bold text-primary">{title}</h2>
            <p className="truncate text-label-sm text-on-surface-variant">{subtitle}</p>
          </div>
        </div>
      </div>
      <nav className="flex flex-1 flex-col gap-1">{children}</nav>
    </aside>
  );
}
