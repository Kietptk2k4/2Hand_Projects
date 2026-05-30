export function SavedPostCardSkeleton() {
  return (
    <article className="flex animate-pulse flex-col overflow-hidden rounded-lg border border-outline-variant bg-surface-container-lowest shadow-sm md:flex-row">
      <div className="h-48 flex-shrink-0 bg-surface-container-high md:h-auto md:w-1/3" />
      <div className="flex flex-grow flex-col gap-3 p-4">
        <div className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-full bg-surface-container-high" />
          <div className="h-4 w-32 rounded bg-surface-container-high" />
        </div>
        <div className="h-5 w-3/4 rounded bg-surface-container-high" />
        <div className="h-4 w-full rounded bg-surface-container-high" />
        <div className="h-4 w-2/3 rounded bg-surface-container-high" />
        <div className="mt-auto flex justify-between border-t border-outline-variant pt-3">
          <div className="h-4 w-24 rounded bg-surface-container-high" />
          <div className="h-4 w-28 rounded bg-surface-container-high" />
        </div>
      </div>
    </article>
  );
}
