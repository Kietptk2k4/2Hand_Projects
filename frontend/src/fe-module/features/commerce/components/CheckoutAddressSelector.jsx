import { formatAddressHeader, formatAddressLine } from "../utils/formatAddressLine";

export function CheckoutAddressSelector({
  addresses,
  selectedAddressId,
  onSelect,
  onAddNew,
}) {
  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <div className="mb-4 flex items-center justify-between gap-2">
        <h2 className="text-headline-sm font-semibold text-on-surface">Địa chỉ giao hàng</h2>
        <button
          type="button"
          onClick={onAddNew}
          className="text-sm font-medium text-primary hover:underline"
        >
          Thêm địa chỉ mới
        </button>
      </div>

      {addresses.length === 0 ? (
        <p className="text-sm text-on-surface-variant">
          Bạn chưa có địa chỉ giao hàng. Vui lòng thêm địa chỉ để tiếp tục.
        </p>
      ) : (
        <div className="flex flex-col gap-3">
          {addresses.map((address) => {
            const selected = address.id === selectedAddressId;
            return (
              <label
                key={address.id}
                className={[
                  "flex cursor-pointer gap-3 rounded-lg border p-4 transition-colors",
                  selected
                    ? "border-primary bg-surface-container-low"
                    : "border-outline-variant hover:border-primary/50",
                ].join(" ")}
              >
                <input
                  type="radio"
                  name="checkout-address"
                  checked={selected}
                  onChange={() => onSelect?.(address.id)}
                  className="mt-1 h-4 w-4 border-outline text-primary focus:ring-primary"
                />
                <div className="min-w-0 flex-1">
                  <p className="font-medium text-on-surface">{formatAddressHeader(address)}</p>
                  <p className="mt-1 text-sm text-on-surface-variant">
                    {formatAddressLine(address)}
                  </p>
                  {address.isDefault ? (
                    <span className="mt-2 inline-block rounded-full bg-primary-container/20 px-2 py-0.5 text-xs font-medium text-primary">
                      Mặc định
                    </span>
                  ) : null}
                </div>
              </label>
            );
          })}
        </div>
      )}
    </section>
  );
}
