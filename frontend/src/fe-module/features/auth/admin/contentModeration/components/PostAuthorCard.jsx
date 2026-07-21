import { PostAuthorInvestigationLink } from "./PostAuthorInvestigationLink.jsx";

function AuthorAvatar({ url, displayName }) {
  if (url) {
    return (
      <img
        src={url}
        alt={displayName ? `Avatar ${displayName}` : "Avatar tác giả"}
        className="h-10 w-10 shrink-0 rounded-full border border-admin-border object-cover bg-admin-surface-muted"
      />
    );
  }

  return (
    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-admin-border bg-admin-surface-muted text-admin-text-muted">
      <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
        person
      </span>
    </div>
  );
}

export function PostAuthorCard({ authorId, authorSummary }) {
  if (!authorId) return null;

  const displayName = authorSummary?.displayName || authorSummary?.display_name || "";
  const avatarUrl = authorSummary?.avatarUrl || authorSummary?.avatar_url || "";

  return (
    <div className="flex items-center gap-3 rounded-lg border border-admin-border-subtle bg-admin-surface-raised p-3">
      <AuthorAvatar url={avatarUrl} displayName={displayName} />
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-admin-text">
          {displayName || "Người dùng chưa có tên hiển thị"}
        </p>
        <PostAuthorInvestigationLink
          authorId={authorId}
          authorSummary={authorSummary}
          className="mt-1 !font-sans !text-xs"
        />
      </div>
    </div>
  );
}
