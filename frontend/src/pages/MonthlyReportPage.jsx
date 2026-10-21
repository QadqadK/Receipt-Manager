import { useEffect, useMemo, useState } from "react";
import apiClient from "../api/client";
import { formatCurrency } from "../utils/formatCurrency";

const now = new Date();
const PAGE_SIZE = 50;

export default function MonthlyReportPage() {
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    setLoading(true);
    setError(null);
    setCurrentPage(1);
    apiClient
      .get("/reports/monthly", { params: { year, month } })
      .then(({ data }) => setReport(data))
      .catch((err) => setError(err.response?.data?.message || "Failed to load report"))
      .finally(() => setLoading(false));
  }, [year, month]);

  async function handleExport(path, mimeType, extension) {
    const response = await apiClient.get(path, {
      params: { year, month },
      responseType: "blob",
    });
    const url = URL.createObjectURL(new Blob([response.data], { type: mimeType }));
    const link = document.createElement("a");
    link.href = url;
    link.download = `receipts-${year}-${String(month).padStart(2, "0")}.${extension}`;
    link.click();
    URL.revokeObjectURL(url);
  }

  const totalPages = Math.max(1, Math.ceil((report?.receipts?.length || 0) / PAGE_SIZE));
  const page = Math.min(currentPage, totalPages);
  const pageItems = useMemo(
    () => (report ? report.receipts.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE) : []),
    [report, page]
  );

  return (
    <div className="page">
      <h1>Monthly Report</h1>
      <div className="report-controls">
        <label>
          Year
          <input type="number" value={year} onChange={(e) => setYear(Number(e.target.value))} />
        </label>
        <label>
          Month
          <select value={month} onChange={(e) => setMonth(Number(e.target.value))}>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
              <option key={m} value={m}>{m}</option>
            ))}
          </select>
        </label>
        <button
          onClick={() => handleExport("/reports/monthly/export", "text/csv", "csv")}
          disabled={loading || !report?.receipts?.length}
        >
          Export CSV
        </button>
        <button
          onClick={() => handleExport("/reports/monthly/export/pdf", "application/pdf", "pdf")}
          disabled={loading || !report?.receipts?.length}
        >
          Export PDF
        </button>
      </div>

      {loading && <p>Loading...</p>}
      {error && <p className="error">{error}</p>}

      {report && !loading && (
        <>
          {report.receipts.length === 0 ? (
            <p>No receipts for this month.</p>
          ) : (
            <>
              <table>
                <thead>
                  <tr>
                    <th className="nowrap">Date</th>
                    <th>Store</th>
                    <th className="nowrap">Category</th>
                    <th>Items</th>
                    <th className="nowrap">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.map((r) => (
                    <tr key={r.id}>
                      <td className="nowrap">{r.receiptDate}</td>
                      <td>{r.storeName}</td>
                      <td className="nowrap">{r.category}</td>
                      <td>{r.items.map((i) => `${i.itemName} x${i.quantity}`).join(", ")}</td>
                      <td className="nowrap">{formatCurrency(r.total)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className="pagination">
                <button type="button" disabled={page <= 1} onClick={() => setCurrentPage((p) => p - 1)}>
                  Previous
                </button>
                <span>Page {page} of {totalPages}</span>
                <button type="button" disabled={page >= totalPages} onClick={() => setCurrentPage((p) => p + 1)}>
                  Next
                </button>
              </div>
            </>
          )}

          <h3>Grand Total: {formatCurrency(report.grandTotal)}</h3>
        </>
      )}
    </div>
  );
}
