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
  return list.map(mapAddressItem);
}

export function mapCreateAddressResponse(data) {
  return mapAddressItem(data);
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
