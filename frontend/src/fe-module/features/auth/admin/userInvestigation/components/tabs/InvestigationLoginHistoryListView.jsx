import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import { AdminFilterButton, AdminStatusBadge, AdminSurfaceCard } from "../../../components/ui";
import { getLoginSuccessVariant } from "../ui/investigationStatusVariants.js";

const LOGIN_METHOD_LABELS = {
  EMAIL: "Email",
  GOOGLE: "Google",
  FACEBOOK: "Facebook",
};

function HistoryRow({ item }) {
  const success = Boolean(item.success);

  return (
    <li className="flex flex-col gap-3 border-b border-admin-border-subtle py-4 last:border-0 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-medium text-admin-text">
            {LOGIN_METHOD_LABELS[item.login_method] || item.login_method || "Đăng nhập"}
          </span>
          <AdminStatusBadge variant={getLoginSuccessVariant(success)}>
            {success ? "Thành công" : "Thất bại"}
          </AdminStatusBadge>
        </div>
        <p className="mt-1 text-sm text-admin-text-secondary">{formatDateTime(item.created_at)}</p>
        <p className="mt-1 text-sm text-admin-text-secondary">IP: {item.ip_address || "—"}</p>
        {item.user_agent ? (
          <p className="mt-1 break-all text-xs text-admin-text-muted">{item.user_agent}</p>
        ) : null}
      </div>
    </li>
  );
}

export function InvestigationLoginHistoryListView({
  items,
  hasNext,
  loadMoreStatus,
  onLoadMore,
}) {
  if (items.length === 0) {
    return (
      <AdminSurfaceCard padding="lg">
        <p className="text-sm text-admin-text-muted">Chưa có lịch sử đăng nhập.</p>
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard padding="md">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-2 border-b border-admin-border pb-3">
        <p className="text-sm font-medium text-admin-text">
          {items.length} lần đăng nhập
        </p>
      </div>
      <ul>
        {items.map((item, index) => (
          <HistoryRow key={`${item.created_at}-${item.ip_address}-${index}`} item={item} />
        ))}
      </ul>

      {hasNext ? (
        <div className="mt-6 flex justify-center border-t border-admin-border pt-6">
          <AdminFilterButton
            type="button"
            variant="primary"
            className="min-w-[10rem]"
            disabled={loadMoreStatus === "loading"}
            onClick={onLoadMore}
          >
            {loadMoreStatus === "loading" ? "Đang tải…" : "Tải thêm"}
          </AdminFilterButton>
        </div>
      ) : null}
    </AdminSurfaceCard>
  );
}
