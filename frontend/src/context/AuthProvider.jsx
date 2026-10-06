import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "@/context/authContext";
import { loginRequest } from "@/api/auth";
import { UNAUTHORIZED_EVENT } from "@/api/client";
import { tokenStorage } from "@/utils/tokenStorage";

// Restore the session after a page refresh, but only if a token exists.
function readStoredUser() {
  return tokenStorage.get() ? tokenStorage.getUser() : null;
}

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const login = useCallback(async (credentials) => {
    const { token, user: loggedInUser } = await loginRequest(credentials);
    tokenStorage.set(token);
    tokenStorage.setUser(loggedInUser);
    setUser(loggedInUser);
    return loggedInUser;
  }, []);

  const logout = useCallback(() => {
    tokenStorage.clear();
    setUser(null);
  }, []);

  // The Axios client fires this when the backend returns 401.
  useEffect(() => {
    const handleUnauthorized = () => setUser(null);
    window.addEventListener(UNAUTHORIZED_EVENT, handleUnauthorized);
    return () =>
      window.removeEventListener(UNAUTHORIZED_EVENT, handleUnauthorized);
  }, []);

  const value = useMemo(
    () => ({ user, isAuthenticated: Boolean(user), login, logout }),
    [user, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}