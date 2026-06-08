export function normalizeErrors(errors) {
  if (!errors) return [];
  if (Array.isArray(errors)) return errors;
  if (typeof errors === "object") {
    return Object.entries(errors).map(([field, reason]) => ({
      field,
      reason: Array.isArray(reason) ? reason[0] : reason,
    }));
  }
  return [];
}

export function unwrapResponse(response) {
  const payload = response?.data;
  if (!payload || payload.success !== true) {
    throw {
      code: payload?.code || response?.status || 500,
      message: payload?.message || "Có lỗi xảy ra. Vui lòng thử lại.",
      errors: normalizeErrors(payload?.errors),
    };
  }
  return payload.data;
}

export function mapAxiosError(error) {
  const status = error?.response?.status || error?.code || 500;
  const payload = error?.response?.data;

  if (!error?.response && error?.code) {
    return error;
  }

  return {
    code: payload?.code || status,
    message: payload?.message || "Có lỗi xảy ra. Vui lòng thử lại.",
    errors: normalizeErrors(payload?.errors),
  };
}
