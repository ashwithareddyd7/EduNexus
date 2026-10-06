import apiClient from "@/api/client";

// HOD: own department only. Admin: every student. Returns a Spring Page.
export async function listStudents({ page = 0, size = 10 }) {
  const { data } = await apiClient.get("/api/students", {
    params: { page, size, sort: "studentId,asc" },
  });
  return data;
}

// Admin only. Disabling blocks login and every later request; no data is deleted.
export async function setStudentStatus(id, enabled) {
  const { data } = await apiClient.patch(`/api/admin/students/${id}/status`, { enabled });
  return data;
}