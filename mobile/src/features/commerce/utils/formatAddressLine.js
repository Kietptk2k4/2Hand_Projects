import {
  getGhnDistrictLabel,
  getGhnProvinceLabel,
  getGhnWardLabel,
} from "./ghnAddressLabelCache";

function labelForCode(code, resolver) {
  if (!code) return "";
  return resolver(code);
}

export function formatAddressLine(address) {
  if (!address) return "";

  const parts = [
    labelForCode(address.wardCode, getGhnWardLabel),
    labelForCode(address.districtCode, getGhnDistrictLabel),
    labelForCode(address.provinceCode, getGhnProvinceLabel),
  ].filter(Boolean);

  return `${address.addressDetail}${parts.length ? `, ${parts.join(", ")}` : ""}`;
}

export function formatAddressHeader(address) {
  if (!address) return "";
  return `${address.receiverName} · ${address.phone}`;
}