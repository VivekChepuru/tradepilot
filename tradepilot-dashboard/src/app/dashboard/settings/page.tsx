'use client';

import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';

interface NegotiationSettings {
  maxAutoDiscountPercent: number;
  maxEscalateDiscountPercent: number;
  isNegotiationEnabled: boolean;
}

interface NegotiationOverride {
  id: number;
  commodity: string;
  maxAutoDiscountPercent: number;
  maxEscalateDiscountPercent: number;
  isNegotiationEnabled: boolean;
}

const inputClass =
  'bg-gray-800 border border-gray-700 text-white rounded px-2 py-1 text-sm';

export default function SettingsPage() {
  const [settings, setSettings] = useState<NegotiationSettings | null>(null);
  const [overrides, setOverrides] = useState<NegotiationOverride[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showAddOverride, setShowAddOverride] = useState(false);
  const [newOverride, setNewOverride] = useState<{
    commodity: string;
    maxAutoDiscountPercent: number;
    maxEscalateDiscountPercent: number;
    isNegotiationEnabled: boolean;
  }>({
    commodity: '',
    maxAutoDiscountPercent: 2,
    maxEscalateDiscountPercent: 5,
    isNegotiationEnabled: true,
  });

  const fetchAll = () => {
    Promise.all([
      fetch('http://localhost:8080/api/negotiation-settings', {
        headers: { Accept: 'application/json' },
      }).then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      }),
      fetch('http://localhost:8080/api/negotiation-settings/overrides', {
        headers: { Accept: 'application/json' },
      }).then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      }),
    ])
      .then(([settingsData, overridesData]) => {
        setSettings(settingsData);
        setOverrides(overridesData);
        setLoading(false);
      })
      .catch(err => console.error('Settings fetch failed:', err));
  };

  useEffect(() => {
    fetchAll();
  }, []);

  const saveSettings = async () => {
    if (!settings) return;
    setSaving(true);
    try {
      const res = await fetch('http://localhost:8080/api/negotiation-settings', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(settings),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      alert('Settings saved');
    } catch (err) {
      console.error('Save settings failed:', err);
    } finally {
      setSaving(false);
    }
  };

  const addOverride = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/negotiation-settings/overrides', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newOverride),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchAll();
      setShowAddOverride(false);
    } catch (err) {
      console.error('Add override failed:', err);
    }
  };

  const deleteOverride = async (id: number) => {
    if (!window.confirm('Delete this override?')) return;
    try {
      const res = await fetch(
        `http://localhost:8080/api/negotiation-settings/overrides/${id}`,
        { method: 'DELETE' }
      );
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchAll();
    } catch (err) {
      console.error('Delete override failed:', err);
    }
  };

  if (loading || !settings) {
    return (
      <div className="p-8">
        <div className="text-gray-500">Loading settings...</div>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-8">
        <h2 className="text-2xl font-bold">Settings</h2>
      </div>

      {/* Global Negotiation Settings */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-8">
        <h3 className="text-lg font-semibold">Global Negotiation Settings</h3>
        <p className="text-gray-500 text-sm mt-1 mb-6">
          Applied to all commodities unless overridden
        </p>

        <div className="flex items-center gap-8 mb-6">
          <div>
            <label className="text-xs text-gray-500 block mb-1">Auto-approve up to</label>
            <div className="flex items-center gap-2">
              <input
                type="number"
                className={`${inputClass} w-20`}
                value={settings.maxAutoDiscountPercent}
                onChange={e =>
                  setSettings({
                    ...settings,
                    maxAutoDiscountPercent: Number(e.target.value),
                  })
                }
              />
              <span className="text-sm text-gray-400">%</span>
            </div>
          </div>

          <div>
            <label className="text-xs text-gray-500 block mb-1">Escalate above</label>
            <div className="flex items-center gap-2">
              <input
                type="number"
                className={`${inputClass} w-20`}
                value={settings.maxEscalateDiscountPercent}
                onChange={e =>
                  setSettings({
                    ...settings,
                    maxEscalateDiscountPercent: Number(e.target.value),
                  })
                }
              />
              <span className="text-sm text-gray-400">%</span>
            </div>
          </div>

          <div>
            <label className="text-xs text-gray-500 block mb-1">Negotiation enabled</label>
            <button
              onClick={() =>
                setSettings({
                  ...settings,
                  isNegotiationEnabled: !settings.isNegotiationEnabled,
                })
              }
              className={`text-xs px-3 py-1.5 rounded-full font-medium ${
                settings.isNegotiationEnabled
                  ? 'bg-emerald-900 text-emerald-300'
                  : 'bg-red-900 text-red-300'
              }`}
            >
              {settings.isNegotiationEnabled ? 'ON' : 'OFF'}
            </button>
          </div>
        </div>

        <div className="flex justify-end">
          <button
            onClick={saveSettings}
            disabled={saving}
            className="bg-emerald-700 hover:bg-emerald-600 disabled:opacity-50 text-white rounded-lg px-4 py-2 text-sm transition-colors"
          >
            {saving ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>

      {/* Per-Commodity Overrides */}
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold">Commodity Overrides</h3>
          <p className="text-gray-500 text-sm mt-1">
            Override global settings for specific commodities
          </p>
        </div>
        <button
          onClick={() => setShowAddOverride(!showAddOverride)}
          className="flex items-center gap-2 bg-emerald-700 hover:bg-emerald-600 text-white rounded-lg px-4 py-2 text-sm transition-colors"
        >
          <Plus size={16} />
          Add Override
        </button>
      </div>

      {showAddOverride && (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-6">
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label className="text-xs text-gray-500 block mb-1">Commodity</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newOverride.commodity}
                onChange={e => setNewOverride({ ...newOverride, commodity: e.target.value })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Auto %</label>
              <input
                type="number"
                className={`${inputClass} w-full`}
                value={newOverride.maxAutoDiscountPercent}
                onChange={e =>
                  setNewOverride({
                    ...newOverride,
                    maxAutoDiscountPercent: Number(e.target.value),
                  })
                }
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Escalate %</label>
              <input
                type="number"
                className={`${inputClass} w-full`}
                value={newOverride.maxEscalateDiscountPercent}
                onChange={e =>
                  setNewOverride({
                    ...newOverride,
                    maxEscalateDiscountPercent: Number(e.target.value),
                  })
                }
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Enabled</label>
              <button
                onClick={() =>
                  setNewOverride({
                    ...newOverride,
                    isNegotiationEnabled: !newOverride.isNegotiationEnabled,
                  })
                }
                className={`text-xs px-3 py-1.5 rounded-full font-medium ${
                  newOverride.isNegotiationEnabled
                    ? 'bg-emerald-900 text-emerald-300'
                    : 'bg-red-900 text-red-300'
                }`}
              >
                {newOverride.isNegotiationEnabled ? 'ON' : 'OFF'}
              </button>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={addOverride}
              className="bg-emerald-700 hover:bg-emerald-600 text-white rounded-lg px-4 py-2 text-sm transition-colors"
            >
              Save
            </button>
            <button
              onClick={() => setShowAddOverride(false)}
              className="bg-gray-800 hover:bg-gray-700 border border-gray-700 text-white rounded-lg px-4 py-2 text-sm transition-colors"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {overrides.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <p className="text-gray-500">
            No overrides — all commodities use global settings.
          </p>
        </div>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-800 text-left text-xs text-gray-500 uppercase tracking-wider">
                <th className="px-6 py-4">Commodity</th>
                <th className="px-6 py-4">Auto%</th>
                <th className="px-6 py-4">Escalate%</th>
                <th className="px-6 py-4">Enabled</th>
                <th className="px-6 py-4">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {overrides.map(override => (
                <tr key={override.id} className="hover:bg-gray-800/50 transition-colors">
                  <td className="px-6 py-4 text-sm">{override.commodity}</td>
                  <td className="px-6 py-4 text-sm">{override.maxAutoDiscountPercent}%</td>
                  <td className="px-6 py-4 text-sm">{override.maxEscalateDiscountPercent}%</td>
                  <td className="px-6 py-4">
                    <span
                      className={`text-xs px-2 py-1 rounded-full ${
                        override.isNegotiationEnabled
                          ? 'bg-emerald-900 text-emerald-300'
                          : 'bg-gray-700 text-gray-400'
                      }`}
                    >
                      {override.isNegotiationEnabled ? 'ON' : 'OFF'}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <button
                      onClick={() => deleteOverride(override.id)}
                      className="text-red-400 hover:text-red-300"
                    >
                      <Trash2 size={16} />
                    </button>
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
