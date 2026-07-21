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
import { RoleCodeBadge } from "./RoleCodeBadge.jsx";
import { RbacUserStatusBadge } from "./RbacUserStatusBadge.jsx";

function SelectionIndicator({ selected }) {
  return (
    <span
      className={[
        "mt-0.5 inline-flex h-4 w-4 shrink-0 items-center justify-center rounded-full border transition-colors duration-200",
        selected
          ? "border-admin-accent bg-admin-accent text-white"
          : "border-admin-border bg-admin-surface",
      ].join(" ")}
      aria-hidden="true"
    >
      {selected ? (
        <span className="block h-1.5 w-1.5 rounded-full bg-white" />
      ) : null}
    </span>
  );
}

function RoleCodesCell({ roleCodes }) {
  if (!roleCodes?.length) {
    return <span className="text-admin-text-muted">—</span>;
  }

  return (
    <span className="inline-flex max-w-full flex-wrap gap-1">
      {roleCodes.map((code) => (
        <RoleCodeBadge key={code} code={code} />
      ))}
    </span>
  );
}

export function RbacUserTable({ items, selectedUserId, onUserSelect }) {
  if (!items?.length) {
    return (
      <p className="py-6 text-center text-sm text-admin-text-muted">
        Không có người dùng phù hợp bộ lọc.
      </p>
    );
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
              <div className="flex items-start gap-3">
                <SelectionIndicator selected={isSelected} />
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium text-admin-text" title={row.email}>
                        {row.email}
                      </p>
                      <p
                        className="mt-0.5 truncate text-sm text-admin-text-secondary"
                        title={row.display_name || undefined}
                      >
                        {row.display_name || "—"}
                      </p>
                    </div>
                    <RbacUserStatusBadge status={row.status} />
                  </div>
                  <div className="mt-2 flex flex-wrap items-center gap-1.5 text-xs text-admin-text-muted">
                    <RoleCodesCell roleCodes={row.role_codes} />
                    <span aria-hidden="true">·</span>
                    <span className="tabular-nums">{formatDateTime(row.created_at)}</span>
                  </div>
                </div>
              </div>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="0" className="w-full" ariaLabel="Danh sách người dùng RBAC">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header className="w-10 pr-2">
              <span className="sr-only">Chọn</span>
            </AdminDataTableCell>
            <AdminDataTableCell header>Email</AdminDataTableCell>
            <AdminDataTableCell header className="hidden lg:table-cell">
              Tên hiển thị
            </AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header className="hidden xl:table-cell">Vai trò</AdminDataTableCell>
            <AdminDataTableCell header className="hidden 2xl:table-cell">
              Ngày tạo
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((row, index) => {
            const isSelected = selectedUserId === row.id;
            const isTabStop = isSelected || (!selectedUserId && index === 0);
            return (
              <AdminDataTableRow
                key={row.id}
                isSelected={isSelected}
                tabIndex={isTabStop ? 0 : -1}
                onClick={() => onUserSelect(row.id, row)}
                aria-label={`Chọn người dùng ${row.email}`}
              >
                <AdminDataTableCell className="w-10 pr-2">
                  <SelectionIndicator selected={isSelected} />
                </AdminDataTableCell>
                <AdminDataTableCell className="max-w-[14rem] truncate font-medium" title={row.email}>
                  {row.email}
                </AdminDataTableCell>
                <AdminDataTableCell
                  className="hidden max-w-[10rem] truncate lg:table-cell"
                  title={row.display_name || undefined}
                >
                  {row.display_name || "—"}
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <RbacUserStatusBadge status={row.status} />
                </AdminDataTableCell>
                <AdminDataTableCell className="hidden xl:table-cell">
                  <RoleCodesCell roleCodes={row.role_codes} />
                </AdminDataTableCell>
                <AdminDataTableCell className="hidden tabular-nums text-admin-text-secondary 2xl:table-cell">
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
