import { fetchSellerProductList } from "../../commerce/api/sellerProductApi";
import { fetchProductDetailIfAvailable } from "../../commerce/api/productDetailApi";
import { mapProductDetailResponse } from "../../commerce/utils/productDetailMapper";
import { mapSellerProductListResponse } from "../../commerce/utils/sellerProductMapper";
import { mapAxiosError } from "./socialApiResponse";
import {
  mapProductDetailToCatalogEntry,
  mapSellerListItemToPickerProduct,
  TAGGABLE_PRODUCT_STATUSES,
} from "../utils/postProductTagMapper";
import {
  getCachedProductCatalogEntry,
  isProductCatalogEntryMissing,
  markProductCatalogEntryMissing,
  setCachedProductCatalogEntry,
} from "../utils/productTagEnrichmentCache";

const pendingCatalogLoads = new Map();

export async function fetchTaggableSellerProducts({ q, page = 1, limit = 50 } = {}) {
  try {
    const data = await fetchSellerProductList({ page, limit, q: q?.trim() || undefined });
    const { items } = mapSellerProductListResponse(data);

    const products = items
      .filter((item) => TAGGABLE_PRODUCT_STATUSES.has(item.status))
      .map(mapSellerListItemToPickerProduct)
      .filter(Boolean);

    for (const product of products) {
      setCachedProductCatalogEntry(product.productId, {
        productId: product.productId,
        name: product.name,
        category: product.category,
        imageUrl: product.imageUrl,
        defaultPrice: product.defaultPrice,
      });
    }

    return products;
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function loadProductCatalogEntry(productId) {
  if (!productId) return null;

  const cached = getCachedProductCatalogEntry(productId);
  if (cached) return cached;

  if (isProductCatalogEntryMissing(productId)) {
    return null;
  }

  const pending = pendingCatalogLoads.get(productId);
  if (pending) {
    return pending;
  }

  const loadPromise = (async () => {
    const data = await fetchProductDetailIfAvailable(productId);
    if (!data) {
      markProductCatalogEntryMissing(productId);
      return null;
    }

    const detail = mapProductDetailResponse(data);
    const entry = mapProductDetailToCatalogEntry(detail);
    if (entry) {
      setCachedProductCatalogEntry(productId, entry);
      return entry;
    }

    markProductCatalogEntryMissing(productId);
    return null;
  })();

  pendingCatalogLoads.set(productId, loadPromise);

  try {
    return await loadPromise;
  } finally {
    pendingCatalogLoads.delete(productId);
  }
}