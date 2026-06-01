export function SellerShopReviewsListSkeleton() {
  return (
    <div className="divide-y divide-outline-variant">
      {[1, 2, 3].map((key) => (
        <div key={key} className="h-40 animate-pulse bg-surface-container-low/60 p-6" />
      ))}
    </div>
  );
}
