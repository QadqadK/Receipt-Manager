import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import apiClient from "../api/client";
import { RECEIPT_CATEGORIES } from "../constants/categories";
import { formatCurrency } from "../utils/formatCurrency";
import { timSort } from "../utils/timsort";

const PAGE_SIZE = 50;

const SORT_FIELDS = [
  { value: "receiptDate", label: "Date" },
  { value: "storeName", label: "Store" },
  { value: "total", label: "Total" },
];

export default function ReceiptListPage() {
  const [receipts, setReceipts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [searchTerm, setSearchTerm] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("All");
  const [sortField, setSortField] = useState("receiptDate");
  const [sortDirection, setSortDirection] = useState("desc");
  const [currentPage, setCurrentPage] = useState(1);

  async function load() {
    setLoading(true);
    try {
      const { data } = await apiClient.get("/receipts", { params: { size: 1000 } });
      setReceipts(data.content);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load receipts");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleDelete(id) {
    if (!confirm("Delete this receipt?")) return;
    await apiClient.delete(`/receipts/${id}`);
    load();
  }

  const filteredSorted = useMemo(() => {
    const term = searchTerm.trim().toLowerCase();
    const filtered = receipts.filter((r) => {
      const matchesSearch = !term || r.storeName.toLowerCase().includes(term);
      const matchesCategory = categoryFilter === "All" || r.category === categoryFilter;
      return matchesSearch && matchesCategory;
    });

    const compareFn = (a, b) => {
      let av = a[sortField];
      let bv = b[sortField];
      if (sortField === "storeName") {
        av = av.toLowerCase();
        bv = bv.toLowerCase();
      }
      const cmp = av < bv ? -1 : av > bv ? 1 : 0;
      return sortDirection === "asc" ? cmp : -cmp;
    };

    return timSort(filtered, compareFn);
  }, [receipts, searchTerm, categoryFilter, sortField, sortDirection]);

  const totalPages = Math.max(1, Math.ceil(filteredSorted.length / PAGE_SIZE));
  const page = Math.min(currentPage, totalPages);
  const pageItems = filteredSorted.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  function resetToFirstPage(setter) {
    return (value) => {
      setter(value);
      setCurrentPage(1);
    };
  }

  function toggleSortDirection() {
    setSortDirection((d) => (d === "asc" ? "desc" : "asc"));
  }

  if (loading) return <div className="page">Loading...</div>;

  return (
    <div className="page">
      <h1>Receipts</h1>
      {error && <p className="error">{error}</p>}

      <div className="receipt-controls">
        <input
          type="text"
          placeholder="Search by company name"
          value={searchTerm}
          onChange={(e) => resetToFirstPage(setSearchTerm)(e.target.value)}
        />
        <select value={categoryFilter} onChange={(e) => resetToFirstPage(setCategoryFilter)(e.target.value)}>
          <option value="All">All categories</option>
          {RECEIPT_CATEGORIES.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
        <select value={sortField} onChange={(e) => setSortField(e.target.value)}>
          {SORT_FIELDS.map((f) => (
            <option key={f.value} value={f.value}>Sort by {f.label}</option>
          ))}
        </select>
        <button type="button" onClick={toggleSortDirection}>
          {sortDirection === "asc" ? "Ascending ▲" : "Descending ▼"}
        </button>
      </div>

      {filteredSorted.length === 0 ? (
        <p>No receipts match your filters. <Link to="/receipts/new">Add one</Link>.</p>
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
                <th></th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((r) => (
                <tr key={r.id}>
                  <td className="nowrap">{r.receiptDate}</td>
                  <td>{r.storeName}</td>
                  <td className="nowrap">{r.category}</td>
                  <td>{r.items.map((i) => i.itemName).join(", ")}</td>
                  <td className="nowrap">{formatCurrency(r.total)}</td>
                  <td className="nowrap">
                    <Link to={`/receipts/${r.id}/edit`}>Edit</Link>{" "}
                    <button onClick={() => handleDelete(r.id)}>Delete</button>
                  </td>
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
    </div>
  );
}
