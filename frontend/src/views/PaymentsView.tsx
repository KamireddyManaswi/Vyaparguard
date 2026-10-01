import React, { useState, useEffect } from 'react';
import type { PaymentObligation } from '../types';
import { api } from '../services/api';
import { formatINR, formatDate, getDaysDifference } from '../utils/formatters';
import { Search, Clock, Calendar, Check, AlertTriangle, ShieldCheck } from 'lucide-react';

interface PaymentsViewProps {
  onRefreshNeeded: () => void;
}

export const PaymentsView: React.FC<PaymentsViewProps> = ({ onRefreshNeeded }) => {
  const [payments, setPayments] = useState<PaymentObligation[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'PENDING' | 'PAID'>('ALL');
  const [rescheduledId, setRescheduledId] = useState<number | null>(null);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      const data = await api.getAllPayments();
      setPayments(data);
    } catch (err) {
      console.error('Failed to load payments:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayments();
  }, []);

  const handleReschedule = (p: PaymentObligation) => {
    setRescheduledId(p.id);
    alert(
      `Negotiation template generated for ${p.supplierName}:\n"Dear ${p.supplierName} Accounts Team, Regarding payment of ${formatINR(p.amount)} due on ${formatDate(p.dueDate)}, we request a 7-day deferral to reconcile incoming warehouse stock. Thank you for your continued partnership."`
    );
    setTimeout(() => setRescheduledId(null), 3000);
  };

  const totalUpcoming = payments
    .filter((p) => p.status !== 'PAID')
    .reduce((acc, p) => acc + p.amount, 0);

  const criticalNext7Days = payments
    .filter((p) => {
      if (p.status === 'PAID') return false;
      const diff = getDaysDifference(p.dueDate);
      return diff >= 0 && diff <= 7;
    })
    .reduce((acc, p) => acc + p.amount, 0);

  const totalPaid = payments
    .filter((p) => p.status === 'PAID')
    .reduce((acc, p) => acc + p.amount, 0);

  const filtered = payments.filter((p) => {
    const matchesSearch =
      p.supplierName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.category.toLowerCase().includes(searchTerm.toLowerCase());

    if (!matchesSearch) return false;
    if (statusFilter === 'ALL') return true;
    return p.status === statusFilter;
  });

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold text-slate-900">Supplier Payment Obligations</h2>
        <p className="text-xs text-slate-500">
          Manage upcoming trade payables, distributor credit cycles, and statutory liabilities.
        </p>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase">Total Scheduled Payables</span>
            <div className="p-2 rounded-lg bg-slate-100 text-slate-600">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <h4 className="text-2xl font-bold text-slate-900 mt-2">{formatINR(totalUpcoming)}</h4>
          <p className="text-xs text-slate-500 mt-1">Total pending supplier obligations</p>
        </div>

        <div className="bg-white rounded-xl border border-amber-200 p-4 shadow-sm bg-amber-50/20">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-amber-700 uppercase">Critical Due in 7 Days</span>
            <div className="p-2 rounded-lg bg-amber-100 text-amber-700">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>
          <h4 className="text-2xl font-bold text-amber-700 mt-2">{formatINR(criticalNext7Days)}</h4>
          <p className="text-xs text-amber-800 mt-1">Driving the projected cash crunch</p>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase">Settled This Month</span>
            <div className="p-2 rounded-lg bg-emerald-50 text-emerald-600">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          <h4 className="text-2xl font-bold text-slate-900 mt-2">{formatINR(totalPaid)}</h4>
          <p className="text-xs text-slate-500 mt-1">Disbursed to vendors on time</p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search by supplier or category..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-xs rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          {(['ALL', 'PENDING', 'PAID'] as const).map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                statusFilter === status
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {status === 'ALL' ? 'All Obligations' : status}
            </button>
          ))}
        </div>
      </div>

      {/* Payments Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-500 text-xs">Loading obligations...</div>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-xs">
            No payment obligations found matching your criteria.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-600 uppercase font-semibold border-b border-slate-200 tracking-wider">
                <tr>
                  <th className="px-4 py-3">Supplier Name</th>
                  <th className="px-4 py-3">Category</th>
                  <th className="px-4 py-3 text-right">Amount</th>
                  <th className="px-4 py-3">Due Date</th>
                  <th className="px-4 py-3">Timeline</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-center">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map((p) => {
                  const daysDiff = getDaysDifference(p.dueDate);
                  const isCritical = daysDiff <= 3 && p.status !== 'PAID';
                  const isDone = rescheduledId === p.id;

                  return (
                    <tr key={p.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3 font-semibold text-slate-900">{p.supplierName}</td>
                      <td className="px-4 py-3">
                        <span className="px-2 py-0.5 rounded text-[11px] font-medium bg-slate-100 text-slate-700">
                          {p.category}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right font-bold text-slate-900">
                        {formatINR(p.amount)}
                      </td>
                      <td className="px-4 py-3 text-slate-600">{formatDate(p.dueDate)}</td>
                      <td className="px-4 py-3">
                        {p.status === 'PAID' ? (
                          <span className="text-slate-400">Settled</span>
                        ) : (
                          <span
                            className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold border ${
                              isCritical
                                ? 'bg-rose-50 text-rose-700 border-rose-200'
                                : 'bg-slate-100 text-slate-700 border-slate-200'
                            }`}
                          >
                            {daysDiff === 0
                              ? 'Due Today'
                              : daysDiff === 1
                              ? 'Due Tomorrow'
                              : daysDiff < 0
                              ? `${Math.abs(daysDiff)} days overdue`
                              : `In ${daysDiff} days`}
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-block px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${
                            p.status === 'PAID'
                              ? 'bg-emerald-100 text-emerald-800 border-emerald-200'
                              : 'bg-amber-100 text-amber-800 border-amber-200'
                          }`}
                        >
                          {p.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center">
                        {p.status !== 'PAID' ? (
                          <button
                            onClick={() => handleReschedule(p)}
                            className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-semibold transition-all ${
                              isDone
                                ? 'bg-slate-900 text-white'
                                : 'bg-slate-100 text-slate-700 hover:bg-slate-200 border border-slate-300'
                            }`}
                          >
                            <Calendar className="w-3.5 h-3.5" />
                            <span>Request Extension</span>
                          </button>
                        ) : (
                          <span className="text-slate-400 text-xs">Completed</span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
