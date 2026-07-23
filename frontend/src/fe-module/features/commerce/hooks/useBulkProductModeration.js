import { useCallback, useState } from "react";
import {
  removeProduct,
  restoreProduct,
} from "../../auth/admin/contentModeration/api/contentModerationAdminApi.js";
import {
  mapModerationPayload,
  mapProductModerationResponse,
} from "../../auth/admin/contentModeration/utils/contentModerationAdminMapper.js";
import { mapContentModerationApiError } from "../../auth/admin/contentModeration/utils/mapContentModerationApiError.js";

async function executeProductBulkAction(productId, mode, { reason, note }) {
  const body = mapModerationPayload({ reason, note });
  if (mode === "restore") {
    const data = await restoreProduct(productId, body);
    return { productId, result: mapProductModerationResponse(data) };
  }
  const data = await removeProduct(productId, body);
  return { productId, result: mapProductModerationResponse(data) };
}

export function useBulkProductModeration() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const execute = useCallback(async ({ productIds, mode, reason, note }) => {
    if (!productIds?.length) {
      return { succeeded: [], failed: [] };
    }

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const results = await Promise.allSettled(
        productIds.map((productId) => executeProductBulkAction(productId, mode, { reason, note })),
      );

      const succeeded = [];
      const failed = [];

      results.forEach((entry, index) => {
        const productId = productIds[index];
        if (entry.status === "fulfilled") {
          succeeded.push(entry.value);
        } else {
          failed.push({
            productId,
            message: mapContentModerationApiError(entry.reason),
          });
        }
      });

      if (failed.length && !succeeded.length) {
        setSubmitError(failed[0]?.message || "Không thể thực hiện thao tác hàng loạt.");
      }

      return { succeeded, failed };
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  const clearError = useCallback(() => setSubmitError(""), []);

  return {
    isSubmitting,
    submitError,
    execute,
    clearError,
  };
}
