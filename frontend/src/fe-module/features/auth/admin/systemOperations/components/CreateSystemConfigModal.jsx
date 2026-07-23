import { useState } from "react";
import { mapApiFieldErrors } from "../utils/systemConfigDisplayUtils.js";
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
  const [fieldErrors, setFieldErrors] = useState({});

  const handleClose = () => {
    setForm(defaultForm);
    setFieldErrors({});
    onClose?.();
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFieldErrors({});
    try {
      await onSubmit?.(form);
      setForm(defaultForm);
    } catch (error) {
      setFieldErrors(mapApiFieldErrors(error?.errors));
    }
  };

  return (
    <CreateSystemConfigModalView
      open={open}
      form={form}
      fieldErrors={fieldErrors}
      pending={pending}
      onFieldChange={(patch) => setForm((prev) => ({ ...prev, ...patch }))}
      onClose={handleClose}
      onSubmit={handleSubmit}
    />
  );
}
