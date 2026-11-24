import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import {
  LayoutDashboard, ListChecks, Code2, Coffee, BarChart3, FolderGit2, CalendarDays, StickyNote,
  Menu, Sun, Moon, LogOut, Terminal,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext.jsx';
import { useTheme } from '../context/ThemeContext.jsx';

const links = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/planner', label: 'Study Planner', icon: ListChecks },
  { to: '/dsa', label: 'DSA Tracker', icon: Code2 },
  { to: '/java', label: 'Java Learning', icon: Coffee },
  { to: '/analytics', label: 'Analytics', icon: BarChart3 },
  { to: '/projects', label: 'Projects', icon: FolderGit2 },
  { to: '/calendar', label: 'Calendar', icon: CalendarDays },
  { to: '/notes', label: 'Notes', icon: StickyNote },
];

export default function Layout() {
  const [open, setOpen] = useState(false);
  const { user, logout } = useAuth();
  const { dark, toggle } = useTheme();

  return (
    <div className="min-h-screen lg:flex">
      {open && <div className="fixed inset-0 z-30 bg-black/40 lg:hidden" onClick={() => setOpen(false)} />}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col border-r border-slate-200 bg-white p-4 transition-transform dark:border-slate-800 dark:bg-slate-900 lg:static lg:translate-x-0 ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="mb-6 flex items-center gap-2 px-2 text-lg font-bold">
          <span className="grid h-8 w-8 place-items-center rounded-lg bg-indigo-600 text-white"><Terminal className="h-4 w-4" /></span>
          DevTrack
        </div>
        <nav className="flex-1 space-y-1">
          {links.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={() => setOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition ${
                  isActive
                    ? 'bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300'
                    : 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800'
                }`
              }
            >
              <Icon className="h-4 w-4" /> {label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-slate-200 pt-3 dark:border-slate-800">
          <div className="mb-2 truncate px-2 text-sm">
            <div className="font-medium">{user?.name}</div>
            <div className="truncate text-xs text-slate-500">{user?.email}</div>
          </div>
          <div className="flex gap-2">
            <button onClick={toggle} className="btn-ghost flex-1">{dark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />} Theme</button>
            <button onClick={logout} className="btn-ghost flex-1"><LogOut className="h-4 w-4" /> Logout</button>
          </div>
        </div>
      </aside>

      <div className="flex-1 min-w-0">
        <header className="sticky top-0 z-20 flex items-center gap-3 border-b border-slate-200 bg-white/80 px-4 py-3 backdrop-blur dark:border-slate-800 dark:bg-slate-900/80 lg:hidden">
          <button onClick={() => setOpen(true)} className="btn-ghost !p-1.5" aria-label="Menu"><Menu className="h-5 w-5" /></button>
          <span className="font-semibold">DevTrack</span>
        </header>
        <main className="mx-auto max-w-6xl p-4 sm:p-6 lg:p-8"><Outlet /></main>
      </div>
    </div>
  );
}
