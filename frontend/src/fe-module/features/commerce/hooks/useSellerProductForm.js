import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useCommerceCategories } from "./useCommerceCategories";
import { SELLER_ACTIVE_BRANDS } from "../constants/sellerProductBrands";
import { fetchActiveBrands } from "../api/brandsApi";
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
  createStep3Baseline,
  detailToFormState,
  isStep1Dirty,
  isStep2Dirty,
  isStep3Dirty,
  mapCreateProductPayload,
  mapCreateProductResponse,
  mapProductCoreResponse,
  mapSellerProductDetailResponse,
  mapUpdateInventoryPayload,
  mapUpdatePricePayload,
  mapUpdateProductAttributesPayload,
  mapUpdateProductMediaPayload,
  mapUpdateProductPayload,
  mergeProductCoreIntoForm,
  pickStep1FormSlice,
  pickStep2FormSlice,
  resolveInitialWizardStep,
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

function syncBaselinesFromDetail(step1BaselineRef, step2BaselineRef, step3BaselineRef, formState, media, attrs) {
  step1BaselineRef.current = pickStep1FormSlice(formState);
  step2BaselineRef.current = pickStep2FormSlice(formState);
  step3BaselineRef.current = createStep3Baseline(media, attrs);
}

export function useSellerProductForm({ mode, productId: routeProductId, initialStep }) {
  const { showSessionExpired } = useAuthSession();
  const { sellerOptions: categories, isLoading: isLoadingCategories } = useCommerceCategories({
    leafOnly: true,
    includeProductCounts: false,
  });
  const [brands, setBrands] = useState(SELLER_ACTIVE_BRANDS);
  const [isLoadingBrands, setIsLoadingBrands] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setIsLoadingBrands(true);
      try {
        const items = await fetchActiveBrands();
        if (!cancelled && items.length > 0) {
          setBrands(items);
        }
      } catch {
        if (!cancelled) {
          setBrands(SELLER_ACTIVE_BRANDS);
        }
      } finally {
        if (!cancelled) {
          setIsLoadingBrands(false);
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);
  const isEdit = mode === "edit";

  const initialStepRef = useRef(initialStep);
  const hasResolvedInitialStepRef = useRef(false);
  const initialProductIdRef = useRef(isEdit ? routeProductId || "" : readSessionProductId());
  const step1BaselineRef = useRef(null);
  const step2BaselineRef = useRef(null);
  const step3BaselineRef = useRef(null);

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

  const applyDetail = useCallback(
    (detail, { setInitialStep = false } = {}) => {
      const formState = detailToFormState(detail);
      const attrs = (detail.attributes || []).map((a) => ({
        name: a.name,
        value: a.value,
      }));
      const media = detail.mediaUrls?.length ? [...detail.mediaUrls] : [];

      if (formState) setForm(formState);
      setAttributes(attrs);
      setMediaUrls(media);
      setStatus(detail.status || "DRAFT");
      setDetailFlags({
        hasPrice: detail.hasPrice,
        hasInventory: detail.hasInventory,
        hasMedia: detail.hasMedia,
      });
      setProductId(detail.productId);
      if (!isEdit) writeSessionProductId(detail.productId);

      if (formState) {
        syncBaselinesFromDetail(step1BaselineRef, step2BaselineRef, step3BaselineRef, formState, media, attrs);
      }

      if (setInitialStep && !hasResolvedInitialStepRef.current) {
        const shouldResolveStep =
          isEdit || (detail.productId && detail.productId === initialProductIdRef.current);
        if (shouldResolveStep) {
          setStep(
            resolveInitialWizardStep({
              mode: isEdit ? "edit" : "create",
              detail,
              requestedStep: initialStepRef.current,
            }),
          );
        }
        hasResolvedInitialStepRef.current = true;
        initialStepRef.current = undefined;
      }
    },
    [isEdit],
  );

  const loadProduct = useCallback(
    async ({ setInitialStep = false } = {}) => {
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
        applyDetail(detail, { setInitialStep });
      } catch (error) {
        handleApiError(error);
      } finally {
        setIsLoading(false);
      }
    },
    [applyDetail, handleApiError, isEdit, productId, routeProductId],
  );

  const loadProductRef = useRef(loadProduct);
  loadProductRef.current = loadProduct;

  useEffect(() => {
    hasResolvedInitialStepRef.current = false;
    initialStepRef.current = initialStep;
    initialProductIdRef.current = isEdit ? routeProductId || "" : readSessionProductId();
  }, [initialStep, isEdit, routeProductId]);

  useEffect(() => {
    const id = isEdit ? routeProductId : productId;
    if (!id) {
      if (isEdit) setIsLoading(false);
      return;
    }
    loadProductRef.current({ setInitialStep: !hasResolvedInitialStepRef.current });
  }, [isEdit, routeProductId, productId]);

  const validateStep1 = useCallback(() => {
    const errors = {};
    if (!form.categoryId) errors.categoryId = "Vui lòng chọn danh mục.";
    if (!form.brandId) errors.brandId = "Vui lòng chọn thương hiệu.";
    else if (!brands.some((brand) => brand.id === form.brandId)) {
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
        const core = mapProductCoreResponse(data);
        if (core) {
          setForm((prev) => {
            const merged = mergeProductCoreIntoForm(prev, core);
            step1BaselineRef.current = pickStep1FormSlice(merged);
            return merged;
          });
          if (core.status) setStatus(core.status);
          setLastSavedAt(core.updatedAt ? new Date(core.updatedAt) : new Date());
        }
      } else {
        const created = await createProduct(mapCreateProductPayload(form));
        const product = mapCreateProductResponse(created);
        const id = product?.productId;
        if (!id) throw new Error("Không nhận được product_id.");
        setProductId(id);
        writeSessionProductId(id);
        setStatus(product.status || "DRAFT");
        setLastSavedAt(new Date());
      }
      return true;
    } catch (error) {
      return handleApiError(error);
    } finally {
      setIsSubmitting(false);
    }
  }, [canEdit, form, handleApiError, productId, validateStep1]);

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

  const isCurrentStepDirty = useCallback(() => {
    if (step === 1) {
      if (!productId) return true;
      return isStep1Dirty(form, step1BaselineRef.current);
    }
    if (step === 2) return isStep2Dirty(form, step2BaselineRef.current);
    if (step === 3) return isStep3Dirty(mediaUrls, attributes, step3BaselineRef.current);
    return false;
  }, [attributes, form, mediaUrls, productId, step]);

  const persistCurrentStep = useCallback(
    async ({ force = false } = {}) => {
      if (step === 1) {
        if (!productId) return saveStep1();
        if (!force && !isStep1Dirty(form, step1BaselineRef.current)) {
          return validateStep1();
        }
        return saveStep1();
      }
      if (step === 2) {
        if (!productId) return false;
        if (!force && !isStep2Dirty(form, step2BaselineRef.current)) {
          return validateStep2();
        }
        return saveStep2();
      }
      if (step === 3) {
        if (!productId) return false;
        if (!force && !isStep3Dirty(mediaUrls, attributes, step3BaselineRef.current)) {
          return validateStep3();
        }
        return saveStep3();
      }
      return true;
    },
    [attributes, form, mediaUrls, productId, saveStep1, saveStep2, saveStep3, step, validateStep1, validateStep2, validateStep3],
  );

  const saveCurrentStep = useCallback(async () => {
    return persistCurrentStep();
  }, [persistCurrentStep]);

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
      if (targetStep === step) return true;

      const isSequentialForward = targetStep === step + 1;
      if (targetStep > maxUnlockedStep && !isSequentialForward) return false;

      if (targetStep > step && canEdit) {
        const ok = await persistCurrentStep();
        if (!ok) return false;
      }

      setStep(targetStep);
      setApiError("");
      return true;
    },
    [canEdit, maxUnlockedStep, persistCurrentStep, step],
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
    if (step === 4) return true;
    if (!isCurrentStepDirty()) return "no_changes";
    if (step === 1) return (await saveStep1()) ? true : false;
    if (step === 2) return (await saveStep2()) ? true : false;
    if (step === 3) return (await saveStep3()) ? true : false;
    return true;
  }, [canEdit, isCurrentStepDirty, saveStep1, saveStep2, saveStep3, step]);

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
    categories,
    isLoadingCategories,
    brands,
    isLoadingBrands,
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
