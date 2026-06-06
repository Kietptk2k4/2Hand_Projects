import { normalizePostMediaUrl } from "../utils/postMediaUrl";

export function PostMediaGrid({ media = [], onMediaClick }) {
  const images = (media || [])
    .filter((item) => item?.url)
    .map((item) => ({ ...item, url: normalizePostMediaUrl(item.url) }));
  if (images.length === 0) return null;

  const handleActivate = (event, index) => {
    event.stopPropagation();
    onMediaClick?.(index);
  };

  if (images.length === 1) {
    return (
      <div
        className={[
          "relative h-64 w-full bg-surface-container-high",
          onMediaClick ? "cursor-pointer" : "",
        ]
          .filter(Boolean)
          .join(" ")}
        onClick={(event) => handleActivate(event, 0)}
        onKeyDown={(event) => {
          if (onMediaClick && (event.key === "Enter" || event.key === " ")) {
            event.preventDefault();
            handleActivate(event, 0);
          }
        }}
        role={onMediaClick ? "button" : undefined}
        tabIndex={onMediaClick ? 0 : undefined}
      >
        <img src={images[0].url} alt="" className="h-full w-full object-cover" loading="lazy" />
      </div>
    );
  }

  if (images.length === 2) {
    return (
      <div className="grid h-64 grid-cols-2 gap-0.5 bg-surface-container-high">
        {images.map((item, index) => (
          <div
            key={item.url || index}
            className={onMediaClick ? "cursor-pointer" : ""}
            onClick={(event) => handleActivate(event, index)}
            onKeyDown={(event) => {
              if (onMediaClick && (event.key === "Enter" || event.key === " ")) {
                event.preventDefault();
                handleActivate(event, index);
              }
            }}
            role={onMediaClick ? "button" : undefined}
            tabIndex={onMediaClick ? 0 : undefined}
          >
            <img src={item.url} alt="" className="h-full w-full object-cover" loading="lazy" />
          </div>
        ))}
      </div>
    );
  }

  const visible = images.slice(0, 4);
  const overflow = images.length - visible.length;

  return (
    <div className="grid h-64 grid-cols-2 grid-rows-2 gap-0.5 bg-surface-container-high">
      {visible.map((item, index) => {
        const isLast = index === visible.length - 1 && overflow > 0;
        return (
          <div
            key={item.url || index}
            className={["relative h-full w-full", onMediaClick ? "cursor-pointer" : ""]
              .filter(Boolean)
              .join(" ")}
            onClick={(event) => handleActivate(event, index)}
            onKeyDown={(event) => {
              if (onMediaClick && (event.key === "Enter" || event.key === " ")) {
                event.preventDefault();
                handleActivate(event, index);
              }
            }}
            role={onMediaClick ? "button" : undefined}
            tabIndex={onMediaClick ? 0 : undefined}
          >
            <img src={item.url} alt="" className="h-full w-full object-cover" loading="lazy" />
            {isLast ? (
              <div className="pointer-events-none absolute inset-0 flex items-center justify-center bg-on-surface/50 text-xl font-semibold text-on-primary">
                +{overflow}
              </div>
            ) : null}
          </div>
        );
      })}
    </div>
  );
}
