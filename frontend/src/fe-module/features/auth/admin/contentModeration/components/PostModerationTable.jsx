import { formatDateTime } from "../../../security/utils/formatDateTime.js";
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
  moderationStatusBadgeVariant,
  statusBadgeVariant,
  truncateModerationId,
} from "../utils/moderationDisplayUtils.js";

export function PostModerationTable({ items, selectedPostId, onRowSelect }) {
  if (!items.length) {
    return (
      <p className="py-6 text-center text-sm text-admin-text-secondary">
        Không có bài viết phù hợp bộ lọc.
      </p>
    );
  }

  return (
    <>
      <AdminMobileCardList>
        {items.map((row) => {
          const isSelected = selectedPostId === row.id;
          return (
            <AdminMobileCard
              key={row.id}
              isSelected={isSelected}
              onClick={() => onRowSelect?.(row)}
              ariaLabel={`Chọn bài viết ${row.id}`}
            >
              <div className="flex items-start justify-between gap-3">
                <p className="font-mono text-xs text-admin-text-muted" title={row.id}>
                  {truncateModerationId(row.id)}
                </p>
                <AdminStatusBadge variant={moderationStatusBadgeVariant(row.moderation_status)}>
                  {row.moderation_status}
                </AdminStatusBadge>
              </div>
              <p className="mt-2 line-clamp-2 text-sm text-admin-text">{row.caption_preview || "—"}</p>
              <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-admin-text-secondary">
                <AdminStatusBadge variant={statusBadgeVariant(row.status)}>{row.status}</AdminStatusBadge>
                <span>{row.like_count ?? 0} thích</span>
                <span>{formatDateTime(row.created_at)}</span>
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="880px" ariaLabel="Danh sách bài viết kiểm duyệt">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Post ID</AdminDataTableCell>
            <AdminDataTableCell header>Nội dung</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Kiểm duyệt</AdminDataTableCell>
            <AdminDataTableCell header>Thích</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((row) => {
            const isSelected = selectedPostId === row.id;
            return (
              <AdminDataTableRow
                key={row.id}
                isSelected={isSelected}
                onClick={() => onRowSelect?.(row)}
              >
                <AdminDataTableCell className="font-mono text-xs" title={row.id}>
                  {truncateModerationId(row.id)}
                </AdminDataTableCell>
                <AdminDataTableCell className="max-w-xs truncate">{row.caption_preview || "—"}</AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminStatusBadge variant={statusBadgeVariant(row.status)}>{row.status}</AdminStatusBadge>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminStatusBadge variant={moderationStatusBadgeVariant(row.moderation_status)}>
                    {row.moderation_status}
                  </AdminStatusBadge>
                </AdminDataTableCell>
                <AdminDataTableCell>{row.like_count ?? 0}</AdminDataTableCell>
                <AdminDataTableCell className="whitespace-nowrap">
                  {formatDateTime(row.created_at)}
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
