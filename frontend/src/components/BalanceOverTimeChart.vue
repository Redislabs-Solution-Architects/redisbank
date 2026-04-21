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

type ApexPoint = { x: number; y: number }

function toNumber(v: unknown): number | null {
  if (typeof v === 'number' && Number.isFinite(v)) return v
  if (typeof v === 'string') {
    const n = Number(v)
    return Number.isFinite(n) ? n : null
  }
  return null
}

function normalizePoints(raw: unknown): ApexPoint[] {
  if (!Array.isArray(raw)) return []
  return raw
    .map((item): ApexPoint | null => {
      if (!item || typeof item !== 'object') return null
      const p = item as Record<string, unknown>
      const x = toNumber(p.x)
      const y = toNumber(p.y)
      if (x == null || y == null) return null
      // Round chart values to avoid floating-point tails breaking SVG path parsing.
      return { x: Math.trunc(x), y: Math.round(y * 100) / 100 }
    })
    .filter((p): p is ApexPoint => p !== null)
    .sort((a, b) => a.x - b.x)
}

const seriesData = computed(() => [
  { name: 'Balance', data: normalizePoints(props.data) }
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