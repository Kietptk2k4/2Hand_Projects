import { delay, http, HttpResponse } from "msw";
import {
  buildProductDetail,
  isValidProductId,
  MOCK_PRODUCT_DETAIL_IDS,
} from "../data/commerceProductDetailData";
import { apiError, apiSuccess } from "../utils/response";

export const commerceProductDetailHandlers = [
  http.get("*/commerce/api/v1/products/:productId", async ({ params }) => {
    await delay(400);

    const productId = params.productId;

    if (!isValidProductId(productId)) {
      return HttpResponse.json(
        apiError("COMMERCE-400", "productId khong hop le."),
        { status: 400 }
      );
    }

    if (productId === MOCK_PRODUCT_DETAIL_IDS.NOT_FOUND) {
      return HttpResponse.json(
        apiError("COMMERCE-404-PRODUCT", "San pham khong ton tai hoac khong kha dung."),
        { status: 404 }
      );
    }

    const detail = buildProductDetail(productId);
    if (!detail) {
      return HttpResponse.json(
        apiError("COMMERCE-404-PRODUCT", "San pham khong ton tai hoac khong kha dung."),
        { status: 404 }
      );
    }

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet san pham thanh cong.", detail),
      { status: 200 }
    );
  }),
];
