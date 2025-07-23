import React from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  Legend,
} from "recharts";
import "../../styles/forecastChart.scss";

const ItJobForecastChart = ({ data }) => {
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div
          style={{
            backgroundColor: "rgba(19, 1, 34, 0.87)",
            borderRadius: "8px",
            padding: "10px",
            color: "white",
            fontSize: "14px",
          }}
        >
          <p
            style={{
              marginBottom: "6px",
              fontSize: "14px",
              fontWeight: "normal",
            }}
          >
            Month: {label}
          </p>
          <p
            style={{
              marginBottom: "6px",
              fontSize: "14px",
              fontWeight: "normal",
            }}
          >
            Job Count: {payload[0].value.toLocaleString()}
          </p>
          <p
            style={{
              marginBottom: "6px",
              fontSize: "14px",
              fontWeight: "normal",
            }}
          >
            {payload[0].payload.forecast ? "Forecast" : "Real Data"}
          </p>
        </div>
      );
    }

    return null;
  };
  return (
    <div className="forecast-chart-wrapper">
      <div className="chart-header">
        <p>IT Job Forecast Chart</p>
      </div>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={data}>
          <CartesianGrid
            stroke="#ab81b5"
            strokeWidth={1}
            vertical={false}
            horizontal={true}
            strokeDasharray="0"
          />
          <XAxis
            axisLine={false}
            dataKey="month"
            tick={{ fill: "#ccc", fontSize: 13 }}
            tickMargin={10} // khoảng cách từ số đến trục
          />
          <YAxis
            tick={{ fill: "#ffffff", fontSize: 13 }}
            tickMargin={20} // khoảng cách từ số đến trục
            width={90} // tăng độ rộng trục Y để không bị cắt
            axisLine={false}
            tickLine={false}
          />
          <Tooltip content={<CustomTooltip />} cursor={{ fill: "#41b6a892" }} />
          <Legend />
          <Line
            type="monotone"
            dataKey="jobCount"
            stroke="#10b981"
            strokeWidth={2}
            dot={{ r: 4 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default ItJobForecastChart;
