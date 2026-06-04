import { formatVndPrice } from "../utils/formatPrice";

function ProductThumbnail({ imageUrl, name }) {
  if (imageUrl) {
    return (
      <img
        src={imageUrl}
        alt=""
        className="h-12 w-12 shrink-0 rounded-lg object-cover"
        loading="lazy"
      />
    );
  }

  return (
    <div
      className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-surface-container-high"
      aria-hidden="true"
    >
      <span className="material-symbols-outlined text-outline">inventory_2</span>
    </div>
  );
}

function CompactStrip({ tags, onViewProduct }) {
  const count = tags.length;
  const first = tags[0];
  const label =
    count === 1
      ? first.name
      : `${count} sản phẩm`;
  const priceLabel =
    count === 1
      ? formatVndPrice(first.price)
      : `từ ${formatVndPrice(first.price)}`;

  return (
    <div
      className="flex items-center justify-between gap-3 rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2"
      onClick={(event) => event.stopPropagation()}
      onKeyDown={(event) => event.stopPropagation()}
      role="presentation"
    >
      <div className="flex min-w-0 flex-1 items-center gap-2">
        <ProductThumbnail imageUrl={first.imageUrl} name={first.name} />
        <div className="min-w-0">
          <p className="truncate text-sm font-medium text-on-surface">{label}</p>
          <p className="text-xs text-on-surface-variant">{priceLabel}</p>
        </div>
      </div>
      <button
        type="button"
        onClick={(event) => {
          event.stopPropagation();
          onViewProduct?.(first.productId);
        }}
        className="shrink-0 rounded-lg bg-primary px-3 py-1.5 text-xs font-medium text-on-primary hover:bg-[#0050cb]"
      >
        Xem
      </button>
    </div>
  );
}

function DetailList({ tags, onViewProduct }) {
  return (
    <ul className="max-h-80 space-y-2 overflow-y-auto">
      {tags.map((tag) => (
        <li
          key={tag.productId}
          className="flex items-center justify-between gap-3 rounded-lg border border-outline-variant bg-surface-container-low p-3 shadow-sm"
        >
          <div className="flex min-w-0 flex-1 items-center gap-3">
            <ProductThumbnail imageUrl={tag.imageUrl} name={tag.name} />
            <div className="min-w-0">
              <h3 className="truncate text-sm font-bold text-on-surface">{tag.name}</h3>
              {tag.category ? (
                <p className="text-xs text-on-surface-variant">{tag.category}</p>
              ) : null}
              <p className="text-sm font-medium text-primary">{formatVndPrice(tag.price)}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={(event) => {
              event.stopPropagation();
              onViewProduct?.(tag.productId);
            }}
            className="shrink-0 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary shadow-sm hover:bg-[#0050cb]"
          >
            Xem sản phẩm
          </button>
        </li>
      ))}
    </ul>
  );
}

export function PostProductTagsBlock({ tags = [], variant = "compact", onViewProduct }) {
  if (!tags.length) return null;

  if (variant === "detail") {
    return (
      <div className="mb-6" onClick={(event) => event.stopPropagation()} role="presentation">
        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
          Sản phẩm gắn kèm
        </p>
        <DetailList tags={tags} onViewProduct={onViewProduct} />
      </div>
    );
  }

  return (
    <div className="mt-4" onClick={(event) => event.stopPropagation()} role="presentation">
      <CompactStrip tags={tags} onViewProduct={onViewProduct} />
    </div>
  );
}
