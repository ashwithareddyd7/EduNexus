import apiClient from "@/api/client";

// Empty strings become undefined so axios leaves them out of the query string.
const opt = (value) => value || undefined;

export async function fetchOverview(departmentId) {
  const { data } = await apiClient.get("/api/hod/stats/overview", {
    params: { departmentId: opt(departmentId) },
  });
  return data;
}

export async function fetchToppers({ departmentId, courseId, semester, limit = 10 }) {
  const { data } = await apiClient.get("/api/hod/stats/toppers", {
    params: {
      departmentId: opt(departmentId),
      courseId: opt(courseId),
      semester: opt(semester),
      limit,
    },
  });
  return data;
}

export async function fetchAverageMarks({ departmentId, courseId, semester }) {
  const { data } = await apiClient.get("/api/hod/stats/average-marks", {
    params: {
      departmentId: opt(departmentId),
      courseId: opt(courseId),
      semester: opt(semester),
    },
  });
  return data;
}

export async function fetchSemesterPerformance({ departmentId, courseId }) {
  const { data } = await apiClient.get("/api/hod/stats/semester-performance", {
    params: { departmentId: opt(departmentId), courseId: opt(courseId) },
  });
  return data;
}

// Admin only.
export async function fetchDepartmentStats() {
  const { data } = await apiClient.get("/api/admin/stats/departments");
  return data;
}