import { Link, useLocation } from 'react-router-dom'

const links = [
  { to: '/',          label: 'Inicio' },
  { to: '/patients',  label: 'Pacientes' },
]

export default function Navbar() {
  const { pathname } = useLocation()

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
      </div>
    </nav>
  )
}