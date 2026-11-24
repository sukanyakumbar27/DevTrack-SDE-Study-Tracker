import { useState } from 'react';
import { ExternalLink, Flag, Pencil, Plus, Trash2 } from 'lucide-react';
import api, { errorMessage } from '../api/client.js';
import useFetch from '../hooks/useFetch.js';
import { Empty, ErrorBox, Field, Modal, PageHeader, ProgressBar, Spinner } from '../components/ui.jsx';
import { pretty } from '../utils.js';

const STATUSES = ['PLANNING', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD'];

function ProjectForm({ project, onClose, onSaved }) {
  const [form, setForm] = useState({
    name: project?.name || '', description: project?.description || '',
    status: project?.status || 'PLANNING', githubUrl: project?.githubUrl || '',
  });
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    try {
      if (project) await api.put(`/projects/${project.id}`, form); else await api.post('/projects', form);
      onSaved();
    } catch (err) { setError(errorMessage(err)); }
  };

  return (
    <Modal title={project ? 'Edit project' : 'New project'} onClose={onClose}>
      <form onSubmit={submit} className="space-y-3">
        <ErrorBox message={error} />
        <Field label="Name"><input className="input" required value={form.name} onChange={set('name')} autoFocus /></Field>
        <Field label="Description"><textarea className="input" rows={3} value={form.description} onChange={set('description')} /></Field>
        <Field label="Status"><select className="input" value={form.status} onChange={set('status')}>{STATUSES.map((s) => <option key={s} value={s}>{pretty(s)}</option>)}</select></Field>
        <Field label="GitHub repository URL"><input className="input" placeholder="https://github.com/you/repo" value={form.githubUrl} onChange={set('githubUrl')} /></Field>
        <div className="flex justify-end gap-2"><button type="button" className="btn-ghost" onClick={onClose}>Cancel</button><button className="btn-primary">Save</button></div>
      </form>
    </Modal>
  );
}

function ProjectTasks({ project, onChanged }) {
  const { data, loading, error, reload } = useFetch(`/projects/${project.id}/tasks`);
  const [title, setTitle] = useState('');
  const [milestone, setMilestone] = useState(false);
  const [actionError, setActionError] = useState('');

  const run = async (fn) => {
    setActionError('');
    try { await fn(); await reload(); onChanged(); } catch (e) { setActionError(errorMessage(e)); }
  };
  const add = (e) => {
    e.preventDefault();
    if (!title.trim()) return;
    run(async () => { await api.post(`/projects/${project.id}/tasks`, { title, milestone, done: false }); setTitle(''); setMilestone(false); });
  };

  if (loading) return <Spinner label="Loading tasks..." />;
  return (
    <div className="mt-4 border-t border-slate-200 pt-4 dark:border-slate-800">
      <ErrorBox message={error || actionError} />
      <ul className="space-y-1">
        {data?.map((t) => (
          <li key={t.id} className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={t.done} onChange={() => run(() => api.put(`/projects/${project.id}/tasks/${t.id}`, { title: t.title, milestone: t.milestone, done: !t.done, dueDate: t.dueDate }))} />
            <span className={`flex-1 ${t.done ? 'text-slate-400 line-through' : ''}`}>{t.title}</span>
            {t.milestone && <span className="badge bg-purple-100 text-purple-700 dark:bg-purple-950 dark:text-purple-300"><Flag className="mr-1 inline h-3 w-3" />Milestone</span>}
            <button className="btn-danger !p-1.5" onClick={() => run(() => api.delete(`/projects/${project.id}/tasks/${t.id}`))} aria-label="Delete task"><Trash2 className="h-3.5 w-3.5" /></button>
          </li>
        ))}
        {data?.length === 0 && <li className="text-sm text-slate-500">No tasks yet.</li>}
      </ul>
      <form onSubmit={add} className="mt-3 flex flex-wrap items-center gap-2">
        <input className="input !w-auto flex-1" placeholder="Add a task or milestone" value={title} onChange={(e) => setTitle(e.target.value)} />
        <label className="flex items-center gap-1 text-xs"><input type="checkbox" checked={milestone} onChange={(e) => setMilestone(e.target.checked)} /> Milestone</label>
        <button className="btn-primary !py-2">Add</button>
      </form>
    </div>
  );
}

export default function Projects() {
  const { data, loading, error, reload } = useFetch('/projects');
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(null);
  const [actionError, setActionError] = useState('');

  const remove = async (p) => {
    if (!window.confirm(`Delete project "${p.name}" and all its tasks?`)) return;
    try { await api.delete(`/projects/${p.id}`); reload(); } catch (e) { setActionError(errorMessage(e)); }
  };

  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  return (
    <>
      <PageHeader title="Projects" subtitle="Personal projects, milestones and progress">
        <button className="btn-primary" onClick={() => setEditing('new')}><Plus className="h-4 w-4" /> New project</button>
      </PageHeader>
      <ErrorBox message={actionError} />
      <div className="mt-2 grid gap-4 lg:grid-cols-2">
        {data.length === 0 && <div className="lg:col-span-2"><Empty text="No projects yet." /></div>}
        {data.map((p) => (
          <div key={p.id} className="card">
            <div className="flex items-start justify-between gap-2">
              <div>
                <h3 className="font-semibold">{p.name}</h3>
                <span className="badge mt-1 bg-slate-100 dark:bg-slate-800">{pretty(p.status)}</span>
              </div>
              <div className="flex">
                {p.githubUrl && <a className="btn-ghost !p-2" href={p.githubUrl} target="_blank" rel="noreferrer" title="Open GitHub"><ExternalLink className="h-4 w-4" /></a>}
                <button className="btn-ghost !p-2" onClick={() => setEditing(p)} title="Edit"><Pencil className="h-4 w-4" /></button>
                <button className="btn-danger !p-2" onClick={() => remove(p)} title="Delete"><Trash2 className="h-4 w-4" /></button>
              </div>
            </div>
            {p.description && <p className="mt-2 text-sm text-slate-500">{p.description}</p>}
            <div className="mt-3">
              <div className="mb-1 flex justify-between text-xs"><span>{p.doneTasks}/{p.totalTasks} tasks</span><span>{p.completionPercent}%</span></div>
              <ProgressBar value={p.completionPercent} color="bg-emerald-500" />
            </div>
            <button className="btn-ghost mt-3 !px-2 !py-1 text-xs" onClick={() => setOpen(open === p.id ? null : p.id)}>{open === p.id ? 'Hide tasks' : 'Show tasks and milestones'}</button>
            {open === p.id && <ProjectTasks project={p} onChanged={reload} />}
          </div>
        ))}
      </div>
      {editing && <ProjectForm project={editing === 'new' ? null : editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); reload(); }} />}
    </>
  );
}
