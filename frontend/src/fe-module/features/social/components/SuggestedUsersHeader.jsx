import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function SuggestedUsersHeader({ searchQuery = "", onSearchChange, onClearSearch }) {
  return (
    <div className="sticky top-16 z-30 flex flex-col gap-2.5 border-b border-outline-variant/40 bg-surface-container-lowest/90 px-4 py-3 backdrop-blur-md">
      <div className="flex items-center gap-3">
        <Link
          to={APP_ROUTES.socialFeed}
          className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full transition-colors hover:bg-surface-container-low"
          aria-label="Về feed"
        >
          <span className="material-symbols-outlined text-[22px] text-on-surface" aria-hidden="true">
            arrow_back
          </span>
        </Link>
        <div className="min-w-0 flex-1">
          <h1 className="truncate text-lg font-extrabold leading-tight text-on-surface">
            Gợi ý theo dõi
          </h1>
          <p className="text-xs text-on-surface-variant/70">
            Những người bạn có thể biết trên 2Hands
          </p>
        </div>
      </div>

      <div className="relative w-full">
        <span
          className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-[18px] text-on-surface-variant/70"
          aria-hidden="true"
        >
          search
        </span>
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => onSearchChange?.(e.target.value)}
          placeholder="Tìm kiếm thành viên, shop..."
          className="w-full rounded-full border border-outline-variant/50 bg-surface-container-low/60 py-1.5 pl-9 pr-8 text-xs text-on-surface outline-none transition-all focus:border-sky-500 focus:bg-surface-container-lowest focus:ring-1 focus:ring-sky-500/30"
        />
        {searchQuery ? (
          <button
            type="button"
            onClick={onClearSearch}
            className="absolute right-2.5 top-1/2 -translate-y-1/2 text-on-surface-variant/70 hover:text-on-surface"
            aria-label="Xóa từ khóa"
          >
            <span className="material-symbols-outlined text-[16px]">close</span>
          </button>
        ) : null}
      </div>
    </div>
  );
}
