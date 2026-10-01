import React from 'react';
import type { DashboardSummary, CashFlowResponse, Invoice, PaymentObligation, RecommendedAction } from '../types';
import { MetricCard } from '../components/MetricCard';
import { CashFlowChart } from '../components/CashFlowChart';
import { RiskAlertCard } from '../components/RiskAlertCard';
import { OverdueInvoicesTable } from '../components/OverdueInvoicesTable';
import { UpcomingPaymentsTable } from '../components/UpcomingPaymentsTable';
import { RecommendedActions } from '../components/RecommendedActions';
import { Wallet, TrendingUp, TrendingDown, ShieldAlert, ArrowUpRight } from 'lucide-react';
import { formatINR } from '../utils/formatters';

interface DashboardViewProps {
  summary: DashboardSummary;
  cashFlowData: CashFlowResponse | null;
  onNavigateTab: (tab: string) => void;
  onExecuteAction: (action: RecommendedAction) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  summary,
  cashFlowData,
  onNavigateTab,
  onExecuteAction,
}) => {
  const chartPoints = cashFlowData?.combinedTimeline || summary.forecast7Days;

  return (
    <div className="space-y-6 pb-12">
      {/* Risk Alert Card */}
      <RiskAlertCard
        hasRisk={summary.hasRisk}
        shortageDate={summary.shortageDate}
        shortageAmount={summary.shortageAmount}
        minimumCashThreshold={summary.minimumCashThreshold}
        minimumProjectedCash={summary.minimumProjectedCash}
        onNavigateToScenarios={() => onNavigateTab('scenarios')}
      />

      {/* 4 Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* 1. Current Cash Card */}
        <MetricCard
          title="Current Cash"
          amount={summary.currentCash}
          subtitle={`Safety threshold: ${formatINR(summary.minimumCashThreshold)}`}
          icon={Wallet}
          badgeText="Verified Bank Cash"
          badgeType="success"
        />

        {/* 2. Receivables Card */}
        <MetricCard
          title="Receivables"
          amount={summary.totalReceivables}
          subtitle={`${formatINR(summary.overdueReceivables)} overdue (${summary.overdueInvoices.length} invoices)`}
          icon={TrendingUp}
          badgeText={summary.overdueReceivables > 0 ? `${formatINR(summary.overdueReceivables)} Overdue` : 'On Schedule'}
          badgeType={summary.overdueReceivables > 0 ? 'danger' : 'neutral'}
        />

        {/* 3. Upcoming Payments Card */}
        <MetricCard
          title="Upcoming Payments"
          amount={summary.upcomingPayments}
          subtitle={`${summary.upcomingPaymentList.length} obligations due in horizon`}
          icon={TrendingDown}
          badgeText="Supplier Liabilities"
          badgeType="warning"
        />

        {/* 4. Projected 7-Day Cash Card */}
        <MetricCard
          title="Projected 7-Day Cash"
          amount={summary.minimumProjectedCash}
          subtitle={
            summary.hasRisk
              ? `Deficit of ${formatINR(summary.shortageAmount)} on ${summary.shortageDate || 'Day 4'}`
              : 'Safe above safety buffer'
          }
          icon={ShieldAlert}
          alert={summary.hasRisk}
          badgeText={summary.hasRisk ? 'Shortage Alert' : 'Healthy Buffer'}
          badgeType={summary.hasRisk ? 'danger' : 'success'}
        />
      </div>

      {/* Cash Flow Chart */}
      <CashFlowChart data={chartPoints} threshold={summary.minimumCashThreshold} />

      {/* Recommended Actions */}
      <RecommendedActions
        actions={summary.recommendedActions}
        onExecuteAction={onExecuteAction}
      />

      {/* Two-Column Tables Section: Overdue Invoices & Upcoming Payments */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div>
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Priority Receivables
            </span>
            <button
              onClick={() => onNavigateTab('invoices')}
              className="text-xs font-semibold text-emerald-600 hover:text-emerald-700 flex items-center gap-1"
            >
              <span>View All Invoices</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>
          <OverdueInvoicesTable invoices={summary.overdueInvoices} />
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Upcoming Liabilities
            </span>
            <button
              onClick={() => onNavigateTab('payments')}
              className="text-xs font-semibold text-emerald-600 hover:text-emerald-700 flex items-center gap-1"
            >
              <span>View All Payments</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </button>
          </div>
          <UpcomingPaymentsTable payments={summary.upcomingPaymentList} />
        </div>
      </div>
    </div>
  );
};
