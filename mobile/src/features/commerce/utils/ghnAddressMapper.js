function pick(obj, camel, snake) {
  return obj?.[camel] ?? obj?.[snake];
}

export function mapGhnProvinceOptions(data) {
  const list = data?.provinces || [];
  return list.map((item) => ({
    value: String(pick(item, "provinceId", "province_id")),
    label: pick(item, "provinceName", "province_name") || String(pick(item, "provinceId", "province_id")),
    provinceId: Number(pick(item, "provinceId", "province_id")),
  }));
}

export function mapGhnDistrictOptions(data) {
  const list = data?.districts || [];
  return list.map((item) => ({
    value: String(pick(item, "districtId", "district_id")),
    label: pick(item, "districtName", "district_name") || String(pick(item, "districtId", "district_id")),
    districtId: Number(pick(item, "districtId", "district_id")),
    provinceId: Number(pick(item, "provinceId", "province_id")),
  }));
}

export function mapGhnWardOptions(data) {
  const list = data?.wards || [];
  return list.map((item) => ({
    value: String(pick(item, "wardCode", "ward_code")),
    label: pick(item, "wardName", "ward_name") || String(pick(item, "wardCode", "ward_code")),
    wardCode: String(pick(item, "wardCode", "ward_code")),
    districtId: Number(pick(item, "districtId", "district_id")),
  }));
}