import { API_BASE_URL } from "./config";
import { apiRequest } from "./httpClient";

/**
 * fetches all system audit log entries for the admin panel
 *
 * @returns {Promise<Array>} array of audit log items
 */
export async function fetchAuditLogs() {
  const response = await apiRequest(`${API_BASE_URL}/admin/audit-logs`, {
    method: "GET",
  });

  return response.data;
}
