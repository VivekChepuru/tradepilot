'use client';

import { useEffect, useState } from 'react';

interface FollowUpJob {
  id: number;
  jobType: string;
  messageTemplate: string;
  scheduledAt: string;
  executedAt: string | null;
  status: string;
  attemptCount: number;
}

const statusColors: Record<string, string> = {
  PENDING: 'bg-amber-900 text-amber-300',
  SENT: 'bg-emerald-900 text-emerald-300',
  FAILED: 'bg-red-900 text-red-300',
  CANCELLED: 'bg-gray-700 text-gray-400',
};

export default function FollowUpsPage() {
  const [jobs, setJobs] = useState<FollowUpJob[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('http://localhost:8080/api/follow-ups')
      .then(res => res.json())
      .then(data => { setJobs(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  return (
    <div className="p-8">
      <h2 className="text-2xl font-bold mb-2">Follow-up Jobs</h2>
      <p className="text-gray-500 text-sm mb-8">{jobs.length} total jobs</p>

      {loading ? (
        <div className="text-gray-500">Loading...</div>
      ) : jobs.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <p className="text-gray-500">No follow-up jobs yet.</p>
        </div>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-800 text-left text-xs text-gray-500 uppercase tracking-wider">
                <th className="px-6 py-4">Type</th>
                <th className="px-6 py-4">Template</th>
                <th className="px-6 py-4">Scheduled</th>
                <th className="px-6 py-4">Executed</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4">Attempts</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {jobs.map(job => (
                <tr key={job.id} className="hover:bg-gray-800/50 transition-colors">
                  <td className="px-6 py-4 text-sm">{job.jobType}</td>
                  <td className="px-6 py-4 text-sm text-gray-400">{job.messageTemplate}</td>
                  <td className="px-6 py-4 text-xs text-gray-400">
                    {new Date(job.scheduledAt).toLocaleString('en-IN')}
                  </td>
                  <td className="px-6 py-4 text-xs text-gray-400">
                    {job.executedAt
                      ? new Date(job.executedAt).toLocaleString('en-IN')
                      : '—'}
                  </td>
                  <td className="px-6 py-4">
                    <span className={`text-xs px-2 py-1 rounded-full ${statusColors[job.status]}`}>
                      {job.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-center">{job.attemptCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}