import React from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";

import "../../styles/chart.scss";

const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div
        style={{
          backgroundColor: "rgba(19, 1, 34, 0.87)",
          borderRadius: "8px",
          padding: "10px",
          color: "white",
          fontSize: "16px",
          //   boxShadow: "0 2px 4px rgba(0, 0, 0, 0.1)",
        }}
      >
        <p
          style={{
            marginBottom: "6px",
            fontSize: "16px",
            fontWeight: "normal",
          }}
        >
          {label}
        </p>
        <p>Average Salary: {payload[0].value.toLocaleString()} VNĐ</p>
      </div>
    );
  }

  return null;
};

const AverageSalaryChart = ({ data }) => {
  return (
    <div className="chart-wrapper">
      <div className="chart-header">
        <p>Average Slary Chart</p>
      </div>
      <ResponsiveContainer width="100%" height={350}>
        <BarChart data={data}>
          {/* ✅ Gradient định nghĩa ở đây */}
          <defs>
            <linearGradient id="barGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#14b8a6" stopOpacity={1} />
              <stop offset="100%" stopColor="#3b82f6" stopOpacity={1} />
            </linearGradient>
          </defs>

          <CartesianGrid
            stroke="#ab81b540"
            strokeDasharray="1 0"
            vertical={false}
          />
          <XAxis dataKey="industryName" tick={false} axisLine={false} />
          <YAxis
            tick={{ fill: "#ffffff", fontSize: 13 }}
            tickMargin={20} // khoảng cách từ số đến trục
            width={90} // tăng độ rộng trục Y để không bị cắt
            axisLine={false}
            tickLine={false}
          />
          <Tooltip content={<CustomTooltip />} cursor={{ fill: "#41b6a892" }} />
          {/* ✅ Dùng fill là gradient */}
          <Bar
            dataKey="averageSalary"
            fill="url(#barGradient)"
            radius={[6, 6, 0, 0]}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export default AverageSalaryChart;
