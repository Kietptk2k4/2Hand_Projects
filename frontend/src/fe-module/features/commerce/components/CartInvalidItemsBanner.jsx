import { getCartValidateReasonLabel } from "../constants/cartConstants";

export function CartInvalidItemsBanner({ items = [] }) {
  const invalid = items.filter((item) => item.validateMessage || item.unavailableReason);
  if (!invalid.length) return null;

  return (
    <div
      className="mb-6 rounded-lg border border-error/25 bg-error-container/15 p-4"
      role="alert"
    >
      <p className="mb-2 text-sm font-medium text-on-surface">
        Một số sản phẩm không thể thanh toán:
      </p>
      <ul className="space-y-2">
        {invalid.map((item) => (
          <li key={item.cartItemId} className="flex items-start gap-2 text-sm text-on-surface-variant">
            <span
              className="material-symbols-outlined mt-0.5 shrink-0 text-error text-[18px]"
              aria-hidden="true"
            >
              error
            </span>
            <span>
              <span className="font-medium text-on-surface">{item.productName}</span>
              {" — "}
              {item.validateMessage ||
                getCartValidateReasonLabel(item.unavailableReason)}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
