export function CommentSkeleton() {
  return (
    <div className="flex gap-3" aria-hidden="true">
      <div className="skeleton-shimmer h-10 w-10 shrink-0 rounded-full" />
      <div className="flex-1 space-y-2">
        <div className="skeleton-shimmer h-4 w-1/3 rounded" />
        <div className="skeleton-shimmer h-16 w-full rounded" />
      </div>
    </div>
  );
}
