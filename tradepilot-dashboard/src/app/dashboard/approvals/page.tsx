'use client';

import { useEffect, useState } from 'react';
import { CheckCircle, XCircle } from 'lucide-react';

interface PendingApproval {
  whatsappMessageId: string;
  fromNumber: string;
  commodity: string | null;
  grade: string | null;
  requestedDiscountPercent: number | null;
  originalPrice: number | null;
  discountedPrice: number | null;
  routingDecision: string;
  processedAt: string;
}

export default function ApprovalsPage() {
  const [approvals, setApprovals] = useState<PendingApproval[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchApprovals = () => {
    fetch('http://localhost:8080/api/approvals')
      .then(res => res.json())
      .then(data => { setApprovals(data); setLoading(false); })
      .catch(() => setLoading(false));
  };

  useEffect(() => { fetchApprovals(); }, []);

  const approve = async (messageId: string) => {
    await fetch(`http://localhost:8080/api/approvals/${messageId}/approve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ discountPercent: 3.0 }),
    });
    fetchApprovals();
  };

  const reject = async (messageId: string) => {
    await fetch(`http://localhost:8080/api/approvals/${messageId}/reject`, {
      method: 'POST',
    });
    fetchApprovals();
  };

  return (
    <div className="p-8">
      <div className="mb-8">
        <h2 className="text-2xl font-bold">Pending Approvals</h2>
        <p className="text-gray-500 text-sm mt-1">
          {approvals.length} negotiation{approvals.length !== 1 ? 's' : ''} awaiting review
        </p>
      </div>

      {loading ? (
        <div className="text-gray-500">Loading...</div>
      ) : approvals.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <p className="text-gray-500">No pending approvals.</p>
          <p className="text-gray-600 text-sm mt-2">
            Negotiation requests appear here when a buyer asks for a 3–5% discount.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {approvals.map(approval => (
            <div
              key={approval.whatsappMessageId}
              className="bg-gray-900 border border-gray-800 rounded-xl p-6"
            >
              <div className="flex items-start justify-between mb-4">
                <div>
                  <div className="font-mono text-sm text-emerald-400 mb-1">
                    {approval.whatsappMessageId}
                  </div>
                  <div className="text-sm text-gray-300">{approval.fromNumber}</div>
                  <div className="text-xs text-gray-500 mt-1">
                    {new Date(approval.processedAt).toLocaleString('en-IN')}
                  </div>
                </div>
                <span className="text-xs px-2 py-1 rounded-full bg-amber-900 text-amber-300">
                  PENDING APPROVAL
                </span>
              </div>

              <div className="grid grid-cols-3 gap-4 mb-4 text-sm">
                <div>
                  <div className="text-gray-500 text-xs mb-1">Commodity</div>
                  <div>{approval.commodity || '—'} {approval.grade || ''}</div>
                </div>
                <div>
                  <div className="text-gray-500 text-xs mb-1">Original Price</div>
                  <div>
                    {approval.originalPrice
                      ? `₹${approval.originalPrice.toLocaleString('en-IN')}/MT`
                      : '—'}
                  </div>
                </div>
                <div>
                  <div className="text-gray-500 text-xs mb-1">Requested Discount</div>
                  <div className="text-amber-400">
                    {approval.requestedDiscountPercent
                      ? `${approval.requestedDiscountPercent}%`
                      : 'Not specified'}
                  </div>
                </div>
              </div>

              <div className="flex gap-3 pt-4 border-t border-gray-800">
                <button
                  onClick={() => approve(approval.whatsappMessageId)}
                  className="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-sm rounded-lg transition-colors"
                >
                  <CheckCircle size={16} />
                  Approve & Send
                </button>
                <button
                  onClick={() => reject(approval.whatsappMessageId)}
                  className="flex items-center gap-2 px-4 py-2 bg-red-900 hover:bg-red-800 text-red-300 text-sm rounded-lg transition-colors"
                >
                  <XCircle size={16} />
                  Reject
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
