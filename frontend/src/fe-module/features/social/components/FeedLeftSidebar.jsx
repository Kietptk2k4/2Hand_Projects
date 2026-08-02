import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAccountProfile } from "../../auth/account/hooks/useAccountProfile";
import { useCurrentUserId } from "../../auth/hooks/useCurrentUserId";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

const DEFAULT_AVATAR_URL = "https://i.pravatar.cc/96?img=11";

export function FeedLeftSidebar({ onComingSoon, onOpenCreatePost }) {
  const navigate = useNavigate();
  const currentUserId = useCurrentUserId();
  const { profile, isLoading } = useAccountProfile();
  const displayName = profile?.profile?.display_name || profile?.email || "Thành viên";
  const avatarUrl = profile?.profile?.avatar_url || DEFAULT_AVATAR_URL;
  const userEmail = profile?.email || profile?.user?.email || "";
  const userHandle = userEmail
    ? (userEmail.includes("@") ? `@${userEmail.split("@")[0]}` : `@${userEmail}`)
    : `@${(displayName || "thanhvien").toLowerCase().replace(/[^a-z0-9]/gi, "")}`;

  const resolvedUserId = currentUserId || profile?.user?.id;
  const selfProfilePath = resolvedUserId
    ? buildSocialProfilePath(resolvedUserId)
    : APP_ROUTES.socialFeed;

  const handleComingSoon = (event) => {
    event.preventDefault();
    onComingSoon?.();
  };

  const navItems = [
    { label: "Trang chủ", icon: "home", to: APP_ROUTES.socialFeed },
    { label: "Khám phá", icon: "search", to: APP_ROUTES.socialSearchPosts },
    { label: "Chợ 2Hands", icon: "storefront", to: APP_ROUTES.commerceHome },
    { label: "Gợi ý theo dõi", icon: "person_add", to: APP_ROUTES.socialSuggestedUsers },
    { label: "Đã lưu", icon: "bookmark", to: APP_ROUTES.socialSavedPosts },
    { label: "Hồ sơ", icon: "person", to: selfProfilePath },
    { label: "Cài đặt", icon: "settings", to: APP_ROUTES.account },
  ];

  return (
    <aside className="sticky top-20 z-30 hidden h-[calc(100vh-5.5rem)] flex-col justify-between py-3 px-2 lg:col-span-3 lg:flex lg:self-start">
      <div className="flex flex-col gap-2">
        {/* Navigation List */}
        <nav className="mt-1 flex flex-col gap-1">
          {navItems.map((item) => {
            if (item.onClick) {
              return (
                <button
                  key={item.label}
                  type="button"
                  onClick={item.onClick}
                  className="group flex w-full items-center gap-4 rounded-full px-4 py-3 text-left transition-colors hover:bg-surface-container-high"
                >
                  <span className="material-symbols-outlined text-[26px] text-on-surface transition-transform group-hover:scale-105" aria-hidden="true">
                    {item.icon}
                  </span>
                  <span className="text-[17px] font-semibold text-on-surface">
                    {item.label}
                  </span>
                </button>
              );
            }
            return (
              <NavLink
                key={item.label}
                to={item.to}
                className={({ isActive }) =>
                  [
                    "group flex w-full items-center gap-4 rounded-full px-4 py-3 transition-colors",
                    isActive
                      ? "font-extrabold text-on-surface bg-surface-container-low"
                      : "text-on-surface hover:bg-surface-container-high",
                  ].join(" ")
                }
              >
                {({ isActive }) => (
                  <>
                    <span
                      className="material-symbols-outlined text-[26px] transition-transform group-hover:scale-105"
                      style={{ fontVariationSettings: isActive ? "'FILL' 1" : "'FILL' 0" }}
                      aria-hidden="true"
                    >
                      {item.icon}
                    </span>
                    <span className={`text-[17px] ${isActive ? "font-bold" : "font-semibold"}`}>
                      {item.label}
                    </span>
                  </>
                )}
              </NavLink>
            );
          })}
        </nav>

        {/* Post Button */}
        <button
          type="button"
          onClick={() => onOpenCreatePost?.()}
          className="mt-3 flex h-12 w-full items-center justify-center rounded-full bg-on-surface text-[16px] font-bold text-surface-container-lowest shadow-md transition-all hover:opacity-90 active:scale-98"
        >
          <span>Đăng bài</span>
        </button>
      </div>

      {/* User Profile Quick Card at Bottom */}
      <div className="mt-auto border-t border-outline-variant/40 pt-3">
        {isLoading ? (
          <div className="flex items-center gap-3 px-3 py-2 animate-pulse">
            <div className="h-10 w-10 rounded-full bg-surface-container-high" />
            <div className="flex-1 space-y-1.5">
              <div className="h-3.5 w-24 rounded bg-surface-container-high" />
              <div className="h-3 w-16 rounded bg-surface-container-high" />
            </div>
          </div>
        ) : (
          <Link
            to={selfProfilePath}
            className="flex w-full items-center justify-between rounded-full p-2.5 transition-colors hover:bg-surface-container-high group"
          >
            <div className="flex items-center gap-3 min-w-0">
              <img
                src={avatarUrl}
                alt=""
                className="h-12 w-12 shrink-0 rounded-full object-cover"
              />
              <div className="min-w-0 flex-1">
                <p className="truncate text-[15px] font-bold text-on-surface">{displayName}</p>
                <p className="truncate text-[15px] text-on-surface-variant/70">{userHandle}</p>
              </div>
            </div>
            <span className="material-symbols-outlined text-[22px] text-on-surface-variant/60" aria-hidden="true">
              more_horiz
            </span>
          </Link>
        )}
      </div>
    </aside>
  );
}