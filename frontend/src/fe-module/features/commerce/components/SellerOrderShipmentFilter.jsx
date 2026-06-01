import { SHIPMENT_FILTER_OPTIONS } from "../constants/sellerOrderConstants";

export function SellerOrderShipmentFilter({ value, onChange, disabled }) {
  return (
    <div className="mb-4 flex flex-wrap items-center gap-2">
      <label htmlFor="shipment-filter" className="text-label-md text-on-surface-variant">
        Lọc vận chuyển:
      </label>
      <select
        id="shipment-filter"
        value={value || ""}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value || null)}
        className="rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-body-sm text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
      >
        {SHIPMENT_FILTER_OPTIONS.map((opt) => (
          <option key={opt.value || "all"} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}
