import { useState } from "react";
import { deleteDocument, downloadDocumentBlob } from "@/api/documents";
import { getErrorMessage } from "@/api/client";
import { documentTypeLabel, formatFileSize } from "@/utils/documents";

export default function DocumentList({ documents, onDeleted, onError }) {
  const [busyId, setBusyId] = useState(null);
  const [confirmId, setConfirmId] = useState(null);

  async function handleDownload(doc) {
    setBusyId(doc.id);
    onError("");
    try {
      const blob = await downloadDocumentBlob(doc.id);
      const url = URL.createObjectURL(blob);
      const link = window.document.createElement("a");
      link.href = url;
      link.download = doc.originalFilename;
      window.document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      // On a blob request the error body is a Blob, so show a generic message.
      onError(
        err.response
          ? "Could not download this document."
          : getErrorMessage(err)
      );
    } finally {
      setBusyId(null);
    }
  }

  async function handleDelete(doc) {
    setBusyId(doc.id);
    onError("");
    try {
      await deleteDocument(doc.id);
      setConfirmId(null);
      onDeleted(doc.id);
    } catch (err) {
      onError(getErrorMessage(err));
    } finally {
      setBusyId(null);
    }
  }

  if (documents.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center text-sm text-slate-500">
        You haven't uploaded any documents yet.
      </div>
    );
  }

  return (
    <ul className="divide-y divide-slate-200 rounded-xl border border-slate-200 bg-white shadow-sm">
      {documents.map((doc) => {
        const busy = busyId === doc.id;
        return (
          <li
            key={doc.id}
            className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="min-w-0">
              <p className="truncate font-medium text-slate-900">
                {doc.originalFilename}
              </p>
              <p className="mt-1 text-xs text-slate-500">
                <span className="mr-2 rounded-full bg-indigo-50 px-2 py-0.5 font-medium text-indigo-700">
                  {documentTypeLabel(doc.documentType)}
                </span>
                {formatFileSize(doc.sizeBytes)}
                {doc.description ? ` · ${doc.description}` : ""}
              </p>
            </div>

            <div className="flex shrink-0 flex-wrap items-center gap-2">
              {confirmId === doc.id ? (
                <>
                  <span className="text-sm text-slate-600">Delete this file?</span>
                  <button
                    type="button"
                    disabled={busy}
                    onClick={() => handleDelete(doc)}
                    className="rounded-lg bg-red-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
                  >
                    {busy ? "Deleting..." : "Yes, delete"}
                  </button>
                  <button
                    type="button"
                    disabled={busy}
                    onClick={() => setConfirmId(null)}
                    className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                </>
              ) : (
                <>
                  <button
                    type="button"
                    disabled={busy}
                    onClick={() => handleDownload(doc)}
                    className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                  >
                    {busy ? "Working..." : "Download"}
                  </button>
                  <button
                    type="button"
                    disabled={busy}
                    onClick={() => setConfirmId(doc.id)}
                    className="rounded-lg border border-red-200 px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 disabled:opacity-50"
                  >
                    Delete
                  </button>
                </>
              )}
            </div>
          </li>
        );
      })}
    </ul>
  );
}