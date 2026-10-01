import type {
  DashboardSummary,
  CashFlowResponse,
  Invoice,
  PaymentObligation,
  CsvImportResult,
  AiChatResponse,
  ScenarioSimulationRequest,
  ScenarioSimulationResponse,
  AgentQueryResponse,
  ProposedAction
} from '../types';

const API_BASE = '/api';

export const api = {
  getDashboard: async (): Promise<DashboardSummary> => {
    const res = await fetch(`${API_BASE}/dashboard`);
    if (!res.ok) throw new Error('Failed to fetch dashboard summary');
    return res.json();
  },

  getCashFlow: async (): Promise<CashFlowResponse> => {
    const res = await fetch(`${API_BASE}/cashflow`);
    if (!res.ok) throw new Error('Failed to fetch cash flow data');
    return res.json();
  },

  getInvoices: async (): Promise<Invoice[]> => {
    const res = await fetch(`${API_BASE}/invoices`);
    if (!res.ok) throw new Error('Failed to fetch invoices');
    return res.json();
  },

  getOverdueInvoices: async (): Promise<Invoice[]> => {
    const res = await fetch(`${API_BASE}/invoices/overdue`);
    if (!res.ok) throw new Error('Failed to fetch overdue invoices');
    return res.json();
  },

  getUpcomingPayments: async (): Promise<PaymentObligation[]> => {
    const res = await fetch(`${API_BASE}/payments/upcoming`);
    if (!res.ok) throw new Error('Failed to fetch upcoming payments');
    return res.json();
  },

  getAllPayments: async (): Promise<PaymentObligation[]> => {
    const res = await fetch(`${API_BASE}/payments`);
    if (!res.ok) throw new Error('Failed to fetch payments');
    return res.json();
  },

  importTransactions: async (file: File): Promise<CsvImportResult> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`${API_BASE}/import/transactions`, {
      method: 'POST',
      body: formData,
    });
    if (!res.ok) throw new Error('Failed to import transactions');
    return res.json();
  },

  importInvoices: async (file: File): Promise<CsvImportResult> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`${API_BASE}/import/invoices`, {
      method: 'POST',
      body: formData,
    });
    if (!res.ok) throw new Error('Failed to import invoices');
    return res.json();
  },

  importPayments: async (file: File): Promise<CsvImportResult> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`${API_BASE}/import/payments`, {
      method: 'POST',
      body: formData,
    });
    if (!res.ok) throw new Error('Failed to import payments');
    return res.json();
  },

  // Legacy fallback
  askAi: async (question: string): Promise<AiChatResponse> => {
    const res = await fetch(`${API_BASE}/ai/ask`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question }),
    });
    if (!res.ok) throw new Error('Failed to get answer from AI advisor');
    return res.json();
  },

  simulateScenario: async (request: ScenarioSimulationRequest): Promise<ScenarioSimulationResponse> => {
    const res = await fetch(`${API_BASE}/scenarios/simulate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });
    if (!res.ok) throw new Error('Failed to simulate scenario');
    return res.json();
  },

  resetDemoData: async (): Promise<{ success: boolean; message: string; dashboard: DashboardSummary }> => {
    const res = await fetch(`${API_BASE}/demo/reset`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to reset demo data');
    return res.json();
  },

  // ============================================
  // UPGRADED AI AGENT API WITH BACKEND TOOLS
  // ============================================

  queryAgent: async (query: string, apiKey?: string): Promise<AgentQueryResponse> => {
    const res = await fetch(`${API_BASE}/agent/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query, apiKey }),
    });
    if (!res.ok) throw new Error('Failed to execute AI agent request');
    return res.json();
  },

  getAgentStatus: async (): Promise<{ apiKeyConfigured: boolean }> => {
    const res = await fetch(`${API_BASE}/agent/status`);
    if (!res.ok) throw new Error('Failed to fetch agent status');
    return res.json();
  },

  setAgentConfig: async (apiKey: string): Promise<{ apiKeyConfigured: boolean }> => {
    const res = await fetch(`${API_BASE}/agent/config`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ apiKey }),
    });
    if (!res.ok) throw new Error('Failed to save API key configuration');
    return res.json();
  },

  getProposedActions: async (): Promise<ProposedAction[]> => {
    const res = await fetch(`${API_BASE}/agent/actions`);
    if (!res.ok) throw new Error('Failed to fetch proposed actions');
    return res.json();
  },

  approveAction: async (id: number): Promise<ProposedAction> => {
    const res = await fetch(`${API_BASE}/agent/actions/${id}/approve`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to approve action');
    return res.json();
  },

  rejectAction: async (id: number): Promise<ProposedAction> => {
    const res = await fetch(`${API_BASE}/agent/actions/${id}/reject`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to reject action');
    return res.json();
  },

  // Direct Deterministic Tool APIs for Inspection & Verification
  testToolCashPosition: async (): Promise<any> => {
    const res = await fetch(`${API_BASE}/agent/tools/cash-position`);
    if (!res.ok) throw new Error('Failed to call get_cash_position tool');
    return res.json();
  },

  testToolOverdueInvoices: async (): Promise<any> => {
    const res = await fetch(`${API_BASE}/agent/tools/overdue-invoices`);
    if (!res.ok) throw new Error('Failed to call get_overdue_invoices tool');
    return res.json();
  },

  testToolUpcomingPayments: async (): Promise<any> => {
    const res = await fetch(`${API_BASE}/agent/tools/upcoming-payments`);
    if (!res.ok) throw new Error('Failed to call get_upcoming_payments tool');
    return res.json();
  },

  testToolRunScenario: async (params: { collectionDelayDays?: number; expenseIncreasePercent?: number; targetCustomer?: string }): Promise<any> => {
    const res = await fetch(`${API_BASE}/agent/tools/run-scenario`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(params),
    });
    if (!res.ok) throw new Error('Failed to call run_cashflow_scenario tool');
    return res.json();
  },

  testToolCreateAction: async (action: { title: string; description: string; priority: string; category?: string }): Promise<ProposedAction> => {
    const res = await fetch(`${API_BASE}/agent/tools/create-action`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(action),
    });
    if (!res.ok) throw new Error('Failed to call create_action tool');
    return res.json();
  }
};
