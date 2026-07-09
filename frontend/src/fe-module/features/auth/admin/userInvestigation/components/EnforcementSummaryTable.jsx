import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminSurfaceCard } from "../../components/ui";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
} from "../../components/ui";
import {
  EnforcementActionBadge,
  EnforcementStatusBadge,
} from "./EnforcementBadges.jsx";

export function EnforcementSummaryTable({ enforcements = [] }) {
  return (
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <div className="flex items-center justify-between border-b border-admin-border bg-admin-surface-muted px-4 py-3 sm:px-6">
        <h3 className="text-sm font-semibold text-admin-text">Thực thi hiện tại</h3>
        <span className="rounded-md bg-admin-surface px-2 py-1 text-xs font-semibold text-admin-text-secondary">
          {enforcements.length} bản ghi
        </span>
      </div>

      {enforcements.length === 0 ? (
        <div className="p-6">
          <p className="text-sm text-admin-text-muted">Không có enforcement đang hiệu lực.</p>
        </div>
      ) : (
        <AdminDataTable minWidth="640px" ariaLabel="Thực thi hiện tại">
          <AdminDataTableHead>
            <AdminDataTableRow>
              <AdminDataTableCell header>Mã thực thi</AdminDataTableCell>
              <AdminDataTableCell header>Hành động</AdminDataTableCell>
              <AdminDataTableCell header>Lý do</AdminDataTableCell>
              <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
              <AdminDataTableCell header>Hết hạn</AdminDataTableCell>
            </AdminDataTableRow>
          </AdminDataTableHead>
          <AdminDataTableBody>
            {enforcements.map((item) => (
              <AdminDataTableRow key={item.enforcement_id}>
                <AdminDataTableCell className="font-mono text-xs text-admin-text-muted">
                  {item.enforcement_id?.slice(0, 8)}…
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <EnforcementActionBadge actionType={item.action_type} />
                </AdminDataTableCell>
                <AdminDataTableCell>{item.reason_code || "—"}</AdminDataTableCell>
                <AdminDataTableCell>
                  <EnforcementStatusBadge
                    status={item.status}
                    possiblyExpired={item.possibly_expired}
                  />
                </AdminDataTableCell>
                <AdminDataTableCell className="text-admin-text-secondary">
                  {item.expires_at ? formatDateTime(item.expires_at) : "Không giới hạn"}
                </AdminDataTableCell>
              </AdminDataTableRow>
            ))}
          </AdminDataTableBody>
        </AdminDataTable>
      )}
    </AdminSurfaceCard>
  );
}
