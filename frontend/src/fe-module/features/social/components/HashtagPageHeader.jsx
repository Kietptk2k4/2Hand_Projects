import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function HashtagPageHeader({ hashtag, totalElements, onSearchTag }) {
  const [tagInput, setTagInput] = useState("");

  useEffect(() => {
    setTagInput("");
  }, [hashtag]);

  const handleSubmit = (event) => {
    event.preventDefault();
    const trimmed = tagInput.trim().replace(/^#/, "");
    if (!trimmed) return;
    onSearchTag?.(trimmed);
  };

  if (!hashtag) return null;

  return (
    <div className="sticky top-16 z-30 flex items-center justify-between gap-4 border-b border-outline-variant/40 bg-surface-container-lowest/90 px-4 py-2.5 backdrop-blur-md">
      <div className="flex items-center gap-4 min-w-0 flex-1">
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
          <h1 className="truncate text-lg font-extrabold leading-tight text-sky-500 hover:underline cursor-pointer">
            #{hashtag}
          </h1>
          <p className="text-xs text-on-surface-variant/70">
            {totalElements !== undefined ? `${totalElements} bài viết` : "Bài viết theo hashtag"}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="relative max-w-xs flex-grow hidden sm:block">
        <span
          className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-[18px] text-on-surface-variant/70"
          aria-hidden="true"
        >
          search
        </span>
        <input
          type="search"
          value={tagInput}
          onChange={(event) => setTagInput(event.target.value)}
          placeholder="Tìm hashtag khác…"
          className="w-full rounded-full border border-outline-variant/50 bg-surface-container-low/50 py-1.5 pl-9 pr-3 text-xs text-on-surface outline-none transition focus:border-sky-500 focus:ring-1 focus:ring-sky-500/30"
        />
      </form>
    </div>
  );
}
