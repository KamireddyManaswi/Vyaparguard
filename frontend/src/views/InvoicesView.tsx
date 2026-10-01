import React, { useState, useEffect } from 'react';
import type { Invoice } from '../types';
import { api } from '../services/api';
import { formatINR, formatDate, getDaysDifference } from '../utils/formatters';
import { Search, Filter, MessageSquare, Check, Plus, AlertCircle, CheckCircle2, Clock } from 'lucide-react';

interface InvoicesViewProps {
  onRefreshNeeded: () => void;
}

export const InvoicesView: React.FC<InvoicesViewProps> = ({ onRefreshNeeded }) => {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'OVERDUE' | 'PENDING' | 'PAID'>('ALL');
  const [remindedId, setRemindedId] = useState<number | null>(null);

  const fetchInvoices = async () => {
    try {
      setLoading(true);
      const data = await api.getInvoices();
      setInvoices(data);
    } catch (err) {
      console.error('Failed to load invoices:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoices();
  }, []);

  const handleSendReminder = (inv: Invoice) => {
    setRemindedId(inv.id);
    const msg = `Namaste ${inv.customerName}, this is a gentle payment reminder from Sri Lakshmi General Stores for Invoice #${inv.invoiceNumber} of ${formatINR(inv.amount)} which was due on ${formatDate(inv.dueDate)}. Please clear via UPI or NEFT. Thank you!`;
    const whatsappUrl = `https://wa.me/?text=${encodeURIComponent(msg)}`;
    window.open(whatsappUrl, '_blank');
    setTimeout(() => setRemindedId(null), 3000);
  };

  // Calculations
  const totalReceivables = invoices
    .filter((i) => i.status !== 'PAID')
    .reduce((acc, i) => acc + i.amount, 0);

  const totalOverdue = invoices
    .filter((i) => i.status === 'OVERDUE')
    .reduce((acc, i) => acc + i.amount, 0);

  const totalPaid = invoices
    .filter((i) => i.status === 'PAID')
    .reduce((acc, i) => acc + i.amount, 0);

  // Filtered list
  const filtered = invoices.filter((i) => {
    const matchesSearch =
      i.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      i.invoiceNumber.toLowerCase().includes(searchTerm.toLowerCase());

    if (!matchesSearch) return false;
    if (statusFilter === 'ALL') return true;
    return i.status === statusFilter;
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'OVERDUE':
        return 'bg-rose-100 text-rose-800 border-rose-200';
      case 'PENDING':
        return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'PAID':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      default:
        return 'bg-slate-100 text-slate-800 border-slate-200';
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header & Stats */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900">Customer Invoices &amp; Receivables</h2>
          <p className="text-xs text-slate-500">
            Track customer dues, overdue payments, and accelerate working capital collections.
          </p>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase">Outstanding Receivables</span>
            <div className="p-2 rounded-lg bg-blue-50 text-blue-600">
              <Clock className="w-4 h-4" />
            </div>
          </div>
          <h4 className="text-2xl font-bold text-slate-900 mt-2">{formatINR(totalReceivables)}</h4>
          <p className="text-xs text-slate-500 mt-1">Pending collection from all customers</p>
        </div>

        <div className="bg-white rounded-xl border border-rose-200 p-4 shadow-sm bg-rose-50/20">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-rose-600 uppercase">Total Overdue Dues</span>
            <div className="p-2 rounded-lg bg-rose-100 text-rose-600">
              <AlertCircle className="w-4 h-4" />
            </div>
          </div>
          <h4 className="text-2xl font-bold text-rose-600 mt-2">{formatINR(totalOverdue)}</h4>
          <p className="text-xs text-rose-700 mt-1">Directly impacting your 7-day cash safety</p>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500 uppercase">Total Settled (30d)</span>
            <div className="p-2 rounded-lg bg-emerald-50 text-emerald-600">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <h4 className="text-2xl font-bold text-slate-900 mt-2">{formatINR(totalPaid)}</h4>
          <p className="text-xs text-slate-500 mt-1">Already received into account</p>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search by customer or invoice #..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-xs rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto overflow-x-auto w-full sm:w-auto">
          {(['ALL', 'OVERDUE', 'PENDING', 'PAID'] as const).map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all whitespace-nowrap ${
                statusFilter === status
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {status === 'ALL' ? 'All Invoices' : status}
            </button>
          ))}
        </div>
      </div>

      {/* Invoices Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-12 text-center text-slate-500 text-xs">Loading invoices...</div>
        ) : filtered.length === 0 ? (
          <div className="p-12 text-center text-slate-500 text-xs">
            No invoices found matching the selected filter.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-600 uppercase font-semibold border-b border-slate-200 tracking-wider">
                <tr>
                  <th className="px-4 py-3">Invoice #</th>
                  <th className="px-4 py-3">Customer Name</th>
                  <th className="px-4 py-3 text-right">Amount</th>
                  <th className="px-4 py-3">Issue Date</th>
                  <th className="px-4 py-3">Due Date</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-center">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map((inv) => {
                  const daysDiff = getDaysDifference(inv.dueDate);
                  const isLate = inv.status === 'OVERDUE' || (inv.status === 'PENDING' && daysDiff < 0);
                  const isReminded = remindedId === inv.id;

                  return (
                    <tr key={inv.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3 font-semibold text-slate-800">{inv.invoiceNumber}</td>
                      <td className="px-4 py-3 font-medium text-slate-900">{inv.customerName}</td>
                      <td className="px-4 py-3 text-right font-bold text-slate-900">
                        {formatINR(inv.amount)}
                      </td>
                      <td className="px-4 py-3 text-slate-600">{formatDate(inv.issueDate)}</td>
                      <td className="px-4 py-3 text-slate-600">
                        <div>{formatDate(inv.dueDate)}</div>
                        {isLate && (
                          <span className="text-[10px] text-rose-600 font-semibold">
                            {Math.abs(daysDiff)} days late
                          </span>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-block px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${getStatusBadge(
                            inv.status
                          )}`}
                        >
                          {inv.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center">
                        {inv.status !== 'PAID' ? (
                          <button
                            onClick={() => handleSendReminder(inv)}
                            className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-md text-xs font-semibold transition-all ${
                              isReminded
                                ? 'bg-emerald-600 text-white'
                                : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200'
                            }`}
                          >
                            {isReminded ? (
                              <>
                                <Check className="w-3.5 h-3.5" />
                                <span>Sent</span>
                              </>
                            ) : (
                              <>
                                <MessageSquare className="w-3.5 h-3.5" />
                                <span>WhatsApp Reminder</span>
                              </>
                            )}
                          </button>
                        ) : (
                          <span className="text-slate-400 text-xs">Settled</span>
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
