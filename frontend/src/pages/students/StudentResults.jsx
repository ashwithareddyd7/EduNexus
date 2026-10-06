import Badge from "@/components/ui/Badge";
import { formatNumber } from "@/utils/format";

function groupBySemester(records) {
  const groups = new Map();
  for (const record of records) {
    if (!groups.has(record.semester)) groups.set(record.semester, []);
    groups.get(record.semester).push(record);
  }
  return [...groups.entries()].sort((a, b) => a[0] - b[0]);
}

export default function StudentResults({ records, gpa, subjects, onEdit }) {
  if (records.length === 0) {
    return <p className="text-sm text-slate-500">No marks have been entered for this student yet.</p>;
  }
  const sgpa = new Map((gpa?.semesters ?? []).map((s) => [s.semester, s.sgpa]));

  return (
    <div className="space-y-6">
      {groupBySemester(records).map(([semester, rows]) => (
        <div key={semester}>
          <h3 className="font-semibold text-slate-900">
            Semester {semester}
            <span className="ml-3 text-sm font-medium text-slate-500">
              SGPA {formatNumber(sgpa.get(semester))}
            </span>
          </h3>
          <div className="mt-2 overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="text-xs uppercase text-slate-500">
                <tr>
                  <th className="py-2 pr-4">Code</th>
                  <th className="py-2 pr-4">Subject</th>
                  <th className="py-2 pr-4">Credits</th>
                  <th className="py-2 pr-4">Marks</th>
                  <th className="py-2 pr-4">%</th>
                  <th className="py-2 pr-4">Grade</th>
                  <th className="py-2 pr-4">Result</th>
                  <th className="py-2 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {rows.map((r) => {
                  const subject = subjects.find((s) => s.code === r.subjectCode);
                  return (
                    <tr key={r.id}>
                      <td className="py-2 pr-4 text-slate-600">{r.subjectCode}</td>
                      <td className="py-2 pr-4 text-slate-900">{r.subjectName}</td>
                      <td className="py-2 pr-4 text-slate-600">{r.credits}</td>
                      <td className="py-2 pr-4 text-slate-900">
                        {Number(r.marksObtained)} / {r.maxMarks}
                      </td>
                      <td className="py-2 pr-4 text-slate-600">{formatNumber(r.percentage)}%</td>
                      <td className="py-2 pr-4 font-semibold text-slate-900">{r.grade}</td>
                      <td className="py-2 pr-4">
                        <Badge tone={r.pass ? "success" : "danger"}>{r.pass ? "Pass" : "Fail"}</Badge>
                      </td>
                      <td className="py-2 text-right">
                        <button
                          type="button"
                          disabled={!subject}
                          onClick={() =>
                            onEdit({ subjectId: subject.id, marks: r.marksObtained })
                          }
                          className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                        >
                          Edit
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </div>
  );
}