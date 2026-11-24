import { useState } from 'react';
import { Check } from 'lucide-react';
import api, { errorMessage } from '../api/client.js';
import useFetch from '../hooks/useFetch.js';
import { ErrorBox, PageHeader, ProgressBar, Spinner } from '../components/ui.jsx';

function TopicCard({ topic, onChanged, onError }) {
  const [notes, setNotes] = useState(topic.notes || '');
  const [saving, setSaving] = useState(false);

  const save = async (body) => {
    setSaving(true);
    try { await api.put(`/java-topics/${topic.id}`, body); await onChanged(); }
    catch (e) { onError(errorMessage(e)); }
    finally { setSaving(false); }
  };

  return (
    <div className="card !p-4">
      <label className="flex cursor-pointer items-center gap-3">
        <input type="checkbox" className="h-4 w-4" checked={topic.completed} onChange={(e) => save({ completed: e.target.checked })} />
        <span className={`font-medium ${topic.completed ? 'text-emerald-600' : ''}`}>{topic.name}</span>
        {topic.completed && <Check className="h-4 w-4 text-emerald-600" />}
      </label>
      <textarea className="input mt-3" rows={2} placeholder="Notes..." value={notes} onChange={(e) => setNotes(e.target.value)} />
      <div className="mt-2 flex justify-end">
        <button className="btn-ghost !py-1 text-xs" disabled={saving || notes === (topic.notes || '')} onClick={() => save({ notes })}>{saving ? 'Saving...' : 'Save notes'}</button>
      </div>
    </div>
  );
}

export default function JavaLearning() {
  const { data, loading, error, reload } = useFetch('/java-topics');
  const [actionError, setActionError] = useState('');
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  const done = data.filter((t) => t.completed).length;
  const percent = Math.round((100 * done) / data.length);

  return (
    <>
      <PageHeader title="Java Learning" subtitle={`${done} of ${data.length} topics completed`} />
      <div className="card mb-4">
        <div className="mb-1 flex justify-between text-sm"><span>Overall progress</span><span className="font-medium">{percent}%</span></div>
        <ProgressBar value={percent} color="bg-emerald-500" />
      </div>
      <ErrorBox message={actionError} />
      <div className="mt-4 grid gap-3 md:grid-cols-2">
        {data.map((t) => <TopicCard key={t.id + String(t.completed)} topic={t} onChanged={reload} onError={setActionError} />)}
      </div>
    </>
  );
}
