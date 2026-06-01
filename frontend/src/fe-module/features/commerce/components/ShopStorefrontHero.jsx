export function ShopStorefrontHero({ shop, onComingSoon }) {
  if (!shop) return null;

  return (
    <section className="mb-8 overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm">
      <div className="relative h-40 w-full bg-surface-container-low md:h-56">
        {shop.coverUrl ? (
          <img
            src={shop.coverUrl}
            alt=""
            className="h-full w-full object-cover"
            loading="eager"
          />
        ) : (
          <div className="h-full w-full bg-gradient-to-br from-surface-container-high to-surface-container" />
        )}
      </div>

      <div className="relative px-4 pb-6 pt-0 md:px-8">
        <div className="-mt-12 flex flex-col items-center text-center md:-mt-14">
          <div className="h-24 w-24 overflow-hidden rounded-full border-4 border-surface-container-lowest bg-surface-container-low shadow-md md:h-28 md:w-28">
            {shop.avatarUrl ? (
              <img
                src={shop.avatarUrl}
                alt={shop.shopName}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center">
                <span className="material-symbols-outlined text-4xl text-outline" aria-hidden="true">
                  storefront
                </span>
              </div>
            )}
          </div>

          <h1 className="mt-4 text-headline-lg-mobile font-bold text-on-surface md:text-headline-lg">
            {shop.shopName}
          </h1>

          {shop.ratingCount > 0 ? (
            <div className="mt-2 flex flex-wrap items-center justify-center gap-1 text-body-sm text-on-surface-variant">
              <span className="material-symbols-outlined fill text-[18px] text-primary" aria-hidden="true">
                star
              </span>
              <span className="font-medium text-on-surface">{shop.ratingAvg}</span>
              <span>· {shop.ratingCount} đánh giá</span>
            </div>
          ) : null}

          {shop.description ? (
            <p className="mt-4 max-w-2xl text-body-md text-on-surface-variant line-clamp-3 md:line-clamp-none">
              {shop.description}
            </p>
          ) : null}

          <div className="mt-6 flex flex-wrap justify-center gap-3">
            <button
              type="button"
              onClick={onComingSoon}
              className="rounded-lg bg-primary px-6 py-2 text-label-md font-medium text-on-primary transition-colors hover:bg-[#0050cb]"
            >
              Theo dõi
            </button>
            <button
              type="button"
              onClick={onComingSoon}
              className="rounded-lg border-2 border-primary px-6 py-2 text-label-md font-medium text-primary transition-colors hover:bg-surface-container-low"
            >
              Nhắn tin
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
