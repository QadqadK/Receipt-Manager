import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function Header() {
  const { username, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <header className="app-header">
      <span className="app-title">Receipt Management System JOKO XYZ</span>
      <div className="header-user">
        <span>{username}</span>
        <button onClick={handleLogout}>Log out</button>
      </div>
    </header>
  );
}
