import { SORT_OPTIONS } from "../constants/productListConstants";

export function ProductListSortSelect({ value, onChange, disabled = false }) {
  return (
    <div className="flex items-center gap-2">
      <span className="text-label-sm text-on-surface-variant">Sắp xếp:</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        disabled={disabled}
        className="cursor-pointer border-none bg-transparent text-label-md font-medium text-primary focus:ring-0 disabled:cursor-not-allowed disabled:opacity-60"
        aria-label="Sắp xếp sản phẩm"
      >
        {SORT_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
}
