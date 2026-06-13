import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

client.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    if (error.response?.status === 403) {
      error.response.data = {
        message: 'No tienes permisos para realizar esta acción.'
      }
    }
    return Promise.reject(error)
  }
)

// Pacientes
export const getPatients = () => client.get('/patients')
export const getPatientById = (id) => client.get(`/patients/${id}`)
export const searchPatients = (term) => client.get(`/patients/search?term=${term}`)
export const createPatient = (data) => client.post('/patients', data)

// Citas
export const getAppointmentsByPatient = (id) => client.get(`/appointments/patient/${id}`)
export const createAppointment = (data) => client.post('/appointments', data)
export const getAvailableSlots = (professionalId, agendaId, date) =>
  client.get(`/appointments/slots?professionalId=${professionalId}&agendaId=${agendaId}&date=${date}`)

// Profesionales
export const getProfessionals = () => client.get('/professionals')
export const getAgendas = (professionalId) => client.get(`/agendas/professional/${professionalId}`)

// Historia clínica
export const getEncountersByPatient = (id) => client.get(`/encounters/patient/${id}`)
export const createEncounter = (data) => client.post('/encounters', data)
export const getConditionsByPatient = (id) => client.get(`/clinical/conditions/patient/${id}`)
export const getAllergiesByPatient = (id) => client.get(`/clinical/allergies/patient/${id}`)
export const getNotesByEncounter = (id) => client.get(`/clinical/notes/encounter/${id}`)
export const addCondition = (data) => client.post('/clinical/conditions', data)
export const addNote = (data) => client.post('/clinical/notes', data)

export const login = (username, password) =>
  client.post('/auth/login', { username, password })