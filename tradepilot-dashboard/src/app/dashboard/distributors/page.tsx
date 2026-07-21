'use client';

import { useEffect, useState } from 'react';
import { Pencil, Trash2, Save, X, Plus, ToggleLeft, ToggleRight } from 'lucide-react';

interface Distributor {
  id: number;
  name: string;
  contactName: string | null;
  phone: string | null;
  city: string | null;
  isActive: boolean;
}

interface EditingDistributor {
  contactName: string;
  phone: string;
  city: string;
}

const inputClass =
  'bg-gray-800 border border-gray-700 text-white rounded px-2 py-1 text-sm';

export default function DistributorsPage() {
  const [distributors, setDistributors] = useState<Distributor[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editValues, setEditValues] = useState<EditingDistributor>({
    contactName: '',
    phone: '',
    city: '',
  });
  const [showAddForm, setShowAddForm] = useState(false);
  const [newDistributor, setNewDistributor] = useState({
    name: '',
    contactName: '',
    phone: '',
    city: '',
  });

  const fetchDistributors = () => {
    fetch('http://localhost:8080/api/distributors', {
      headers: { Accept: 'application/json' },
    })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((data: Distributor[]) => {
        setDistributors(data);
        setLoading(false);
      })
      .catch(err => console.error('Distributors fetch failed:', err));
  };

  useEffect(() => {
    fetchDistributors();
  }, []);

  const startEdit = (distributor: Distributor) => {
    setEditingId(distributor.id);
    setEditValues({
      contactName: distributor.contactName ?? '',
      phone: distributor.phone ?? '',
      city: distributor.city ?? '',
    });
  };

  const saveEdit = async (id: number) => {
    try {
      const res = await fetch(`http://localhost:8080/api/distributors/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(editValues),
      });
      if (res.status === 409) {
        const err = await res.json().catch(() => null);
        alert(err?.message || 'Conflict updating distributor');
        return;
      }
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchDistributors();
      setEditingId(null);
    } catch (err) {
      console.error('Save edit failed:', err);
    }
  };

  const toggleActive = async (id: number) => {
    try {
      const res = await fetch(`http://localhost:8080/api/distributors/${id}/toggle`, {
        method: 'PATCH',
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchDistributors();
    } catch (err) {
      console.error('Toggle active failed:', err);
    }
  };

  const deleteDistributor = async (id: number) => {
    if (!window.confirm('Delete this distributor?')) return;
    try {
      const res = await fetch(`http://localhost:8080/api/distributors/${id}`, {
        method: 'DELETE',
      });
      if (res.status === 409) {
        alert('Cannot delete — distributor has existing price rules');
        return;
      }
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchDistributors();
    } catch (err) {
      console.error('Delete distributor failed:', err);
    }
  };

  const addDistributor = async () => {
    if (!newDistributor.name.trim()) {
      alert('Name is required');
      return;
    }
    try {
      const res = await fetch('http://localhost:8080/api/distributors', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newDistributor),
      });
      if (res.status === 409) {
        alert('Distributor already exists');
        return;
      }
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      fetchDistributors();
      setNewDistributor({ name: '', contactName: '', phone: '', city: '' });
      setShowAddForm(false);
    } catch (err) {
      console.error('Add distributor failed:', err);
    }
  };

  return (
    <div className="p-8">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">Distributors</h2>
          <p className="text-gray-500 text-sm mt-1">
            {distributors.length} distributors configured
          </p>
        </div>
        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="flex items-center gap-2 bg-emerald-700 hover:bg-emerald-600 text-white rounded-lg px-4 py-2 text-sm transition-colors"
        >
          <Plus size={16} />
          Add Distributor
        </button>
      </div>

      {showAddForm && (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 mb-6">
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label className="text-xs text-gray-500 block mb-1">Name</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newDistributor.name}
                onChange={e =>
                  setNewDistributor({ ...newDistributor, name: e.target.value })
                }
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Contact Name</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newDistributor.contactName}
                onChange={e =>
                  setNewDistributor({ ...newDistributor, contactName: e.target.value })
                }
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">Phone</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newDistributor.phone}
                onChange={e =>
                  setNewDistributor({ ...newDistributor, phone: e.target.value })
                }
              />
            </div>
            <div>
              <label className="text-xs text-gray-500 block mb-1">City</label>
              <input
                type="text"
                className={`${inputClass} w-full`}
                value={newDistributor.city}
                onChange={e =>
                  setNewDistributor({ ...newDistributor, city: e.target.value })
                }
              />
            </div>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={addDistributor}
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
        <div className="text-gray-500">Loading distributors...</div>
      ) : distributors.length === 0 ? (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-12 text-center">
          <p className="text-gray-500">No distributors yet.</p>
        </div>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden overflow-x-auto">
          <table className="w-full min-w-[700px]">
            <thead>
              <tr className="border-b border-gray-800 text-left text-xs text-gray-500 uppercase tracking-wider">
                <th className="px-6 py-4">Name</th>
                <th className="px-6 py-4">Contact</th>
                <th className="px-6 py-4">Phone</th>
                <th className="px-6 py-4">City</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {distributors.map(distributor => {
                const isEditing = editingId === distributor.id;
                return (
                  <tr
                    key={distributor.id}
                    className={`hover:bg-gray-800/50 transition-colors ${
                      distributor.isActive ? '' : 'opacity-50'
                    }`}
                  >
                    <td className="px-6 py-4 text-sm">{distributor.name}</td>
                    <td className="px-6 py-4 text-sm">
                      {isEditing ? (
                        <input
                          type="text"
                          className={`${inputClass} w-full`}
                          value={editValues.contactName}
                          onChange={e =>
                            setEditValues({ ...editValues, contactName: e.target.value })
                          }
                        />
                      ) : (
                        distributor.contactName || '—'
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      {isEditing ? (
                        <input
                          type="text"
                          className={`${inputClass} w-full`}
                          value={editValues.phone}
                          onChange={e =>
                            setEditValues({ ...editValues, phone: e.target.value })
                          }
                        />
                      ) : (
                        distributor.phone || '—'
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      {isEditing ? (
                        <input
                          type="text"
                          className={`${inputClass} w-full`}
                          value={editValues.city}
                          onChange={e =>
                            setEditValues({ ...editValues, city: e.target.value })
                          }
                        />
                      ) : (
                        distributor.city || '—'
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`text-xs px-2 py-1 rounded-full ${
                          distributor.isActive
                            ? 'bg-emerald-900 text-emerald-300'
                            : 'bg-gray-700 text-gray-400'
                        }`}
                      >
                        {distributor.isActive ? 'ACTIVE' : 'INACTIVE'}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      {isEditing ? (
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => saveEdit(distributor.id)}
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
                            onClick={() => startEdit(distributor)}
                            className="text-gray-400 hover:text-white"
                          >
                            <Pencil size={16} />
                          </button>
                          <button
                            onClick={() => toggleActive(distributor.id)}
                            className="text-gray-400 hover:text-white"
                          >
                            {distributor.isActive ? (
                              <ToggleRight size={16} />
                            ) : (
                              <ToggleLeft size={16} />
                            )}
                          </button>
                          <button
                            onClick={() => deleteDistributor(distributor.id)}
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
