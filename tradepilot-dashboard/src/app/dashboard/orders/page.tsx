'use client';

import { useEffect, useState } from 'react';
import { Order } from '@/types';

const statusColors: Record<string, string> = {
  INQUIRY: 'bg-gray-700 text-gray-300',
  QUOTED: 'bg-blue-900 text-blue-300',
  NEGOTIATING: 'bg-amber-900 text-amber-300',
  CONFIRMED: 'bg-emerald-900 text-emerald-300',
  DISPATCHED: 'bg-purple-900 text-purple-300',
  DELIVERED: 'bg-green-900 text-green-300',
  CANCELLED: 'bg-red-900 text-red-300',
};

const paymentColors: Record<string, string> = {
  PENDING: 'bg-gray-700 text-gray-300',
  PARTIAL: 'bg-amber-900 text-amber-300',
  PAID: 'bg-emerald-900 text-emerald-300',
  OVERDUE: 'bg-red-900 text-red-300',
};

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
  fetch('http://localhost:8080/api/orders', {
    headers: {
      'Accept': 'application/json',
    },
  })
    .then(res => {
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return res.json();
    })
    .then(data => {
      setOrders(data);
      setLoading(false);
    })
    .catch(err => console.error('Stats fetch failed:', err));
}, []);

  return (
    <div className="p-8">
      <div className="mb-8">
        <h2 className="text-2xl font-bold">Orders</h2>
        <p className="text-gray-500 text-sm mt-1">
          {orders.length} total orders
        </p>
      </div>

      {loading ? (
        <div className="text-gray-500">Loading orders...</div>
      ) : orders.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <p className="text-gray-500">No orders yet.</p>
          <p className="text-gray-600 text-sm mt-2">
            Orders appear here when buyers send price inquiries.
          </p>
        </div>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-800 text-left text-xs text-gray-500 uppercase tracking-wider">
                <th className="px-6 py-4">Reference</th>
                <th className="px-6 py-4">Contact</th>
                <th className="px-6 py-4">Commodity</th>
                <th className="px-6 py-4">Quoted Price</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4">Payment</th>
                <th className="px-6 py-4">Created</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {orders.map(order => (
                <tr key={order.id} className="hover:bg-gray-800/50 transition-colors">
                  <td className="px-6 py-4 font-mono text-sm text-emerald-400">
                    {order.orderReference}
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm">
                      {order.tradeContact?.displayName || order.tradeContact?.whatsappNumber}
                    </div>
                    <div className="text-xs text-gray-500">
                      {order.tradeContact?.whatsappNumber}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm">{order.commodity}</div>
                    {order.grade && (
                      <div className="text-xs text-gray-500">{order.grade}</div>
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm">
                    {order.quotedPrice
                      ? `₹${order.quotedPrice.toLocaleString('en-IN')}/MT`
                      : '—'}
                  </td>
                  <td className="px-6 py-4">
                    <span className={`text-xs px-2 py-1 rounded-full ${statusColors[order.status]}`}>
                      {order.status}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`text-xs px-2 py-1 rounded-full ${paymentColors[order.paymentStatus]}`}>
                      {order.paymentStatus}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-xs text-gray-500">
                    {new Date(order.createdAt).toLocaleDateString('en-IN')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}