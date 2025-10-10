<!-- src/layout/AppShell.vue -->
<template>
  <div id="app" class="d-flex">
    <aside :class="['sidebar', { active: sidebarActive }]" id="sidebar">
      <div class="sidebar-wrapper">
        <Sidebar @navigate="onNavigate" />
      </div>
    </aside>

    <main id="main" class="flex-grow-1">
      <!-- header : on ne force plus d-flex sur le container principal -->
      <header class="mb-3">
        <!-- le burger n’apparaît que sur mobile et n’empiète pas sur le slot -->
        <div class="d-xl-none mb-2">
          <button class="btn btn-link p-0" @click="toggleSidebar">
            <i class="bi bi-justify fs-3"></i>
          </button>
        </div>

        <!-- le slot peut utiliser la grille Bootstrap librement -->
        <slot name="header" />
      </header>

      <div class="page-heading">
        <slot />
      </div>

      <footer class="mt-auto">
        <div class="footer clearfix mb-0 text-muted d-flex justify-content-between">
          <div>Theme: 2021 © Mazer</div>
          <div>
            Theme crafted with <span class="text-danger"><i class="bi bi-heart"></i></span>
            by <a href="http://ahmadsaugi.com">A. Saugi</a>
          </div>
        </div>
      </footer>
    </main>
  </div>
</template>

// src/layout/AppShell.vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Sidebar from '@/components/Sidebar.vue'

defineSlots<{
  header?: () => any
  default?: () => any
}>()

const sidebarActive = ref(true)
function toggleSidebar() { sidebarActive.value = !sidebarActive.value }
function onNavigate() { if (window.innerWidth < 1200) sidebarActive.value = false }

onMounted(() => {
  if (window.innerWidth < 1200) sidebarActive.value = false
  window.addEventListener('resize', () => {
    sidebarActive.value = window.innerWidth >= 1200
  })
})
</script>

<style scoped>
.sidebar { width: 280px; transition: transform .2s ease; }
.sidebar:not(.active) { transform: translateX(-100%); }
@media (min-width: 1200px) {
  .sidebar { transform: none !important; }
}
</style>