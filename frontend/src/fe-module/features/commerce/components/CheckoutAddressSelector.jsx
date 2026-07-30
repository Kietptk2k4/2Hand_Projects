import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { formatAddressHeader, formatAddressLine } from "../utils/formatAddressLine";

export function CheckoutAddressSelector({
  addresses,
  selectedAddressId,
  onSelect,
  onAddNew,
}) {
  return (
    <section className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-6 shadow-xs sm:p-7">
      <div className="mb-5 flex flex-wrap items-center justify-between gap-3 border-b border-outline-variant/60 pb-3.5">
        <h2 className="text-base font-black text-on-surface sm:text-lg flex items-center gap-2">
          <span className="material-symbols-outlined text-primary text-xl" aria-hidden="true">
            location_on
          </span>
          ĐỊA CHỈ GIAO HÀNG
        </h2>
        <div className="flex flex-wrap items-center gap-3">
          <Link
            to={APP_ROUTES.commerceAddresses}
            className="text-xs font-bold text-on-surface-variant hover:text-primary hover:underline transition-colors"
          >
            Quản lý địa chỉ
          </Link>
          <button
            type="button"
            onClick={onAddNew}
            className="text-xs font-bold text-primary hover:underline cursor-pointer"
          >
            + Thêm địa chỉ mới
          </button>
        </div>
      </div>

      {addresses.length === 0 ? (
        <p className="text-xs font-semibold text-on-surface-variant">
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
                  "flex cursor-pointer items-start gap-3.5 rounded-2xl border p-4 transition-all shadow-xs",
                  selected
                    ? "border-primary bg-primary/5 ring-2 ring-primary/20"
                    : "border-outline-variant/80 bg-surface-container-lowest hover:bg-surface-container-low/50",
                ].join(" ")}
              >
                <input
                  type="radio"
                  name="checkout-address"
                  checked={selected}
                  onChange={() => onSelect?.(address.id)}
                  className="mt-1 h-4 w-4 border-outline text-primary focus:ring-primary cursor-pointer"
                />
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-xs font-bold text-on-surface sm:text-sm">
                      {formatAddressHeader(address)}
                    </p>
                    {address.isDefault ? (
                      <span className="rounded-md bg-emerald-50 px-2 py-0.5 text-[10px] font-bold text-emerald-700 border border-emerald-200">
                        Mặc định
                      </span>
                    ) : null}
                  </div>
                  <p className="mt-1 text-xs font-medium text-on-surface-variant">
                    {formatAddressLine(address)}
                  </p>
                </div>
              </label>
            );
          })}
        </div>
      )}
    </section>
  );
}
