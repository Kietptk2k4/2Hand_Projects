import { useState } from "react";
import { mapApiFieldErrors } from "../utils/announcementDisplayUtils.js";
import { CreateSystemAnnouncementDrawerView } from "./CreateSystemAnnouncementDrawerView.jsx";

const defaultForm = {
  title: "",
  content: "",
  severity: "INFO",
  pinned: false,
  dismissible: true,
};

export function CreateSystemAnnouncementDrawer({ open, onClose, onSubmit, pending }) {
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
    <CreateSystemAnnouncementDrawerView
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
