import { useState } from "react";
import { updateMyProfile } from "@/api/students";
import { getErrorMessage } from "@/api/client";
import Alert from "@/components/ui/Alert";
import Button from "@/components/ui/Button";
import TextField from "@/components/ui/TextField";

export default function EditProfileForm({ profile, onSaved, onCancel }) {
  const [form, setForm] = useState({
    fullName: profile.fullName ?? "",
    phone: profile.phone ?? "",
  });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((previous) => ({ ...previous, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.fullName.trim()) {
      setError("Please enter your full name.");
      return;
    }
    if (form.phone.trim().length > 20) {
      setError("Phone can be at most 20 characters.");
      return;
    }

    setError("");
    setSubmitting(true);
    try {
      await updateMyProfile({
        fullName: form.fullName.trim(),
        phone: form.phone.trim() || null,
      });
      await onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {error && <Alert>{error}</Alert>}

      <TextField
        id="fullName"
        label="Full name"
        type="text"
        autoComplete="name"
        required
        maxLength={100}
        value={form.fullName}
        onChange={handleChange}
      />
      <TextField
        id="phone"
        label="Phone"
        type="tel"
        autoComplete="tel"
        maxLength={20}
        value={form.phone}
        onChange={handleChange}
      />

      <div className="flex gap-3">
        <Button type="submit" disabled={submitting}>
          {submitting ? "Saving..." : "Save changes"}
        </Button>
        <Button
          variant="secondary"
          disabled={submitting}
          onClick={onCancel}
        >
          Cancel
        </Button>
      </div>
    </form>
  );
}
