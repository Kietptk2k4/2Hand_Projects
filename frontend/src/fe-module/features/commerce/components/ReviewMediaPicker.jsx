import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  ACCEPT_REVIEW_MEDIA,
  MAX_REVIEW_MEDIA,
} from "../constants/reviewMediaConstants";
import { validateReviewMediaSelection } from "../utils/reviewMediaValidation";

function buildPreviewEntry(file) {
  const isVideo = file.type.startsWith("video/");
  return {
    id: `${file.name}-${file.size}-${file.lastModified}`,
    file,
    previewUrl: URL.createObjectURL(file),
    isVideo,
  };
}

export function ReviewMediaPicker({
  existingMedia = [],
  existingMediaCount,
  maxMedia = MAX_REVIEW_MEDIA,
  disabled = false,
  onFilesChange,
}) {
  const inputRef = useRef(null);
  const [entries, setEntries] = useState([]);
  const [validationError, setValidationError] = useState("");

  const persistedCount =
    existingMediaCount ?? (Array.isArray(existingMedia) ? existingMedia.length : 0);
  const remainingSlots = Math.max(0, maxMedia - persistedCount - entries.length);
  const totalSelected = persistedCount + entries.length;

  useEffect(() => {
    onFilesChange?.(entries.map((entry) => entry.file));
  }, [entries, onFilesChange]);

  useEffect(
    () => () => {
      entries.forEach((entry) => URL.revokeObjectURL(entry.previewUrl));
    },
    [entries],
  );

  const canAddMore = remainingSlots > 0 && !disabled;

  const helperText = useMemo(() => {
    if (disabled) {
      return "Không thể thêm ảnh/video cho đánh giá này.";
    }
    return `JPEG, PNG, WebP (tối đa 5MB) · MP4, WebM (tối đa 50MB). Đã chọn ${totalSelected}/${maxMedia}.`;
  }, [disabled, maxMedia, totalSelected]);

  const handleFilesPicked = useCallback(
    (fileList) => {
      setValidationError("");
      const result = validateReviewMediaSelection(fileList, persistedCount + entries.length);
      if (!result.valid) {
        setValidationError(result.message);
        return;
      }

      const nextEntries = result.files.map(buildPreviewEntry);
      setEntries((prev) => [...prev, ...nextEntries]);
      if (inputRef.current) {
        inputRef.current.value = "";
      }
    },
    [entries.length, persistedCount],
  );

  const handleInputChange = useCallback(
    (event) => {
      handleFilesPicked(event.target.files);
    },
    [handleFilesPicked],
  );

  const handleRemove = useCallback((id) => {
    setEntries((prev) => {
      const target = prev.find((entry) => entry.id === id);
      if (target) {
        URL.revokeObjectURL(target.previewUrl);
      }
      return prev.filter((entry) => entry.id !== id);
    });
    setValidationError("");
  }, []);

  const openPicker = useCallback(() => {
    if (!canAddMore) return;
    inputRef.current?.click();
  }, [canAddMore]);

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        accept={ACCEPT_REVIEW_MEDIA}
        multiple
        className="hidden"
        disabled={!canAddMore}
        onChange={handleInputChange}
      />

      <button
        type="button"
        onClick={openPicker}
        disabled={!canAddMore}
        className={[
          "w-full rounded-lg border-2 border-dashed p-6 text-center transition-colors",
          canAddMore
            ? "border-outline-variant bg-surface-container-low hover:border-primary hover:bg-surface-container"
            : "cursor-not-allowed border-outline-variant/60 bg-surface-container-low opacity-60",
        ].join(" ")}
      >
        <span className="material-symbols-outlined mb-2 text-4xl text-on-surface-variant" aria-hidden="true">
          cloud_upload
        </span>
        <p className="text-label-md text-on-surface">
          {canAddMore ? "Chọn ảnh hoặc video" : "Đã đạt giới hạn file"}
        </p>
        <p className="mt-1 text-body-sm text-on-surface-variant">
          {canAddMore
            ? `Còn thêm được ${remainingSlots} file`
            : `Tối đa ${maxMedia} file mỗi đánh giá`}
        </p>
      </button>

      <p className="mt-2 text-body-sm text-on-surface-variant">{helperText}</p>

      {validationError ? (
        <p className="mt-2 text-body-sm text-error">{validationError}</p>
      ) : null}

      {existingMedia?.length > 0 ? (
        <ul className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
          {existingMedia.map((item) => {
            const isVideo = String(item.type).toUpperCase() === "VIDEO";
            return (
              <li
                key={item.id || item.url}
                className="relative overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high"
              >
                {isVideo ? (
                  <video
                    src={item.url}
                    className="aspect-square w-full object-cover"
                    muted
                    playsInline
                  />
                ) : (
                  <img src={item.url} alt="" className="aspect-square w-full object-cover" />
                )}
                <span className="absolute bottom-1 left-1 rounded bg-on-surface/70 px-1.5 py-0.5 text-label-sm text-on-primary">
                  Đã tải
                </span>
                {isVideo ? (
                  <span className="absolute right-1 top-1 rounded bg-on-surface/70 px-1.5 py-0.5 text-label-sm text-on-primary">
                    Video
                  </span>
                ) : null}
              </li>
            );
          })}
        </ul>
      ) : null}

      {entries.length > 0 ? (
        <ul className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
          {entries.map((entry) => (
            <li
              key={entry.id}
              className="relative overflow-hidden rounded-lg border border-outline-variant bg-surface-container-high"
            >
              {entry.isVideo ? (
                <video
                  src={entry.previewUrl}
                  className="aspect-square w-full object-cover"
                  muted
                  playsInline
                />
              ) : (
                <img
                  src={entry.previewUrl}
                  alt=""
                  className="aspect-square w-full object-cover"
                />
              )}
              <button
                type="button"
                onClick={() => handleRemove(entry.id)}
                disabled={disabled}
                className="absolute right-1 top-1 flex h-7 w-7 items-center justify-center rounded-full bg-on-surface/70 text-on-primary hover:bg-on-surface disabled:opacity-50"
                aria-label="Xóa file"
              >
                <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                  close
                </span>
              </button>
              {entry.isVideo ? (
                <span className="absolute bottom-1 left-1 rounded bg-on-surface/70 px-1.5 py-0.5 text-label-sm text-on-primary">
                  Video
                </span>
              ) : null}
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
