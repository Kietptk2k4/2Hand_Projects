import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";

export const MOCK_ADDRESS_IDS = {
  DEFAULT: "a1000000-0000-4000-8000-000000000001",
  SECOND: "a1000000-0000-4000-8000-000000000002",
};

const addressesByUserId = new Map();

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

export function getAddressesForUser(userId) {
  return addressesByUserId.get(userId) || [];
}

export function findAddressForUser(userId, addressId) {
  return getAddressesForUser(userId).find((item) => item.id === addressId) || null;
}

const VN_PHONE_REGEX = /^(0|\+84)\d{9,10}$/;

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

  if (
    !receiver_name?.trim() ||
    !phone?.trim() ||
    !province_code ||
    !district_code ||
    !ward_code ||
    !address_detail?.trim()
  ) {
    return { error: "COMMERCE-400-VALIDATION", status: 400 };
  }

  const normalizedPhone = phone.trim().replace(/\s/g, "");
  if (!VN_PHONE_REGEX.test(normalizedPhone)) {
    return { error: "COMMERCE-400-PHONE", status: 400 };
  }

  if (!addressesByUserId.has(userId)) {
    addressesByUserId.set(userId, []);
  }

  const list = addressesByUserId.get(userId);
  const isFirst = list.length === 0;
  const shouldDefault = isFirst || Boolean(is_default);

  if (shouldDefault) {
    for (const item of list) {
      item.is_default = false;
    }
  }

  const now = new Date().toISOString();
  const newAddress = {
    id: `a1000000-0000-4000-8000-${crypto.randomUUID().slice(0, 12)}`,
    receiver_name: receiver_name.trim(),
    phone: normalizedPhone,
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
