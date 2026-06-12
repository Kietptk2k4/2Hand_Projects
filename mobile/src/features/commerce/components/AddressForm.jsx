import { useCallback, useEffect, useState } from "react";
import { Pressable, Switch, Text, View } from "react-native";
import { EMPTY_ADDRESS_FORM } from "../constants/addressFormConstants";
import { mapAddressApiError } from "../constants/addressConstants";
import { addressFormToInitialValues } from "../utils/addressMapper";
import { validateAddressForm } from "../utils/validateAddressForm";
import { useGhnAddressOptions } from "../hooks/useGhnAddressOptions";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { CommerceFormTextInput } from "./CommerceFormTextInput";
import { GHNDistrictPicker } from "./GHNDistrictPicker";
import { GHNProvincePicker } from "./GHNProvincePicker";
import { GHNWardPicker } from "./GHNWardPicker";

function createStyles(colors) {
  return {
    form: { gap: 16 },
    apiError: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.error}4D`,
      backgroundColor: colors.errorContainer,
      padding: 12,
    },
    apiErrorText: { fontSize: 14, color: colors.onErrorContainer },
    ghnError: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: `${colors.error}4D`,
      backgroundColor: colors.errorContainer,
      padding: 12,
      gap: 8,
    },
    retryText: { fontSize: 14, fontWeight: "600", color: colors.primary },
    defaultRow: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      paddingHorizontal: 12,
      paddingVertical: 10,
    },
    defaultLabel: { fontSize: 14, color: colors.onSurface },
    submitButton: {
      borderRadius: 12,
      backgroundColor: colors.primary,
      paddingVertical: 14,
      alignItems: "center",
    },
    submitButtonDisabled: { opacity: 0.6 },
    submitText: { fontSize: 16, fontWeight: "600", color: colors.onPrimary },
  };
}

export function AddressForm({ initialAddress, isSubmitting, onSubmit, submitLabel = "Lưu địa chỉ" }) {
  const styles = useThemedStyles(createStyles);
  const [form, setForm] = useState(EMPTY_ADDRESS_FORM);
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState("");

  const {
    provinces,
    districts,
    wards,
    isLoadingProvinces,
    isLoadingDistricts,
    isLoadingWards,
    loadError,
    retry,
  } = useGhnAddressOptions({
    provinceCode: form.provinceCode,
    districtCode: form.districtCode,
  });

  useEffect(() => {
    setForm(initialAddress ? addressFormToInitialValues(initialAddress) : EMPTY_ADDRESS_FORM);
    setFieldErrors({});
    setApiError("");
  }, [initialAddress]);

  const updateField = useCallback((name, value) => {
    setForm((prev) => {
      const next = { ...prev, [name]: value };
      if (name === "provinceCode") {
        next.districtCode = "";
        next.wardCode = "";
      }
      if (name === "districtCode") {
        next.wardCode = "";
      }
      return next;
    });
    setFieldErrors((prev) => ({ ...prev, [name]: "" }));
    setApiError("");
  }, []);

  const handleSubmit = useCallback(async () => {
    const errors = validateAddressForm(form);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setApiError("");
    try {
      await onSubmit?.(form);
    } catch (error) {
      if (error?.code === "COMMERCE-400-PHONE") {
        setFieldErrors((prev) => ({
          ...prev,
          phone: error?.message || "Số điện thoại không hợp lệ.",
        }));
        return;
      }
      setApiError(mapAddressApiError(error) || "Không thể lưu địa chỉ.");
    }
  }, [form, onSubmit]);

  return (
    <View style={styles.form}>
      {apiError ? (
        <View style={styles.apiError}>
          <Text style={styles.apiErrorText}>{apiError}</Text>
        </View>
      ) : null}

      {loadError ? (
        <View style={styles.ghnError}>
          <Text style={styles.apiErrorText}>{loadError}</Text>
          <Pressable onPress={retry} disabled={isSubmitting}>
            <Text style={styles.retryText}>Thử lại</Text>
          </Pressable>
        </View>
      ) : null}

      <CommerceFormTextInput
        label="Họ tên người nhận"
        value={form.receiverName}
        onChangeText={(value) => updateField("receiverName", value)}
        placeholder="Nguyễn Văn A"
        error={fieldErrors.receiverName}
      />

      <CommerceFormTextInput
        label="Số điện thoại"
        value={form.phone}
        onChangeText={(value) => updateField("phone", value)}
        placeholder="0901234567"
        keyboardType="phone-pad"
        error={fieldErrors.phone}
      />

      <GHNProvincePicker
        value={form.provinceCode}
        options={provinces}
        isLoading={isLoadingProvinces}
        disabled={isSubmitting}
        error={fieldErrors.provinceCode}
        onChange={(value) => updateField("provinceCode", value)}
      />

      <GHNDistrictPicker
        value={form.districtCode}
        options={districts}
        isLoading={isLoadingDistricts}
        disabled={isSubmitting}
        provinceSelected={Boolean(form.provinceCode)}
        error={fieldErrors.districtCode}
        onChange={(value) => updateField("districtCode", value)}
      />

      <GHNWardPicker
        value={form.wardCode}
        options={wards}
        isLoading={isLoadingWards}
        disabled={isSubmitting}
        districtSelected={Boolean(form.districtCode)}
        error={fieldErrors.wardCode}
        onChange={(value) => updateField("wardCode", value)}
      />

      <CommerceFormTextInput
        label="Địa chỉ chi tiết"
        value={form.addressDetail}
        onChangeText={(value) => updateField("addressDetail", value)}
        placeholder="Số nhà, tên đường..."
        multiline
        error={fieldErrors.addressDetail}
      />

      <View style={styles.defaultRow}>
        <Text style={styles.defaultLabel}>Đặt làm địa chỉ mặc định</Text>
        <Switch
          value={form.isDefault}
          onValueChange={(value) => updateField("isDefault", value)}
          disabled={isSubmitting}
        />
      </View>

      <Pressable
        style={[styles.submitButton, isSubmitting ? styles.submitButtonDisabled : null]}
        disabled={isSubmitting}
        onPress={handleSubmit}
      >
        <Text style={styles.submitText}>{isSubmitting ? "Đang lưu..." : submitLabel}</Text>
      </Pressable>
    </View>
  );
}