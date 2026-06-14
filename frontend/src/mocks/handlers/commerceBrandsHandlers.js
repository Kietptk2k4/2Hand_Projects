import { delay, http, HttpResponse } from "msw";
import { listActiveBrands } from "../data/adminCatalogData";
import { apiSuccess } from "../utils/response";

export const commerceBrandsHandlers = [
  http.get("*/commerce/api/v1/brands", async () => {
    await delay(80);
    return HttpResponse.json(apiSuccess({ items: listActiveBrands() }));
  }),
];
