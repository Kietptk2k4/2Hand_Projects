import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { DetailRow } from "../../adminAudit/components/AdminActionLogDetailDrawerView.jsx";
import { AdminFilterButton, AdminStatusBadge } from "../../components/ui";
import {
  getPostModerationStatusLabel,
  getPostStatusLabel,
} from "../constants/postModerationDisplayLabels.js";
import {
  moderationStatusBadgeVariant,
  statusBadgeVariant,
} from "../utils/moderationDisplayUtils.js";
import { AuditCopyableId } from "../../adminAudit/components/AuditCopyableId.jsx";
import { PostAuthorCard } from "./PostAuthorCard.jsx";
import { PostModerationHistoryPanel } from "./PostModerationHistoryPanel.jsx";
import { PostModerationMediaPreview } from "./PostModerationMediaPreview.jsx";

function DrawerDetailSkeleton() {
  return (
    <div className="space-y-4">
      <div className="h-28 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-20 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-40 animate-pulse rounded-lg bg-admin-surface-muted" />
    </div>
  );
}

export function PostModerationDrawerView({
  postId,
  post,
  authorSummary,
  detailStatus,
  detailErrorMessage,
  canModeratePost,
  canRestorePost,
  lastResult,
  historyRefreshToken,
  disabled,
  onClose,
  onModerate,
  onRestore,
}) {
  if (!postId) return null;

  const status = post?.status;
  const moderationStatus = post?.moderation_status;
  const caption = post?.caption || post?.caption_preview || "";
  const thumbnailUrl = post?.thumbnail_url || "";
  const media = post?.media || [];

  return (
    <div
      className="fixed inset-0 z-50 flex min-h-dvh sm:justify-end"
      role="dialog"
      aria-modal="true"
      aria-labelledby="post-mod-drawer-title"
    >
      <button
        type="button"
        aria-label="Đóng chi tiết bài viết"
        className="absolute inset-0 bg-admin-text/40 backdrop-blur-sm"
        onClick={onClose}
      />

      <aside className="relative z-10 flex h-full max-h-dvh w-full flex-col bg-admin-surface shadow-[var(--shadow-admin-surface)] sm:max-w-xl sm:border-l sm:border-admin-border">
        <div className="shrink-0 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0 flex-1">
              {status ? (
                <div className="flex flex-wrap items-center gap-2">
                  <AdminStatusBadge variant={statusBadgeVariant(status)}>
                    {getPostStatusLabel(status)}
                  </AdminStatusBadge>
                  <AdminStatusBadge variant={moderationStatusBadgeVariant(moderationStatus)}>
                    {getPostModerationStatusLabel(moderationStatus)}
                  </AdminStatusBadge>
                </div>
              ) : null}
              <h2
                id="post-mod-drawer-title"
                className="mt-2 text-balance text-lg font-semibold tracking-tight text-admin-text"
              >
                Chi tiết bài viết
              </h2>
              {caption ? (
                <p className="mt-2 whitespace-pre-wrap text-sm text-admin-text-secondary">{caption}</p>
              ) : detailStatus === "loading" ? (
                <p className="mt-2 text-sm text-admin-text-secondary">Đang tải nội dung bài viết…</p>
              ) : (
                <p className="mt-2 text-sm text-admin-text-secondary">Không có nội dung xem trước.</p>
              )}
            </div>
            <button
              type="button"
              onClick={onClose}
              className="inline-flex min-h-10 min-w-10 shrink-0 items-center justify-center rounded-lg border border-admin-border text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
              aria-label="Đóng"
            >
              <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                close
              </span>
            </button>
          </div>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto px-4 py-5 sm:px-6">
          {detailStatus === "loading" && !post?.caption ? <DrawerDetailSkeleton /> : null}

          {detailStatus === "error" ? (
            <div className="mb-4 rounded-lg border border-admin-danger/30 bg-admin-danger-soft/20 px-4 py-3 text-sm text-admin-danger">
              {detailErrorMessage}
            </div>
          ) : null}

          <div className="space-y-6">
            <section>
              <h3 className="text-sm font-semibold text-admin-text">Media</h3>
              <div className="mt-3">
                <PostModerationMediaPreview media={media} thumbnailUrl={thumbnailUrl} />
              </div>
            </section>

            <AuditCopyableId label="Post ID" value={postId} />

            {authorSummary?.userId || post?.author_id ? (
              <section>
                <h3 className="mb-3 text-sm font-semibold text-admin-text">Tác giả</h3>
                <PostAuthorCard
                  authorId={authorSummary?.userId || post?.author_id}
                  authorSummary={authorSummary}
                />
              </section>
            ) : null}

            <dl className="grid gap-4">
              {post ? (
                <>
                  <DetailRow label="Lượt thích" value={String(post.like_count ?? 0)} />
                  {post.reply_count != null ? (
                    <DetailRow label="Bình luận" value={String(post.reply_count)} />
                  ) : null}
                  {post.visibility ? <DetailRow label="Hiển thị" value={post.visibility} /> : null}
                  {post.moderation_reason ? (
                    <DetailRow label="Lý do kiểm duyệt" value={post.moderation_reason} />
                  ) : null}
                  {post.last_moderation_log_id ? (
                    <DetailRow
                      label="Moderation log gần nhất"
                      value={post.last_moderation_log_id}
                      mono
                    />
                  ) : null}
                  <DetailRow
                    label="Ngày tạo"
                    value={post.created_at ? formatDateTime(post.created_at) : "—"}
                  />
                  <DetailRow
                    label="Cập nhật"
                    value={post.updated_at ? formatDateTime(post.updated_at) : "—"}
                  />
                  {post.hashtags?.length ? (
                    <DetailRow label="Hashtag" value={post.hashtags.join(", ")} />
                  ) : null}
                </>
              ) : null}
            </dl>

            <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
              {canModeratePost ? (
                <AdminFilterButton
                  type="button"
                  variant="primary"
                  disabled={disabled}
                  onClick={onModerate}
                  className="inline-flex gap-2 sm:min-w-[11rem]"
                >
                  <span className="material-symbols-outlined text-base" aria-hidden="true">
                    gavel
                  </span>
                  Kiểm duyệt
                </AdminFilterButton>
              ) : null}
              {canRestorePost ? (
                <AdminFilterButton
                  type="button"
                  variant="secondary"
                  disabled={disabled}
                  onClick={onRestore}
                  className="inline-flex gap-2 sm:min-w-[11rem]"
                >
                  <span className="material-symbols-outlined text-base" aria-hidden="true">
                    restore
                  </span>
                  Khôi phục
                </AdminFilterButton>
              ) : null}
            </div>

            {lastResult ? (
              <div className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-4 text-sm">
                <p className="font-medium text-admin-text">Lần thao tác gần nhất</p>
                <dl className="mt-2 grid gap-1 text-admin-text-secondary">
                  {lastResult.action ? (
                    <div>
                      <dt className="inline font-medium text-admin-text">Hành động: </dt>
                      <dd className="inline">{lastResult.action}</dd>
                    </div>
                  ) : null}
                  <div>
                    <dt className="inline font-medium text-admin-text">moderation_log_id: </dt>
                    <dd className="inline break-all font-mono">{lastResult.moderationLogId || "—"}</dd>
                  </div>
                  {lastResult.actedAt ? (
                    <div>
                      <dt className="inline font-medium text-admin-text">Thời gian: </dt>
                      <dd className="inline">{formatDateTime(lastResult.actedAt)}</dd>
                    </div>
                  ) : null}
                </dl>
              </div>
            ) : null}

            <PostModerationHistoryPanel postId={postId} refreshToken={historyRefreshToken} />
          </div>
        </div>
      </aside>
    </div>
  );
}
