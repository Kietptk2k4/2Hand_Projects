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
      window.alert(error?.message || "Khong the thu hoi thiet bi.");
    }
  };

  return (
    <AccountCard className="mt-6">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div>
          <h2 className="text-lg font-semibold text-on-surface">Thiet bi nhan push</h2>
          <p className="mt-1 text-sm text-on-surface-variant">
            Quan ly cac thiet bi da dang ky nhan thong bao day (FCM).
          </p>
        </div>
        <button
          type="button"
          onClick={reload}
          className="rounded-lg border border-outline-variant px-3 py-2 text-sm font-medium text-on-surface hover:bg-surface-container-low"
        >
          Lam moi
        </button>
      </div>

      {status === "loading" ? (
        <p className="text-sm text-on-surface-variant">Dang tai danh sach thiet bi...</p>
      ) : null}

      {status === "error" ? <p className="text-sm text-error">{errorMessage}</p> : null}

      {status === "ready" && items.length === 0 ? (
        <p className="text-sm text-on-surface-variant">
          Chua co thiet bi nao duoc dang ky. Ung dung mobile se tu dong dang ky khi bat push.
        </p>
      ) : null}

      {items.length > 0 ? (
        <ul className="divide-y divide-outline-variant/60 rounded-lg border border-outline-variant">
          {items.map((item) => (
            <li key={item.id} className="flex items-center justify-between gap-4 px-4 py-3">
              <div className="min-w-0">
                <p className="text-sm font-medium text-on-surface">{item.deviceType}</p>
                <p className="font-mono text-xs text-on-surface-variant">{item.maskedDeviceToken}</p>
                <p className="mt-1 text-xs text-on-surface-variant">
                  Cap nhat: {formatDateTime(item.updatedAt)}
                </p>
              </div>
              <span
                className={[
                  "shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold",
                  item.active ? "bg-primary/10 text-primary" : "bg-surface-container-low text-on-surface-variant",
                ].join(" ")}
              >
                {item.active ? "Dang hoat dong" : "Da thu hoi"}
              </span>
            </li>
          ))}
        </ul>
      ) : null}

      {localDeviceToken ? (
        <div className="mt-4 rounded-lg border border-outline-variant bg-surface-container-low px-4 py-3">
          <p className="text-sm text-on-surface">
            Thiet bi hien tai co token da luu tren trinh duyet nay.
          </p>
          <button
            type="button"
            onClick={handleRevokeLocal}
            disabled={Boolean(revokingId)}
            className="mt-3 rounded-lg border border-error/40 px-3 py-2 text-sm font-medium text-error hover:bg-red-50 disabled:opacity-60"
          >
            {revokingId ? "Dang thu hoi..." : "Thu hoi thiet bi nay"}
          </button>
        </div>
      ) : null}
    </AccountCard>
  );
}