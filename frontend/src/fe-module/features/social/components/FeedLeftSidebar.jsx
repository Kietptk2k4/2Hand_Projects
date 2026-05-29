import { Link } from "react-router-dom";
import { useAccountProfile } from "../../auth/account/hooks/useAccountProfile";
import { APP_ROUTES } from "../../../shared/constants/routes";

const DEFAULT_AVATAR_URL = "https://i.pravatar.cc/96?img=11";

const QUICK_LINKS = [
  { icon: "bookmark", label: "Saved Items", color: "text-primary" },
  { icon: "group", label: "My Network", color: "text-secondary" },
  { icon: "event", label: "Events", color: "text-[#565a5b]" },
];

export function FeedLeftSidebar() {
  const { profile, isLoading } = useAccountProfile();
  const displayName = profile?.profile?.display_name || profile?.email || "Thành viên";
  const title = profile?.profile?.bio || "Thành viên 2Hands";
  const avatarUrl = profile?.profile?.avatar_url || DEFAULT_AVATAR_URL;

  return (
    <aside className="hidden flex-col gap-6 lg:flex lg:col-span-3">
      <div className="flex flex-col items-center rounded-xl border border-outline-variant bg-surface-container-lowest p-6 text-center shadow-sm">
        {isLoading ? (
          <div className="mb-4 h-24 w-24 animate-pulse rounded-full bg-surface-container-high" />
        ) : (
          <div className="mb-4 h-24 w-24 overflow-hidden rounded-full border-2 border-[#d8e3fb]">
            <img src={avatarUrl} alt="" className="h-full w-full object-cover" />
          </div>
        )}
        <h2 className="text-xl font-semibold text-on-surface">{displayName}</h2>
        <p className="mt-1 text-sm text-on-surface-variant">{title}</p>
        <div className="mt-4 flex w-full justify-between border-t border-outline-variant pt-4">
          <div className="flex flex-1 flex-col items-center">
            <span className="text-xl font-semibold text-on-surface">—</span>
            <span className="text-xs font-semibold text-on-surface-variant">Posts</span>
          </div>
          <div className="mx-2 w-px bg-outline-variant" />
          <div className="flex flex-1 flex-col items-center">
            <span className="text-xl font-semibold text-on-surface">—</span>
            <span className="text-xs font-semibold text-on-surface-variant">Followers</span>
          </div>
        </div>
        <Link
          to={APP_ROUTES.account}
          className="mt-4 text-sm font-medium text-primary hover:underline"
        >
          Xem hồ sơ
        </Link>
      </div>

      <div className="flex flex-col gap-2 rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-2 text-sm font-medium uppercase tracking-wider text-on-surface">Quick Links</h3>
        {QUICK_LINKS.map((item) => (
          <a
            key={item.label}
            href="#"
            className="flex items-center gap-3 rounded-lg p-2 text-on-surface transition-colors hover:bg-[#f0f3ff]"
            onClick={(event) => event.preventDefault()}
          >
            <span className={`material-symbols-outlined ${item.color}`} aria-hidden="true">
              {item.icon}
            </span>
            <span className="text-sm">{item.label}</span>
          </a>
        ))}
      </div>
    </aside>
  );
}
