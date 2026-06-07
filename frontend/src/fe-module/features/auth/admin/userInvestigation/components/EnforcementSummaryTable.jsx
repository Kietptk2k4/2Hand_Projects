import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState } from "../../../../../shared/ui/PageState.jsx";
import {
  EnforcementActionBadge,
  EnforcementStatusBadge,
} from "./EnforcementBadges.jsx";

export function EnforcementSummaryTable({ enforcements = [] }) {
  return (
    <AccountCard className="!p-0 overflow-hidden">
      <div className="flex items-center justify-between border-b border-outline-variant bg-surface-container-low px-6 py-3">
        <h3 className="text-sm font-semibold text-on-surface">Thực thi hiện tại</h3>
        <span className="rounded-md bg-surface-container-highest px-2 py-1 text-xs font-semibold text-on-surface">
          {enforcements.length} bản ghi
        </span>
      </div>

      {enforcements.length === 0 ? (
        <div className="p-6">
          <EmptyState message="Không có enforcement đang hiệu lực." />
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px] border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-outline-variant bg-surface-container-lowest text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
                <th className="px-6 py-3">Mã thực thi</th>
                <th className="px-6 py-3">Hành động</th>
                <th className="px-6 py-3">Lý do</th>
                <th className="px-6 py-3">Trạng thái</th>
                <th className="px-6 py-3">Hết hạn</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-outline-variant/50">
              {enforcements.map((item) => (
                <tr key={item.enforcement_id} className="hover:bg-surface-container-low/50">
                  <td className="px-6 py-3 font-mono text-xs text-on-surface-variant">
                    {item.enforcement_id?.slice(0, 8)}…
                  </td>
                  <td className="px-6 py-3">
                    <EnforcementActionBadge actionType={item.action_type} />
                  </td>
                  <td className="px-6 py-3">{item.reason_code || "—"}</td>
                  <td className="px-6 py-3">
                    <EnforcementStatusBadge
                      status={item.status}
                      possiblyExpired={item.possibly_expired}
                    />
                  </td>
                  <td className="px-6 py-3 text-on-surface-variant">
                    {item.expires_at ? formatDateTime(item.expires_at) : "Không giới hạn"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </AccountCard>
  );
}
