/** GHN master-data: province_id, district_id, ward_code via GhnAddressFields + shipping/ghn APIs */
export const VN_PHONE_REGEX = /^(0|\+84)\d{9,10}$/;

export const EMPTY_ADDRESS_FORM = {
  receiverName: "",
  phone: "",
  provinceCode: "",
  districtCode: "",
  wardCode: "",
  addressDetail: "",
  isDefault: false,
};
