import { useCallback, useEffect, useId, useRef, useState } from "react";

const inputClass =
  "w-full rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20";

function buildMockPicsumUrl(kind, seed) {
  if (kind === "avatar") {
    return `https://picsum.photos/seed/${seed}/200/200`;
  }
  return `https://picsum.photos/seed/${seed}/1200/400`;
}

/**
 * MVP: chọn file → preview local + gán mock URL (picsum).
 * Upload MinIO qua presigned URL — task sau.
 */
export function ShopImageUploadField({
  label,
  hint,
  icon = "image",
  aspectHint,
  value,
  onChange,
  disabled = false,
}) {
  const inputId = useId();
  const inputRef = useRef(null);
  const [previewUrl, setPreviewUrl] = useState("");

  useEffect(() => {
    return () => {
      if (previewUrl?.startsWith("blob:")) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  const handleFileChange = useCallback(
    (event) => {
      const file = event.target.files?.[0];
      if (!file) return;

      if (previewUrl?.startsWith("blob:")) {
        URL.revokeObjectURL(previewUrl);
      }

      const objectUrl = URL.createObjectURL(file);
      setPreviewUrl(objectUrl);

      const seed = `create-shop-${aspectHint}-${file.name}-${Date.now()}`.replace(/\s/g, "-");
      const mockUrl = buildMockPicsumUrl(aspectHint, seed);
      onChange?.(mockUrl);
    },
    [aspectHint, onChange, previewUrl],
  );

  const displayUrl = previewUrl || value;

  return (
    <div className="flex flex-col">
      <label htmlFor={inputId} className="mb-1 block text-label-md font-medium text-on-surface">
        {label}
      </label>

      <input
        ref={inputRef}
        id={inputId}
        type="file"
        accept="image/*"
        className="sr-only"
        disabled={disabled}
        onChange={handleFileChange}
      />

      <button
        type="button"
        disabled={disabled}
        onClick={() => inputRef.current?.click()}
        className={[
          "group flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-outline-variant bg-surface-container-low p-6 transition-colors hover:bg-surface-container",
          aspectHint === "cover" ? "min-h-[140px]" : "",
          disabled ? "cursor-not-allowed opacity-60" : "cursor-pointer",
        ].join(" ")}
      >
        {displayUrl ? (
          <img
            src={displayUrl}
            alt=""
            className={[
              "rounded-lg object-cover",
              aspectHint === "avatar" ? "h-24 w-24" : "h-28 w-full max-w-sm",
            ].join(" ")}
          />
        ) : (
          <>
            <span
              className="material-symbols-outlined mb-1 text-4xl text-on-surface-variant transition-colors group-hover:text-primary"
              aria-hidden="true"
            >
              {icon}
            </span>
            <span className="text-center text-label-sm text-on-surface-variant">{hint}</span>
          </>
        )}
      </button>

      <p className="mt-1 text-xs text-on-surface-variant">
        Ảnh dùng URL mock (picsum). Upload MinIO — task sau.
      </p>

      <label className="mt-2 block text-label-sm text-on-surface-variant" htmlFor={`${inputId}-url`}>
        Hoặc dán URL ảnh
      </label>
      <input
        id={`${inputId}-url`}
        type="url"
        className={inputClass}
        placeholder="https://..."
        value={value || ""}
        disabled={disabled}
        onChange={(event) => onChange?.(event.target.value)}
      />
    </div>
  );
}
