import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

const DEFAULT_AVATAR_URL = "https://i.pravatar.cc/96?img=11";

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

export function FeedComposer({ onComingSoon }) {
  const { user } = useAuthSession();
  const avatarUrl = user?.avatar_url || user?.profile?.avatar_url || DEFAULT_AVATAR_URL;

  return (
    <div className="flex items-start gap-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <img
        src={avatarUrl}
        alt=""
        className="h-10 w-10 shrink-0 rounded-full border border-outline-variant object-cover"
      />
      <div className="flex flex-1 flex-col gap-3">
        <input
          type="text"
          readOnly
          onFocus={onComingSoon}
          placeholder="Start a post or share an update..."
          className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-4 py-3 text-base text-on-surface outline-none transition placeholder:text-on-surface-variant/70 focus:border-primary focus:ring-1 focus:ring-primary"
          aria-label="Soạn bài viết"
        />
        <div className="flex items-center justify-between">
          <div className="flex gap-2">
            <button
              type="button"
              onClick={onComingSoon}
              className="rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low"
              title="Thêm ảnh"
              aria-label="Thêm ảnh"
            >
              <ImageIcon />
            </button>
            <button
              type="button"
              onClick={onComingSoon}
              className="rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low"
              title="Thêm tài liệu"
              aria-label="Thêm tài liệu"
            >
              <ArticleIcon />
            </button>
          </div>
          <button
            type="button"
            onClick={onComingSoon}
            className="rounded-lg bg-primary px-6 py-2 text-sm font-medium text-on-primary shadow-sm transition-colors hover:bg-[#0050cb]"
          >
            Post
          </button>
        </div>
      </div>
    </div>
  );
}
