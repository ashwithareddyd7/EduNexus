import apiClient from "@/api/client";

export async function getStudentById(id) {
  const { data } = await apiClient.get(`/api/students/${id}`);
  return data;
}

export async function getStudentDocuments(id) {
  const { data } = await apiClient.get(`/api/students/${id}/documents`);
  return data;
}

// All subjects of a course, or just one semester's.
export async function listSubjects(courseId, semester) {
  const { data } = await apiClient.get("/api/subjects", {
    params: { courseId, semester: semester || undefined },
  });
  return data;
}

export async function createSubject({ courseId, semesterNumber, code, name, credits, maxMarks }) {
  const { data } = await apiClient.post("/api/subjects", {
    courseId,
    semesterNumber,
    code,
    name,
    credits,
    maxMarks,
  });
  return data;
}

// Creates the record, or overwrites the marks if one already exists (a retake).
export async function saveMarks({ studentId, subjectId, marks }) {
  const { data } = await apiClient.put("/api/records", { studentId, subjectId, marks });
  return data;
}