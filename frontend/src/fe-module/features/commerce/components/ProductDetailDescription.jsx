import { getConditionLabel } from "../constants/productDetailConstants";

export function ProductDetailDescription({ product }) {
  if (!product) return null;

  const specRows = [
    ...(product.attributes || []).map((attr) => ({
      label: attr.attributeName,
      value: attr.attributeValue,
    })),
    ...(product.weightGram != null
      ? [{ label: "Khối lượng", value: `${product.weightGram} g` }]
      : []),
  ];

  return (
    <div className="flex flex-col gap-6">
      {product.description ? (
        <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
          <h2 className="mb-4 border-b border-outline-variant pb-2 text-headline-sm font-semibold text-on-surface">
            Mô tả sản phẩm
          </h2>
          <p className="whitespace-pre-line text-body-md text-on-surface-variant">
            {product.description}
          </p>
        </section>
      ) : null}

      {specRows.length > 0 ? (
        <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
          <h2 className="mb-4 border-b border-outline-variant pb-2 text-headline-sm font-semibold text-on-surface">
            Thông số kỹ thuật
          </h2>
          <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {specRows.map((row) => (
              <div key={`${row.label}-${row.value}`}>
                <dt className="text-label-sm font-medium text-on-surface-variant">{row.label}</dt>
                <dd className="mt-1 text-body-md text-on-surface">{row.value}</dd>
              </div>
            ))}
            <div>
              <dt className="text-label-sm font-medium text-on-surface-variant">Tình trạng</dt>
              <dd className="mt-1 text-body-md text-on-surface">
                {getConditionLabel(product.condition)}
              </dd>
            </div>
          </dl>
        </section>
      ) : null}
    </div>
  );
}
