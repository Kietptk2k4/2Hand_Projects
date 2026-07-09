export function AdminFilterBar({ children, actions, className = "", onSubmit }) {
  const content = (
    <div
      className={[
        "grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
      {actions ? (
        <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap md:col-span-2 lg:col-span-4">
          {actions}
        </div>
      ) : null}
    </div>
  );

  if (onSubmit) {
    return <form onSubmit={onSubmit}>{content}</form>;
  }

  return content;
}

export function AdminFilterField({ label, htmlFor, children, className = "" }) {
  return (
    <div className={["min-w-0", className].filter(Boolean).join(" ")}>
      <label
        htmlFor={htmlFor}
        className="mb-1.5 block text-xs font-medium text-admin-text-secondary"
      >
        {label}
      </label>
      {children}
    </div>
  );
}

export function AdminFilterInput({ id, className = "", ...props }) {
  return (
    <input
      id={id}
      className={[
        "w-full min-h-11 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text outline-none transition-colors placeholder:text-admin-text-muted focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      {...props}
    />
  );
}

export function AdminFilterSelect({ id, className = "", children, ...props }) {
  return (
    <select
      id={id}
      className={[
        "w-full min-h-11 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm text-admin-text outline-none transition-colors focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      {...props}
    >
      {children}
    </select>
  );
}

export function AdminFilterButton({ variant = "secondary", className = "", ...props }) {
  const variants = {
    primary:
      "bg-admin-accent text-white hover:bg-admin-accent-strong focus-visible:ring-admin-accent-soft",
    secondary:
      "border border-admin-border bg-admin-surface text-admin-text-secondary hover:bg-admin-surface-muted hover:text-admin-text focus-visible:ring-admin-accent-soft",
  };

  return (
    <button
      type={props.type ?? "button"}
      className={[
        "inline-flex min-h-11 w-full items-center justify-center rounded-lg px-4 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 sm:w-auto",
        variants[variant] ?? variants.secondary,
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      {...props}
    />
  );
}
