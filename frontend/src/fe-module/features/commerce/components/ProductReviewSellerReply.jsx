import { formatReviewDate } from "../utils/formatReviewDate";

export function ProductReviewSellerReply({ sellerReply }) {
  if (!sellerReply) return null;

  return (
    <div className="mt-4 rounded-lg border border-outline-variant bg-surface-container-low p-4">
      <p className="text-label-sm font-semibold text-primary">Phản hồi từ shop</p>
      <p className="mt-2 text-sm text-on-surface">{sellerReply.content}</p>
      {sellerReply.createdAt ? (
        <p className="mt-2 text-xs text-on-surface-variant">
          {formatReviewDate(sellerReply.createdAt)}
        </p>
      ) : null}
    </div>
  );
}
