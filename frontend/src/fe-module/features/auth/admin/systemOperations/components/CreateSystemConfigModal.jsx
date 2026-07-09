import { useState } from "react";
import { CreateSystemConfigModalView } from "./CreateSystemConfigModalView.jsx";

const defaultForm = {
  configKey: "",
  configValue: "",
  valueType: "STRING",
  description: "",
  active: true,
  reason: "",
};

export function CreateSystemConfigModal({ open, onClose, onSubmit, pending }) {
  const [form, setForm] = useState(defaultForm);

  const handleClose = () => {
    setForm(defaultForm);
    onClose?.();
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await onSubmit?.(form);
    setForm(defaultForm);
  };

  return (
    <CreateSystemConfigModalView
      open={open}
      form={form}
      pending={pending}
      onFieldChange={(patch) => setForm((prev) => ({ ...prev, ...patch }))}
      onClose={handleClose}
      onSubmit={handleSubmit}
    />
  );
}
