import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { RbacUserStatusBadge } from "./RbacUserStatusBadge.jsx";

export function RbacUserTable({ items, selectedUserId, onUserSelect }) {
  if (!items?.length) {
    return <p className="text-sm text-admin-text-muted">Không có người dùng phù hợp bộ lọc.</p>;
  }

  return (
    <>
      <AdminMobileCardList>
        {items.map((row) => {
          const isSelected = selectedUserId === row.id;
          return (
            <AdminMobileCard
              key={row.id}
              isSelected={isSelected}
              onClick={() => onUserSelect(row.id, row)}
              ariaLabel={`Chọn người dùng ${row.email}`}
            >
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0 flex-1">
                  <p className="truncate font-medium text-admin-text" title={row.email}>
                    {row.email}
                  </p>
                  <p className="mt-0.5 truncate text-sm text-admin-text-secondary" title={row.display_name || undefined}>
                    {row.display_name || "—"}
                  </p>
                </div>
                <RbacUserStatusBadge status={row.status} />
              </div>
              <p className="mt-2 text-xs text-admin-text-muted">
                {row.role_codes?.length > 0 ? row.role_codes.join(", ") : "—"} ·{" "}
                {formatDateTime(row.created_at)}
              </p>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="720px" ariaLabel="Danh sách người dùng RBAC">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Email</AdminDataTableCell>
            <AdminDataTableCell header>Tên hiển thị</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Vai trò</AdminDataTableCell>
            <AdminDataTableCell header>Ngày tạo</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((row) => {
            const isSelected = selectedUserId === row.id;
            return (
              <AdminDataTableRow
                key={row.id}
                isSelected={isSelected}
                onClick={() => onUserSelect(row.id, row)}
              >
                <AdminDataTableCell>{row.email}</AdminDataTableCell>
                <AdminDataTableCell>{row.display_name || "—"}</AdminDataTableCell>
                <AdminDataTableCell>
                  <RbacUserStatusBadge status={row.status} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  {row.role_codes?.length > 0 ? row.role_codes.join(", ") : "—"}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-admin-text-secondary">
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
