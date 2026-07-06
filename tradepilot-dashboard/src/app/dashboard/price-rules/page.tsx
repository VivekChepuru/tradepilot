'use client';

import { useEffect, useState } from 'react';
import { Plus, Pencil, ToggleLeft, ToggleRight, Trash2, Save, X } from 'lucide-react';

interface PriceRule {
  id: number;
  commodity: string;
  grade: string | null;
  basePrice: number;
  marginPercent: number;
  freightPerUnit: number;
  gstPercent: number;
  unit: string;
  distributorName: string | null;
  active: boolean;
}

interface EditingRule {
  basePrice: number;
  marginPercent: number;
  freightPerUnit: number;
}

const inputClass =
  'bg-gray-800 border border-gray-700 text-white rounded px-2 py-1 text-sm';

export default function PriceRulesPage() {
  const [rules, setRules] = useState<PriceRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editValues, setEditValues] = useState<EditingRule>({
    basePrice: 0,
    marginPercent: 0,
    freightPerUnit: 0,
  });
  const [showAddForm, setShowAddForm] = useState(false);
  const [newRule, setNewRule] = useState<Partial<PriceRule>>({
    commodity: '',
    grade: '',
    basePrice: 0,
    marginPercent: 4.5,
    freightPerUnit: 800,
    gstPercent: 18,
    unit: 'MT',
    distributorName: '',
    active: true,
  });

  const fetchRules = () => {
    fetch('http://localhost:8080/api/price-rules', {
      headers: { Accept: 'application/json' },
    })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((data: PriceRule[]) => {
        setRules(data);
        setLoading(false);
      })
      .catch(err => console.error('Price rules fetch failed:', err));
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const startEdit = (rule: PriceRule) => {
    setEditingId(rule.id);
    setEditValues({
      basePrice: rule.basePrice,
      marginPercent: rule.marginPercent,
      freightPerUnit: rule.freightPerUnit,
    });
  };

  const saveEdit = async (id: number) => {
    try {
      const res = await fetch(`http://localhost:8080/api/price-rules/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(editValues),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchRules();
      setEditingId(null);
    } catch (err) {
      console.error('Save edit failed:', err);
    }
  };

  const toggleActive = async (id: number) => {
    try {
      const res = await fetch(`http://localhost:8080/api/price-rules/${id}/toggle`, {
        method: 'PATCH',
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchRules();
    } catch (err) {
      console.error('Toggle active failed:', err);
    }
  };

  const deleteRule = async (id: number) => {
    if (!window.confirm('Delete this price rule?')) return;
    try {
      const res = await fetch(`http://localhost:8080/api/price-rules/${id}`, {
        method: 'DELETE',
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchRules();
    } catch (err) {
      console.error('Delete rule failed:', err);
    }
  };

  const addRule = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/price-rules', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newRule),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchRules();
      setShowAddForm(false);
    } catch (err) {
      console.error('Add rule failed:', err);
    }
  };

  const finalPrice = (rule: PriceRule) =>
    rule.basePrice * (1 + rule.marginPercent / 100) + rule.freightPerUnit;

  return (
    <div className="p-8">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Price Rules</h2>
          <p className="text-gray-500 text-sm mt-1">{rules.length} rules configured</p>
        </div>
        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="flex items-center gap-2 bg-emerald-700 hover:bg-emerald-600 text-white rounded-lg px-4 py-2 text-sm transition-colors"
        >
          <Plus size={16} />
          Add Rule
        </button>
      </div>

      {showAddForm && (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-6">
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label className="text-xs text-gray-500 block mb-1">Commodity</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newRule.commodity ?? ''}
                onChange={e => setNewRule({ ...newRule, commodity: e.target.value })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Grade</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newRule.grade ?? ''}
                onChange={e => setNewRule({ ...newRule, grade: e.target.value })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Base Price</label>
              <input
                type="number"
                className={`${inputClass} w-full`}
                value={newRule.basePrice ?? 0}
                onChange={e => setNewRule({ ...newRule, basePrice: Number(e.target.value) })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Margin %</label>
              <input
                type="number"
                className={`${inputClass} w-full`}
                value={newRule.marginPercent ?? 0}
                onChange={e => setNewRule({ ...newRule, marginPercent: Number(e.target.value) })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Freight</label>
              <input
                type="number"
                className={`${inputClass} w-full`}
                value={newRule.freightPerUnit ?? 0}
                onChange={e => setNewRule({ ...newRule, freightPerUnit: Number(e.target.value) })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">GST %</label>
              <input
                type="number"
                className={`${inputClass} w-full`}
                value={newRule.gstPercent ?? 0}
                onChange={e => setNewRule({ ...newRule, gstPercent: Number(e.target.value) })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Unit</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newRule.unit ?? ''}
                onChange={e => setNewRule({ ...newRule, unit: e.target.value })}
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Distributor Name</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newRule.distributorName ?? ''}
                onChange={e => setNewRule({ ...newRule, distributorName: e.target.value })}
              />
            </div>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={addRule}
              className="flex items-center gap-2 bg-emerald-700 hover:bg-emerald-600 text-white rounded-lg px-4 py-2 text-sm transition-colors"
            >
              <Save size={14} />
              Save
            </button>
            <button
              onClick={() => setShowAddForm(false)}
              className="flex items-center gap-2 bg-gray-800 hover:bg-gray-700 border border-gray-700 text-white rounded-lg px-4 py-2 text-sm transition-colors"
            >
              <X size={14} />
              Cancel
            </button>
          </div>
        </div>
      )}

      {loading ? (
        <div className="text-gray-500">Loading price rules...</div>
      ) : rules.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <p className="text-gray-500">No price rules yet.</p>
        </div>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
          <table className="w-full min-w-[900px]">
            <thead>
              <tr className="border-b border-gray-800 text-left text-xs text-gray-500 uppercase tracking-wider">
                <th className="px-6 py-4">Commodity</th>
                <th className="px-6 py-4">Grade</th>
                <th className="px-6 py-4">Distributor</th>
                <th className="px-6 py-4">Base Price</th>
                <th className="px-6 py-4">Margin</th>
                <th className="px-6 py-4">Freight</th>
                <th className="px-6 py-4">GST</th>
                <th className="px-6 py-4">Unit</th>
                <th className="px-6 py-4">Final</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {rules.map(rule => {
                const isEditing = editingId === rule.id;
                return (
                  <tr
                    key={rule.id}
                    className={`hover:bg-gray-800/50 transition-colors ${
                      rule.active ? '' : 'opacity-50'
                    }`}
                  >
                    <td className="px-6 py-4 text-sm">{rule.commodity}</td>
                    <td className="px-6 py-4 text-sm text-gray-400">{rule.grade || '—'}</td>
                    <td className="px-6 py-4 text-sm text-gray-400">
                      {rule.distributorName || '—'}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      {isEditing ? (
                        <input
                          type="number"
                          className={`${inputClass} w-24`}
                          value={editValues.basePrice}
                          onChange={e =>
                            setEditValues({ ...editValues, basePrice: Number(e.target.value) })
                          }
                        />
                      ) : (
                        `₹${rule.basePrice.toLocaleString('en-IN')}`
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      {isEditing ? (
                        <input
                          type="number"
                          className={`${inputClass} w-20`}
                          value={editValues.marginPercent}
                          onChange={e =>
                            setEditValues({ ...editValues, marginPercent: Number(e.target.value) })
                          }
                        />
                      ) : (
                        `${rule.marginPercent}%`
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      {isEditing ? (
                        <input
                          type="number"
                          className={`${inputClass} w-24`}
                          value={editValues.freightPerUnit}
                          onChange={e =>
                            setEditValues({ ...editValues, freightPerUnit: Number(e.target.value) })
                          }
                        />
                      ) : (
                        `₹${rule.freightPerUnit.toLocaleString('en-IN')}`
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm">{rule.gstPercent}%</td>
                    <td className="px-6 py-4 text-sm">{rule.unit}</td>
                    <td className="px-6 py-4 text-sm text-emerald-400">
                      ₹{finalPrice(rule).toLocaleString('en-IN', { maximumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`text-xs px-2 py-1 rounded-full ${
                          rule.active
                            ? 'bg-emerald-900 text-emerald-300'
                            : 'bg-gray-700 text-gray-400'
                        }`}
                      >
                        {rule.active ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      {isEditing ? (
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => saveEdit(rule.id)}
                            className="text-emerald-400 hover:text-emerald-300"
                          >
                            <Save size={16} />
                          </button>
                          <button
                            onClick={() => setEditingId(null)}
                            className="text-gray-400 hover:text-white"
                          >
                            <X size={16} />
                          </button>
                        </div>
                      ) : (
                        <div className="flex items-center gap-3">
                          <button
                            onClick={() => startEdit(rule)}
                            className="text-gray-400 hover:text-white"
                          >
                            <Pencil size={16} />
                          </button>
                          <button
                            onClick={() => toggleActive(rule.id)}
                            className="text-gray-400 hover:text-white"
                          >
                            {rule.active ? <ToggleRight size={16} /> : <ToggleLeft size={16} />}
                          </button>
                          <button
                            onClick={() => deleteRule(rule.id)}
                            className="text-red-400 hover:text-red-300"
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
