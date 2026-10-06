import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import useAsyncData from "@/hooks/useAsyncData";
import { getErrorMessage } from "@/api/client";
import { getGpa } from "@/api/students";
import { getRecords } from "@/api/records";
import { downloadDocumentBlob } from "@/api/documents";
import { getStudentById, getStudentDocuments, listSubjects } from "@/api/staffAcademics";
import { documentTypeLabel, formatFileSize } from "@/utils/documents";
import { formatNumber } from "@/utils/format";
import saveBlob from "@/utils/saveBlob";
import Badge from "@/components/ui/Badge";
import Section from "@/components/ui/Section";
import StatCard from "@/components/ui/StatCard";
import MarksForm from "@/pages/students/MarksForm";
import StudentResults from "@/pages/students/StudentResults";

function BackLink() {
  return (
    <Link to="/students" className="text-sm font-medium text-indigo-600 hover:underline">
      &larr; Back to students
    </Link>
  );
}

export default function StudentDetailPage() {
  const { id } = useParams();
  const [refreshKey, setRefreshKey] = useState(0);
  const [editing, setEditing] = useState(null);
  const [busyDocId, setBusyDocId] = useState(null);
  const [docError, setDocError] = useState("");

  const profile = useAsyncData(() => getStudentById(id), [id]);
  const records = useAsyncData(() => getRecords(id), [id, refreshKey]);
  const gpa = useAsyncData(() => getGpa(id), [id, refreshKey]);
  const docs = useAsyncData(() => getStudentDocuments(id), [id]);

  const courseId = profile.data?.courseId;
  const subjects = useAsyncData(
    () => (courseId ? listSubjects(courseId) : Promise.resolve([])),
    [courseId]
  );

  if (profile.error) {
    return (
      <div className="space-y-4">
        <BackLink />
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {profile.error}
        </p>
      </div>
    );
  }
  if (profile.loading || !profile.data) {
    return <p className="text-sm text-slate-500">Loading student...</p>;
  }

  const p = profile.data;
  const recordRows = records.data ?? [];
  const semesters = gpa.data?.semesters ?? [];
  const totalCredits = semesters.reduce((sum, s) => sum + s.credits, 0);
  const failed = recordRows.filter((r) => !r.pass).length;

  function handleEdit(target) {
    setEditing((previous) => ({ ...target, nonce: (previous?.nonce ?? 0) + 1 }));
    window.document.getElementById("marks-form")?.scrollIntoView({ behavior: "smooth" });
  }

  async function handleDownload(doc) {
    setBusyDocId(doc.id);
    setDocError("");
    try {
      saveBlob(await downloadDocumentBlob(doc.id), doc.originalFilename);
    } catch (err) {
      setDocError(err.response ? "Could not download this document." : getErrorMessage(err));
    } finally {
      setBusyDocId(null);
    }
  }

  return (
    <div className="space-y-6">
      <BackLink />

      <div className="rounded-2xl bg-white p-6 shadow">
        <div className="flex flex-wrap items-center gap-3">
          <h1 className="text-2xl font-bold text-slate-900">{p.fullName}</h1>
          {p.enabled === false ? (
            <Badge tone="danger">Disabled</Badge>
          ) : (
            <Badge tone="success">Active</Badge>
          )}
        </div>
        <p className="mt-1 text-slate-600">
          {p.studentId} &middot; {p.courseName} &middot; {p.departmentName} &middot; Semester{" "}
          {p.currentSemester} &middot; Year {p.year}
        </p>
        <p className="mt-1 text-sm text-slate-500">
          {p.email}
          {p.phone ? ` · ${p.phone}` : ""}
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="CGPA" value={semesters.length ? formatNumber(gpa.data.cgpa) : "-"} />
        <StatCard label="Total credits" value={totalCredits} />
        <StatCard label="Subjects" value={recordRows.length} />
        <StatCard label="Subjects failed" value={failed} />
      </div>

      <div id="marks-form">
        <Section
          title="Enter or update marks"
          loading={subjects.loading}
          error={subjects.error}
        >
          <MarksForm
            key={editing ? editing.nonce : "new"}
            studentId={p.id}
            subjects={subjects.data ?? []}
            editing={editing}
            onSaved={() => setRefreshKey((k) => k + 1)}
          />
        </Section>
      </div>

      <Section title="Results" loading={records.loading || gpa.loading} error={records.error || gpa.error}>
        <StudentResults
          records={recordRows}
          gpa={gpa.data}
          subjects={subjects.data ?? []}
          onEdit={handleEdit}
        />
      </Section>

      <Section title="Documents" loading={docs.loading} error={docs.error}>
        {docError && (
          <p role="alert" className="mb-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {docError}
          </p>
        )}
        {(docs.data ?? []).length === 0 ? (
          <p className="text-sm text-slate-500">This student has not uploaded any documents.</p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {docs.data.map((d) => (
              <li key={d.id} className="flex items-center justify-between gap-4 py-3">
                <div className="min-w-0">
                  <p className="truncate font-medium text-slate-900">{d.originalFilename}</p>
                  <p className="text-xs text-slate-500">
                    {documentTypeLabel(d.documentType)} &middot; {formatFileSize(d.sizeBytes)}
                    {d.description ? ` · ${d.description}` : ""}
                  </p>
                </div>
                <button
                  type="button"
                  disabled={busyDocId === d.id}
                  onClick={() => handleDownload(d)}
                  className="shrink-0 rounded-lg border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                >
                  {busyDocId === d.id ? "Working..." : "Download"}
                </button>
              </li>
            ))}
          </ul>
        )}
      </Section>
    </div>
  );
}