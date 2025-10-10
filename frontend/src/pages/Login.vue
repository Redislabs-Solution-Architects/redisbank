<template>
  <div id="auth" class="container-fluid vh-100">
    <div class="row h-100">
      <div class="col-lg-5 col-12 d-flex align-items-center justify-content-center">
        <div class="p-4" style="max-width: 400px; width: 100%;">
          <div class="text-center mb-4">
            <img :src="logoUrl" alt="Logo" style="height: 60px;" />
            <div class="fw-bold mt-1">bank</div>
          </div>

          <h2 class="fw-bold mb-2">Welcome to Redis Enterprise Bank!</h2>
          <p class="text-muted mb-4">To log in, please enter your credentials</p>

          <form @submit.prevent="handleLogin">
            <div class="position-relative mb-3">
              <input v-model="username" class="form-control form-control-lg" placeholder="Username" autofocus />
              <i class="bi bi-person position-absolute top-50 translate-middle-y ms-2"></i>
            </div>

            <div class="position-relative mb-3">
              <input v-model="password" type="password" class="form-control form-control-lg" placeholder="Password" />
              <i class="bi bi-shield-lock position-absolute top-50 translate-middle-y ms-2"></i>
            </div>

            <div class="form-check d-flex align-items-center mb-4">
              <input v-model="keepLoggedIn" class="form-check-input me-2" type="checkbox" id="keepLoggedIn" />
              <label class="form-check-label" for="keepLoggedIn">Keep me logged in</label>
            </div>

            <button class="btn btn-primary btn-lg w-100 shadow-lg" :disabled="loading">
              <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
              Log in
            </button>

            <p v-if="error" class="text-danger mt-3">{{ error }}</p>
          </form>
        </div>
      </div>

      <div class="col-lg-7 d-none d-lg-block bg-primary"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import logoUrl from '@/assets/images/logo/logonew.png'

const router = useRouter()
const username = ref('lars')
const password = ref('larsje')
const keepLoggedIn = ref(false)
const loading = ref(false)
const error = ref<string>('')

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    const body = new URLSearchParams()
    body.set('username', username.value)
    body.set('password', password.value)
    if (keepLoggedIn.value) body.set('remember-me', 'on')

    await api.post('/perform_login', body.toString(), {
      baseURL: '/', // /perform_login n'est pas sous /api
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })

    router.push('/dashboard')
  } catch {
    error.value = 'Invalid credentials or server error'
  } finally {
    loading.value = false
  }
}
</script>