import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  buildCommerceCategoryPath,
  buildCommerceShopPath,
} from "../../commerce/utils/commerceRoutes";
import { ProductTagUnavailableModal } from "./ProductTagUnavailableModal";
import { formatVndPrice } from "../utils/formatPrice";

function handleViewProduct(tag, onViewProduct, showUnavailable) {
  if (!tag?.productId) return;
  if (tag.available === false) {
    showUnavailable();
    return;
  }
  onViewProduct?.(tag.productId);
}

function ProductThumbnail({ imageUrl, name }) {
  if (imageUrl) {
    return (
      <img
        src={imageUrl}
        alt=""
        className="h-12 w-12 shrink-0 rounded-xl border border-outline-variant/30 object-cover"
        loading="lazy"
      />
    );
  }

  return (
    <div
      className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl border border-outline-variant/30 bg-surface-container-high"
      aria-hidden="true"
    >
      <span className="material-symbols-outlined text-outline">inventory_2</span>
    </div>
  );
}

function TagMetaLinks({ tag, onViewCategory, onViewShop }) {
  const hasCategoryLink = Boolean(tag.categoryId);
  const hasShopLink = Boolean(tag.shopId);

  if (!hasCategoryLink && !hasShopLink) {
    if (tag.category) {
      return <p className="text-xs text-on-surface-variant">{tag.category}</p>;
    }
    return null;
  }

  return (
    <p className="flex flex-wrap items-center gap-x-1.5 gap-y-0.5 text-xs text-on-surface-variant">
      {hasCategoryLink ? (
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation();
            onViewCategory?.(tag.categoryId);
          }}
          className="font-semibold text-sky-500 hover:underline"
        >
          {tag.category || "Danh mục"}
        </button>
      ) : tag.category ? (
        <span>{tag.category}</span>
      ) : null}
      {hasCategoryLink && hasShopLink ? <span aria-hidden="true">·</span> : null}
      {hasShopLink ? (
        <button
          type="button"
          onClick={(event) => {
            event.stopPropagation();
            onViewShop?.(tag.shopId);
          }}
          className="font-semibold text-sky-500 hover:underline"
        >
          Xem shop
        </button>
      ) : null}
    </p>
  );
}

function CompactStrip({ tags, onViewProduct, onViewCategory, onViewShop, showUnavailable }) {
  const count = tags.length;
  const first = tags[0];
  const label = count === 1 ? first.name : `${count} sản phẩm gắn kèm`;
  const priceLabel = count === 1 ? formatVndPrice(first.price) : `từ ${formatVndPrice(first.price)}`;

  return (
    <div
      className="flex items-center justify-between gap-3 rounded-2xl border border-outline-variant/40 bg-surface-container-low/30 p-2.5 transition-all hover:bg-surface-container-low/70 hover:border-outline-variant/60"
      onClick={(event) => event.stopPropagation()}
      onKeyDown={(event) => event.stopPropagation()}
      role="presentation"
    >
      <div className="flex min-w-0 flex-1 items-center gap-3">
        <ProductThumbnail imageUrl={first.imageUrl} name={first.name} />
        <div className="min-w-0">
          <p className="truncate text-xs font-bold text-on-surface">{label}</p>
          <p className="mt-0.5 text-xs font-extrabold text-on-surface">{priceLabel}</p>
        </div>
      </div>
      <button
        type="button"
        onClick={(event) => {
          event.stopPropagation();
          handleViewProduct(first, onViewProduct, showUnavailable);
        }}
        className="shrink-0 rounded-full bg-zinc-900 px-4 py-1.5 text-xs font-bold text-white shadow-xs transition-all hover:bg-zinc-800 active:scale-95"
      >
        Xem sản phẩm
      </button>
    </div>
  );
}

function DetailList({ tags, onViewProduct, onViewCategory, onViewShop, showUnavailable }) {
  return (
    <ul className="max-h-80 space-y-2.5 overflow-y-auto">
      {tags.map((tag) => (
        <li
          key={tag.productId}
          className="flex items-center justify-between gap-3 rounded-2xl border border-outline-variant/40 bg-surface-container-lowest p-3 shadow-xs transition-all hover:border-outline-variant/70"
        >
          <div className="flex min-w-0 flex-1 items-center gap-3">
            <ProductThumbnail imageUrl={tag.imageUrl} name={tag.name} />
            <div className="min-w-0">
              <h3 className="truncate text-sm font-bold text-on-surface">{tag.name}</h3>
              <TagMetaLinks
                tag={tag}
                onViewCategory={onViewCategory}
                onViewShop={onViewShop}
              />
              <p className="mt-0.5 text-sm font-extrabold text-on-surface">{formatVndPrice(tag.price)}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={(event) => {
              event.stopPropagation();
              handleViewProduct(tag, onViewProduct, showUnavailable);
            }}
            className="shrink-0 rounded-full bg-zinc-900 px-4 py-1.5 text-xs font-bold text-white shadow-xs transition-all hover:bg-zinc-800 active:scale-95"
          >
            Xem sản phẩm
          </button>
        </li>
      ))}
    </ul>
  );
}

export function PostProductTagsBlock({ tags = [], variant = "compact", onViewProduct }) {
  const navigate = useNavigate();
  const [unavailableOpen, setUnavailableOpen] = useState(false);
  const showUnavailable = useCallback(() => setUnavailableOpen(true), []);
  const closeUnavailable = useCallback(() => setUnavailableOpen(false), []);

  const onViewCategory = useCallback(
    (categoryId) => {
      if (!categoryId) return;
      navigate(buildCommerceCategoryPath(categoryId));
    },
    [navigate]
  );

  const onViewShop = useCallback(
    (shopId) => {
      if (!shopId) return;
      navigate(buildCommerceShopPath(shopId));
    },
    [navigate]
  );

  if (!tags.length) return null;

  if (variant === "detail") {
    return (
      <>
        <div className="mb-6" onClick={(event) => event.stopPropagation()} role="presentation">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
            Sản phẩm gắn kèm
          </p>
          <DetailList
            tags={tags}
            onViewProduct={onViewProduct}
            onViewCategory={onViewCategory}
            onViewShop={onViewShop}
            showUnavailable={showUnavailable}
          />
        </div>
        <ProductTagUnavailableModal isOpen={unavailableOpen} onClose={closeUnavailable} />
      </>
    );
  }

  return (
    <>
      <div className="mt-4" onClick={(event) => event.stopPropagation()} role="presentation">
        <CompactStrip
          tags={tags}
          onViewProduct={onViewProduct}
          onViewCategory={onViewCategory}
          onViewShop={onViewShop}
          showUnavailable={showUnavailable}
        />
      </div>
      <ProductTagUnavailableModal isOpen={unavailableOpen} onClose={closeUnavailable} />
    </>
  );
}
