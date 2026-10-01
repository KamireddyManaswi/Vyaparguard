import React from 'react';
import { AlertTriangle, CheckCircle2, ArrowRight, Calendar, TrendingDown } from 'lucide-react';
import { formatINR, formatDate } from '../utils/formatters';

interface RiskAlertCardProps {
  hasRisk: boolean;
  shortageDate?: string | null;
  shortageAmount: number;
  minimumCashThreshold: number;
  minimumProjectedCash: number;
  onNavigateToScenarios?: () => void;
}

export const RiskAlertCard: React.FC<RiskAlertCardProps> = ({
  hasRisk,
  shortageDate,
  shortageAmount,
  minimumCashThreshold,
  minimumProjectedCash,
  onNavigateToScenarios,
}) => {
  if (!hasRisk) {
    return (
      <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-5 shadow-sm">
        <div className="flex items-start gap-3">
          <div className="p-2 bg-emerald-100 rounded-lg text-emerald-700">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-emerald-900">Cash Reserves Secure</h4>
            <p className="text-xs text-emerald-700 mt-1">
              Your projected 7-day minimum cash balance remains safely above the {formatINR(minimumCashThreshold)} safety threshold.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gradient-to-r from-rose-50 via-amber-50 to-orange-50 border-2 border-rose-300 rounded-xl p-5 shadow-md">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-start gap-3.5">
          <div className="p-3 bg-rose-500 rounded-xl text-white shadow-md shadow-rose-500/20 shrink-0">
            <AlertTriangle className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold uppercase tracking-wider text-rose-700 px-2 py-0.5 rounded bg-rose-100 border border-rose-200">
                Action Required
              </span>
              <h4 className="text-base font-bold text-slate-900">Cash Shortage Risk Detected</h4>
            </div>
            <p className="text-sm text-slate-700 mt-1.5 font-medium">
              Projected balance falls to{' '}
              <span className="text-rose-600 font-bold">{formatINR(minimumProjectedCash)}</span>, dipping{' '}
              <span className="text-rose-600 font-bold">{formatINR(shortageAmount)} below</span> your{' '}
              {formatINR(minimumCashThreshold)} safety threshold on{' '}
              <span className="font-bold underline decoration-rose-400 decoration-2">
                {formatDate(shortageDate)}
              </span>
              .
            </p>
            <div className="flex flex-wrap items-center gap-4 mt-2.5 text-xs text-slate-600">
              <span className="flex items-center gap-1 font-medium">
                <Calendar className="w-3.5 h-3.5 text-rose-500" />
                Shortage Date: <strong>{formatDate(shortageDate)}</strong>
              </span>
              <span className="flex items-center gap-1 font-medium">
                <TrendingDown className="w-3.5 h-3.5 text-rose-500" />
                Deficit Amount: <strong>{formatINR(shortageAmount)}</strong>
              </span>
            </div>
          </div>
        </div>

        {onNavigateToScenarios && (
          <button
            onClick={onNavigateToScenarios}
            className="self-start md:self-center flex items-center gap-2 px-4 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-bold shadow-sm transition-all shrink-0 hover:shadow-md"
          >
            <span>Simulate Fix in Scenarios</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
};
