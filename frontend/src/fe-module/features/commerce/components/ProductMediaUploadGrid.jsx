import { useCallback, useId, useRef, useState } from "react";
import { requestProductMediaUploadUrl, uploadProductMediaFile } from "../api/sellerProductApi";
import {
  PRODUCT_MEDIA_ALLOWED_TYPES,
  PRODUCT_MEDIA_IMAGE_TYPES,
  PRODUCT_MEDIA_KIND,
  PRODUCT_MEDIA_MAX_BYTES,
  PRODUCT_MEDIA_MAX_COUNT,
  PRODUCT_MEDIA_MAX_VIDEO_BYTES,
  PRODUCT_MEDIA_MAX_VIDEO_COUNT,
  PRODUCT_MEDIA_VIDEO_TYPES,
} from "../constants/productMediaConstants";

function isVideoUrl(url) {
  return /\/videos\//.test(url) || /\.(mp4|webm)(\?|$)/i.test(url);
}

function countByType(urls) {
  let images = 0;
  let videos = 0;
  for (const url of urls) {
    if (isVideoUrl(url)) {
      videos += 1;
    } else {
      images += 1;
    }
  }
  return { images, videos };
}

export function ProductMediaUploadGrid({ productId, mediaUrls, disabled, onChange, error }) {
  const inputId = useId();
  const inputRef = useRef(null);
  const [uploadError, setUploadError] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(null);

  const { images: imageCount, videos: videoCount } = countByType(mediaUrls);
  const canAddImage = imageCount < PRODUCT_MEDIA_MAX_COUNT;
  const canAddVideo = videoCount < PRODUCT_MEDIA_MAX_VIDEO_COUNT;
  const canAddMore = canAddImage || canAddVideo;

  const handleFiles = useCallback(
    async (event) => {
      const files = Array.from(event.target.files || []);
      if (!files.length) return;

      if (!productId) {
        setUploadError("Vui lòng lưu thông tin sản phẩm (bước 1) trước khi tải ảnh/video.");
        event.target.value = "";
        return;
      }

      let slotsImages = PRODUCT_MEDIA_MAX_COUNT - imageCount;
      let slotsVideos = PRODUCT_MEDIA_MAX_VIDEO_COUNT - videoCount;

      if (slotsImages <= 0 && slotsVideos <= 0) {
        setUploadError(
          `Tối đa ${PRODUCT_MEDIA_MAX_COUNT} ảnh và ${PRODUCT_MEDIA_MAX_VIDEO_COUNT} video cho mỗi sản phẩm.`,
        );
        event.target.value = "";
        return;
      }

      const filesToUpload = [];
      for (const file of files) {
        const isImage = PRODUCT_MEDIA_IMAGE_TYPES.includes(file.type);
        const isVideo = PRODUCT_MEDIA_VIDEO_TYPES.includes(file.type);

        if (!isImage && !isVideo) {
          setUploadError("Định dạng không được hỗ trợ. Chọn JPG, PNG, WEBP, MP4 hoặc WebM.");
          event.target.value = "";
          return;
        }
        if (isImage) {
          if (slotsImages <= 0) continue;
          if (file.size > PRODUCT_MEDIA_MAX_BYTES) {
            setUploadError("Ảnh vượt quá 5MB.");
            event.target.value = "";
            return;
          }
          slotsImages -= 1;
        }
        if (isVideo) {
          if (slotsVideos <= 0) continue;
          if (file.size > PRODUCT_MEDIA_MAX_VIDEO_BYTES) {
            setUploadError("Video vượt quá 50MB.");
            event.target.value = "";
            return;
          }
          slotsVideos -= 1;
        }
        filesToUpload.push(file);
      }

      if (!filesToUpload.length) {
        setUploadError("Đã đủ số lượng ảnh/video cho phép.");
        event.target.value = "";
        return;
      }

      setUploadError("");
      setIsUploading(true);
      setUploadProgress(0);

      const next = [...mediaUrls];

      try {
        for (let index = 0; index < filesToUpload.length; index++) {
          const file = filesToUpload[index];
          const isVideo = PRODUCT_MEDIA_VIDEO_TYPES.includes(file.type);
          const mediaKind = isVideo ? PRODUCT_MEDIA_KIND.PRODUCT_VIDEO : PRODUCT_MEDIA_KIND.PRODUCT_IMAGE;

          const baseProgress = Math.round((index / filesToUpload.length) * 100);
          setUploadProgress(baseProgress + 5);

          const uploadMeta = await requestProductMediaUploadUrl(productId, {
            contentType: file.type,
            fileSizeBytes: file.size,
            mediaKind,
          });

          setUploadProgress(baseProgress + 40);
          await uploadProductMediaFile(uploadMeta.uploadUrl, file);
          next.push(uploadMeta.mediaUrl);
          setUploadProgress(Math.round(((index + 1) / filesToUpload.length) * 100));
        }

        onChange?.(next);
      } catch (uploadErr) {
        setUploadError(uploadErr?.message || "Upload ảnh/video thất bại. Vui lòng thử lại.");
      } finally {
        setIsUploading(false);
        setUploadProgress(null);
        if (inputRef.current) {
          inputRef.current.value = "";
        }
      }
    },
    [imageCount, mediaUrls, onChange, productId, videoCount],
  );

  const removeAt = (index) => {
    onChange?.(mediaUrls.filter((_, i) => i !== index));
  };

  const setPrimary = (index) => {
    if (index === 0 || isVideoUrl(mediaUrls[index])) return;
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
        disabled={fieldDisabled || !canAddMore}
        onClick={() => inputRef.current?.click()}
        className="flex w-full flex-col items-center justify-center rounded-xl border-2 border-dashed border-outline-variant bg-surface-container-low p-8 transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:opacity-60"
      >
        <span className="material-symbols-outlined mb-2 text-[40px] text-primary" aria-hidden="true">
          cloud_upload
        </span>
        <span className="text-label-md font-medium text-on-surface">
          {isUploading ? "Đang tải lên..." : "Tải ảnh/video lên"}
        </span>
        <span className="mt-1 text-body-sm text-on-surface-variant">
          JPG, PNG, WEBP (5MB) — tối đa {PRODUCT_MEDIA_MAX_COUNT} ảnh; MP4, WebM (50MB) — tối đa{" "}
          {PRODUCT_MEDIA_MAX_VIDEO_COUNT} video (MinIO)
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
          {mediaUrls.map((url, index) => {
            const video = isVideoUrl(url);
            const isPrimary = !video && index === 0;

            return (
              <li
                key={`${url}-${index}`}
                className="group relative aspect-square overflow-hidden rounded-lg border border-outline-variant"
              >
                {video ? (
                  <video src={url} className="h-full w-full object-cover" controls muted playsInline />
                ) : (
                  <img src={url} alt="" className="h-full w-full object-cover" />
                )}
                {video ? (
                  <span className="absolute left-2 top-2 rounded bg-secondary px-2 py-0.5 text-[10px] font-semibold text-on-secondary">
                    Video
                  </span>
                ) : isPrimary ? (
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
                  aria-label={video ? "Xóa video" : "Xóa ảnh"}
                >
                  <span className="material-symbols-outlined text-[18px]">close</span>
                </button>
              </li>
            );
          })}
        </ul>
      ) : null}
    </div>
  );
}
