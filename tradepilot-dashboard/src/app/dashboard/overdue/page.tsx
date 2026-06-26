'use client';

import { useEffect, useState } from 'react';
import { AlertCircle, Send, CheckCircle } from 'lucide-react';

interface OverdueFlag {
  id: number;
  status: string;
  flaggedAt: string;
  lastManualReminderAt: string | null;
  lastReminderTone: string | null;
  order: {
    id: number;
    orderReference: string;
    commodity: string;
    grade: string | null;
    quotedPrice: number | null;
    totalAmount: number | null;
    paymentStatus: string;
    tradeContact: {
      whatsappNumber: string;
      displayName: string | null;
    };
  };
}

export default function OverduePage() {
  const [flags, setFlags] = useState<OverdueFlag[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedTone, setSelectedTone] = useState<Record<number, string>>({});

  const fetchFlags = () => {
    fetch('http://localhost:8080/api/payments/overdue', {
      headers: { 'Accept': 'application/json' },
    })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((data: OverdueFlag[]) => {
        setFlags(data);
        setSelectedTone(prev => {
          const next: Record<number, string> = {};
          data.forEach(f => {
            next[f.id] = prev[f.id] ?? 'POLITE';
          });
          return next;
        });
        setLoading(false);
      })
      .catch(err => console.error('Overdue fetch failed:', err));
  };

  useEffect(() => {
    fetchFlags();
  }, []);

  const sendReminder = async (flagId: number) => {
    try {
      const res = await fetch(
        `http://localhost:8080/api/payments/overdue/${flagId}/remind`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ tone: selectedTone[flagId] }),
        }
      );
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchFlags();
    } catch (err) {
      console.error('Send reminder failed:', err);
    }
  };

  const markAsPaid = async (flagId: number) => {
    try {
      const res = await fetch(
        `http://localhost:8080/api/payments/overdue/${flagId}/paid`,
        { method: 'POST' }
      );
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchFlags();
    } catch (err) {
      console.error('Mark as paid failed:', err);
    }
  };

  const openFlags = flags.filter(f => f.status === 'OPEN');

  return (
    <div className="p-8">
      <div className="mb-8">
        <h2 className="text-2xl font-bold">Overdue Payments</h2>
        <p className="text-gray-500 text-sm mt-1">
          {openFlags.length} open overdue {openFlags.length === 1 ? 'flag' : 'flags'}
        </p>
      </div>

      {loading ? (
        <div className="text-gray-500">Loading overdue flags...</div>
      ) : openFlags.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <p className="text-gray-500">No overdue payments.</p>
          <p className="text-gray-600 text-sm mt-2">
            Overdue payment flags will appear here for follow-up.
          </p>
        </div>
      ) : (
        openFlags.map(flag => (
          <div
            key={flag.id}
            className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-4"
          >
            {/* Top row */}
            <div className="flex items-center gap-4 mb-3">
              <span className="font-mono text-emerald-400 text-sm">
                {flag.order.orderReference}
              </span>
              <span className="text-gray-400 text-sm">
                {flag.order.tradeContact.whatsappNumber}
              </span>
              <span className="text-gray-500 text-xs ml-auto">
                Flagged: {new Date(flag.flaggedAt).toLocaleDateString('en-IN')}
              </span>
            </div>

            {/* Second row */}
            <div className="flex items-center gap-3 mb-3">
              <span className="text-sm text-white">
                {flag.order.commodity}
                {flag.order.grade && (
                  <span className="text-gray-400"> — {flag.order.grade}</span>
                )}
              </span>
              {flag.order.quotedPrice !== null && (
                <span className="text-sm text-gray-300">
                  ₹{flag.order.quotedPrice.toLocaleString('en-IN')}/MT
                </span>
              )}
              <span className="text-xs px-2 py-1 rounded-full bg-red-900 text-red-300 ml-auto">
                OVERDUE
              </span>
            </div>

            {/* Last reminder info */}
            <div className="mb-4 text-xs text-gray-500">
              {flag.lastManualReminderAt ? (
                <>
                  Last reminded:{' '}
                  {new Date(flag.lastManualReminderAt).toLocaleDateString('en-IN')}
                  {flag.lastReminderTone && ` (${flag.lastReminderTone})`}
                </>
              ) : (
                'No reminder sent yet'
              )}
            </div>

            {/* Action row */}
            <div className="flex items-center gap-3">
              <select
                value={selectedTone[flag.id] ?? 'POLITE'}
                onChange={e =>
                  setSelectedTone(prev => ({ ...prev, [flag.id]: e.target.value }))
                }
                className="bg-gray-800 border border-gray-700 text-white rounded-lg px-3 py-2 text-sm"
              >
                <option value="POLITE">POLITE</option>
                <option value="FIRM">FIRM</option>
                <option value="LEGAL_WARNING">LEGAL_WARNING</option>
              </select>

              <button
                onClick={() => sendReminder(flag.id)}
                className="flex items-center gap-2 bg-blue-700 hover:bg-blue-600 border border-gray-700 text-white rounded-lg px-3 py-2 text-sm transition-colors"
              >
                <Send size={14} />
                Send Reminder
              </button>

              <button
                onClick={() => markAsPaid(flag.id)}
                className="flex items-center gap-2 bg-emerald-700 hover:bg-emerald-600 border border-gray-700 text-white rounded-lg px-3 py-2 text-sm transition-colors"
              >
                <CheckCircle size={14} />
                Mark as Paid
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
