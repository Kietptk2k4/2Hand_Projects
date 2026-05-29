export function FeedPostSkeleton() {
  return (
    <div
      className="animate-pulse overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm"
      aria-hidden="true"
    >
      <div className="mb-4 flex gap-3">
        <div className="h-12 w-12 rounded-full bg-surface-container-high" />
        <div className="flex flex-1 flex-col gap-2">
          <div className="h-4 w-32 rounded bg-surface-container-high" />
          <div className="h-3 w-24 rounded bg-surface-container-high" />
        </div>
      </div>
      <div className="space-y-2">
        <div className="h-3 w-full rounded bg-surface-container-high" />
        <div className="h-3 w-5/6 rounded bg-surface-container-high" />
        <div className="h-3 w-2/3 rounded bg-surface-container-high" />
      </div>
      <div className="mt-4 h-48 rounded-lg bg-surface-container-high" />
    </div>
  );
}
