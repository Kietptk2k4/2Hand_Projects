export function CartSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
      <div className="flex flex-col gap-4 lg:col-span-8">
        {[1, 2, 3].map((key) => (
          <div
            key={key}
            className="flex animate-pulse flex-col gap-4 rounded-lg border border-outline-variant bg-surface-container-lowest p-4 sm:flex-row"
          >
            <div className="h-32 w-full rounded-lg bg-surface-container sm:w-32" />
            <div className="flex flex-1 flex-col gap-3">
              <div className="h-5 w-2/3 rounded bg-surface-container" />
              <div className="h-4 w-1/3 rounded bg-surface-container" />
              <div className="mt-auto flex justify-between">
                <div className="h-8 w-28 rounded-full bg-surface-container" />
                <div className="h-6 w-24 rounded bg-surface-container" />
              </div>
            </div>
          </div>
        ))}
      </div>
      <div className="lg:col-span-4">
        <div className="animate-pulse rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
          <div className="mb-4 h-6 w-1/2 rounded bg-surface-container" />
          <div className="space-y-3">
            <div className="h-4 rounded bg-surface-container" />
            <div className="h-4 rounded bg-surface-container" />
          </div>
          <div className="mt-6 h-12 rounded-lg bg-surface-container" />
        </div>
      </div>
    </div>
  );
}
