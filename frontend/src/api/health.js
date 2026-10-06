import apiClient from "@/api/client";

// Use the health URL you built in Phase 1.
// If yours is different (for example /actuator/health), change it here.
export async function checkBackendHealth() {
  const { data } = await apiClient.get("/api/health");
  return data;
}