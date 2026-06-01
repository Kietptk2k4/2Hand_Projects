export function ShopStorefrontHeroSkeleton() {
  return (
    <section className="mb-8 overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest">
      <div className="h-40 animate-pulse bg-surface-container-low md:h-56" />
      <div className="flex flex-col items-center px-4 pb-6">
        <div className="-mt-12 h-24 w-24 animate-pulse rounded-full border-4 border-surface-container-lowest bg-surface-container-low md:-mt-14 md:h-28 md:w-28" />
        <div className="mt-4 h-8 w-48 animate-pulse rounded bg-surface-container-low" />
        <div className="mt-3 h-4 w-32 animate-pulse rounded bg-surface-container-low" />
        <div className="mt-4 h-16 w-full max-w-lg animate-pulse rounded bg-surface-container-low" />
      </div>
    </section>
  );
}
