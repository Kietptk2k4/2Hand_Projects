export function AdminDataTable({
  children,
  minWidth = "880px",
  tableLayout,
  className = "",
  ariaLabel,
}) {
  const resolvedMinWidth = minWidth && minWidth !== "0" ? minWidth : undefined;
  const layoutClass =
    tableLayout === "auto"
      ? "table-auto"
      : tableLayout === "fixed"
        ? "table-fixed"
        : resolvedMinWidth
          ? ""
          : "table-fixed";

  return (
    <div
      className={[
        "w-full max-w-full overflow-x-auto overscroll-x-contain [-webkit-overflow-scrolling:touch] [scrollbar-width:thin]",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <table
        className={[
          "hidden w-full min-w-full text-left text-sm md:table",
          layoutClass,
        ]
          .filter(Boolean)
          .join(" ")}
        style={resolvedMinWidth ? { minWidth: resolvedMinWidth } : undefined}
        aria-label={ariaLabel}
      >
        {children}
      </table>
    </div>
  );
}

export function AdminDataTableHead({ children, sticky = true }) {
  return (
    <thead
      className={[
        "border-b border-admin-border text-admin-text-muted",
        sticky ? "sticky top-0 z-10 bg-admin-surface" : "",
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
    </thead>
  );
}

export function AdminDataTableBody({ children }) {
  return <tbody>{children}</tbody>;
}

export function AdminDataTableRow({
  children,
  className = "",
  onClick,
  isSelected,
  tabIndex,
  ...props
}) {
  const resolvedTabIndex =
    tabIndex !== undefined
      ? tabIndex
      : onClick
        ? isSelected
          ? 0
          : -1
        : undefined;

  return (
    <tr
      className={[
        "border-b border-admin-border-subtle align-top transition-colors",
        onClick
          ? "cursor-pointer hover:bg-admin-surface-muted focus-visible:outline-none focus-visible:bg-admin-accent-soft/30"
          : "",
        isSelected
          ? "bg-admin-accent-soft/40 [box-shadow:inset_3px_0_0_0_var(--color-admin-accent)]"
          : "",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      onClick={onClick}
      aria-selected={isSelected ? "true" : undefined}
      tabIndex={resolvedTabIndex}
      onKeyDown={
        onClick
          ? (event) => {
              if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                onClick(event);
              }
            }
          : undefined
      }
      {...props}
    >
      {children}
    </tr>
  );
}

export function AdminDataTableCell({ children, className = "", header = false, ...props }) {
  const Tag = header ? "th" : "td";
  return (
    <Tag
      className={[
        header ? "py-2.5 pr-3 font-medium" : "py-3 pr-3 text-admin-text",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      {...props}
    >
      {children}
    </Tag>
  );
}

export function AdminMobileCardList({ children, className = "" }) {
  return (
    <div className={["grid gap-3 md:hidden", className].filter(Boolean).join(" ")}>{children}</div>
  );
}

export function AdminMobileCard({
  children,
  className = "",
  onClick,
  isSelected,
  ariaLabel,
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={ariaLabel}
      aria-current={isSelected ? "true" : undefined}
      className={[
        "w-full rounded-xl border border-admin-border bg-admin-surface p-4 text-left shadow-[var(--shadow-admin-surface)] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent focus-visible:ring-offset-2 focus-visible:ring-offset-admin-canvas",
        isSelected ? "border-admin-accent-border bg-admin-accent-soft/30" : "hover:bg-admin-surface-muted",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
    </button>
  );
}
