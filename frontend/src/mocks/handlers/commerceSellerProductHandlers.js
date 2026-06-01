import { delay, http, HttpResponse } from "msw";
import {
  archiveProductForSeller,
  createProductForSeller,
  getSellerProductForUser,
  listSellerProducts,
  pauseProductForSeller,
  publishProductForSeller,
  setProductMedia,
  updateProductAttributesForSeller,
  updateProductForSeller,
  updateProductInventoryForSeller,
  updateProductMediaForSeller,
  updateProductPriceForSeller,
} from "../data/commerceSellerProductData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function requireAuth(request) {
  const user = getUserByToken(request);
  if (!user) {
    return {
      error: HttpResponse.json(apiError("COMMERCE-401", "Authentication required."), {
        status: 401,
      }),
    };
  }
  return { user };
}

function mapError(result) {
  return HttpResponse.json(apiError(result.error, result.message || "Co loi xay ra."), {
    status: result.status,
  });
}

export const commerceSellerProductHandlers = [
  /** FE-only list until backend exposes seller product catalog */
  http.get("*/commerce/api/v1/seller/products", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page") || "1");
    const limit = Number(url.searchParams.get("limit") || "10");
    const status = url.searchParams.get("status") || undefined;
    const q = url.searchParams.get("q") || undefined;

    const result = listSellerProducts(auth.user.id, { page, limit, status, q });
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach san pham thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/seller/products/:productId", async ({ params, request }) => {
    await delay(200);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = getSellerProductForUser(auth.user.id, params.productId);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet san pham thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/seller/products", async ({ request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = createProductForSeller(auth.user.id, body);
    if (result.error) return mapError(result);

    if (body.thumbnail_url) {
      setProductMedia(auth.user.id, result.data.product_id, body.thumbnail_url);
    }

    return HttpResponse.json(
      apiSuccess(200, "Tao san pham draft thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.patch("*/commerce/api/v1/seller/products/:productId", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = updateProductForSeller(auth.user.id, params.productId, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat san pham thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.put("*/commerce/api/v1/seller/products/:productId/attributes", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = updateProductAttributesForSeller(auth.user.id, params.productId, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat thuoc tinh san pham thanh cong.", result.data),
      { status: 200 },
    );
  }),

  /** FE-only media — chờ API upload chính thức */
  http.patch("*/commerce/api/v1/seller/products/:productId/media", async ({ params, request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = updateProductMediaForSeller(auth.user.id, params.productId, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat hinh anh san pham thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/seller/products/:productId/prices", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = updateProductPriceForSeller(auth.user.id, params.productId, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat gia san pham thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.patch("*/commerce/api/v1/seller/products/:productId/inventory", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = updateProductInventoryForSeller(auth.user.id, params.productId, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat ton kho san pham thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/seller/products/:productId/publish", async ({ params, request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = publishProductForSeller(auth.user.id, params.productId);
    if (result.error) return mapError(result);

    const message = result.data.already_published
      ? "San pham da duoc dang ban."
      : "Publish san pham thanh cong.";

    return HttpResponse.json(apiSuccess(200, message, result.data), { status: 200 });
  }),

  http.post("*/commerce/api/v1/seller/products/:productId/pause", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = pauseProductForSeller(auth.user.id, params.productId);
    if (result.error) return mapError(result);

    const message = result.data.already_paused
      ? "San pham da tam dung."
      : "Pause san pham thanh cong.";

    return HttpResponse.json(apiSuccess(200, message, result.data), { status: 200 });
  }),

  http.post("*/commerce/api/v1/seller/products/:productId/archive", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = archiveProductForSeller(auth.user.id, params.productId);
    if (result.error) return mapError(result);

    const message = result.data.already_archived
      ? "San pham da duoc archive truoc do."
      : "Archive san pham thanh cong.";

    return HttpResponse.json(apiSuccess(200, message, result.data), { status: 200 });
  }),
];
