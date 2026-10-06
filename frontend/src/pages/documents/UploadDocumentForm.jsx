import { useRef, useState } from "react";
import { uploadDocument } from "@/api/documents";
import { getErrorMessage } from "@/api/client";
import {
  ACCEPT_ATTR,
  DOCUMENT_TYPES,
  formatFileSize,
  validateFile,
} from "@/utils/documents";

const fieldClass =
  "mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm " +
  "focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200";

export default function UploadDocumentForm({ onUploaded }) {
  const [type, setType] = useState(DOCUMENT_TYPES[0].value);
  const [description, setDescription] = useState("");
  const [file, setFile] = useState(null);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const fileInputRef = useRef(null);

  function handleFileChange(event) {
    const chosen = event.target.files?.[0] ?? null;
    setError(chosen ? validateFile(chosen) ?? "" : "");
    setFile(chosen);
  }

  function reset() {
    setFile(null);
    setDescription("");
    setType(DOCUMENT_TYPES[0].value);
    if (fileInputRef.current) fileInputRef.current.value = "";
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const problem = validateFile(file);
    if (problem) {
      setError(problem);
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      const created = await uploadDocument({ file, type, description });
      onUploaded(created);
      reset();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
    >
      <h2 className="text-lg font-semibold text-slate-900">Upload a document</h2>
      <p className="mt-1 text-sm text-slate-500">
        PDF, JPG or PNG, up to 5 MB.
      </p>

      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <label className="block text-sm font-medium text-slate-700">
          Document type
          <select
            value={type}
            onChange={(e) => setType(e.target.value)}
            className={fieldClass}
          >
            {DOCUMENT_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Description (optional)
          <input
            type="text"
            value={description}
            maxLength={255}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g. Semester 3 marks memo"
            className={fieldClass}
          />
        </label>
      </div>

      <label className="mt-4 block text-sm font-medium text-slate-700">
        File
        <input
          ref={fileInputRef}
          type="file"
          accept={ACCEPT_ATTR}
          onChange={handleFileChange}
          className="mt-1 block w-full text-sm text-slate-600 file:mr-4 file:rounded-lg
                     file:border-0 file:bg-indigo-50 file:px-4 file:py-2 file:text-sm
                     file:font-medium file:text-indigo-700 hover:file:bg-indigo-100"
        />
      </label>
      {file && !error && (
        <p className="mt-1 text-xs text-slate-500">
          {file.name} ({formatFileSize(file.size)})
        </p>
      )}

      {error && (
        <p role="alert" className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={submitting || !file}
        className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white
                   hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {submitting ? "Uploading..." : "Upload"}
      </button>
    </form>
  );
}
