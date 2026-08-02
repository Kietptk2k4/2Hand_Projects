import { useNavigate } from "react-router-dom";
import { useCurrentUserAvatarUrl } from "../../auth/hooks/useCurrentUserAvatarUrl";
import { useCurrentUserId } from "../../auth/hooks/useCurrentUserId";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { buildSocialProfilePath } from "../utils/socialProfileRoutes";

function ImageIcon() {
  return (
    <span className="material-symbols-outlined text-[22px]" aria-hidden="true">
      image
    </span>
  );
}

function ArticleIcon() {
  return (
    <span className="material-symbols-outlined text-[22px]" aria-hidden="true">
      article
    </span>
  );
}

export function FeedComposer({ onOpenCreatePost, onOpenCreatePostWithFilePicker }) {
  const navigate = useNavigate();
  const currentUserId = useCurrentUserId();
  const { isWriteBlocked, suspendMessage } = useSocialWriteBlock();
  const avatarUrl = useCurrentUserAvatarUrl();
  const blockedTitle = isWriteBlocked ? suspendMessage : undefined;

  const openModal = () => {
    if (isWriteBlocked) return;
    onOpenCreatePost?.();
  };
  const openWithPicker = () => {
    if (isWriteBlocked) return;
    onOpenCreatePostWithFilePicker?.();
  };

  const openSelfProfile = () => {
    if (currentUserId) navigate(buildSocialProfilePath(currentUserId));
  };

  return (
    <div className="border-b border-outline-variant/40 bg-surface-container-lowest p-4">
      <div className="flex gap-3">
        <button
          type="button"
          onClick={openSelfProfile}
          className="shrink-0 transition-opacity hover:opacity-85"
          aria-label="Xem hồ sơ của bạn"
          title="Xem hồ sơ"
        >
          <img
            src={avatarUrl}
            alt=""
            className="h-10 w-10 rounded-full object-cover"
          />
        </button>
        <div className="flex-1">
          <input
            type="text"
            readOnly
            disabled={isWriteBlocked}
            onClick={openModal}
            onFocus={openModal}
            placeholder={
              isWriteBlocked
                ? "Tài khoản bị đình chỉ — không thể đăng bài"
                : "Chuyện gì đang xảy ra?"
            }
            title={blockedTitle}
            className="w-full cursor-pointer bg-transparent py-2 text-base text-on-surface outline-none placeholder:text-on-surface-variant/60 disabled:cursor-not-allowed"
            aria-label="Soạn bài viết"
            aria-disabled={isWriteBlocked}
          />

          <div className="mt-3 flex items-center justify-between border-t border-outline-variant/20 pt-3">
            <div className="-ml-1.5 flex items-center gap-0.5">
              <button
                type="button"
                onClick={openWithPicker}
                disabled={isWriteBlocked}
                className="flex h-9 w-9 items-center justify-center rounded-full text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface disabled:opacity-50"
                title={blockedTitle || "Thêm ảnh/video"}
                aria-label="Thêm ảnh/video"
              >
                <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                  image
                </span>
              </button>
              <button
                type="button"
                onClick={openWithPicker}
                disabled={isWriteBlocked}
                className="flex h-9 w-9 items-center justify-center rounded-full text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface disabled:opacity-50"
                title={blockedTitle || "Thêm tệp"}
                aria-label="Thêm tệp"
              >
                <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                  article
                </span>
              </button>
              <button
                type="button"
                onClick={openModal}
                disabled={isWriteBlocked}
                className="flex h-9 w-9 items-center justify-center rounded-full text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface disabled:opacity-50"
                title="Biểu tượng cảm xúc"
                aria-label="Thêm cảm xúc"
              >
                <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
                  sentiment_satisfied
                </span>
              </button>
            </div>

            <button
              type="button"
              onClick={openModal}
              disabled={isWriteBlocked}
              className="rounded-full bg-on-surface px-5 py-1.5 text-sm font-bold text-surface-container-lowest shadow-xs transition-all hover:opacity-90 active:scale-[0.97] disabled:opacity-50"
            >
              Đăng
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
