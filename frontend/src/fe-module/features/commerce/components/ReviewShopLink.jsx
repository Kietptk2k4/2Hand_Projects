import { useState } from "react";
import { Link } from "react-router-dom";
import { buildCommerceShopPath } from "../utils/commerceRoutes";

export function ReviewShopLink({
  shopId,
  shopName,
  avatarUrl,
  prefix = "Phản hồi từ",
  className = "",
}) {
  const [hasAvatarError, setHasAvatarError] = useState(false);
  const resolvedName = shopName || "shop";
  const showAvatar = Boolean(shopId && avatarUrl && !hasAvatarError);

  const content = (
    <>
      <div
        className="flex h-8 w-8 shrink-0 items-center justify-center overflow-hidden rounded-full bg-surface-container-high text-on-surface-variant"
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
          <span className="material-symbols-outlined text-[18px]">storefront</span>
        )}
      </div>
      <span className="text-label-sm font-semibold text-primary transition-colors group-hover:underline">
        {prefix} {resolvedName}
      </span>
    </>
  );

  if (shopId) {
    return (
      <Link
        to={buildCommerceShopPath(shopId)}
        className={`group inline-flex items-center gap-2 ${className}`}
      >
        {content}
      </Link>
    );
  }

  return <div className={`inline-flex items-center gap-2 ${className}`}>{content}</div>;
}