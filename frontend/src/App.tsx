import React, { useState, useEffect } from 'react';
import { api } from './services/api';
import type { DashboardSummary, CashFlowResponse, RecommendedAction } from './types';
import { Navbar } from './components/Navbar';
import { DashboardView } from './views/DashboardView';
import { InvoicesView } from './views/InvoicesView';
import { PaymentsView } from './views/PaymentsView';
import { AiAgentView } from './views/AiAgentView';
import { ScenariosView } from './views/ScenariosView';
import { ImportView } from './views/ImportView';
import { Loader2, AlertCircle } from 'lucide-react';

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<string>('dashboard');
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [cashFlowData, setCashFlowData] = useState<CashFlowResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [dashRes, flowRes] = await Promise.all([
        api.getDashboard(),
        api.getCashFlow(),
      ]);
      setSummary(dashRes);
      setCashFlowData(flowRes);
    } catch (err: any) {
      console.error('Failed to load dashboard data:', err);
      setError(err.message || 'Unable to connect to VyaparGuard backend server.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleExecuteAction = (action: RecommendedAction) => {
    if (action.actionType === 'FOLLOW_UP') {
      setCurrentTab('invoices');
    } else if (action.actionType === 'DELAY_PAYMENT') {
      setCurrentTab('payments');
    } else {
      setCurrentTab('scenarios');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      {/* Top Navigation */}
      <Navbar
        currentTab={currentTab}
        setCurrentTab={setCurrentTab}
        businessName={summary?.businessName || 'Sri Lakshmi General Stores'}
        onDataRefresh={loadData}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {loading && !summary ? (
          <div className="flex flex-col items-center justify-center py-24 space-y-4">
            <Loader2 className="w-8 h-8 text-emerald-600 animate-spin" />
            <p className="text-sm font-medium text-slate-600">
              Loading financial intelligence from database...
            </p>
          </div>
        ) : error && !summary ? (
          <div className="bg-rose-50 border border-rose-200 rounded-xl p-8 text-center max-w-lg mx-auto my-12">
            <div className="w-12 h-12 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center mx-auto mb-3">
              <AlertCircle className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-rose-900">Backend Connection Error</h3>
            <p className="text-xs text-rose-700 mt-1">{error}</p>
            <button
              onClick={loadData}
              className="mt-4 px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-bold transition-all shadow-sm"
            >
              Retry Connection
            </button>
          </div>
        ) : summary ? (
          <>
            {currentTab === 'dashboard' && (
              <DashboardView
                summary={summary}
                cashFlowData={cashFlowData}
                onNavigateTab={setCurrentTab}
                onExecuteAction={handleExecuteAction}
              />
            )}

            {currentTab === 'invoices' && (
              <InvoicesView onRefreshNeeded={loadData} />
            )}

            {currentTab === 'payments' && (
              <PaymentsView onRefreshNeeded={loadData} />
            )}

            {currentTab === 'ai-agent' && (
              <AiAgentView summary={summary} />
            )}

            {currentTab === 'scenarios' && (
              <ScenariosView summary={summary} />
            )}

            {currentTab === 'import' && (
              <ImportView onImportSuccess={loadData} />
            )}
          </>
        ) : null}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-4 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>
            <strong>VyaparGuard MVP</strong> — Know your cash. Act before the shortage.
          </span>
          <span className="text-[11px] text-slate-400">
            Automated Indian Small Business Cash Flow Risk Intelligence
          </span>
        </div>
      </footer>
    </div>
  );
};

export default App;
