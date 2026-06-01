export function SellerOrderListSkeleton() {
  return (
    <div className="animate-pulse space-y-3 rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
      {Array.from({ length: 6 }).map((_, index) => (
        <div key={index} className="flex gap-4 border-b border-outline-variant/40 pb-3 last:border-0">
          <div className="h-4 w-4 rounded bg-surface-container-high" />
          <div className="h-10 w-10 rounded-lg bg-surface-container-high" />
          <div className="flex-1 space-y-2">
            <div className="h-4 w-1/3 rounded bg-surface-container-high" />
            <div className="h-3 w-2/3 rounded bg-surface-container-high" />
          </div>
        </div>
      ))}
    </div>
  );
}
