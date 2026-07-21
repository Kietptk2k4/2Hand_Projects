import { formatAdminDateTime } from "../../utils/formatAdminDateTime.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCardList,
  AdminSurfaceCard,
} from "../../components/ui";
import { isSystemRole } from "../utils/roleListUtils.js";
import { RoleCodeBadge } from "./RoleCodeBadge.jsx";

function AdminDateTimeCell({ value, title }) {
  const { time, date } = formatAdminDateTime(value);

  return (
    <div className="tabular-nums" title={title}>
      <p className="font-mono text-xs text-admin-text">{time}</p>
      <p className="mt-0.5 text-xs text-admin-text-muted">{date}</p>
    </div>
  );
}

function RolePermissionsAction({ onClick, className = "", variant = "secondary" }) {
  return (
    <AdminFilterButton
      type="button"
      variant={variant}
      className={[
        "min-h-11 border-transparent text-admin-accent hover:bg-admin-accent-soft",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      onClick={onClick}
    >
      <span className="material-symbols-outlined text-base" aria-hidden="true">
        key
      </span>
      Xem quyền
    </AdminFilterButton>
  );
}

function RoleIconAction({ label, icon, onClick, className = "" }) {
  return (
    <AdminFilterButton
      type="button"
      variant="secondary"
      aria-label={label}
      title={label}
      className={[
        "min-h-11 min-w-11 border-transparent px-2 text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      onClick={onClick}
    >
      <span className="material-symbols-outlined text-base" aria-hidden="true">
        {icon}
      </span>
    </AdminFilterButton>
  );
}

function RoleRowActions({
  role,
  canManageRoles,
  onViewRolePermissions,
  onEditRole,
  onDeleteRole,
  className = "",
}) {
  const protectedRole = isSystemRole(role.code);

  return (
    <div
      className={["flex flex-wrap items-center gap-1", className].filter(Boolean).join(" ")}
      onClick={(event) => event.stopPropagation()}
    >
      <RolePermissionsAction
        onClick={() => onViewRolePermissions?.(role.id)}
      />
      {canManageRoles && !protectedRole ? (
        <>
          <RoleIconAction
            label={`Sửa vai trò ${role.code}`}
            icon="edit"
            onClick={() => onEditRole?.(role)}
          />
          <RoleIconAction
            label={`Xóa vai trò ${role.code}`}
            icon="delete"
            className="hover:text-admin-danger"
            onClick={() => onDeleteRole?.(role)}
          />
        </>
      ) : null}
    </div>
  );
}

function RoleMobileCard({
  role,
  canManageRoles,
  onViewRolePermissions,
  onEditRole,
  onDeleteRole,
  showCreatedColumn,
}) {
  const updated = formatAdminDateTime(role.updated_at);
  const created = formatAdminDateTime(role.created_at);
  const showCreated =
    showCreatedColumn &&
    role.created_at &&
    role.updated_at &&
    new Date(role.created_at).getTime() !== new Date(role.updated_at).getTime();
  const protectedRole = isSystemRole(role.code);

  return (
    <AdminSurfaceCard padding="md" className="md:hidden">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <p className="text-sm font-medium text-admin-text">{role.name}</p>
          <p className="mt-2 text-xs text-admin-text-muted">
            {showCreated ? (
              <>
                <span className="font-medium text-admin-text-secondary">Tạo: </span>
                <span className="font-mono tabular-nums">{created.time}</span>
                <span className="mx-1">·</span>
                <span className="tabular-nums">{created.date}</span>
                <span className="mx-2">|</span>
              </>
            ) : null}
            <span className="font-medium text-admin-text-secondary">Cập nhật: </span>
            <span className="font-mono tabular-nums">{updated.time}</span>
            <span className="mx-1">·</span>
            <span className="tabular-nums">{updated.date}</span>
          </p>
        </div>
        <RoleCodeBadge code={role.code} className="shrink-0" />
      </div>

      <RolePermissionsAction
        variant="primary"
        className="mt-4 w-full justify-center px-3 py-2"
        onClick={() => onViewRolePermissions?.(role.id)}
      />

      {canManageRoles && !protectedRole ? (
        <div className="mt-2 grid grid-cols-2 gap-2">
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11 w-full justify-center"
            onClick={() => onEditRole?.(role)}
          >
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              edit
            </span>
            Sửa
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11 w-full justify-center text-admin-danger hover:bg-admin-danger/10"
            onClick={() => onDeleteRole?.(role)}
          >
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              delete
            </span>
            Xóa
          </AdminFilterButton>
        </div>
      ) : null}
    </AdminSurfaceCard>
  );
}

export function RoleListTable({
  roles,
  showCreatedColumn = true,
  onViewRolePermissions,
  onEditRole,
  onDeleteRole,
  canManageRoles = true,
}) {
  if (!roles?.length) {
    return null;
  }

  const tableMinWidth = showCreatedColumn ? "860px" : "740px";

  return (
    <>
      <AdminMobileCardList className="mb-0 space-y-3 md:hidden">
        {roles.map((role) => (
          <RoleMobileCard
            key={role.id}
            role={role}
            canManageRoles={canManageRoles}
            showCreatedColumn={showCreatedColumn}
            onViewRolePermissions={onViewRolePermissions}
            onEditRole={onEditRole}
            onDeleteRole={onDeleteRole}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth={tableMinWidth} ariaLabel="Danh sách vai trò">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Mã</AdminDataTableCell>
            <AdminDataTableCell header>Tên</AdminDataTableCell>
            {showCreatedColumn ? <AdminDataTableCell header>Tạo lúc</AdminDataTableCell> : null}
            <AdminDataTableCell header>{showCreatedColumn ? "Cập nhật" : "Thời gian"}</AdminDataTableCell>
            <AdminDataTableCell header>Thao tác</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {roles.map((role) => {
            const hasDistinctCreated =
              showCreatedColumn &&
              role.created_at &&
              role.updated_at &&
              new Date(role.created_at).getTime() !== new Date(role.updated_at).getTime();

            return (
              <AdminDataTableRow
                key={role.id}
                onClick={() => onViewRolePermissions?.(role.id)}
                className="cursor-pointer transition-colors hover:bg-admin-surface-muted"
              >
                <AdminDataTableCell>
                  <RoleCodeBadge code={role.code} />
                </AdminDataTableCell>
                <AdminDataTableCell className="text-admin-text-secondary">{role.name}</AdminDataTableCell>
                {showCreatedColumn ? (
                  <AdminDataTableCell>
                    {hasDistinctCreated ? (
                      <AdminDateTimeCell value={role.created_at} />
                    ) : (
                      <span className="text-xs text-admin-text-muted">—</span>
                    )}
                  </AdminDataTableCell>
                ) : null}
                <AdminDataTableCell>
                  <AdminDateTimeCell value={role.updated_at} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <RoleRowActions
                    role={role}
                    canManageRoles={canManageRoles}
                    onViewRolePermissions={onViewRolePermissions}
                    onEditRole={onEditRole}
                    onDeleteRole={onDeleteRole}
                  />
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
