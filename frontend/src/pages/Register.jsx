import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Terminal } from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { errorMessage } from '../api/client.js';
import { ErrorBox, Field } from '../components/ui.jsx';

export default function Register() {
  const { register } = useAuth();
  const [form, setForm] = useState({ name: '', email: '', password: '', loadSampleData: true });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await register(form);
    } catch (err) {
      setError(errorMessage(err));
      setLoading(false);
    }
  };

  return (
    <div className="grid min-h-screen place-items-center p-4">
      <form onSubmit={submit} className="card w-full max-w-sm space-y-4">
        <div className="flex items-center gap-2 text-xl font-bold">
          <span className="grid h-9 w-9 place-items-center rounded-lg bg-indigo-600 text-white"><Terminal className="h-5 w-5" /></span>
          Create account
        </div>
        <ErrorBox message={error} />
        <Field label="Name"><input required className="input" value={form.name} onChange={set('name')} /></Field>
        <Field label="Email"><input type="email" required className="input" value={form.email} onChange={set('email')} /></Field>
        <Field label="Password (min 8 characters)"><input type="password" required minLength={8} className="input" value={form.password} onChange={set('password')} /></Field>
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={form.loadSampleData} onChange={(e) => setForm({ ...form, loadSampleData: e.target.checked })} />
          Load sample data so the charts are not empty
        </label>
        <button className="btn-primary w-full" disabled={loading}>{loading ? 'Creating...' : 'Create account'}</button>
        <p className="text-center text-sm text-slate-500">Already registered? <Link className="font-medium text-indigo-600" to="/login">Sign in</Link></p>
      </form>
    </div>
  );
}
