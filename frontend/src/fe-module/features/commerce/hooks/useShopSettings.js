import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { updateShopProfile, updateShopVacation } from "../api/sellerShopApi";
import {
  EMPTY_SHOP_SETTINGS_FORM,
  mapShopSettingsApiError,
  SHOP_NAME_MAX,
  VACATION_MESSAGE_MAX,
} from "../constants/shopSettingsConstants";
import { APP_ROUTES } from "../../../shared/constants/routes";
import {
  computeDirtyProfile,
  computeDirtyVacation,
  mapUpdateShopProfilePayload,
  mapUpdateShopVacationPayload,
  mapUpdateShopProfileResponse,
  mapUpdateShopVacationResponse,
  shopToForm,
} from "../utils/shopSettingsMapper";
import { useMyShop } from "./useMyShop";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useShopSettings() {
  const navigate = useNavigate();
  const { showSessionExpired } = useAuthSession();
  const { shop, isLoading, isNotFound, isError, errorMessage, reload } = useMyShop();

  const [form, setForm] = useState(EMPTY_SHOP_SETTINGS_FORM);
  const [snapshot, setSnapshot] = useState(EMPTY_SHOP_SETTINGS_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isNotFound) {
      navigate(APP_ROUTES.commerceCreateShop, {
        replace: true,
        state: { message: "Bạn chưa có cửa hàng. Hãy tạo shop trước." },
      });
    }
  }, [isNotFound, navigate]);

  useEffect(() => {
    if (!shop) return;
    const next = shopToForm(shop);
    setForm(next);
    setSnapshot(next);
    setFieldErrors({});
    setApiError("");
  }, [shop]);

  const dirtyProfile = useMemo(() => computeDirtyProfile(form, snapshot), [form, snapshot]);
  const dirtyVacation = useMemo(
    () => computeDirtyVacation(form, snapshot),
    [form, snapshot],
  );

  const isDirty = useMemo(
    () => Object.keys(dirtyProfile).length > 0 || dirtyVacation,
    [dirtyProfile, dirtyVacation],
  );

  const updateField = useCallback((name, value) => {
    setForm((prev) => {
      if (name === "isVacation" && value === false) {
        return { ...prev, isVacation: false };
      }
      return { ...prev, [name]: value };
    });
    setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    setApiError("");
  }, []);

  const resetForm = useCallback(() => {
    setForm({ ...snapshot });
    setFieldErrors({});
    setApiError("");
  }, [snapshot]);

  const validate = useCallback(() => {
    const errors = {};

    if (dirtyProfile.shopName || Object.keys(dirtyProfile).length > 0) {
      const name = form.shopName.trim();
      if (!name) {
        errors.shopName = "Vui lòng nhập tên cửa hàng.";
      } else if (name.length > SHOP_NAME_MAX) {
        errors.shopName = `Tên cửa hàng tối đa ${SHOP_NAME_MAX} ký tự.`;
      }
    }

    if (form.isVacation && form.vacationMessage.length > VACATION_MESSAGE_MAX) {
      errors.vacationMessage = `Thông báo tối đa ${VACATION_MESSAGE_MAX} ký tự.`;
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [dirtyProfile, form.isVacation, form.shopName, form.vacationMessage]);

  const save = useCallback(async () => {
    if (!isDirty) return false;
    if (!validate()) return false;

    setIsSubmitting(true);
    setApiError("");

    const profileBody = mapUpdateShopProfilePayload(form, dirtyProfile);
    const vacationBody = dirtyVacation ? mapUpdateShopVacationPayload(form, true) : null;

    let nextShop = shop;

    try {
      if (profileBody) {
        const raw = await updateShopProfile(profileBody);
        nextShop = mapUpdateShopProfileResponse(raw);
      }

      if (vacationBody) {
        const raw = await updateShopVacation(vacationBody);
        const vacationResult = mapUpdateShopVacationResponse(raw);
        if (nextShop) {
          nextShop = {
            ...nextShop,
            isVacation: vacationResult.isVacation,
            vacationMessage: vacationResult.vacationMessage ?? "",
            updatedAt: vacationResult.updatedAt,
          };
        }
      }

      if (nextShop) {
        const nextForm = shopToForm(nextShop);
        setForm(nextForm);
        setSnapshot(nextForm);
      }

      return true;
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return false;
      }
      setApiError(mapShopSettingsApiError(error));
      return false;
    } finally {
      setIsSubmitting(false);
    }
  }, [
    dirtyProfile,
    dirtyVacation,
    form,
    isDirty,
    shop,
    showSessionExpired,
    validate,
  ]);

  return {
    shop,
    form,
    fieldErrors,
    apiError,
    isLoading,
    isNotFound,
    isError,
    loadErrorMessage: errorMessage,
    isSubmitting,
    isDirty,
    updateField,
    resetForm,
    save,
    retry: reload,
  };
}
