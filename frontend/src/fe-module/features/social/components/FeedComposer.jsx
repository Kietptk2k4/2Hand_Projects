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
    <div className="flex items-start gap-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <button
        type="button"
        onClick={openSelfProfile}
        className="shrink-0 rounded-full p-0.5 ring-2 ring-primary ring-offset-2 ring-offset-surface-container-lowest"
        aria-label="Xem hồ sơ của bạn"
        title="Xem hồ sơ"
      >
        <img
          src={avatarUrl}
          alt=""
          className="h-10 w-10 rounded-full border border-outline-variant object-cover"
        />
      </button>
      <div className="flex flex-1 flex-col gap-3">
        <input
          type="text"
          readOnly
          disabled={isWriteBlocked}
          onClick={openModal}
          onFocus={openModal}
          placeholder={
            isWriteBlocked
              ? "Tài khoản bị đình chỉ — không thể đăng bài"
              : "Bắt đầu đăng bài hoặc chia sẻ cập nhật..."
          }
          title={blockedTitle}
          className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-4 py-3 text-base text-on-surface outline-none transition placeholder:text-on-surface-variant/70 focus:border-primary focus:ring-1 focus:ring-primary disabled:cursor-not-allowed disabled:opacity-60"
          aria-label="Soạn bài viết"
          aria-disabled={isWriteBlocked}
        />
        <div className="flex items-center justify-between">
          <div className="flex gap-2">
            <button
              type="button"
              onClick={openWithPicker}
              disabled={isWriteBlocked}
              className="rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low disabled:cursor-not-allowed disabled:opacity-50"
              title={blockedTitle || "Thêm ảnh"}
              aria-label="Thêm ảnh"
            >
              <ImageIcon />
            </button>
            <button
              type="button"
              onClick={openWithPicker}
              disabled={isWriteBlocked}
              className="rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low disabled:cursor-not-allowed disabled:opacity-50"
              title={blockedTitle || "Thêm tài liệu"}
              aria-label="Thêm tài liệu"
            >
              <ArticleIcon />
            </button>
          </div>
          <span className="rounded-lg px-6 py-2 text-sm font-medium text-on-surface-variant">
            Đăng bài
          </span>
        </div>
      </div>
    </div>
  );
}
