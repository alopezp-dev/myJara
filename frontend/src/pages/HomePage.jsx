import { Link } from 'react-router-dom'

const modules = [
  {
    title: 'Gestión de citas',
    to: '/appointments/new',
    icon: (
      <svg viewBox="0 0 48 48" fill="none" className="w-14 h-14" stroke="currentColor" strokeWidth="2">
        <rect x="6" y="10" width="36" height="32" rx="3" />
        <line x1="6" y1="18" x2="42" y2="18" />
        <line x1="16" y1="6" x2="16" y2="14" />
        <line x1="32" y1="6" x2="32" y2="14" />
        <circle cx="34" cy="34" r="7" fill="currentColor" stroke="none" className="text-green-600" />
        <line x1="34" y1="30" x2="34" y2="38" stroke="white" strokeWidth="2.5" />
        <line x1="30" y1="34" x2="38" y2="34" stroke="white" strokeWidth="2.5" />
      </svg>
    )
  },
  {
    title: 'Pacientes',
    to: '/patients',
    icon: (
      <svg viewBox="0 0 48 48" fill="none" className="w-14 h-14" stroke="currentColor" strokeWidth="2">
        <circle cx="24" cy="16" r="8" />
        <path d="M8 40c0-8.837 7.163-16 16-16s16 7.163 16 16" />
      </svg>
    )
  },
  {
    title: 'Mis tratamientos',
    to: '/treatments',
    icon: (
      <svg viewBox="0 0 48 48" fill="none" className="w-14 h-14" stroke="currentColor" strokeWidth="2">
        <rect x="10" y="6" width="28" height="36" rx="2" />
        <line x1="17" y1="16" x2="31" y2="16" />
        <line x1="17" y1="22" x2="31" y2="22" />
        <line x1="17" y1="28" x2="25" y2="28" />
        <rect x="14" y="4" width="8" height="6" rx="1" fill="currentColor" />
      </svg>
    )
  },
  {
    title: 'Mis informes',
    to: '/reports',
    icon: (
      <svg viewBox="0 0 48 48" fill="none" className="w-14 h-14" stroke="currentColor" strokeWidth="2">
        <rect x="8" y="4" width="24" height="32" rx="2" />
        <rect x="16" y="12" width="24" height="32" rx="2" fill="white" stroke="currentColor" />
        <line x1="22" y1="22" x2="34" y2="22" />
        <line x1="22" y1="28" x2="34" y2="28" />
        <line x1="22" y1="34" x2="30" y2="34" />
      </svg>
    )
  },
  {
    title: 'Historial clínico',
    to: '/patients',
    icon: (
      <svg viewBox="0 0 48 48" fill="none" className="w-14 h-14" stroke="currentColor" strokeWidth="2">
        <path d="M10 8h28v32a4 4 0 01-4 4H14a4 4 0 01-4-4V8z" />
        <line x1="18" y1="4" x2="18" y2="12" />
        <line x1="30" y1="4" x2="30" y2="12" />
        <circle cx="24" cy="28" r="6" />
        <line x1="24" y1="25" x2="24" y2="31" />
        <line x1="21" y1="28" x2="27" y2="28" />
      </svg>
    )
  },
  {
    title: 'Calendario',
    to: '/calendar',
    icon: (
      <svg viewBox="0 0 48 48" fill="none" className="w-14 h-14" stroke="currentColor" strokeWidth="2">
        <rect x="6" y="10" width="36" height="32" rx="3" />
        <line x1="6" y1="18" x2="42" y2="18" />
        <line x1="16" y1="6" x2="16" y2="14" />
        <line x1="32" y1="6" x2="32" y2="14" />
        <rect x="14" y="24" width="6" height="6" rx="1" />
        <rect x="26" y="24" width="6" height="6" rx="1" />
        <rect x="14" y="34" width="6" height="6" rx="1" />
      </svg>
    )
  },
]

export default function HomePage() {
  return (
    <div>
      {/* Cabecera del paciente */}
      <div className="bg-gray-100 border border-gray-200 rounded-xl px-5 py-4 mb-8 flex items-center gap-4">
        <div className="w-14 h-14 rounded-full bg-green-600 flex items-center justify-center flex-shrink-0">
          <svg viewBox="0 0 48 48" fill="white" className="w-8 h-8">
            <circle cx="24" cy="16" r="8" />
            <path d="M8 40c0-8.837 7.163-16 16-16s16 7.163 16 16" />
          </svg>
        </div>
        <div>
          <p className="font-bold text-gray-800 text-lg leading-tight">SISTEMA MYJARA</p>
          <p className="text-sm text-gray-500">Servicio Extremeño de Salud</p>
        </div>
      </div>

      {/* Grid de módulos */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-6">
        {modules.map(m => (
          <Link
            key={m.to + m.title}
            to={m.to}
            className="flex flex-col items-center gap-3 bg-white border border-gray-200 rounded-xl p-6 hover:border-green-400 hover:shadow-md transition-all text-green-700 group"
          >
            <div className="text-green-600 group-hover:scale-110 transition-transform">
              {m.icon}
            </div>
            <span className="text-sm font-semibold text-gray-700 text-center leading-tight">
              {m.title}
            </span>
          </Link>
        ))}
      </div>
    </div>
  )
}