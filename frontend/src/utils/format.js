export function formatNumber(value, digits = 2) {
  if (value === null || value === undefined || value === "") return "-";
  return Number(value).toFixed(digits);
}