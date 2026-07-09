import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminSurfaceCard } from "../../components/ui";
import { InvestigationDetailRow } from "./ui/InvestigationDetailRow.jsx";
import { UserStatusBadge } from "./EnforcementBadges.jsx";

function VerifiedRow({ verified, label }) {
  return (
    <InvestigationDetailRow label={label}>
      <div className="flex items-center gap-2">
        {verified ? (
          <span className="text-admin-success" aria-hidden>
            ✓
          </span>
        ) : (
          <span className="text-admin-danger" aria-hidden>
            ✕
          </span>
        )}
        <span>{verified ? "Đã xác thực" : "Chưa xác thực"}</span>
      </div>
    </InvestigationDetailRow>
  );
}

export function InvestigationProfileCardView({ profile, copied, onCopyId }) {
  if (!profile) return null;

  return (
    <AdminSurfaceCard padding="lg">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-center gap-4">
          <div className="flex h-16 w-16 shrink-0 items-center justify-center overflow-hidden rounded-full border-2 border-admin-border bg-admin-surface-muted">
            {profile.avatar_url ? (
              <img src={profile.avatar_url} alt="" className="h-full w-full object-cover" />
            ) : (
              <span className="text-2xl text-admin-text-muted" aria-hidden>
                👤
              </span>
            )}
          </div>
          <div className="min-w-0">
            <h2 className="text-xl font-semibold text-admin-text">
              {profile.display_name || profile.email}
            </h2>
            <p className="text-sm text-admin-text-secondary">{profile.email}</p>
            <div className="mt-1 flex flex-wrap items-center gap-2">
              <span className="break-all font-mono text-xs text-admin-text-muted">
                {profile.user_id}
              </span>
              <button
                type="button"
                onClick={onCopyId}
                className="text-xs font-medium text-admin-accent hover:underline"
              >
                {copied ? "Đã sao chép" : "Sao chép UUID"}
              </button>
            </div>
          </div>
        </div>
        <UserStatusBadge status={profile.status} />
      </div>

      <div className="grid gap-6 sm:grid-cols-2">
        <VerifiedRow verified={profile.email_verified} label="Email xác thực" />
        <VerifiedRow verified={profile.phone_verified} label="Số điện thoại xác thực" />
        <InvestigationDetailRow label="Đăng nhập cuối">
          {formatDateTime(profile.last_login_at)}
        </InvestigationDetailRow>
        <InvestigationDetailRow label="Ngày tạo">
          {formatDateTime(profile.created_at)}
        </InvestigationDetailRow>
        {profile.bio ? (
          <InvestigationDetailRow label="Giới thiệu" className="sm:col-span-2">
            {profile.bio}
          </InvestigationDetailRow>
        ) : null}
        {profile.website ? (
          <InvestigationDetailRow label="Website" className="sm:col-span-2">
            <a
              href={profile.website}
              target="_blank"
              rel="noreferrer"
              className="text-admin-accent hover:underline"
            >
              {profile.website}
            </a>
          </InvestigationDetailRow>
        ) : null}
        <InvestigationDetailRow label="Quyền riêng tư">
          {profile.is_private ? "Tài khoản riêng tư" : "Tài khoản công khai"}
        </InvestigationDetailRow>
      </div>
    </AdminSurfaceCard>
  );
}
