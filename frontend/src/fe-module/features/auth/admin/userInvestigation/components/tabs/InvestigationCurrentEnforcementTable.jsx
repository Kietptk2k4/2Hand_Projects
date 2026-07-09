import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCardList,
  AdminSurfaceCard,
} from "../../../components/ui";
import {
  EnforcementActionBadge,
  EnforcementStatusBadge,
} from "../EnforcementBadges.jsx";

function CurrentEnforcementMobileCard({ item, canRevoke, onRevoke }) {
  return (
    <AdminSurfaceCard padding="md" className="shadow-[var(--shadow-admin-surface)]">
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="font-mono text-xs text-admin-text-muted">
            {item.enforcement_id?.slice(0, 8)}…
          </p>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <EnforcementActionBadge actionType={item.action_type} />
            <span className="text-sm font-medium text-admin-text">{item.reason_code}</span>
          </div>
        </div>
        <EnforcementStatusBadge status="ACTIVE" possiblyExpired={item.possibly_expired} />
      </div>
      <p className="mt-2 text-sm text-admin-text-secondary">{item.description || "—"}</p>
      <dl className="mt-3 grid gap-2 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-admin-text-muted">Thời hạn</dt>
          <dd className="text-admin-text">
            {item.expires_at ? formatDateTime(item.expires_at) : "Không giới hạn"}
          </dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Người xử lý</dt>
          <dd className="font-mono text-xs text-admin-text">
            {item.enforced_by?.slice(0, 8) || "—"}…
          </dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="text-admin-text-muted">Tạo lúc</dt>
          <dd className="text-admin-text">{formatDateTime(item.created_at)}</dd>
        </div>
      </dl>
      {canRevoke ? (
        <div className="mt-4 border-t border-admin-border-subtle pt-3">
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11 w-full border-admin-danger/30 text-admin-danger hover:bg-admin-danger-soft"
            onClick={() => onRevoke?.(item)}
          >
            Thu hồi
          </AdminFilterButton>
        </div>
      ) : null}
    </AdminSurfaceCard>
  );
}

export function InvestigationCurrentEnforcementTable({
  enforcements,
  canRevoke,
  onRevoke,
}) {
  if (enforcements.length === 0) {
    return (
      <div className="p-6">
        <p className="text-sm text-admin-text-muted">Không có enforcement đang hiệu lực.</p>
      </div>
    );
  }

  return (
    <>
      <AdminMobileCardList className="p-4">
        {enforcements.map((item) => (
          <CurrentEnforcementMobileCard
            key={item.enforcement_id}
            item={item}
            canRevoke={canRevoke}
            onRevoke={onRevoke}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="900px" ariaLabel="Danh sách thực thi hiện tại">
      <AdminDataTableHead>
        <AdminDataTableRow>
          <AdminDataTableCell header>ID thực thi</AdminDataTableCell>
          <AdminDataTableCell header>Loại / Mã lý do</AdminDataTableCell>
          <AdminDataTableCell header>Chi tiết</AdminDataTableCell>
          <AdminDataTableCell header>Thời hạn</AdminDataTableCell>
          <AdminDataTableCell header className="hidden md:table-cell">
            Người xử lý
          </AdminDataTableCell>
          <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
          <AdminDataTableCell header className="text-right">
            Hành động
          </AdminDataTableCell>
        </AdminDataTableRow>
      </AdminDataTableHead>
      <AdminDataTableBody>
        {enforcements.map((item) => (
          <AdminDataTableRow key={item.enforcement_id}>
            <AdminDataTableCell className="font-mono text-xs text-admin-text-muted">
              {item.enforcement_id?.slice(0, 8)}…
            </AdminDataTableCell>
            <AdminDataTableCell>
              <div className="flex flex-col items-start gap-1">
                <EnforcementActionBadge actionType={item.action_type} />
                <span className="text-sm font-medium text-admin-text">{item.reason_code}</span>
              </div>
            </AdminDataTableCell>
            <AdminDataTableCell className="max-w-xs">
              <p className="truncate text-sm text-admin-text" title={item.description}>
                {item.description || "—"}
              </p>
              <p className="mt-1 text-xs text-admin-text-muted">
                Tạo: {formatDateTime(item.created_at)}
              </p>
            </AdminDataTableCell>
            <AdminDataTableCell className="text-sm text-admin-text">
              {item.expires_at ? formatDateTime(item.expires_at) : "Không giới hạn"}
            </AdminDataTableCell>
            <AdminDataTableCell className="hidden font-mono text-xs text-admin-text-muted md:table-cell">
              {item.enforced_by?.slice(0, 8) || "—"}…
            </AdminDataTableCell>
            <AdminDataTableCell>
              <EnforcementStatusBadge status="ACTIVE" possiblyExpired={item.possibly_expired} />
            </AdminDataTableCell>
            <AdminDataTableCell className="text-right">
              {canRevoke ? (
                <AdminFilterButton
                  type="button"
                  variant="secondary"
                  className="min-h-11 border-transparent text-admin-danger hover:border-admin-danger/30 hover:bg-admin-danger-soft"
                  onClick={() => onRevoke?.(item)}
                >
                  Thu hồi
                </AdminFilterButton>
              ) : null}
            </AdminDataTableCell>
          </AdminDataTableRow>
        ))}
      </AdminDataTableBody>
    </AdminDataTable>
    </>
  );
}

export function InvestigationCurrentEnforcementPanel({
  enforcements,
  canRevoke,
  onRevoke,
}) {
  return (
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <div className="flex items-center justify-between border-b border-admin-border bg-admin-surface-muted px-4 py-3 sm:px-6">
        <h3 className="text-sm font-semibold text-admin-text">
          Danh sách thực thi ({enforcements.length})
        </h3>
      </div>
      <InvestigationCurrentEnforcementTable
        enforcements={enforcements}
        canRevoke={canRevoke}
        onRevoke={onRevoke}
      />
    </AdminSurfaceCard>
  );
}
