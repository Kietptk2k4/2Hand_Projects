import { datetimeLocalNow } from "../utils/sellerProductMapper";
import { VndPriceInput } from "./VndPriceInput";

const inputClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-4 py-2.5 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary";

const errorClass = "mt-1 text-sm text-error";

export function SellerProductPromotionFields({
  form,
  fieldErrors,
  disabled,
  onFieldChange,
  inputClassName = inputClass,
}) {
  const hasSalePrice = form.salePrice !== "" && form.salePrice != null;
  const minStartAt = datetimeLocalNow();

  const handleSalePriceChange = (value) => {
    if (value === "") {
      onFieldChange("salePrice", "");
      onFieldChange("saleStartAt", "");
      onFieldChange("saleEndAt", "");
      onFieldChange("saleEndForever", true);
      return;
    }

    onFieldChange("salePrice", value);
    if (!form.saleStartAt?.trim()) {
      onFieldChange("saleStartAt", minStartAt);
    }
  };

  const handleSaleEndForeverChange = (checked) => {
    onFieldChange("saleEndForever", checked);
    if (checked) {
      onFieldChange("saleEndAt", "");
    }
  };

  return (
    <>
      <div>
        <label htmlFor="sale-price" className="mb-1 block text-label-md font-medium text-on-surface">
          Giá khuyến mãi (₫)
        </label>
        <VndPriceInput
          id="sale-price"
          className={inputClassName}
          placeholder="Ví dụ: 680.000"
          value={form.salePrice}
          disabled={disabled}
          onChange={handleSalePriceChange}
        />
        {fieldErrors.salePrice ? <p className={errorClass}>{fieldErrors.salePrice}</p> : null}
      </div>

      <div>
        <label htmlFor="sale-start" className="mb-1 block text-label-md font-medium text-on-surface">
          Ngày bắt đầu khuyến mãi
        </label>
        <input
          id="sale-start"
          type="datetime-local"
          className={inputClassName}
          value={form.saleStartAt}
          min={minStartAt}
          disabled={disabled || !hasSalePrice}
          onChange={(e) => onFieldChange("saleStartAt", e.target.value)}
        />
        {fieldErrors.saleStartAt ? <p className={errorClass}>{fieldErrors.saleStartAt}</p> : null}
        {!hasSalePrice ? (
          <p className="mt-1 text-xs text-on-surface-variant">Nhập giá khuyến mãi để thiết lập thời gian.</p>
        ) : null}
      </div>

      <div>
        <label htmlFor="sale-end" className="mb-1 block text-label-md font-medium text-on-surface">
          Ngày kết thúc khuyến mãi
        </label>
        <input
          id="sale-end"
          type="datetime-local"
          className={inputClassName}
          value={form.saleEndAt}
          min={form.saleStartAt || minStartAt}
          disabled={disabled || !hasSalePrice || form.saleEndForever}
          onChange={(e) => onFieldChange("saleEndAt", e.target.value)}
        />
        <label className="mt-2 flex items-center gap-2 text-xs font-medium text-on-surface-variant">
          <input
            type="checkbox"
            className="rounded border-outline-variant text-primary focus:ring-primary/20"
            checked={Boolean(form.saleEndForever)}
            disabled={disabled || !hasSalePrice}
            onChange={(e) => handleSaleEndForeverChange(e.target.checked)}
          />
          Không giới hạn
        </label>
        {fieldErrors.saleEndAt ? <p className={errorClass}>{fieldErrors.saleEndAt}</p> : null}
      </div>
    </>
  );
}
