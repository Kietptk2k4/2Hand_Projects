import { ProductAttributesEditor } from "./ProductAttributesEditor";
import { ProductMediaUploadGrid } from "./ProductMediaUploadGrid";

export function SellerProductMediaAttributesStep({
  productId,
  mediaUrls,
  attributes,
  fieldErrors,
  disabled,
  onMediaChange,
  onAttributesChange,
}) {
  return (
    <div className="space-y-8">
      <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
        <h2 className="mb-1 text-headline-sm font-semibold text-on-surface">Hình ảnh sản phẩm</h2>
        <p className="mb-6 text-body-sm text-on-surface-variant">
          Tải ít nhất một ảnh để có thể đăng bán. Ảnh đầu tiên là ảnh chính.
        </p>
        <ProductMediaUploadGrid
          productId={productId}
          mediaUrls={mediaUrls}
          disabled={disabled}
          onChange={onMediaChange}
          error={fieldErrors.media}
        />
      </section>

      <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
        <h2 className="mb-1 text-headline-sm font-semibold text-on-surface">Thuộc tính</h2>
        <p className="mb-6 text-body-sm text-on-surface-variant">
          Thêm thuộc tính giúp người mua chọn đúng biến thể (màu, size, …).
        </p>
        <ProductAttributesEditor
          attributes={attributes}
          disabled={disabled}
          onChange={onAttributesChange}
          fieldErrors={fieldErrors}
        />
      </section>
    </div>
  );
}
