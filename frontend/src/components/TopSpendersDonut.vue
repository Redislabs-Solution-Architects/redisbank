<template>
  <ApexChart type="donut" height="260" :options="options" :series="series" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ApexChart from 'vue3-apexcharts'
import type { TopSpenders } from '@/services/bankApi'

const props = defineProps<{ data: TopSpenders }>()

const series = computed(() =>
  (props.data.series ?? [])
    .map((v) => (Number.isFinite(v) ? Math.max(0, Math.round(v * 100) / 100) : 0))
)
const options = computed(() => ({
  labels: props.data.labels ?? [],
  legend: { position: 'right' }
}))
</script>