import React, { useState } from 'react';
import type { PaymentObligation } from '../types';
import { formatINR, formatDate, getDaysDifference } from '../utils/formatters';
import { Clock, Calendar, Check, ExternalLink } from 'lucide-react';

interface UpcomingPaymentsTableProps {
  payments: PaymentObligation[];
  onActionClick?: (payment: PaymentObligation) => void;
}

export const UpcomingPaymentsTable: React.FC<UpcomingPaymentsTableProps> = ({
  payments,
  onActionClick,
}) => {
  const [rescheduledId, setRescheduledId] = useState<number | null>(null);

  const handleAction = (p: PaymentObligation) => {
    setRescheduledId(p.id);
    if (onActionClick) {
      onActionClick(p);
    } else {
      setTimeout(() => setRescheduledId(null), 2500);
    }
  };

  if (payments.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-6 text-center shadow-sm">
        <div className="w-10 h-10 rounded-full bg-slate-100 text-slate-500 flex items-center justify-center mx-auto mb-2">
          <Clock className="w-5 h-5" />
        </div>
        <p className="text-sm font-semibold text-slate-800">No Upcoming Payments</p>
        <p className="text-xs text-slate-500 mt-1">
          No supplier obligations are scheduled in the immediate horizon.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="p-4 border-b border-slate-100 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-md bg-amber-50 text-amber-600">
            <Clock className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-900">Upcoming Supplier Payments</h3>
            <p className="text-xs text-slate-500">Scheduled trade payables &amp; business liabilities</p>
          </div>
        </div>
        <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-amber-100 text-amber-800">
          {payments.length} Scheduled
        </span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 text-slate-600 uppercase font-semibold border-b border-slate-200 tracking-wider">
            <tr>
              <th className="px-4 py-3">Supplier / Vendor</th>
              <th className="px-4 py-3">Category</th>
              <th className="px-4 py-3 text-right">Amount</th>
              <th className="px-4 py-3">Due Date</th>
              <th className="px-4 py-3">Timeline</th>
              <th className="px-4 py-3 text-center">Manage</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {payments.map((p) => {
              const daysDiff = getDaysDifference(p.dueDate);
              const isUrgent = daysDiff <= 3;
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
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-semibold border ${
                        isUrgent
                          ? 'bg-rose-50 text-rose-700 border-rose-200'
                          : 'bg-amber-50 text-amber-700 border-amber-200'
                      }`}
                    >
                      {daysDiff === 0
                        ? 'Due Today'
                        : daysDiff === 1
                        ? 'Due Tomorrow'
                        : `In ${daysDiff} days`}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center">
                    <button
                      onClick={() => handleAction(p)}
                      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-semibold transition-all ${
                        isDone
                          ? 'bg-slate-800 text-white'
                          : 'bg-slate-100 text-slate-700 hover:bg-slate-200 border border-slate-300'
                      }`}
                    >
                      {isDone ? (
                        <>
                          <Check className="w-3.5 h-3.5 text-emerald-400" />
                          <span>Simulated Delay</span>
                        </>
                      ) : (
                        <>
                          <Calendar className="w-3.5 h-3.5" />
                          <span>Request Extension</span>
                        </>
                      )}
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
