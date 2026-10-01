import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import type { DashboardSummary, AgentQueryResponse, ProposedAction, ScenarioComparison } from '../types';
import { formatINR } from '../utils/formatters';
import {
  Bot,
  Send,
  Sparkles,
  AlertTriangle,
  CheckCircle2,
  Key,
  ShieldAlert,
  ArrowRight,
  Check,
  X,
  HelpCircle,
  Clock,
  Layers,
  SlidersHorizontal,
  ChevronDown,
  ChevronUp
} from 'lucide-react';

interface AiAgentViewProps {
  summary: DashboardSummary;
}

interface ChatMessage {
  id: string;
  sender: 'user' | 'agent';
  queryText?: string;
  response?: AgentQueryResponse;
  rawText?: string;
  timestamp: string;
}

export const AiAgentView: React.FC<AiAgentViewProps> = ({ summary }) => {
  const [apiKey, setApiKey] = useState<string>(() => localStorage.getItem('vyapar_guard_api_key') || '');
  const [showKeyModal, setShowKeyModal] = useState<boolean>(false);
  const [keyConfigured, setKeyConfigured] = useState<boolean>(false);
  const [inputQuery, setInputQuery] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [chatHistory, setChatHistory] = useState<ChatMessage[]>([]);
  const [actionsList, setActionsList] = useState<ProposedAction[]>([]);
  const [toolInspectorOpen, setToolInspectorOpen] = useState<boolean>(false);
  const [toolOutput, setToolOutput] = useState<{ name: string; data: any } | null>(null);

  useEffect(() => {
    checkStatus();
    loadActions();
  }, []);

  const checkStatus = async () => {
    try {
      const res = await api.getAgentStatus();
      setKeyConfigured(res.apiKeyConfigured || Boolean(apiKey));
    } catch (e) {
      console.error(e);
    }
  };

  const loadActions = async () => {
    try {
      const actions = await api.getProposedActions();
      setActionsList(actions);
    } catch (e) {
      console.error(e);
    }
  };

  const handleSaveKey = async (newKey: string) => {
    const trimmed = newKey.trim();
    setApiKey(trimmed);
    localStorage.setItem('vyapar_guard_api_key', trimmed);
    try {
      await api.setAgentConfig(trimmed);
      setKeyConfigured(true);
      setShowKeyModal(false);
    } catch (e) {
      console.error(e);
    }
  };

  const handleApprove = async (id: number) => {
    try {
      const updated = await api.approveAction(id);
      setActionsList((prev) => prev.map((a) => (a.id === id ? updated : a)));
    } catch (e) {
      console.error(e);
    }
  };

  const handleReject = async (id: number) => {
    try {
      const updated = await api.rejectAction(id);
      setActionsList((prev) => prev.map((a) => (a.id === id ? updated : a)));
    } catch (e) {
      console.error(e);
    }
  };

  const handleSend = async (queryText?: string) => {
    const q = (queryText || inputQuery).trim();
    if (!q || loading) return;

    const userMsg: ChatMessage = {
      id: `u-${Date.now()}`,
      sender: 'user',
      queryText: q,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setChatHistory((prev) => [...prev, userMsg]);
    if (!queryText) setInputQuery('');
    setLoading(true);

    try {
      const res = await api.queryAgent(q, apiKey || undefined);
      const agentMsg: ChatMessage = {
        id: `a-${Date.now()}`,
        sender: 'agent',
        response: res,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setChatHistory((prev) => [...prev, agentMsg]);
      if (res.createdActions && res.createdActions.length > 0) {
        loadActions();
      }
    } catch (err: any) {
      setChatHistory((prev) => [
        ...prev,
        {
          id: `a-err-${Date.now()}`,
          sender: 'agent',
          rawText: err.message || 'Error communicating with AI Agent.',
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  // Direct tool testing functions
  const testTool = async (toolName: string) => {
    setLoading(true);
    try {
      let data: any = null;
      if (toolName === 'get_cash_position') {
        data = await api.testToolCashPosition();
      } else if (toolName === 'get_overdue_invoices') {
        data = await api.testToolOverdueInvoices();
      } else if (toolName === 'get_upcoming_payments') {
        data = await api.testToolUpcomingPayments();
      } else if (toolName === 'run_cashflow_scenario') {
        data = await api.testToolRunScenario({ targetCustomer: 'ABC Traders', collectionDelayDays: 5 });
      } else if (toolName === 'create_action') {
        data = await api.testToolCreateAction({
          title: 'Follow up with ABC Traders regarding ₹30,000 overdue invoice',
          description: 'Payment is 5 days late. Send immediate WhatsApp statement.',
          priority: 'HIGH',
          category: 'RECEIVABLES',
        });
        loadActions();
      }
      setToolOutput({ name: toolName, data });
    } catch (e: any) {
      setToolOutput({ name: toolName, data: { error: e.message } });
    } finally {
      setLoading(false);
    }
  };

  const getRiskBadgeClasses = (risk?: string) => {
    switch (risk?.toUpperCase()) {
      case 'CRITICAL':
      case 'HIGH':
        return 'bg-rose-100 text-rose-800 border-rose-300';
      case 'MEDIUM':
        return 'bg-amber-100 text-amber-800 border-amber-300';
      default:
        return 'bg-emerald-100 text-emerald-800 border-emerald-300';
    }
  };

  const suggestedQuestions = [
    'Will I have enough cash next week?',
    'Which invoices are overdue?',
    'What is causing my cash-flow risk?',
    'What happens if ABC Traders pays 5 days late?',
    'What should I do today?',
  ];

  return (
    <div className="space-y-6 pb-12 max-w-5xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-200 pb-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <span>Ask VyaparGuard</span>
            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800">
              Tool-Equipped Agent
            </span>
          </h2>
          <p className="text-xs text-slate-500 mt-0.5">
            Agentic cash-flow intelligence powered by deterministic Java backend tools.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowKeyModal(true)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold border transition-all ${
              keyConfigured
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100'
                : 'bg-amber-50 text-amber-800 border-amber-300 hover:bg-amber-100'
            }`}
          >
            <Key className="w-3.5 h-3.5" />
            <span>{keyConfigured ? 'LLM API Key Active' : 'Configure LLM Key'}</span>
          </button>

          <button
            onClick={() => setToolInspectorOpen(!toolInspectorOpen)}
            className="flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-semibold bg-white border border-slate-300 text-slate-700 hover:bg-slate-50 transition-colors"
          >
            <Layers className="w-3.5 h-3.5 text-slate-500" />
            <span>Deterministic Tools</span>
            {toolInspectorOpen ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
          </button>
        </div>
      </div>

      {/* Missing Key Banner */}
      {!keyConfigured && (
        <div className="bg-amber-50 border border-amber-300 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-xs">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg bg-amber-100 text-amber-800">
              <AlertTriangle className="w-4 h-4" />
            </div>
            <div>
              <p className="text-xs font-bold text-amber-900">
                AI assistant requires an LLM API key. Financial dashboard remains available.
              </p>
              <p className="text-[11px] text-amber-700 mt-0.5">
                Add your Gemini or OpenAI API key to execute live reasoning, or inspect the 5 backend tools directly below.
              </p>
            </div>
          </div>
          <button
            onClick={() => setShowKeyModal(true)}
            className="px-3.5 py-1.5 bg-amber-800 hover:bg-amber-900 text-white rounded-lg text-xs font-semibold whitespace-nowrap transition-colors self-start sm:self-auto"
          >
            Enter API Key
          </button>
        </div>
      )}

      {/* Deterministic Tools Inspector Drawer */}
      {toolInspectorOpen && (
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm space-y-3">
          <div className="flex items-center justify-between border-b border-slate-100 pb-2">
            <div>
              <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wide">
                Direct Backend Tools (Pure Java Execution)
              </h4>
              <p className="text-[11px] text-slate-500">
                These exact tools are invoked by the Agent to retrieve deterministic numbers.
              </p>
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => testTool('get_cash_position')}
              className="px-2.5 py-1.5 rounded-lg text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-200"
            >
              1. get_cash_position()
            </button>
            <button
              onClick={() => testTool('get_overdue_invoices')}
              className="px-2.5 py-1.5 rounded-lg text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-200"
            >
              2. get_overdue_invoices()
            </button>
            <button
              onClick={() => testTool('get_upcoming_payments')}
              className="px-2.5 py-1.5 rounded-lg text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-200"
            >
              3. get_upcoming_payments()
            </button>
            <button
              onClick={() => testTool('run_cashflow_scenario')}
              className="px-2.5 py-1.5 rounded-lg text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-200"
            >
              4. run_cashflow_scenario("ABC Traders", 5)
            </button>
            <button
              onClick={() => testTool('create_action')}
              className="px-2.5 py-1.5 rounded-lg text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-200"
            >
              5. create_action()
            </button>
          </div>

          {toolOutput && (
            <div className="mt-3 bg-slate-900 text-slate-100 p-3 rounded-lg text-xs font-mono overflow-x-auto max-h-56">
              <div className="text-emerald-400 font-bold mb-1">// Tool Output: {toolOutput.name}()</div>
              <pre>{JSON.stringify(toolOutput.data, null, 2)}</pre>
            </div>
          )}
        </div>
      )}

      {/* Suggested Questions */}
      <div>
        <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
          Suggested Questions
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2">
          {suggestedQuestions.map((q, i) => (
            <button
              key={i}
              onClick={() => handleSend(q)}
              disabled={loading}
              className="flex items-center justify-between p-3 bg-white rounded-xl border border-slate-200 hover:border-emerald-300 hover:bg-emerald-50/30 text-left text-xs font-medium text-slate-700 transition-all shadow-2xs group"
            >
              <span>{q}</span>
              <Sparkles className="w-3.5 h-3.5 text-emerald-600 group-hover:scale-110 transition-transform shrink-0" />
            </button>
          ))}
        </div>
      </div>

      {/* Chat Display Area */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden flex flex-col min-h-[460px]">
        <div className="p-4 sm:p-6 space-y-6 flex-1 overflow-y-auto">
          {chatHistory.length === 0 ? (
            <div className="py-16 text-center text-slate-500 space-y-2">
              <div className="w-12 h-12 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto mb-2">
                <Bot className="w-6 h-6" />
              </div>
              <p className="text-sm font-bold text-slate-800">Ask any question to the VyaparGuard AI Agent</p>
              <p className="text-xs text-slate-500 max-w-md mx-auto">
                The agent will understand your intent, invoke deterministic backend tools, reason over verified financials, and recommend concrete actions.
              </p>
            </div>
          ) : (
            chatHistory.map((msg) => {
              const isUser = msg.sender === 'user';
              const resp = msg.response;

              return (
                <div key={msg.id} className={`flex gap-3 ${isUser ? 'flex-row-reverse' : ''}`}>
                  {/* Avatar */}
                  <div
                    className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
                      isUser
                        ? 'bg-slate-900 text-white'
                        : 'bg-gradient-to-tr from-emerald-600 to-teal-500 text-white'
                    }`}
                  >
                    {isUser ? <span className="text-xs font-bold">You</span> : <Bot className="w-4 h-4" />}
                  </div>

                  {/* Message Content */}
                  <div className={`max-w-[90%] space-y-3 ${isUser ? 'text-right' : 'text-left'}`}>
                    {isUser ? (
                      <div className="inline-block px-4 py-2.5 rounded-2xl text-xs font-semibold bg-slate-900 text-white">
                        {msg.queryText}
                      </div>
                    ) : resp ? (
                      <div className="space-y-4">
                        {/* High-Level Tool Activity Badges */}
                        {resp.toolCallsExecuted && resp.toolCallsExecuted.length > 0 && (
                          <div className="flex flex-wrap items-center gap-1.5">
                            {resp.toolCallsExecuted.map((tc, idx) => (
                              <span
                                key={idx}
                                className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-[11px] font-semibold bg-emerald-50 text-emerald-800 border border-emerald-200"
                              >
                                <Check className="w-3 h-3 text-emerald-600" />
                                <span>{tc}</span>
                              </span>
                            ))}
                          </div>
                        )}

                        {/* Missing Key Notification */}
                        {resp.apiKeyMissing && (
                          <div className="bg-amber-50 border border-amber-300 rounded-xl p-4 text-xs text-amber-900 font-medium">
                            {resp.message}
                          </div>
                        )}

                        {/* Summary & Risk Header */}
                        {resp.summary && (
                          <div className="bg-slate-50 border border-slate-200 rounded-xl p-4 space-y-2">
                            <div className="flex items-center justify-between">
                              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                                Agent Summary
                              </span>
                              {resp.risk && (
                                <span
                                  className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase border ${getRiskBadgeClasses(
                                    resp.risk
                                  )}`}
                                >
                                  Risk: {resp.risk}
                                </span>
                              )}
                            </div>
                            <p className="text-xs text-slate-800 font-medium leading-relaxed">
                              {resp.summary}
                            </p>
                          </div>
                        )}

                        {/* Scenario Comparison Card (BASELINE vs 5-DAY DELAY) */}
                        {resp.scenarioComparison && (
                          <div className="bg-white rounded-xl border border-slate-300 p-4 shadow-sm space-y-3">
                            <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                              <h4 className="text-xs font-bold text-slate-900 flex items-center gap-1.5">
                                <SlidersHorizontal className="w-3.5 h-3.5 text-purple-600" />
                                <span>Scenario Comparison: Baseline vs. {resp.scenarioComparison.collectionDelayDays}-Day Delay</span>
                              </h4>
                              <span className="text-[11px] font-semibold text-slate-500">
                                Target: {resp.scenarioComparison.targetCustomer}
                              </span>
                            </div>

                            <div className="overflow-x-auto">
                              <table className="w-full text-xs text-left">
                                <thead className="bg-slate-50 text-slate-600 uppercase font-semibold text-[10px]">
                                  <tr>
                                    <th className="px-3 py-2">Metric</th>
                                    <th className="px-3 py-2 text-right">Baseline</th>
                                    <th className="px-3 py-2 text-right">With 5-Day Delay</th>
                                    <th className="px-3 py-2 text-center">Status</th>
                                  </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100 font-medium">
                                  <tr>
                                    <td className="px-3 py-2 text-slate-600">Ending Cash (Day 7)</td>
                                    <td className="px-3 py-2 text-right">{formatINR(resp.scenarioComparison.baselineEndingCash)}</td>
                                    <td className="px-3 py-2 text-right text-rose-600 font-bold">{formatINR(resp.scenarioComparison.scenarioEndingCash)}</td>
                                    <td className="px-3 py-2 text-center text-rose-600 font-semibold">Delayed</td>
                                  </tr>
                                  <tr>
                                    <td className="px-3 py-2 text-slate-600">Minimum Cash Balance</td>
                                    <td className="px-3 py-2 text-right">{formatINR(resp.scenarioComparison.baselineMinimumCash)}</td>
                                    <td className="px-3 py-2 text-right font-bold text-rose-600">{formatINR(resp.scenarioComparison.scenarioMinimumCash)}</td>
                                    <td className="px-3 py-2 text-center text-rose-600 font-semibold">Critical</td>
                                  </tr>
                                  <tr>
                                    <td className="px-3 py-2 text-slate-600">Shortage Amount</td>
                                    <td className="px-3 py-2 text-right">{formatINR(resp.scenarioComparison.shortageAmount)}</td>
                                    <td className="px-3 py-2 text-right font-bold text-rose-600">{formatINR(resp.scenarioComparison.shortageAmount)}</td>
                                    <td className="px-3 py-2 text-center text-rose-600 font-semibold">Below ₹50,000</td>
                                  </tr>
                                  <tr>
                                    <td className="px-3 py-2 text-slate-600">Risk Level</td>
                                    <td className="px-3 py-2 text-right">HIGH</td>
                                    <td className="px-3 py-2 text-right font-bold text-rose-700">{resp.scenarioComparison.riskLevel}</td>
                                    <td className="px-3 py-2 text-center">
                                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-100 text-rose-800">
                                        Action Required
                                      </span>
                                    </td>
                                  </tr>
                                </tbody>
                              </table>
                            </div>
                          </div>
                        )}

                        {/* Findings & Evidence Grid */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                          {resp.findings && resp.findings.length > 0 && (
                            <div className="bg-white rounded-xl border border-slate-200 p-3.5 space-y-2">
                              <span className="text-[11px] font-bold text-slate-900 uppercase tracking-wide block">
                                Findings
                              </span>
                              <ul className="space-y-1.5 text-xs text-slate-700">
                                {resp.findings.map((f, i) => (
                                  <li key={i} className="flex items-start gap-1.5">
                                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 mt-1.5 shrink-0"></span>
                                    <span>{f}</span>
                                  </li>
                                ))}
                              </ul>
                            </div>
                          )}

                          {resp.evidence && resp.evidence.length > 0 && (
                            <div className="bg-white rounded-xl border border-slate-200 p-3.5 space-y-2">
                              <span className="text-[11px] font-bold text-slate-900 uppercase tracking-wide block">
                                Evidence (Deterministic Backend Data)
                              </span>
                              <ul className="space-y-1.5 text-xs text-slate-700">
                                {resp.evidence.map((e, i) => (
                                  <li key={i} className="flex items-start gap-1.5 font-mono text-[11px]">
                                    <span className="text-emerald-600 font-bold shrink-0">#</span>
                                    <span>{e}</span>
                                  </li>
                                ))}
                              </ul>
                            </div>
                          )}
                        </div>

                        {/* Recommended Actions */}
                        {resp.recommendedActions && resp.recommendedActions.length > 0 && (
                          <div className="bg-emerald-50/50 border border-emerald-200 rounded-xl p-3.5 space-y-2">
                            <span className="text-[11px] font-bold text-emerald-900 uppercase tracking-wide block">
                              Recommended Actions
                            </span>
                            <ul className="space-y-1 text-xs text-emerald-900">
                              {resp.recommendedActions.map((act, i) => (
                                <li key={i} className="flex items-center gap-2">
                                  <ArrowRight className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                                  <span>{act}</span>
                                </li>
                              ))}
                            </ul>
                          </div>
                        )}

                        {/* Assumptions */}
                        {resp.assumptions && resp.assumptions.length > 0 && (
                          <div className="text-[11px] text-slate-500 italic">
                            <span>Assumptions: </span>
                            {resp.assumptions.join(' • ')}
                          </div>
                        )}
                      </div>
                    ) : (
                      <div className="bg-slate-100 rounded-xl p-3 text-xs text-slate-700">
                        {msg.rawText}
                      </div>
                    )}
                  </div>
                </div>
              );
            })
          )}

          {loading && (
            <div className="flex gap-3">
              <div className="w-8 h-8 rounded-full bg-emerald-600 text-white flex items-center justify-center animate-spin">
                <Bot className="w-4 h-4" />
              </div>
              <div className="bg-slate-100 rounded-2xl p-3 text-xs text-slate-600 animate-pulse">
                Understanding intent &amp; executing deterministic backend tools...
              </div>
            </div>
          )}
        </div>

        {/* Input Bar */}
        <div className="p-3 border-t border-slate-200 bg-slate-50 flex items-center gap-2">
          <input
            type="text"
            placeholder="Ask about cash shortages, invoice follow-ups, customer delay impacts..."
            value={inputQuery}
            onChange={(e) => setInputQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSend()}
            className="flex-1 px-4 py-2.5 text-xs bg-white rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
          <button
            onClick={() => handleSend()}
            disabled={loading || !inputQuery.trim()}
            className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white text-xs font-bold rounded-xl transition-all shadow-xs flex items-center gap-1.5"
          >
            <span>Ask</span>
            <Send className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Action Plan Section (Stored in Database with Approve / Reject) */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <div>
            <h3 className="text-sm font-bold text-slate-900">
              Agent Proposed Action Plan (Database Stored)
            </h3>
            <p className="text-xs text-slate-500">
              Actions generated by the agent during "What should I do today?". Approve or reject directly without sending external messages.
            </p>
          </div>
          <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">
            {actionsList.length} Proposed Actions
          </span>
        </div>

        {actionsList.length === 0 ? (
          <div className="py-8 text-center text-slate-500 text-xs">
            No proposed actions generated yet. Ask the agent <strong>"What should I do today?"</strong> to create actionable tasks.
          </div>
        ) : (
          <div className="space-y-3">
            {actionsList.map((action) => (
              <div
                key={action.id}
                className="flex flex-col sm:flex-row sm:items-center justify-between p-3.5 rounded-xl border border-slate-200 bg-slate-50/50 hover:bg-white transition-all gap-3"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${
                        action.priority === 'HIGH'
                          ? 'bg-rose-50 text-rose-700 border-rose-200'
                          : action.priority === 'MEDIUM'
                          ? 'bg-amber-50 text-amber-700 border-amber-200'
                          : 'bg-blue-50 text-blue-700 border-blue-200'
                      }`}
                    >
                      {action.priority}
                    </span>
                    <h5 className="text-xs font-bold text-slate-900">{action.title}</h5>
                  </div>
                  <p className="text-xs text-slate-600">{action.description}</p>
                </div>

                <div className="flex items-center gap-2 shrink-0 self-end sm:self-auto">
                  {action.status === 'PROPOSED' ? (
                    <>
                      <button
                        onClick={() => handleApprove(action.id)}
                        className="flex items-center gap-1 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold transition-colors shadow-2xs"
                      >
                        <Check className="w-3.5 h-3.5" />
                        <span>Approve</span>
                      </button>
                      <button
                        onClick={() => handleReject(action.id)}
                        className="flex items-center gap-1 px-3 py-1.5 bg-white hover:bg-rose-50 text-rose-700 border border-rose-300 rounded-lg text-xs font-semibold transition-colors"
                      >
                        <X className="w-3.5 h-3.5" />
                        <span>Reject</span>
                      </button>
                    </>
                  ) : (
                    <span
                      className={`px-3 py-1 rounded-lg text-xs font-bold ${
                        action.status === 'APPROVED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-rose-100 text-rose-800'
                      }`}
                    >
                      {action.status}
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* API Key Modal */}
      {showKeyModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                <Key className="w-4 h-4 text-emerald-600" />
                <span>Configure LLM API Key</span>
              </h3>
              <button
                onClick={() => setShowKeyModal(false)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-xs text-slate-600">
              VyaparGuard AI Agent uses LLM function calling with deterministic backend tools. Provide a Google Gemini or OpenAI API Key:
            </p>

            <div className="space-y-1.5">
              <label className="text-[11px] font-bold text-slate-700 uppercase">
                Gemini or OpenAI API Key
              </label>
              <input
                type="password"
                placeholder="AIzaSy... or sk-..."
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                className="w-full px-3.5 py-2 text-xs rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-emerald-500 font-mono"
              />
            </div>

            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                onClick={() => setShowKeyModal(false)}
                className="px-3.5 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                onClick={() => handleSaveKey(apiKey)}
                className="px-4 py-1.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg transition-colors shadow-xs"
              >
                Save &amp; Activate
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
