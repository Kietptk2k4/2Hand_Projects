import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
  AdminStatusBadge,
} from "../../components/ui";
import {
  getCommentModerationStatusLabel,
  getCommentStatusLabel,
} from "../constants/commentModerationDisplayLabels.js";
import {
  moderationStatusBadgeVariant,
  statusBadgeVariant,
  truncateModerationId,
} from "../utils/moderationDisplayUtils.js";
import { formatPostListDateTime } from "../utils/postDateTimeDisplay.js";
import { resolveCommentAuthorSummary } from "../utils/commentModerationListMapper.js";
import { CommentPostLink } from "./CommentPostLink.jsx";
import { PostAuthorInvestigationLink } from "./PostAuthorInvestigationLink.jsx";

export function CommentModerationTable({
  items,
  selectedCommentId,
  selectedCommentIds = [],
  selectionEnabled = false,
  authorSummaries = {},
  onRowSelect,
  onToggleComment,
  onToggleAll,
}) {
  if (!items.length) {
    return null;
  }

  const allSelected = items.every((row) => selectedCommentIds.includes(row.id));

  return (
    <>
      <AdminMobileCardList>
        {items.map((row) => {
          const isDrawerSelected = selectedCommentId === row.id;
          const isBulkSelected = selectedCommentIds.includes(row.id);
          const createdAt = formatPostListDateTime(row.created_at);
          const authorSummary = resolveCommentAuthorSummary(row, authorSummaries);

          return (
            <AdminMobileCard
              key={row.id}
              isSelected={isDrawerSelected}
              onClick={() => onRowSelect?.(row)}
              ariaLabel={`Chọn bình luận ${row.id}`}
            >
              {selectionEnabled ? (
                <label
                  className="mb-3 flex min-h-10 items-center gap-2"
                  onClick={(event) => event.stopPropagation()}
                >
                  <input
                    type="checkbox"
                    checked={isBulkSelected}
                    onChange={() => onToggleComment?.(row.id)}
                    aria-label={`Chọn bình luận ${row.id}`}
                  />
                  <span className="text-xs text-admin-text-secondary">Chọn để thao tác hàng loạt</span>
                </label>
              ) : null}
              <div className="flex items-start justify-between gap-3">
                <p className="line-clamp-2 min-w-0 flex-1 text-sm text-admin-text">
                  {row.content_preview || "—"}
                </p>
                <span className="material-symbols-outlined shrink-0 text-admin-text-muted" aria-hidden="true">
                  chevron_right
                </span>
              </div>
              <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-admin-text-secondary">
                <AdminStatusBadge variant={moderationStatusBadgeVariant(row.moderation_status)}>
                  {getCommentModerationStatusLabel(row.moderation_status)}
                </AdminStatusBadge>
                <AdminStatusBadge variant={statusBadgeVariant(row.status)}>
                  {getCommentStatusLabel(row.status)}
                </AdminStatusBadge>
                <span>{row.like_count ?? 0} thích</span>
                <span>
                  {createdAt.date} {createdAt.time}
                </span>
              </div>
              {authorSummary ? (
                <div className="mt-2" onClick={(event) => event.stopPropagation()}>
                  <PostAuthorInvestigationLink authorId={authorSummary.userId} authorSummary={authorSummary} />
                </div>
              ) : null}
              <div className="mt-2" onClick={(event) => event.stopPropagation()}>
                <CommentPostLink postId={row.post_id} />
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="1100px" ariaLabel="Danh sách bình luận kiểm duyệt">
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
            <AdminDataTableCell header className="min-w-[14rem]">
              Nội dung
            </AdminDataTableCell>
            <AdminDataTableCell header>Tác giả</AdminDataTableCell>
            <AdminDataTableCell header>Bài viết</AdminDataTableCell>
            <AdminDataTableCell header>Kiểm duyệt</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Thích</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
            <AdminDataTableCell header className="w-28">
              Comment ID
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((row) => {
            const isSelected = selectedCommentId === row.id;
            const isBulkSelected = selectedCommentIds.includes(row.id);
            const createdAt = formatPostListDateTime(row.created_at);
            const authorSummary = resolveCommentAuthorSummary(row, authorSummaries);

            return (
              <AdminDataTableRow
                key={row.id}
                isSelected={isSelected}
                onClick={() => onRowSelect?.(row)}
              >
                {selectionEnabled ? (
                  <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={isBulkSelected}
                      onChange={() => onToggleComment?.(row.id)}
                      aria-label={`Chọn bình luận ${row.id}`}
                    />
                  </AdminDataTableCell>
                ) : null}
                <AdminDataTableCell className="max-w-md">
                  <p className="line-clamp-2 text-sm text-admin-text">{row.content_preview || "—"}</p>
                  {row.parent_comment_id ? (
                    <span className="mt-1 inline-flex text-[11px] text-admin-text-muted">Trả lời</span>
                  ) : null}
                  {row.media_count > 0 ? (
                    <span className="mt-1 inline-flex items-center gap-1 text-[11px] text-admin-text-muted">
                      <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
                        image
                      </span>
                      {row.media_count}
                    </span>
                  ) : null}
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  {authorSummary ? (
                    <PostAuthorInvestigationLink
                      authorId={authorSummary.userId}
                      authorSummary={authorSummary}
                    />
                  ) : (
                    "—"
                  )}
                </AdminDataTableCell>
                <AdminDataTableCell onClick={(event) => event.stopPropagation()}>
                  <CommentPostLink postId={row.post_id} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminStatusBadge variant={moderationStatusBadgeVariant(row.moderation_status)}>
                    {getCommentModerationStatusLabel(row.moderation_status)}
                  </AdminStatusBadge>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminStatusBadge variant={statusBadgeVariant(row.status)}>
                    {getCommentStatusLabel(row.status)}
                  </AdminStatusBadge>
                </AdminDataTableCell>
                <AdminDataTableCell className="tabular-nums">{row.like_count ?? 0}</AdminDataTableCell>
                <AdminDataTableCell className="whitespace-nowrap">
                  <div className="text-sm text-admin-text">{createdAt.date}</div>
                  <div className="text-xs text-admin-text-muted">{createdAt.time}</div>
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs text-admin-text-muted" title={row.id}>
                  {truncateModerationId(row.id)}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-admin-text-muted">
                  <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
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
