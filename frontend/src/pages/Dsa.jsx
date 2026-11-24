import { useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import api, { errorMessage } from '../api/client.js';
import useFetch from '../hooks/useFetch.js';
import { Empty, ErrorBox, Field, Modal, PageHeader, ProgressBar, Spinner, StatCard } from '../components/ui.jsx';
import { difficultyColor, pretty, todayISO } from '../utils.js';

const TOPICS = ['ARRAYS', 'STRINGS', 'LINKED_LISTS', 'STACKS_AND_QUEUES', 'HASHING', 'BINARY_SEARCH', 'SORTING', 'RECURSION', 'TREES', 'GRAPHS', 'DYNAMIC_PROGRAMMING'];

function ProblemForm({ problem, onClose, onSaved }) {
  const [form, setForm] = useState({
    name: problem?.name || '', platform: problem?.platform || 'LeetCode', topic: problem?.topic || 'ARRAYS',
    difficulty: problem?.difficulty || 'EASY', status: problem?.status || 'SOLVED',
    solvedDate: problem?.solvedDate || todayISO(), notes: problem?.notes || '',
  });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const body = { ...form, solvedDate: form.status === 'SOLVED' ? form.solvedDate : null };
      if (problem) await api.put(`/dsa/${problem.id}`, body); else await api.post('/dsa', body);
      onSaved();
    } catch (err) { setError(errorMessage(err)); setSaving(false); }
  };

  return (
    <Modal title={problem ? 'Edit problem' : 'Add problem'} onClose={onClose}>
      <form onSubmit={submit} className="space-y-3">
        <ErrorBox message={error} />
        <Field label="Problem name"><input className="input" required value={form.name} onChange={set('name')} autoFocus /></Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Platform"><input className="input" value={form.platform} onChange={set('platform')} /></Field>
          <Field label="Topic">
            <select className="input" value={form.topic} onChange={set('topic')}>{TOPICS.map((t) => <option key={t} value={t}>{pretty(t)}</option>)}</select>
          </Field>
          <Field label="Difficulty">
            <select className="input" value={form.difficulty} onChange={set('difficulty')}><option value="EASY">Easy</option><option value="MEDIUM">Medium</option><option value="HARD">Hard</option></select>
          </Field>
          <Field label="Status">
            <select className="input" value={form.status} onChange={set('status')}><option value="SOLVED">Solved</option><option value="ATTEMPTED">Attempted</option><option value="REVISIT">Revisit</option></select>
          </Field>
        </div>
        {form.status === 'SOLVED' && <Field label="Date solved"><input type="date" className="input" value={form.solvedDate} onChange={set('solvedDate')} /></Field>}
        <Field label="Approach notes"><textarea className="input" rows={4} value={form.notes} onChange={set('notes')} placeholder="Idea, time and space complexity, mistakes..." /></Field>
        <div className="flex justify-end gap-2"><button type="button" className="btn-ghost" onClick={onClose}>Cancel</button><button className="btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Save'}</button></div>
      </form>
    </Modal>
  );
}

export default function Dsa() {
  const list = useFetch('/dsa');
  const summary = useFetch('/dsa/summary');
  const [topic, setTopic] = useState('ALL');
  const [difficulty, setDifficulty] = useState('ALL');
  const [editing, setEditing] = useState(null);
  const [actionError, setActionError] = useState('');

  const reloadAll = () => { list.reload(); summary.reload(); };
  const remove = async (p) => {
    if (!window.confirm(`Delete "${p.name}"?`)) return;
    try { await api.delete(`/dsa/${p.id}`); reloadAll(); } catch (e) { setActionError(errorMessage(e)); }
  };

  if (list.loading || summary.loading) return <Spinner />;
  if (list.error || summary.error) return <ErrorBox message={list.error || summary.error} onRetry={reloadAll} />;

  const s = summary.data;
  const max = Math.max(1, ...s.byTopic.map((t) => t.value));
  const rows = list.data.filter((p) => (topic === 'ALL' || p.topic === topic) && (difficulty === 'ALL' || p.difficulty === difficulty));

  return (
    <>
      <PageHeader title="DSA Tracker" subtitle="Log problems and see where you are strong or weak">
        <button className="btn-primary" onClick={() => setEditing('new')}><Plus className="h-4 w-4" /> Add problem</button>
      </PageHeader>

      <div className="grid gap-4 sm:grid-cols-4">
        <StatCard label="Solved" value={s.totalSolved} />
        {s.byDifficulty.map((d) => <StatCard key={d.name} label={pretty(d.name)} value={d.value} />)}
      </div>

      <div className="card mt-4">
        <h2 className="mb-3 font-semibold">Solved by topic</h2>
        <div className="grid gap-x-8 gap-y-2 sm:grid-cols-2">
          {s.byTopic.map((t) => (
            <div key={t.name}>
              <div className="mb-1 flex justify-between text-xs"><span>{pretty(t.name)}</span><span>{t.value}</span></div>
              <ProgressBar value={(100 * t.value) / max} />
            </div>
          ))}
        </div>
      </div>

      <div className="mt-6 flex flex-wrap gap-2">
        <select className="input !w-auto" value={topic} onChange={(e) => setTopic(e.target.value)}>
          <option value="ALL">All topics</option>{TOPICS.map((t) => <option key={t} value={t}>{pretty(t)}</option>)}
        </select>
        <select className="input !w-auto" value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
          <option value="ALL">All difficulties</option><option value="EASY">Easy</option><option value="MEDIUM">Medium</option><option value="HARD">Hard</option>
        </select>
      </div>
      <ErrorBox message={actionError} />

      <div className="mt-3 space-y-2">
        {rows.length === 0 && <Empty text="No problems match. Add your first one!" />}
        {rows.map((p) => (
          <div key={p.id} className="card flex items-start gap-3 !p-4">
            <div className="min-w-0 flex-1">
              <div className="font-medium">{p.name}</div>
              <div className="mt-1 flex flex-wrap items-center gap-2 text-xs">
                <span className={`badge ${difficultyColor[p.difficulty]}`}>{pretty(p.difficulty)}</span>
                <span className="badge bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300">{pretty(p.topic)}</span>
                <span className="badge bg-slate-100 dark:bg-slate-800">{pretty(p.status)}</span>
                <span className="text-slate-500">{p.platform}{p.solvedDate && ` - ${p.solvedDate}`}</span>
              </div>
              {p.notes && <p className="mt-2 whitespace-pre-wrap text-sm text-slate-500">{p.notes}</p>}
            </div>
            <button className="btn-ghost !p-2" onClick={() => setEditing(p)} title="Edit"><Pencil className="h-4 w-4" /></button>
            <button className="btn-danger !p-2" onClick={() => remove(p)} title="Delete"><Trash2 className="h-4 w-4" /></button>
          </div>
        ))}
      </div>

      {editing && <ProblemForm problem={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); reloadAll(); }} />}
    </>
  );
}
