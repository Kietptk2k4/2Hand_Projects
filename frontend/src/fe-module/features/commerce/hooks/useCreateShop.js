import { useCallback, useState } from "react";
import { createShop } from "../api/sellerShopApi";
import {
  EMPTY_CREATE_SHOP_FORM,
  extractExistingShopIdFromError,
  mapCreateShopApiError,
  SHOP_NAME_MAX,
} from "../constants/createShopConstants";
import { VN_PHONE_REGEX } from "../constants/addressFormConstants";
import { mapCreateShopPayload, mapCreateShopResponse } from "../utils/createShopMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useCreateShop() {
  const { showSessionExpired } = useAuthSession();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState(EMPTY_CREATE_SHOP_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [existingShopId, setExistingShopId] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const updateField = useCallback((name, value) => {
    setForm((prev) => {
      if (name.startsWith("pickup.")) {
        const pickupKey = name.replace("pickup.", "");
        const pickup = { ...prev.pickup, [pickupKey]: value };
        if (pickupKey === "provinceCode") {
          pickup.districtCode = "";
          pickup.wardCode = "";
        }
        if (pickupKey === "districtCode") {
          pickup.wardCode = "";
        }
        return { ...prev, pickup };
      }

      return { ...prev, [name]: value };
    });
    setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    setApiError("");
    setExistingShopId(null);
  }, []);

  const validateStep1 = useCallback(() => {
    const errors = {};
    const name = form.shopName.trim();
    if (!name) {
      errors.shopName = "Vui lòng nhập tên shop.";
    } else if (name.length > SHOP_NAME_MAX) {
      errors.shopName = `Tên shop tối đa ${SHOP_NAME_MAX} ký tự.`;
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [form.shopName]);

  const validateStep2 = useCallback(() => {
    if (!form.includePickup) {
      setFieldErrors({});
      return true;
    }

    const errors = {};
    const pickup = form.pickup;

    if (!pickup.pickupName.trim()) {
      errors["pickup.pickupName"] = "Vui lòng nhập tên điểm lấy hàng / kho.";
    }
    if (!pickup.phone.trim()) {
      errors["pickup.phone"] = "Vui lòng nhập số điện thoại.";
    } else if (!VN_PHONE_REGEX.test(pickup.phone.trim().replace(/\s/g, ""))) {
      errors["pickup.phone"] = "Số điện thoại không hợp lệ (0 hoặc +84 và 9–10 chữ số).";
    }
    if (!pickup.provinceCode) {
      errors["pickup.provinceCode"] = "Vui lòng chọn tỉnh/thành phố.";
    }
    if (!pickup.districtCode) {
      errors["pickup.districtCode"] = "Vui lòng chọn quận/huyện.";
    }
    if (!pickup.wardCode) {
      errors["pickup.wardCode"] = "Vui lòng chọn phường/xã.";
    }
    if (!pickup.addressDetail.trim()) {
      errors["pickup.addressDetail"] = "Vui lòng nhập địa chỉ chi tiết.";
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [form.includePickup, form.pickup]);

  const nextStep = useCallback(() => {
    if (!validateStep1()) return false;
    setStep(2);
    return true;
  }, [validateStep1]);

  const prevStep = useCallback(() => {
    setStep(1);
    setApiError("");
    setExistingShopId(null);
  }, []);

  const submit = useCallback(async () => {
    if (!validateStep2()) return null;

    setIsSubmitting(true);
    setApiError("");
    setExistingShopId(null);

    try {
      const payload = mapCreateShopPayload(form);
      const raw = await createShop({
        shopName: payload.shop_name,
        description: payload.description,
        avatarUrl: payload.avatar_url,
        coverUrl: payload.cover_url,
        pickupProfile: payload.pickup_profile
          ? {
              pickupName: payload.pickup_profile.pickup_name,
              phone: payload.pickup_profile.phone,
              provinceCode: payload.pickup_profile.province_code,
              districtCode: payload.pickup_profile.district_code,
              wardCode: payload.pickup_profile.ward_code,
              addressDetail: payload.pickup_profile.address_detail,
            }
          : undefined,
      });
      return mapCreateShopResponse(raw);
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        throw error;
      }

      const shopId = extractExistingShopIdFromError(error);
      if (shopId) {
        setExistingShopId(shopId);
      }

      setApiError(mapCreateShopApiError(error));
      return null;
    } finally {
      setIsSubmitting(false);
    }
  }, [form, showSessionExpired, validateStep2]);

  return {
    step,
    form,
    fieldErrors,
    apiError,
    existingShopId,
    isSubmitting,
    updateField,
    nextStep,
    prevStep,
    submit,
    validateStep1,
    validateStep2,
  };
}
