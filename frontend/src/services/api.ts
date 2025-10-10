import axios from 'axios'

const api = axios.create({
  baseURL: '/api', // proxifié vers 8080 en dev
  withCredentials: true // utile si le back dépose un cookie de session
})

// Optionnel: on attache un token si tu en as un plus tard
api.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('rb_token')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

api.interceptors.response.use(
  (r) => r,
  (err) => {
    // Rediriger sur /login si 401
    if (err?.response?.status === 401 && location.pathname !== '/login') {
      location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api