import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";

export const MOCK_ADDRESS_IDS = {
  DEFAULT: "a1000000-0000-4000-8000-000000000001",
  SECOND: "a1000000-0000-4000-8000-000000000002",
};

const addressesByUserId = new Map();
const VN_PHONE_REGEX = /^(0|\+84)\d{9,10}$/;

function seedAddresses() {
  addressesByUserId.set(MOCK_CART_DEMO_USER_ID, [
    {
      id: MOCK_ADDRESS_IDS.DEFAULT,
      receiver_name: "Nguyễn Văn An",
      phone: "0901234567",
      province_code: "79",
      district_code: "760",
      ward_code: "26734",
      address_detail: "123 Nguyễn Huệ",
      is_default: true,
      created_at: "2026-05-01T08:00:00Z",
      updated_at: "2026-05-10T10:00:00Z",
    },
    {
      id: MOCK_ADDRESS_IDS.SECOND,
      receiver_name: "Trần Thị Bình",
      phone: "0918765432",
      province_code: "01",
      district_code: "001",
      ward_code: "00001",
      address_detail: "45 Phố Huế",
      is_default: false,
      created_at: "2026-05-05T09:00:00Z",
      updated_at: "2026-05-05T09:00:00Z",
    },
  ]);
}

seedAddresses();

export function sortAddresses(list) {
  return [...list].sort((a, b) => {
    if (a.is_default !== b.is_default) {
      return b.is_default ? 1 : -1;
    }
    const updatedA = new Date(a.updated_at).getTime();
    const updatedB = new Date(b.updated_at).getTime();
    if (updatedA !== updatedB) {
      return updatedB - updatedA;
    }
    return new Date(b.created_at).getTime() - new Date(a.created_at).getTime();
  });
}

export function getAddressesForUser(userId) {
  const list = addressesByUserId.get(userId) || [];
  return sortAddresses(list.map((item) => ({ ...item })));
}

export function findAddressForUser(userId, addressId) {
  return getAddressesForUser(userId).find((item) => item.id === addressId) || null;
}

function validateAddressRecord(record) {
  if (
    !record.receiver_name?.trim() ||
    !record.phone?.trim() ||
    !record.province_code ||
    !record.district_code ||
    !record.ward_code ||
    !record.address_detail?.trim()
  ) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const normalizedPhone = record.phone.trim().replace(/\s/g, "");
  if (!VN_PHONE_REGEX.test(normalizedPhone)) {
    return { error: "COMMERCE-400-PHONE", status: 400 };
  }

  return { normalizedPhone };
}

function unsetAllDefaults(list) {
  for (const item of list) {
    item.is_default = false;
  }
}

export function toAddressMutationResponse(address, userId) {
  return {
    address_id: address.id,
    user_id: userId,
    receiver_name: address.receiver_name,
    phone: address.phone,
    province_code: address.province_code,
    district_code: address.district_code,
    ward_code: address.ward_code,
    address_detail: address.address_detail,
    is_default: address.is_default,
    created_at: address.created_at,
    updated_at: address.updated_at,
  };
}

export function addAddressForUser(userId, body) {
  const {
    receiver_name,
    phone,
    province_code,
    district_code,
    ward_code,
    address_detail,
    is_default,
  } = body || {};

  const draft = {
    receiver_name,
    phone,
    province_code,
    district_code,
    ward_code,
    address_detail,
  };

  const validation = validateAddressRecord(draft);
  if (validation.error) {
    return validation;
  }

  if (!addressesByUserId.has(userId)) {
    addressesByUserId.set(userId, []);
  }

  const list = addressesByUserId.get(userId);
  const isFirst = list.length === 0;
  const shouldDefault = isFirst || Boolean(is_default);

  if (shouldDefault) {
    unsetAllDefaults(list);
  }

  const now = new Date().toISOString();
  const newAddress = {
    id: `a1000000-0000-4000-8000-${crypto.randomUUID().replace(/-/g, "").slice(0, 12)}`,
    receiver_name: receiver_name.trim(),
    phone: validation.normalizedPhone,
    province_code,
    district_code,
    ward_code,
    address_detail: address_detail.trim(),
    is_default: shouldDefault,
    created_at: now,
    updated_at: now,
  };

  list.push(newAddress);
  return { address: newAddress };
}

export function updateAddressForUser(userId, addressId, patch) {
  if (!patch || Object.keys(patch).length === 0) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const list = addressesByUserId.get(userId) || [];
  const index = list.findIndex((item) => item.id === addressId);
  if (index < 0) {
    return { error: "COMMERCE-404-ADDRESS", status: 404 };
  }

  const current = list[index];
  const merged = {
    ...current,
    receiver_name:
      patch.receiver_name !== undefined ? patch.receiver_name : current.receiver_name,
    phone: patch.phone !== undefined ? patch.phone : current.phone,
    province_code:
      patch.province_code !== undefined ? patch.province_code : current.province_code,
    district_code:
      patch.district_code !== undefined ? patch.district_code : current.district_code,
    ward_code: patch.ward_code !== undefined ? patch.ward_code : current.ward_code,
    address_detail:
      patch.address_detail !== undefined ? patch.address_detail : current.address_detail,
    is_default:
      patch.is_default !== undefined ? Boolean(patch.is_default) : current.is_default,
  };

  const validation = validateAddressRecord(merged);
  if (validation.error) {
    return validation;
  }

  if (patch.is_default === true) {
    unsetAllDefaults(list);
    merged.is_default = true;
  }

  merged.phone = validation.normalizedPhone;
  merged.receiver_name = merged.receiver_name.trim();
  merged.address_detail = merged.address_detail.trim();
  merged.updated_at = new Date().toISOString();

  list[index] = merged;
  return { address: merged };
}

export function deleteAddressForUser(userId, addressId) {
  const list = addressesByUserId.get(userId) || [];
  const index = list.findIndex((item) => item.id === addressId);
  if (index < 0) {
    return { error: "COMMERCE-404-ADDRESS", status: 404 };
  }

  const removed = list[index];
  const wasDefault = removed.is_default;
  list.splice(index, 1);

  let newDefaultAddressId = null;
  if (wasDefault && list.length > 0) {
    const oldest = [...list].sort(
      (a, b) => new Date(a.created_at).getTime() - new Date(b.created_at).getTime(),
    )[0];
    oldest.is_default = true;
    newDefaultAddressId = oldest.id;
  }

  const deletedAt = new Date().toISOString();

  return {
    data: {
      address_id: removed.id,
      user_id: userId,
      was_default: wasDefault,
      new_default_address_id: newDefaultAddressId,
      deleted_at: deletedAt,
    },
  };
}

export function setDefaultAddressForUser(userId, addressId) {
  const list = addressesByUserId.get(userId) || [];
  const target = list.find((item) => item.id === addressId);
  if (!target) {
    return { error: "COMMERCE-404-ADDRESS", status: 404 };
  }

  unsetAllDefaults(list);
  target.is_default = true;
  target.updated_at = new Date().toISOString();

  return { address: target };
}
