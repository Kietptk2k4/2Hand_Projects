export function PostMediaGrid({ media = [] }) {
  const images = (media || []).filter((item) => item?.url);
  if (images.length === 0) return null;

  if (images.length === 1) {
    return (
      <div className="relative h-64 w-full bg-surface-container-high">
        <img src={images[0].url} alt="" className="h-full w-full object-cover" loading="lazy" />
      </div>
    );
  }

  if (images.length === 2) {
    return (
      <div className="grid h-64 grid-cols-2 gap-0.5 bg-surface-container-high">
        {images.map((item, index) => (
          <img
            key={item.url || index}
            src={item.url}
            alt=""
            className="h-full w-full object-cover"
            loading="lazy"
          />
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
          <div key={item.url || index} className="relative h-full w-full">
            <img src={item.url} alt="" className="h-full w-full object-cover" loading="lazy" />
            {isLast ? (
              <div className="absolute inset-0 flex items-center justify-center bg-on-surface/50 text-xl font-semibold text-on-primary">
                +{overflow}
              </div>
            ) : null}
          </div>
        );
      })}
    </div>
  );
}
