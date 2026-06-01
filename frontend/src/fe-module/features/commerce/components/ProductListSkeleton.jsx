export function ProductListSkeleton({ count = 8 }) {
  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {Array.from({ length: count }, (_, index) => (
        <div
          key={index}
          className="overflow-hidden rounded-lg border border-outline-variant bg-surface-container-lowest"
        >
          <div className="h-48 animate-pulse bg-surface-container-low" />
          <div className="space-y-3 p-4">
            <div className="h-5 w-3/4 animate-pulse rounded bg-surface-container-low" />
            <div className="h-4 w-1/2 animate-pulse rounded bg-surface-container-low" />
            <div className="h-4 w-2/3 animate-pulse rounded bg-surface-container-low" />
            <div className="mt-4 h-8 w-full animate-pulse rounded bg-surface-container-low" />
          </div>
        </div>
      ))}
    </div>
  );
}
