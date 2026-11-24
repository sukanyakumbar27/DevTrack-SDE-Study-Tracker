import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import Layout from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Planner from './pages/Planner.jsx';
import Dsa from './pages/Dsa.jsx';
import JavaLearning from './pages/JavaLearning.jsx';
import Analytics from './pages/Analytics.jsx';
import Projects from './pages/Projects.jsx';
import Calendar from './pages/Calendar.jsx';
import Notes from './pages/Notes.jsx';

function Protected({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" replace />;
}

export default function App() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/register" element={user ? <Navigate to="/" replace /> : <Register />} />
      <Route element={<Protected><Layout /></Protected>}>
        <Route index element={<Dashboard />} />
        <Route path="planner" element={<Planner />} />
        <Route path="dsa" element={<Dsa />} />
        <Route path="java" element={<JavaLearning />} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="projects" element={<Projects />} />
        <Route path="calendar" element={<Calendar />} />
        <Route path="notes" element={<Notes />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
