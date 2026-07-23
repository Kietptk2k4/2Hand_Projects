import { Link } from "react-router-dom";
import { buildAdminSearchParams } from "../../auth/admin/adminUrlParams.js";
import { AuditCopyableId } from "../../auth/admin/adminAudit/components/AuditCopyableId.jsx";
import { PostAuthorInvestigationLink } from "../../auth/admin/contentModeration/components/PostAuthorInvestigationLink.jsx";
import { formatPostListDateTime } from "../../auth/admin/contentModeration/utils/postDateTimeDisplay.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../auth/admin/components/ui";
import { AdminReviewStatusBadge } from "./AdminReviewStatusBadge";
import { StarRating } from "./StarRating";

function ProductModerationLink({ productId, productTitle }) {
  if (!productId) return <span className="text-admin-text-muted">—</span>;

  const to = `/admin?${buildAdminSearchParams({
    section: "contentModeration",
    tab: "product-moderation",
    productId,
  }).toString()}`;

  return (
    <Link
      to={to}
      onClick={(event) => event.stopPropagation()}
      className="flex min-w-0 items-start gap-2 text-sm font-medium text-admin-accent hover:underline"
    >
      <span className="line-clamp-2">{productTitle || productId}</span>
    </Link>
  );
}

export function ReviewModerationTable({
  items,
  selectedReviewId,
  selectedReviewIds = [],
  selectionEnabled = false,
  onRowSelect,
  onToggleReview,
  onToggleAll,
}) {
  if (!items?.length) return null;

  const allSelected = items.every((review) => selectedReviewIds.includes(review.reviewId));

  return (
    <>
      <AdminMobileCardList>
        {items.map((review) => {
          const isDrawerSelected = selectedReviewId === review.reviewId;
          const isBulkSelected = selectedReviewIds.includes(review.reviewId);
          const createdAt = formatPostListDateTime(review.createdAt);

          return (
            <AdminMobileCard
              key={review.reviewId}
              isSelected={isDrawerSelected}
              onClick={() => onRowSelect?.(review)}
              ariaLabel={`Chọn đánh giá ${review.reviewId}`}
            >
              {selectionEnabled ? (
                <label
                  className="mb-3 flex min-h-10 items-center gap-2"
                  onClick={(event) => event.stopPropagation()}
                >
                  <input
                    type="checkbox"
                    checked={isBulkSelected}
                    onChange={() => onToggleReview?.(review.reviewId)}
                    aria-label={`Chọn đánh giá ${review.reviewId}`}
                  />
                  <span className="text-xs text-admin-text-secondary">Chọn để thao tác hàng loạt</span>
                </label>
              ) : null}
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0 flex-1">
                  <StarRating rating={review.rating ?? 0} />
                  <p className="mt-2 line-clamp-3 text-sm text-admin-text">
                    {review.comment?.trim() || "—"}
                  </p>
                  <div className="mt-2 flex flex-wrap items-center gap-2">
                    <AdminReviewStatusBadge status={review.status} />
                  </div>
                  <p className="mt-1 text-xs text-admin-text-muted">
                    {createdAt.date} {createdAt.time}
                  </p>
                </div>
                <span className="material-symbols-outlined shrink-0 text-admin-text-muted" aria-hidden="true">
                  chevron_right
                </span>
              </div>
              <div className="mt-2 space-y-1" onClick={(event) => event.stopPropagation()}>
                <ProductModerationLink productId={review.productId} productTitle={review.productTitle} />
                <PostAuthorInvestigationLink authorId={review.buyerId} />
                <PostAuthorInvestigationLink authorId={review.sellerId} />
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="1100px" ariaLabel="Danh sách đánh giá kiểm duyệt">
        <AdminDataTableHead>
          <AdminDataTableRow>
            {selectionEnabled ? (
              <AdminDataTableCell header className="w-12">
                <input
                  type="checkbox"
                  checked={allSelected}
                  onChange={() => onToggleAll?.(items)}
                  aria-label="Chọn tất cả trên trang"
                />
              </AdminDataTableCell>
            ) : null}
            <AdminDataTableCell header className="min-w-[16rem]">
              Đánh giá
            </AdminDataTableCell>
            <AdminDataTableCell header>Sản phẩm</AdminDataTableCell>
            <AdminDataTableCell header>Người mua</AdminDataTableCell>
            <AdminDataTableCell header>Chủ shop</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
            <AdminDataTableCell header>Review ID</AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((review) => {
            const isDrawerSelected = selectedReviewId === review.reviewId;
            const isBulkSelected = selectedReviewIds.includes(review.reviewId);
            const createdAt = formatPostListDateTime(review.createdAt);

            return (
              <AdminDataTableRow
                key={review.reviewId}
                isSelected={isDrawerSelected}
                onClick={() => onRowSelect?.(review)}
                ariaLabel={`Chọn đánh giá ${review.reviewId}`}
              >
                {selectionEnabled ? (
                  <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={isBulkSelected}
                      onChange={() => onToggleReview?.(review.reviewId)}
                      aria-label={`Chọn đánh giá ${review.reviewId}`}
                    />
                  </AdminDataTableCell>
                ) : null}
                <AdminDataTableCell>
                  <div className="min-w-0">
                    <StarRating rating={review.rating ?? 0} />
                    <p className="mt-2 line-clamp-2 text-sm text-admin-text">
                      {review.comment?.trim() || "—"}
                    </p>
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <div className="flex items-start gap-2">
                    {review.productThumbnailUrl ? (
                      <img
                        src={review.productThumbnailUrl}
                        alt=""
                        className="h-10 w-10 shrink-0 rounded-lg border border-admin-border object-cover"
                      />
                    ) : null}
                    <ProductModerationLink
                      productId={review.productId}
                      productTitle={review.productTitle}
                    />
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <PostAuthorInvestigationLink authorId={review.buyerId} />
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <PostAuthorInvestigationLink authorId={review.sellerId} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminReviewStatusBadge status={review.status} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <div className="text-sm text-admin-text-secondary">
                    <div>{createdAt.date}</div>
                    <div className="text-xs text-admin-text-muted">{createdAt.time}</div>
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <AuditCopyableId value={review.reviewId} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                    chevron_right
                  </span>
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
