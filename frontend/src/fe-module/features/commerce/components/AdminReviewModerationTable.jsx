import { formatShortOrderId } from "../utils/formatOrderDate";
import { getBuyerInitials } from "../utils/buyerInitials";
import { AdminReviewStatusBadge } from "./AdminReviewStatusBadge";
import { StarRating } from "./StarRating";

function formatCreatedAt(iso) {
  if (!iso) return { date: "—", time: "" };
  try {
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return { date: iso, time: "" };
    return {
      date: d.toLocaleDateString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }),
      time: d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }),
    };
  } catch {
    return { date: iso, time: "" };
  }
}

export function AdminReviewModerationTable({ items, disabled, onModerate }) {
  if (!items?.length) return null;

  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-left">
        <thead>
          <tr className="border-b border-outline-variant bg-surface-container-low">
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Sản phẩm
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Người mua
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Đánh giá
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Trạng thái
            </th>
            <th className="p-4 text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Ngày tạo
            </th>
            <th className="p-4 text-right text-label-sm font-semibold uppercase tracking-wide text-on-surface-variant">
              Thao tác
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-outline-variant">
          {items.map((review) => {
            const { date, time } = formatCreatedAt(review.createdAt);
            const isHidden = review.status === "HIDDEN";
            const actionLabel = isHidden ? "Xem xét lại" : "Kiểm duyệt";

            return (
              <tr
                key={review.reviewId}
                className="group transition-colors hover:bg-surface-container-low/40"
              >
                <td className="p-4">
                  <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high">
                      {review.productThumbnailUrl ? (
                        <img
                          src={review.productThumbnailUrl}
                          alt=""
                          className="h-full w-full object-cover"
                        />
                      ) : (
                        <span className="material-symbols-outlined text-on-surface-variant">
                          inventory_2
                        </span>
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="line-clamp-2 text-label-md font-medium text-on-surface group-hover:text-primary">
                        {review.productTitle || "—"}
                      </p>
                      <p className="font-mono text-body-sm text-on-surface-variant">
                        Order: {formatShortOrderId(review.orderItemId)}
                      </p>
                    </div>
                  </div>
                </td>
                <td className="p-4">
                  <div className="flex items-center gap-2">
                    {review.buyerAvatarUrl ? (
                      <img
                        src={review.buyerAvatarUrl}
                        alt=""
                        className="h-8 w-8 rounded-full object-cover"
                      />
                    ) : (
                      <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary/10 text-label-sm font-semibold text-primary">
                        {getBuyerInitials(review.buyerDisplayName)}
                      </span>
                    )}
                    <span className="text-body-sm text-on-surface">
                      {review.buyerDisplayName || "—"}
                    </span>
                  </div>
                </td>
                <td className="max-w-xs p-4">
                  <StarRating rating={review.rating ?? 0} />
                  <p
                    className={[
                      "mt-1 line-clamp-2 text-body-sm",
                      isHidden
                        ? "text-on-surface-variant/70 line-through decoration-on-surface-variant/40"
                        : "text-on-surface-variant",
                    ].join(" ")}
                  >
                    {review.comment?.trim() || "—"}
                  </p>
                </td>
                <td className="p-4">
                  <AdminReviewStatusBadge status={review.status} />
                </td>
                <td className="p-4 text-body-sm text-on-surface-variant">
                  <div>{date}</div>
                  {time ? <div className="text-label-sm">{time}</div> : null}
                </td>
                <td className="p-4 text-right">
                  <button
                    type="button"
                    disabled={disabled}
                    onClick={() => onModerate?.(review)}
                    className={[
                      "inline-flex items-center gap-1 rounded-lg px-3 py-1.5 text-label-sm font-medium transition-colors disabled:opacity-50",
                      isHidden
                        ? "border border-primary text-primary hover:bg-primary/5"
                        : "border border-outline-variant text-on-surface hover:border-primary hover:text-primary",
                    ].join(" ")}
                  >
                    <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                      {isHidden ? "visibility" : "gavel"}
                    </span>
                    {actionLabel}
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
