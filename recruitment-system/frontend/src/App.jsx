import React from "react";
import Dashboard from "./components/Dashboard/Dashboard";
import "./app.scss"; // Import your main styles

const App = () => {
  return (
    <div className="app_wrapper">
      <p className="app_text">Dashboard</p>
      <div className="app_container">
        <Dashboard />
      </div>
    </div>
  );
};

export default App;
