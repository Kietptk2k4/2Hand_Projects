import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { getShopById, registerShopFromCreate, updatePublicShopFromSellerRecord } from "./commerceShopData";

const shopBySellerId = new Map();

const VN_PHONE_REGEX = /^(0|\+84)\d{9,10}$/;
const SHOP_NAME_MAX = 255;
const VACATION_MESSAGE_MAX = 500;

function generateShopId() {
  const segment = () =>
    Math.floor(Math.random() * 0x10000)
      .toString(16)
      .padStart(4, "0");
  return `s${segment()}${segment()}-4000-8000-${segment()}${segment()}`;
}

function toMyShopResponse(record) {
  const publicShop = getShopById(record.shop_id);

  return {
    shop_id: record.shop_id,
    seller_id: record.seller_id,
    shop_name: record.shop_name,
    description: record.description ?? "",
    avatar_url: record.avatar_url ?? "",
    cover_url: record.cover_url ?? "",
    status: record.status,
    rating_avg: publicShop?.rating_avg ?? 0,
    rating_count: publicShop?.rating_count ?? 0,
    is_vacation: Boolean(record.is_vacation),
    vacation_message: record.vacation_message ?? null,
    created_at: record.created_at,
    updated_at: record.updated_at,
  };
}

export function getShopBySellerId(userId) {
  return shopBySellerId.get(userId) || null;
}

export function getMyShopForSeller(userId) {
  const record = getShopBySellerId(userId);
  if (!record) {
    return { error: "COMMERCE-404-SHOP", status: 404, message: "Ban chua co shop." };
  }
  return { data: toMyShopResponse(record) };
}

export function updateShopProfileForSeller(userId, body) {
  const record = getShopBySellerId(userId);
  if (!record) {
    return { error: "COMMERCE-404-SHOP", status: 404, message: "Ban chua co shop." };
  }

  const keys = ["shop_name", "description", "avatar_url", "cover_url"];
  const hasField = keys.some((key) => body?.[key] !== undefined);

  if (!hasField) {
    return {
      error: "COMMERCE-400-VALIDATION",
      status: 400,
      message: "Phai gui it nhat mot truong cap nhat.",
    };
  }

  if (body.shop_name !== undefined) {
    const name = String(body.shop_name).trim();
    if (!name) {
      return {
        error: "COMMERCE-400-VALIDATION",
        status: 400,
        message: "Ten shop khong duoc de trong.",
      };
    }
    if (name.length > SHOP_NAME_MAX) {
      return {
        error: "COMMERCE-400-VALIDATION",
        status: 400,
        message: "Ten shop qua dai.",
      };
    }
    record.shop_name = name;
  }

  if (body.description !== undefined) {
    record.description = String(body.description).trim();
  }

  if (body.avatar_url !== undefined) {
    const url = String(body.avatar_url).trim();
    if (url && !url.startsWith("http")) {
      return { error: "COMMERCE-400-MEDIA-URL", status: 400, message: "URL avatar khong hop le." };
    }
    record.avatar_url = url;
  }

  if (body.cover_url !== undefined) {
    const url = String(body.cover_url).trim();
    if (url && !url.startsWith("http")) {
      return { error: "COMMERCE-400-MEDIA-URL", status: 400, message: "URL cover khong hop le." };
    }
    record.cover_url = url;
  }

  record.updated_at = new Date().toISOString();
  shopBySellerId.set(userId, record);
  updatePublicShopFromSellerRecord(record);

  return { data: toMyShopResponse(record) };
}

export function updateShopVacationForSeller(userId, body) {
  const record = getShopBySellerId(userId);
  if (!record) {
    return { error: "COMMERCE-404-SHOP", status: 404, message: "Ban chua co shop." };
  }

  if (body?.is_vacation === undefined) {
    return {
      error: "COMMERCE-400-VALIDATION",
      status: 400,
      message: "Thieu is_vacation.",
    };
  }

  const isVacation = Boolean(body.is_vacation);

  if (isVacation && body.vacation_message != null) {
    const message = String(body.vacation_message);
    if (message.length > VACATION_MESSAGE_MAX) {
      return {
        error: "COMMERCE-400-VALIDATION",
        status: 400,
        message: "Thong bao nghi le qua dai.",
      };
    }
    record.vacation_message = message.trim() || null;
  }

  record.is_vacation = isVacation;
  if (!isVacation) {
    record.vacation_message = null;
  }

  record.updated_at = new Date().toISOString();
  shopBySellerId.set(userId, record);
  updatePublicShopFromSellerRecord(record);

  return {
    data: {
      shop_id: record.shop_id,
      seller_id: record.seller_id,
      status: record.status,
      is_vacation: record.is_vacation,
      vacation_message: record.vacation_message,
      updated_at: record.updated_at,
    },
  };
}

export function createShopForUser(userId, body) {
  const shopName = body?.shop_name?.trim();

  if (!shopName) {
    return { error: "COMMERCE-400-VALIDATION", status: 400, message: "Ten shop la bat buoc." };
  }

  if (shopName.length > SHOP_NAME_MAX) {
    return { error: "COMMERCE-400-VALIDATION", status: 400, message: "Ten shop qua dai." };
  }

  const existing = getShopBySellerId(userId);
  if (existing) {
    return {
      error: "COMMERCE-409-SHOP-EXISTS",
      status: 409,
      message: "Ban da co shop tren 2Hands.",
      data: { shop_id: existing.shop_id },
    };
  }

  const pickup = body?.pickup_profile;
  if (pickup) {
    const required = [
      "pickup_name",
      "phone",
      "province_code",
      "district_code",
      "ward_code",
      "address_detail",
    ];
    const missing = required.some((key) => !pickup[key]?.toString().trim());
    if (missing) {
      return {
        error: "COMMERCE-400-VALIDATION",
        status: 400,
        message: "Thieu thong tin dia chi lay hang.",
      };
    }

    const normalizedPhone = pickup.phone.trim().replace(/\s/g, "");
    if (!VN_PHONE_REGEX.test(normalizedPhone)) {
      return {
        error: "COMMERCE-400-VALIDATION",
        status: 400,
        message: "So dien thoai khong hop le.",
      };
    }
  }

  const now = new Date().toISOString();
  const shopId = generateShopId();

  const record = {
    shop_id: shopId,
    seller_id: userId,
    shop_name: shopName,
    description: body?.description?.trim() || "",
    avatar_url:
      body?.avatar_url?.trim() ||
      `https://picsum.photos/seed/shop-${shopId}-avatar/200/200`,
    cover_url:
      body?.cover_url?.trim() ||
      `https://picsum.photos/seed/shop-${shopId}-cover/1200/400`,
    status: "ACTIVE",
    is_vacation: false,
    vacation_message: null,
    shipping_profile_created: Boolean(pickup),
    created_at: now,
    updated_at: now,
    pickup_profile: pickup || null,
  };

  shopBySellerId.set(userId, record);
  registerShopFromCreate(record);

  return { data: record };
}

/** QA: storefront sau seed — active@2hands.vn */
export const MOCK_DEMO_SELLER_SHOP_ID = "s2000000-0000-4000-8000-000000000010";

/** Dev mock: demo buyer có shop sẵn để QA settings không cần create trước. */
function seedDemoSellerShop() {
  if (getShopBySellerId(MOCK_CART_DEMO_USER_ID)) return;

  const shopId = MOCK_DEMO_SELLER_SHOP_ID;
  const now = "2026-05-01T08:00:00Z";

  const record = {
    shop_id: shopId,
    seller_id: MOCK_CART_DEMO_USER_ID,
    shop_name: "Cửa hàng Demo 2Hands",
    description: "Shop demo cho seller active@2hands.vn — chỉnh sửa tại Cài đặt cửa hàng.",
    avatar_url: "https://picsum.photos/seed/demo-seller-shop-avatar/200/200",
    cover_url: "https://picsum.photos/seed/demo-seller-shop-cover/1200/400",
    status: "ACTIVE",
    is_vacation: false,
    vacation_message: null,
    shipping_profile_created: false,
    created_at: now,
    updated_at: now,
    pickup_profile: null,
  };

  shopBySellerId.set(MOCK_CART_DEMO_USER_ID, record);
  registerShopFromCreate(record);
}

seedDemoSellerShop();
