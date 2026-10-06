import apiClient from "@/api/client";

// Returns a Spring Page. Download and delete reuse src/api/documents.js.
export async function fetchStaffDocuments({ departmentId, type, page = 0, size = 10 }) {
  const { data } = await apiClient.get("/api/hod/documents", {
    params: {
      departmentId: departmentId || undefined,
      type: type || undefined,
      page,
      size,
    },
  });
  return data;
}