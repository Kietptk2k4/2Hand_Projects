import { useCallback, useState } from "react";
import { useCommerceCategories } from "./useCommerceCategories";
import { SELLER_ACTIVE_BRANDS } from "../constants/sellerProductBrands";
import {
  createProduct,
  publishProduct,
  updateProductInventory,
  updateProductPrice,
} from "../api/sellerProductApi";
import {
  EMPTY_CREATE_PRODUCT_FORM,
  mapSellerProductApiError,
  TITLE_MAX,
} from "../constants/sellerProductConstants";
import {
  mapCreateProductPayload,
  mapCreateProductResponse,
  mapUpdateInventoryPayload,
  mapUpdatePricePayload,
} from "../utils/sellerProductMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useCreateSellerProduct() {
  const { showSessionExpired } = useAuthSession();
  const { sellerOptions: categories, isLoading: isLoadingCategories } = useCommerceCategories({
    leafOnly: true,
    includeProductCounts: false,
  });
  const [step, setStep] = useState(1);
  const [form, setForm] = useState(EMPTY_CREATE_PRODUCT_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const updateField = useCallback((name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    setApiError("");
  }, []);

  const validateStep1 = useCallback(() => {
    const errors = {};
    if (!form.categoryId) errors.categoryId = "Vui lòng chọn danh mục.";
    if (!form.brandId) errors.brandId = "Vui lòng chọn thương hiệu.";
    else if (!SELLER_ACTIVE_BRANDS.some((brand) => brand.id === form.brandId)) {
      errors.brandId = "Thương hiệu không hợp lệ.";
    }
    if (!form.title.trim()) errors.title = "Vui lòng nhập tên sản phẩm.";
    else if (form.title.trim().length > TITLE_MAX) {
      errors.title = `Tối đa ${TITLE_MAX} ký tự.`;
    }
    if (!form.description.trim()) errors.description = "Vui lòng nhập mô tả.";
    const weight = Number(form.weightGram);
    if (!form.weightGram || !Number.isInteger(weight) || weight <= 0) {
      errors.weightGram = "Khối lượng phải là số nguyên dương (gram).";
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [form]);

  const validateStep2 = useCallback(() => {
    const errors = {};
    const price = Number(form.price);
    if (form.price === "" || !Number.isFinite(price) || price < 0) {
      errors.price = "Vui lòng nhập giá bán hợp lệ.";
    }
    if (form.salePrice !== "") {
      const sale = Number(form.salePrice);
      if (!Number.isFinite(sale) || sale < 0 || sale > price) {
        errors.salePrice = "Giá khuyến mãi phải từ 0 đến giá bán.";
      }
    }
    const stock = Number(form.stockQuantity);
    if (form.stockQuantity === "" || !Number.isInteger(stock) || (stock !== 0 && stock !== 1)) {
      errors.stockQuantity = "Tồn kho chỉ được 0 hoặc 1 (mỗi listing là một món).";
    }
    const thresholdRaw = form.lowStockThreshold;
    if (thresholdRaw !== "" && thresholdRaw != null) {
      const threshold = Number(thresholdRaw);
      if (!Number.isInteger(threshold) || (threshold !== 0 && threshold !== 1)) {
        errors.lowStockThreshold = "Ngưỡng sắp hết hàng chỉ được 0 hoặc 1.";
      }
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [form]);

  const nextStep = useCallback(() => {
    if (!validateStep1()) return false;
    setStep(2);
    return true;
  }, [validateStep1]);

  const prevStep = useCallback(() => {
    setStep(1);
    setApiError("");
  }, []);

  const runCreateFlow = useCallback(
    async ({ publishAfter }) => {
      if (!validateStep2()) return null;

      setIsSubmitting(true);
      setApiError("");

      try {
        const created = await createProduct(mapCreateProductPayload(form));
        const product = mapCreateProductResponse(created);
        const productId = product?.productId;
        if (!productId) throw new Error("Không nhận được product_id.");

        await updateProductPrice(productId, mapUpdatePricePayload(form));
        await updateProductInventory(productId, mapUpdateInventoryPayload(form));

        let publishError = null;
        if (publishAfter) {
          try {
            await publishProduct(productId);
          } catch (error) {
            publishError = mapSellerProductApiError(error);
          }
        }

        return { productId, publishError };
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setApiError(mapSellerProductApiError(error));
        return null;
      } finally {
        setIsSubmitting(false);
      }
    },
    [form, showSessionExpired, validateStep2],
  );

  const saveDraft = useCallback(() => runCreateFlow({ publishAfter: false }), [runCreateFlow]);

  const saveAndPublish = useCallback(
    () => runCreateFlow({ publishAfter: true }),
    [runCreateFlow],
  );

  return {
    step,
    form,
    fieldErrors,
    apiError,
    isSubmitting,
    categories,
    isLoadingCategories,
    brands: SELLER_ACTIVE_BRANDS,
    updateField,
    nextStep,
    prevStep,
    saveDraft,
    saveAndPublish,
    validateStep1,
  };
}
