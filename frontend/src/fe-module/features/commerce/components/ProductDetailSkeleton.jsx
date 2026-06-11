export function ProductDetailSkeleton() {
  return (
    <div className="animate-pulse space-y-8">
      <div className="h-4 w-48 rounded bg-surface-container-low" />
      <div className="grid grid-cols-1 gap-8 lg:grid-cols-12 lg:gap-10">
        <div className="lg:col-span-5">
          <div className="aspect-square max-w-[450px] rounded-xl bg-surface-container-low" />
          <div className="mt-3 flex gap-2 lg:hidden">
            <div className="h-16 w-16 rounded-lg bg-surface-container-low" />
            <div className="h-16 w-16 rounded-lg bg-surface-container-low" />
            <div className="h-16 w-16 rounded-lg bg-surface-container-low" />
          </div>
        </div>
        <div className="space-y-4 lg:col-span-7">
          <div className="h-10 w-3/4 rounded bg-surface-container-low" />
          <div className="h-6 w-1/2 rounded bg-surface-container-low" />
          <div className="h-48 rounded-xl bg-surface-container-low" />
          <div className="h-32 rounded-xl bg-surface-container-low" />
        </div>
      </div>
      <div className="space-y-4">
        <div className="h-40 rounded-xl bg-surface-container-low" />
        <div className="h-32 rounded-xl bg-surface-container-low" />
      </div>
    </div>
  );
}
