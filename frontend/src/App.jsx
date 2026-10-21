import { Navigate, Route, Routes } from "react-router-dom";
import "./App.css";
import { AuthProvider, useAuth } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Header from "./components/Header";
import Sidebar from "./components/Sidebar";
import Footer from "./components/Footer";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ReceiptListPage from "./pages/ReceiptListPage";
import ReceiptFormPage from "./pages/ReceiptFormPage";
import MonthlyReportPage from "./pages/MonthlyReportPage";

function AppLayout() {
  const { isAuthenticated } = useAuth();

  const routes = (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/receipts" element={<ReceiptListPage />} />
        <Route path="/receipts/new" element={<ReceiptFormPage />} />
        <Route path="/receipts/:id/edit" element={<ReceiptFormPage />} />
        <Route path="/reports" element={<MonthlyReportPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/receipts" replace />} />
    </Routes>
  );

  if (!isAuthenticated) {
    return routes;
  }

  return (
    <div className="app-shell">
      <Header />
      <div className="app-body">
        <Sidebar />
        <main className="app-content">{routes}</main>
      </div>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppLayout />
    </AuthProvider>
  );
}
