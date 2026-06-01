import { useEffect, useState } from "react";
import {
  clearCommerceSearchHistory,
  getCommerceSearchHistory,
} from "../utils/commerceSearchHistoryStorage";

export function CommerceSearchSidebar({ onSelectKeyword, refreshKey = 0, onComingSoon }) {
  const [history, setHistory] = useState([]);

  const refreshHistory = () => {
    setHistory(getCommerceSearchHistory());
  };

  useEffect(() => {
    refreshHistory();
  }, [refreshKey]);

  const handleClearHistory = () => {
    clearCommerceSearchHistory();
    refreshHistory();
  };

  const handleSelect = (keyword) => {
    onSelectKeyword?.(keyword);
    refreshHistory();
  };

  return (
    <aside className="hidden flex-col gap-6 lg:flex lg:w-64 lg:shrink-0">
      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-sm font-medium uppercase tracking-wider text-on-surface">
            Tìm kiếm gần đây
          </h2>
          {history.length > 0 ? (
            <button
              type="button"
              onClick={handleClearHistory}
              className="text-xs font-medium text-primary hover:underline"
            >
              Xóa
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
                  className="flex w-full items-center gap-2 rounded-lg px-2 py-2 text-left text-sm text-on-surface transition-colors hover:bg-surface-container-low"
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

      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm opacity-60">
        <h2 className="mb-3 text-sm font-medium uppercase tracking-wider text-on-surface">Bộ lọc</h2>
        <p className="mb-3 text-sm text-on-surface-variant">
          Lọc theo giá, tình trạng và đánh giá sẽ sớm có mặt.
        </p>
        <button
          type="button"
          onClick={onComingSoon}
          className="text-sm font-medium text-primary hover:underline"
        >
          Tính năng đang phát triển
        </button>
      </div>
    </aside>
  );
}
