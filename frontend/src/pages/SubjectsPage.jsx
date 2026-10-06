import { useState } from "react";
import useAsyncData from "@/hooks/useAsyncData";
import { getCourses } from "@/api/courses";
import { createSubject, listSubjects } from "@/api/staffAcademics";
import { getErrorMessage } from "@/api/client";
import Section from "@/components/ui/Section";

const fieldClass =
  "mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm " +
  "focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200";

const EMPTY_FORM = { semesterNumber: "1", code: "", name: "", credits: "4", maxMarks: "100" };

function validate(form) {
  const code = form.code.trim();
  const name = form.name.trim();
  const credits = Number(form.credits);
  const maxMarks = Number(form.maxMarks);
  if (!code) return "Enter a subject code.";
  if (code.length > 20) return "Subject code can be at most 20 characters.";
  if (!name) return "Enter a subject name.";
  if (name.length > 150) return "Subject name can be at most 150 characters.";
  if (!Number.isInteger(credits) || credits < 1 || credits > 10) return "Credits must be between 1 and 10.";
  if (!Number.isInteger(maxMarks) || maxMarks < 1 || maxMarks > 999) return "Maximum marks must be between 1 and 999.";
  return "";
}

export default function SubjectsPage() {
  const [courseId, setCourseId] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const courses = useAsyncData(() => getCourses(), []);
  const courseList = courses.data ?? [];
  const activeId = courseId || (courseList[0] ? String(courseList[0].id) : "");
  const course = courseList.find((c) => String(c.id) === activeId);

  const subjects = useAsyncData(
    () => (activeId ? listSubjects(activeId) : Promise.resolve([])),
    [activeId, refreshKey]
  );
  const subjectRows = [...(subjects.data ?? [])].sort(
    (a, b) => a.semester - b.semester || a.code.localeCompare(b.code)
  );

  function change(name, value) {
    setForm((previous) => ({ ...previous, [name]: value }));
  }

  function changeCourse(value) {
    setCourseId(value);
    setForm((previous) => ({ ...previous, semesterNumber: "1" }));
    setError("");
    setNotice("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setNotice("");
    const problem = validate(form);
    if (problem) {
      setError(problem);
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      const created = await createSubject({
        courseId: Number(activeId),
        semesterNumber: Number(form.semesterNumber),
        code: form.code.trim(),
        name: form.name.trim(),
        credits: Number(form.credits),
        maxMarks: Number(form.maxMarks),
      });
      setNotice(`Added ${created.code} - ${created.name}.`);
      setForm((previous) => ({ ...previous, code: "", name: "" }));
      setRefreshKey((k) => k + 1);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Subjects</h1>
        <p className="mt-1 text-sm text-slate-500">
          Subjects must exist before marks can be entered. HODs can only add subjects to courses
          in their own department.
        </p>
      </div>

      <Section title="Course" loading={courses.loading} error={courses.error}>
        <select
          value={activeId}
          onChange={(e) => changeCourse(e.target.value)}
          className={fieldClass + " max-w-md"}
        >
          {courseList.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name} ({c.departmentName})
            </option>
          ))}
        </select>
      </Section>

      <Section title="Add a subject" loading={false}>
        <form onSubmit={handleSubmit}>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
            <label className="block text-sm font-medium text-slate-700">
              Semester
              <select
                value={form.semesterNumber}
                onChange={(e) => change("semesterNumber", e.target.value)}
                className={fieldClass}
              >
                {Array.from({ length: course?.totalSemesters ?? 8 }, (_, i) => i + 1).map((n) => (
                  <option key={n} value={n}>
                    Semester {n}
                  </option>
                ))}
              </select>
            </label>
            <label className="block text-sm font-medium text-slate-700">
              Code
              <input
                type="text"
                maxLength={20}
                value={form.code}
                onChange={(e) => change("code", e.target.value)}
                className={fieldClass}
              />
            </label>
            <label className="block text-sm font-medium text-slate-700 lg:col-span-2">
              Name
              <input
                type="text"
                maxLength={150}
                value={form.name}
                onChange={(e) => change("name", e.target.value)}
                className={fieldClass}
              />
            </label>
            <div className="grid grid-cols-2 gap-4">
              <label className="block text-sm font-medium text-slate-700">
                Credits
                <input
                  type="number"
                  min="1"
                  max="10"
                  value={form.credits}
                  onChange={(e) => change("credits", e.target.value)}
                  className={fieldClass}
                />
              </label>
              <label className="block text-sm font-medium text-slate-700">
                Max marks
                <input
                  type="number"
                  min="1"
                  max="999"
                  value={form.maxMarks}
                  onChange={(e) => change("maxMarks", e.target.value)}
                  className={fieldClass}
                />
              </label>
            </div>
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
            disabled={submitting || !activeId}
            className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
          >
            {submitting ? "Adding..." : "Add subject"}
          </button>
        </form>
      </Section>

      <Section title="Subjects in this course" loading={subjects.loading} error={subjects.error}>
        {subjectRows.length === 0 ? (
          <p className="text-sm text-slate-500">No subjects yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-xs uppercase text-slate-500">
                <tr>
                  <th className="py-2 pr-4">Sem</th>
                  <th className="py-2 pr-4">Code</th>
                  <th className="py-2 pr-4">Name</th>
                  <th className="py-2 pr-4">Credits</th>
                  <th className="py-2">Max marks</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {subjectRows.map((s) => (
                  <tr key={s.id}>
                    <td className="py-2 pr-4 text-slate-600">{s.semester}</td>
                    <td className="py-2 pr-4 text-slate-600">{s.code}</td>
                    <td className="py-2 pr-4 text-slate-900">{s.name}</td>
                    <td className="py-2 pr-4 text-slate-600">{s.credits}</td>
                    <td className="py-2 text-slate-600">{s.maxMarks}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Section>
    </div>
  );
}