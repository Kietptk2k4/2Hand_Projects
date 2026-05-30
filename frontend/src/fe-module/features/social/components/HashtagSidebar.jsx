import { TRENDING_HASHTAGS } from "../constants/hashtagPostsConstants";

export function HashtagSidebar({ currentHashtag, onSelectTag }) {
  return (
    <aside className="hidden flex-col gap-6 lg:flex lg:col-span-3">
      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h2 className="mb-4 flex items-center gap-2 text-lg font-semibold text-on-surface">
          <span className="material-symbols-outlined text-primary" aria-hidden="true">
            trending_up
          </span>
          Trending Tags
        </h2>
        <div className="flex flex-wrap gap-2">
          {TRENDING_HASHTAGS.map((tag) => {
            const isActive = currentHashtag?.toLowerCase() === tag.toLowerCase();
            return (
              <button
                key={tag}
                type="button"
                onClick={() => onSelectTag?.(tag)}
                className={[
                  "rounded-full px-3 py-1 text-xs font-semibold transition-colors",
                  isActive
                    ? "bg-primary-fixed text-on-primary-fixed"
                    : "bg-surface-container text-on-surface hover:bg-primary-fixed hover:text-on-primary-fixed",
                ].join(" ")}
              >
                #{tag}
              </button>
            );
          })}
        </div>
      </div>
    </aside>
  );
}
