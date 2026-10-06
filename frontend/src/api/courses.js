import apiClient from "@/api/client";

export async function getCourses() {
  const { data } = await apiClient.get("/api/courses");
  return Array.isArray(data) ? data : data.content ?? [];
}
