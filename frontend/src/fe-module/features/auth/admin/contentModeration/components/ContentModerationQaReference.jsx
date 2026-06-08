import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";
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

  // return (
  //   <AccountCard className="mb-6 border border-dashed border-outline-variant bg-surface-container-low/40">
  //     <p className="text-label-sm font-semibold text-on-surface">QA mock (VITE_USE_MOCK=true)</p>
  //     <p className="mt-1 text-xs text-on-surface-variant">
  //       Dang nhap admin@2hands.vn. Write qua admin-service; danh sach commerce van tu commerce API.
  //     </p>
  //     <dl className="mt-3 space-y-2">
  //       {rows.map((row) => (
  //         <div key={row.label} className="flex flex-col gap-0.5 sm:flex-row sm:gap-3">
  //           <dt className="shrink-0 text-xs font-medium text-on-surface-variant sm:w-40">{row.label}</dt>
  //           <dd className="break-all font-mono text-xs text-on-surface">{row.value}</dd>
  //         </div>
  //       ))}
  //     </dl>
  //   </AccountCard>
  // );
}