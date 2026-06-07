import { useState } from "react";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { UserStatusBadge } from "./EnforcementBadges.jsx";

function VerifiedRow({ verified, label }) {
  return (
    <div>
      <p className="mb-1 text-xs font-semibold text-on-surface-variant">{label}</p>
      <div className="flex items-center gap-2 text-sm text-on-surface">
        {verified ? (
          <span className="text-green-700" aria-hidden>
            ✓
          </span>
        ) : (
          <span className="text-error" aria-hidden>
            ✕
          </span>
        )}
        <span>{verified ? "Đã xác thực" : "Chưa xác thực"}</span>
      </div>
    </div>
  );
}

export function InvestigationProfileCard({ profile }) {
  const [copied, setCopied] = useState(false);

  if (!profile) return null;

  const onCopyId = async () => {
    if (!profile?.user_id) return;
    try {
      await navigator.clipboard.writeText(profile.user_id);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  };

  return (
    <div className="relative overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <div className="absolute -right-8 -top-8 h-32 w-32 rounded-bl-full bg-primary-fixed opacity-20" />

      <div className="relative z-10 mb-6 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-center gap-4">
          <div className="flex h-16 w-16 shrink-0 items-center justify-center overflow-hidden rounded-full border-2 border-surface bg-surface-container-highest">
            {profile.avatar_url ? (
              <img
                src={profile.avatar_url}
                alt=""
                className="h-full w-full object-cover"
              />
            ) : (
              <span className="text-2xl text-tertiary" aria-hidden>
                👤
              </span>
            )}
          </div>
          <div className="min-w-0">
            <h2 className="text-xl font-semibold text-on-surface">
              {profile.display_name || profile.email}
            </h2>
            <p className="text-sm text-on-surface-variant">{profile.email}</p>
            <div className="mt-1 flex flex-wrap items-center gap-2">
              <span className="break-all font-mono text-xs text-on-surface-variant">
                {profile.user_id}
              </span>
              <button
                type="button"
                onClick={onCopyId}
                className="text-xs font-medium text-primary hover:underline"
              >
                {copied ? "Đã sao chép" : "Sao chép UUID"}
              </button>
            </div>
          </div>
        </div>
        <UserStatusBadge status={profile.status} />
      </div>

      <div className="relative z-10 grid gap-6 sm:grid-cols-2">
        <VerifiedRow verified={profile.email_verified} label="Email xác thực" />
        <VerifiedRow verified={profile.phone_verified} label="Số điện thoại xác thực" />
        <div>
          <p className="mb-1 text-xs font-semibold text-on-surface-variant">Đăng nhập cuối</p>
          <p className="text-sm text-on-surface">{formatDateTime(profile.last_login_at)}</p>
        </div>
        <div>
          <p className="mb-1 text-xs font-semibold text-on-surface-variant">Ngày tạo</p>
          <p className="text-sm text-on-surface">{formatDateTime(profile.created_at)}</p>
        </div>
        {profile.bio ? (
          <div className="sm:col-span-2">
            <p className="mb-1 text-xs font-semibold text-on-surface-variant">Giới thiệu</p>
            <p className="text-sm text-on-surface">{profile.bio}</p>
          </div>
        ) : null}
        {profile.website ? (
          <div className="sm:col-span-2">
            <p className="mb-1 text-xs font-semibold text-on-surface-variant">Website</p>
            <a
              href={profile.website}
              target="_blank"
              rel="noreferrer"
              className="text-sm text-primary hover:underline"
            >
              {profile.website}
            </a>
          </div>
        ) : null}
        <div>
          <p className="mb-1 text-xs font-semibold text-on-surface-variant">Quyền riêng tư</p>
          <p className="text-sm text-on-surface">
            {profile.is_private ? "Tài khoản riêng tư" : "Tài khoản công khai"}
          </p>
        </div>
      </div>
    </div>
  );
}
