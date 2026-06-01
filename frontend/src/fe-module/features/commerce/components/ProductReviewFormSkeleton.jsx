export function ProductReviewFormSkeleton() {
  return (
    <div className="animate-pulse grid grid-cols-1 gap-6 lg:grid-cols-12" aria-hidden="true">
      <div className="h-80 rounded-xl border border-outline-variant bg-surface-container-lowest lg:col-span-5" />
      <div className="h-96 rounded-xl border border-outline-variant bg-surface-container-lowest lg:col-span-7" />
    </div>
  );
}
