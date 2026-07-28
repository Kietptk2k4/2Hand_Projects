import { useState } from "react";
import { getCartValidateReasonLabel } from "../constants/cartConstants";

export function CartInvalidItemsBanner({ items = [], onRemoveInvalidItems }) {
  const [collapsed, setCollapsed] = useState(false);
  const invalid = items.filter((item) => item.validateMessage || item.unavailableReason);
  
  if (!invalid.length) return null;

  return (
    <div
      className="mb-6 overflow-hidden rounded-2xl border border-amber-200 bg-gradient-to-r from-amber-50 to-orange-50 p-4 shadow-xs"
      role="alert"
    >
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="material-symbols-outlined text-amber-600 text-xl" aria-hidden="true">
            warning
          </span>
          <p className="text-xs font-bold text-amber-900">
            Có {invalid.length} sản phẩm tạm thời không khả dụng trong giỏ hàng
          </p>
        </div>

        <div className="flex items-center gap-3">
          {onRemoveInvalidItems ? (
            <button
              type="button"
              onClick={onRemoveInvalidItems}
              className="text-xs font-bold text-amber-800 underline hover:text-amber-950 cursor-pointer"
            >
              Bỏ sản phẩm lỗi
            </button>
          ) : null}

          <button
            type="button"
            onClick={() => setCollapsed((prev) => !prev)}
            className="flex items-center gap-1 text-xs font-semibold text-amber-800 hover:text-amber-950 cursor-pointer"
          >
            <span>{collapsed ? "Xem danh sách" : "Thu gọn"}</span>
            <span className="material-symbols-outlined text-sm">
              {collapsed ? "expand_more" : "expand_less"}
            </span>
          </button>
        </div>
      </div>

      {!collapsed ? (
        <ul className="mt-3 space-y-1.5 border-t border-amber-200/80 pt-3">
          {invalid.map((item) => (
            <li key={item.cartItemId} className="flex items-center gap-2 text-xs text-amber-900/80">
              <span className="h-1.5 w-1.5 rounded-full bg-amber-500" />
              <span className="font-bold text-amber-950 line-clamp-1 max-w-xs sm:max-w-md">
                {item.productName}
              </span>
              <span>—</span>
              <span className="text-amber-800 font-medium">
                {item.validateMessage || getCartValidateReasonLabel(item.unavailableReason)}
              </span>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
