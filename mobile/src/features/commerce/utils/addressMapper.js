export function mapAddressItem(item) {
  return {
    id: item.id || item.address_id,
    receiverName: item.receiver_name,
    phone: item.phone,
    provinceCode: item.province_code,
    districtCode: item.district_code,
    wardCode: item.ward_code,
    addressDetail: item.address_detail,
    isDefault: Boolean(item.is_default),
    createdAt: item.created_at,
    updatedAt: item.updated_at,
  };
}

export function mapAddressesResponse(data) {
  const list = data?.addresses || [];
  return sortAddressesClient(list.map(mapAddressItem));
}

export function mapCreateAddressResponse(data) {
  return mapAddressItem(data);
}

export function mapDeleteAddressResponse(data) {
  if (!data) return null;

  return {
    addressId: data.address_id,
    userId: data.user_id,
    wasDefault: Boolean(data.was_default),
    newDefaultAddressId: data.new_default_address_id ?? null,
    deletedAt: data.deleted_at,
  };
}

export function sortAddressesClient(addresses) {
  return [...addresses].sort((a, b) => {
    if (a.isDefault !== b.isDefault) {
      return a.isDefault ? -1 : 1;
    }
    const updatedA = new Date(a.updatedAt || 0).getTime();
    const updatedB = new Date(b.updatedAt || 0).getTime();
    if (updatedA !== updatedB) {
      return updatedB - updatedA;
    }
    return new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime();
  });
}

export function addressFormToInitialValues(address) {
  if (!address) {
    return {
      receiverName: "",
      phone: "",
      provinceCode: "",
      districtCode: "",
      wardCode: "",
      addressDetail: "",
      isDefault: false,
    };
  }

  return {
    receiverName: address.receiverName || "",
    phone: address.phone || "",
    provinceCode: address.provinceCode || "",
    districtCode: address.districtCode || "",
    wardCode: address.wardCode || "",
    addressDetail: address.addressDetail || "",
    isDefault: Boolean(address.isDefault),
  };
}

export function toCreateAddressPayload(form) {
  return {
    receiver_name: form.receiverName?.trim(),
    phone: form.phone?.trim(),
    province_code: form.provinceCode,
    district_code: form.districtCode,
    ward_code: form.wardCode,
    address_detail: form.addressDetail?.trim(),
    is_default: Boolean(form.isDefault),
  };
}

export function toUpdateAddressPayload(form) {
  return {
    receiver_name: form.receiverName?.trim(),
    phone: form.phone?.trim(),
    province_code: form.provinceCode,
    district_code: form.districtCode,
    ward_code: form.wardCode,
    address_detail: form.addressDetail?.trim(),
    is_default: Boolean(form.isDefault),
  };
}
