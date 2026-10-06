import Badge from "@/components/ui/Badge";
import { formatNumber } from "@/utils/format";

function passTone(rate) {
  const n = Number(rate);
  if (n >= 75) return "success";
  if (n < 50) return "danger";
  return "neutral";
}

export default function SubjectAveragesTable({ rows }) {
  if (rows.length === 0) {
    return <p className="text-sm text-slate-500">No marks have been entered yet.</p>;
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead className="text-xs uppercase text-slate-500">
          <tr>
            <th className="py-2 pr-4">Code</th>
            <th className="py-2 pr-4">Subject</th>
            <th className="py-2 pr-4">Sem</th>
            <th className="py-2 pr-4">Students</th>
            <th className="py-2 pr-4">Average</th>
            <th className="py-2">Pass rate</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {rows.map((r) => (
            <tr key={`${r.semester}-${r.subjectCode}`}>
              <td className="py-2 pr-4 text-slate-600">{r.subjectCode}</td>
              <td className="py-2 pr-4 text-slate-900">{r.subjectName}</td>
              <td className="py-2 pr-4 text-slate-600">{r.semester}</td>
              <td className="py-2 pr-4 text-slate-600">{r.students}</td>
              <td className="py-2 pr-4 font-medium text-slate-900">
                {formatNumber(r.averagePercentage)}%
              </td>
              <td className="py-2">
                <Badge tone={passTone(r.passRate)}>{formatNumber(r.passRate, 1)}%</Badge>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}