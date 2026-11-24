import { useMemo, useState } from 'react';
import { Plus, Pencil, Trash2, Timer } from 'lucide-react';
import api, { errorMessage } from '../api/client.js';
import useFetch from '../hooks/useFetch.js';
import TaskForm from '../components/TaskForm.jsx';
import { Empty, ErrorBox, PageHeader, ProgressBar, Spinner } from '../components/ui.jsx';
import { pretty, priorityColor, todayISO } from '../utils.js';

const FILTERS = ['ALL', 'PENDING', 'IN_PROGRESS', 'COMPLETED'];

export default function Planner() {
  const { data: tasks, loading, error, reload } = useFetch('/tasks');
  const [filter, setFilter] = useState('ALL');
  const [date, setDate] = useState(todayISO());
  const [allDates, setAllDates] = useState(false);
  const [editing, setEditing] = useState(null); // null | 'new' | task
  const [actionError, setActionError] = useState('');

  const dayTasks = useMemo(
    () => (tasks || []).filter((t) => allDates || t.scheduledDate === date),
    [tasks, date, allDates]
  );
  const visible = dayTasks.filter((t) => filter === 'ALL' || t.status === filter);
  const done = dayTasks.filter((t) => t.status === 'COMPLETED').length;
  const percent = dayTasks.length ? Math.round((100 * done) / dayTasks.length) : 0;

  const run = async (fn) => {
    setActionError('');
    try { await fn(); await reload(); } catch (e) { setActionError(errorMessage(e)); }
  };
  const toggle = (t) => run(() => api.patch(`/tasks/${t.id}/toggle`));
  const remove = (t) => window.confirm(`Delete "${t.title}"?`) && run(() => api.delete(`/tasks/${t.id}`));
  const logTime = (t) => {
    const v = window.prompt('How many minutes did you study?', '30');
    const minutes = Number(v);
    if (v && minutes > 0) run(() => api.post(`/tasks/${t.id}/log-time`, { minutes }));
  };

  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  return (
    <>
      <PageHeader title="Study Planner" subtitle="Plan tasks, track time, check them off">
        <button className="btn-primary" onClick={() => setEditing('new')}><Plus className="h-4 w-4" /> New task</button>
      </PageHeader>

      <div className="card mb-4">
        <div className="flex flex-wrap items-center gap-3">
          <input type="date" className="input !w-auto" value={date} disabled={allDates} onChange={(e) => setDate(e.target.value)} />
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={allDates} onChange={(e) => setAllDates(e.target.checked)} /> All dates</label>
          <div className="ml-auto flex flex-wrap gap-1">
            {FILTERS.map((f) => (
              <button key={f} onClick={() => setFilter(f)} className={`btn !px-3 !py-1.5 ${filter === f ? 'bg-indigo-600 text-white' : 'btn-ghost'}`}>{f === 'ALL' ? 'All' : pretty(f)}</button>
            ))}
          </div>
        </div>
        <div className="mt-4">
          <div className="mb-1 flex justify-between text-sm"><span>Completion {allDates ? '(all dates)' : 'for this day'}</span><span className="font-medium">{done}/{dayTasks.length} - {percent}%</span></div>
          <ProgressBar value={percent} color="bg-emerald-500" />
        </div>
      </div>

      <ErrorBox message={actionError} />
      <div className="mt-4 space-y-2">
        {visible.length === 0 && <Empty text="No tasks here. Add one with the New task button." />}
        {visible.map((t) => (
          <div key={t.id} className="card flex items-start gap-3 !p-4">
            <input type="checkbox" className="mt-1 h-4 w-4" checked={t.status === 'COMPLETED'} onChange={() => toggle(t)} aria-label="Mark completed" />
            <div className="min-w-0 flex-1">
              <div className={`font-medium ${t.status === 'COMPLETED' ? 'text-slate-400 line-through' : ''}`}>{t.title}</div>
              {t.description && <p className="mt-0.5 text-sm text-slate-500">{t.description}</p>}
              <div className="mt-2 flex flex-wrap items-center gap-2 text-xs">
                <span className={`badge ${priorityColor[t.priority]}`}>{pretty(t.priority)}</span>
                <span className="badge bg-slate-100 dark:bg-slate-800">{pretty(t.status)}</span>
                {t.subject && <span className="badge bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300">{t.subject}</span>}
                <span className="text-slate-500">{t.actualMinutes}/{t.estimatedMinutes} min{allDates && ` - ${t.scheduledDate}`}</span>
              </div>
            </div>
            <div className="flex shrink-0">
              <button className="btn-ghost !p-2" title="Log time" onClick={() => logTime(t)}><Timer className="h-4 w-4" /></button>
              <button className="btn-ghost !p-2" title="Edit" onClick={() => setEditing(t)}><Pencil className="h-4 w-4" /></button>
              <button className="btn-danger !p-2" title="Delete" onClick={() => remove(t)}><Trash2 className="h-4 w-4" /></button>
            </div>
          </div>
        ))}
      </div>

      {editing && (
        <TaskForm
          task={editing === 'new' ? null : editing}
          defaultDate={allDates ? todayISO() : date}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); reload(); }}
        />
      )}
    </>
  );
}
