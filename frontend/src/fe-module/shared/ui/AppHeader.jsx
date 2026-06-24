import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { SocialSearchSuggestionsPanel } from "../../features/social/components/SocialSearchSuggestionsPanel";
import { SocialUserSearchDropdown } from "../../features/social/components/SocialUserSearchDropdown";
import { getMyProfile } from "../../features/auth/api/authApi";
import { useAuthSession } from "../../features/auth/hooks/useAuthSession.jsx";
import { resolveDevMediaUrl } from "../utils/getClientUploadOrigin";
import { buildSocialSearchPath } from "../../features/social/utils/socialSearchRoutes";
import { APP_ROUTES } from "../constants/routes";
import { NotificationBell } from "../../features/notification/components/NotificationBell.jsx";
import { HeaderAccountMenu } from "./HeaderAccountMenu.jsx";

const DEFAULT_AVATAR_URL = "https://i.pravatar.cc/96?img=11";

const NAV_LINKS = [
  { label: "Social", to: APP_ROUTES.socialFeed },
  { label: "Commerce", to: APP_ROUTES.commerceHome },
  // { label: "Services", to: "#services" },
  // { label: "My Bookings", to: "#bookings" },
];

function SearchIcon() {
  return (
    <svg
      className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-header-muted"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      aria-hidden="true"
    >
      <circle cx="11" cy="11" r="7" />
      <path d="M20 20L17 17" strokeLinecap="round" />
    </svg>
  );
}

function HeaderIconButton({ label, children, to }) {
  const className =
    "flex h-9 w-9 items-center justify-center rounded-full text-header-nav transition-colors hover:bg-header-border/60";

  if (to) {
    return (
      <Link to={to} className={className} aria-label={label}>
        {children}
      </Link>
    );
  }

  return (
    <button type="button" className={className} aria-label={label}>
      {children}
    </button>
  );
}

function HelpIcon() {
  return (
    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      <path d="M9.5 9.5a2.5 2.5 0 014.5 1.5c0 2-2.5 2-2.5 4" strokeLinecap="round" />
      <circle cx="12" cy="17" r="0.75" fill="currentColor" stroke="none" />
    </svg>
  );
}

function SettingsIcon() {
  return (
    <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" aria-hidden="true">
      <circle cx="12" cy="12" r="3" />
      <path
        d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"
        strokeLinecap="round"
      />
    </svg>
  );
}

export function AppHeader({ className = "" }) {
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { isAuthenticated, user } = useAuthSession();
  const [profileAvatarUrl, setProfileAvatarUrl] = useState(null);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchPanelMode, setSearchPanelMode] = useState("suggestions");
  const [searchInput, setSearchInput] = useState("");
  const searchRef = useRef(null);
  const isSocialRoute = location.pathname.startsWith("/social");
  const isSearchPage = location.pathname === APP_ROUTES.socialSearchPosts;
  const socialSearchEnabled = isSocialRoute && isAuthenticated;

  const submitPostSearch = useCallback(() => {
    const trimmed = searchInput.trim();
    if (!trimmed) return;
    setSearchOpen(false);
    setSearchPanelMode("suggestions");
    navigate(buildSocialSearchPath(trimmed));
  }, [navigate, searchInput]);

  useEffect(() => {
    if (!isAuthenticated) {
      setProfileAvatarUrl(null);
      return undefined;
    }

    let cancelled = false;
    getMyProfile()
      .then((data) => {
        if (!cancelled && data?.profile?.avatar_url) {
          setProfileAvatarUrl(data.profile.avatar_url);
        }
      })
      .catch(() => {});

    return () => {
      cancelled = true;
    };
  }, [isAuthenticated]);

  useEffect(() => {
    if (isSearchPage) {
      setSearchInput(searchParams.get("q") ?? "");
    }
  }, [isSearchPage, searchParams]);

  useEffect(() => {
    if (!searchOpen) return undefined;
    const onPointerDown = (event) => {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setSearchOpen(false);
        setSearchPanelMode("suggestions");
      }
    };
    document.addEventListener("pointerdown", onPointerDown);
    return () => document.removeEventListener("pointerdown", onPointerDown);
  }, [searchOpen]);

  const avatarUrl = resolveDevMediaUrl(
    profileAvatarUrl || user?.avatar_url || user?.profile?.avatar_url || DEFAULT_AVATAR_URL
  );
  const userLabel = user?.display_name || user?.email || "";

  return (
    <header
      className={[
        "sticky top-0 z-50 w-full border-b border-header-border bg-header-surface",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      <div className="mx-auto flex h-16 w-full max-w-[1280px] items-center justify-between gap-3 px-4 md:gap-6 md:px-8">
        <div className="flex min-w-0 flex-1 items-center gap-4 md:gap-6">
          <Link to={APP_ROUTES.socialFeed} className="shrink-0 text-xl font-bold text-header-brand md:text-2xl">
            2Hands
          </Link>

          <div
            ref={searchRef}
            className="relative hidden min-w-0 max-w-[280px] flex-1 sm:block lg:max-w-[320px]"
          >
            <label>
              <span className="sr-only">Tìm kiếm</span>
              <SearchIcon />
              <input
                type="search"
                name="global-search"
                placeholder="Tìm kiếm..."
                value={searchInput}
                readOnly={!socialSearchEnabled}
                aria-disabled={!socialSearchEnabled}
                onChange={(event) => {
                  if (!socialSearchEnabled) return;
                  setSearchInput(event.target.value);
                }}
                onFocus={() => {
                  if (!socialSearchEnabled) return;
                  setSearchPanelMode("suggestions");
                  setSearchOpen(true);
                }}
                onKeyDown={(event) => {
                  if (!socialSearchEnabled) return;
                  if (event.key === "Enter") {
                    event.preventDefault();
                    submitPostSearch();
                  }
                }}
                className={[
                  "w-full rounded-lg border border-header-border bg-surface-container-lowest py-2 pl-10 pr-3 text-sm text-on-surface outline-none transition placeholder:text-header-muted focus:border-primary focus:ring-1 focus:ring-primary/30",
                  !socialSearchEnabled ? "cursor-default opacity-70" : "",
                ]
                  .filter(Boolean)
                  .join(" ")}
              />
            </label>
            {searchOpen && socialSearchEnabled && searchPanelMode === "suggestions" ? (
              <SocialSearchSuggestionsPanel
                onSelectKeyword={(keyword) => {
                  setSearchInput(keyword);
                  setSearchOpen(false);
                  navigate(buildSocialSearchPath(keyword));
                }}
                onFindUsers={() => setSearchPanelMode("users")}
              />
            ) : null}
            {searchOpen && socialSearchEnabled && searchPanelMode === "users" ? (
              <SocialUserSearchDropdown
                onClose={() => {
                  setSearchOpen(false);
                  setSearchPanelMode("suggestions");
                }}
              />
            ) : null}
          </div>
        </div>

        <div className="flex shrink-0 items-center gap-4 md:gap-6">
          <nav
            className="hidden items-center gap-6 text-sm font-medium text-header-nav md:flex"
            aria-label="Main"
          >
            {NAV_LINKS.map((item) => {
              const isActive =
                !item.to.startsWith("#") &&
                (location.pathname === item.to || location.pathname.startsWith(`${item.to}/`));
              const linkClass = [
                "transition-colors hover:text-header-brand",
                isActive ? "border-b-2 border-header-brand pb-0.5 text-header-brand" : "",
              ]
                .filter(Boolean)
                .join(" ");

              return item.to.startsWith("#") ? (
                <a key={item.label} href={item.to} className={linkClass}>
                  {item.label}
                </a>
              ) : (
                <Link key={item.label} to={item.to} className={linkClass}>
                  {item.label}
                </Link>
              );
            })}
          </nav>

          <div className="flex items-center gap-2 md:gap-3">
            <NotificationBell />
            <HeaderIconButton label="Trợ giúp">
              <HelpIcon />
            </HeaderIconButton>
            <HeaderIconButton label="Cài đặt" to={APP_ROUTES.account}>
              <SettingsIcon />
            </HeaderIconButton>

            <HeaderAccountMenu
              avatarUrl={avatarUrl}
              isAuthenticated={isAuthenticated}
              userLabel={userLabel}
              userId={user?.id}
            />
          </div>
        </div>
      </div>
    </header>
  );
}
