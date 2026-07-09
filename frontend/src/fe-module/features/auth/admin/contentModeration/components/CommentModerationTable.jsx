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
import { statusBadgeVariant, truncateModerationId } from "../utils/moderationDisplayUtils.js";

export function CommentModerationTable({ items, selectedCommentId, onRowSelect }) {
  if (!items.length) {
    return (
      <p className="py-6 text-center text-sm text-admin-text-secondary">
        Không có bình luận phù hợp bộ lọc.
      </p>
    );
  }

  return (
    <>
      <AdminMobileCardList>
        {items.map((row) => {
          const isSelected = selectedCommentId === row.id;
          return (
            <AdminMobileCard
              key={row.id}
              isSelected={isSelected}
              onClick={() => onRowSelect?.(row)}
              ariaLabel={`Chọn bình luận ${row.id}`}
            >
              <div className="flex items-start justify-between gap-3">
                <p className="font-mono text-xs text-admin-text-muted" title={row.id}>
                  {truncateModerationId(row.id)}
                </p>
                <AdminStatusBadge variant={statusBadgeVariant(row.status)}>{row.status}</AdminStatusBadge>
              </div>
              <p className="mt-1 font-mono text-[11px] text-admin-text-muted" title={row.post_id}>
                Post: {truncateModerationId(row.post_id)}
              </p>
              <p className="mt-2 line-clamp-2 text-sm text-admin-text">{row.content_preview || "—"}</p>
              <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-admin-text-secondary">
                <span>{row.like_count ?? 0} thích</span>
                <span>{formatDateTime(row.created_at)}</span>
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="880px" ariaLabel="Danh sách bình luận kiểm duyệt">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Comment ID</AdminDataTableCell>
            <AdminDataTableCell header>Post ID</AdminDataTableCell>
            <AdminDataTableCell header>Nội dung</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Thích</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((row) => {
            const isSelected = selectedCommentId === row.id;
            return (
              <AdminDataTableRow
                key={row.id}
                isSelected={isSelected}
                onClick={() => onRowSelect?.(row)}
              >
                <AdminDataTableCell className="font-mono text-xs" title={row.id}>
                  {truncateModerationId(row.id)}
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs" title={row.post_id}>
                  {truncateModerationId(row.post_id)}
                </AdminDataTableCell>
                <AdminDataTableCell className="max-w-xs truncate">{row.content_preview || "—"}</AdminDataTableCell>
                <AdminDataTableCell>
                  <AdminStatusBadge variant={statusBadgeVariant(row.status)}>{row.status}</AdminStatusBadge>
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
