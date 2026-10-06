import { formatNumber } from "@/utils/format";

export default function DepartmentsTable({ rows }) {
  if (rows.length === 0) {
    return <p className="text-sm text-slate-500">No departments yet.</p>;
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead className="text-xs uppercase text-slate-500">
          <tr>
            <th className="py-2 pr-4">Department</th>
            <th className="py-2 pr-4">Students</th>
            <th className="py-2 pr-4">With marks</th>
            <th className="py-2 pr-4">Average CGPA</th>
            <th className="py-2">Pass rate</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {rows.map((d) => (
            <tr key={d.departmentId}>
              <td className="py-2 pr-4 font-medium text-slate-900">{d.departmentName}</td>
              <td className="py-2 pr-4 text-slate-600">{d.students}</td>
              <td className="py-2 pr-4 text-slate-600">{d.studentsWithMarks}</td>
              <td className="py-2 pr-4 text-slate-900">{formatNumber(d.averageCgpa)}</td>
              <td className="py-2 text-slate-900">{formatNumber(d.passRate, 1)}%</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}