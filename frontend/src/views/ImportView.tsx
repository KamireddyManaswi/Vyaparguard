import React, { useState } from 'react';
import { api } from '../services/api';
import type { CsvImportResult } from '../types';
import {
  UploadCloud,
  FileSpreadsheet,
  CheckCircle2,
  AlertCircle,
  Download,
  FileCheck,
  AlertTriangle
} from 'lucide-react';

interface ImportViewProps {
  onImportSuccess: () => void;
}

type ImportType = 'transactions' | 'invoices' | 'payments';

export const ImportView: React.FC<ImportViewProps> = ({ onImportSuccess }) => {
  const [activeType, setActiveType] = useState<ImportType>('transactions');
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<CsvImportResult | null>(null);

  const sampleCsvData: Record<ImportType, { filename: string; content: string }> = {
    transactions: {
      filename: 'sample_transactions.csv',
      content: `date,type,amount,category,description
2026-10-01,CASH_IN,15500.00,Daily Sales,Counter UPI receipts
2026-10-01,CASH_OUT,4500.00,Inventory,Fresh Milk & Bread stock
2026-09-30,CASH_IN,18200.00,Daily Sales,Evening grocery checkout
2026-09-30,CASH_OUT,8500.00,Utilities,Monthly Electricity Bill
2026-09-29,CASH_IN,22000.00,B2B Payment,Cheque clearance from Royal Hotel`,
    },
    invoices: {
      filename: 'sample_invoices.csv',
      content: `invoiceNumber,customerName,amount,issueDate,dueDate,status
INV-2026-110,Mehta General Stores,18500.00,2026-09-15,2026-09-28,OVERDUE
INV-2026-111,Sri Krishna Sweets,24000.00,2026-09-20,2026-10-04,PENDING
INV-2026-112,Modern Cafe & Provisions,12000.00,2026-09-22,2026-10-06,PENDING
INV-2026-113,Annapurna Bhojanalaya,16500.00,2026-09-10,2026-09-24,OVERDUE`,
    },
    payments: {
      filename: 'sample_payments.csv',
      content: `supplierName,amount,dueDate,category,status
ITC Agro Distribution Hub,45000.00,2026-10-04,FMCG Inventory,PENDING
Hindustan Unilever Wholesale,32000.00,2026-10-05,Packaged Goods,PENDING
Tata Tea & Coffee Agency,18500.00,2026-10-07,Beverages,PENDING
Commercial Shop Rent (Landlord),25000.00,2026-10-08,Rent,PENDING`,
    },
  };

  const downloadSample = (type: ImportType) => {
    const item = sampleCsvData[type];
    const blob = new Blob([item.content], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', item.filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
      setResult(null);
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    setLoading(true);
    setResult(null);

    try {
      let res: CsvImportResult;
      if (activeType === 'transactions') {
        res = await api.importTransactions(file);
      } else if (activeType === 'invoices') {
        res = await api.importInvoices(file);
      } else {
        res = await api.importPayments(file);
      }

      setResult(res);
      if (res.importedCount > 0) {
        onImportSuccess();
      }
    } catch (err: any) {
      setResult({
        success: false,
        importedCount: 0,
        failedCount: 1,
        errors: [err.message || 'Network error occurred while uploading file.'],
        message: 'Import failed.',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 pb-12 max-w-4xl mx-auto">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold text-slate-900">Data Import &amp; CSV Integration</h2>
        <p className="text-xs text-slate-500">
          Upload your existing ledger sheets, billing CSVs, or trade supplier obligations. All data is validated before updating financial forecasts.
        </p>
      </div>

      {/* Import Type Tabs */}
      <div className="flex border-b border-slate-200">
        {[
          { id: 'transactions', label: '1. Transactions CSV', desc: 'Actual cash inflows & outflows' },
          { id: 'invoices', label: '2. Customer Invoices CSV', desc: 'Debtor receivables & dues' },
          { id: 'payments', label: '3. Supplier Payments CSV', desc: 'Payables & upcoming obligations' },
        ].map((tab) => (
          <button
            key={tab.id}
            onClick={() => {
              setActiveType(tab.id as ImportType);
              setFile(null);
              setResult(null);
            }}
            className={`flex-1 py-3 px-4 text-left border-b-2 transition-all ${
              activeType === tab.id
                ? 'border-emerald-600 bg-emerald-50/30'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            <span
              className={`text-xs font-bold block ${
                activeType === tab.id ? 'text-emerald-800' : 'text-slate-700'
              }`}
            >
              {tab.label}
            </span>
            <span className="text-[11px] text-slate-500 block">{tab.desc}</span>
          </button>
        ))}
      </div>

      {/* Main Upload Box */}
      <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-50 p-4 rounded-xl border border-slate-200">
          <div>
            <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wide">
              Expected CSV Schema for {activeType.toUpperCase()}
            </h4>
            <p className="text-xs text-slate-600 mt-0.5">
              {activeType === 'transactions' && 'Columns: date, type (CASH_IN / CASH_OUT), amount, category, description'}
              {activeType === 'invoices' && 'Columns: invoiceNumber, customerName, amount, issueDate, dueDate, status'}
              {activeType === 'payments' && 'Columns: supplierName, amount, dueDate, category, status'}
            </p>
          </div>
          <button
            onClick={() => downloadSample(activeType)}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-white hover:bg-slate-100 text-slate-700 text-xs font-semibold rounded-lg border border-slate-300 transition-colors shrink-0"
          >
            <Download className="w-3.5 h-3.5 text-emerald-600" />
            <span>Download Sample CSV</span>
          </button>
        </div>

        {/* Drag & Drop Area */}
        <div className="border-2 border-dashed border-slate-300 hover:border-emerald-500 rounded-2xl p-8 text-center bg-slate-50/50 hover:bg-emerald-50/20 transition-all">
          <input
            type="file"
            id="csv-file-input"
            accept=".csv"
            onChange={handleFileChange}
            className="hidden"
          />
          <label htmlFor="csv-file-input" className="cursor-pointer block">
            <div className="w-12 h-12 rounded-full bg-white border border-slate-200 flex items-center justify-center mx-auto text-emerald-600 shadow-sm mb-3">
              <UploadCloud className="w-6 h-6" />
            </div>
            <p className="text-sm font-semibold text-slate-800">
              {file ? file.name : 'Click to browse or drop CSV file here'}
            </p>
            <p className="text-xs text-slate-500 mt-1">
              Supports UTF-8 CSV with header row up to 10MB
            </p>
          </label>
        </div>

        {file && (
          <div className="flex items-center justify-between bg-emerald-50 border border-emerald-200 p-3 rounded-xl">
            <div className="flex items-center gap-2">
              <FileCheck className="w-4 h-4 text-emerald-600" />
              <span className="text-xs font-medium text-emerald-900">{file.name}</span>
              <span className="text-[10px] text-emerald-700">({(file.size / 1024).toFixed(1)} KB)</span>
            </div>
            <button
              onClick={handleUpload}
              disabled={loading}
              className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold transition-all shadow-xs disabled:opacity-50"
            >
              {loading ? 'Validating & Importing...' : 'Import Data Now'}
            </button>
          </div>
        )}

        {/* Result & Validation Feedback */}
        {result && (
          <div
            className={`p-4 rounded-xl border space-y-3 ${
              result.failedCount === 0
                ? 'bg-emerald-50 border-emerald-300'
                : 'bg-amber-50 border-amber-300'
            }`}
          >
            <div className="flex items-start gap-3">
              <div
                className={`p-1.5 rounded-lg text-white ${
                  result.failedCount === 0 ? 'bg-emerald-600' : 'bg-amber-600'
                }`}
              >
                {result.failedCount === 0 ? (
                  <CheckCircle2 className="w-4 h-4" />
                ) : (
                  <AlertTriangle className="w-4 h-4" />
                )}
              </div>
              <div className="flex-1">
                <h4 className="text-xs font-bold text-slate-900">{result.message}</h4>
                <div className="flex items-center gap-4 mt-1 text-xs text-slate-700">
                  <span>
                    Successfully Imported: <strong>{result.importedCount} records</strong>
                  </span>
                  {result.failedCount > 0 && (
                    <span className="text-rose-600 font-semibold">
                      Validation Errors: <strong>{result.failedCount} records</strong>
                    </span>
                  )}
                </div>
              </div>
            </div>

            {/* Error List */}
            {result.errors.length > 0 && (
              <div className="bg-white/80 rounded-lg p-3 border border-rose-200 text-xs space-y-1">
                <span className="text-[11px] font-bold text-rose-700 block uppercase">
                  Validation Error Details:
                </span>
                <ul className="list-disc pl-5 space-y-0.5 text-slate-700">
                  {result.errors.map((err, idx) => (
                    <li key={idx}>{err}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
