function OrderCardSkeleton() {
  return (
    <div className="animate-pulse rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
      <div className="flex items-center justify-between gap-3">
        <div className="h-4 w-32 rounded bg-surface-container-high" />
        <div className="h-4 w-24 rounded bg-surface-container-high" />
      </div>
      <div className="mt-3 flex gap-2">
        <div className="h-6 w-24 rounded-full bg-surface-container-high" />
        <div className="h-6 w-20 rounded-full bg-surface-container-high" />
      </div>
      <div className="mt-4 flex gap-3">
        <div className="h-16 w-16 shrink-0 rounded-lg bg-surface-container-high" />
        <div className="flex-1 space-y-2">
          <div className="h-4 w-3/4 rounded bg-surface-container-high" />
          <div className="h-4 w-1/2 rounded bg-surface-container-high" />
        </div>
        <div className="h-6 w-20 rounded bg-surface-container-high" />
      </div>
    </div>
  );
}

export function OrderListSkeleton({ count = 4 }) {
  return (
    <div className="flex flex-col gap-4" aria-hidden="true">
      {Array.from({ length: count }, (_, index) => (
        <OrderCardSkeleton key={index} />
      ))}
    </div>
  );
}
