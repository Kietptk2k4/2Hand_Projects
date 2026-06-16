import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAccountProfile } from "../../auth/account/hooks/useAccountProfile";
import { useCurrentUserId } from "../../auth/hooks/useCurrentUserId";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useFeedSidebarStats } from "../hooks/useFeedSidebarStats";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";
import { formatSocialCount } from "../utils/formatSocialCount";

const DEFAULT_AVATAR_URL = "https://i.pravatar.cc/96?img=11";
const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

function SidebarStatValue({ value, isLoading }) {
  if (isLoading) {
    return <span className="mb-0.5 inline-block h-7 w-10 animate-pulse rounded bg-surface-container-high" />;
  }
  return (
    <span className="text-xl font-semibold text-on-surface">
      {formatSocialCount(value) ?? "\u2014"}
    </span>
  );
}

const QUICK_LINKS = [
  { icon: "bookmark", label: "Đã lưu", color: "text-primary", to: APP_ROUTES.socialSavedPosts },
  // { icon: "group", label: "Mạng lưới", color: "text-secondary" },
  // { icon: "event", label: "Sự kiện", color: "text-[#565a5b]" },
];

export function FeedLeftSidebar({ onComingSoon, stats: statsOverride }) {
  const navigate = useNavigate();
  const currentUserId = useCurrentUserId();
  const { profile, isLoading } = useAccountProfile();
  const displayName = profile?.profile?.display_name || profile?.email || "Thành viên";
  const title = profile?.profile?.bio || "Thành viên 2Hands";
  const avatarUrl = profile?.profile?.avatar_url || DEFAULT_AVATAR_URL;
  const resolvedUserId = currentUserId || profile?.user?.id;
  const internalStats = useFeedSidebarStats(statsOverride ? null : resolvedUserId);
  const { postCount, followerCount, savedCount, isLoading: isStatsLoading } =
    statsOverride ?? internalStats;
  const selfProfilePath = resolvedUserId
    ? buildSocialProfilePath(resolvedUserId)
    : APP_ROUTES.socialFeed;

  const goToSelfProfile = () => {
    if (resolvedUserId) navigate(buildSocialProfilePath(resolvedUserId));
  };

  const handleComingSoon = (event) => {
    event.preventDefault();
    if (onComingSoon) {
      onComingSoon();
      return;
    }
    window.alert(COMING_SOON_MESSAGE);
  };

  return (
    <aside className="hidden flex-col gap-6 lg:col-span-3 lg:flex lg:sticky lg:top-20 lg:max-h-[calc(100vh-5rem)] lg:self-start lg:overflow-y-auto">
      <div className="flex flex-col items-center rounded-xl border border-outline-variant bg-surface-container-lowest p-6 text-center shadow-sm">
        {isLoading ? (
          <div className="mb-4 h-24 w-24 animate-pulse rounded-full bg-surface-container-high" />
        ) : (
          <button
            type="button"
            onClick={goToSelfProfile}
            className="mb-4 h-24 w-24 overflow-hidden rounded-full border-2 border-[#d8e3fb] ring-2 ring-primary/20 transition hover:ring-primary/50"
            aria-label="Xem hồ sơ của bạn"
          >
            <img src={avatarUrl} alt="" className="h-full w-full object-cover" />
          </button>
        )}
        <button type="button" onClick={goToSelfProfile} className="text-left">
          <h2 className="text-xl font-semibold text-on-surface hover:text-primary">{displayName}</h2>
        </button>
        <p className="mt-1 text-sm text-on-surface-variant">{title}</p>
        <div className="mt-4 flex w-full justify-between border-t border-outline-variant pt-4">
          <button
            type="button"
            onClick={goToSelfProfile}
            disabled={!resolvedUserId}
            className="flex flex-1 flex-col items-center rounded-lg transition-colors hover:bg-surface-container-high disabled:cursor-default disabled:hover:bg-transparent"
          >
            <SidebarStatValue value={postCount} isLoading={isStatsLoading} />
            <span className="text-xs font-semibold text-on-surface-variant">Bài viết</span>
          </button>
          <div className="mx-2 w-px bg-outline-variant" />
          <button
            type="button"
            onClick={goToSelfProfile}
            disabled={!resolvedUserId}
            className="flex flex-1 flex-col items-center rounded-lg transition-colors hover:bg-surface-container-high disabled:cursor-default disabled:hover:bg-transparent"
          >
            <SidebarStatValue value={followerCount} isLoading={isStatsLoading} />
            <span className="text-xs font-semibold text-on-surface-variant">Người theo dõi</span>
          </button>
        </div>
        <Link
          to={selfProfilePath}
          className="mt-4 text-sm font-medium text-primary hover:underline"
        >
          Xem hồ sơ
        </Link>
        <Link
          to={APP_ROUTES.account}
          className="mt-2 text-xs text-on-surface-variant hover:text-primary hover:underline"
        >
          Cài đặt tài khoản
        </Link>
      </div>

      <div className="flex flex-col gap-2 rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-2 text-sm font-medium uppercase tracking-wider text-on-surface">Liên kết nhanh</h3>
        {QUICK_LINKS.map((item) =>
          item.to ? (
            <NavLink
              key={item.label}
              to={item.to}
              className={({ isActive }) =>
                [
                  "flex items-center gap-3 rounded-lg p-2 text-sm transition-colors hover:bg-[#f0f3ff]",
                  isActive ? "bg-[#f0f3ff] text-primary" : "text-on-surface",
                ].join(" ")
              }
            >
              <span
                className={`material-symbols-outlined ${item.color}`}
                aria-hidden="true"
              >
                {item.icon}
              </span>
              <span className="flex-1 text-left">{item.label}</span>
              {item.to === APP_ROUTES.socialSavedPosts ? (
                isStatsLoading ? (
                  <span className="inline-block h-4 w-6 animate-pulse rounded bg-surface-container-high" />
                ) : savedCount !== null ? (
                  <span className="text-xs font-semibold text-on-surface-variant">
                    {formatSocialCount(savedCount)}
                  </span>
                ) : null
              ) : null}
            </NavLink>
          ) : (
            <button
              key={item.label}
              type="button"
              className="flex w-full items-center gap-3 rounded-lg p-2 text-left text-on-surface transition-colors hover:bg-[#f0f3ff]"
              onClick={handleComingSoon}
            >
              <span className={`material-symbols-outlined ${item.color}`} aria-hidden="true">
                {item.icon}
              </span>
              <span className="text-sm">{item.label}</span>
            </button>
          )
        )}
      </div>
    </aside>
  );
}