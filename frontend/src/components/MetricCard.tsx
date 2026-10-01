import React from 'react';
import type { LucideIcon } from 'lucide-react';
import { formatINR } from '../utils/formatters';

interface MetricCardProps {
  title: string;
  amount: number;
  subtitle?: string;
  icon: LucideIcon;
  badgeText?: string;
  badgeType?: 'danger' | 'warning' | 'success' | 'neutral';
  alert?: boolean;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  amount,
  subtitle,
  icon: Icon,
  badgeText,
  badgeType = 'neutral',
  alert = false,
}) => {
  const getBadgeClasses = () => {
    switch (badgeType) {
      case 'danger':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'warning':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'success':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      default:
        return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  };

  const getIconWrapperClasses = () => {
    if (alert) return 'bg-rose-100 text-rose-600';
    if (badgeType === 'success') return 'bg-emerald-100 text-emerald-600';
    if (badgeType === 'warning') return 'bg-amber-100 text-amber-600';
    return 'bg-slate-100 text-slate-700';
  };

  return (
    <div
      className={`bg-white rounded-xl border p-5 transition-all shadow-sm hover:shadow-md ${
        alert ? 'border-rose-300 ring-1 ring-rose-200' : 'border-slate-200'
      }`}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{title}</p>
          <h3 className={`text-2xl font-bold mt-1 tracking-tight ${alert ? 'text-rose-600' : 'text-slate-900'}`}>
            {formatINR(amount)}
          </h3>
        </div>
        <div className={`p-2.5 rounded-xl ${getIconWrapperClasses()}`}>
          <Icon className="w-5 h-5" />
        </div>
      </div>

      <div className="mt-3 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
        <span className="text-slate-500 truncate">{subtitle}</span>
        {badgeText && (
          <span className={`px-2 py-0.5 rounded-md font-semibold border ${getBadgeClasses()}`}>
            {badgeText}
          </span>
        )}
      </div>
    </div>
  );
};
