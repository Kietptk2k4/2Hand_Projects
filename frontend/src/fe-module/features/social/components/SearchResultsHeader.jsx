import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function SearchResultsHeader({ keyword, totalElements, onSearch, onClear }) {
  const [inputVal, setInputVal] = useState(keyword || "");

  useEffect(() => {
    setInputVal(keyword || "");
  }, [keyword]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const trimmed = inputVal.trim();
    if (trimmed) {
      onSearch?.(trimmed);
    }
  };

  const handleClear = () => {
    setInputVal("");
    onClear?.();
  };

  return (
    <div className="sticky top-16 z-30 flex flex-col gap-3 border-b border-outline-variant/40 bg-surface-container-lowest/90 px-4 py-3.5 backdrop-blur-md">
      {/* Row 1: Back arrow + Title */}
      <div className="flex items-center gap-3">
        <Link
          to={APP_ROUTES.socialFeed}
          className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full transition-colors hover:bg-surface-container-low"
          aria-label="Về feed"
        >
          <span className="material-symbols-outlined text-[24px] text-on-surface" aria-hidden="true">
            arrow_back
          </span>
        </Link>
        <h1 className="text-xl font-extrabold leading-tight text-on-surface">Khám phá</h1>
      </div>

      {/* Row 2: Search Input Bar Below Title */}
      <form onSubmit={handleSubmit} className="relative w-full">
        <span
          className="material-symbols-outlined absolute left-3.5 top-1/2 -translate-y-1/2 text-[20px] text-on-surface-variant/70"
          aria-hidden="true"
        >
          search
        </span>
        <input
          type="text"
          value={inputVal}
          onChange={(e) => setInputVal(e.target.value)}
          placeholder="Tìm kiếm bài viết, hashtag, danh mục..."
          className="w-full rounded-full border border-outline-variant/50 bg-surface-container-low/60 py-2.5 pl-11 pr-10 text-[15px] text-on-surface outline-none transition-all focus:border-sky-500 focus:bg-surface-container-lowest focus:ring-1 focus:ring-sky-500/30"
        />
        {inputVal ? (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-3.5 top-1/2 -translate-y-1/2 text-on-surface-variant/70 hover:text-on-surface"
            aria-label="Xóa từ khóa"
          >
            <span className="material-symbols-outlined text-[20px]">close</span>
          </button>
        ) : null}
      </form>

      {keyword ? (
        <div className="flex items-center justify-between pl-12 text-xs text-on-surface-variant/70">
          <p>
            Kết quả cho <span className="text-sky-500 font-bold">&quot;{keyword}&quot;</span> ({totalElements || 0} kết quả)
          </p>
          <button
            type="button"
            onClick={handleClear}
            className="text-sky-500 font-bold hover:underline"
          >
            Bỏ lọc
          </button>
        </div>
      ) : null}
    </div>
  );
}
