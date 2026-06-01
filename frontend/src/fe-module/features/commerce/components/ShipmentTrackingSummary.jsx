import { formatVndPrice } from "../../social/utils/formatPrice";

export function ShipmentTrackingSummary({ detail }) {
  if (!detail) return null;

  const hasFee = detail.shippingFee != null;
  const hasCod = detail.codAmount != null && detail.codAmount > 0;
  const hasWeight = detail.weightGram != null;

  if (!hasFee && !hasCod && !hasWeight) return null;

  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-6">
      <h2 className="mb-3 text-headline-sm font-semibold text-on-surface">Tóm tắt phí</h2>
      <dl className="space-y-2 text-body-sm">
        {hasFee ? (
          <div className="flex justify-between gap-2">
            <dt className="text-on-surface-variant">Phí vận chuyển</dt>
            <dd className="font-medium text-on-surface">{formatVndPrice(detail.shippingFee)}</dd>
          </div>
        ) : null}
        {hasCod ? (
          <div className="flex justify-between gap-2">
            <dt className="text-on-surface-variant">Thu hộ (COD)</dt>
            <dd className="font-medium text-on-surface">{formatVndPrice(detail.codAmount)}</dd>
          </div>
        ) : null}
        {hasWeight ? (
          <div className="flex justify-between gap-2">
            <dt className="text-on-surface-variant">Khối lượng</dt>
            <dd className="font-medium text-on-surface">{detail.weightGram} g</dd>
          </div>
        ) : null}
      </dl>
    </section>
  );
}
