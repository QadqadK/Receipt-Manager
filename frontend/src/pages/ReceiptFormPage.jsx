import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import apiClient from "../api/client";
import { RECEIPT_CATEGORIES } from "../constants/categories";

const emptyItem = () => ({ itemName: "", price: "", quantity: 1 });

export default function ReceiptFormPage() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();

  const [storeName, setStoreName] = useState("");
  const [receiptDate, setReceiptDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [category, setCategory] = useState(RECEIPT_CATEGORIES[0]);
  const [items, setItems] = useState([emptyItem()]);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    apiClient.get(`/receipts/${id}`).then(({ data }) => {
      setStoreName(data.storeName);
      setReceiptDate(data.receiptDate);
      setCategory(data.category);
      setItems(data.items.map((i) => ({ itemName: i.itemName, price: i.price, quantity: i.quantity })));
    });
  }, [id, isEdit]);

  function updateItem(index, field, value) {
    setItems((prev) => prev.map((it, i) => (i === index ? { ...it, [field]: value } : it)));
  }

  function addItem() {
    setItems((prev) => [...prev, emptyItem()]);
  }

  function removeItem(index) {
    setItems((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const payload = {
        storeName,
        receiptDate,
        category,
        items: items.map((i) => ({
          itemName: i.itemName,
          price: Number(i.price),
          quantity: Number(i.quantity),
        })),
      };
      if (isEdit) {
        await apiClient.put(`/receipts/${id}`, payload);
      } else {
        await apiClient.post("/receipts", payload);
      }
      navigate("/receipts");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to save receipt");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <h1>{isEdit ? "Edit Receipt" : "Add Receipt"}</h1>
      <form onSubmit={handleSubmit}>
        <label>
          Store name
          <input value={storeName} onChange={(e) => setStoreName(e.target.value)} required />
        </label>
        <label>
          Date
          <input type="date" value={receiptDate} onChange={(e) => setReceiptDate(e.target.value)} required />
        </label>
        <label>
          Category
          <select value={category} onChange={(e) => setCategory(e.target.value)} required>
            {RECEIPT_CATEGORIES.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </label>

        <h3>Items</h3>
        {items.map((item, index) => (
          <div className="item-row" key={index}>
            <input
              placeholder="Item name"
              value={item.itemName}
              onChange={(e) => updateItem(index, "itemName", e.target.value)}
              required
            />
            <input
              type="number"
              step="0.01"
              min="0"
              placeholder="Price"
              value={item.price}
              onChange={(e) => updateItem(index, "price", e.target.value)}
              required
            />
            <input
              type="number"
              min="1"
              placeholder="Qty"
              value={item.quantity}
              onChange={(e) => updateItem(index, "quantity", e.target.value)}
              required
            />
            <button type="button" onClick={() => removeItem(index)} disabled={items.length === 1}>
              Remove
            </button>
          </div>
        ))}
        <button type="button" onClick={addItem}>+ Add item</button>

        {error && <p className="error">{error}</p>}
        <div className="form-actions">
          <button type="submit" disabled={submitting}>Save Receipt</button>
        </div>
      </form>
    </div>
  );
}
