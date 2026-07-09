import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
  AdminSurfaceCard,
} from "../../components/ui";

function RoleMobileCard({ role, onViewRolePermissions }) {
  return (
    <AdminMobileCard ariaLabel={`Vai trò ${role.code}`}>
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="font-medium text-admin-text">{role.code}</p>
          <p className="mt-0.5 text-sm text-admin-text-secondary">{role.name}</p>
        </div>
      </div>
      <p className="mt-2 text-xs text-admin-text-muted">
        Tạo: {formatDateTime(role.created_at)} · Cập nhật: {formatDateTime(role.updated_at)}
      </p>
      <div className="mt-3 border-t border-admin-border-subtle pt-3">
        <AdminFilterButton
          type="button"
          variant="secondary"
          className="min-h-11 w-full border-transparent text-admin-accent hover:bg-admin-accent-soft sm:w-auto"
          onClick={() => onViewRolePermissions?.(role.id)}
        >
          Xem permission
        </AdminFilterButton>
      </div>
    </AdminMobileCard>
  );
}

export function RoleListTable({ roles, onViewRolePermissions }) {
  if (!roles?.length) {
    return (
      <AdminSurfaceCard padding="lg" className="text-center">
        <p className="text-sm text-admin-text-muted">Không có vai trò nào.</p>
      </AdminSurfaceCard>
    );
  }

  return (
    <>
      <AdminMobileCardList className="mb-0 md:hidden">
        {roles.map((role) => (
          <RoleMobileCard key={role.id} role={role} onViewRolePermissions={onViewRolePermissions} />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="640px" ariaLabel="Danh sách vai trò">
      <AdminDataTableHead>
        <AdminDataTableRow>
          <AdminDataTableCell header>Code</AdminDataTableCell>
          <AdminDataTableCell header>Tên</AdminDataTableCell>
          <AdminDataTableCell header>Tạo lúc</AdminDataTableCell>
          <AdminDataTableCell header>Cập nhật</AdminDataTableCell>
          <AdminDataTableCell header>Thao tác</AdminDataTableCell>
        </AdminDataTableRow>
      </AdminDataTableHead>
      <AdminDataTableBody>
        {roles.map((role) => (
          <AdminDataTableRow key={role.id}>
            <AdminDataTableCell className="font-medium">{role.code}</AdminDataTableCell>
            <AdminDataTableCell className="text-admin-text-secondary">{role.name}</AdminDataTableCell>
            <AdminDataTableCell className="text-admin-text-secondary">
              {formatDateTime(role.created_at)}
            </AdminDataTableCell>
            <AdminDataTableCell className="text-admin-text-secondary">
              {formatDateTime(role.updated_at)}
            </AdminDataTableCell>
            <AdminDataTableCell>
              <AdminFilterButton
                type="button"
                variant="secondary"
                className="min-h-11 border-transparent px-2 py-1 text-admin-accent hover:bg-admin-accent-soft"
                onClick={() => onViewRolePermissions?.(role.id)}
              >
                Xem permission
              </AdminFilterButton>
            </AdminDataTableCell>
          </AdminDataTableRow>
        ))}
      </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
