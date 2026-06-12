const provinceLabels = new Map();
const districtLabels = new Map();
const wardLabels = new Map();

export function cacheGhnProvinceOptions(options = []) {
  options.forEach((item) => {
    if (item?.value) provinceLabels.set(String(item.value), item.label);
  });
}

export function cacheGhnDistrictOptions(options = []) {
  options.forEach((item) => {
    if (item?.value) districtLabels.set(String(item.value), item.label);
  });
}

export function cacheGhnWardOptions(options = []) {
  options.forEach((item) => {
    if (item?.value) wardLabels.set(String(item.value), item.label);
  });
}

export function getGhnProvinceLabel(code) {
  if (!code) return "";
  return provinceLabels.get(String(code)) || String(code);
}

export function getGhnDistrictLabel(code) {
  if (!code) return "";
  return districtLabels.get(String(code)) || String(code);
}

export function getGhnWardLabel(code) {
  if (!code) return "";
  return wardLabels.get(String(code)) || String(code);
}