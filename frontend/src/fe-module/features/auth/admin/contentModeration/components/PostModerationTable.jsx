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
  getPostModerationStatusLabel,
  getPostStatusLabel,
} from "../constants/postModerationDisplayLabels.js";
import {
  moderationStatusBadgeVariant,
  statusBadgeVariant,
  truncateModerationId,
} from "../utils/moderationDisplayUtils.js";
import { formatPostListDateTime } from "../utils/postDateTimeDisplay.js";
import { resolvePostAuthorSummary } from "../utils/postModerationListMapper.js";
import { PostAuthorInvestigationLink } from "./PostAuthorInvestigationLink.jsx";
import { PostModerationThumbnail } from "./PostModerationThumbnail.jsx";

export function PostModerationTable({
  items,
  selectedPostId,
  selectedPostIds = [],
  selectionEnabled = false,
  authorSummaries = {},
  onRowSelect,
  onTogglePost,
  onToggleAll,
}) {
  if (!items.length) {
    return null;
  }

  const allSelected = items.every((row) => selectedPostIds.includes(row.id));

  return (
    <>
      <AdminMobileCardList>
        {items.map((row) => {
          const isDrawerSelected = selectedPostId === row.id;
          const isBulkSelected = selectedPostIds.includes(row.id);
          const createdAt = formatPostListDateTime(row.created_at);
          const authorSummary = resolvePostAuthorSummary(row, authorSummaries);
          return (
            <AdminMobileCard
              key={row.id}
              isSelected={isDrawerSelected}
              onClick={() => onRowSelect?.(row)}
              ariaLabel={`Chọn bài viết ${row.id}`}
            >
              {selectionEnabled ? (
                <label
                  className="mb-3 flex min-h-10 items-center gap-2"
                  onClick={(event) => event.stopPropagation()}
                >
                  <input
                    type="checkbox"
                    checked={isBulkSelected}
                    onChange={() => onTogglePost?.(row.id)}
                    aria-label={`Chọn bài viết ${row.id}`}
                  />
                  <span className="text-xs text-admin-text-secondary">Chọn để thao tác hàng loạt</span>
                </label>
              ) : null}
              <div className="flex items-start gap-3">
                <PostModerationThumbnail url={row.thumbnail_url} />
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-3">
                    <p className="font-mono text-xs text-admin-text-muted" title={row.id}>
                      {truncateModerationId(row.id)}
                    </p>
                    <span className="material-symbols-outlined shrink-0 text-admin-text-muted" aria-hidden="true">
                      chevron_right
                    </span>
                  </div>
                  <p className="mt-2 line-clamp-2 text-sm text-admin-text">{row.caption_preview || "—"}</p>
                </div>
              </div>
              <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-admin-text-secondary">
                <AdminStatusBadge variant={moderationStatusBadgeVariant(row.moderation_status)}>
                  {getPostModerationStatusLabel(row.moderation_status)}
                </AdminStatusBadge>
                <AdminStatusBadge variant={statusBadgeVariant(row.status)}>
                  {getPostStatusLabel(row.status)}
                </AdminStatusBadge>
                <span>{row.like_count ?? 0} thích</span>
                <span>
                  {createdAt.date} {createdAt.time}
                </span>
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="1100px" ariaLabel="Danh sách bài viết kiểm duyệt">
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
            <AdminDataTableCell header className="w-16">
              Ảnh
            </AdminDataTableCell>
            <AdminDataTableCell header>Post ID</AdminDataTableCell>
            <AdminDataTableCell header>Nội dung</AdminDataTableCell>
            <AdminDataTableCell header>Tác giả</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Kiểm duyệt</AdminDataTableCell>
            <AdminDataTableCell header>Thích</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((row) => {
            const isDrawerSelected = selectedPostId === row.id;
            const isBulkSelected = selectedPostIds.includes(row.id);
            const createdAt = formatPostListDateTime(row.created_at);
            const authorSummary = resolvePostAuthorSummary(row, authorSummaries);
            return (
              <AdminDataTableRow
                key={row.id}
                isSelected={isDrawerSelected}
                onClick={() => onRowSelect?.(row)}
              >
                {selectionEnabled ? (
                  <AdminDataTableCell>
                    <input
                      type="checkbox"
                      checked={isBulkSelected}
                      onClick={(event) => event.stopPropagation()}
                      onChange={() => onTogglePost?.(row.id)}
                      aria-label={`Chọn bài viết ${row.id}`}
                    />
                  </AdminDataTableCell>
                ) : null}
                <AdminDataTableCell>
                  <PostModerationThumbnail url={row.thumbnail_url} />
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs" title={row.id}>
                  {truncateModerationId(row.id)}
                </AdminDataTableCell>
                <AdminDataTableCell className="max-w-xs truncate">
                  {row.caption_preview || "—"}
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <PostAuthorInvestigationLink
                    authorId={row.author_id}
                    authorSummary={authorSummary}
                  />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminStatusBadge variant={statusBadgeVariant(row.status)}>
                    {getPostStatusLabel(row.status)}
                  </AdminStatusBadge>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminStatusBadge variant={moderationStatusBadgeVariant(row.moderation_status)}>
                    {getPostModerationStatusLabel(row.moderation_status)}
                  </AdminStatusBadge>
                </AdminDataTableCell>
                <AdminDataTableCell>{row.like_count ?? 0}</AdminDataTableCell>
                <AdminDataTableCell className="whitespace-nowrap">
                  <div className="text-sm text-admin-text">{createdAt.time}</div>
                  <div className="text-xs text-admin-text-muted">{createdAt.date}</div>
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
