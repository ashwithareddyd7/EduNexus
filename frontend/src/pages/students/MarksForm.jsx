import { useState } from "react";
import { Link } from "react-router-dom";
import { saveMarks } from "@/api/staffAcademics";
import { getErrorMessage } from "@/api/client";

const fieldClass =
  "mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm " +
  "focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200";

// subjects: [{ id, code, name, maxMarks, semester }]; editing: { subjectId, marks } or null.
// The parent changes the "key" to reset this form when Edit is clicked on a result row.
export default function MarksForm({ studentId, subjects, editing, onSaved }) {
  const [subjectId, setSubjectId] = useState(editing ? String(editing.subjectId) : "");
  const [marks, setMarks] = useState(editing ? String(Number(editing.marks)) : "");
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (subjects.length === 0) {
    return (
      <p className="text-sm text-slate-600">
        This course has no subjects yet.{" "}
        <Link to="/subjects" className="font-medium text-indigo-600 hover:underline">
          Add subjects first
        </Link>
        .
      </p>
    );
  }

  const selected = subjects.find((s) => String(s.id) === subjectId);
  const semesters = [...new Set(subjects.map((s) => s.semester))].sort((a, b) => a - b);

  async function handleSubmit(event) {
    event.preventDefault();
    setNotice("");
    if (!selected) {
      setError("Choose a subject.");
      return;
    }
    if (!/^\d{1,3}(\.\d{1,2})?$/.test(marks.trim())) {
      setError("Enter marks as a number with at most 2 decimals.");
      return;
    }
    const value = Number(marks);
    if (value > selected.maxMarks) {
      setError(`Marks cannot exceed ${selected.maxMarks} for this subject.`);
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      const result = await saveMarks({ studentId, subjectId: selected.id, marks: value });
      setNotice(
        `Saved ${result.subjectCode}: ${Number(result.marksObtained)} / ${selected.maxMarks}, ` +
          `grade ${result.grade} (${result.pass ? "pass" : "fail"}).`
      );
      setMarks("");
      onSaved();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="grid gap-4 sm:grid-cols-3">
        <label className="block text-sm font-medium text-slate-700 sm:col-span-2">
          Subject
          <select
            value={subjectId}
            onChange={(e) => setSubjectId(e.target.value)}
            className={fieldClass}
          >
            <option value="">Select a subject</option>
            {semesters.map((sem) => (
              <optgroup key={sem} label={`Semester ${sem}`}>
                {subjects
                  .filter((s) => s.semester === sem)
                  .sort((a, b) => a.code.localeCompare(b.code))
                  .map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.code} - {s.name} (max {s.maxMarks})
                    </option>
                  ))}
              </optgroup>
            ))}
          </select>
        </label>

        <label className="block text-sm font-medium text-slate-700">
          Marks{selected ? ` (out of ${selected.maxMarks})` : ""}
          <input
            type="text"
            inputMode="decimal"
            value={marks}
            onChange={(e) => setMarks(e.target.value)}
            className={fieldClass}
          />
        </label>
      </div>

      {error && (
        <p role="alert" className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </p>
      )}
      {notice && (
        <p className="mt-3 rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">{notice}</p>
      )}

      <button
        type="submit"
        disabled={submitting}
        className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
      >
        {submitting ? "Saving..." : "Save marks"}
      </button>
      <p className="mt-2 text-xs text-slate-500">
        Saving marks for a subject that already has marks overwrites them.
      </p>
    </form>
  );
}