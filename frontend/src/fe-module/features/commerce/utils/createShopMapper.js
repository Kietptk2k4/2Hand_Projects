function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapCreateShopPayload(form) {
  const body = {
    shop_name: form.shopName.trim(),
  };

  const description = form.description?.trim();
  if (description) {
    body.description = description;
  }

  if (form.avatarUrl?.trim()) {
    body.avatar_url = form.avatarUrl.trim();
  }

  if (form.coverUrl?.trim()) {
    body.cover_url = form.coverUrl.trim();
  }

  if (form.includePickup) {
    body.pickup_profile = {
      pickup_name: form.pickup.pickupName.trim(),
      phone: form.pickup.phone.trim().replace(/\s/g, ""),
      province_code: form.pickup.provinceCode,
      district_code: form.pickup.districtCode,
      ward_code: form.pickup.wardCode,
      address_detail: form.pickup.addressDetail.trim(),
    };
  }

  return body;
}

export function mapCreateShopResponse(data) {
  if (!data) return null;

  return {
    shopId: pick(data, "shopId", "shop_id"),
    sellerId: pick(data, "sellerId", "seller_id"),
    shopName: pick(data, "shopName", "shop_name"),
    description: data.description ?? "",
    avatarUrl: pick(data, "avatarUrl", "avatar_url"),
    coverUrl: pick(data, "coverUrl", "cover_url"),
    status: data.status,
    isVacation: Boolean(data.is_vacation ?? data.isVacation),
    shippingProfileCreated: Boolean(
      data.shipping_profile_created ?? data.shippingProfileCreated,
    ),
    createdAt: pick(data, "createdAt", "created_at"),
    updatedAt: pick(data, "updatedAt", "updated_at"),
  };
}
