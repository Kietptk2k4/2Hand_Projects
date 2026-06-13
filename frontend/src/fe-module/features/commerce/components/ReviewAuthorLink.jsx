import { useState } from "react";
import { Link } from "react-router-dom";
import { buildSocialProfilePath } from "../../social/utils/socialProfileRoutes";

const FALLBACK_NAME = "Người mua";

export function ReviewAuthorLink({ buyerId, displayName, avatarUrl, className = "" }) {
  const [hasAvatarError, setHasAvatarError] = useState(false);
  const name = displayName || FALLBACK_NAME;
  const showAvatar = Boolean(buyerId && avatarUrl && !hasAvatarError);

  const avatar = (
    <div
      className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-full bg-surface-container-high text-on-surface-variant"
      aria-hidden="true"
    >
      {showAvatar ? (
        <img
          src={avatarUrl}
          alt=""
          className="h-full w-full object-cover"
          onError={() => setHasAvatarError(true)}
        />
      ) : (
        <span className="material-symbols-outlined">person</span>
      )}
    </div>
  );

  const label = (
    <p className="text-sm font-semibold text-on-surface transition-colors group-hover:text-primary">
      {name}
    </p>
  );

  if (buyerId) {
    return (
      <Link
        to={buildSocialProfilePath(buyerId)}
        className={`group flex min-w-0 items-center gap-3 ${className}`}
      >
        {avatar}
        {label}
      </Link>
    );
  }

  return (
    <div className={`flex min-w-0 items-center gap-3 ${className}`}>
      {avatar}
      {label}
    </div>
  );
}