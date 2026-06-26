'use client';

import { ReactNode } from 'react';
import Link from 'next/link';
import {
  LayoutDashboard,
  MessageSquare,
  ShoppingCart,
  Clock,
  Bell,
  AlertCircle
} from 'lucide-react';

export default function DashboardLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex h-screen bg-gray-950 text-white">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-900 border-r border-gray-800 flex flex-col">
        {/* Logo */}
        <div className="p-6 border-b border-gray-800">
          <h1 className="text-xl font-bold text-emerald-400">TradePilot</h1>
          <p className="text-xs text-gray-500 mt-1">AI Trade Platform</p>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-1">
          <Link
            href="/dashboard"
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
          >
            <LayoutDashboard size={18} />
            <span>Overview</span>
          </Link>
          <Link
            href="/dashboard/orders"
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
          >
            <ShoppingCart size={18} />
            <span>Orders</span>
          </Link>
          <Link
            href="/dashboard/approvals"
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
          >
            <Bell size={18} />
            <span>Approvals</span>
          </Link>
          <Link
            href="/dashboard/follow-ups"
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
          >
            <Clock size={18} />
            <span>Follow-ups</span>
          </Link>
          <Link
            href="/dashboard/overdue"
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
          >
            <AlertCircle size={18} />
            <span>Overdue</span>
          </Link>
          <Link
            href="/dashboard/inbox"
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
          >
            <MessageSquare size={18} />
            <span>Inbox</span>
          </Link>
        </nav>

        {/* Status */}
        <div className="p-4 border-t border-gray-800">
          <div className="flex items-center gap-2 text-xs text-gray-500">
            <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>System Online</span>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        {children}
      </main>
    </div>
  );
}