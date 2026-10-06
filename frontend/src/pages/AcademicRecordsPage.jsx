import { useEffect, useState } from "react";
import Spinner from "@/components/ui/Spinner";
import { Link } from "react-router-dom";
import { getErrorMessage } from "@/api/client";
import { getGpa, getMyProfile } from "@/api/students";
import { getRecords } from "@/api/records";
import Alert from "@/components/ui/Alert";
import Badge from "@/components/ui/Badge";
import SelectField from "@/components/ui/SelectField";
import StatCard from "@/components/ui/StatCard";

function formatNumber(value, digits = 2) {
  return value === null || value === undefined
    ? "--"
    : Number(value).toFixed(digits);
}

// -> [[semesterNumber, rows], ...] sorted by semester
function groupBySemester(records) {
  const groups = new Map();
  for (const record of records) {
    if (!groups.has(record.semester)) groups.set(record.semester, []);
    groups.get(record.semester).push(record);
  }
  return [...groups.entries()].sort((a, b) => a[0] - b[0]);
}

const HEADERS = [
  "Code",
  "Subject",
  "Credits",
  "Marks",
  "Percentage",
  "Grade",
  "Points",
  "Result",
];

function SemesterTable({ semester, rows, sgpa }) {
  const credits = rows.reduce((sum, row) => sum + row.credits, 0);

  return (
    <section className="mt-8">
      <h2 className="text-lg font-semibold text-slate-900">
        Semester {semester}
      </h2>
      <div className="mt-3 overflow-x-auto rounded-2xl bg-white shadow">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
            <tr>
              {HEADERS.map((header) => (
                <th key={header} className="px-4 py-3 font-semibold">
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {rows.map((row) => (
              <tr key={row.id}>
                <td className="px-4 py-3 font-medium text-slate-700">
                  {row.subjectCode}
                </td>
                <td className="px-4 py-3 text-slate-900">{row.subjectName}</td>
                <td className="px-4 py-3 text-slate-700">{row.credits}</td>
                <td className="px-4 py-3 text-slate-700">
                  {Number(row.marksObtained)} / {row.maxMarks}
                </td>
                <td className="px-4 py-3 text-slate-700">
                  {formatNumber(row.percentage)}%
                </td>
                <td className="px-4 py-3 font-semibold text-slate-900">
                  {row.grade}
                </td>
                <td className="px-4 py-3 text-slate-700">{row.gradePoints}</td>
                <td className="px-4 py-3">
                  <Badge tone={row.pass ? "success" : "danger"}>
                    {row.pass ? "Pass" : "Fail"}
                  </Badge>
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot className="bg-slate-50 text-sm font-semibold text-slate-900">
            <tr>
              <td className="px-4 py-3" colSpan={2}>
                Semester total
              </td>
              <td className="px-4 py-3">{credits}</td>
              <td className="px-4 py-3" colSpan={5}>
                SGPA {formatNumber(sgpa)}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>
    </section>
  );
}

export default function AcademicRecordsPage() {
  const [state, setState] = useState({ status: "loading" });
  const [semesterFilter, setSemesterFilter] = useState("all");

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const profile = await getMyProfile();
        if (!profile) {
          if (!cancelled) setState({ status: "no-profile" });
          return;
        }
        const [records, gpa] = await Promise.all([
          getRecords(profile.id),
          getGpa(profile.id),
        ]);
        if (!cancelled) setState({ status: "ready", records, gpa });
      } catch (error) {
        if (!cancelled) {
          setState({ status: "error", message: getErrorMessage(error) });
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  if (state.status === "loading") {
    return <Spinner label="Loading your results..." />;
  }

  if (state.status === "error") {
    return <Alert>{state.message}</Alert>;
  }

  if (state.status === "no-profile") {
    return (
      <div className="mx-auto max-w-lg rounded-2xl bg-white p-8 text-center shadow">
        <h1 className="text-2xl font-bold text-slate-900">
          Complete your profile first
        </h1>
        <p className="mt-2 text-slate-600">
          Your results are linked to your student profile.
        </p>
        <Link
          to="/profile"
          className="mt-6 inline-block rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white hover:bg-indigo-700"
        >
          Go to my profile
        </Link>
      </div>
    );
  }

  const { records, gpa } = state;
  const groups = groupBySemester(records);
  const visibleGroups =
    semesterFilter === "all"
      ? groups
      : groups.filter(([semester]) => String(semester) === semesterFilter);

  const totalCredits = gpa.semesters.reduce((sum, item) => sum + item.credits, 0);
  const failed = records.filter((record) => !record.pass).length;
  const sgpaBySemester = new Map(
    gpa.semesters.map((item) => [item.semester, item.sgpa])
  );

  return (
    <div>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h1 className="text-2xl font-bold text-slate-900">Academic Records</h1>
        {groups.length > 1 && (
          <div className="w-48">
            <SelectField
              id="semesterFilter"
              label="Show"
              value={semesterFilter}
              onChange={(event) => setSemesterFilter(event.target.value)}
            >
              <option value="all">All semesters</option>
              {groups.map(([semester]) => (
                <option key={semester} value={semester}>
                  Semester {semester}
                </option>
              ))}
            </SelectField>
          </div>
        )}
      </div>

      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="CGPA"
          value={groups.length ? formatNumber(gpa.cgpa) : "--"}
        />
        <StatCard label="Total credits" value={totalCredits} />
        <StatCard label="Subjects" value={records.length} />
        <StatCard
          label="Subjects failed"
          value={failed}
          hint={failed === 0 ? "None" : "Needs attention"}
        />
      </div>

      {groups.length === 0 ? (
        <div className="mt-8 rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500">
          No results have been entered for you yet.
        </div>
      ) : (
        visibleGroups.map(([semester, rows]) => (
          <SemesterTable
            key={semester}
            semester={semester}
            rows={rows}
            sgpa={sgpaBySemester.get(semester)}
          />
        ))
      )}
    </div>
  );
}
