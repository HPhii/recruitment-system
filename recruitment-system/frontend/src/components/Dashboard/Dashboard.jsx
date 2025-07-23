import React, { useEffect, useState } from "react";
import axios from "axios";
import AverageSalaryChart from "../AverageSalaryChart/AverageSalaryChart";
import ItJobForecastChart from "../ItJobForecastChart/ItJobForecastChart";
import "./Dashboard.scss";

const Dashboard = () => {
  const [salaryData, setSalaryData] = useState([]);
  const [forecastData, setForecastData] = useState([]);

  const getSalaryData = async () => {
    try {
      const response = await axios.get(
        "http://localhost:8080/api/salary/average-by-industry"
      );
      console.log("Salary Data Response:", response);
      setSalaryData(response.data);
    } catch (error) {
      console.error("Error fetching salary data:", error);
    }
  };

  const getForecastData = async () => {
    try {
      const response = await axios.get(
        "http://localhost:8080/api/forecast/it-demand"
      );
      setForecastData(response.data);
    } catch (error) {
      console.error("Error fetching forecast data:", error);
    }
  };

  useEffect(() => {
    getSalaryData();
    getForecastData();
  }, []);

  const sortedData = [...salaryData].sort((a, b) => {
    if (a.industryName === "Others") return 1;
    if (b.industryName === "Others") return -1;
    return 0;
  });

  return (
    <div>
      <div className="dashboard-average-salary">
        <AverageSalaryChart data={sortedData} />
      </div>
      <div className="dashboard-it-job-forecast">
        <ItJobForecastChart data={forecastData} />
      </div>
    </div>
  );
};

export default Dashboard;
