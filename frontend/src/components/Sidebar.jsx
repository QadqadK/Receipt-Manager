import { NavLink } from "react-router-dom";

export default function Sidebar() {
  return (
    <nav className="sidebar">
      <NavLink to="/receipts" end>Receipts</NavLink>
      <NavLink to="/receipts/new">Add Receipt</NavLink>
      <NavLink to="/reports">Monthly Report</NavLink>
    </nav>
  );
}
