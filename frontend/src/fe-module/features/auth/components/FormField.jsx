export function FormField({
  label,
  id,
  type = "text",
  value,
  onChange,
  placeholder,
  error,
  required = false,
}) {
  return (
    <div className="space-y-2">
      <label htmlFor={id} className="text-sm font-medium text-on-surface">
        {label}
      </label>
      <input
        id={id}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? `${id}-error` : undefined}
        className={[
          "w-full rounded-lg border bg-white px-3 py-2 text-sm outline-none transition",
          error
            ? "border-error focus:border-error"
            : "border-outline-variant focus:border-primary",
        ].join(" ")}
      />
      {error ? (
        <p id={`${id}-error`} className="text-xs text-error">
          {error}
        </p>
      ) : null}
    </div>
  );
}

