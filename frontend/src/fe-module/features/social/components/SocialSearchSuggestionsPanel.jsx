import { useEffect, useState } from "react";
import { getSearchHistory } from "../utils/searchHistoryStorage";

export function SocialSearchSuggestionsPanel({ onSelectKeyword, onFindUsers }) {
  const [history, setHistory] = useState([]);

  useEffect(() => {
    setHistory(getSearchHistory());
  }, []);

  return (
    <div className="absolute left-0 right-0 top-full z-50 mt-1 overflow-hidden rounded-lg border border-header-border bg-white shadow-lg">
      <div className="border-b border-header-border px-3 py-2">
        <button
          type="button"
          onClick={onFindUsers}
          className="text-sm font-medium text-primary hover:underline"
        >
          Tìm người dùng
        </button>
      </div>

      <p className="border-b border-header-border px-3 py-2 text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
        Tìm kiếm gần đây
      </p>

      {history.length === 0 ? (
        <p className="px-3 py-3 text-sm text-on-surface-variant">Chưa có từ khóa gần đây.</p>
      ) : (
        <ul className="max-h-48 overflow-y-auto py-1">
          {history.map((item) => (
            <li key={item}>
              <button
                type="button"
                onClick={() => onSelectKeyword?.(item)}
                className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-on-surface hover:bg-account-surface-low"
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
  );
}
