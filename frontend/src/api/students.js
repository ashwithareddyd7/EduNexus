import apiClient from "@/api/client";

// Items inside GpaResponse.semesters. Adjust here if the field names differ.
// Matches the backend record SemesterGpa(int semester, int credits, BigDecimal sgpa).
function normalizeSemester(item) {
  return {
    semester: item.semester,
    credits: item.credits,
    sgpa: item.sgpa,
  };
}
// Returns null when the student has not created a profile yet (404).
export async function getMyProfile() {
  try {
    const { data } = await apiClient.get("/api/students/me");
    return data;
  } catch (error) {
    if (error.response?.status === 404) return null;
    throw error;
  }
}

export async function getGpa(studentId) {
  const { data } = await apiClient.get(`/api/students/${studentId}/gpa`);
  return {
    cgpa: data.cgpa ?? null,
    semesters: (data.semesters ?? [])
      .map(normalizeSemester)
      .sort((a, b) => a.semester - b.semester),
  };
}

export async function getMyDocuments() {
  const { data } = await apiClient.get("/api/students/me/documents");
  return Array.isArray(data) ? data : data.content ?? [];
}
