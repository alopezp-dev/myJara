import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  getProfessionals,
  getAgendas,
  getPatientById,
  getAvailableSlots,
  createAppointment,
  searchPatients
} from '../api/client'
import Spinner from '../components/common/Spinner'
import ErrorMessage from '../components/common/ErrorMessage'

const STEPS = [
  'Identificación del paciente',
  'Tipo de profesional',
  'Selección de servicio',
  'Fecha y hora',
  'Selección de hueco',
  'Confirmación'
]

const PROFESSIONAL_TYPES = [
  { value: 'Medicina General', label: 'Médico/a de Familia' },
  { value: 'Enfermería', label: 'Enfermera/o' },
  { value: 'Pediatría', label: 'Pediatra' },
  { value: 'Cardiología', label: 'Cardiólogo/a' },
]

const days = ['', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado', 'domingo']

function StepHeader({ current, total, title }) {
  return (
    <div className="bg-gray-800 text-white px-5 py-3 rounded-t-xl flex items-center justify-between">
      <span className="text-sm font-medium">{current}/{total} Cita — {title}</span>
      <div className="flex gap-1">
        {Array.from({ length: total }).map((_, i) => (
          <div
            key={i}
            className={`w-2 h-2 rounded-full ${i < current ? 'bg-green-400' : 'bg-gray-600'}`}
          />
        ))}
      </div>
    </div>
  )
}

function NavButtons({ onPrev, onNext, nextLabel = 'SIGUIENTE ›', nextDisabled = false, isLast = false, submitting = false }) {
  return (
    <div className={`flex ${onPrev ? 'justify-between' : 'justify-end'} border-t border-gray-200 mt-6 pt-4`}>
      {onPrev && (
        <button
          type="button"
          onClick={onPrev}
          className="px-6 py-2.5 text-sm font-semibold text-gray-600 hover:text-gray-800 transition-colors"
        >
          ‹ ANTERIOR
        </button>
      )}
      <button
        type={isLast ? 'submit' : 'button'}
        onClick={isLast ? undefined : onNext}
        disabled={nextDisabled || submitting}
        className="px-8 py-2.5 text-sm font-semibold text-white bg-green-600 rounded-lg hover:bg-green-700 disabled:opacity-40 transition-colors"
      >
        {submitting ? 'Guardando...' : nextLabel}
      </button>
    </div>
  )
}

function PatientSearchResults({ term, selectedId, onSelect }) {
  const { data: results = [], isLoading } = useQuery({
    queryKey: ['search', term],
    queryFn: () => searchPatients(term).then(r => r.data),
    enabled: !!term && term.length >= 2
  })

  if (!term || term.length < 2) return (
    <p className="text-xs text-gray-400 text-center py-2">Escribe al menos 2 caracteres para buscar.</p>
  )
  if (isLoading) return <Spinner />
  if (results.length === 0) return (
    <p className="text-sm text-gray-400 text-center py-2">No se encontraron pacientes.</p>
  )

  return (
    <div className="border border-gray-200 rounded-lg overflow-hidden">
      {results.map(p => (
        <div
          key={p.id}
          className={`flex items-center justify-between px-4 py-3 border-b border-gray-100 cursor-pointer hover:bg-gray-50 transition-colors ${
            selectedId === String(p.id) ? 'bg-green-50' : ''
          }`}
          onClick={() => onSelect(String(p.id))}
        >
          <div>
            <p className="text-sm font-medium text-gray-800">
              {p.firstName} {p.lastName}
            </p>
            <p className="text-xs text-gray-400">{p.healthCard} · {p.dni || '—'}</p>
          </div>
          <input
            type="radio"
            name="patientId"
            value={p.id}
            checked={selectedId === String(p.id)}
            readOnly
            className="accent-green-600 w-4 h-4"
          />
        </div>
      ))}
    </div>
  )
}

export default function NewAppointmentPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const patientIdParam = searchParams.get('patientId')

  const [step, setStep] = useState(patientIdParam ? 2 : 1)
  const [form, setForm] = useState({
    patientId: patientIdParam || '',
    searchTerm: '',
    specialty: '',
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
    enabled: !!form.patientId,
    staleTime: 1000 * 60 * 5
  })

  const { data: professionals = [] } = useQuery({
    queryKey: ['professionals', form.specialty],
    queryFn: () => getProfessionals().then(r =>
      r.data.filter(p => !form.specialty || p.specialty === form.specialty)
    ),
    enabled: step >= 3
  })

  const { data: agendas = [] } = useQuery({
    queryKey: ['agendas', form.professionalId],
    queryFn: () => getAgendas(form.professionalId).then(r => r.data),
    enabled: !!form.professionalId
  })

  const selectedProfessional = professionals.find(p => p.id === Number(form.professionalId))
  const selectedAgenda = agendas.find(a => a.id === Number(form.agendaId))

  const dateValid = (() => {
    if (!form.date || !selectedAgenda) return true
    const d = new Date(form.date)
    const dow = d.getDay() === 0 ? 7 : d.getDay()
    return dow === selectedAgenda.dayOfWeek
  })()

  const { data: slots = [], isLoading: slotsLoading } = useQuery({
    queryKey: ['slots', form.professionalId, form.agendaId, form.date],
    queryFn: () => getAvailableSlots(form.professionalId, form.agendaId, form.date).then(r => r.data),
    enabled: !!form.professionalId && !!form.agendaId && !!form.date && dateValid && step === 5
  })

  const patientName = patient
    ? `${patient.firstName} ${patient.lastName}`
    : form.patientId ? 'Cargando...' : '—'

  const next = () => { setError(null); setStep(s => s + 1) }
  const prev = () => { setError(null); setStep(s => s - 1) }

  const handleSubmit = async (e) => {
    e.preventDefault()
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
      setStep(6)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-xl">

      {/* Cabecera paciente */}
      <div className="flex items-center gap-3 bg-gray-100 border border-gray-200 rounded-xl px-4 py-3 mb-4">
        <div className="w-10 h-10 rounded-full bg-green-600 flex items-center justify-center flex-shrink-0">
          <svg viewBox="0 0 48 48" fill="white" className="w-6 h-6">
            <circle cx="24" cy="16" r="8" />
            <path d="M8 40c0-8.837 7.163-16 16-16s16 7.163 16 16" />
          </svg>
        </div>
        <div>
          <p className="font-bold text-gray-800 text-sm uppercase">{patientName}</p>
          <p className="text-xs text-gray-500">{patient?.healthCard || '—'}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <StepHeader current={step} total={6} title={STEPS[step - 1]} />

          <div className="p-6">

            {/* Paso 1 — Identificación del paciente */}
            {step === 1 && (
              <div className="flex flex-col gap-4">
                <p className="text-sm text-gray-500">
                  Introduzca el nombre, apellido, DNI o tarjeta sanitaria:
                </p>
                <input
                  type="text"
                  placeholder="Buscar paciente..."
                  value={form.searchTerm}
                  onChange={e => set('searchTerm', e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                  autoFocus
                />
                <PatientSearchResults
                  term={form.searchTerm}
                  selectedId={form.patientId}
                  onSelect={id => set('patientId', id)}
                />
                <NavButtons
                  onNext={next}
                  nextDisabled={!form.patientId}
                />
              </div>
            )}

            {/* Paso 2 — Tipo de profesional */}
            {step === 2 && (
              <div className="flex flex-col gap-1">
                <p className="text-sm text-gray-500 mb-3">Seleccione el tipo de profesional:</p>
                {PROFESSIONAL_TYPES.map(t => (
                  <label
                    key={t.value}
                    className="flex items-center justify-between px-4 py-3 border-b border-gray-100 cursor-pointer hover:bg-gray-50 transition-colors"
                  >
                    <span className="text-sm text-gray-700">{t.label}</span>
                    <input
                      type="radio"
                      name="specialty"
                      value={t.value}
                      checked={form.specialty === t.value}
                      onChange={e => set('specialty', e.target.value)}
                      className="accent-green-600 w-4 h-4"
                    />
                  </label>
                ))}
                <p className="text-xs text-gray-400 mt-4">
                  También puede solicitar su cita llamando al teléfono <strong>900 100 737</strong> en horario de 7:00 a 22:00
                </p>
                <NavButtons
                  onPrev={patientIdParam ? undefined : prev}
                  onNext={next}
                  nextDisabled={!form.specialty}
                />
              </div>
            )}

            {/* Paso 3 — Selección de servicio */}
            {step === 3 && (
              <div>
                <p className="text-sm text-gray-500 mb-3">Seleccione el profesional:</p>
                {professionals.length === 0 && (
                  <p className="text-sm text-gray-400">No hay profesionales disponibles para esta especialidad.</p>
                )}
                {professionals.map(p => (
                  <div
                    key={p.id}
                    className={`px-4 py-3 border-b border-gray-100 cursor-pointer hover:bg-gray-50 transition-colors ${
                      form.professionalId === String(p.id) ? 'bg-green-50' : ''
                    }`}
                    onClick={() => {
                      set('professionalId', String(p.id))
                      set('agendaId', '')
                      set('date', '')
                      set('slot', '')
                    }}
                  >
                    <div className="flex items-center justify-between">
                      <p className="text-sm font-medium text-green-700">
                        {p.specialty?.toUpperCase()} — {p.firstName} {p.lastName}
                      </p>
                      <input
                        type="radio"
                        name="professionalId"
                        value={p.id}
                        checked={form.professionalId === String(p.id)}
                        readOnly
                        className="accent-green-600 w-4 h-4"
                      />
                    </div>
                    {form.professionalId === String(p.id) && agendas.length > 0 && (
                      <div className="mt-2 flex flex-col gap-1">
                        {agendas.map(a => (
                          <label
                            key={a.id}
                            className="flex items-center gap-2 cursor-pointer"
                            onClick={e => e.stopPropagation()}
                          >
                            <input
                              type="radio"
                              name="agendaId"
                              value={a.id}
                              checked={form.agendaId === String(a.id)}
                              onChange={e => set('agendaId', e.target.value)}
                              className="accent-green-600"
                            />
                            <span className="text-xs text-gray-500">
                              {days[a.dayOfWeek].charAt(0).toUpperCase() + days[a.dayOfWeek].slice(1)} · {a.startTime.slice(0,5)}–{a.endTime.slice(0,5)} · {a.center}
                            </span>
                          </label>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
                <NavButtons
                  onPrev={prev}
                  onNext={next}
                  nextDisabled={!form.professionalId || !form.agendaId}
                />
              </div>
            )}

            {/* Paso 4 — Fecha */}
            {step === 4 && (
              <div className="flex flex-col gap-5">
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 flex flex-col gap-4">
                  <div className="flex items-center gap-3">
                    <span className="text-green-600 text-xl">📅</span>
                    <div className="flex-1">
                      <p className="text-xs text-green-600 font-medium mb-1">Introduzca la fecha de la cita:</p>
                      <input
                        type="date"
                        value={form.date}
                        onChange={e => { set('date', e.target.value); set('slot', '') }}
                        className="w-full bg-transparent text-sm text-gray-800 border-b border-gray-300 focus:outline-none focus:border-green-500 pb-1"
                      />
                    </div>
                  </div>
                  {form.date && !dateValid && selectedAgenda && (
                    <p className="text-red-500 text-xs">
                      Esta agenda solo está disponible los {days[selectedAgenda.dayOfWeek]}.
                    </p>
                  )}
                  <div className="flex items-center gap-3 border-t border-gray-200 pt-3">
                    <span className="text-green-600 text-xl">🔍</span>
                    <div>
                      <p className="text-xs text-green-600 font-medium">Tipo de búsqueda:</p>
                      <p className="text-xs text-gray-500">Más cercano a partir del día seleccionado</p>
                    </div>
                  </div>
                </div>
                <NavButtons
                  onPrev={prev}
                  onNext={next}
                  nextDisabled={!form.date || !dateValid}
                />
              </div>
            )}

            {/* Paso 5 — Selección de hueco */}
            {step === 5 && (
              <div>
                {slotsLoading && <Spinner />}
                {!slotsLoading && slots.length === 0 && (
                  <p className="text-sm text-gray-400 text-center py-4">No hay huecos disponibles para este día.</p>
                )}
                {!slotsLoading && slots.map(s => (
                  <div
                    key={s}
                    className={`flex items-center justify-between px-4 py-3 border-b border-gray-100 cursor-pointer hover:bg-gray-50 transition-colors ${
                      form.slot === s ? 'bg-green-50' : ''
                    }`}
                    onClick={() => set('slot', s)}
                  >
                    <div>
                      <p className="text-sm">
                        <span className="text-green-600 font-medium">Día: </span>
                        <span className="text-gray-700 capitalize">
                          {days[new Date(form.date).getDay() === 0 ? 7 : new Date(form.date).getDay()]} — {new Date(form.date).toLocaleDateString('es-ES')}
                        </span>
                      </p>
                      <p className="text-sm">
                        <span className="text-green-600 font-medium">Hora: </span>
                        <span className="text-gray-700">{s}</span>
                      </p>
                      <p className="text-sm">
                        <span className="text-green-600 font-medium">Servicio: </span>
                        <span className="text-gray-700 uppercase">{form.specialty}</span>
                      </p>
                      <p className="text-sm">
                        <span className="text-green-600 font-medium">Profesional: </span>
                        <span className="text-gray-700">
                          {selectedProfessional ? `${selectedProfessional.firstName} ${selectedProfessional.lastName}` : '—'}
                        </span>
                      </p>
                    </div>
                    <input
                      type="radio"
                      name="slot"
                      value={s}
                      checked={form.slot === s}
                      readOnly
                      className="accent-green-600 w-4 h-4"
                    />
                  </div>
                ))}
                <NavButtons
                  onPrev={prev}
                  onNext={next}
                  nextDisabled={!form.slot}
                />
              </div>
            )}

            {/* Paso 6 — Confirmación */}
            {step === 6 && (
              <div className="flex flex-col gap-4">
                <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex flex-col gap-2">
                  <p className="text-sm font-semibold text-green-800 mb-2">Resumen de la cita</p>
                  <p className="text-sm"><span className="text-green-600 font-medium">Paciente: </span>{patientName}</p>
                  <p className="text-sm"><span className="text-green-600 font-medium">Profesional: </span>{selectedProfessional ? `${selectedProfessional.firstName} ${selectedProfessional.lastName}` : '—'}</p>
                  <p className="text-sm"><span className="text-green-600 font-medium">Especialidad: </span>{form.specialty}</p>
                  <p className="text-sm"><span className="text-green-600 font-medium">Centro: </span>{selectedAgenda?.center || '—'}</p>
                  <p className="text-sm">
                    <span className="text-green-600 font-medium">Fecha: </span>
                    {form.date ? new Date(form.date).toLocaleDateString('es-ES', {
                      weekday: 'long', day: '2-digit', month: 'long', year: 'numeric'
                    }) : '—'}
                  </p>
                  <p className="text-sm"><span className="text-green-600 font-medium">Hora: </span>{form.slot}</p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Motivo de la consulta <span className="text-gray-400 font-normal">(opcional)</span>
                  </label>
                  <input
                    type="text"
                    placeholder="Ej: Revisión anual, dolor de cabeza..."
                    value={form.reason}
                    onChange={e => set('reason', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                  />
                </div>
                {error && <ErrorMessage message={error} />}
                <NavButtons
                  onPrev={prev}
                  nextLabel="CONFIRMAR CITA"
                  isLast={true}
                  submitting={submitting}
                />
              </div>
            )}

          </div>
        </div>
      </form>
    </div>
  )
}