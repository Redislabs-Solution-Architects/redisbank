<template>
  <ApexChart
    type="area"
    height="260"
    :options="options"
    :series="seriesData"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ApexChart from 'vue3-apexcharts'
import type { BalanceSeries } from '@/services/bankApi'

const props = defineProps<{ data: BalanceSeries }>()

/* Le back renvoie déjà le format attendu par ApexCharts (array de points),
   on le passe tel quel. */
const seriesData = computed(() => [
  { name: 'Balance', data: props.data as unknown[] }
])

const options = {
  chart: { id: 'balanceArea', toolbar: { show: false } },
  dataLabels: { enabled: false },
  xaxis: { type: 'datetime' },
  stroke: { curve: 'smooth', width: 2 },
  fill: { opacity: 0.35 },
  noData: { text: 'Loading...' }
}
</script>