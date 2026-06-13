import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Navbar from './components/common/Navbar'
import HomePage from './pages/HomePage'
import PatientsPage from './pages/PatientsPage'
import PatientDetailPage from './pages/PatientDetailPage'
import NewAppointmentPage from './pages/NewAppointmentPage'
import LoginPage from './pages/LoginPage'

function PrivateRoute({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}

export default function App() {
  const { user } = useAuth()

  return (
    <div className="min-h-screen bg-gray-50">
      {user && <Navbar />}
      <main className={user ? "max-w-7xl mx-auto px-4 py-8" : ""}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<PrivateRoute><HomePage /></PrivateRoute>} />
          <Route path="/patients" element={<PrivateRoute><PatientsPage /></PrivateRoute>} />
          <Route path="/patients/:id" element={<PrivateRoute><PatientDetailPage /></PrivateRoute>} />
          <Route path="/appointments/new" element={<PrivateRoute><NewAppointmentPage /></PrivateRoute>} />
        </Routes>
      </main>
    </div>
  )
}