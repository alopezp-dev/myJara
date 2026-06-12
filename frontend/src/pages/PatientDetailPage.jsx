import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  getPatientById,
  getAppointmentsByPatient,
  getEncountersByPatient,
  getConditionsByPatient,
  getAllergiesByPatient
} from '../api/client'
import Spinner from '../components/common/Spinner'
import ErrorMessage from '../components/common/ErrorMessage'

function Badge({ text, color = 'gray' }) {
  const colors = {
    green:  'bg-green-100 text-green-700',
    red:    'bg-red-100 text-red-700',
    blue:   'bg-blue-100 text-blue-700',
    yellow: 'bg-yellow-100 text-yellow-700',
    gray:   'bg-gray-100 text-gray-600',
  }
  return (
    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${colors[color]}`}>
      {text}
    </span>
  )
}

function Section({ title, children }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
      <div className="px-5 py-3 border-b border-gray-100 bg-gray-50">
        <h2 className="font-semibold text-gray-700 text-sm uppercase tracking-wide">
          {title}
        </h2>
      </div>
      <div className="p-5">{children}</div>
    </div>
  )
}

function Field({ label, value }) {
  return (
    <div>
      <p className="text-xs text-gray-400 mb-0.5">{label}</p>
      <p className="text-sm font-medium text-gray-800">{value || '—'}</p>
    </div>
  )
}

export default function PatientDetailPage() {
  const { id } = useParams()

  const { data: patient, isLoading, isError } = useQuery({
    queryKey: ['patient', id],
    queryFn: () => getPatientById(id).then(r => r.data)
  })

  const { data: appointments = [] } = useQuery({
    queryKey: ['appointments', id],
    queryFn: () => getAppointmentsByPatient(id).then(r => r.data),
    enabled: !!patient
  })

  const { data: encounters = [] } = useQuery({
    queryKey: ['encounters', id],
    queryFn: () => getEncountersByPatient(id).then(r => r.data),
    enabled: !!patient
  })

  const { data: conditions = [] } = useQuery({
    queryKey: ['conditions', id],
    queryFn: () => getConditionsByPatient(id).then(r => r.data),
    enabled: !!patient
  })

  const { data: allergies = [] } = useQuery({
    queryKey: ['allergies', id],
    queryFn: () => getAllergiesByPatient(id).then(r => r.data),
    enabled: !!patient
  })

  if (isLoading) return <Spinner />
  if (isError) return <ErrorMessage message="No se pudo cargar el paciente." />

  return (
    <div>
      {/* Cabecera */}
      <div className="flex items-center gap-3 mb-6">
        <Link to="/patients" className="text-gray-400 hover:text-gray-600 text-sm">
          ← Pacientes
        </Link>
        <span className="text-gray-300">/</span>
        <span className="text-sm text-gray-600">
          {patient.firstName} {patient.lastName}
        </span>
      </div>

      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">
            {patient.firstName} {patient.lastName}
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            TIS: {patient.healthCard} · DNI: {patient.dni || '—'}
          </p>
        </div>
        <Link
          to={`/appointments/new?patientId=${id}`}
          className="bg-blue-600 text-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
        >
          + Nueva cita
        </Link>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* Columna izquierda */}
        <div className="flex flex-col gap-6">

          {/* Datos personales */}
          <Section title="Datos personales">
            <div className="grid grid-cols-2 gap-4">
              <Field label="Nombre" value={patient.firstName} />
              <Field label="Apellidos" value={patient.lastName} />
              <Field label="Fecha de nacimiento" value={patient.birthDate} />
              <Field label="Género" value={patient.gender} />
              <Field label="Teléfono" value={patient.phone} />
              <Field label="Email" value={patient.email} />
              <Field label="Municipio" value={patient.municipality} />
              <Field label="Dirección" value={patient.address} />
            </div>
          </Section>

          {/* Alergias */}
          <Section title={`Alergias (${allergies.length})`}>
            {allergies.length === 0
              ? <p className="text-sm text-gray-400">Sin alergias registradas.</p>
              : allergies.map(a => (
                <div key={a.id} className="flex items-center justify-between py-2 border-b border-gray-50 last:border-0">
                  <div>
                    <p className="text-sm font-medium text-gray-800">{a.substance}</p>
                    <p className="text-xs text-gray-400">{a.reaction || '—'}</p>
                  </div>
                  <Badge
                    text={a.severity}
                    color={a.severity === 'SEVERE' ? 'red' : a.severity === 'MODERATE' ? 'yellow' : 'gray'}
                  />
                </div>
              ))
            }
          </Section>
        </div>

        {/* Columna central — Citas */}
        <div className="flex flex-col gap-6">
          <Section title={`Citas (${appointments.length})`}>
            {appointments.length === 0
              ? <p className="text-sm text-gray-400">Sin citas registradas.</p>
              : appointments.map(a => (
                <div key={a.id} className="py-2 border-b border-gray-50 last:border-0">
                  <div className="flex items-center justify-between mb-1">
                    <p className="text-sm font-medium text-gray-800">
                      {new Date(a.startTime).toLocaleDateString('es-ES', {
                        day: '2-digit', month: 'short', year: 'numeric'
                      })}
                    </p>
                    <Badge
                      text={a.status}
                      color={
                        a.status === 'COMPLETED' ? 'green' :
                        a.status === 'CANCELLED' ? 'red' :
                        a.status === 'SCHEDULED' ? 'blue' : 'gray'
                      }
                    />
                  </div>
                  <p className="text-xs text-gray-400">{a.professionalName}</p>
                  <p className="text-xs text-gray-400">{a.reason || '—'}</p>
                </div>
              ))
            }
          </Section>
        </div>

        {/* Columna derecha — Historia clínica */}
        <div className="flex flex-col gap-6">

          {/* Diagnósticos activos */}
          <Section title={`Diagnósticos (${conditions.length})`}>
            {conditions.length === 0
              ? <p className="text-sm text-gray-400">Sin diagnósticos registrados.</p>
              : conditions.map(c => (
                <div key={c.id} className="py-2 border-b border-gray-50 last:border-0">
                  <div className="flex items-center justify-between mb-0.5">
                    <span className="text-xs font-mono text-blue-600">{c.cie10Code}</span>
                    <Badge
                      text={c.status}
                      color={c.status === 'ACTIVE' ? 'red' : c.status === 'CHRONIC' ? 'yellow' : 'green'}
                    />
                  </div>
                  <p className="text-sm font-medium text-gray-800">{c.cie10Desc}</p>
                  <p className="text-xs text-gray-400">Desde {c.onsetDate || '—'}</p>
                </div>
              ))
            }
          </Section>

          {/* Episodios */}
          <Section title={`Episodios (${encounters.length})`}>
            {encounters.length === 0
              ? <p className="text-sm text-gray-400">Sin episodios registrados.</p>
              : encounters.map(e => (
                <div key={e.id} className="py-2 border-b border-gray-50 last:border-0">
                  <div className="flex items-center justify-between mb-1">
                    <p className="text-sm font-medium text-gray-800">
                      {new Date(e.startDate).toLocaleDateString('es-ES', {
                        day: '2-digit', month: 'short', year: 'numeric'
                      })}
                    </p>
                    <Badge
                      text={e.status}
                      color={e.status === 'COMPLETED' ? 'green' : e.status === 'IN_PROGRESS' ? 'blue' : 'gray'}
                    />
                  </div>
                  <p className="text-xs text-gray-400">{e.professionalName}</p>
                  <p className="text-xs text-gray-400">{e.reason || '—'}</p>
                </div>
              ))
            }
          </Section>
        </div>

      </div>
    </div>
  )
}