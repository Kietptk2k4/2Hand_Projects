import { useRef } from "react";
import { MAX_COMMENT_LENGTH } from "../constants/commentConstants";
import { POST_MEDIA_ACCEPT } from "../constants/createPostConstants";

function CommentMediaPreview({ mediaItems, onRemove, disabled }) {
  if (!mediaItems.length) return null;

  return (
    <div className="flex flex-wrap gap-2 px-1 pt-2">
      {mediaItems.map((item) => (
        <div
          key={item.id}
          className="relative h-16 w-16 shrink-0 overflow-hidden rounded-md border border-outline-variant bg-surface-container-high"
        >
          {item.type === "VIDEO" ? (
            <video
              src={item.previewUrl}
              className="h-full w-full object-cover"
              muted
              playsInline
              aria-hidden="true"
            />
          ) : (
            <img src={item.previewUrl} alt="" className="h-full w-full object-cover" />
          )}
          {item.status === "uploading" || item.status === "pending" ? (
            <div className="absolute inset-0 flex items-center justify-center bg-on-background/40">
              <span
                className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent"
                aria-label="Đang tải"
              />
            </div>
          ) : null}
          {item.status === "error" ? (
            <div
              className="absolute inset-0 flex items-center justify-center bg-error/20 p-1 text-center text-[10px] text-error"
              title={item.errorMessage}
            >
              Lỗi
            </div>
          ) : null}
          <button
            type="button"
            onClick={() => onRemove(item.id)}
            disabled={disabled}
            className="absolute right-0.5 top-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-on-background/70 text-on-primary hover:bg-on-background disabled:opacity-50"
            aria-label="Xóa media"
          >
            <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
              close
            </span>
          </button>
        </div>
      ))}
    </div>
  );
}

export function CommentComposer({
  value,
  onChange,
  onSubmit,
  mediaUpload,
  placeholder = "Viết bình luận...",
  disabled = false,
  isSubmitting = false,
  inputRef,
  variant = "default",
  onClearError,
}) {
  const fileInputRef = useRef(null);
  const isCompact = variant === "compact";

  const canSubmit =
    !disabled &&
    !isSubmitting &&
    !mediaUpload?.isUploading &&
    !mediaUpload?.hasUploadError &&
    value.trim().length > 0;

  const handleKeyDown = (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      if (canSubmit) onSubmit?.();
    }
  };

  const handlePickFiles = (event) => {
    const files = event.target.files;
    if (files?.length) {
      mediaUpload?.addFiles(files);
    }
    event.target.value = "";
  };

  const inputClassName = isCompact
    ? "w-full rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary disabled:opacity-60"
    : "flex-1 rounded-full border border-outline-variant bg-surface-container-low px-4 py-2 text-sm outline-none transition focus:border-primary focus:ring-1 focus:ring-primary disabled:opacity-60";

  return (
    <div className="min-w-0 flex-1 space-y-0">
      <CommentMediaPreview
        mediaItems={mediaUpload?.mediaItems || []}
        onRemove={mediaUpload?.removeMedia}
        disabled={disabled || isSubmitting}
      />
      <div className={isCompact ? "space-y-2" : "flex items-center gap-2"}>
        <div className={isCompact ? "" : "flex min-w-0 flex-1 items-center gap-2"}>
          <input
            ref={inputRef}
            type="text"
            value={value}
            onChange={(event) => {
              onChange?.(event.target.value);
              onClearError?.();
            }}
            onKeyDown={handleKeyDown}
            maxLength={MAX_COMMENT_LENGTH}
            placeholder={placeholder}
            disabled={disabled || isSubmitting}
            className={inputClassName}
          />
          {!isCompact ? (
            <>
              <input
                ref={fileInputRef}
                type="file"
                accept={POST_MEDIA_ACCEPT}
                multiple
                className="hidden"
                onChange={handlePickFiles}
                disabled={disabled || isSubmitting || !mediaUpload?.canAddMore}
              />
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={disabled || isSubmitting || !mediaUpload?.canAddMore}
                className="shrink-0 rounded-full p-2 text-on-surface-variant transition hover:bg-surface-container-high hover:text-primary disabled:opacity-50"
                aria-label="Đính kèm ảnh hoặc video"
              >
                <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                  attach_file
                </span>
              </button>
            </>
          ) : null}
        </div>
        {isCompact ? (
          <div className="flex items-center gap-2">
            <input
              ref={fileInputRef}
              type="file"
              accept={POST_MEDIA_ACCEPT}
              multiple
              className="hidden"
              onChange={handlePickFiles}
              disabled={disabled || isSubmitting || !mediaUpload?.canAddMore}
            />
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              disabled={disabled || isSubmitting || !mediaUpload?.canAddMore}
              className="rounded-lg border border-outline-variant px-2.5 py-1.5 text-xs font-medium text-on-surface-variant hover:border-primary hover:text-primary disabled:opacity-50"
            >
              Đính kèm
            </button>
            <button
              type="button"
              onClick={onSubmit}
              disabled={!canSubmit}
              className="rounded-lg bg-primary px-3 py-1.5 text-xs font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
            >
              {isSubmitting ? "Đang gửi..." : "Gửi"}
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={onSubmit}
            disabled={!canSubmit}
            className="shrink-0 rounded-full p-2 text-primary hover:bg-primary-fixed disabled:opacity-50"
            aria-label="Gửi bình luận"
          >
            {isSubmitting ? (
              <span
                className="inline-block h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent"
                aria-hidden="true"
              />
            ) : (
              <span className="material-symbols-outlined" aria-hidden="true">
                send
              </span>
            )}
          </button>
        )}
      </div>
    </div>
  );
}