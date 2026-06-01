import { EMPTY_SHOP_SETTINGS_FORM } from "../constants/shopSettingsConstants";

function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapMyShopResponse(data) {
  if (!data) return null;

  return {
    shopId: pick(data, "shopId", "shop_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    shopName: pick(data, "shopName", "shop_name") ?? "",
    description: data.description ?? "",
    avatarUrl: pick(data, "avatarUrl", "avatar_url") ?? "",
    coverUrl: pick(data, "coverUrl", "cover_url") ?? "",
    status: data.status,
    ratingAvg: data.rating_avg ?? data.ratingAvg ?? 0,
    ratingCount: data.rating_count ?? data.ratingCount ?? 0,
    isVacation: Boolean(data.is_vacation ?? data.isVacation),
    vacationMessage: data.vacation_message ?? data.vacationMessage ?? "",
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
  };
}

export function shopToForm(shop) {
  if (!shop) {
    return { ...EMPTY_SHOP_SETTINGS_FORM };
  }

  return {
    shopName: shop.shopName ?? "",
    description: shop.description ?? "",
    avatarUrl: shop.avatarUrl ?? "",
    coverUrl: shop.coverUrl ?? "",
    isVacation: Boolean(shop.isVacation),
    vacationMessage: shop.vacationMessage ?? "",
  };
}

export function mapUpdateShopProfilePayload(form, dirtyProfile) {
  if (!dirtyProfile || Object.keys(dirtyProfile).length === 0) return null;

  const body = {};

  if (dirtyProfile.shopName) {
    body.shop_name = form.shopName.trim();
  }
  if (dirtyProfile.description) {
    body.description = form.description.trim();
  }
  if (dirtyProfile.avatarUrl) {
    body.avatar_url = form.avatarUrl.trim();
  }
  if (dirtyProfile.coverUrl) {
    body.cover_url = form.coverUrl.trim();
  }

  return Object.keys(body).length > 0 ? body : null;
}

export function mapUpdateShopVacationPayload(form, dirtyVacation) {
  if (!dirtyVacation) return null;

  if (!form.isVacation) {
    return { is_vacation: false };
  }

  const message = form.vacationMessage?.trim();
  const body = { is_vacation: true };
  if (message) {
    body.vacation_message = message;
  }
  return body;
}

export function mapUpdateShopProfileResponse(data) {
  return mapMyShopResponse(data);
}

export function mapUpdateShopVacationResponse(data) {
  if (!data) return null;

  return {
    shopId: pick(data, "shopId", "shop_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    status: data.status,
    isVacation: Boolean(data.is_vacation ?? data.isVacation),
    vacationMessage: data.vacation_message ?? data.vacationMessage ?? "",
    updatedAt: pick(data, "updatedAt", "updated_at"),
  };
}

export function computeDirtyProfile(form, snapshot) {
  const dirty = {};
  if (form.shopName !== snapshot.shopName) dirty.shopName = true;
  if (form.description !== snapshot.description) dirty.description = true;
  if (form.avatarUrl !== snapshot.avatarUrl) dirty.avatarUrl = true;
  if (form.coverUrl !== snapshot.coverUrl) dirty.coverUrl = true;
  return dirty;
}

export function computeDirtyVacation(form, snapshot) {
  return (
    form.isVacation !== snapshot.isVacation ||
    form.vacationMessage !== snapshot.vacationMessage
  );
}
