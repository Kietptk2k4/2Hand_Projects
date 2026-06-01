import { registerShopFromCreate } from "./commerceShopData";

const shopBySellerId = new Map();

const VN_PHONE_REGEX = /^(0|\+84)\d{9,10}$/;

function generateShopId() {
  const segment = () =>
    Math.floor(Math.random() * 0x10000)
      .toString(16)
      .padStart(4, "0");
  return `s${segment()}${segment()}-4000-8000-${segment()}${segment()}`;
}

export function getShopBySellerId(userId) {
  return shopBySellerId.get(userId) || null;
}

export function createShopForUser(userId, body) {
  const shopName = body?.shop_name?.trim();

  if (!shopName) {
    return { error: "COMMERCE-400-VALIDATION", status: 400, message: "Ten shop la bat buoc." };
  }

  if (shopName.length > 255) {
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
    shipping_profile_created: Boolean(pickup),
    created_at: now,
    updated_at: now,
    pickup_profile: pickup || null,
  };

  shopBySellerId.set(userId, record);
  registerShopFromCreate(record);

  return { data: record };
}
