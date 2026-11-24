import {
  Bar, BarChart, CartesianGrid, Cell, Legend, Line, LineChart, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { TrendingDown, TrendingUp } from 'lucide-react';
import useFetch from '../hooks/useFetch.js';
import { ErrorBox, PageHeader, Spinner } from '../components/ui.jsx';
import { CHART_COLORS, pretty, shortDate } from '../utils.js';

function ChartCard({ title, children }) {
  return (
    <div className="card">
      <h2 className="mb-3 font-semibold">{title}</h2>
      <div className="h-64">{children}</div>
    </div>
  );
}

function Delta({ label, value, unit }) {
  const up = value >= 0;
  const Icon = up ? TrendingUp : TrendingDown;
  return (
    <div className="card flex items-center gap-3">
      <Icon className={`h-6 w-6 ${up ? 'text-emerald-500' : 'text-red-500'}`} />
      <div>
        <div className="text-xs text-slate-500">{label}</div>
        <div className="text-lg font-semibold">{up ? '+' : ''}{value}{unit}</div>
        <div className="text-xs text-slate-500">last 7 days vs previous 7</div>
      </div>
    </div>
  );
}

export default function Analytics() {
  const { data, loading, error, reload } = useFetch('/analytics');
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  const daily = data.daily.map((d) => ({ ...d, label: shortDate(d.date) }));
  const weekly = data.weekly.map((w) => ({ ...w, label: shortDate(w.weekStart) }));
  const topics = data.dsaByTopic.filter((t) => t.value > 0).map((t) => ({ ...t, name: pretty(t.name) }));
  const difficulties = data.dsaByDifficulty.map((d) => ({ ...d, name: pretty(d.name) }));
  const status = [{ name: 'Completed', value: data.completedTasks }, { name: 'Pending', value: data.pendingTasks }];

  return (
    <>
      <PageHeader title="Study Analytics" subtitle="Everything below is calculated from your saved data" />
      <div className="mb-4 grid gap-4 sm:grid-cols-2">
        <Delta label="Productivity change" value={data.productivityDelta} unit=" pts" />
        <Delta label="Study hours change" value={data.hoursDelta} unit=" h" />
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <ChartCard title="Daily productivity % (14 days)">
          <ResponsiveContainer><LineChart data={daily}><CartesianGrid strokeDasharray="3 3" opacity={0.3} /><XAxis dataKey="label" fontSize={11} /><YAxis domain={[0, 100]} fontSize={11} /><Tooltip /><Line type="monotone" dataKey="completionPercent" name="Completion %" stroke="#6366f1" strokeWidth={2} dot={false} /></LineChart></ResponsiveContainer>
        </ChartCard>
        <ChartCard title="Weekly study hours (8 weeks)">
          <ResponsiveContainer><BarChart data={weekly}><CartesianGrid strokeDasharray="3 3" opacity={0.3} /><XAxis dataKey="label" fontSize={11} /><YAxis fontSize={11} /><Tooltip /><Bar dataKey="hours" name="Hours" fill="#22c55e" radius={[4, 4, 0, 0]} /></BarChart></ResponsiveContainer>
        </ChartCard>
        <ChartCard title="Monthly progress">
          <ResponsiveContainer><BarChart data={data.monthly}><CartesianGrid strokeDasharray="3 3" opacity={0.3} /><XAxis dataKey="month" fontSize={11} /><YAxis fontSize={11} /><Tooltip /><Legend /><Bar dataKey="tasksCompleted" name="Tasks completed" fill="#6366f1" /><Bar dataKey="dsaSolved" name="DSA solved" fill="#f59e0b" /><Bar dataKey="hours" name="Hours" fill="#22c55e" /></BarChart></ResponsiveContainer>
        </ChartCard>
        <ChartCard title="DSA problems solved by topic">
          {topics.length === 0 ? <p className="py-20 text-center text-sm text-slate-500">No solved problems yet.</p> : (
            <ResponsiveContainer><BarChart data={topics} layout="vertical" margin={{ left: 40 }}><CartesianGrid strokeDasharray="3 3" opacity={0.3} /><XAxis type="number" allowDecimals={false} fontSize={11} /><YAxis type="category" dataKey="name" fontSize={11} width={110} /><Tooltip /><Bar dataKey="value" name="Solved" fill="#a855f7" radius={[0, 4, 4, 0]} /></BarChart></ResponsiveContainer>
          )}
        </ChartCard>
        <ChartCard title="DSA by difficulty">
          <ResponsiveContainer><BarChart data={difficulties}><CartesianGrid strokeDasharray="3 3" opacity={0.3} /><XAxis dataKey="name" fontSize={11} /><YAxis allowDecimals={false} fontSize={11} /><Tooltip /><Bar dataKey="value" name="Solved" radius={[4, 4, 0, 0]}>{difficulties.map((_, i) => <Cell key={i} fill={['#22c55e', '#f59e0b', '#ef4444'][i]} />)}</Bar></BarChart></ResponsiveContainer>
        </ChartCard>
        <ChartCard title="Subject-wise study hours">
          {data.subjects.length === 0 ? <p className="py-20 text-center text-sm text-slate-500">Log study time to see this chart.</p> : (
            <ResponsiveContainer><PieChart><Pie data={data.subjects} dataKey="value" nameKey="name" outerRadius={85} label>{data.subjects.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}</Pie><Tooltip /><Legend /></PieChart></ResponsiveContainer>
          )}
        </ChartCard>
        <ChartCard title="Completed vs pending tasks">
          <ResponsiveContainer><PieChart><Pie data={status} dataKey="value" nameKey="name" innerRadius={50} outerRadius={85} label><Cell fill="#22c55e" /><Cell fill="#f59e0b" /></Pie><Tooltip /><Legend /></PieChart></ResponsiveContainer>
        </ChartCard>
      </div>
    </>
  );
}
