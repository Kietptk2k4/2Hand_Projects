export function ShipmentTrackingSkeleton() {
  return (
    <div className="animate-pulse space-y-6" aria-hidden="true">
      <div className="h-8 w-56 rounded bg-surface-container-high" />
      <div className="h-40 rounded-xl border border-outline-variant bg-surface-container-lowest" />
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
        <div className="space-y-4 lg:col-span-8">
          <div className="h-72 rounded-xl border border-outline-variant bg-surface-container-lowest" />
          <div className="h-48 rounded-xl border border-outline-variant bg-surface-container-lowest" />
        </div>
        <div className="space-y-4 lg:col-span-4">
          <div className="h-36 rounded-xl border border-outline-variant bg-surface-container-lowest" />
          <div className="h-40 rounded-xl border border-outline-variant bg-surface-container-lowest" />
          <div className="h-32 rounded-xl border border-outline-variant bg-surface-container-lowest" />
        </div>
      </div>
    </div>
  );
}
