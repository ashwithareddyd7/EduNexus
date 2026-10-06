import apiClient from "@/api/client";

// Rows match the backend record RecordResponse.
export async function getRecords(studentId) {
  const { data } = await apiClient.get(`/api/students/${studentId}/records`);
  return Array.isArray(data) ? data : data.content ?? [];
}
