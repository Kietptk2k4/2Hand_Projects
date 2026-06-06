import { useCallback, useId, useRef, useState } from "react";
import { requestProductMediaUploadUrl, uploadProductMediaFile } from "../api/sellerProductApi";
import {
  PRODUCT_MEDIA_ALLOWED_TYPES,
  PRODUCT_MEDIA_KIND,
  PRODUCT_MEDIA_MAX_BYTES,
  PRODUCT_MEDIA_MAX_COUNT,
} from "../constants/productMediaConstants";

export function ProductMediaUploadGrid({ productId, mediaUrls, disabled, onChange, error }) {
  const inputId = useId();
  const inputRef = useRef(null);
  const [uploadError, setUploadError] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(null);

  const handleFiles = useCallback(
    async (event) => {
      const files = Array.from(event.target.files || []);
      if (!files.length) return;

      if (!productId) {
        setUploadError("Vui lòng lưu thông tin sản phẩm (bước 1) trước khi tải ảnh.");
        event.target.value = "";
        return;
      }

      const remainingSlots = PRODUCT_MEDIA_MAX_COUNT - mediaUrls.length;
      if (remainingSlots <= 0) {
        setUploadError(`Tối đa ${PRODUCT_MEDIA_MAX_COUNT} ảnh cho mỗi sản phẩm.`);
        event.target.value = "";
        return;
      }

      const filesToUpload = files.slice(0, remainingSlots);
      setUploadError("");
      setIsUploading(true);
      setUploadProgress(0);

      const next = [...mediaUrls];

      try {
        for (let index = 0; index < filesToUpload.length; index++) {
          const file = filesToUpload[index];

          if (!PRODUCT_MEDIA_ALLOWED_TYPES.includes(file.type)) {
            throw { message: "Định dạng không được hỗ trợ. Chỉ JPG, PNG, WEBP." };
          }
          if (file.size > PRODUCT_MEDIA_MAX_BYTES) {
            throw { message: "Tệp vượt quá 5MB." };
          }

          const baseProgress = Math.round((index / filesToUpload.length) * 100);
          setUploadProgress(baseProgress + 5);

          const uploadMeta = await requestProductMediaUploadUrl(productId, {
            contentType: file.type,
            fileSizeBytes: file.size,
            mediaKind: PRODUCT_MEDIA_KIND.PRODUCT_IMAGE,
          });

          setUploadProgress(baseProgress + 40);
          await uploadProductMediaFile(uploadMeta.uploadUrl, file);
          next.push(uploadMeta.mediaUrl);
          setUploadProgress(Math.round(((index + 1) / filesToUpload.length) * 100));
        }

        onChange?.(next);
      } catch (uploadErr) {
        setUploadError(uploadErr?.message || "Upload ảnh thất bại. Vui lòng thử lại.");
      } finally {
        setIsUploading(false);
        setUploadProgress(null);
        if (inputRef.current) {
          inputRef.current.value = "";
        }
      }
    },
    [mediaUrls, onChange, productId],
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

  const fieldDisabled = disabled || isUploading;
  const displayError = error || uploadError;

  return (
    <div>
      <input
        ref={inputRef}
        id={inputId}
        type="file"
        accept={PRODUCT_MEDIA_ALLOWED_TYPES.join(",")}
        multiple
        className="sr-only"
        disabled={fieldDisabled}
        onChange={handleFiles}
      />

      <button
        type="button"
        disabled={fieldDisabled || mediaUrls.length >= PRODUCT_MEDIA_MAX_COUNT}
        onClick={() => inputRef.current?.click()}
        className="flex w-full flex-col items-center justify-center rounded-xl border-2 border-dashed border-outline-variant bg-surface-container-low p-8 transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:opacity-60"
      >
        <span className="material-symbols-outlined mb-2 text-[40px] text-primary" aria-hidden="true">
          cloud_upload
        </span>
        <span className="text-label-md font-medium text-on-surface">
          {isUploading ? "Đang tải lên..." : "Tải ảnh lên"}
        </span>
        <span className="mt-1 text-body-sm text-on-surface-variant">
          JPG, PNG, WEBP — tối đa 5MB/ảnh, {PRODUCT_MEDIA_MAX_COUNT} ảnh/sản phẩm (MinIO)
        </span>
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
      ) : null}

      {displayError ? <p className="mt-2 text-sm text-error">{displayError}</p> : null}

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
                  disabled={fieldDisabled}
                  onClick={() => setPrimary(index)}
                  className="absolute left-2 top-2 rounded bg-surface/90 px-2 py-0.5 text-[10px] font-medium opacity-0 transition-opacity group-hover:opacity-100"
                >
                  Đặt làm chính
                </button>
              )}
              <button
                type="button"
                disabled={fieldDisabled}
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
