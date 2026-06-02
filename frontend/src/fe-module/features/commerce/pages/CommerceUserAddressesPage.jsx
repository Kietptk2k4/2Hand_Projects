import { useCallback, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { DeleteUserAddressConfirmDialog } from "../components/DeleteUserAddressConfirmDialog";
import { UserAddressCard } from "../components/UserAddressCard";
import { UserAddressFormModal } from "../components/UserAddressFormModal";
import { useUserAddresses } from "../hooks/useUserAddresses";

function AddressListSkeleton() {
  return (
    <div className="flex flex-col gap-4" aria-hidden="true">
      {[1, 2].map((key) => (
        <div
          key={key}
          className="h-32 animate-pulse rounded-xl border border-outline-variant bg-surface-container-high"
        />
      ))}
    </div>
  );
}

export function CommerceUserAddressesPage() {
  const {
    addresses,
    isLoading,
    errorMessage,
    isCreating,
    isUpdating,
    isDeleting,
    mutatingAddressId,
    isMutating,
    retry,
    createAddress,
    updateAddress,
    deleteAddress,
    setDefaultAddress,
  } = useUserAddresses();

  const [toastMessage, setToastMessage] = useState("");
  const [formMode, setFormMode] = useState(null);
  const [editingAddress, setEditingAddress] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const dismissToast = useCallback(() => setToastMessage(""), []);

  const openCreate = useCallback(() => {
    setEditingAddress(null);
    setFormMode("create");
  }, []);

  const openEdit = useCallback((address) => {
    setEditingAddress(address);
    setFormMode("edit");
  }, []);

  const closeForm = useCallback(() => {
    setFormMode(null);
    setEditingAddress(null);
  }, []);

  const handleCreateSubmit = useCallback(
    async (form) => {
      const message = await createAddress(form);
      if (message) setToastMessage(message);
    },
    [createAddress],
  );

  const handleEditSubmit = useCallback(
    async (form) => {
      if (!editingAddress?.id) return;
      const message = await updateAddress(editingAddress.id, form);
      if (message) setToastMessage(message);
    },
    [editingAddress, updateAddress],
  );

  const handleSetDefault = useCallback(
    async (address) => {
      const message = await setDefaultAddress(address.id);
      if (message) setToastMessage(message);
    },
    [setDefaultAddress],
  );

  const handleConfirmDelete = useCallback(async () => {
    if (!deleteTarget?.id) return;
    const message = await deleteAddress(deleteTarget.id);
    if (message) {
      setToastMessage(message);
      setDeleteTarget(null);
    }
  }, [deleteAddress, deleteTarget]);

  const isEmpty = !isLoading && !errorMessage && addresses.length === 0;

  return (
    <CommerceShell>
      <div className="mx-auto w-full max-w-[1280px]">
        <header className="mb-6 flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-headline-md font-bold text-on-surface md:text-headline-lg">
              Sổ địa chỉ giao hàng
            </h1>
            <p className="mt-1 text-body-sm text-on-surface-variant">
              Quản lý địa chỉ nhận hàng khi mua sắm. Đơn đã đặt vẫn giữ địa chỉ tại thời điểm thanh
              toán.
            </p>
          </div>
          <button
            type="button"
            onClick={openCreate}
            disabled={isMutating}
            className="rounded-lg bg-primary px-4 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
          >
            Thêm địa chỉ
          </button>
        </header>

        {isLoading ? <AddressListSkeleton /> : null}

        {errorMessage ? (
          <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
            <p className="text-sm text-on-error-container">{errorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {isEmpty ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-10 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              location_off
            </span>
            <p className="text-body-sm text-on-surface-variant">Bạn chưa có địa chỉ giao hàng.</p>
            <button
              type="button"
              onClick={openCreate}
              className="mt-4 rounded-lg bg-primary px-4 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Thêm địa chỉ đầu tiên
            </button>
          </div>
        ) : null}

        {!isLoading && !errorMessage && addresses.length > 0 ? (
          <div className="flex flex-col gap-4">
            {addresses.map((address) => (
              <UserAddressCard
                key={address.id}
                address={address}
                disabled={isMutating}
                isMutating={mutatingAddressId === address.id}
                onEdit={openEdit}
                onSetDefault={handleSetDefault}
                onDelete={setDeleteTarget}
              />
            ))}
          </div>
        ) : null}
      </div>

      <UserAddressFormModal
        mode="create"
        open={formMode === "create"}
        onClose={closeForm}
        onSubmit={handleCreateSubmit}
        isSubmitting={isCreating}
      />

      <UserAddressFormModal
        mode="edit"
        open={formMode === "edit"}
        initialValues={editingAddress}
        onClose={closeForm}
        onSubmit={handleEditSubmit}
        isSubmitting={isUpdating}
      />

      <DeleteUserAddressConfirmDialog
        open={Boolean(deleteTarget)}
        address={deleteTarget}
        isSubmitting={isDeleting}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleConfirmDelete}
      />

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
