import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  getProfessionals,
  getAgendas,
  getPatientById,
  getAvailableSlots,
  createAppointment
} from '../api/client'
import Spinner from '../components/common/Spinner'
import ErrorMessage from '../components/common/ErrorMessage'

export default function NewAppointmentPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const patientIdParam = searchParams.get('patientId')

  const [form, setForm] = useState({
    patientId: patientIdParam || '',
    professionalId: '',
    agendaId: '',
    date: '',
    slot: '',
    reason: ''
  })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const set = (field, value) => setForm(f => ({ ...f, [field]: value }))

  const { data: patient } = useQuery({
    queryKey: ['patient', form.patientId],
    queryFn: () => getPatientById(form.patientId).then(r => r.data),
    enabled: !!form.patientId
  })

  const { data: professionals = [] } = useQuery({
    queryKey: ['professionals'],
    queryFn: () => getProfessionals().then(r => r.data)
  })

  const { data: agendas = [] } = useQuery({
    queryKey: ['agendas', form.professionalId],
    queryFn: () => getAgendas(form.professionalId).then(r => r.data),
    enabled: !!form.professionalId
  })

  const selectedAgenda = agendas.find(a => a.id === Number(form.agendaId))

  // Validar que el día de la semana coincide con la agenda
  const dateValid = (() => {
    if (!form.date || !selectedAgenda) return true
    const date = new Date(form.date)
    const dow = date.getDay() === 0 ? 7 : date.getDay()
    return dow === selectedAgenda.dayOfWeek
  })()

  const { data: slots = [], isLoading: slotsLoading } = useQuery({
    queryKey: ['slots', form.professionalId, form.agendaId, form.date],
    queryFn: () => getAvailableSlots(form.professionalId, form.agendaId, form.date).then(r => r.data),
    enabled: !!form.professionalId && !!form.agendaId && !!form.date && dateValid
  })

  const days = ['', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado', 'domingo']

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.slot) { setError('Selecciona un horario disponible.'); return }
    setError(null)
    setSubmitting(true)
    try {
      await createAppointment({
        patientId: Number(form.patientId),
        professionalId: Number(form.professionalId),
        agendaId: Number(form.agendaId),
        startTime: `${form.date}T${form.slot}:00`,
        reason: form.reason
      })
      navigate(`/patients/${form.patientId}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear la cita.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Nueva cita</h1>

      <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 flex flex-col gap-5">

        {/* Paciente */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Paciente</label>
          {patient ? (
            <div className="flex items-center justify-between bg-blue-50 border border-blue-200 rounded-lg px-4 py-2.5">
              <span className="text-sm font-medium text-blue-800">{patient.firstName} {patient.lastName}</span>
              <span className="text-xs text-blue-500">{patient.healthCard}</span>
            </div>
          ) : (
            <input
              type="number"
              placeholder="ID del paciente"
              value={form.patientId}
              onChange={e => set('patientId', e.target.value)}
              required
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          )}
        </div>

        {/* Profesional */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Profesional</label>
          <select
            value={form.professionalId}
            onChange={e => { set('professionalId', e.target.value); set('agendaId', ''); set('date', ''); set('slot', '') }}
            required
            className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Selecciona un profesional...</option>
            {professionals.map(p => (
              <option key={p.id} value={p.id}>
                {p.firstName} {p.lastName} — {p.specialty}
              </option>
            ))}
          </select>
        </div>

        {/* Agenda */}
        {form.professionalId && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Agenda</label>
            {agendas.length === 0
              ? <p className="text-sm text-gray-400">Este profesional no tiene agendas activas.</p>
              : (
                <select
                  value={form.agendaId}
                  onChange={e => { set('agendaId', e.target.value); set('date', ''); set('slot', '') }}
                  required
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Selecciona una agenda...</option>
                  {agendas.map(a => (
                    <option key={a.id} value={a.id}>
                      {days[a.dayOfWeek].charAt(0).toUpperCase() + days[a.dayOfWeek].slice(1)} · {a.startTime.slice(0,5)}–{a.endTime.slice(0,5)} · {a.center} · {a.slotMinutes} min
                    </option>
                  ))}
                </select>
              )
            }
          </div>
        )}

        {/* Fecha */}
        {form.agendaId && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Fecha</label>
            <input
              type="date"
              value={form.date}
              onChange={e => { set('date', e.target.value); set('slot', '') }}
              required
              className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {form.date && !dateValid && (
              <p className="text-red-500 text-xs mt-1">
                Esta agenda solo está disponible los {selectedAgenda && days[selectedAgenda.dayOfWeek]}.
              </p>
            )}
          </div>
        )}

        {/* Slots disponibles */}
        {form.date && dateValid && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Horario disponible
            </label>
            {slotsLoading && <Spinner />}
            {!slotsLoading && slots.length === 0 && (
              <p className="text-sm text-gray-400">No hay huecos disponibles para este día.</p>
            )}
            {!slotsLoading && slots.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {slots.map(s => (
                  <button
                    key={s}
                    type="button"
                    onClick={() => set('slot', s)}
                    className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition-colors ${
                      form.slot === s
                        ? 'bg-blue-600 text-white border-blue-600'
                        : 'bg-white text-gray-700 border-gray-300 hover:border-blue-400'
                    }`}
                  >
                    {s}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Motivo */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Motivo de la consulta</label>
          <input
            type="text"
            placeholder="Ej: Revisión anual, dolor de cabeza..."
            value={form.reason}
            onChange={e => set('reason', e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {error && <ErrorMessage message={error} />}

        <div className="flex gap-3 justify-end pt-2">
          <button
            type="button"
            onClick={() => navigate(-1)}
            className="px-4 py-2 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={submitting || !form.slot}
            className="px-6 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Guardando...' : 'Crear cita'}
          </button>
        </div>

      </form>
    </div>
  )
}