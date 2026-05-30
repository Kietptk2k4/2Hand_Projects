import { useEffect, useState } from "react";
import { POPULAR_SEARCH_HASHTAGS } from "../constants/searchPostsConstants";
import { clearSearchHistory, getSearchHistory } from "../utils/searchHistoryStorage";

export function SearchSidebar({ onSelectKeyword, refreshKey = 0 }) {
  const [history, setHistory] = useState([]);

  const refreshHistory = () => {
    setHistory(getSearchHistory());
  };

  useEffect(() => {
    refreshHistory();
  }, [refreshKey]);

  const handleClearHistory = () => {
    clearSearchHistory();
    refreshHistory();
  };

  const handleSelect = (keyword) => {
    onSelectKeyword?.(keyword);
    refreshHistory();
  };

  return (
    <aside className="hidden flex-col gap-6 lg:flex lg:col-span-3">
      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-sm font-medium uppercase tracking-wider text-on-surface">
            Lịch sử tìm kiếm
          </h2>
          {history.length > 0 ? (
            <button
              type="button"
              onClick={handleClearHistory}
              className="text-xs font-medium text-primary hover:underline"
            >
              Xóa lịch sử
            </button>
          ) : null}
        </div>
        {history.length === 0 ? (
          <p className="text-sm text-on-surface-variant">Chưa có lịch sử tìm kiếm.</p>
        ) : (
          <ul className="flex flex-col gap-1">
            {history.map((item) => (
              <li key={item}>
                <button
                  type="button"
                  onClick={() => handleSelect(item)}
                  className="flex w-full items-center gap-2 rounded-lg px-2 py-2 text-left text-sm text-on-surface transition-colors hover:bg-[#f0f3ff]"
                >
                  <span className="material-symbols-outlined text-[18px] text-on-surface-variant" aria-hidden="true">
                    history
                  </span>
                  {item}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h2 className="mb-4 text-sm font-medium uppercase tracking-wider text-on-surface">
          Hashtag phổ biến
        </h2>
        <div className="flex flex-wrap gap-2">
          {POPULAR_SEARCH_HASHTAGS.map((tag) => (
            <button
              key={tag}
              type="button"
              onClick={() => handleSelect(tag)}
              className="rounded-full border border-outline-variant bg-surface-container-low px-3 py-1 text-sm font-medium text-primary transition-colors hover:bg-[#e7eeff]"
            >
              #{tag}
            </button>
          ))}
        </div>
      </div>
    </aside>
  );
}
