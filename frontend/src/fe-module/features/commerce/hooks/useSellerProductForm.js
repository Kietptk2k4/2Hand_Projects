import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { SELLER_ACTIVE_CATEGORIES } from "../constants/sellerProductCategories";
import { SELLER_ACTIVE_BRANDS } from "../constants/sellerProductBrands";
import {
  createProduct,
  fetchSellerProductDetail,
  publishProduct,
  updateProduct,
  updateProductAttributes,
  updateProductInventory,
  updateProductMedia,
  updateProductPrice,
} from "../api/sellerProductApi";
import {
  ATTRIBUTE_NAME_MAX,
  ATTRIBUTE_VALUE_MAX,
  EMPTY_CREATE_PRODUCT_FORM,
  EMPTY_PRODUCT_ATTRIBUTES,
  mapSellerProductApiError,
  READ_ONLY_STATUSES,
  TITLE_MAX,
  WIZARD_SESSION_KEY,
} from "../constants/sellerProductConstants";
import {
  detailToFormState,
  mapCreateProductPayload,
  mapCreateProductResponse,
  mapSellerProductDetailResponse,
  mapUpdateInventoryPayload,
  mapUpdatePricePayload,
  mapUpdateProductAttributesPayload,
  mapUpdateProductMediaPayload,
  mapUpdateProductPayload,
} from "../utils/sellerProductMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function readSessionProductId() {
  try {
    return sessionStorage.getItem(WIZARD_SESSION_KEY) || "";
  } catch {
    return "";
  }
}

function writeSessionProductId(id) {
  try {
    if (id) sessionStorage.setItem(WIZARD_SESSION_KEY, id);
    else sessionStorage.removeItem(WIZARD_SESSION_KEY);
  } catch {
    /* ignore */
  }
}

export function useSellerProductForm({ mode, productId: routeProductId }) {
  const { showSessionExpired } = useAuthSession();
  const isEdit = mode === "edit";

  const [step, setStep] = useState(1);
  const [productId, setProductId] = useState(isEdit ? routeProductId || "" : readSessionProductId());
  const [form, setForm] = useState(EMPTY_CREATE_PRODUCT_FORM);
  const [attributes, setAttributes] = useState(EMPTY_PRODUCT_ATTRIBUTES);
  const [mediaUrls, setMediaUrls] = useState([]);
  const [status, setStatus] = useState("DRAFT");
  const [detailFlags, setDetailFlags] = useState({
    hasPrice: false,
    hasInventory: false,
    hasMedia: false,
  });
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [isLoading, setIsLoading] = useState(isEdit);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [lastSavedAt, setLastSavedAt] = useState(null);

  const canEdit = !READ_ONLY_STATUSES.includes(status);
  const maxUnlockedStep = productId ? 4 : 1;

  const updateField = useCallback((name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    setApiError("");
  }, []);

  const handleApiError = useCallback(
    (error) => {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        throw error;
      }
      setApiError(mapSellerProductApiError(error));
      return false;
    },
    [showSessionExpired],
  );

  const applyDetail = useCallback((detail) => {
    const formState = detailToFormState(detail);
    if (formState) setForm(formState);
    setAttributes(
      (detail.attributes || []).map((a) => ({
        name: a.name,
        value: a.value,
      })),
    );
    setMediaUrls(detail.mediaUrls?.length ? [...detail.mediaUrls] : []);
    setStatus(detail.status || "DRAFT");
    setDetailFlags({
      hasPrice: detail.hasPrice,
      hasInventory: detail.hasInventory,
      hasMedia: detail.hasMedia,
    });
    setProductId(detail.productId);
    if (!isEdit) writeSessionProductId(detail.productId);
  }, [isEdit]);

  const loadProduct = useCallback(async () => {
    const id = isEdit ? routeProductId : productId;
    if (!id) {
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setApiError("");
    try {
      const data = await fetchSellerProductDetail(id);
      const detail = mapSellerProductDetailResponse(data);
      if (!detail) throw new Error("Không tải được sản phẩm.");
      applyDetail(detail);
    } catch (error) {
      handleApiError(error);
    } finally {
      setIsLoading(false);
    }
  }, [applyDetail, handleApiError, isEdit, productId, routeProductId]);

  const loadProductRef = useRef(loadProduct);
  loadProductRef.current = loadProduct;

  useEffect(() => {
    const id = isEdit ? routeProductId : productId;
    if (!id) {
      if (isEdit) setIsLoading(false);
      return;
    }
    loadProductRef.current();
  }, [isEdit, routeProductId, productId]);

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
      errors.price = "Vui lòng nhập giá niêm yết hợp lệ.";
    }
    if (form.salePrice !== "") {
      const sale = Number(form.salePrice);
      if (!Number.isFinite(sale) || sale < 0 || sale > price) {
        errors.salePrice = "Giá khuyến mãi phải từ 0 đến giá niêm yết.";
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

  const validateStep3 = useCallback(() => {
    const errors = {};
    if (mediaUrls.length === 0) {
      errors.media = "Cần ít nhất một ảnh sản phẩm.";
    }

    const names = new Set();
    for (let i = 0; i < attributes.length; i += 1) {
      const attr = attributes[i];
      const name = attr.name?.trim();
      const value = attr.value?.trim();
      if (!name || !value) {
        errors[`attr_${i}`] = "Tên và giá trị thuộc tính không được để trống.";
        continue;
      }
      if (name.length > ATTRIBUTE_NAME_MAX || value.length > ATTRIBUTE_VALUE_MAX) {
        errors[`attr_${i}`] = `Tên tối đa ${ATTRIBUTE_NAME_MAX}, giá trị tối đa ${ATTRIBUTE_VALUE_MAX} ký tự.`;
      }
      const key = name.toLowerCase();
      if (key === "brand") {
        errors[`attr_${i}`] = 'Không dùng thuộc tính "brand" — hãy chọn thương hiệu ở bước Thông tin.';
      }
      if (names.has(key)) {
        errors[`attr_${i}`] = "Tên thuộc tính không được trùng.";
      }
      names.add(key);
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [attributes, mediaUrls]);

  const validateCurrentStep = useCallback(
    (targetStep) => {
      if (targetStep === 1) return validateStep1();
      if (targetStep === 2) return validateStep2();
      if (targetStep === 3) return validateStep3();
      return true;
    },
    [validateStep1, validateStep2, validateStep3],
  );

  const saveStep1 = useCallback(async () => {
    if (!validateStep1() || !canEdit) return false;

    setIsSubmitting(true);
    setApiError("");
    try {
      if (productId) {
        const data = await updateProduct(productId, mapUpdateProductPayload(form));
        const detail = mapSellerProductDetailResponse(data);
        if (detail) applyDetail(detail);
      } else {
        const created = await createProduct(mapCreateProductPayload(form));
        const product = mapCreateProductResponse(created);
        const id = product?.productId;
        if (!id) throw new Error("Không nhận được product_id.");
        setProductId(id);
        writeSessionProductId(id);
        setStatus(product.status || "DRAFT");
      }
      setLastSavedAt(new Date());
      return true;
    } catch (error) {
      return handleApiError(error);
    } finally {
      setIsSubmitting(false);
    }
  }, [applyDetail, canEdit, form, handleApiError, isEdit, productId, validateStep1]);

  const saveStep2 = useCallback(async () => {
    if (!productId || !validateStep2() || !canEdit) return false;

    setIsSubmitting(true);
    setApiError("");
    try {
      await updateProductPrice(productId, mapUpdatePricePayload(form));
      await updateProductInventory(productId, mapUpdateInventoryPayload(form));
      setDetailFlags((prev) => ({ ...prev, hasPrice: true, hasInventory: true }));
      setLastSavedAt(new Date());
      await loadProduct();
      return true;
    } catch (error) {
      return handleApiError(error);
    } finally {
      setIsSubmitting(false);
    }
  }, [canEdit, form, handleApiError, loadProduct, productId, validateStep2]);

  const saveStep3 = useCallback(async () => {
    if (!productId || !validateStep3() || !canEdit) return false;

    setIsSubmitting(true);
    setApiError("");
    try {
      await updateProductMedia(productId, mapUpdateProductMediaPayload(mediaUrls));
      await updateProductAttributes(productId, mapUpdateProductAttributesPayload(attributes));
      setDetailFlags((prev) => ({ ...prev, hasMedia: mediaUrls.length > 0 }));
      setLastSavedAt(new Date());
      await loadProduct();
      return true;
    } catch (error) {
      return handleApiError(error);
    } finally {
      setIsSubmitting(false);
    }
  }, [attributes, canEdit, handleApiError, loadProduct, mediaUrls, productId, validateStep3]);

  const saveCurrentStep = useCallback(async () => {
    if (step === 1) return saveStep1();
    if (step === 2) return saveStep2();
    if (step === 3) return saveStep3();
    return true;
  }, [saveStep1, saveStep2, saveStep3, step]);

  const publishProductAction = useCallback(async () => {
    if (!productId) return { ok: false };

    setIsSubmitting(true);
    setApiError("");
    try {
      await publishProduct(productId);
      writeSessionProductId("");
      return { ok: true };
    } catch (error) {
      handleApiError(error);
      return { ok: false };
    } finally {
      setIsSubmitting(false);
    }
  }, [handleApiError, productId]);

  const goToStep = useCallback(
    async (targetStep) => {
      if (targetStep < 1 || targetStep > 4) return false;
      if (targetStep > maxUnlockedStep) return false;
      if (targetStep === step) return true;

      if (targetStep > step && canEdit) {
        const ok = await saveCurrentStep();
        if (!ok) return false;
      }

      setStep(targetStep);
      setApiError("");
      return true;
    },
    [canEdit, maxUnlockedStep, saveCurrentStep, step],
  );

  const goNext = useCallback(async () => {
    if (step >= 4) return false;
    return goToStep(step + 1);
  }, [goToStep, step]);

  const goBack = useCallback(() => {
    if (step <= 1) return;
    setStep((s) => s - 1);
    setApiError("");
  }, [step]);

  const saveDraftShortcut = useCallback(async () => {
    if (!canEdit) return false;
    if (step === 1) return saveStep1();
    if (step === 2) return saveStep2();
    if (step === 3) return saveStep3();
    return true;
  }, [canEdit, saveStep1, saveStep2, saveStep3, step]);

  const reviewChecklist = useMemo(
    () => ({
      hasPrice: detailFlags.hasPrice || (form.price !== "" && Number(form.price) >= 0),
      hasInventory:
        detailFlags.hasInventory ||
        (form.stockQuantity !== "" && Number.isInteger(Number(form.stockQuantity))),
      hasMedia: detailFlags.hasMedia || mediaUrls.length > 0,
      hasCategory: Boolean(form.categoryId),
    }),
    [detailFlags, form.categoryId, form.price, form.stockQuantity, mediaUrls.length],
  );

  const canPublish =
    canEdit &&
    (status === "DRAFT" || status === "PAUSED") &&
    reviewChecklist.hasPrice &&
    reviewChecklist.hasInventory &&
    reviewChecklist.hasMedia &&
    reviewChecklist.hasCategory;

  return {
    mode,
    isEdit,
    step,
    productId,
    form,
    attributes,
    setAttributes,
    mediaUrls,
    setMediaUrls,
    status,
    fieldErrors,
    apiError,
    isLoading,
    isSubmitting,
    lastSavedAt,
    canEdit,
    maxUnlockedStep,
    categories: SELLER_ACTIVE_CATEGORIES,
    brands: SELLER_ACTIVE_BRANDS,
    reviewChecklist,
    canPublish,
    updateField,
    loadProduct,
    saveStep1,
    saveStep2,
    saveStep3,
    saveCurrentStep,
    saveDraftShortcut,
    publishProductAction,
    goNext,
    goBack,
    goToStep,
    validateCurrentStep,
  };
}
