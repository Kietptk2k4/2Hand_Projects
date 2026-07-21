import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminSurfaceCard } from "../../components/ui";
import { InvestigationDetailRow } from "./ui/InvestigationDetailRow.jsx";
import { UserStatusBadge } from "./EnforcementBadges.jsx";

function VerifiedRow({ verified, label }) {
  return (
    <InvestigationDetailRow label={label}>
      <div className="flex items-center gap-2">
        <span
          className={[
            "material-symbols-outlined text-base",
            verified ? "text-admin-success" : "text-admin-danger",
          ].join(" ")}
          aria-hidden="true"
        >
          {verified ? "verified" : "cancel"}
        </span>
        <span>{verified ? "Đã xác thực" : "Chưa xác thực"}</span>
      </div>
    </InvestigationDetailRow>
  );
}

function getInitials(profile) {
  const source = profile?.display_name?.trim() || profile?.email?.trim() || "";
  if (!source) return "?";
  const parts = source.split(/[\s@._-]+/).filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }
  return source.slice(0, 2).toUpperCase();
}

export function InvestigationProfileCardView({ profile, copied, onCopyId }) {
  if (!profile) return null;

  return (
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <div className="flex flex-col gap-4 border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 sm:flex-row sm:items-start sm:justify-between lg:px-5">
        <div className="flex items-center gap-4">
          <div className="flex h-14 w-14 shrink-0 items-center justify-center overflow-hidden rounded-2xl border border-admin-border bg-admin-accent-soft">
            {profile.avatar_url ? (
              <img
                src={profile.avatar_url}
                alt=""
                className="h-full w-full object-cover"
              />
            ) : (
              <span className="text-sm font-semibold text-admin-accent-strong" aria-hidden="true">
                {getInitials(profile)}
              </span>
            )}
          </div>
          <div className="min-w-0">
            <h2 className="text-lg font-semibold tracking-tight text-balance text-admin-text">
              {profile.display_name || profile.email}
            </h2>
            <p className="mt-0.5 text-sm text-admin-text-secondary">{profile.email}</p>
            <div className="mt-1.5 flex flex-wrap items-center gap-2">
              <span className="break-all font-mono text-[11px] text-admin-text-muted">
                {profile.user_id}
              </span>
              <button
                type="button"
                onClick={onCopyId}
                className="text-xs font-medium text-admin-accent transition-colors hover:text-admin-accent-strong focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
              >
                {copied ? "Đã sao chép" : "Sao chép UUID"}
              </button>
            </div>
          </div>
        </div>
        <UserStatusBadge status={profile.status} />
      </div>

      <div className="grid gap-5 px-4 py-4 sm:grid-cols-2 lg:px-5 lg:py-5">
        <VerifiedRow verified={profile.email_verified} label="Email xác thực" />
        <VerifiedRow verified={profile.phone_verified} label="Số điện thoại xác thực" />
        <InvestigationDetailRow label="Đăng nhập cuối">
          <span className="tabular-nums">{formatDateTime(profile.last_login_at)}</span>
        </InvestigationDetailRow>
        <InvestigationDetailRow label="Ngày tạo">
          <span className="tabular-nums">{formatDateTime(profile.created_at)}</span>
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
              className="text-admin-accent transition-colors hover:text-admin-accent-strong hover:underline"
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
