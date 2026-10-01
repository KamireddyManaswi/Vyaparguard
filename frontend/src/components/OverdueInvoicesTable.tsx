import React, { useState } from 'react';
import type { Invoice } from '../types';
import { formatINR, formatDate, getDaysDifference } from '../utils/formatters';
import { MessageSquare, PhoneCall, AlertCircle, Check } from 'lucide-react';

interface OverdueInvoicesTableProps {
  invoices: Invoice[];
  onActionClick?: (invoice: Invoice) => void;
}

export const OverdueInvoicesTable: React.FC<OverdueInvoicesTableProps> = ({
  invoices,
  onActionClick,
}) => {
  const [remindedId, setRemindedId] = useState<number | null>(null);

  const handleReminder = (inv: Invoice) => {
    setRemindedId(inv.id);
    if (onActionClick) {
      onActionClick(inv);
    } else {
      setTimeout(() => setRemindedId(null), 2500);
    }
  };

  if (invoices.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-6 text-center shadow-sm">
        <div className="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto mb-2">
          <Check className="w-5 h-5" />
        </div>
        <p className="text-sm font-semibold text-slate-800">No Overdue Invoices</p>
        <p className="text-xs text-slate-500 mt-1">
          All your customer payments are currently on schedule.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="p-4 border-b border-slate-100 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-md bg-rose-50 text-rose-600">
            <AlertCircle className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-900">Overdue Customer Invoices</h3>
            <p className="text-xs text-slate-500">Uncollected receipts causing working capital strain</p>
          </div>
        </div>
        <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-rose-100 text-rose-800">
          {invoices.length} Pending Collections
        </span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 text-slate-600 uppercase font-semibold border-b border-slate-200 tracking-wider">
            <tr>
              <th className="px-4 py-3">Invoice #</th>
              <th className="px-4 py-3">Customer Party</th>
              <th className="px-4 py-3 text-right">Amount</th>
              <th className="px-4 py-3">Due Date</th>
              <th className="px-4 py-3">Delay</th>
              <th className="px-4 py-3 text-center">Follow-Up Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {invoices.map((inv) => {
              const daysDiff = Math.abs(getDaysDifference(inv.dueDate));
              const isReminded = remindedId === inv.id;

              return (
                <tr key={inv.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="px-4 py-3 font-semibold text-slate-800">{inv.invoiceNumber}</td>
                  <td className="px-4 py-3 font-medium text-slate-900">{inv.customerName}</td>
                  <td className="px-4 py-3 text-right font-bold text-slate-900">
                    {formatINR(inv.amount)}
                  </td>
                  <td className="px-4 py-3 text-slate-600">{formatDate(inv.dueDate)}</td>
                  <td className="px-4 py-3">
                    <span className="inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-semibold bg-rose-50 text-rose-700 border border-rose-200">
                      {daysDiff} days late
                    </span>
                  </td>
                  <td className="px-4 py-3 text-center">
                    <button
                      onClick={() => handleReminder(inv)}
                      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-semibold transition-all ${
                        isReminded
                          ? 'bg-emerald-600 text-white'
                          : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200'
                      }`}
                    >
                      {isReminded ? (
                        <>
                          <Check className="w-3.5 h-3.5" />
                          <span>Reminder Sent</span>
                        </>
                      ) : (
                        <>
                          <MessageSquare className="w-3.5 h-3.5" />
                          <span>Send WhatsApp</span>
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
