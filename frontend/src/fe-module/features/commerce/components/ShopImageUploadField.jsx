import { useCallback, useEffect, useId, useRef, useState } from "react";
import { requestShopMediaUploadUrl, uploadShopMediaFile } from "../api/sellerShopApi";
import {
  SHOP_MEDIA_ALLOWED_TYPES,
  SHOP_MEDIA_KIND,
  SHOP_MEDIA_MAX_BYTES,
} from "../constants/shopMediaConstants";

const inputClass =
  "w-full rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-sm text-on-surface focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20";

function resolveUploadMediaKind(uploadMediaKind, aspectHint) {
  if (uploadMediaKind) return uploadMediaKind;
  if (aspectHint === "cover") return SHOP_MEDIA_KIND.SHOP_COVER;
  return SHOP_MEDIA_KIND.SHOP_AVATAR;
}

export function ShopImageUploadField({
  label,
  hint,
  icon = "image",
  aspectHint,
  uploadMediaKind,
  value,
  onChange,
  disabled = false,
}) {
  const inputId = useId();
  const inputRef = useRef(null);
  const [previewUrl, setPreviewUrl] = useState("");
  const [uploadProgress, setUploadProgress] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const objectUrlRef = useRef(null);

  useEffect(() => {
    return () => {
      if (objectUrlRef.current) {
        URL.revokeObjectURL(objectUrlRef.current);
      }
    };
  }, []);

  const handleFileChange = useCallback(
    async (event) => {
      const file = event.target.files?.[0];
      if (!file || disabled || isUploading) return;

      setErrorMessage("");

      if (!SHOP_MEDIA_ALLOWED_TYPES.includes(file.type)) {
        setErrorMessage("Định dạng không được hỗ trợ. Chỉ JPG, PNG, WEBP.");
        return;
      }
      if (file.size > SHOP_MEDIA_MAX_BYTES) {
        setErrorMessage("Tệp vượt quá 5MB.");
        return;
      }

      if (objectUrlRef.current) {
        URL.revokeObjectURL(objectUrlRef.current);
      }
      const objectUrl = URL.createObjectURL(file);
      objectUrlRef.current = objectUrl;
      setPreviewUrl(objectUrl);

      setIsUploading(true);
      setUploadProgress(10);

      try {
        const mediaKind = resolveUploadMediaKind(uploadMediaKind, aspectHint);
        const uploadMeta = await requestShopMediaUploadUrl({
          contentType: file.type,
          fileSizeBytes: file.size,
          mediaKind,
        });

        setUploadProgress(45);
        await uploadShopMediaFile(uploadMeta.uploadUrl, file);
        setUploadProgress(100);
        onChange?.(uploadMeta.mediaUrl);
      } catch (error) {
        setErrorMessage(error?.message || "Upload ảnh thất bại. Vui lòng thử lại.");
        setUploadProgress(null);
        if (objectUrlRef.current) {
          URL.revokeObjectURL(objectUrlRef.current);
          objectUrlRef.current = null;
        }
        setPreviewUrl(value || "");
      } finally {
        setIsUploading(false);
        if (inputRef.current) {
          inputRef.current.value = "";
        }
      }
    },
    [aspectHint, disabled, isUploading, onChange, uploadMediaKind, value],
  );

  const displayUrl = previewUrl || value;
  const fieldDisabled = disabled || isUploading;

  return (
    <div className="flex flex-col">
      <label htmlFor={inputId} className="mb-1 block text-label-md font-medium text-on-surface">
        {label}
      </label>

      <input
        ref={inputRef}
        id={inputId}
        type="file"
        accept={SHOP_MEDIA_ALLOWED_TYPES.join(",")}
        className="sr-only"
        disabled={fieldDisabled}
        onChange={handleFileChange}
      />

      <button
        type="button"
        disabled={fieldDisabled}
        onClick={() => inputRef.current?.click()}
        className={[
          "group flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-outline-variant bg-surface-container-low p-6 transition-colors hover:bg-surface-container",
          aspectHint === "cover" ? "min-h-[140px]" : "",
          fieldDisabled ? "cursor-not-allowed opacity-60" : "cursor-pointer",
        ].join(" ")}
      >
        {displayUrl ? (
          <img
            src={displayUrl}
            alt=""
            className={[
              "rounded-lg object-cover",
              aspectHint === "avatar" ? "h-24 w-24" : "h-28 w-full max-w-sm",
              isUploading ? "opacity-60" : "",
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

      {uploadProgress !== null ? (
        <div className="mt-2">
          <div className="mb-1 flex justify-between text-xs text-on-surface-variant">
            <span>{uploadProgress >= 100 ? "Hoàn tất" : "Đang tải lên..."}</span>
            <span>{uploadProgress}%</span>
          </div>
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-outline-variant/30">
            <div
              className="h-full rounded-full bg-primary transition-all duration-300"
              style={{ width: `${uploadProgress}%` }}
            />
          </div>
        </div>
      ) : (
        <p className="mt-1 text-xs text-on-surface-variant">
          Chọn ảnh để upload lên MinIO (JPG, PNG, WEBP, tối đa 5MB).
        </p>
      )}

      {errorMessage ? <p className="mt-1 text-sm text-error">{errorMessage}</p> : null}

      <label className="mt-2 block text-label-sm text-on-surface-variant" htmlFor={`${inputId}-url`}>
        Hoặc dán URL ảnh
      </label>
      <input
        id={`${inputId}-url`}
        type="url"
        className={inputClass}
        placeholder="http://localhost:9000/2hands-commerce-shop/..."
        value={value || ""}
        disabled={fieldDisabled}
        onChange={(event) => onChange?.(event.target.value)}
      />
    </div>
  );
}
