import { useMemo, useState } from 'react';
import { ChevronLeft, ChevronRight, Plus } from 'lucide-react';
import api, { errorMessage } from '../api/client.js';
import useFetch from '../hooks/useFetch.js';
import TaskForm from '../components/TaskForm.jsx';
import { Empty, ErrorBox, PageHeader, Spinner } from '../components/ui.jsx';
import { pretty, toISO, todayISO } from '../utils.js';

const startOfWeek = (d) => { const x = new Date(d); x.setDate(x.getDate() - ((x.getDay() + 6) % 7)); x.setHours(0, 0, 0, 0); return x; };
const addDays = (d, n) => { const x = new Date(d); x.setDate(x.getDate() + n); return x; };

export default function Calendar() {
  const { data, loading, error, reload } = useFetch('/tasks');
  const [view, setView] = useState('month');
  const [cursor, setCursor] = useState(new Date());
  const [selected, setSelected] = useState(todayISO());
  const [form, setForm] = useState(null); // null | 'new' | task
  const [actionError, setActionError] = useState('');

  const byDate = useMemo(() => {
    const map = {};
    (data || []).forEach((t) => { (map[t.scheduledDate] ||= []).push(t); });
    return map;
  }, [data]);

  const days = useMemo(() => {
    if (view === 'week') {
      const start = startOfWeek(cursor);
      return Array.from({ length: 7 }, (_, i) => addDays(start, i));
    }
    const first = new Date(cursor.getFullYear(), cursor.getMonth(), 1);
    const start = startOfWeek(first);
    return Array.from({ length: 42 }, (_, i) => addDays(start, i));
  }, [view, cursor]);

  const move = (dir) => {
    const d = new Date(cursor);
    if (view === 'week') d.setDate(d.getDate() + 7 * dir); else d.setMonth(d.getMonth() + dir, 1);
    setCursor(d);
  };

  const toggle = async (t) => {
    setActionError('');
    try { await api.patch(`/tasks/${t.id}/toggle`); reload(); } catch (e) { setActionError(errorMessage(e)); }
  };

  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  const title = view === 'month'
    ? cursor.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })
    : `Week of ${days[0].toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}`;
  const selectedTasks = byDate[selected] || [];

  return (
    <>
      <PageHeader title="Calendar" subtitle="See and schedule study activities">
        <button className="btn-primary" onClick={() => setForm('new')}><Plus className="h-4 w-4" /> Schedule task</button>
      </PageHeader>

      <div className="card">
        <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-1">
            <button className="btn-ghost !p-2" onClick={() => move(-1)} aria-label="Previous"><ChevronLeft className="h-4 w-4" /></button>
            <span className="min-w-40 text-center font-semibold">{title}</span>
            <button className="btn-ghost !p-2" onClick={() => move(1)} aria-label="Next"><ChevronRight className="h-4 w-4" /></button>
            <button className="btn-ghost" onClick={() => { setCursor(new Date()); setSelected(todayISO()); }}>Today</button>
          </div>
          <div className="flex gap-1">
            {['month', 'week'].map((v) => <button key={v} onClick={() => setView(v)} className={`btn !px-3 !py-1.5 ${view === v ? 'bg-indigo-600 text-white' : 'btn-ghost'}`}>{pretty(v)}</button>)}
          </div>
        </div>

        <div className="grid grid-cols-7 gap-1 text-center text-xs text-slate-500">
          {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((d) => <div key={d} className="py-1">{d}</div>)}
        </div>
        <div className="grid grid-cols-7 gap-1">
          {days.map((d) => {
            const iso = toISO(d);
            const tasks = byDate[iso] || [];
            const done = tasks.filter((t) => t.status === 'COMPLETED').length;
            const pending = tasks.length - done;
            const outside = view === 'month' && d.getMonth() !== cursor.getMonth();
            return (
              <button
                key={iso}
                onClick={() => setSelected(iso)}
                className={`flex flex-col rounded-lg border p-1.5 text-left text-xs transition ${view === 'week' ? 'min-h-32' : 'min-h-16'} ${
                  selected === iso ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950' : 'border-slate-200 hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800'
                } ${outside ? 'opacity-40' : ''}`}
              >
                <span className={`font-medium ${iso === todayISO() ? 'text-indigo-600' : ''}`}>{d.getDate()}</span>
                {view === 'week' ? (
                  <div className="mt-1 space-y-1">
                    {tasks.map((t) => (
                      <div key={t.id} className={`truncate rounded px-1 ${t.status === 'COMPLETED' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300' : 'bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300'}`}>{t.title}</div>
                    ))}
                  </div>
                ) : (
                  <div className="mt-auto flex gap-1">
                    {done > 0 && <span className="rounded bg-emerald-500 px-1 text-white">{done}</span>}
                    {pending > 0 && <span className="rounded bg-amber-500 px-1 text-white">{pending}</span>}
                  </div>
                )}
              </button>
            );
          })}
        </div>
        <div className="mt-3 flex gap-4 text-xs text-slate-500">
          <span><span className="mr-1 inline-block h-2 w-2 rounded bg-emerald-500" />Completed</span>
          <span><span className="mr-1 inline-block h-2 w-2 rounded bg-amber-500" />Pending</span>
        </div>
      </div>

      <div className="card mt-4">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="font-semibold">{new Date(selected + 'T00:00:00').toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' })}</h2>
          <button className="btn-ghost" onClick={() => setForm('new')}><Plus className="h-4 w-4" /> Add</button>
        </div>
        <ErrorBox message={actionError} />
        {selectedTasks.length === 0 ? <Empty text="Nothing scheduled for this day." /> : (
          <ul className="space-y-2">
            {selectedTasks.map((t) => (
              <li key={t.id} className="flex items-center gap-3 text-sm">
                <input type="checkbox" checked={t.status === 'COMPLETED'} onChange={() => toggle(t)} />
                <button className={`flex-1 text-left ${t.status === 'COMPLETED' ? 'text-slate-400 line-through' : ''}`} onClick={() => setForm(t)}>{t.title}</button>
                <span className="text-xs text-slate-500">{t.subject} - {t.estimatedMinutes} min</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      {form && <TaskForm task={form === 'new' ? null : form} defaultDate={selected} onClose={() => setForm(null)} onSaved={() => { setForm(null); reload(); }} />}
    </>
  );
}
