import apiClient from "@/api/client";

export async function fetchMyDocuments() {
  const { data } = await apiClient.get("/api/students/me/documents");
  return data;
}

// The client defaults to JSON, so this overrides it for a multipart upload.
export async function uploadDocument({ file, type, description }) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("type", type);
  if (description && description.trim()) {
    formData.append("description", description.trim());
  }

  const { data } = await apiClient.post("/api/students/me/documents", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    timeout: 60000,
  });
  return data;
}

// A plain <a href> can't send the JWT, so fetch the file as a blob first.
export async function downloadDocumentBlob(id) {
  const response = await apiClient.get(`/api/documents/${id}/download`, {
    responseType: "blob",
  });
  return response.data;
}

export async function deleteDocument(id) {
  await apiClient.delete(`/api/documents/${id}`);
}