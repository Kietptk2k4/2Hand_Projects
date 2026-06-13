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
    <div className="rounded-lg border border-outline-variant bg-account-surface-low p-4">
      <div className="mb-2 flex justify-between text-xs font-semibold">
        <span className="text-primary">
          {uploadProgress >= 100 ? "Hoan tat" : "Đang tải len..."}
          {selectedFile?.name ? ` ${selectedFile.name}` : ""}
        </span>
        <span>{uploadProgress}%</span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-outline-variant/30">
        <div
          className="h-full rounded-full bg-primary-container transition-all duration-300"
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
    maxSizeMessage: "Tep vuot qua 5MB.",
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
    maxSizeMessage: "Tep vuot qua 5MB.",
  });

  const submitAvatar = async () => {
    try {
      await avatar.onSubmit();
      await refetch();
      onNotify?.({ variant: "success", message: "Cập nhật anh dai dien thành công." });
    } catch (error) {
      onNotify?.({ variant: "error", message: error?.message });
    }
  };

  const submitCover = async () => {
    try {
      await cover.onSubmit();
      await refetch();
      onNotify?.({ variant: "success", message: "Cập nhật anh bia thanh cong." });
    } catch (error) {
      onNotify?.({ variant: "error", message: error?.message });
    }
  };

  return (
    <div className="space-y-10">
      <section>
        <TabPanelHeader
          title="Cập nhật anh dai dien"
          subtitle="Chọn một buc anh the hien su chuyen nghiep của ban."
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
                <div className="relative h-48 w-48 overflow-hidden rounded-full border-4 border-white shadow-sm md:h-64 md:w-64">
                  <img
                    src={avatar.previewUrl}
                    alt=""
                    className={[
                      "h-full w-full object-cover transition",
                      avatar.uploadProgress !== null && avatar.uploadProgress < 100 ? "opacity-50" : "",
                    ].join(" ")}
                  />
                  <div className="absolute inset-0 flex items-center justify-center bg-black/20 opacity-0 transition group-hover:opacity-100">
                    <span className="text-3xl text-white">📷</span>
                  </div>
                </div>
              </button>
              <p className="mt-4 text-center text-xs font-semibold text-on-surface-variant">Nhap vào anh de thay doi</p>
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
                <h3 className="mb-3 text-base font-semibold text-on-surface">Huong dan tai len</h3>
                <ul className="space-y-2 text-sm text-on-surface-variant">
                  <li>• Su dung hinh anh ro net, chup chinh dien khuon mat.</li>
                  <li>• Dinh dang: JPG, PNG, WEBP.</li>
                  <li>• Toi da 5MB. Do phan giai khuyen dung 500x500px.</li>
                </ul>
              </div>

              <UploadProgressBlock uploadProgress={avatar.uploadProgress} selectedFile={avatar.selectedFile} />
              {avatar.errorMessage ? <p className="text-sm text-error">{avatar.errorMessage}</p> : null}
            </div>
          </div>

          <div className="mt-8 flex justify-end gap-3 border-t border-outline-variant pt-6">
            <SecondaryButton type="button" onClick={avatar.resetSelection} disabled={avatar.isSubmitting}>
              Huy
            </SecondaryButton>
            <PrimaryButton
              type="button"
              onClick={submitAvatar}
              loading={avatar.isSubmitting}
              disabled={!avatar.selectedFile || avatar.isSubmitting}
            >
              Cập nhật anh dai dien
            </PrimaryButton>
          </div>
        </AccountCard>
      </section>

      <section>
        <TabPanelHeader
          title="Cập nhật anh bia"
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
                <div className="relative h-40 w-full overflow-hidden rounded-xl border border-outline-variant shadow-sm md:h-52">
                  <img
                    src={cover.previewUrl}
                    alt=""
                    className={[
                      "h-full w-full object-cover transition",
                      cover.uploadProgress !== null && cover.uploadProgress < 100 ? "opacity-50" : "",
                    ].join(" ")}
                  />
                  <div className="absolute inset-0 flex items-center justify-center bg-black/20 opacity-0 transition group-hover:opacity-100">
                    <span className="text-3xl text-white">📷</span>
                  </div>
                </div>
              </button>
              <p className="mt-4 text-center text-xs font-semibold text-on-surface-variant">Nhap vào anh bia de thay doi</p>
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
                <h3 className="mb-3 text-base font-semibold text-on-surface">Huong dan tai len</h3>
                <ul className="space-y-2 text-sm text-on-surface-variant">
                  <li>• Su dung hinh anh ngang, ro net, khong chua noi dung nhay cam.</li>
                  <li>• Dinh dang: JPG, PNG, WEBP.</li>
                  <li>• Toi da 5MB. Do phan giai khuyen dung 1500x500px.</li>
                </ul>
              </div>

              <UploadProgressBlock uploadProgress={cover.uploadProgress} selectedFile={cover.selectedFile} />
              {cover.errorMessage ? <p className="text-sm text-error">{cover.errorMessage}</p> : null}
            </div>
          </div>

          <div className="mt-8 flex justify-end gap-3 border-t border-outline-variant pt-6">
            <SecondaryButton type="button" onClick={cover.resetSelection} disabled={cover.isSubmitting}>
              Huy
            </SecondaryButton>
            <PrimaryButton
              type="button"
              onClick={submitCover}
              loading={cover.isSubmitting}
              disabled={!cover.selectedFile || cover.isSubmitting}
            >
              Cập nhật anh bia
            </PrimaryButton>
          </div>
        </AccountCard>
      </section>
    </div>
  );
}
