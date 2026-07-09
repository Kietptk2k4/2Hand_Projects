import { formatShortOrderId } from "../utils/formatOrderDate";
import { getBuyerInitials } from "../utils/buyerInitials";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../auth/admin/components/ui";
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
    <>
      <AdminMobileCardList>
        {items.map((review) => {
          const { date, time } = formatCreatedAt(review.createdAt);
          const isHidden = review.status === "HIDDEN";
          const actionLabel = isHidden ? "Xem xét lại" : "Kiểm duyệt";

          return (
            <AdminMobileCard key={review.reviewId}>
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-admin-border bg-admin-surface-muted">
                  {review.productThumbnailUrl ? (
                    <img
                      src={review.productThumbnailUrl}
                      alt=""
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                      inventory_2
                    </span>
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="line-clamp-2 text-sm font-medium text-admin-text">
                    {review.productTitle || "—"}
                  </p>
                  <p className="mt-0.5 font-mono text-xs text-admin-text-muted">
                    Order {formatShortOrderId(review.orderItemId)}
                  </p>
                  <div className="mt-2">
                    <StarRating rating={review.rating ?? 0} />
                  </div>
                  <p
                    className={[
                      "mt-1 line-clamp-2 text-xs",
                      isHidden
                        ? "text-admin-text-muted line-through decoration-admin-text-muted/40"
                        : "text-admin-text-secondary",
                    ].join(" ")}
                  >
                    {review.comment?.trim() || "—"}
                  </p>
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    <AdminReviewStatusBadge status={review.status} />
                    <span className="text-xs text-admin-text-muted">
                      {review.buyerDisplayName || "—"}
                    </span>
                  </div>
                  <p className="mt-1 text-xs text-admin-text-muted">
                    {date}
                    {time ? ` · ${time}` : ""}
                  </p>
                </div>
              </div>
              <AdminFilterButton
                type="button"
                variant={isHidden ? "primary" : "secondary"}
                className="mt-3 w-full"
                disabled={disabled}
                onClick={() => onModerate?.(review)}
              >
                <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                  {isHidden ? "visibility" : "gavel"}
                </span>
                {actionLabel}
              </AdminFilterButton>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="960px" ariaLabel="Danh sách đánh giá kiểm duyệt">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Sản phẩm</AdminDataTableCell>
            <AdminDataTableCell header>Người mua</AdminDataTableCell>
            <AdminDataTableCell header>Đánh giá</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Thao tác
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((review) => {
            const { date, time } = formatCreatedAt(review.createdAt);
            const isHidden = review.status === "HIDDEN";
            const actionLabel = isHidden ? "Xem xét lại" : "Kiểm duyệt";

            return (
              <AdminDataTableRow key={review.reviewId}>
                <AdminDataTableCell>
                  <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-admin-border bg-admin-surface-muted">
                      {review.productThumbnailUrl ? (
                        <img
                          src={review.productThumbnailUrl}
                          alt=""
                          className="h-full w-full object-cover"
                        />
                      ) : (
                        <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                          inventory_2
                        </span>
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="line-clamp-2 text-sm font-medium text-admin-text">
                        {review.productTitle || "—"}
                      </p>
                      <p className="font-mono text-xs text-admin-text-muted">
                        Order: {formatShortOrderId(review.orderItemId)}
                      </p>
                    </div>
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <div className="flex items-center gap-2">
                    {review.buyerAvatarUrl ? (
                      <img
                        src={review.buyerAvatarUrl}
                        alt=""
                        className="h-8 w-8 rounded-full object-cover"
                      />
                    ) : (
                      <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-admin-accent-soft text-xs font-semibold text-admin-accent-strong">
                        {getBuyerInitials(review.buyerDisplayName)}
                      </span>
                    )}
                    <span className="text-sm text-admin-text">
                      {review.buyerDisplayName || "—"}
                    </span>
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell className="max-w-xs">
                  <StarRating rating={review.rating ?? 0} />
                  <p
                    className={[
                      "mt-1 line-clamp-2 text-sm",
                      isHidden
                        ? "text-admin-text-muted line-through decoration-admin-text-muted/40"
                        : "text-admin-text-secondary",
                    ].join(" ")}
                  >
                    {review.comment?.trim() || "—"}
                  </p>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminReviewStatusBadge status={review.status} />
                </AdminDataTableCell>
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  <div>{date}</div>
                  {time ? <div className="text-xs">{time}</div> : null}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-right">
                  <AdminFilterButton
                    type="button"
                    variant={isHidden ? "primary" : "secondary"}
                    disabled={disabled}
                    onClick={() => onModerate?.(review)}
                  >
                    <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                      {isHidden ? "visibility" : "gavel"}
                    </span>
                    {actionLabel}
                  </AdminFilterButton>
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
