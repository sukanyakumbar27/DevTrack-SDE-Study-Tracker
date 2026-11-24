import { useState } from 'react';
import { Pencil, Plus, Search, Trash2 } from 'lucide-react';
import api, { errorMessage } from '../api/client.js';
import useFetch from '../hooks/useFetch.js';
import { Empty, ErrorBox, Field, Modal, PageHeader, Spinner } from '../components/ui.jsx';
import { pretty } from '../utils.js';

const CATEGORIES = ['CODING', 'CONCEPT', 'INTERVIEW'];
const catColor = {
  CODING: 'bg-indigo-100 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300',
  CONCEPT: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300',
  INTERVIEW: 'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300',
};

function NoteForm({ note, onClose, onSaved }) {
  const [form, setForm] = useState({ title: note?.title || '', content: note?.content || '', category: note?.category || 'CODING' });
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    try {
      if (note) await api.put(`/notes/${note.id}`, form); else await api.post('/notes', form);
      onSaved();
    } catch (err) { setError(errorMessage(err)); }
  };

  return (
    <Modal title={note ? 'Edit note' : 'New note'} onClose={onClose}>
      <form onSubmit={submit} className="space-y-3">
        <ErrorBox message={error} />
        <Field label="Title"><input className="input" required value={form.title} onChange={set('title')} autoFocus /></Field>
        <Field label="Category"><select className="input" value={form.category} onChange={set('category')}>{CATEGORIES.map((c) => <option key={c} value={c}>{pretty(c)}</option>)}</select></Field>
        <Field label="Content"><textarea className="input font-mono" rows={9} value={form.content} onChange={set('content')} /></Field>
        <div className="flex justify-end gap-2"><button type="button" className="btn-ghost" onClick={onClose}>Cancel</button><button className="btn-primary">Save</button></div>
      </form>
    </Modal>
  );
}

export default function Notes() {
  const { data, loading, error, reload } = useFetch('/notes');
  const [category, setCategory] = useState('ALL');
  const [query, setQuery] = useState('');
  const [editing, setEditing] = useState(null);
  const [actionError, setActionError] = useState('');

  const remove = async (n) => {
    if (!window.confirm(`Delete "${n.title}"?`)) return;
    try { await api.delete(`/notes/${n.id}`); reload(); } catch (e) { setActionError(errorMessage(e)); }
  };

  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  const q = query.trim().toLowerCase();
  const rows = data.filter((n) => (category === 'ALL' || n.category === category)
    && (!q || n.title.toLowerCase().includes(q) || (n.content || '').toLowerCase().includes(q)));

  return (
    <>
      <PageHeader title="Notes" subtitle="Coding notes, concepts and interview prep">
        <button className="btn-primary" onClick={() => setEditing('new')}><Plus className="h-4 w-4" /> New note</button>
      </PageHeader>
      <div className="mb-4 flex flex-wrap gap-2">
        <div className="relative flex-1 sm:max-w-xs">
          <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
          <input className="input !pl-9" placeholder="Search notes" value={query} onChange={(e) => setQuery(e.target.value)} />
        </div>
        {['ALL', ...CATEGORIES].map((c) => <button key={c} onClick={() => setCategory(c)} className={`btn !px-3 !py-1.5 ${category === c ? 'bg-indigo-600 text-white' : 'btn-ghost'}`}>{c === 'ALL' ? 'All' : pretty(c)}</button>)}
      </div>
      <ErrorBox message={actionError} />
      <div className="mt-2 grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {rows.length === 0 && <div className="md:col-span-2 lg:col-span-3"><Empty text="No notes found." /></div>}
        {rows.map((n) => (
          <div key={n.id} className="card flex flex-col">
            <div className="flex items-start justify-between gap-2">
              <h3 className="font-semibold">{n.title}</h3>
              <span className={`badge shrink-0 ${catColor[n.category]}`}>{pretty(n.category)}</span>
            </div>
            <p className="mt-2 line-clamp-6 flex-1 whitespace-pre-wrap text-sm text-slate-500">{n.content}</p>
            <div className="mt-3 flex items-center justify-between">
              <span className="text-xs text-slate-400">{new Date(n.updatedAt).toLocaleDateString()}</span>
              <div>
                <button className="btn-ghost !p-2" onClick={() => setEditing(n)} title="Edit"><Pencil className="h-4 w-4" /></button>
                <button className="btn-danger !p-2" onClick={() => remove(n)} title="Delete"><Trash2 className="h-4 w-4" /></button>
              </div>
            </div>
          </div>
        ))}
      </div>
      {editing && <NoteForm note={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); reload(); }} />}
    </>
  );
}
