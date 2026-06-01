export function CheckoutSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
      <div className="space-y-6 lg:col-span-7">
        {[1, 2, 3].map((key) => (
          <div
            key={key}
            className="animate-pulse rounded-xl border border-outline-variant bg-surface-container-lowest p-6"
          >
            <div className="mb-4 h-6 w-40 rounded bg-surface-container" />
            <div className="h-20 rounded bg-surface-container" />
          </div>
        ))}
      </div>
      <div className="lg:col-span-5">
        <div className="animate-pulse rounded-xl border border-outline-variant bg-surface-container-lowest p-6">
          <div className="mb-4 h-6 w-32 rounded bg-surface-container" />
          <div className="space-y-3">
            <div className="h-16 rounded bg-surface-container" />
            <div className="h-4 rounded bg-surface-container" />
            <div className="h-12 rounded-lg bg-surface-container" />
          </div>
        </div>
      </div>
    </div>
  );
}
