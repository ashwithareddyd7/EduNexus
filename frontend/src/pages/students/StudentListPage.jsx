import { useState } from "react";
import useAuth from "@/context/useAuth";
import useAsyncData from "@/hooks/useAsyncData";
import { listStudents, setStudentStatus } from "@/api/studentAdmin";
import { getErrorMessage } from "@/api/client";
import Badge from "@/components/ui/Badge";
import Pagination from "@/components/ui/Pagination";

const PAGE_SIZE = 10;

function StatusBadge({ enabled }) {
  if (typeof enabled !== "boolean") return <span className="text-slate-400">-</span>;
  return enabled ? (
    <Badge tone="success">Active</Badge>
  ) : (
    <Badge tone="danger">Disabled</Badge>
  );
}

export default function StudentListPage({ title, manageStatus = false }) {
  const { user } = useAuth();
  const isAdmin = user.role === "ADMIN";

  const [page, setPage] = useState(0);
  const [statusById, setStatusById] = useState({}); // latest status after a change
  const [busyId, setBusyId] = useState(null);
  const [confirmId, setConfirmId] = useState(null);
  const [actionError, setActionError] = useState("");
  const [notice, setNotice] = useState("");

  const students = useAsyncData(() => listStudents({ page, size: PAGE_SIZE }), [page]);

  const rows = students.data?.content ?? [];
  // Works whether the backend sends a flat Page or a nested "page" object.
  const totalPages = students.data?.totalPages ?? students.data?.page?.totalPages ?? 1;
  const totalElements =
    students.data?.totalElements ?? students.data?.page?.totalElements ?? rows.length;

  async function changeStatus(student, enabled) {
    setBusyId(student.id);
    setActionError("");
    setNotice("");
    try {
      const result = await setStudentStatus(student.id, enabled);
      setStatusById((current) => ({ ...current, [student.id]: result.enabled }));
      setConfirmId(null);
      setNotice(
        `${student.fullName}'s account is now ${result.enabled ? "enabled" : "disabled"}.`
      );
    } catch (err) {
      setActionError(getErrorMessage(err));
    } finally {
      setBusyId(null);
    }
  }

  const subtitle = manageStatus
    ? "Disable an account to block that student from logging in. No data is deleted."
    : isAdmin
      ? "All students across departments."
      : "Students in your department.";

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
        <p className="mt-1 text-sm text-slate-500">{subtitle}</p>
      </div>

      {(students.error || actionError) && (
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {students.error || actionError}
        </p>
      )}
      {notice && (
        <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">{notice}</p>
      )}

      <div className="rounded-2xl bg-white p-6 shadow">
        {students.loading ? (
          <p className="text-sm text-slate-500">Loading students...</p>
        ) : rows.length === 0 ? (
          <p className="text-sm text-slate-500">No students found.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-xs uppercase text-slate-500">
                <tr>
                  <th className="py-2 pr-4">Roll no.</th>
                  <th className="py-2 pr-4">Student</th>
                  <th className="py-2 pr-4">Course</th>
                  <th className="py-2 pr-4">Year / Sem</th>
                  <th className="py-2 pr-4">Phone</th>
                  <th className="py-2 pr-4">Status</th>
                  {manageStatus && <th className="py-2 text-right">Actions</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {rows.map((s) => {
                  const enabled = statusById[s.id] ?? s.enabled;
                  const busy = busyId === s.id;
                  return (
                    <tr key={s.id}>
                      <td className="py-2 pr-4 text-slate-600">{s.studentId}</td>
                      <td className="py-2 pr-4">
                        <p className="font-medium text-slate-900">{s.fullName}</p>
                        <p className="text-xs text-slate-500">{s.email}</p>
                      </td>
                      <td className="py-2 pr-4">
                        <p className="text-slate-900">{s.courseName}</p>
                        {isAdmin && (
                          <p className="text-xs text-slate-500">{s.departmentName}</p>
                        )}
                      </td>
                      <td className="py-2 pr-4 text-slate-600">
                        {s.year} / {s.currentSemester}
                      </td>
                      <td className="py-2 pr-4 text-slate-600">{s.phone || "-"}</td>
                      <td className="py-2 pr-4">
                        <StatusBadge enabled={enabled} />
                      </td>
                      {manageStatus && (
                        <td className="py-2">
                          <div className="flex items-center justify-end gap-2">
                            {confirmId === s.id ? (
                              <>
                                <span className="text-slate-600">Disable this account?</span>
                                <button
                                  type="button"
                                  disabled={busy}
                                  onClick={() => changeStatus(s, false)}
                                  className="rounded-lg bg-red-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
                                >
                                  {busy ? "Disabling..." : "Yes, disable"}
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
                            ) : enabled === false ? (
                              <button
                                type="button"
                                disabled={busy}
                                onClick={() => changeStatus(s, true)}
                                className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                              >
                                {busy ? "Enabling..." : "Enable"}
                              </button>
                            ) : (
                              <button
                                type="button"
                                disabled={busy || typeof enabled !== "boolean"}
                                onClick={() => setConfirmId(s.id)}
                                className="rounded-lg border border-red-200 px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 disabled:opacity-50"
                              >
                                Disable
                              </button>
                            )}
                          </div>
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          page={page}
          totalPages={totalPages}
          totalElements={totalElements}
          noun="student"
          loading={students.loading}
          onChange={setPage}
        />
      </div>
    </div>
  );
}