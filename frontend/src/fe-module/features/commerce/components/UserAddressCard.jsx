import { formatAddressHeader, formatAddressLine } from "../utils/formatAddressLine";

export function UserAddressCard({
  address,
  disabled,
  isMutating,
  onEdit,
  onSetDefault,
  onDelete,
}) {
  const isDefault = address.isDefault;

  return (
    <article
      className={[
        "rounded-xl border bg-surface-container-lowest p-4 shadow-sm md:p-5",
        isDefault ? "border-primary" : "border-outline-variant",
      ].join(" ")}
    >
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <p className="font-medium text-on-surface">{formatAddressHeader(address)}</p>
            {isDefault ? (
              <span className="rounded-full bg-primary/10 px-2.5 py-0.5 text-label-sm font-medium text-primary">
                Mặc định
              </span>
            ) : null}
          </div>
          <p className="mt-2 text-body-sm text-on-surface-variant">{formatAddressLine(address)}</p>
        </div>

        <div className="flex flex-wrap gap-2 sm:shrink-0">
          <button
            type="button"
            onClick={() => onEdit?.(address)}
            disabled={disabled || isMutating}
            className="rounded-lg border border-outline-variant px-3 py-2 text-label-md text-on-surface hover:bg-surface-container-low disabled:opacity-50"
          >
            Sửa
          </button>
          {!isDefault ? (
            <button
              type="button"
              onClick={() => onSetDefault?.(address)}
              disabled={disabled || isMutating}
              className="rounded-lg border border-primary px-3 py-2 text-label-md font-medium text-primary hover:bg-surface-container-low disabled:opacity-50"
            >
              Đặt mặc định
            </button>
          ) : null}
          <button
            type="button"
            onClick={() => onDelete?.(address)}
            disabled={disabled || isMutating}
            className="rounded-lg border border-error/40 px-3 py-2 text-label-md text-error hover:bg-error-container/30 disabled:opacity-50"
          >
            Xóa
          </button>
        </div>
      </div>
    </article>
  );
}
