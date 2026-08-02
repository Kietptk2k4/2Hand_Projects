export function AuthAlert({ variant = "info", title, message, onDismiss }) {
  const styles =
    variant === "success"
      ? "border-emerald-500/30 bg-emerald-500/10 text-on-surface"
      : variant === "error"
        ? "border-error/40 bg-error-container/40 text-on-error-container"
        : "border-sky-500/30 bg-sky-500/10 text-on-surface";

  return (
    <div className={`rounded-2xl border px-4 py-3 text-sm ${styles}`} role="alert">
      <div className="flex items-start justify-between gap-3">
        <div>
          {title ? <p className="font-bold">{title}</p> : null}
          {message ? <p className={title ? "mt-0.5 text-xs" : ""}>{message}</p> : null}
        </div>
        {onDismiss ? (
          <button
            type="button"
            onClick={onDismiss}
            className="shrink-0 text-on-surface-variant hover:text-on-surface"
            aria-label="Đóng thông báo"
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
    <header className="mb-6">
      <h1 className="text-2xl font-extrabold text-on-surface md:text-3xl leading-tight">{title}</h1>
      {subtitle ? <p className="mt-1 text-sm text-on-surface-variant/70">{subtitle}</p> : null}
    </header>
  );
}

export function AccountCard({ children, className = "" }) {
  return (
    <div
      className={[
        "rounded-2xl border border-outline-variant/40 bg-surface-container-lowest p-6 transition-colors shadow-2xs",
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
    <label htmlFor={htmlFor} className="text-xs font-bold text-on-surface">
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
        value={value ?? ""}
        onChange={onChange}
        onBlur={onBlur}
        placeholder={placeholder}
        disabled={disabled}
        maxLength={maxLength}
        aria-invalid={Boolean(error)}
        className={[
          "w-full rounded-xl border bg-surface-container-lowest px-3.5 py-2.5 text-sm text-on-surface outline-none transition placeholder:text-on-surface-variant/50",
          error
            ? "border-error focus:border-error focus:ring-1 focus:ring-error"
            : "border-outline-variant/50 focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30",
          disabled ? "cursor-not-allowed opacity-60 bg-surface-container-low" : "",
        ].join(" ")}
      />
      {error ? <p className="text-xs text-error font-medium">{error}</p> : null}
    </div>
  );
}

export function AccountSkeleton() {
  return (
    <div className="animate-pulse space-y-6" aria-busy="true" aria-label="Đang tải">
      <div className="h-8 w-48 rounded-lg bg-outline-variant/30" />
      <div className="h-4 w-72 max-w-full rounded-lg bg-outline-variant/20" />
      <div className="space-y-4 rounded-2xl border border-outline-variant/40 bg-surface-container-lowest p-6">
        <div className="h-4 w-full rounded-md bg-outline-variant/20" />
        <div className="h-4 w-5/6 rounded-md bg-outline-variant/20" />
        <div className="h-4 w-2/3 rounded-md bg-outline-variant/20" />
        <div className="h-10 w-full rounded-xl bg-outline-variant/20" />
        <div className="h-10 w-full rounded-xl bg-outline-variant/20" />
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
        "inline-flex min-w-[130px] items-center justify-center gap-2 rounded-full bg-zinc-900 px-6 py-2.5 text-sm font-bold text-white shadow-2xs transition-all hover:bg-zinc-800 active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-50",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {loading ? (
        <>
          <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
          <span>Đang xử lý...</span>
        </>
      ) : (
        children
      )}
    </button>
  );
}

export function MaterialIcon({ name, className = "", filled = false }) {
  return (
    <span
      className={["material-symbols-outlined", className].filter(Boolean).join(" ")}
      style={filled ? { fontVariationSettings: "'FILL' 1" } : undefined}
      aria-hidden="true"
    >
      {name}
    </span>
  );
}

export function PasswordField({
  id,
  name,
  label,
  value,
  onChange,
  onBlur,
  placeholder,
  error,
  visible,
  onToggleVisible,
  disabled,
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-xs font-bold text-on-surface">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          name={name}
          type={visible ? "text" : "password"}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          placeholder={placeholder}
          disabled={disabled}
          autoComplete={name === "current_password" ? "current-password" : "new-password"}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
          className={[
            "w-full rounded-xl border bg-surface-container-lowest px-3.5 py-2.5 pr-12 text-sm text-on-surface outline-none transition placeholder:text-on-surface-variant/50",
            error
              ? "border-error focus:border-error focus:ring-1 focus:ring-error"
              : "border-outline-variant/50 focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30",
            disabled ? "cursor-not-allowed opacity-70 bg-surface-container-low" : "",
          ].join(" ")}
        />
        <button
          type="button"
          onClick={onToggleVisible}
          disabled={disabled}
          className="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-on-surface-variant/70 hover:text-on-surface disabled:cursor-not-allowed"
          aria-label={visible ? `Ẩn ${label}` : `Hiện ${label}`}
        >
          <MaterialIcon name={visible ? "visibility" : "visibility_off"} className="text-[20px]" />
        </button>
      </div>
      {error ? (
        <p id={`${id}-error`} className="text-xs text-error font-medium">
          {error}
        </p>
      ) : null}
    </div>
  );
}

const PASSWORD_CHECKLIST_ITEMS = [
  { key: "length", label: "8-32 ký tự" },
  { key: "uppercase", label: "1 chữ hoa" },
  { key: "lowercase", label: "1 chữ thường" },
  { key: "number", label: "1 chữ số" },
];

export function PasswordChecklist({ checklistState }) {
  return (
    <div className="mt-2 rounded-xl bg-surface-container-low/60 p-3">
      <ul className="flex flex-col gap-2 text-xs font-semibold">
        {PASSWORD_CHECKLIST_ITEMS.map((item) => {
          const met = Boolean(checklistState[item.key]);
          return (
            <li
              key={item.key}
              className={["flex items-center gap-2", met ? "text-sky-500" : "text-on-surface-variant/70"].join(" ")}
            >
              <MaterialIcon
                name={met ? "check_circle" : "radio_button_unchecked"}
                className="text-base"
                filled={met}
              />
              <span>{item.label}</span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

export function SecondaryButton({ children, disabled, type = "button", onClick, className = "" }) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={[
        "rounded-full border border-outline-variant/60 bg-surface-container-lowest px-6 py-2.5 text-sm font-bold text-on-surface shadow-2xs transition-all hover:bg-surface-container-low active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-50",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {children}
    </button>
  );
}

