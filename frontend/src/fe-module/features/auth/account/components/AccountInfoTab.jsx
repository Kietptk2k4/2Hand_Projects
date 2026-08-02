import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../../shared/constants/routes";
import { getUserStatusLabel, NOT_UPDATED } from "../../constants/authUiStrings";
import { AccountCard, TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";

function InfoRow({ label, value, children }) {
  return (
    <div className="flex flex-col gap-1 border-b border-outline-variant/30 py-3 last:border-0 sm:flex-row sm:items-center sm:justify-between">
      <span className="text-xs font-bold text-on-surface-variant/80">{label}</span>
      <div className="text-sm font-medium text-on-surface">{children || value || NOT_UPDATED}</div>
    </div>
  );
}

function StatusBadge({ status }) {
  const styles =
    status === "ACTIVE"
      ? "bg-emerald-500/10 text-emerald-600 border border-emerald-500/20"
      : status === "PENDING_VERIFICATION"
        ? "bg-amber-500/10 text-amber-600 border border-amber-500/20"
        : "bg-outline-variant/30 text-on-surface-variant";

  return (
    <span className={`inline-flex rounded-full px-3 py-0.5 text-xs font-bold ${styles}`}>
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
          <h2 className="mb-3 text-lg font-extrabold text-on-surface leading-tight">Tài khoản</h2>
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
          <h2 className="mb-3 text-lg font-extrabold text-on-surface leading-tight">Hồ sơ</h2>
          <div className="mb-4 flex items-center gap-4">
            {userProfile?.avatar_url ? (
              <img
                src={userProfile.avatar_url}
                alt=""
                className="h-14 w-14 rounded-full border-2 border-surface-container-low object-cover shadow-2xs"
              />
            ) : null}
            <div>
              <p className="font-bold text-on-surface text-base">{userProfile?.display_name || NOT_UPDATED}</p>
              <button
                type="button"
                onClick={() => onTabChange("edit")}
                className="mt-0.5 text-xs font-bold text-sky-500 hover:underline"
              >
                Chỉnh sửa hồ sơ
              </button>
            </div>
          </div>
          <InfoRow label="Giới thiệu" value={userProfile?.bio} />
          <InfoRow label="Website">
            {userProfile?.website ? (
              <a href={userProfile.website} className="text-sky-500 hover:underline font-medium" target="_blank" rel="noreferrer">
                {userProfile.website}
              </a>
            ) : null}
          </InfoRow>
          <InfoRow label="Mạng xã hội">
            {userProfile?.social_links && Object.keys(userProfile.social_links).length > 0 ? (
              <ul className="space-y-1">
                {Object.entries(userProfile.social_links).map(([key, url]) => (
                  <li key={key}>
                    <a href={url} className="text-sky-500 hover:underline font-medium" target="_blank" rel="noreferrer">
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
          <h2 className="mb-3 text-lg font-extrabold text-on-surface leading-tight">Cài đặt (tóm tắt)</h2>
          <InfoRow
            label="Giao diện"
            value={APPEARANCE_LABELS[settings?.appearance_mode] || settings?.appearance_mode}
          />
          <div className="mt-4 flex flex-wrap gap-4 text-xs font-bold">
            <button type="button" onClick={() => onTabChange("settings")} className="text-sky-500 hover:underline">
              Cập nhật cài đặt
            </button>
            <Link to={APP_ROUTES.accountPassword} className="text-sky-500 hover:underline">
              Đổi mật khẩu
            </Link>
          </div>
        </AccountCard>

        <AccountCard>
          <h2 className="mb-3 text-lg font-extrabold text-on-surface leading-tight">Bảo mật</h2>
          <p className="text-sm text-on-surface-variant/70">
            Xem phiên đăng nhập đang hoạt động và lịch sử đăng nhập của tài khoản.
          </p>
          <div className="mt-4">
            <Link to={APP_ROUTES.accountSecurity} className="text-xs font-bold text-sky-500 hover:underline">
              Bảo mật tài khoản →
            </Link>
          </div>
        </AccountCard>
      </div>
    </div>
  );
}
