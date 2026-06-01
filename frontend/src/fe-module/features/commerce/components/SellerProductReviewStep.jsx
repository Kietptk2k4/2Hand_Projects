import { PRODUCT_CONDITIONS, STATUS_LABELS } from "../constants/sellerProductConstants";
import { formatVnd } from "../utils/sellerProductMapper";

function CheckItem({ ok, label }) {
  return (
    <li className="flex items-center gap-2 text-body-md">
      <span
        className={[
          "material-symbols-outlined text-[20px]",
          ok ? "text-primary" : "text-error",
        ].join(" ")}
        aria-hidden="true"
      >
        {ok ? "check_circle" : "cancel"}
      </span>
      <span className={ok ? "text-on-surface" : "text-on-surface-variant"}>{label}</span>
    </li>
  );
}

export function SellerProductReviewStep({
  form,
  categories,
  mediaUrls,
  attributes,
  status,
  reviewChecklist,
  canPublish,
  apiError,
}) {
  const categoryName =
    categories.find((c) => c.id === form.categoryId)?.name || form.categoryId || "—";
  const conditionLabel =
    PRODUCT_CONDITIONS.find((c) => c.value === form.condition)?.label || form.condition;

  return (
    <div className="space-y-6">
      <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
        <h2 className="mb-6 text-headline-sm font-semibold text-on-surface">Tóm tắt sản phẩm</h2>

        <dl className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <dt className="text-label-sm text-on-surface-variant">Tên</dt>
            <dd className="text-body-md font-medium text-on-surface">{form.title || "—"}</dd>
          </div>
          <div>
            <dt className="text-label-sm text-on-surface-variant">Trạng thái</dt>
            <dd className="text-body-md text-on-surface">{STATUS_LABELS[status] || status}</dd>
          </div>
          <div>
            <dt className="text-label-sm text-on-surface-variant">Danh mục</dt>
            <dd className="text-body-md text-on-surface">{categoryName}</dd>
          </div>
          <div>
            <dt className="text-label-sm text-on-surface-variant">Tình trạng</dt>
            <dd className="text-body-md text-on-surface">{conditionLabel}</dd>
          </div>
          <div>
            <dt className="text-label-sm text-on-surface-variant">Giá niêm yết</dt>
            <dd className="text-body-md text-on-surface">{formatVnd(form.price)}</dd>
          </div>
          <div>
            <dt className="text-label-sm text-on-surface-variant">Giá khuyến mãi</dt>
            <dd className="text-body-md text-on-surface">
              {form.salePrice !== "" ? formatVnd(form.salePrice) : "—"}
            </dd>
          </div>
          <div>
            <dt className="text-label-sm text-on-surface-variant">Tồn kho</dt>
            <dd className="text-body-md text-on-surface">{form.stockQuantity || "—"}</dd>
          </div>
          <div>
            <dt className="text-label-sm text-on-surface-variant">Cân nặng</dt>
            <dd className="text-body-md text-on-surface">
              {form.weightGram ? `${form.weightGram} g` : "—"}
            </dd>
          </div>
        </dl>

        <div className="mt-4">
          <dt className="text-label-sm text-on-surface-variant">Mô tả</dt>
          <dd className="mt-1 whitespace-pre-wrap text-body-md text-on-surface">
            {form.description || "—"}
          </dd>
        </div>

        {mediaUrls.length > 0 ? (
          <div className="mt-6">
            <p className="mb-2 text-label-sm text-on-surface-variant">Hình ảnh</p>
            <div className="flex flex-wrap gap-2">
              {mediaUrls.map((url) => (
                <img
                  key={url}
                  src={url}
                  alt=""
                  className="h-16 w-16 rounded-lg border border-outline-variant object-cover"
                />
              ))}
            </div>
          </div>
        ) : null}

        {attributes.length > 0 ? (
          <div className="mt-6">
            <p className="mb-2 text-label-sm text-on-surface-variant">Thuộc tính</p>
            <ul className="space-y-1 text-body-sm text-on-surface">
              {attributes.map((a) => (
                <li key={a.name}>
                  <span className="font-medium">{a.name}:</span> {a.value}
                </li>
              ))}
            </ul>
          </div>
        ) : null}
      </section>

      <section className="rounded-xl border border-outline-variant bg-surface-container-low p-6">
        <h3 className="mb-4 text-label-lg font-semibold text-on-surface">Điều kiện đăng bán</h3>
        <ul className="space-y-2">
          <CheckItem ok={reviewChecklist.hasPrice} label="Đã thiết lập giá bán" />
          <CheckItem ok={reviewChecklist.hasInventory} label="Đã thiết lập tồn kho" />
          <CheckItem ok={reviewChecklist.hasMedia} label="Có ít nhất một ảnh" />
          <CheckItem ok={reviewChecklist.hasCategory} label="Danh mục hợp lệ" />
        </ul>
        {!canPublish && (status === "DRAFT" || status === "PAUSED") ? (
          <p className="mt-4 text-body-sm text-on-surface-variant">
            Hoàn thiện các mục trên trước khi đăng bán.
          </p>
        ) : null}
      </section>

      {apiError ? (
        <div className="rounded-lg border border-error/30 bg-error-container/40 p-4" role="alert">
          <p className="text-sm text-on-error-container">{apiError}</p>
        </div>
      ) : null}
    </div>
  );
}
