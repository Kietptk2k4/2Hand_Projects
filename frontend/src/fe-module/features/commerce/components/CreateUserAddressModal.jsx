import { UserAddressFormModal } from "./UserAddressFormModal";

/** @deprecated Use UserAddressFormModal with mode="create" */
export function CreateUserAddressModal(props) {
  return <UserAddressFormModal mode="create" {...props} />;
}
