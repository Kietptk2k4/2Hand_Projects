import { AddressPicker } from "./AddressPicker";

export function ShippingAddressSection({
  addresses,
  selectedAddressId,
  onSelect,
  disabled,
}) {
  return (
    <AddressPicker
      addresses={addresses}
      selectedAddressId={selectedAddressId}
      onSelect={onSelect}
      disabled={disabled}
    />
  );
}