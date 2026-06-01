export function ProductReviewMediaStrip({ media = [] }) {
  if (!media.length) return null;

  return (
    <div className="mt-3 flex gap-2 overflow-x-auto pb-1">
      {media.map((item) => (
        <div
          key={item.mediaId}
          className="relative h-20 w-20 shrink-0 overflow-hidden rounded-lg border border-outline-variant bg-surface-container"
        >
          {item.mediaType === "VIDEO" ? (
            <>
              <img src={item.url} alt="" className="h-full w-full object-cover" />
              <span
                className="absolute inset-0 flex items-center justify-center bg-black/30 text-white"
                aria-hidden="true"
              >
                <span className="material-symbols-outlined">play_circle</span>
              </span>
            </>
          ) : (
            <img src={item.url} alt="" className="h-full w-full object-cover" />
          )}
        </div>
      ))}
    </div>
  );
}
