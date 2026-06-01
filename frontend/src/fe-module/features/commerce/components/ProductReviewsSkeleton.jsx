export function ProductReviewsSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
      <div className="space-y-4 lg:col-span-4">
        <div className="animate-pulse rounded-xl border border-outline-variant bg-surface-container-lowest p-6">
          <div className="h-10 w-16 rounded bg-surface-container" />
          <div className="mt-3 h-5 w-32 rounded bg-surface-container" />
        </div>
        <div className="animate-pulse rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
          <div className="h-10 rounded bg-surface-container" />
          <div className="mt-4 flex gap-2">
            {[1, 2, 3].map((key) => (
              <div key={key} className="h-8 w-16 rounded-full bg-surface-container" />
            ))}
          </div>
        </div>
      </div>
      <div className="space-y-4 lg:col-span-8">
        {[1, 2, 3].map((key) => (
          <div
            key={key}
            className="animate-pulse rounded-xl border border-outline-variant bg-surface-container-lowest p-5"
          >
            <div className="flex gap-3">
              <div className="h-10 w-10 rounded-full bg-surface-container" />
              <div className="flex-1 space-y-2">
                <div className="h-4 w-24 rounded bg-surface-container" />
                <div className="h-4 w-32 rounded bg-surface-container" />
              </div>
            </div>
            <div className="mt-4 h-16 rounded bg-surface-container" />
          </div>
        ))}
      </div>
    </div>
  );
}
