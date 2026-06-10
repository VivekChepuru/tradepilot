'use client';

export default function ApprovalsPage() {
  return (
    <div className="p-8">
      <h2 className="text-2xl font-bold mb-2">Pending Approvals</h2>
      <p className="text-gray-500 text-sm">
        Messages that need your review before sending.
      </p>
      <div className="mt-8 bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
        <p className="text-gray-500">No pending approvals.</p>
      </div>
    </div>
  );
}