export function SellerShipmentListSkeleton() {
  return (
    <div className="space-y-4">
      {[1, 2, 3].map((key) => (
        <div
          key={key}
          className="h-36 animate-pulse rounded-xl border border-outline-variant/40 bg-surface-container-low"
        />
      ))}
    </div>
  );
}
