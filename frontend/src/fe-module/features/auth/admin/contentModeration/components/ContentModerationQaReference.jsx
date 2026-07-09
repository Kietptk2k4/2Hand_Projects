import { AdminSurfaceCard } from "../../components/ui";
import { ADMIN_CONTENT_MODERATION_QA } from "../../../../../shared/constants/adminContentModerationQaIds.js";

const QA_ROWS = {
  "action-logs": [
    { label: "logId (detail drawer)", value: ADMIN_CONTENT_MODERATION_QA.audit.logId },
    { label: "admin_id filter", value: ADMIN_CONTENT_MODERATION_QA.audit.filterAdminId },
  ],
  "post-moderation": [
    { label: "postId", value: ADMIN_CONTENT_MODERATION_QA.post.sample },
    { label: "404", value: ADMIN_CONTENT_MODERATION_QA.post.notFound },
  ],
  "comment-moderation": [
    { label: "commentId", value: ADMIN_CONTENT_MODERATION_QA.comment.sample },
    { label: "404", value: ADMIN_CONTENT_MODERATION_QA.comment.notFound },
  ],
  "shop-moderation": [
    { label: "ACTIVE shop", value: ADMIN_CONTENT_MODERATION_QA.shop.active },
    { label: "SUSPENDED shop", value: ADMIN_CONTENT_MODERATION_QA.shop.suspended },
    { label: "CLOSED shop", value: ADMIN_CONTENT_MODERATION_QA.shop.closed },
  ],
  "product-moderation": [
    { label: "ACTIVE product", value: ADMIN_CONTENT_MODERATION_QA.product.active },
    { label: "REMOVED product", value: ADMIN_CONTENT_MODERATION_QA.product.removed },
  ],
  "review-moderation": [
    { label: "VISIBLE review", value: ADMIN_CONTENT_MODERATION_QA.review.visible },
    { label: "HIDDEN review", value: ADMIN_CONTENT_MODERATION_QA.review.hidden },
  ],
};

export function ContentModerationQaReference({ activeTab }) {
  if (!import.meta.env.DEV) return null;

  const rows = QA_ROWS[activeTab];
  if (!rows?.length) return null;

  return (
    <AdminSurfaceCard
      padding="md"
      className="mb-6 max-w-full min-w-0 border border-dashed border-admin-border bg-admin-surface-raised/80"
    >
      <p className="text-xs font-semibold uppercase tracking-[0.08em] text-admin-text-muted">
        QA mock (VITE_USE_MOCK=true)
      </p>
      <p className="mt-1 text-xs text-admin-text-secondary">
        Đăng nhập admin@2hands.vn. Write qua admin-service; danh sách commerce vẫn từ commerce API.
      </p>
      <dl className="mt-3 space-y-2">
        {rows.map((row) => (
          <div key={row.label} className="flex min-w-0 flex-col gap-0.5 sm:flex-row sm:gap-3">
            <dt className="shrink-0 text-xs font-medium text-admin-text-muted sm:w-40">{row.label}</dt>
            <dd className="min-w-0 break-all font-mono text-xs text-admin-text">{row.value}</dd>
          </div>
        ))}
      </dl>
    </AdminSurfaceCard>
  );
}
