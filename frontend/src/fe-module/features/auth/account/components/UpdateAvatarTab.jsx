import { useEffect, useRef, useState } from "react";
import { requestAvatarUploadUrl, updateMyAvatar } from "../../api/authApi";
import { AVATAR_ALLOWED_TYPES, AVATAR_MAX_BYTES } from "../accountSchemas.js";
import {
  AccountCard,
  PrimaryButton,
  SecondaryButton,
  TabPanelHeader,
} from "../../../../shared/ui/auth/authUi.jsx";

const DEFAULT_AVATAR = "https://i.pravatar.cc/256?img=12";

export function UpdateAvatarTab({ profile, refetch, onNotify }) {
  const currentUrl = profile?.profile?.avatar_url || DEFAULT_AVATAR;
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
    if (!AVATAR_ALLOWED_TYPES.includes(file.type)) {
      setErrorMessage("Định dạng không được hỗ trợ. Chỉ JPG, PNG, WEBP.");
      return;
    }
    if (file.size > AVATAR_MAX_BYTES) {
      setErrorMessage("Tep vuot qua 5MB.");
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
    setPreviewUrl(currentUrl);
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
      const uploadMeta = await requestAvatarUploadUrl({
        content_type: selectedFile.type,
        file_size_bytes: selectedFile.size,
      });

      setUploadProgress(40);

      await fetch(uploadMeta.upload_url, {
        method: "PUT",
        headers: { "Content-Type": selectedFile.type },
        body: selectedFile,
      });

      setUploadProgress(75);

      await updateMyAvatar({ avatar_url: uploadMeta.avatar_url });

      setUploadProgress(100);
      await refetch();
      resetSelection();
      onNotify?.({ variant: "success", message: "Cập nhật anh dai dien thành công." });
    } catch (error) {
      setErrorMessage(error?.message || "Có lỗi xảy ra. Vui lòng thử lại.");
      onNotify?.({ variant: "error", message: error?.message });
      setUploadProgress(null);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div>
      <TabPanelHeader
        title="Cập nhật anh dai dien"
        subtitle="Chọn một buc anh the hien su chuyen nghiep của ban."
      />

      <AccountCard>
        <div className="grid grid-cols-1 gap-8 md:grid-cols-2 md:items-start">
          <div className="flex flex-col items-center">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="group relative"
              aria-label="Chọn ảnh đại diện"
            >
              <div className="relative h-48 w-48 overflow-hidden rounded-full border-4 border-white shadow-sm md:h-64 md:w-64">
                <img
                  src={previewUrl}
                  alt=""
                  className={[
                    "h-full w-full object-cover transition",
                    uploadProgress !== null && uploadProgress < 100 ? "opacity-50" : "",
                  ].join(" ")}
                />
                <div className="absolute inset-0 flex items-center justify-center bg-black/20 opacity-0 transition group-hover:opacity-100">
                  <span className="text-3xl text-white">📷</span>
                </div>
              </div>
            </button>
            <p className="mt-4 text-center text-xs font-semibold text-on-surface-variant">Nhap vào anh de thay doi</p>
            <input
              ref={fileInputRef}
              type="file"
              accept={AVATAR_ALLOWED_TYPES.join(",")}
              className="sr-only"
              onChange={onPickFile}
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

            {uploadProgress !== null ? (
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
            ) : null}

            {errorMessage ? <p className="text-sm text-error">{errorMessage}</p> : null}
          </div>
        </div>

        <div className="mt-8 flex justify-end gap-3 border-t border-outline-variant pt-6">
          <SecondaryButton type="button" onClick={resetSelection} disabled={isSubmitting}>
            Huy
          </SecondaryButton>
          <PrimaryButton
            type="button"
            onClick={onSubmit}
            loading={isSubmitting}
            disabled={!selectedFile || isSubmitting}
          >
            Cập nhật anh dai dien
          </PrimaryButton>
        </div>
      </AccountCard>
    </div>
  );
}
