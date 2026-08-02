import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function SavedPostsHeader({ totalElements }) {
  return (
    <div className="sticky top-16 z-30 flex items-center gap-4 border-b border-outline-variant/40 bg-surface-container-lowest/90 px-4 py-2.5 backdrop-blur-md">
      <Link
        to={APP_ROUTES.socialFeed}
        className="flex h-9 w-9 items-center justify-center rounded-full transition-colors hover:bg-surface-container-low"
        aria-label="Về feed"
      >
        <span className="material-symbols-outlined text-[22px] text-on-surface" aria-hidden="true">
          arrow_back
        </span>
      </Link>
      <div className="min-w-0 flex-1">
        <h1 className="truncate text-lg font-extrabold leading-tight text-on-surface">Đã lưu</h1>
        <p className="text-xs text-on-surface-variant/70">
          {totalElements !== undefined ? `${totalElements} bài viết đã lưu` : "Bài viết bạn đã đánh dấu"}
        </p>
      </div>
    </div>
  );
}
