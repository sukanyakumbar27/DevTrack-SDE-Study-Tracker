import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { CalendarDays, Clock, CheckCircle2, Flame, Code2, Target } from 'lucide-react';
import useFetch from '../hooks/useFetch.js';
import { ErrorBox, PageHeader, ProgressBar, Spinner, StatCard } from '../components/ui.jsx';
import { weekday } from '../utils.js';

export default function Dashboard() {
  const { data, loading, error, reload } = useFetch('/dashboard');
  if (loading) return <Spinner />;
  if (error) return <ErrorBox message={error} onRetry={reload} />;

  const todayLabel = new Date(data.today + 'T00:00:00').toLocaleDateString(undefined, {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
  });
  const week = data.weeklyProgress.map((d) => ({ ...d, day: weekday(d.date) }));

  return (
    <>
      <PageHeader title="Dashboard" subtitle="Your preparation at a glance" />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <StatCard icon={CalendarDays} label="Today" value={todayLabel.split(',')[0]} sub={todayLabel.split(',').slice(1).join(',').trim()} />
        <StatCard icon={Clock} label="Total study hours" value={`${data.totalStudyHours} h`} sub={`${data.todayStudyHours} h today`} />
        <StatCard icon={CheckCircle2} label="Tasks today" value={`${data.tasksCompletedToday} done / ${data.tasksPendingToday} pending`} sub={`${data.totalTasksCompleted} completed overall, ${data.totalTasksPending} pending overall`} />
        <StatCard icon={Target} label="Daily productivity" value={`${data.productivityPercent}%`}>
          <ProgressBar value={data.productivityPercent} color="bg-emerald-500" />
        </StatCard>
        <StatCard icon={Flame} label="Study streak" value={`${data.streak} day${data.streak === 1 ? '' : 's'}`} sub="Days in a row with study activity" />
        <StatCard icon={Code2} label="DSA problems solved" value={data.dsaSolved} />
      </div>

      <div className="card mt-6">
        <h2 className="mb-4 font-semibold">Weekly progress (study hours)</h2>
        <div className="h-64">
          <ResponsiveContainer>
            <BarChart data={week}>
              <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
              <XAxis dataKey="day" fontSize={12} />
              <YAxis fontSize={12} />
              <Tooltip />
              <Bar dataKey="hours" name="Hours" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </>
  );
}
