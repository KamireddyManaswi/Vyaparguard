import React, { useState } from 'react';
import {
  ShieldAlert,
  LayoutDashboard,
  FileText,
  CreditCard,
  Bot,
  SlidersHorizontal,
  UploadCloud,
  RotateCcw,
  Store
} from 'lucide-react';
import { api } from '../services/api';

interface NavbarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  businessName: string;
  onDataRefresh: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTab,
  setCurrentTab,
  businessName,
  onDataRefresh
}) => {
  const [resetting, setResetting] = useState(false);

  const handleResetDemo = async () => {
    if (confirm('Reset to initial demo data (Sri Lakshmi General Stores with 7-day risk scenario)?')) {
      try {
        setResetting(true);
        await api.resetDemoData();
        onDataRefresh();
      } catch (err) {
        console.error('Error resetting demo:', err);
        alert('Failed to reset demo data.');
      } finally {
        setResetting(false);
      }
    }
  };

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'invoices', label: 'Invoices', icon: FileText },
    { id: 'payments', label: 'Payments', icon: CreditCard },
    { id: 'ai-agent', label: 'AI Agent', icon: Bot },
    { id: 'scenarios', label: 'Scenarios', icon: SlidersHorizontal },
    { id: 'import', label: 'Import', icon: UploadCloud },
  ];

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Tagline */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center text-white shadow-md shadow-emerald-500/20">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xl font-bold tracking-tight text-slate-900">
                  Vyapar<span className="text-emerald-600">Guard</span>
                </span>
                <span className="text-[10px] font-semibold tracking-wider uppercase px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800">
                  MVP
                </span>
              </div>
              <p className="text-xs text-slate-500 hidden sm:block">
                Know your cash. Act before the shortage.
              </p>
            </div>
          </div>

          {/* Business Badge & Reset Demo */}
          <div className="flex items-center gap-3">
            <div className="hidden md:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-50 border border-slate-200 text-xs font-medium text-slate-700">
              <Store className="w-4 h-4 text-emerald-600" />
              <span>{businessName || 'Sri Lakshmi General Stores'}</span>
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            </div>

            <button
              onClick={handleResetDemo}
              disabled={resetting}
              title="Reset database to hackathon demo dataset"
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-50 hover:bg-emerald-100 text-emerald-700 border border-emerald-200 transition-all disabled:opacity-50"
            >
              <RotateCcw className={`w-3.5 h-3.5 ${resetting ? 'animate-spin' : ''}`} />
              <span>{resetting ? 'Resetting...' : 'Load Demo Data'}</span>
            </button>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="flex space-x-1 sm:space-x-4 border-t border-slate-100 py-1 overflow-x-auto scrollbar-none">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setCurrentTab(item.id)}
                className={`flex items-center gap-2 px-3 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition-colors ${
                  isActive
                    ? 'bg-slate-900 text-white shadow-sm'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-emerald-400' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>
      </div>
    </header>
  );
};
