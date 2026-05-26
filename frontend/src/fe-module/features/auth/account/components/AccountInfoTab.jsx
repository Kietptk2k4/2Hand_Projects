import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../../shared/constants/routes";
import { AccountCard, TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";

function InfoRow({ label, value, children }) {
  return (
    <div className="flex flex-col gap-1 border-b border-outline-variant/50 py-3 last:border-0 sm:flex-row sm:items-center sm:justify-between">
      <span className="text-sm font-medium text-on-surface-variant">{label}</span>
      <div className="text-sm text-on-surface">{children || value || "Chua cap nhat"}</div>
    </div>
  );
}

function StatusBadge({ status }) {
  const styles =
    status === "ACTIVE"
      ? "bg-green-50 text-green-800"
      : status === "PENDING_VERIFICATION"
        ? "bg-amber-50 text-amber-800"
        : "bg-outline-variant/30 text-on-surface-variant";

  return <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${styles}`}>{status}</span>;
}

function formatDateTime(value) {
  if (!value) return null;
  try {
    return new Date(value).toLocaleString("vi-VN");
  } catch {
    return value;
  }
}

const APPEARANCE_LABELS = {
  LIGHT: "Sang",
  DARK: "Toi",
  SYSTEM: "Theo he thong",
};

export function AccountInfoTab({ profile, onTabChange }) {
  const { user, profile: userProfile, settings } = profile || {};

  return (
    <div>
      <TabPanelHeader
        title="Thong tin tai khoan"
        subtitle="Xem thong tin tai khoan, ho so va cai dat tom tat."
      />

      <div className="space-y-6">
        <AccountCard>
          <h2 className="mb-4 text-lg font-semibold text-on-surface">Tai khoan</h2>
          <InfoRow label="Email" value={user?.email} />
          <InfoRow label="Trang thai">
            <StatusBadge status={user?.status || "UNKNOWN"} />
          </InfoRow>
          <InfoRow label="Email da xac thuc">
            {user?.email_verified ? "Da xac thuc" : "Chua xac thuc"}
          </InfoRow>
          <InfoRow label="So dien thoai" value={user?.phone} />
          <InfoRow label="Lan dang nhap gan nhat" value={formatDateTime(user?.last_login_at)} />
        </AccountCard>

        <AccountCard>
          <h2 className="mb-4 text-lg font-semibold text-on-surface">Ho so</h2>
          <div className="mb-4 flex items-center gap-4">
            {userProfile?.avatar_url ? (
              <img
                src={userProfile.avatar_url}
                alt=""
                className="h-16 w-16 rounded-full border border-outline-variant object-cover"
              />
            ) : null}
            <div>
              <p className="font-medium text-on-surface">{userProfile?.display_name || "Chua cap nhat"}</p>
              <button
                type="button"
                onClick={() => onTabChange("edit")}
                className="mt-1 text-sm font-medium text-primary hover:underline"
              >
                Chinh sua ho so
              </button>
            </div>
          </div>
          <InfoRow label="Gioi thieu" value={userProfile?.bio} />
          <InfoRow label="Website">
            {userProfile?.website ? (
              <a href={userProfile.website} className="text-primary hover:underline" target="_blank" rel="noreferrer">
                {userProfile.website}
              </a>
            ) : null}
          </InfoRow>
          <InfoRow label="Mang xa hoi">
            {userProfile?.social_links && Object.keys(userProfile.social_links).length > 0 ? (
              <ul className="space-y-1">
                {Object.entries(userProfile.social_links).map(([key, url]) => (
                  <li key={key}>
                    <a href={url} className="text-primary hover:underline" target="_blank" rel="noreferrer">
                      {key}: {url}
                    </a>
                  </li>
                ))}
              </ul>
            ) : null}
          </InfoRow>
          <InfoRow label="Che do rieng tu">
            {userProfile?.is_private ? "Rieng tu" : "Cong khai"}
          </InfoRow>
        </AccountCard>

        <AccountCard>
          <h2 className="mb-4 text-lg font-semibold text-on-surface">Cai dat (tom tat)</h2>
          <InfoRow
            label="Giao dien"
            value={APPEARANCE_LABELS[settings?.appearance_mode] || settings?.appearance_mode}
          />
          <div className="mt-4 flex flex-wrap gap-3 text-sm">
            <button type="button" onClick={() => onTabChange("settings")} className="font-medium text-primary hover:underline">
              Cap nhat cai dat
            </button>
            <Link to={APP_ROUTES.changePassword} className="font-medium text-primary hover:underline">
              Doi mat khau
            </Link>
          </div>
        </AccountCard>
      </div>
    </div>
  );
}
