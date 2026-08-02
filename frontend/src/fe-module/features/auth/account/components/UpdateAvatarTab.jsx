import { useEffect, useRef, useState } from "react";
import {
  requestAvatarUploadUrl,
  requestCoverUploadUrl,
  updateMyAvatar,
  updateMyCover,
} from "../../api/authApi";
import {
  AVATAR_ALLOWED_TYPES,
  AVATAR_MAX_BYTES,
  COVER_ALLOWED_TYPES,
  COVER_MAX_BYTES,
} from "../accountSchemas.js";
import { uploadProfileImage } from "../profileImageUpload.js";
import {
  AccountCard,
  PrimaryButton,
  SecondaryButton,
  TabPanelHeader,
} from "../../../../shared/ui/auth/authUi.jsx";

const DEFAULT_AVATAR = "https://i.pravatar.cc/256?img=12";
const DEFAULT_COVER =
  "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=1200&q=80";

function useProfileImageUpload({
  currentUrl,
  defaultUrl,
  allowedTypes,
  maxBytes,
  requestUploadUrl,
  persistImageUrl,
  getPublicUrl,
  invalidTypeMessage,
  maxSizeMessage,
}) {
  const [previewUrl, setPreviewUrl] = useState(currentUrl);
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const fileInputRef = useRef(null);
  const objectUrlRef = useRef(null);

  useEffect(() => {
    setPreviewUrl(currentUrl);
    setSelectedFile(null);
    setUploadProgress(null);
    setErrorMessage("");
  }, [currentUrl]);

  useEffect(() => {
    return () => {
      if (objectUrlRef.current) {
        URL.revokeObjectURL(objectUrlRef.current);
      }
    };
  }, []);

  const onPickFile = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setErrorMessage("");
    if (!allowedTypes.includes(file.type)) {
      setErrorMessage(invalidTypeMessage);
      return;
    }
    if (file.size > maxBytes) {
      setErrorMessage(maxSizeMessage);
      return;
    }

    if (objectUrlRef.current) {
      URL.revokeObjectURL(objectUrlRef.current);
    }
    const localUrl = URL.createObjectURL(file);
    objectUrlRef.current = localUrl;
    setPreviewUrl(localUrl);
    setSelectedFile(file);
    setUploadProgress(null);
  };

  const resetSelection = () => {
    if (objectUrlRef.current) {
      URL.revokeObjectURL(objectUrlRef.current);
      objectUrlRef.current = null;
    }
    setPreviewUrl(currentUrl || defaultUrl);
    setSelectedFile(null);
    setUploadProgress(null);
    setErrorMessage("");
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const onSubmit = async () => {
    if (!selectedFile || isSubmitting) return;

    setIsSubmitting(true);
    setErrorMessage("");
    setUploadProgress(0);

    try {
      await uploadProfileImage(selectedFile, {
        requestUploadUrl,
        persistImageUrl,
        getPublicUrl,
        onProgress: setUploadProgress,
      });
      resetSelection();
      return true;
    } catch (error) {
      setErrorMessage(error?.message || "Có lỗi xảy ra. Vui lòng thử lại.");
      setUploadProgress(null);
      throw error;
    } finally {
      setIsSubmitting(false);
    }
  };

  return {
    previewUrl: previewUrl || defaultUrl,
    selectedFile,
    uploadProgress,
    errorMessage,
    isSubmitting,
    fileInputRef,
    onPickFile,
    resetSelection,
    onSubmit,
  };
}

function UploadProgressBlock({ uploadProgress, selectedFile }) {
  if (uploadProgress === null) return null;

  return (
    <div className="rounded-xl border border-outline-variant/40 bg-surface-container-low/40 p-4">
      <div className="mb-2 flex justify-between text-xs font-bold">
        <span className="text-sky-500">
          {uploadProgress >= 100 ? "Hoàn tất" : "Đang tải lên..."}
          {selectedFile?.name ? ` ${selectedFile.name}` : ""}
        </span>
        <span>{uploadProgress}%</span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-outline-variant/30">
        <div
          className="h-full rounded-full bg-sky-500 transition-all duration-300"
          style={{ width: `${uploadProgress}%` }}
        />
      </div>
    </div>
  );
}

export function UpdateAvatarTab({ profile, refetch, onNotify }) {
  const currentAvatarUrl = profile?.profile?.avatar_url || DEFAULT_AVATAR;
  const currentCoverUrl = profile?.profile?.cover_url || DEFAULT_COVER;

  const avatar = useProfileImageUpload({
    currentUrl: currentAvatarUrl,
    defaultUrl: DEFAULT_AVATAR,
    allowedTypes: AVATAR_ALLOWED_TYPES,
    maxBytes: AVATAR_MAX_BYTES,
    requestUploadUrl: requestAvatarUploadUrl,
    persistImageUrl: (url) => updateMyAvatar({ avatar_url: url }),
    getPublicUrl: (meta) => meta.avatar_url,
    invalidTypeMessage: "Định dạng không được hỗ trợ. Chỉ JPG, PNG, WEBP.",
    maxSizeMessage: "Tệp vượt quá 5MB.",
  });

  const cover = useProfileImageUpload({
    currentUrl: currentCoverUrl,
    defaultUrl: DEFAULT_COVER,
    allowedTypes: COVER_ALLOWED_TYPES,
    maxBytes: COVER_MAX_BYTES,
    requestUploadUrl: requestCoverUploadUrl,
    persistImageUrl: (url) => updateMyCover({ cover_url: url }),
    getPublicUrl: (meta) => meta.cover_url,
    invalidTypeMessage: "Định dạng không được hỗ trợ. Chỉ JPG, PNG, WEBP.",
    maxSizeMessage: "Tệp vượt quá 5MB.",
  });

  const submitAvatar = async () => {
    try {
      await avatar.onSubmit();
      await refetch();
      onNotify?.({ variant: "success", message: "Cập nhật ảnh đại diện thành công." });
    } catch (error) {
      onNotify?.({ variant: "error", message: error?.message });
    }
  };

  const submitCover = async () => {
    try {
      await cover.onSubmit();
      await refetch();
      onNotify?.({ variant: "success", message: "Cập nhật ảnh bìa thành công." });
    } catch (error) {
      onNotify?.({ variant: "error", message: error?.message });
    }
  };

  return (
    <div className="space-y-10">
      <section>
        <TabPanelHeader
          title="Cập nhật ảnh đại diện"
          subtitle="Chọn một bức ảnh thể hiện sự chuyên nghiệp của bạn."
        />

        <AccountCard>
          <div className="grid grid-cols-1 gap-8 md:grid-cols-2 md:items-start">
            <div className="flex flex-col items-center">
              <button
                type="button"
                onClick={() => avatar.fileInputRef.current?.click()}
                className="group relative"
                aria-label="Chọn ảnh đại diện"
              >
                <div className="relative h-44 w-44 overflow-hidden rounded-full border-4 border-surface-container-low shadow-2xs md:h-56 md:w-56 shrink-0">
                  <img
                    src={avatar.previewUrl}
                    alt=""
                    className={[
                      "h-full w-full object-cover transition duration-300 group-hover:scale-105",
                      avatar.uploadProgress !== null && avatar.uploadProgress < 100 ? "opacity-50" : "",
                    ].join(" ")}
                  />
                  <div className="absolute inset-0 flex items-center justify-center bg-black/30 opacity-0 transition duration-200 group-hover:opacity-100">
                    <span className="material-symbols-outlined text-3xl text-white">photo_camera</span>
                  </div>
                </div>
              </button>
              <p className="mt-3 text-center text-xs font-bold text-on-surface-variant/70">Nhấp vào ảnh để thay đổi</p>
              <input
                ref={avatar.fileInputRef}
                type="file"
                accept={AVATAR_ALLOWED_TYPES.join(",")}
                className="sr-only"
                onChange={avatar.onPickFile}
              />
            </div>

            <div className="space-y-6">
              <div>
                <h3 className="mb-2 text-base font-extrabold text-on-surface">Hướng dẫn tải lên</h3>
                <ul className="space-y-2 text-xs font-medium text-on-surface-variant/70">
                  <li>• Sử dụng hình ảnh rõ nét, chụp chính diện khuôn mặt.</li>
                  <li>• Định dạng: JPG, PNG, WEBP.</li>
                  <li>• Tối đa 5MB. Độ phân giải khuyến dùng 500x500px.</li>
                </ul>
              </div>

              <UploadProgressBlock uploadProgress={avatar.uploadProgress} selectedFile={avatar.selectedFile} />
              {avatar.errorMessage ? <p className="text-xs text-error font-medium">{avatar.errorMessage}</p> : null}
            </div>
          </div>

          <div className="mt-8 flex justify-end gap-3 border-t border-outline-variant/30 pt-6">
            <SecondaryButton type="button" onClick={avatar.resetSelection} disabled={avatar.isSubmitting}>
              Hủy
            </SecondaryButton>
            <PrimaryButton
              type="button"
              onClick={submitAvatar}
              loading={avatar.isSubmitting}
              disabled={!avatar.selectedFile || avatar.isSubmitting}
            >
              Cập nhật ảnh đại diện
            </PrimaryButton>
          </div>
        </AccountCard>
      </section>

      <section>
        <TabPanelHeader
          title="Cập nhật ảnh bìa"
          subtitle="Chọn ảnh nền hiển thị trên trang hồ sơ của bạn."
        />

        <AccountCard>
          <div className="grid grid-cols-1 gap-8 md:grid-cols-2 md:items-start">
            <div className="flex flex-col items-center">
              <button
                type="button"
                onClick={() => cover.fileInputRef.current?.click()}
                className="group relative w-full max-w-xl"
                aria-label="Chọn ảnh bìa"
              >
                <div className="relative h-40 w-full overflow-hidden rounded-2xl border border-outline-variant/40 shadow-2xs md:h-52">
                  <img
                    src={cover.previewUrl}
                    alt=""
                    className={[
                      "h-full w-full object-cover transition duration-300 group-hover:scale-105",
                      cover.uploadProgress !== null && cover.uploadProgress < 100 ? "opacity-50" : "",
                    ].join(" ")}
                  />
                  <div className="absolute inset-0 flex items-center justify-center bg-black/30 opacity-0 transition duration-200 group-hover:opacity-100">
                    <span className="material-symbols-outlined text-3xl text-white">photo_camera</span>
                  </div>
                </div>
              </button>
              <p className="mt-3 text-center text-xs font-bold text-on-surface-variant/70">Nhấp vào ảnh bìa để thay đổi</p>
              <input
                ref={cover.fileInputRef}
                type="file"
                accept={COVER_ALLOWED_TYPES.join(",")}
                className="sr-only"
                onChange={cover.onPickFile}
              />
            </div>

            <div className="space-y-6">
              <div>
                <h3 className="mb-2 text-base font-extrabold text-on-surface">Hướng dẫn tải lên</h3>
                <ul className="space-y-2 text-xs font-medium text-on-surface-variant/70">
                  <li>• Sử dụng hình ảnh ngang, rõ nét, không chứa nội dung nhạy cảm.</li>
                  <li>• Định dạng: JPG, PNG, WEBP.</li>
                  <li>• Tối đa 5MB. Độ phân giải khuyến dùng 1500x500px.</li>
                </ul>
              </div>

              <UploadProgressBlock uploadProgress={cover.uploadProgress} selectedFile={cover.selectedFile} />
              {cover.errorMessage ? <p className="text-xs text-error font-medium">{cover.errorMessage}</p> : null}
            </div>
          </div>

          <div className="mt-8 flex justify-end gap-3 border-t border-outline-variant/30 pt-6">
            <SecondaryButton type="button" onClick={cover.resetSelection} disabled={cover.isSubmitting}>
              Hủy
            </SecondaryButton>
            <PrimaryButton
              type="button"
              onClick={submitCover}
              loading={cover.isSubmitting}
              disabled={!cover.selectedFile || cover.isSubmitting}
            >
              Cập nhật ảnh bìa
            </PrimaryButton>
          </div>
        </AccountCard>
      </section>
    </div>
  );
}
