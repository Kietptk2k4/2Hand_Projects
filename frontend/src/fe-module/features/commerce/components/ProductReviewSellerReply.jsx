import { formatReviewDate } from "../utils/formatReviewDate";
import { ReviewShopLink } from "./ReviewShopLink";

export function ProductReviewSellerReply({ sellerReply, shop }) {
  if (!sellerReply) return null;

  return (
    <div className="mt-4 rounded-lg border border-outline-variant bg-surface-container-low p-4">
      <ReviewShopLink
        shopId={shop?.shopId}
        shopName={shop?.shopName}
        avatarUrl={shop?.avatarUrl}
      />
      <p className="mt-2 text-sm text-on-surface">{sellerReply.content}</p>
      {sellerReply.createdAt ? (
        <p className="mt-2 text-xs text-on-surface-variant">
          {formatReviewDate(sellerReply.createdAt)}
        </p>
      ) : null}
    </div>
  );
}
