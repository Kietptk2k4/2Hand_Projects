import { useEffect, useState } from "react";

export function HashtagPageHeader({ hashtag, totalElements, onSearchTag }) {
  const [tagInput, setTagInput] = useState("");

  useEffect(() => {
    setTagInput("");
  }, [hashtag]);

  const handleSubmit = (event) => {
    event.preventDefault();
    const trimmed = tagInput.trim();
    if (!trimmed) return;
    onSearchTag?.(trimmed);
  };

  if (!hashtag) return null;

  return (
    <div className="sticky top-16 z-40 border-b border-outline-variant bg-surface/90 px-4 py-3 backdrop-blur-md md:px-8">
      <div className="mx-auto flex max-w-[1280px] flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary-container text-on-primary-container shadow-sm">
            <span className="material-symbols-outlined text-[28px]" aria-hidden="true">
              tag
            </span>
          </div>
          <div>
            <h1 className="text-2xl font-semibold text-on-surface">#{hashtag}</h1>
            <p className="text-sm text-on-surface-variant">{totalElements} bài viết</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="relative max-w-md flex-grow">
          <span
            className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline"
            aria-hidden="true"
          >
            search
          </span>
          <input
            type="search"
            value={tagInput}
            onChange={(event) => setTagInput(event.target.value)}
            placeholder="Tìm hashtag khác…"
            className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2 pl-10 pr-3 text-sm text-on-surface shadow-sm outline-none transition focus:border-primary focus:ring-1 focus:ring-primary"
          />
        </form>
      </div>
    </div>
  );
}
