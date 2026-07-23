import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { DetailRow } from "../../adminAudit/components/AdminActionLogDetailDrawerView.jsx";
import { AuditCopyableId } from "../../adminAudit/components/AuditCopyableId.jsx";
import { AdminFilterButton, AdminStatusBadge } from "../../components/ui";
import {
  getCommentModerationStatusLabel,
  getCommentStatusLabel,
} from "../constants/commentModerationDisplayLabels.js";
import {
  moderationStatusBadgeVariant,
  statusBadgeVariant,
} from "../utils/moderationDisplayUtils.js";
import { CommentModerationHistoryPanel } from "./CommentModerationHistoryPanel.jsx";
import { CommentPostLink } from "./CommentPostLink.jsx";
import { PostAuthorCard } from "./PostAuthorCard.jsx";
import { PostModerationMediaPreview } from "./PostModerationMediaPreview.jsx";
import { getPostModerationStatusLabel } from "../constants/postModerationDisplayLabels.js";

function DrawerDetailSkeleton() {
  return (
    <div className="space-y-4">
      <div className="h-28 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-20 animate-pulse rounded-lg bg-admin-surface-muted" />
      <div className="h-40 animate-pulse rounded-lg bg-admin-surface-muted" />
    </div>
  );
}

export function CommentModerationDrawerView({
  commentId,
  comment,
  authorSummary,
  detailStatus,
  detailErrorMessage,
  canModerateComment,
  canRestoreComment,
  lastResult,
  historyRefreshToken,
  disabled,
  onClose,
  onModerate,
  onRestore,
}) {
  if (!commentId) return null;

  const status = comment?.status;
  const moderationStatus = comment?.moderation_status;
  const contentText = comment?.content_text || comment?.content_preview || "";
  const media = comment?.media || [];
  const postContext = comment?.post;

  return (
    <div
      className="fixed inset-0 z-50 flex min-h-dvh sm:justify-end"
      role="dialog"
      aria-modal="true"
      aria-labelledby="comment-mod-drawer-title"
    >
      <button
        type="button"
        aria-label="Đóng chi tiết bình luận"
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
                    {getCommentStatusLabel(status)}
                  </AdminStatusBadge>
                  <AdminStatusBadge variant={moderationStatusBadgeVariant(moderationStatus)}>
                    {getCommentModerationStatusLabel(moderationStatus)}
                  </AdminStatusBadge>
                  {comment?.parent_comment_id ? (
                    <AdminStatusBadge variant="neutral">Trả lời</AdminStatusBadge>
                  ) : null}
                </div>
              ) : null}
              <h2
                id="comment-mod-drawer-title"
                className="mt-2 text-balance text-lg font-semibold tracking-tight text-admin-text"
              >
                Chi tiết bình luận
              </h2>
              {contentText ? (
                <p className="mt-2 whitespace-pre-wrap text-sm text-admin-text-secondary">{contentText}</p>
              ) : detailStatus === "loading" ? (
                <p className="mt-2 text-sm text-admin-text-secondary">Đang tải nội dung bình luận…</p>
              ) : (
                <p className="mt-2 text-sm text-admin-text-secondary">Không có nội dung.</p>
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
          {detailStatus === "loading" && !comment?.content_text ? <DrawerDetailSkeleton /> : null}

          {detailStatus === "error" ? (
            <div className="mb-4 rounded-lg border border-admin-danger/30 bg-admin-danger-soft/20 px-4 py-3 text-sm text-admin-danger">
              {detailErrorMessage}
            </div>
          ) : null}

          <div className="space-y-6">
            {media.length ? (
              <section>
                <h3 className="text-sm font-semibold text-admin-text">Media</h3>
                <div className="mt-3">
                  <PostModerationMediaPreview media={media} />
                </div>
              </section>
            ) : null}

            <AuditCopyableId label="Comment ID" value={commentId} />

            {authorSummary?.userId || comment?.author_id ? (
              <section>
                <h3 className="mb-3 text-sm font-semibold text-admin-text">Tác giả</h3>
                <PostAuthorCard
                  authorId={authorSummary?.userId || comment?.author_id}
                  authorSummary={authorSummary}
                />
              </section>
            ) : null}

            {postContext || comment?.post_id ? (
              <section className="rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-4">
                <h3 className="text-sm font-semibold text-admin-text">Bài viết</h3>
                <div className="mt-3 flex gap-3">
                  {postContext?.thumbnail_url ? (
                    <img
                      src={postContext.thumbnail_url}
                      alt=""
                      className="h-16 w-16 shrink-0 rounded-lg object-cover"
                    />
                  ) : null}
                  <div className="min-w-0 flex-1">
                    <p className="line-clamp-2 text-sm text-admin-text">
                      {postContext?.caption_preview || "—"}
                    </p>
                    <div className="mt-2 flex flex-wrap items-center gap-2">
                      <CommentPostLink postId={comment?.post_id || postContext?.id} />
                      {postContext?.moderation_status ? (
                        <AdminStatusBadge variant={moderationStatusBadgeVariant(postContext.moderation_status)}>
                          {getPostModerationStatusLabel(postContext.moderation_status)}
                        </AdminStatusBadge>
                      ) : null}
                    </div>
                  </div>
                </div>
              </section>
            ) : null}

            {comment?.parent_comment ? (
              <section className="rounded-lg border border-admin-border-subtle bg-admin-surface-muted/50 p-4">
                <h3 className="text-sm font-semibold text-admin-text">Bình luận gốc</h3>
                <p className="mt-2 text-sm text-admin-text-secondary">
                  {comment.parent_comment.content_preview || "—"}
                </p>
              </section>
            ) : null}

            <dl className="grid gap-4">
              {comment ? (
                <>
                  <DetailRow label="Lượt thích" value={String(comment.like_count ?? 0)} />
                  {comment.moderation_reason ? (
                    <DetailRow label="Lý do kiểm duyệt" value={comment.moderation_reason} />
                  ) : null}
                  {comment.last_moderation_log_id ? (
                    <DetailRow
                      label="Moderation log gần nhất"
                      value={comment.last_moderation_log_id}
                      mono
                    />
                  ) : null}
                  <DetailRow
                    label="Ngày tạo"
                    value={comment.created_at ? formatDateTime(comment.created_at) : "—"}
                  />
                  <DetailRow
                    label="Cập nhật"
                    value={comment.updated_at ? formatDateTime(comment.updated_at) : "—"}
                  />
                </>
              ) : null}
            </dl>

            <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
              {canModerateComment ? (
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
              {canRestoreComment ? (
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

            <CommentModerationHistoryPanel commentId={commentId} refreshToken={historyRefreshToken} />
          </div>
        </div>
      </aside>
    </div>
  );
}
