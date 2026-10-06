import apiClient from "@/api/client";

// "ROLE_ADMIN" or "ADMIN" (string, or { name } / { authority }) -> "ADMIN"
function normalizeRole(role) {
  if (!role) return null;
  const value = typeof role === "string" ? role : role.name ?? role.authority;
  return value ? value.replace(/^ROLE_/, "") : null;
}

export async function loginRequest({ email, password }) {
  const { data } = await apiClient.post("/api/auth/login", { email, password });

  const token = data.token ?? data.accessToken;
  const source = data.user ?? data;
  const role = normalizeRole(source.role ?? source.roles?.[0]);

  if (!token || !role) {
    throw new Error("Unexpected login response from the server.");
  }

  return {
    token,
    user: {
      id: source.id ?? source.userId ?? null,
      name: source.fullName ?? source.name ?? source.email ?? email,
      email: source.email ?? email,
      role,
    },
  };
}

// Students only. If your backend uses different field names, change them here.
export async function registerRequest({ fullName, email, password }) {
  const { data } = await apiClient.post("/api/auth/register", {
    fullName,
    email,
    password,
  });
  return data;
}