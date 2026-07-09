import { useState } from "react";
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

  const handleSubmit = async (event) => {
    event.preventDefault();
    await onSubmit?.(form);
    setForm(defaultForm);
  };

  return (
    <CreateSystemAnnouncementDrawerView
      open={open}
      form={form}
      pending={pending}
      onFieldChange={(patch) => setForm((prev) => ({ ...prev, ...patch }))}
      onClose={onClose}
      onSubmit={handleSubmit}
    />
  );
}
