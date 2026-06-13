import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function Navbar() {
  const { pathname } = useLocation()
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const links = [
    { to: '/',         label: 'Inicio' },
    { to: '/patients', label: 'Pacientes' },
  ]

  return (
    <nav className="bg-blue-700 text-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 flex items-center gap-8 h-14">
        <span className="font-bold text-lg tracking-tight">MyJara</span>
        {links.map(l => (
          <Link
            key={l.to}
            to={l.to}
            className={`text-sm font-medium hover:text-blue-200 transition-colors ${
              pathname === l.to ? 'text-white border-b-2 border-white pb-0.5' : 'text-blue-200'
            }`}
          >
            {l.label}
          </Link>
        ))}
        <div className="ml-auto flex items-center gap-4">
          <span className="text-sm text-blue-200">{user?.fullName}</span>
          <span className="text-xs bg-blue-800 px-2 py-0.5 rounded-full text-blue-200">
            {user?.role}
          </span>
          <button
            onClick={handleLogout}
            className="text-sm text-blue-200 hover:text-white transition-colors"
          >
            Cerrar sesión
          </button>
        </div>
      </div>
    </nav>
  )
}