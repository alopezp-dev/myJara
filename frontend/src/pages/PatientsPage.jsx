import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { searchPatients, getPatients } from '../api/client'
import Spinner from '../components/common/Spinner'
import ErrorMessage from '../components/common/ErrorMessage'

export default function PatientsPage() {
  const [term, setTerm] = useState('')

  const { data, isLoading, isError } = useQuery({
    queryKey: ['patients', term],
    queryFn: () => term.length >= 2
      ? searchPatients(term).then(r => r.data)
      : getPatients().then(r => r.data),
  })

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Pacientes</h1>
        <Link
          to="/patients/new"
          className="bg-blue-600 text-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          + Nuevo paciente
        </Link>
      </div>

      {/* Buscador */}
      <div className="mb-6">
        <input
          type="text"
          placeholder="Buscar por nombre, apellido, DNI o tarjeta sanitaria..."
          value={term}
          onChange={e => setTerm(e.target.value)}
          className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {/* Resultados */}
      {isLoading && <Spinner />}
      {isError && <ErrorMessage message="Error al cargar los pacientes." />}
      {data && data.length === 0 && (
        <p className="text-gray-500 text-sm text-center py-8">
          No se encontraron pacientes.
        </p>
      )}
      {data && data.length > 0 && (
        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Paciente</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">DNI</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Tarjeta sanitaria</th>
                <th className="text-left px-4 py-3 font-medium text-gray-600">Municipio</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {data.map(p => (
                <tr key={p.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 font-medium text-gray-800">
                    {p.firstName} {p.lastName}
                  </td>
                  <td className="px-4 py-3 text-gray-500">{p.dni || '—'}</td>
                  <td className="px-4 py-3 text-gray-500">{p.healthCard}</td>
                  <td className="px-4 py-3 text-gray-500">{p.municipality || '—'}</td>
                  <td className="px-4 py-3 text-right">
                    <Link
                      to={`/patients/${p.id}`}
                      className="text-blue-600 hover:text-blue-800 font-medium"
                    >
                      Ver ficha →
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}