import { useEffect, useRef, useState } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { POST_MEDIA_ACCEPT, VISIBILITY_OPTIONS } from "../constants/createPostConstants";
import { DEFAULT_WORKSPACE_LABEL } from "../constants/socialUiStrings";
import { formatVndPrice } from "../utils/formatPrice";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { ProductPickerPanel } from "./ProductPickerPanel";

const DEFAULT_AVATAR = "https://i.pravatar.cc/96?img=11";

export function PostFormModal({
  mode = "create",
  title,
  titleId,
  onClose,
  onToast,
  form,
  openFilePickerOnMount = false,
  isLoadingInitial = false,
  loadError = "",
}) {
  const { user } = useAuthSession();
  const { isWriteBlocked, suspendMessage } = useSocialWriteBlock();
  const fileInputRef = useRef(null);
  const [showProductPicker, setShowProductPicker] = useState(false);
  const [showVisibilityMenu, setShowVisibilityMenu] = useState(false);

  const isEdit = mode === "edit";
  const zIndexClass = isEdit ? "z-[60]" : "z-50";

  const {
    caption,
    setCaption,
    visibility,
    setVisibility,
    allowComments,
    setAllowComments,
    hashtags,
    hashtagInput,
    setHashtagInput,
    addHashtag,
    removeHashtag,
    productTags,
    addProductTag,
    removeProductTag,
    updateProductTagPrice,
    mediaItems,
    activeMedia,
    activeMediaIndex,
    setActiveMediaIndex,
    addFiles,
    removeMedia,
    submit,
    submitUpdate,
    resetForm,
    isSubmitting,
    isUploadingMedia,
    globalError,
    fieldErrors,
  } = form;

  const displayName = user?.display_name || user?.email || DEFAULT_WORKSPACE_LABEL;
  const avatarUrl = user?.avatar_url || user?.profile?.avatar_url || DEFAULT_AVATAR;
  const visibilityMeta = VISIBILITY_OPTIONS.find((item) => item.value === visibility);

  const isBusy = isSubmitting || isUploadingMedia || isLoadingInitial || isWriteBlocked;

  useEffect(() => {
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
      if (!isEdit) resetForm?.();
    };
  }, [isEdit, resetForm]);

  useEffect(() => {
    const onKeyDown = (event) => {
      if (event.key === "Escape") onClose?.();
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  useEffect(() => {
    if (!openFilePickerOnMount || isEdit) return;
    const timer = window.setTimeout(() => fileInputRef.current?.click(), 150);
    return () => window.clearTimeout(timer);
  }, [openFilePickerOnMount, isEdit]);

  const onFileChange = (event) => {
    addFiles(event.target.files);
    event.target.value = "";
  };

  const showComingSoon = () => onToast?.("Tính năng đang được phát triển.");

  const handlePublish = () => submit?.(true);
  const handleSaveDraft = () => submit?.(false);
  const handleUpdate = () => submitUpdate?.();

  return (
    <>
      <div
        className={`fixed inset-0 ${zIndexClass} flex items-center justify-center bg-on-background/40 p-4 backdrop-blur-md md:p-8`}
        role="presentation"
        onClick={onClose}
      >
        <div
          className="flex max-h-[92vh] w-full max-w-[1024px] flex-col overflow-hidden rounded-xl border border-outline-variant/30 bg-surface-container-lowest shadow-lg ring-1 ring-outline-variant/30"
          role="dialog"
          aria-modal="true"
          aria-labelledby={titleId}
          onClick={(event) => event.stopPropagation()}
        >
          <header className="flex items-center justify-between border-b border-outline-variant/50 bg-surface px-6 py-3">
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={onClose}
                className="rounded-full p-2 transition-colors hover:bg-surface-variant"
                aria-label="Đóng"
              >
                <span className="material-symbols-outlined text-on-surface-variant" aria-hidden="true">
                  close
                </span>
              </button>
              <h2 id={titleId} className="text-xl font-semibold text-on-surface">
                {title}
              </h2>
            </div>
            {!isEdit ? (
              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={handleSaveDraft}
                  disabled={isBusy}
                  className="flex items-center gap-1 rounded-full bg-primary-fixed px-4 py-2 text-sm font-medium text-primary transition-colors hover:bg-primary-fixed-dim disabled:opacity-50"
                >
                  <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                    draft
                  </span>
                  Lưu nháp
                </button>
                <button
                  type="button"
                  onClick={handlePublish}
                  disabled={isBusy}
                  className="rounded-full bg-primary px-5 py-2 text-sm font-semibold text-on-primary shadow-sm transition-colors hover:bg-[#0050cb] disabled:opacity-50"
                >
                  {isSubmitting ? "Đang đăng..." : "Đăng bài"}
                </button>
              </div>
            ) : null}
          </header>

          {globalError ? (
            <div className="border-b border-error/30 bg-error-container/30 px-6 py-2 text-sm text-on-error-container">
              {globalError}
            </div>
          ) : null}

          {loadError ? (
            <div className="border-b border-error/30 bg-error-container/30 px-6 py-2 text-sm text-on-error-container">
              {loadError}
            </div>
          ) : null}

          <div className="relative flex flex-1 flex-col overflow-hidden md:flex-row">
            {isLoadingInitial ? (
              <div className="absolute inset-0 z-20 flex items-center justify-center bg-surface-container-lowest/80">
                <div
                  className="h-10 w-10 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                  aria-label="Đang tải bài viết"
                />
              </div>
            ) : null}

            <div className="flex w-full flex-col border-outline-variant/30 md:w-[55%] md:border-r">
              <div className="relative flex min-h-[280px] flex-1 items-center justify-center overflow-hidden bg-surface-container-high md:min-h-[360px]">
                {activeMedia?.previewUrl || activeMedia?.mediaUrl ? (
                  activeMedia.type === "VIDEO" ? (
                    <video
                      src={activeMedia.previewUrl || activeMedia.mediaUrl}
                      className="absolute inset-0 h-full w-full object-cover"
                      controls
                    />
                  ) : (
                    <img
                      src={activeMedia.previewUrl || activeMedia.mediaUrl}
                      alt=""
                      className="absolute inset-0 h-full w-full object-cover"
                    />
                  )
                ) : (
                  <div className="flex flex-col items-center gap-2 text-on-surface-variant">
                    <span className="material-symbols-outlined text-5xl" aria-hidden="true">
                      add_photo_alternate
                    </span>
                    <p className="text-sm">Thêm ảnh hoặc video</p>
                  </div>
                )}

                <div className="pointer-events-none absolute inset-0 bg-gradient-to-b from-on-background/20 via-transparent to-on-background/40" />

                {isEdit && activeMedia ? (
                  <div className="absolute right-4 top-4 z-10 flex gap-2">
                    <button
                      type="button"
                      onClick={() => removeMedia(activeMedia.id)}
                      className="rounded-full bg-on-background/60 p-2 text-on-primary backdrop-blur-sm hover:bg-error"
                      aria-label="Xóa media"
                    >
                      <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                        delete
                      </span>
                    </button>
                    <button
                      type="button"
                      onClick={() => fileInputRef.current?.click()}
                      disabled={mediaItems.length >= 10}
                      className="rounded-full bg-on-background/60 p-2 text-on-primary backdrop-blur-sm hover:bg-primary disabled:opacity-50"
                      aria-label="Đổi ảnh"
                    >
                      <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                        photo_camera
                      </span>
                    </button>
                  </div>
                ) : null}

                <button
                  type="button"
                  onClick={() => setShowProductPicker(true)}
                  className="absolute left-1/2 top-4 z-10 flex -translate-x-1/2 items-center gap-2 rounded-full border border-outline-variant/30 bg-surface-container-lowest/90 px-3 py-1.5 text-xs font-semibold shadow-sm backdrop-blur-md transition hover:bg-surface-container-lowest"
                >
                  <span className="material-symbols-outlined text-[16px] text-primary" aria-hidden="true">
                    sell
                  </span>
                  Gắn dịch vụ / sản phẩm
                </button>

                <div className="absolute bottom-4 left-4 right-4 z-10 flex items-center justify-between">
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={showComingSoon}
                      className="rounded-full bg-on-background/60 p-2 text-on-primary backdrop-blur-sm"
                      aria-label="Cắt ảnh"
                    >
                      <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                        crop
                      </span>
                    </button>
                    <button
                      type="button"
                      onClick={showComingSoon}
                      className="rounded-full bg-on-background/60 p-2 text-on-primary backdrop-blur-sm"
                      aria-label="Bộ lọc"
                    >
                      <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                        auto_fix_high
                      </span>
                    </button>
                  </div>
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={mediaItems.length >= 10}
                    className="rounded-full bg-primary p-2 text-on-primary shadow-sm hover:bg-[#0050cb] disabled:opacity-50"
                    aria-label="Thêm media"
                  >
                    <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                      add_photo_alternate
                    </span>
                  </button>
                </div>

                {activeMedia?.status === "uploading" ? (
                  <div className="absolute inset-x-0 bottom-16 z-10 px-6">
                    <div className="h-1.5 overflow-hidden rounded-full bg-surface-container-lowest/80">
                      <div
                        className="h-full bg-primary transition-all"
                        style={{ width: `${activeMedia.progress || 0}%` }}
                      />
                    </div>
                  </div>
                ) : null}
              </div>

              <div className="flex gap-2 overflow-x-auto border-t border-outline-variant/30 bg-surface p-3">
                {mediaItems.map((item, index) => (
                  <div key={item.id} className="group relative shrink-0">
                    <button
                      type="button"
                      onClick={() => setActiveMediaIndex(index)}
                      className={[
                        "h-16 w-16 overflow-hidden rounded-lg border-2",
                        index === activeMediaIndex ? "border-primary" : "border-outline-variant/50",
                      ].join(" ")}
                    >
                      {item.previewUrl ? (
                        <img src={item.previewUrl} alt="" className="h-full w-full object-cover" />
                      ) : (
                        <div className="flex h-full w-full items-center justify-center bg-surface-container-high">
                          <span className="material-symbols-outlined text-outline" aria-hidden="true">
                            image
                          </span>
                        </div>
                      )}
                    </button>
                    <button
                      type="button"
                      onClick={() => removeMedia(item.id)}
                      className="absolute -right-1 -top-1 rounded-full bg-error p-0.5 text-on-primary opacity-100 md:opacity-0 md:group-hover:opacity-100"
                      aria-label="Xóa media"
                    >
                      <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
                        close
                      </span>
                    </button>
                  </div>
                ))}
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  disabled={mediaItems.length >= 10}
                  className="flex h-16 w-16 shrink-0 items-center justify-center rounded-lg border border-dashed border-outline-variant text-outline hover:border-primary hover:text-primary disabled:opacity-50"
                  aria-label="Thêm file"
                >
                  <span className="material-symbols-outlined" aria-hidden="true">
                    add
                  </span>
                </button>
              </div>
              {fieldErrors.media ? (
                <p className="px-4 pb-2 text-xs text-error">{fieldErrors.media}</p>
              ) : null}
            </div>

            <div className="flex w-full flex-col overflow-y-auto bg-surface-container-lowest md:w-[45%]">
              <div className="flex items-center justify-between border-b border-outline-variant/30 p-6">
                <div className="flex items-center gap-3">
                  <img src={avatarUrl} alt="" className="h-10 w-10 rounded-full object-cover" />
                  <div>
                    <p className="text-sm font-semibold text-on-surface">{displayName}</p>
                    <p className="text-xs text-on-surface-variant">
                      {isEdit ? "Chỉnh sửa bài viết của bạn" : "Đang đăng với tài khoản của bạn"}
                    </p>
                  </div>
                </div>
                <div className="relative">
                  <button
                    type="button"
                    onClick={() => setShowVisibilityMenu((prev) => !prev)}
                    className="flex items-center gap-1 rounded-lg border border-outline-variant/50 bg-surface-container px-3 py-1.5 text-xs hover:bg-surface-variant"
                  >
                    <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
                      {visibilityMeta?.icon || "public"}
                    </span>
                    {visibilityMeta?.label}
                    <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
                      expand_more
                    </span>
                  </button>
                  {showVisibilityMenu ? (
                    <div className="absolute right-0 top-full z-20 mt-1 min-w-[140px] rounded-lg border border-outline-variant bg-surface-container-lowest py-1 shadow-lg">
                      {VISIBILITY_OPTIONS.map((option) => (
                        <button
                          key={option.value}
                          type="button"
                          onClick={() => {
                            setVisibility(option.value);
                            setShowVisibilityMenu(false);
                          }}
                          className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm hover:bg-surface-container-low"
                        >
                          <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
                            {option.icon}
                          </span>
                          {option.label}
                        </button>
                      ))}
                    </div>
                  ) : null}
                </div>
              </div>

              <div className="flex flex-1 flex-col gap-6 p-6">
                <div>
                  <textarea
                    value={caption}
                    onChange={(event) => setCaption(event.target.value)}
                    placeholder="Viết mô tả... Giới thiệu dịch vụ, chia sẻ mẹo hoặc đặt câu hỏi."
                    className="min-h-[120px] w-full resize-none border-none bg-transparent p-0 text-base text-on-surface outline-none placeholder:text-outline"
                    maxLength={2000}
                  />
                  {fieldErrors.caption ? (
                    <p className="mt-1 text-xs text-error">{fieldErrors.caption}</p>
                  ) : null}
                </div>

                <div>
                  <label className="text-xs font-semibold text-on-surface-variant">Hashtag</label>
                  <div className="mt-2 flex flex-wrap gap-2">
                    {hashtags.map((tag) => (
                      <span
                        key={tag}
                        className="flex items-center gap-1 rounded-full bg-surface-container-high px-3 py-1 text-sm text-on-surface-variant"
                      >
                        #{tag}
                        <button
                          type="button"
                          onClick={() => removeHashtag(tag)}
                          className="hover:text-error"
                          aria-label={`Xóa ${tag}`}
                        >
                          <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
                            close
                          </span>
                        </button>
                      </span>
                    ))}
                    <input
                      type="text"
                      value={hashtagInput}
                      onChange={(event) => setHashtagInput(event.target.value)}
                      onKeyDown={(event) => {
                        if (event.key === "Enter" || event.key === ",") {
                          event.preventDefault();
                          addHashtag(hashtagInput);
                        }
                      }}
                      onBlur={() => addHashtag(hashtagInput)}
                      placeholder="Thêm hashtag..."
                      className="min-w-[120px] border-none bg-transparent p-0 text-sm outline-none placeholder:text-outline"
                    />
                  </div>
                </div>

                <hr className="border-outline-variant/30" />

                <div>
                  <div className="mb-3 flex items-center justify-between">
                    <h3 className="text-sm font-semibold text-on-surface">Sản phẩm đã gắn</h3>
                    <button
                      type="button"
                      onClick={() => setShowProductPicker(true)}
                      className="text-xs font-medium text-primary hover:underline"
                    >
                      {productTags.length > 0 ? "Đổi" : "Thêm"}
                    </button>
                  </div>
                  {productTags.length === 0 ? (
                    <p className="text-sm text-on-surface-variant">Chưa gắn sản phẩm.</p>
                  ) : (
                    <ul className="space-y-2">
                      {productTags.map((tag) => (
                        <li
                          key={tag.productId}
                          className="flex items-center gap-3 rounded-lg border border-outline-variant/50 bg-surface-container-low p-3"
                        >
                          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-lg bg-surface-container-high">
                            <span className="material-symbols-outlined text-outline" aria-hidden="true">
                              sell
                            </span>
                          </div>
                          <div className="min-w-0 flex-1">
                            <p className="truncate text-sm font-semibold text-on-surface">{tag.name}</p>
                            <p className="text-xs text-on-surface-variant">{tag.category}</p>
                            <input
                              type="number"
                              min="0"
                              value={tag.price}
                              onChange={(event) =>
                                updateProductTagPrice(tag.productId, event.target.value)
                              }
                              className="mt-1 w-full rounded border border-outline-variant bg-surface-container-lowest px-2 py-1 text-xs"
                            />
                          </div>
                          <div className="flex flex-col items-end gap-2">
                            <span className="text-sm font-semibold">{formatVndPrice(tag.price)}</span>
                            <button
                              type="button"
                              onClick={() => removeProductTag(tag.productId)}
                              className="text-xs text-error hover:underline"
                            >
                              Gỡ
                            </button>
                          </div>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>

                <label className="flex cursor-pointer items-center gap-2">
                  <input
                    type="checkbox"
                    checked={allowComments}
                    onChange={(event) => setAllowComments(event.target.checked)}
                    className="rounded border-outline-variant text-primary focus:ring-primary"
                  />
                  <span className="text-sm text-on-surface">Cho phép bình luận</span>
                </label>

                <button
                  type="button"
                  onClick={showComingSoon}
                  className="flex items-center gap-2 rounded-lg border border-transparent p-2 transition hover:border-outline-variant/30 hover:bg-surface-container"
                >
                  <span className="material-symbols-outlined text-on-surface-variant" aria-hidden="true">
                    location_on
                  </span>
                  <span className="text-sm text-on-surface-variant">Thêm địa điểm</span>
                  <span className="material-symbols-outlined ml-auto text-outline" aria-hidden="true">
                    chevron_right
                  </span>
                </button>
              </div>
            </div>
          </div>

          {isEdit ? (
            <footer className="flex items-center justify-end gap-3 border-t border-outline-variant/50 bg-surface px-6 py-4">
              <button
                type="button"
                onClick={onClose}
                disabled={isSubmitting}
                className="rounded-full border border-outline-variant px-6 py-2.5 text-sm font-medium text-on-surface transition-colors hover:bg-surface-variant disabled:opacity-50"
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={handleUpdate}
                disabled={isBusy || Boolean(loadError)}
                className="rounded-full bg-primary px-6 py-2.5 text-sm font-semibold text-on-primary shadow-sm transition-colors hover:bg-[#0050cb] disabled:opacity-50"
              >
                {isSubmitting ? "Đang lưu..." : "Cập nhật"}
              </button>
            </footer>
          ) : null}
        </div>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept={POST_MEDIA_ACCEPT}
        multiple
        className="hidden"
        onChange={onFileChange}
      />

      {showProductPicker ? (
        <ProductPickerPanel
          selectedIds={productTags.map((item) => item.productId)}
          onSelect={(product) => {
            addProductTag(product);
            setShowProductPicker(false);
          }}
          onClose={() => setShowProductPicker(false)}
        />
      ) : null}
    </>
  );
}
