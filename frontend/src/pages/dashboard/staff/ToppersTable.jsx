import Badge from "@/components/ui/Badge";
import { formatNumber } from "@/utils/format";

export default function ToppersTable({ rows, gpaLabel }) {
  if (rows.length === 0) {
    return <p className="text-sm text-slate-500">No marks have been entered yet.</p>;
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead className="text-xs uppercase text-slate-500">
          <tr>
            <th className="py-2 pr-4">Rank</th>
            <th className="py-2 pr-4">Roll no.</th>
            <th className="py-2 pr-4">Name</th>
            <th className="py-2 pr-4">Course</th>
            <th className="py-2 pr-4">{gpaLabel}</th>
            <th className="py-2">Backlogs</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {rows.map((r) => (
            <tr key={r.studentProfileId}>
              <td className="py-2 pr-4 font-semibold text-slate-900">{r.rank}</td>
              <td className="py-2 pr-4 text-slate-600">{r.rollNumber}</td>
              <td className="py-2 pr-4 text-slate-900">{r.fullName}</td>
              <td className="py-2 pr-4 text-slate-600">{r.courseName}</td>
              <td className="py-2 pr-4 font-medium text-slate-900">{formatNumber(r.gpa)}</td>
              <td className="py-2">
                {r.failedSubjects > 0 ? (
                  <Badge tone="danger">{r.failedSubjects} failed</Badge>
                ) : (
                  <Badge tone="success">Clear</Badge>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}