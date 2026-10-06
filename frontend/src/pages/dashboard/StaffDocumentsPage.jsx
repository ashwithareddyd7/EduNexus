import { useState } from "react";
import useAuth from "@/context/useAuth";
import useAsyncData from "@/hooks/useAsyncData";
import { fetchStaffDocuments } from "@/api/staffDocuments";
import { fetchDepartmentStats } from "@/api/stats";
import { deleteDocument, downloadDocumentBlob } from "@/api/documents";
import { getErrorMessage } from "@/api/client";
import { DOCUMENT_TYPES, documentTypeLabel, formatFileSize } from "@/utils/documents";
import saveBlob from "@/utils/saveBlob";

const PAGE_SIZE = 10;

const selectClass =
  "mt-1 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm " +
  "focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200";

export default function StaffDocumentsPage() {
  const { user } = useAuth();
  const isAdmin = user.role === "ADMIN";

  const [departmentId, setDepartmentId] = useState("");
  const [type, setType] = useState("");
  const [page, setPage] = useState(0);
  const [refreshKey, setRefreshKey] = useState(0);
  const [busyId, setBusyId] = useState(null);
  const [confirmId, setConfirmId] = useState(null);
  const [actionError, setActionError] = useState("");

  const departments = useAsyncData(
    () => (isAdmin ? fetchDepartmentStats() : Promise.resolve([])),
    [isAdmin]
  );
  const docs = useAsyncData(
    () => fetchStaffDocuments({ departmentId, type, page, size: PAGE_SIZE }),
    [departmentId, type, page, refreshKey]
  );

  const rows = docs.data?.content ?? [];
  // Works whether the backend sends a flat Page or a nested "page" object.
  const totalPages = docs.data?.totalPages ?? docs.data?.page?.totalPages ?? 1;
  const totalElements = docs.data?.totalElements ?? docs.data?.page?.totalElements ?? rows.length;

  async function handleDownload(doc) {
    setBusyId(doc.id);
    setActionError("");
    try {
      saveBlob(await downloadDocumentBlob(doc.id), doc.originalFilename);
    } catch (err) {
      setActionError(
        err.response ? "Could not download this document." : getErrorMessage(err)
      );
    } finally {
      setBusyId(null);
    }
  }

  async function handleDelete(doc) {
    setBusyId(doc.id);
    setActionError("");
    try {
      await deleteDocument(doc.id);
      setConfirmId(null);
      if (rows.length === 1 && page > 0) setPage(page - 1);
      else setRefreshKey((k) => k + 1);
    } catch (err) {
      setActionError(getErrorMessage(err));
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Staff Documents</h1>
        <p className="mt-1 text-sm text-slate-500">
          {isAdmin
            ? "Documents uploaded by students. Admins can download and delete."
            : "Documents uploaded by students in your department."}
        </p>
      </div>

      <div className="flex flex-wrap gap-4">
        {isAdmin && (
          <label className="block text-sm font-medium text-slate-700">
            Department
            <select
              value={departmentId}
              onChange={(e) => {
                setDepartmentId(e.target.value);
                setPage(0);
              }}
              className={selectClass}
            >
              <option value="">All departments</option>
              {(departments.data ?? []).map((d) => (
                <option key={d.departmentId} value={d.departmentId}>
                  {d.departmentName}
                </option>
              ))}
            </select>
          </label>
        )}
        <label className="block text-sm font-medium text-slate-700">
          Type
          <select
            value={type}
            onChange={(e) => {
              setType(e.target.value);
              setPage(0);
            }}
            className={selectClass}
          >
            <option value="">All types</option>
            {DOCUMENT_TYPES.map((t) => (
              <option key={t.value} value={t.value}>
                {t.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {(docs.error || actionError) && (
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {docs.error || actionError}
        </p>
      )}

      <div className="rounded-2xl bg-white p-6 shadow">
        {docs.loading ? (
          <p className="text-sm text-slate-500">Loading documents...</p>
        ) : rows.length === 0 ? (
          <p className="text-sm text-slate-500">No documents found.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-xs uppercase text-slate-500">
                <tr>
                  <th className="py-2 pr-4">Student</th>
                  <th className="py-2 pr-4">Course</th>
                  <th className="py-2 pr-4">Type</th>
                  <th className="py-2 pr-4">File</th>
                  <th className="py-2 pr-4">Size</th>
                  <th className="py-2 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {rows.map((d) => {
                  const busy = busyId === d.id;
                  return (
                    <tr key={d.id}>
                      <td className="py-2 pr-4">
                        <p className="font-medium text-slate-900">{d.studentName}</p>
                        <p className="text-xs text-slate-500">{d.rollNumber}</p>
                      </td>
                      <td className="py-2 pr-4 text-slate-600">{d.courseName}</td>
                      <td className="py-2 pr-4 text-slate-600">{documentTypeLabel(d.documentType)}</td>
                      <td className="py-2 pr-4">
                        <p className="text-slate-900">{d.originalFilename}</p>
                        {d.description && (
                          <p className="text-xs text-slate-500">{d.description}</p>
                        )}
                      </td>
                      <td className="py-2 pr-4 text-slate-600">{formatFileSize(d.sizeBytes)}</td>
                      <td className="py-2">
                        <div className="flex items-center justify-end gap-2">
                          {confirmId === d.id ? (
                            <>
                              <button
                                type="button"
                                disabled={busy}
                                onClick={() => handleDelete(d)}
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
                                onClick={() => handleDownload(d)}
                                className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                              >
                                {busy ? "Working..." : "Download"}
                              </button>
                              {isAdmin && (
                                <button
                                  type="button"
                                  disabled={busy}
                                  onClick={() => setConfirmId(d.id)}
                                  className="rounded-lg border border-red-200 px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 disabled:opacity-50"
                                >
                                  Delete
                                </button>
                              )}
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        <div className="mt-4 flex items-center justify-between text-sm text-slate-600">
          <span>
            {totalElements} document{totalElements === 1 ? "" : "s"}
          </span>
          <div className="flex items-center gap-2">
            <button
              type="button"
              disabled={page === 0 || docs.loading}
              onClick={() => setPage(page - 1)}
              className="rounded-lg border border-slate-300 px-3 py-1.5 hover:bg-slate-50 disabled:opacity-50"
            >
              Previous
            </button>
            <span>
              Page {page + 1} of {Math.max(totalPages, 1)}
            </span>
            <button
              type="button"
              disabled={page + 1 >= totalPages || docs.loading}
              onClick={() => setPage(page + 1)}
              className="rounded-lg border border-slate-300 px-3 py-1.5 hover:bg-slate-50 disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}