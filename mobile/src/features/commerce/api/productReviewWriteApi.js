import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchReviewContext(orderItemId) {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/reviews/context", {
      params: { order_item_id: orderItemId },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchMyProductReview(productId) {
  try {
    const response = await commerceApiClient.get(
      `/commerce/api/v1/me/products/${productId}/review`,
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchReviewForEdit(reviewId) {
  try {
    const response = await commerceApiClient.get(`/commerce/api/v1/reviews/${reviewId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createProductReview({ orderItemId, rating, comment }) {
  try {
    const body = {
      order_item_id: orderItemId,
      rating,
    };
    if (comment != null && comment !== "") {
      body.comment = comment;
    }

    const response = await commerceApiClient.post("/commerce/api/v1/reviews", body);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

function toFormDataFile(asset) {
  const uri = asset.uri;
  const name = asset.name || asset.fileName || `review-media-${Date.now()}`;
  const type = asset.type || asset.mimeType || "image/jpeg";
  return { uri, name, type };
}

export async function uploadReviewMedia(reviewId, files) {
  const formData = new FormData();
  (files || []).forEach((file) => {
    formData.append("files", toFormDataFile(file));
  });

  try {
    const response = await commerceApiClient.post(
      `/commerce/api/v1/reviews/${reviewId}/media`,
      formData,
      {
        transformRequest: [
          (data, headers) => {
            if (headers) {
              delete headers["Content-Type"];
            }
            return data;
          },
        ],
      },
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateProductReview(reviewId, patch) {
  try {
    const body = {};
    if (patch.rating !== undefined) {
      body.rating = patch.rating;
    }
    if (patch.comment !== undefined) {
      body.comment = patch.comment;
    }

    const response = await commerceApiClient.patch(`/commerce/api/v1/reviews/${reviewId}`, body);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
