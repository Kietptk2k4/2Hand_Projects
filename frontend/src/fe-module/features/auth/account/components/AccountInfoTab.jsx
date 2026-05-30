import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../../shared/constants/routes";
import { getUserStatusLabel, NOT_UPDATED } from "../../constants/authUiStrings";
import { AccountCard, TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";

function InfoRow({ label, value, children }) {
  return (
    <div className="flex flex-col gap-1 border-b border-outline-variant/50 py-3 last:border-0 sm:flex-row sm:items-center sm:justify-between">
      <span className="text-sm font-medium text-on-surface-variant">{label}</span>
      <div className="text-sm text-on-surface">{children || value || NOT_UPDATED}</div>
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

  return (
    <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${styles}`}>
      {getUserStatusLabel(status)}
    </span>
  );
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
  LIGHT: "Sáng",
  DARK: "Tối",
  SYSTEM: "Theo hệ thống",
};

export function AccountInfoTab({ profile, onTabChange }) {
  const { user, profile: userProfile, settings } = profile || {};

  return (
    <div>
      <TabPanelHeader
        title="Thông tin tài khoản"
        subtitle="Xem thông tin tài khoản, hồ sơ và cài đặt tóm tắt."
      />

      <div className="space-y-6">
        <AccountCard>
          <h2 className="mb-4 text-lg font-semibold text-on-surface">Tài khoản</h2>
          <InfoRow label="Email" value={user?.email} />
          <InfoRow label="Trạng thái">
            <StatusBadge status={user?.status || "UNKNOWN"} />
          </InfoRow>
          <InfoRow label="Email đã xác thực">
            {user?.email_verified ? "Đã xác thực" : "Chưa xác thực"}
          </InfoRow>
          <InfoRow label="Số điện thoại" value={user?.phone} />
          <InfoRow label="Lần đăng nhập gần nhất" value={formatDateTime(user?.last_login_at)} />
        </AccountCard>

        <AccountCard>
          <h2 className="mb-4 text-lg font-semibold text-on-surface">Hồ sơ</h2>
          <div className="mb-4 flex items-center gap-4">
            {userProfile?.avatar_url ? (
              <img
                src={userProfile.avatar_url}
                alt=""
                className="h-16 w-16 rounded-full border border-outline-variant object-cover"
              />
            ) : null}
            <div>
              <p className="font-medium text-on-surface">{userProfile?.display_name || NOT_UPDATED}</p>
              <button
                type="button"
                onClick={() => onTabChange("edit")}
                className="mt-1 text-sm font-medium text-primary hover:underline"
              >
                Chỉnh sửa hồ sơ
              </button>
            </div>
          </div>
          <InfoRow label="Giới thiệu" value={userProfile?.bio} />
          <InfoRow label="Website">
            {userProfile?.website ? (
              <a href={userProfile.website} className="text-primary hover:underline" target="_blank" rel="noreferrer">
                {userProfile.website}
              </a>
            ) : null}
          </InfoRow>
          <InfoRow label="Mạng xã hội">
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
          <InfoRow label="Chế độ riêng tư">
            {userProfile?.is_private ? "Riêng tư" : "Công khai"}
          </InfoRow>
        </AccountCard>

        <AccountCard>
          <h2 className="mb-4 text-lg font-semibold text-on-surface">Cài đặt (tóm tắt)</h2>
          <InfoRow
            label="Giao diện"
            value={APPEARANCE_LABELS[settings?.appearance_mode] || settings?.appearance_mode}
          />
          <div className="mt-4 flex flex-wrap gap-3 text-sm">
            <button type="button" onClick={() => onTabChange("settings")} className="font-medium text-primary hover:underline">
              Cập nhật cài đặt
            </button>
            <Link to={APP_ROUTES.accountPassword} className="font-medium text-primary hover:underline">
              Đổi mật khẩu
            </Link>
          </div>
        </AccountCard>

        <AccountCard>
          <h2 className="mb-4 text-lg font-semibold text-on-surface">Bảo mật</h2>
          <p className="text-sm text-on-surface-variant">
            Xem phiên đăng nhập đang hoạt động và lịch sử đăng nhập của tài khoản.
          </p>
          <div className="mt-4">
            <Link to={APP_ROUTES.accountSecurity} className="font-medium text-primary hover:underline">
              Bảo mật tài khoản
            </Link>
          </div>
        </AccountCard>
      </div>
    </div>
  );
}
