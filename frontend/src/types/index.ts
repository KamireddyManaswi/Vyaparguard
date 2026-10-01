export interface Business {
  id: number;
  name: string;
  openingCash: number;
  minimumCashThreshold: number;
}

export type TransactionType = 'CASH_IN' | 'CASH_OUT';
export type InvoiceStatus = 'PENDING' | 'PAID' | 'OVERDUE';
export type PaymentStatus = 'PENDING' | 'PAID' | 'OVERDUE';

export interface Transaction {
  id: number;
  businessId: number;
  date: string;
  type: TransactionType;
  amount: number;
  category: string;
  description: string;
}

export interface Invoice {
  id: number;
  businessId: number;
  invoiceNumber: string;
  customerName: string;
  amount: number;
  issueDate: string;
  dueDate: string;
  paidDate?: string | null;
  status: InvoiceStatus;
}

export interface PaymentObligation {
  id: number;
  businessId: number;
  supplierName: string;
  amount: number;
  dueDate: string;
  category: string;
  status: PaymentStatus;
}

export interface CashFlowPoint {
  date: string;
  label: string;
  inflow: number;
  outflow: number;
  netChange: number;
  balance: number;
  threshold: number;
  isProjected: boolean;
  isShortage: boolean;
}

export interface RecommendedAction {
  id: string;
  title: string;
  description: string;
  priority: 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW';
  actionType: 'FOLLOW_UP' | 'DELAY_PAYMENT' | 'REDUCE_EXPENSE' | 'INCENTIVE';
  impactAmount: number;
  targetEntity: string;
}

export interface ProposedAction {
  id: number;
  businessId: number;
  title: string;
  description: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  category: string;
  status: 'PROPOSED' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

export interface ScenarioComparison {
  targetCustomer: string;
  delayedInvoiceAmount: number;
  collectionDelayDays: number;
  baselineMinimumCash: number;
  scenarioMinimumCash: number;
  baselineEndingCash: number;
  scenarioEndingCash: number;
  shortageDate: string;
  shortageAmount: number;
  riskLevel: string;
}

export interface AgentQueryResponse {
  success: boolean;
  apiKeyMissing: boolean;
  message?: string;
  summary?: string;
  findings: string[];
  evidence: string[];
  risk?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  recommendedActions: string[];
  assumptions: string[];
  toolCallsExecuted: string[];
  scenarioComparison?: ScenarioComparison | null;
  createdActions: ProposedAction[];
}

export interface DashboardSummary {
  businessId: number;
  businessName: string;
  openingCash: number;
  minimumCashThreshold: number;
  currentCash: number;
  totalReceivables: number;
  overdueReceivables: number;
  upcomingPayments: number;
  sevenDayProjectedCash: number;
  minimumProjectedCash: number;
  shortageAmount: number;
  shortageDate?: string | null;
  hasRisk: boolean;
  riskMessage?: string | null;
  forecast7Days: CashFlowPoint[];
  overdueInvoices: Invoice[];
  upcomingPaymentList: PaymentObligation[];
  recommendedActions: RecommendedAction[];
}

export interface CashFlowResponse {
  currentCash: number;
  minimumCashThreshold: number;
  historical: CashFlowPoint[];
  forecast: CashFlowPoint[];
  combinedTimeline: CashFlowPoint[];
}

export interface CsvImportResult {
  success: boolean;
  importedCount: number;
  failedCount: number;
  errors: string[];
  message: string;
}

export interface AiChatResponse {
  answer: string;
  keyInsights: string[];
  suggestedFollowUps: string[];
  riskLevel: 'HIGH' | 'MEDIUM' | 'NORMAL' | 'LOW';
}

export interface ScenarioSimulationRequest {
  collectedInvoiceIds?: number[];
  delayedPaymentIds?: number[];
  delayDays?: number;
  additionalInflow?: number;
  expenseReductionPercent?: number;
}

export interface ScenarioSimulationResponse {
  originalMinCash: number;
  simulatedMinCash: number;
  cashDifference: number;
  originalRisk: boolean;
  simulatedRisk: boolean;
  outcomeSummary: string;
  simulatedShortageDate?: string | null;
  originalForecast: CashFlowPoint[];
  simulatedForecast: CashFlowPoint[];
}
