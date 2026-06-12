import { Link } from 'react-router-dom'

const cards = [
  {
    title: 'Pacientes',
    desc: 'Buscar, registrar y gestionar pacientes del SES',
    to: '/patients',
    color: 'bg-blue-600'
  },
  {
    title: 'Nueva cita',
    desc: 'Programar una cita médica para un paciente',
    to: '/appointments/new',
    color: 'bg-green-600'
  },
]

export default function HomePage() {
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-2">
        Sistema de Información Sanitaria
      </h1>
      <p className="text-gray-500 mb-8">
        Servicio Extremeño de Salud — MyJara v0.3.0
      </p>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {cards.map(c => (
          <Link
            key={c.to}
            to={c.to}
            className="block rounded-xl border border-gray-200 bg-white shadow-sm hover:shadow-md transition-shadow p-6"
          >
            <div className={`w-10 h-10 rounded-lg ${c.color} mb-4`} />
            <h2 className="font-semibold text-gray-800 mb-1">{c.title}</h2>
            <p className="text-sm text-gray-500">{c.desc}</p>
          </Link>
        ))}
      </div>
    </div>
  )
}