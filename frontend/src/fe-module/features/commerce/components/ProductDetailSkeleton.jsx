export function ProductDetailSkeleton() {
  return (
    <div className="animate-pulse space-y-8">
      <div className="aspect-[16/10] rounded-xl bg-surface-container-low md:aspect-[21/9]" />
      <div className="grid gap-8 lg:grid-cols-12">
        <div className="space-y-4 lg:col-span-8">
          <div className="h-10 w-3/4 rounded bg-surface-container-low" />
          <div className="h-6 w-1/2 rounded bg-surface-container-low" />
          <div className="h-24 rounded-xl bg-surface-container-low" />
          <div className="h-40 rounded-xl bg-surface-container-low" />
        </div>
        <div className="lg:col-span-4">
          <div className="h-64 rounded-xl bg-surface-container-low" />
        </div>
      </div>
    </div>
  );
}
