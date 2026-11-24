import { useState } from 'react';
import api, { errorMessage } from '../api/client.js';
import { Modal, Field, ErrorBox } from './ui.jsx';
import { todayISO } from '../utils.js';

/** Create / edit a study task. `task` = existing task to edit, `defaultDate` = pre-filled date. */
export default function TaskForm({ task, defaultDate, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: task?.title || '',
    description: task?.description || '',
    subject: task?.subject || '',
    priority: task?.priority || 'MEDIUM',
    status: task?.status || 'PENDING',
    estimatedMinutes: task?.estimatedMinutes ?? 30,
    scheduledDate: task?.scheduledDate || defaultDate || todayISO(),
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const body = { ...form, estimatedMinutes: Number(form.estimatedMinutes) };
      if (task) await api.put(`/tasks/${task.id}`, body);
      else await api.post('/tasks', body);
      onSaved();
    } catch (err) {
      setError(errorMessage(err));
      setSaving(false);
    }
  };

  return (
    <Modal title={task ? 'Edit task' : 'New study task'} onClose={onClose}>
      <form onSubmit={submit} className="space-y-3">
        <ErrorBox message={error} />
        <Field label="Title"><input className="input" required value={form.title} onChange={set('title')} autoFocus /></Field>
        <Field label="Description"><textarea className="input" rows={2} value={form.description} onChange={set('description')} /></Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Subject"><input className="input" placeholder="DSA, Java..." value={form.subject} onChange={set('subject')} /></Field>
          <Field label="Priority">
            <select className="input" value={form.priority} onChange={set('priority')}>
              <option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option>
            </select>
          </Field>
          <Field label="Estimated minutes"><input type="number" min="0" className="input" value={form.estimatedMinutes} onChange={set('estimatedMinutes')} /></Field>
          <Field label="Date"><input type="date" className="input" required value={form.scheduledDate} onChange={set('scheduledDate')} /></Field>
        </div>
        <Field label="Status">
          <select className="input" value={form.status} onChange={set('status')}>
            <option value="PENDING">Pending</option><option value="IN_PROGRESS">In progress</option><option value="COMPLETED">Completed</option>
          </select>
        </Field>
        <div className="flex justify-end gap-2 pt-2">
          <button type="button" className="btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Save task'}</button>
        </div>
      </form>
    </Modal>
  );
}
