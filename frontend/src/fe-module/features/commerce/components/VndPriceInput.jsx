import { formatVndInputValue } from "../utils/vndInputFormat";

export function VndPriceInput({
  id,
  value,
  onChange,
  disabled,
  className,
  placeholder,
}) {
  return (
    <input
      id={id}
      type="text"
      inputMode="numeric"
      autoComplete="off"
      className={className}
      placeholder={placeholder}
      value={value ?? ""}
      disabled={disabled}
      onChange={(e) => onChange(formatVndInputValue(e.target.value))}
    />
  );
}
