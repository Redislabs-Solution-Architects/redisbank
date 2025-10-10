<!-- src/components/Sidebar.vue -->
<template>
  <div class="p-3">
    <div class="text-center mb-3">
      <img :src="logoUrl" height="65" alt="logo" />
      <div class="fw-bold">bank</div>
    </div>
    <ul class="list-unstyled">
      <li class="mb-2">
        <a class="d-flex align-items-center text-decoration-none" @click.prevent="$emit('navigate')" href="#">
          <i class="bi bi-grid-fill me-2"></i>
          <span>Account overview</span>
        </a>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import logoUrl from '@/assets/images/logo/logonew.png'
</script>