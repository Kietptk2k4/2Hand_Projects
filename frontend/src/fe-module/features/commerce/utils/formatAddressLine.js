const LOCATION_LABELS = {
  "79": "TP. Hồ Chí Minh",
  "760": "Quận 1",
  "761": "Quận 3",
  "26734": "Phường Bến Nghé",
  "26735": "Phường Bến Thành",
  "01": "Hà Nội",
  "001": "Quận Ba Đình",
  "002": "Quận Hoàn Kiếm",
  "00001": "Phường Phúc Xá",
  "00002": "Phường Tràng Tiền",
  "48": "Đà Nẵng",
  "490": "Quận Hải Châu",
  "49001": "Phường Hải Châu I",
};

function labelForCode(code) {
  if (!code) return "";
  return LOCATION_LABELS[code] || code;
}

export function formatAddressLine(address) {
  if (!address) return "";

  const parts = [
    labelForCode(address.wardCode),
    labelForCode(address.districtCode),
    labelForCode(address.provinceCode),
  ].filter(Boolean);

  return `${address.addressDetail}${parts.length ? `, ${parts.join(", ")}` : ""}`;
}

export function formatAddressHeader(address) {
  if (!address) return "";
  return `${address.receiverName} · ${address.phone}`;
}
