export function AuthAlert({ variant = "info", title, message, onDismiss }) {
  const styles =
    variant === "success"
      ? "border-primary/30 bg-account-surface-low text-on-surface"
      : variant === "error"
        ? "border-error/40 bg-error-container text-on-error-container"
        : "border-outline-variant bg-surface-container text-on-surface";

  return (
    <div className={`rounded-lg border px-4 py-3 text-sm ${styles}`} role="alert">
      <div className="flex items-start justify-between gap-3">
        <div>
          {title ? <p className="font-semibold">{title}</p> : null}
          {message ? <p className={title ? "mt-1" : ""}>{message}</p> : null}
        </div>
        {onDismiss ? (
          <button
            type="button"
            onClick={onDismiss}
            className="shrink-0 text-on-surface-variant hover:text-on-surface"
            aria-label="Dong thong bao"
          >
            ×
          </button>
        ) : null}
      </div>
    </div>
  );
}

export function TabPanelHeader({ title, subtitle }) {
  return (
    <header className="mb-8">
      <h1 className="text-2xl font-semibold text-on-surface md:text-3xl">{title}</h1>
      {subtitle ? <p className="mt-2 text-base text-on-surface-variant">{subtitle}</p> : null}
    </header>
  );
}

export function AccountCard({ children, className = "" }) {
  return (
    <div
      className={[
        "rounded-xl border border-outline-variant bg-white p-6 shadow-[0_4px_6px_-1px_rgba(0,0,0,0.08)]",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
    </div>
  );
}

export function AccountFieldLabel({ htmlFor, children, required }) {
  return (
    <label htmlFor={htmlFor} className="text-xs font-semibold text-on-surface">
      {children}
      {required ? <span className="text-error"> *</span> : null}
    </label>
  );
}

export function AccountTextInput({
  id,
  name,
  value,
  onChange,
  onBlur,
  type = "text",
  placeholder,
  error,
  disabled,
  maxLength,
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <input
        id={id}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        placeholder={placeholder}
        disabled={disabled}
        maxLength={maxLength}
        aria-invalid={Boolean(error)}
        className={[
          "w-full rounded-lg border bg-white px-3 py-2.5 text-base outline-none transition",
          error
            ? "border-error focus:border-error focus:ring-1 focus:ring-error"
            : "border-outline-variant focus:border-primary focus:ring-1 focus:ring-primary/30",
          disabled ? "cursor-not-allowed opacity-60" : "",
        ].join(" ")}
      />
      {error ? <p className="text-sm text-error">{error}</p> : null}
    </div>
  );
}

export function AccountSkeleton() {
  return (
    <div className="animate-pulse space-y-6" aria-busy="true" aria-label="Dang tai">
      <div className="h-8 w-48 rounded bg-outline-variant/40" />
      <div className="h-4 w-72 max-w-full rounded bg-outline-variant/30" />
      <div className="space-y-4 rounded-xl border border-outline-variant/40 bg-white p-6">
        <div className="h-4 w-full rounded bg-outline-variant/30" />
        <div className="h-4 w-5/6 rounded bg-outline-variant/30" />
        <div className="h-4 w-2/3 rounded bg-outline-variant/30" />
        <div className="h-10 w-full rounded-lg bg-outline-variant/30" />
        <div className="h-10 w-full rounded-lg bg-outline-variant/30" />
      </div>
    </div>
  );
}

export function PrimaryButton({ children, disabled, loading, type = "button", onClick, className = "" }) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={[
        "inline-flex min-w-[140px] items-center justify-center gap-2 rounded-lg bg-primary-container px-6 py-2.5 text-sm font-medium text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {loading ? (
        <>
          <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
          <span>Dang xu ly...</span>
        </>
      ) : (
        children
      )}
    </button>
  );
}

export function SecondaryButton({ children, disabled, type = "button", onClick, className = "" }) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={[
        "rounded-lg border-2 border-outline-variant px-6 py-2.5 text-sm font-medium text-on-surface transition hover:bg-account-surface-low disabled:cursor-not-allowed disabled:opacity-60",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
    </button>
  );
}
