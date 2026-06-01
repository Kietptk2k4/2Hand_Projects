import { formatVndPrice } from "../../social/utils/formatPrice";
import { formatProductUpdatedAt } from "../utils/sellerProductMapper";
import { SellerProductRowActions } from "./SellerProductRowActions";
import { SellerProductStatusBadge } from "./SellerProductStatusBadge";

export function SellerProductTable({ items, disabled, onAction, onComingSoon }) {
  return (
    <div className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full text-left text-body-sm">
          <thead className="border-b border-outline-variant bg-surface-bright text-label-md text-on-surface-variant">
            <tr>
              <th className="px-4 py-3 font-medium">Sản phẩm</th>
              <th className="px-4 py-3 font-medium">Danh mục</th>
              <th className="px-4 py-3 font-medium">Giá bán</th>
              <th className="px-4 py-3 font-medium">Tồn kho</th>
              <th className="px-4 py-3 font-medium">Trạng thái</th>
              <th className="px-4 py-3 font-medium">Cập nhật</th>
              <th className="px-4 py-3 text-right font-medium">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant">
            {items.map((product) => {
              const lowStock =
                product.status === "ACTIVE" &&
                product.stockQuantity != null &&
                product.lowStockThreshold != null &&
                product.stockQuantity > 0 &&
                product.stockQuantity <= product.lowStockThreshold;

              return (
                <tr key={product.productId} className="hover:bg-surface-container-low/50">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      <div className="h-12 w-12 shrink-0 overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high">
                        {product.thumbnailUrl ? (
                          <img
                            src={product.thumbnailUrl}
                            alt=""
                            className="h-full w-full object-cover"
                            loading="lazy"
                          />
                        ) : (
                          <div className="flex h-full w-full items-center justify-center">
                            <span className="material-symbols-outlined text-outline" aria-hidden="true">
                              image
                            </span>
                          </div>
                        )}
                      </div>
                      <div className="min-w-0">
                        <p className="line-clamp-2 font-medium text-on-surface">{product.title}</p>
                        {product.skuCode ? (
                          <p className="text-label-sm text-on-surface-variant">{product.skuCode}</p>
                        ) : null}
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-on-surface-variant">{product.categoryName || "—"}</td>
                  <td className="px-4 py-3 font-medium text-on-surface">
                    {product.effectivePrice != null
                      ? formatVndPrice(product.effectivePrice)
                      : "Chưa thiết lập"}
                  </td>
                  <td className="px-4 py-3">
                    {product.stockQuantity != null ? (
                      <span
                        className={[
                          "inline-flex items-center gap-1",
                          lowStock ? "font-semibold text-error" : "text-on-surface",
                        ].join(" ")}
                      >
                        {lowStock ? (
                          <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
                            warning
                          </span>
                        ) : null}
                        {product.stockQuantity}
                      </span>
                    ) : (
                      <span className="text-on-surface-variant">—</span>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <SellerProductStatusBadge status={product.status} />
                  </td>
                  <td className="px-4 py-3 text-on-surface-variant">
                    {formatProductUpdatedAt(product.updatedAt)}
                  </td>
                  <td className="px-4 py-3">
                    <SellerProductRowActions
                      product={product}
                      disabled={disabled}
                      onAction={onAction}
                      onComingSoon={onComingSoon}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
