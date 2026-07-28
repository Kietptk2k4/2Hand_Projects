import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { formatVndPrice } from "../../social/utils/formatPrice";
import { buildCommerceSellerProductEditPath } from "../utils/commerceRoutes";
import { formatProductUpdatedAt } from "../utils/sellerProductMapper";
import { SellerProductRowActions } from "./SellerProductRowActions";
import { SellerProductStatusBadge } from "./SellerProductStatusBadge";

// Curated high quality fallback images for seller product thumbnails when missing/broken
const FALLBACK_SELLER_IMAGES = [
  "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500&auto=format&fit=crop&q=80",
  "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=80",
];

function getRandomSellerFallback(productId) {
  if (!productId) return FALLBACK_SELLER_IMAGES[0];
  let hash = 0;
  const str = String(productId);
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  return FALLBACK_SELLER_IMAGES[Math.abs(hash) % FALLBACK_SELLER_IMAGES.length];
}

function ProductRowItem({ product, disabled, onAction, onEdit }) {
  const [imgError, setImgError] = useState(false);
  const navigate = useNavigate();

  const lowStock =
    product.status === "ACTIVE" &&
    product.stockQuantity != null &&
    product.lowStockThreshold != null &&
    product.stockQuantity > 0 &&
    product.stockQuantity <= product.lowStockThreshold;

  const editPath = buildCommerceSellerProductEditPath(product.productId);

  const openProduct = () => {
    if (!product.productId) return;
    navigate(editPath);
  };

  const handleRowKeyDown = (event) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openProduct();
    }
  };

  const displayImageSrc =
    !imgError && product.thumbnailUrl
      ? product.thumbnailUrl
      : getRandomSellerFallback(product.productId);

  return (
    <tr
      onClick={openProduct}
      onKeyDown={handleRowKeyDown}
      tabIndex={0}
      role="link"
      aria-label={`Xem chi tiết ${product.title}`}
      className="cursor-pointer transition-colors hover:bg-surface-container-low/60 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-inset"
    >
      {/* Product info (Image + Title + SKU) */}
      <td className="px-4 py-3.5">
        <div className="flex items-center gap-3">
          <div className="h-14 w-14 shrink-0 overflow-hidden rounded-xl border border-outline-variant/60 bg-surface-container-low">
            <img
              src={displayImageSrc}
              alt={product.title || "Sản phẩm"}
              onError={() => setImgError(true)}
              className="h-full w-full object-cover transition-transform duration-300 hover:scale-105"
              loading="lazy"
            />
          </div>
          <div className="min-w-0">
            <p className="line-clamp-2 text-xs font-bold text-on-surface sm:text-sm">
              {product.title}
            </p>
            {product.skuCode ? (
              <span className="mt-0.5 inline-block rounded-md bg-surface-container-high px-1.5 py-0.5 text-[10px] font-mono text-slate-500">
                {product.skuCode}
              </span>
            ) : null}
          </div>
        </div>
      </td>

      {/* Category */}
      <td className="px-4 py-3.5 text-xs font-semibold text-on-surface-variant">
        {product.categoryName || "—"}
      </td>

      {/* Price */}
      <td className="px-4 py-3.5 text-xs font-bold text-on-surface sm:text-sm">
        {product.effectivePrice != null ? (
          <span className="text-red-600 font-black">
            {formatVndPrice(product.effectivePrice)}
          </span>
        ) : (
          <span className="text-slate-400 font-normal italic">Chưa thiết lập</span>
        )}
      </td>

      {/* Stock */}
      <td className="px-4 py-3.5">
        {product.stockQuantity != null ? (
          <span
            className={[
              "inline-flex items-center gap-1 text-xs font-bold rounded-lg px-2 py-0.5",
              product.stockQuantity === 0
                ? "bg-rose-50 text-rose-700 border border-rose-200"
                : lowStock
                ? "bg-amber-50 text-amber-800 border border-amber-200"
                : "bg-emerald-50 text-emerald-800 border border-emerald-200",
            ].join(" ")}
          >
            {lowStock ? (
              <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
                warning
              </span>
            ) : null}
            {product.stockQuantity === 0 ? "Hết hàng (0)" : `Tồn kho (${product.stockQuantity})`}
          </span>
        ) : (
          <span className="text-slate-400">—</span>
        )}
      </td>

      {/* Status Badge */}
      <td className="px-4 py-3.5">
        <SellerProductStatusBadge status={product.status} />
      </td>

      {/* Updated Date */}
      <td className="px-4 py-3.5 text-xs text-slate-400 font-medium">
        {formatProductUpdatedAt(product.updatedAt)}
      </td>

      {/* Actions */}
      <td className="px-4 py-3.5 text-right" onClick={(event) => event.stopPropagation()}>
        <SellerProductRowActions
          product={product}
          disabled={disabled}
          onAction={onAction}
          onEdit={onEdit}
        />
      </td>
    </tr>
  );
}

export function SellerProductTable({ items, disabled, onAction, onEdit }) {
  return (
    <div className="overflow-hidden rounded-2xl border border-outline-variant bg-surface-container-lowest shadow-xs">
      <div className="overflow-x-auto">
        <table className="min-w-full text-left text-xs">
          <thead className="border-b border-outline-variant/60 bg-surface-container-low text-[11px] font-bold uppercase tracking-wider text-on-surface-variant">
            <tr>
              <th className="px-4 py-3.5">Sản phẩm</th>
              <th className="px-4 py-3.5">Danh mục</th>
              <th className="px-4 py-3.5">Giá bán</th>
              <th className="px-4 py-3.5">Tồn kho</th>
              <th className="px-4 py-3.5">Trạng thái</th>
              <th className="px-4 py-3.5">Cập nhật</th>
              <th className="px-4 py-3.5 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-outline-variant/40">
            {items.map((product) => (
              <ProductRowItem
                key={product.productId}
                product={product}
                disabled={disabled}
                onAction={onAction}
                onEdit={onEdit}
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
