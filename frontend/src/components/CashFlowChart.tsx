import React from 'react';
import {
  ResponsiveContainer,
  ComposedChart,
  Area,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ReferenceLine,
  CartesianGrid,
  Legend,
} from 'recharts';
import type { CashFlowPoint } from '../types';
import { formatINR } from '../utils/formatters';

interface CashFlowChartProps {
  data: CashFlowPoint[];
  threshold: number;
}

export const CashFlowChart: React.FC<CashFlowChartProps> = ({ data, threshold }) => {
  // Chart tooltip component
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      const point = payload[0].payload as CashFlowPoint;
      const isShortage = point.balance < threshold;

      return (
        <div className="bg-slate-900 text-white p-3 rounded-lg shadow-xl text-xs space-y-1.5 border border-slate-700 min-w-[180px]">
          <div className="flex items-center justify-between border-b border-slate-700 pb-1">
            <span className="font-semibold text-slate-200">{label}</span>
            {point.isProjected && (
              <span className="px-1.5 py-0.5 rounded bg-blue-500/20 text-blue-300 text-[10px] font-medium">
                Forecast
              </span>
            )}
          </div>
          <div className="flex justify-between">
            <span className="text-slate-400">Cash Balance:</span>
            <span className={`font-bold ${isShortage ? 'text-rose-400' : 'text-emerald-400'}`}>
              {formatINR(point.balance)}
            </span>
          </div>
          {point.inflow > 0 && (
            <div className="flex justify-between">
              <span className="text-slate-400">Expected Inflow:</span>
              <span className="text-emerald-400 font-medium">+{formatINR(point.inflow)}</span>
            </div>
          )}
          {point.outflow > 0 && (
            <div className="flex justify-between">
              <span className="text-slate-400">Expected Outflow:</span>
              <span className="text-rose-400 font-medium">-{formatINR(point.outflow)}</span>
            </div>
          )}
          <div className="flex justify-between border-t border-slate-800 pt-1 text-[11px] text-slate-400">
            <span>Safety Threshold:</span>
            <span>{formatINR(threshold)}</span>
          </div>
          {isShortage && (
            <div className="pt-1 text-rose-300 font-medium text-[11px] flex items-center gap-1">
              ⚠️ Shortage: {formatINR(threshold - point.balance)}
            </div>
          )}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-100 gap-2">
        <div>
          <h3 className="text-base font-bold text-slate-900">Cash-Flow Trajectory (14-Day View)</h3>
          <p className="text-xs text-slate-500">
            Past 7 days actuals &amp; upcoming 7 days deterministic projection
          </p>
        </div>
        <div className="flex items-center gap-4 text-xs">
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded-full bg-emerald-500"></span>
            <span className="text-slate-600 font-medium">Projected Balance</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-4 h-0.5 bg-rose-500 border-t-2 border-dashed border-rose-500"></span>
            <span className="text-slate-600 font-medium">Safety Threshold ({formatINR(threshold)})</span>
          </div>
        </div>
      </div>

      <div className="h-72 w-full mt-4">
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={data} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
            <defs>
              <linearGradient id="balanceGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10b981" stopOpacity={0.25} />
                <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
            <XAxis
              dataKey="label"
              tickLine={false}
              axisLine={{ stroke: '#cbd5e1' }}
              tick={{ fill: '#64748b', fontSize: 11 }}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`}
              tick={{ fill: '#64748b', fontSize: 11 }}
              domain={['auto', 'auto']}
            />
            <Tooltip content={<CustomTooltip />} />
            <ReferenceLine
              y={threshold}
              stroke="#ef4444"
              strokeDasharray="4 4"
              strokeWidth={2}
              label={{
                value: `Safety Min: ${formatINR(threshold)}`,
                position: 'insideTopRight',
                fill: '#ef4444',
                fontSize: 10,
                fontWeight: 600,
              }}
            />
            <Area
              type="monotone"
              dataKey="balance"
              stroke="#059669"
              strokeWidth={3}
              fillOpacity={1}
              fill="url(#balanceGrad)"
              name="Cash Balance"
            />
            <Line
              type="monotone"
              dataKey="balance"
              stroke="#059669"
              strokeWidth={2.5}
              dot={(props: any) => {
                const isUnder = props.payload.balance < threshold;
                return (
                  <circle
                    key={`dot-${props.index}`}
                    cx={props.cx}
                    cy={props.cy}
                    r={props.payload.isProjected ? (isUnder ? 5 : 4) : 3}
                    fill={isUnder ? '#ef4444' : '#10b981'}
                    stroke="#ffffff"
                    strokeWidth={2}
                  />
                );
              }}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>

      <div className="mt-3 bg-slate-50 rounded-lg p-3 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-600 gap-2 border border-slate-200">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
          <span>Actual balance verified from verified bank inflows/outflows.</span>
        </div>
        <div className="flex items-center gap-2 text-rose-600 font-medium">
          <span>Dips below threshold trigger proactive supplier &amp; debtor alerts.</span>
        </div>
      </div>
    </div>
  );
};
