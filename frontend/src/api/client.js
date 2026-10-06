import axios from "axios";
import { tokenStorage } from "@/utils/tokenStorage";

// Fired when the backend rejects the token. AuthContext (Phase 13)
// will listen for this and send the user back to the login page.
export const UNAUTHORIZED_EVENT = "edunexus:unauthorized";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

// Attach the JWT to every outgoing request if we have one.
apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On 401, drop the stale token and tell the app.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      tokenStorage.clear();
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
    }
    return Promise.reject(error);
  }
);

// Turns any Axios error into a message safe to show the user.
export function getErrorMessage(error) {
  if (error.response?.data?.message) return error.response.data.message;
  if (error.code === "ECONNABORTED") return "The request timed out.";
  if (error.request && !error.response) return "Cannot reach the server.";
      return error.message || "Something went wrong.";
}

export default apiClient;