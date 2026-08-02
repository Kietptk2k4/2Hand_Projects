import { AccountCard } from "../../../shared/ui/auth/authUi.jsx";
import { useDeviceTokens } from "../hooks/useDeviceTokens";

function formatDateTime(value) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "-";
  return date.toLocaleString("vi-VN");
}

export function NotificationDeviceTokensSection() {
  const { items, status, errorMessage, revokingId, reload, revokeToken, localDeviceToken } =
    useDeviceTokens();

  const handleRevokeLocal = async () => {
    if (!localDeviceToken) return;
    try {
      await revokeToken(localDeviceToken);
    } catch (error) {
      window.alert(error?.message || "Không thể thu hồi thiết bị.");
    }
  };

  return (
    <AccountCard className="mt-6">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div>
          <h2 className="text-base font-extrabold text-on-surface">Thiết bị nhận push</h2>
          <p className="mt-1 text-xs text-on-surface-variant/70">
            Quản lý các thiết bị đã đăng ký nhận thông báo đẩy (FCM).
          </p>
        </div>
        <button
          type="button"
          onClick={reload}
          className="rounded-full border border-outline-variant/60 bg-surface-container-lowest px-4 py-1.5 text-xs font-bold text-on-surface hover:bg-surface-container-low transition-all"
        >
          Làm mới
        </button>
      </div>

      {status === "loading" ? (
        <p className="text-xs text-on-surface-variant/70">Đang tải danh sách thiết bị...</p>
      ) : null}

      {status === "error" ? <p className="text-xs text-error font-medium">{errorMessage}</p> : null}

      {status === "ready" && items.length === 0 ? (
        <p className="text-xs text-on-surface-variant/70">
          Chưa có thiết bị nào được đăng ký. Ứng dụng mobile sẽ tự động đăng ký khi bật push.
        </p>
      ) : null}

      {items.length > 0 ? (
        <ul className="divide-y divide-outline-variant/40 rounded-xl border border-outline-variant/40 overflow-hidden">
          {items.map((item) => (
            <li key={item.id} className="flex items-center justify-between gap-4 px-4 py-3 bg-surface-container-lowest">
              <div className="min-w-0">
                <p className="text-xs font-bold text-on-surface">{item.deviceType}</p>
                <p className="font-mono text-[11px] text-on-surface-variant/70">{item.maskedDeviceToken}</p>
                <p className="mt-0.5 text-[11px] text-on-surface-variant/60">
                  Cập nhật: {formatDateTime(item.updatedAt)}
                </p>
              </div>
              <span
                className={[
                  "shrink-0 rounded-full px-2.5 py-0.5 text-[11px] font-bold",
                  item.active ? "bg-emerald-500/10 text-emerald-600 border border-emerald-500/20" : "bg-surface-container-low text-on-surface-variant/70",
                ].join(" ")}
              >
                {item.active ? "Đang hoạt động" : "Đã thu hồi"}
              </span>
            </li>
          ))}
        </ul>
      ) : null}

      {localDeviceToken ? (
        <div className="mt-4 rounded-xl border border-outline-variant/40 bg-surface-container-low/40 px-4 py-3">
          <p className="text-xs text-on-surface font-medium">
            Thiết bị hiện tại có token đã lưu trên trình duyệt này.
          </p>
          <button
            type="button"
            onClick={handleRevokeLocal}
            disabled={Boolean(revokingId)}
            className="mt-2.5 rounded-full border border-error/40 px-4 py-1.5 text-xs font-bold text-error hover:bg-error-container/40 disabled:opacity-60 transition-all"
          >
            {revokingId ? "Đang thu hồi..." : "Thu hồi thiết bị này"}
          </button>
        </div>
      ) : null}
    </AccountCard>
  );
}