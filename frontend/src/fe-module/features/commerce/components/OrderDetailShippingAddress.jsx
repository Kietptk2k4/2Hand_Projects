export function OrderDetailShippingAddress({ address }) {
  if (!address) return null;

  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-6">
      <h2 className="mb-3 flex items-center justify-between text-headline-sm font-semibold text-on-surface">
        Địa chỉ giao hàng
        <span className="material-symbols-outlined text-on-surface-variant" aria-hidden="true">
          location_on
        </span>
      </h2>

      <div className="text-body-sm leading-relaxed text-on-surface-variant">
        <p className="font-medium text-on-surface">{address.receiverName}</p>
        <p className="mt-1">{address.fullAddress || address.addressDetail}</p>
        {address.phone ? (
          <p className="mt-2 flex items-center gap-1">
            <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
              phone
            </span>
            {address.phone}
          </p>
        ) : null}
      </div>
    </section>
  );
}
