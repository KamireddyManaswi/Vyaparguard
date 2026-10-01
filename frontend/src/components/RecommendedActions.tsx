import React from 'react';
import type { RecommendedAction } from '../types';
import { formatINR } from '../utils/formatters';
import {
  Sparkles,
  PhoneCall,
  Clock,
  Scissors,
  ArrowRight,
  TrendingUp,
  BadgeAlert
} from 'lucide-react';

interface RecommendedActionsProps {
  actions: RecommendedAction[];
  onExecuteAction?: (action: RecommendedAction) => void;
}

export const RecommendedActions: React.FC<RecommendedActionsProps> = ({
  actions,
  onExecuteAction,
}) => {
  const getActionIcon = (actionType: string) => {
    switch (actionType) {
      case 'FOLLOW_UP':
        return PhoneCall;
      case 'DELAY_PAYMENT':
        return Clock;
      case 'REDUCE_EXPENSE':
        return Scissors;
      default:
        return TrendingUp;
    }
  };

  const getPriorityClasses = (priority: string) => {
    switch (priority) {
      case 'URGENT':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'HIGH':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'MEDIUM':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2">
          <div className="p-1.5 rounded-lg bg-emerald-50 text-emerald-600">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-900">Recommended Cash Defense Actions</h3>
            <p className="text-xs text-slate-500">
              Deterministic actions to prevent balance breaching minimum threshold
            </p>
          </div>
        </div>
        <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">
          AI Rule Engine
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5 mt-4">
        {actions.map((act) => {
          const Icon = getActionIcon(act.actionType);

          return (
            <div
              key={act.id}
              className="flex flex-col justify-between p-4 rounded-xl border border-slate-200 bg-slate-50/50 hover:bg-white hover:border-slate-300 transition-all hover:shadow-sm"
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <div className="p-2 rounded-lg bg-white border border-slate-200 text-slate-800 shadow-2xs">
                    <Icon className="w-4 h-4 text-emerald-600" />
                  </div>
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${getPriorityClasses(
                      act.priority
                    )}`}
                  >
                    {act.priority}
                  </span>
                </div>

                <h4 className="text-xs font-bold text-slate-900 line-clamp-1">{act.title}</h4>
                <p className="text-xs text-slate-600 mt-1 line-clamp-3 leading-relaxed">
                  {act.description}
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-200/60 flex items-center justify-between">
                <div>
                  <span className="text-[10px] uppercase font-semibold text-slate-500 block">
                    Cash Impact
                  </span>
                  <span className="text-xs font-bold text-emerald-700">
                    +{formatINR(act.impactAmount)}
                  </span>
                </div>

                <button
                  onClick={() => onExecuteAction && onExecuteAction(act)}
                  className="flex items-center gap-1 px-2.5 py-1 bg-white hover:bg-slate-900 hover:text-white border border-slate-300 rounded-md text-xs font-medium text-slate-700 transition-all shadow-2xs"
                >
                  <span>Act</span>
                  <ArrowRight className="w-3 h-3" />
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
