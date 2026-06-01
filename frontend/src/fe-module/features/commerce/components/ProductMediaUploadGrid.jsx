import { useCallback, useId, useRef } from "react";

function buildMockPicsumUrl(seed) {
  return `https://picsum.photos/seed/${seed}/600/600`;
}

export function ProductMediaUploadGrid({ mediaUrls, disabled, onChange, error }) {
  const inputId = useId();
  const inputRef = useRef(null);

  const handleFiles = useCallback(
    (event) => {
      const files = Array.from(event.target.files || []);
      if (!files.length) return;

      const next = [...mediaUrls];
      for (const file of files) {
        const seed = `product-media-${file.name}-${Date.now()}`.replace(/\s/g, "-");
        next.push(buildMockPicsumUrl(seed));
      }
      onChange?.(next);
      event.target.value = "";
    },
    [mediaUrls, onChange],
  );

  const removeAt = (index) => {
    onChange?.(mediaUrls.filter((_, i) => i !== index));
  };

  const setPrimary = (index) => {
    if (index === 0) return;
    const next = [...mediaUrls];
    const [item] = next.splice(index, 1);
    next.unshift(item);
    onChange?.(next);
  };

  return (
    <div>
      <input
        ref={inputRef}
        id={inputId}
        type="file"
        accept="image/*"
        multiple
        className="sr-only"
        disabled={disabled}
        onChange={handleFiles}
      />

      <button
        type="button"
        disabled={disabled}
        onClick={() => inputRef.current?.click()}
        className="flex w-full flex-col items-center justify-center rounded-xl border-2 border-dashed border-outline-variant bg-surface-container-low p-8 transition-colors hover:bg-surface-container"
      >
        <span className="material-symbols-outlined mb-2 text-[40px] text-primary" aria-hidden="true">
          cloud_upload
        </span>
        <span className="text-label-md font-medium text-on-surface">Tải ảnh lên</span>
        <span className="mt-1 text-body-sm text-on-surface-variant">
          JPG, PNG — mock picsum (MinIO bucket 2hands-commerce-product — task sau)
        </span>
      </button>

      {error ? <p className="mt-2 text-sm text-error">{error}</p> : null}

      {mediaUrls.length > 0 ? (
        <ul className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
          {mediaUrls.map((url, index) => (
            <li key={`${url}-${index}`} className="group relative aspect-square overflow-hidden rounded-lg border border-outline-variant">
              <img src={url} alt="" className="h-full w-full object-cover" />
              {index === 0 ? (
                <span className="absolute left-2 top-2 rounded bg-primary px-2 py-0.5 text-[10px] font-semibold text-on-primary">
                  Ảnh chính
                </span>
              ) : (
                <button
                  type="button"
                  disabled={disabled}
                  onClick={() => setPrimary(index)}
                  className="absolute left-2 top-2 rounded bg-surface/90 px-2 py-0.5 text-[10px] font-medium opacity-0 transition-opacity group-hover:opacity-100"
                >
                  Đặt làm chính
                </button>
              )}
              <button
                type="button"
                disabled={disabled}
                onClick={() => removeAt(index)}
                className="absolute right-2 top-2 rounded-full bg-error-container p-1 text-on-error-container"
                aria-label="Xóa ảnh"
              >
                <span className="material-symbols-outlined text-[18px]">close</span>
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
