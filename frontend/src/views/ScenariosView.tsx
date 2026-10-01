import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import type { DashboardSummary, ScenarioSimulationResponse, Invoice, PaymentObligation } from '../types';
import { formatINR } from '../utils/formatters';
import {
  SlidersHorizontal,
  CheckCircle2,
  AlertTriangle,
  Play,
  RotateCcw,
  TrendingUp,
  Clock,
  Sparkles,
  ArrowRight
} from 'lucide-react';
import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ReferenceLine,
  CartesianGrid,
  Legend,
} from 'recharts';

interface ScenariosViewProps {
  summary: DashboardSummary;
}

export const ScenariosView: React.FC<ScenariosViewProps> = ({ summary }) => {
  const [selectedInvoiceIds, setSelectedInvoiceIds] = useState<number[]>([1]); // Default select Mehta Kirana
  const [selectedPaymentIds, setSelectedPaymentIds] = useState<number[]>([1]); // Default select ITC Distribution
  const [additionalInflow, setAdditionalInflow] = useState<number>(0);
  const [simResult, setSimResult] = useState<ScenarioSimulationResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  const runSimulation = async () => {
    try {
      setLoading(true);
      const res = await api.simulateScenario({
        collectedInvoiceIds: selectedInvoiceIds,
        delayedPaymentIds: selectedPaymentIds,
        delayDays: 7,
        additionalInflow: additionalInflow,
      });
      setSimResult(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    runSimulation();
  }, []);

  const toggleInvoice = (id: number) => {
    setSelectedInvoiceIds((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  };

  const togglePayment = (id: number) => {
    setSelectedPaymentIds((prev) =>
      prev.includes(id) ? prev.filter((p) => p !== id) : [...prev, id]
    );
  };

  // Combine charts for comparison
  const comparisonData = simResult
    ? simResult.originalForecast.map((orig, idx) => {
        const sim = simResult.simulatedForecast[idx];
        return {
          label: orig.label,
          original: orig.balance,
          simulated: sim ? sim.balance : orig.balance,
          threshold: orig.threshold,
        };
      })
    : [];

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold text-slate-900 flex items-center gap-2">
          <span>What-If Cash Flow Scenario Simulator</span>
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-purple-100 text-purple-800">
            Interactive Sandbox
          </span>
        </h2>
        <p className="text-xs text-slate-500">
          Simulate collecting overdue receivables, negotiating supplier payment terms, or adjusting expenses to see immediate impact on your 7-day cash curve.
        </p>
      </div>

      {/* Outcome Banner */}
      {simResult && (
        <div
          className={`rounded-xl border p-5 transition-all shadow-sm ${
            !simResult.simulatedRisk
              ? 'bg-emerald-50/80 border-emerald-300'
              : 'bg-amber-50/80 border-amber-300'
          }`}
        >
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-start gap-3">
              <div
                className={`p-2.5 rounded-xl text-white ${
                  !simResult.simulatedRisk ? 'bg-emerald-600' : 'bg-amber-600'
                }`}
              >
                {!simResult.simulatedRisk ? (
                  <CheckCircle2 className="w-5 h-5" />
                ) : (
                  <AlertTriangle className="w-5 h-5" />
                )}
              </div>
              <div>
                <h4 className="text-sm font-bold text-slate-900">
                  {!simResult.simulatedRisk
                    ? 'Risk Eliminated in Simulation!'
                    : 'Shortage Reduced but Still Active'}
                </h4>
                <p className="text-xs text-slate-700 mt-1 font-medium leading-relaxed">
                  {simResult.outcomeSummary}
                </p>
                <div className="flex items-center gap-4 mt-2 text-xs font-semibold">
                  <span className="text-slate-600">
                    Original Min: <strong className="text-rose-600">{formatINR(simResult.originalMinCash)}</strong>
                  </span>
                  <ArrowRight className="w-3.5 h-3.5 text-slate-400" />
                  <span className="text-slate-600">
                    Simulated Min: <strong className="text-emerald-600">{formatINR(simResult.simulatedMinCash)}</strong>
                  </span>
                  <span className="px-2 py-0.5 rounded bg-white text-emerald-700 border border-emerald-200">
                    +{formatINR(simResult.cashDifference)} Liquidity Buffer
                  </span>
                </div>
              </div>
            </div>

            <button
              onClick={runSimulation}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-bold transition-all shrink-0 self-start sm:self-auto"
            >
              <RotateCcw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              <span>Recalculate Curve</span>
            </button>
          </div>
        </div>
      )}

      {/* Comparison Chart */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-3 border-b border-slate-100 gap-2">
          <div>
            <h3 className="text-sm font-bold text-slate-900">
              Original Forecast vs. Simulated Cash Curve
            </h3>
            <p className="text-xs text-slate-500">
              Notice how targeted collections or payment deferrals keep balance above the red threshold line.
            </p>
          </div>
          <div className="flex items-center gap-4 text-xs">
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-rose-500"></span>
              <span className="text-slate-600 font-medium">Original Trajectory</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-3 h-0.5 bg-emerald-600"></span>
              <span className="text-slate-600 font-medium">Simulated Trajectory</span>
            </div>
          </div>
        </div>

        <div className="h-64 w-full mt-4">
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart data={comparisonData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
              <XAxis dataKey="label" tickLine={false} tick={{ fontSize: 11, fill: '#64748b' }} />
              <YAxis
                tickLine={false}
                axisLine={false}
                tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`}
                tick={{ fontSize: 11, fill: '#64748b' }}
              />
              <Tooltip
                formatter={(val: any) => formatINR(Number(val))}
                contentStyle={{ backgroundColor: '#0f172a', borderRadius: '8px', color: '#fff', fontSize: '12px' }}
              />
              <ReferenceLine
                y={summary.minimumCashThreshold}
                stroke="#ef4444"
                strokeDasharray="4 4"
                label={{ value: 'Safety Threshold', fill: '#ef4444', fontSize: 10 }}
              />
              <Line
                type="monotone"
                dataKey="original"
                stroke="#f43f5e"
                strokeWidth={2}
                strokeDasharray="4 4"
                dot={{ r: 3, fill: '#f43f5e' }}
                name="Original Forecast"
              />
              <Line
                type="monotone"
                dataKey="simulated"
                stroke="#059669"
                strokeWidth={3}
                dot={{ r: 4, fill: '#059669' }}
                name="Simulated Scenario"
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Interactive Controls Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Overdue Invoices to Collect */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-3">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2">
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900">
                1. Accelerate Customer Collections
              </h4>
              <p className="text-xs text-slate-500">Select overdue invoices to simulate collecting on Day 1</p>
            </div>
            <TrendingUp className="w-4 h-4 text-emerald-600" />
          </div>

          <div className="space-y-2">
            {summary.overdueInvoices.map((inv) => {
              const selected = selectedInvoiceIds.includes(inv.id);
              return (
                <label
                  key={inv.id}
                  className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-all ${
                    selected
                      ? 'bg-emerald-50/60 border-emerald-300 text-slate-900'
                      : 'bg-slate-50/50 border-slate-200 text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <input
                      type="checkbox"
                      checked={selected}
                      onChange={() => toggleInvoice(inv.id)}
                      className="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500"
                    />
                    <div>
                      <span className="text-xs font-bold block">{inv.customerName}</span>
                      <span className="text-[10px] text-slate-500">{inv.invoiceNumber}</span>
                    </div>
                  </div>
                  <span className="text-xs font-bold text-emerald-700">+{formatINR(inv.amount)}</span>
                </label>
              );
            })}
          </div>
        </div>

        {/* Supplier Obligations to Defer */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-3">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2">
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900">
                2. Defer Supplier Obligations
              </h4>
              <p className="text-xs text-slate-500">Select supplier payments to negotiate a 7-day deferral</p>
            </div>
            <Clock className="w-4 h-4 text-amber-600" />
          </div>

          <div className="space-y-2">
            {summary.upcomingPaymentList.slice(0, 4).map((p) => {
              const selected = selectedPaymentIds.includes(p.id);
              return (
                <label
                  key={p.id}
                  className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-all ${
                    selected
                      ? 'bg-amber-50/60 border-amber-300 text-slate-900'
                      : 'bg-slate-50/50 border-slate-200 text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <input
                      type="checkbox"
                      checked={selected}
                      onChange={() => togglePayment(p.id)}
                      className="w-4 h-4 rounded text-amber-600 focus:ring-amber-500"
                    />
                    <div>
                      <span className="text-xs font-bold block">{p.supplierName}</span>
                      <span className="text-[10px] text-slate-500">{p.category}</span>
                    </div>
                  </div>
                  <span className="text-xs font-bold text-slate-900">{formatINR(p.amount)}</span>
                </label>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
