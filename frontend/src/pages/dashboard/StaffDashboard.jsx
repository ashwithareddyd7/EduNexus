import { useState } from "react";
import useAuth from "@/context/useAuth";
import useAsyncData from "@/hooks/useAsyncData";
import { ROLE_LABELS } from "@/config/navigation";
import {
  fetchAverageMarks,
  fetchDepartmentStats,
  fetchOverview,
  fetchSemesterPerformance,
  fetchToppers,
} from "@/api/stats";
import StatCard from "@/components/ui/StatCard";
import BarChart from "@/components/ui/BarChart";
import Section from "@/components/ui/Section";
import ToppersTable from "@/pages/dashboard/staff/ToppersTable";
import SubjectAveragesTable from "@/pages/dashboard/staff/SubjectAveragesTable";
import DepartmentsTable from "@/pages/dashboard/staff/DepartmentsTable";
import { formatNumber } from "@/utils/format";

const selectClass =
  "mt-1 max-w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm " +
  "focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-200";

function Filter({ label, value, onChange, children }) {
  return (
    <label className="block text-sm font-medium text-slate-700">
      {label}
      <select value={value} onChange={(e) => onChange(e.target.value)} className={selectClass}>
        {children}
      </select>
    </label>
  );
}

export default function StaffDashboard() {
  const { user } = useAuth();
  const isAdmin = user.role === "ADMIN";

  // A HOD is always limited to their own department by the backend, so only admins pick one.
  const [departmentId, setDepartmentId] = useState("");
  const [courseId, setCourseId] = useState("");
  const [semester, setSemester] = useState("");

  const departments = useAsyncData(
    () => (isAdmin ? fetchDepartmentStats() : Promise.resolve([])),
    [isAdmin]
  );
  const overview = useAsyncData(() => fetchOverview(departmentId), [departmentId]);
  const performance = useAsyncData(
    () => fetchSemesterPerformance({ departmentId, courseId }),
    [departmentId, courseId]
  );
  const toppers = useAsyncData(
    () => fetchToppers({ departmentId, courseId, semester }),
    [departmentId, courseId, semester]
  );
  const averages = useAsyncData(
    () => fetchAverageMarks({ departmentId, courseId, semester }),
    [departmentId, courseId, semester]
  );

  function changeDepartment(value) {
    setDepartmentId(value);
    setCourseId("");
    setSemester("");
  }

  function changeCourse(value) {
    setCourseId(value);
    setSemester("");
  }

  const courseOptions = overview.data?.studentsPerCourse ?? [];
  const semesterRows = performance.data ?? [];

  const perCourse = courseOptions.map((c) => ({ label: c.courseName, value: c.students }));
  const sgpaItems = semesterRows.map((p) => ({
    label: `Semester ${p.semester} (${p.students} students)`,
    value: Number(p.averageSgpa),
  }));
  const passItems = semesterRows.map((p) => ({
    label: `Semester ${p.semester}`,
    value: Number(p.passRate),
  }));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Welcome, {user.name}</h1>
        <p className="mt-1 text-slate-600">
          {ROLE_LABELS[user.role] ?? user.role}
          {overview.data ? ` · ${overview.data.departmentName}` : ""}
        </p>
      </div>

      <div className="flex flex-wrap gap-4">
        {isAdmin && (
          <Filter label="Department" value={departmentId} onChange={changeDepartment}>
            <option value="">All departments</option>
            {(departments.data ?? []).map((d) => (
              <option key={d.departmentId} value={d.departmentId}>
                {d.departmentName}
              </option>
            ))}
          </Filter>
        )}
        <Filter label="Course" value={courseId} onChange={changeCourse}>
          <option value="">All courses</option>
          {courseOptions.map((c) => (
            <option key={c.courseId} value={c.courseId}>
              {c.courseName}
            </option>
          ))}
        </Filter>
        <Filter label="Semester" value={semester} onChange={setSemester}>
          <option value="">All semesters</option>
          {semesterRows.map((p) => (
            <option key={p.semester} value={p.semester}>
              Semester {p.semester}
            </option>
          ))}
        </Filter>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Total students" value={overview.data ? overview.data.totalStudents : "-"} />
        <StatCard label="Total documents" value={overview.data ? overview.data.totalDocuments : "-"} />
        <StatCard label="Courses" value={overview.data ? courseOptions.length : "-"} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Section title="Students per course" loading={overview.loading} error={overview.error}>
          <BarChart items={perCourse} />
        </Section>
        <Section
          title="Average SGPA by semester"
          loading={performance.loading}
          error={performance.error}
        >
          <BarChart items={sgpaItems} max={10} format={(v) => formatNumber(v)} />
        </Section>
      </div>

      <Section
        title="Pass rate by semester"
        subtitle="Students who cleared every subject of that semester."
        loading={performance.loading}
        error={performance.error}
      >
        <BarChart items={passItems} max={100} format={(v) => `${formatNumber(v, 1)}%`} />
      </Section>

      <Section
        title="Toppers"
        subtitle={semester ? `Ranked by SGPA for semester ${semester}.` : "Ranked by CGPA."}
        loading={toppers.loading}
        error={toppers.error}
      >
        <ToppersTable
          rows={toppers.data ?? []}
          gpaLabel={semester ? "SGPA" : "CGPA"}
        />
      </Section>

      <Section
        title="Average marks by subject"
        loading={averages.loading}
        error={averages.error}
      >
        <SubjectAveragesTable rows={averages.data ?? []} />
      </Section>

      {isAdmin && (
        <Section
          title="Departments"
          loading={departments.loading}
          error={departments.error}
        >
          <DepartmentsTable rows={departments.data ?? []} />
        </Section>
      )}
    </div>
  );
}