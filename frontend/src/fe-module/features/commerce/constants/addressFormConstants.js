/** Mock địa giới hành chính — value = code gửi API */
export const MOCK_PROVINCES = [
  { value: "79", label: "TP. Hồ Chí Minh" },
  { value: "01", label: "Hà Nội" },
  { value: "48", label: "Đà Nẵng" },
];

export const MOCK_DISTRICTS_BY_PROVINCE = {
  "79": [
    { value: "760", label: "Quận 1" },
    { value: "761", label: "Quận 3" },
  ],
  "01": [
    { value: "001", label: "Quận Ba Đình" },
    { value: "002", label: "Quận Hoàn Kiếm" },
  ],
  "48": [{ value: "490", label: "Quận Hải Châu" }],
};

export const MOCK_WARDS_BY_DISTRICT = {
  "760": [{ value: "26734", label: "Phường Bến Nghé" }],
  "761": [{ value: "26735", label: "Phường Bến Thành" }],
  "001": [{ value: "00001", label: "Phường Phúc Xá" }],
  "002": [{ value: "00002", label: "Phường Tràng Tiền" }],
  "490": [{ value: "49001", label: "Phường Hải Châu I" }],
};

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
