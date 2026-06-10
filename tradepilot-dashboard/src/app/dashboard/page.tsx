'use client';

import { useEffect, useState } from 'react';
import {
  ShoppingCart,
  TrendingUp,
  AlertCircle,
  Clock,
} from 'lucide-react';

interface Stats {
  totalOrders: number;
  quotedOrders: number;
  confirmedOrders: number;
  overduePayments: number;
  pendingFollowUps: number;
}

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats>({
    totalOrders: 0,
    quotedOrders: 0,
    confirmedOrders: 0,
    overduePayments: 0,
    pendingFollowUps: 0,
  });

  useEffect(() => {
    fetch('http://localhost:8080/api/dashboard/stats', {
      headers: { 'Accept': 'application/json' },
    })
      .then(res => res.json())
      .then(data => setStats(data))
      .catch(err => console.error('Stats fetch failed:', err));
  }, []);

  const cards = [
    {
      label: 'Total Orders',
      value: stats.totalOrders,
      icon: ShoppingCart,
      color: 'text-blue-400',
      bg: 'bg-blue-400/10',
    },
    {
      label: 'Quoted',
      value: stats.quotedOrders,
      icon: TrendingUp,
      color: 'text-emerald-400',
      bg: 'bg-emerald-400/10',
    },
    {
      label: 'Overdue Payments',
      value: stats.overduePayments,
      icon: AlertCircle,
      color: 'text-red-400',
      bg: 'bg-red-400/10',
    },
    {
      label: 'Pending Follow-ups',
      value: stats.pendingFollowUps,
      icon: Clock,
      color: 'text-amber-400',
      bg: 'bg-amber-400/10',
    },
  ];

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h2 className="text-2xl font-bold">Overview</h2>
          <p className="text-gray-500 text-sm mt-1">
            TradePilot AI — Live Dashboard
          </p>
        </div>
        <div className="flex items-center gap-2 text-sm">
          <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          <span className="text-emerald-400">Live</span>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 mb-8 lg:grid-cols-4">
        {cards.map(card => (
          <div
            key={card.label}
            className="bg-gray-900 border border-gray-800 rounded-xl p-6"
          >
            <div className={`inline-flex p-2 rounded-lg ${card.bg} mb-4`}>
              <card.icon size={20} className={card.color} />
            </div>
            <div className="text-3xl font-bold mb-1">{card.value}</div>
            <div className="text-sm text-gray-500">{card.label}</div>
          </div>
        ))}
      </div>

      <div className="bg-gray-900 border border-gray-800 rounded-xl p-6">
        <h3 className="font-semibold mb-4">Order Pipeline</h3>
        <div className="flex items-center gap-2 flex-wrap">
          {['INQUIRY', 'QUOTED', 'NEGOTIATING', 'CONFIRMED', 'DISPATCHED', 'DELIVERED'].map(
            (status, i, arr) => (
              <div key={status} className="flex items-center gap-2">
                <div className="text-xs px-3 py-1 rounded-full bg-gray-800 text-gray-300 border border-gray-700">
                  {status}
                </div>
                {i < arr.length - 1 && (
                  <div className="text-gray-700">→</div>
                )}
              </div>
            )
          )}
        </div>
      </div>
    </div>
  );
}