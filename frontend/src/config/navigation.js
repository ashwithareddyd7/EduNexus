export const ROLE_LABELS = {
  STUDENT: "Student",
  HOD: "Head of Department",
  ADMIN: "Administrator",
};

export const NAV_ITEMS = [
  { label: "Dashboard", to: "/dashboard", roles: ["STUDENT", "HOD", "ADMIN"] },

  { label: "My Profile", to: "/profile", roles: ["STUDENT"] },
  { label: "Academic Records", to: "/academics", roles: ["STUDENT"] },
  { label: "My Documents", to: "/documents", roles: ["STUDENT"] },

  { label: "Students", to: "/students", roles: ["HOD", "ADMIN"] },
  { label: "Subjects", to: "/subjects", roles: ["HOD", "ADMIN"] },
  { label: "Staff Documents", to: "/staff/documents", roles: ["HOD", "ADMIN"] },

  { label: "Account Status", to: "/admin/accounts", roles: ["ADMIN"] },
];

export function navItemsForRole(role) {
  return NAV_ITEMS.filter((item) => item.roles.includes(role));
}