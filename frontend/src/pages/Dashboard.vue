<template>
  <AppShell>
    <!-- Dashboard.vue -->
    <template #header>
      <div class="page-title">
        <div class="row align-items-center">
          <!-- left: title + subtitle -->
          <div class="col-12 col-md-6 order-md-1 order-last">
            <!-- no custom class, let Mazer style the <h3> -->
            <h3>Checking account</h3>
            <p class="text-subtitle text-muted mb-0">Transaction overview</p>
          </div>

          <!-- right: breadcrumb -->
          <div class="col-12 col-md-6 order-md-2 order-first">
            <nav aria-label="breadcrumb" class="breadcrumb-header float-start float-lg-end">
              <ol class="breadcrumb mb-0">
                <li class="breadcrumb-item"><a href="#">Redis Bank</a></li>
                <li class="breadcrumb-item active" aria-current="page">Checking accounts</li>
                <li class="breadcrumb-item active" aria-current="page">{{ accountSummary?.account }}</li>
              </ol>
            </nav>
          </div>
        </div>
      </div>
    </template>

    <section class="mb-3">
      <div class="card">
        <div class="card-header">
          <div class="row">
            <div class="col-md-6"><h5 class="mb-0">Checking account {{ accountSummary?.account }}</h5></div>
            <div class="col-md-6"><h6 class="mb-0">Account holder: L.S. Rosenquist, Nieuwegein, The Netherlands</h6></div>
          </div>
          <div class="row mt-2">
            <div class="col-md-6">Current balance: <span class="fw-bold">{{ currentBalance }}</span></div>
          </div>
        </div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-6">
              <div class="mb-1">Balance over time</div>
              <!-- BalanceSeries = unknown[] ; le composant accepte "data" -->
              <BalanceOverTimeChart :data="balanceSeries" />
            </div>
            <div class="col-md-6">
              <div class="mb-1">Biggest spenders</div>
              <TopSpendersDonut :data="spenders" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="mb-3">
      <SearchTransactions />
    </section>

    <section>
      <div class="card">
        <div class="card-header d-flex justify-content-between">
          <div>Most recent transactions</div>
          <div>Current balance: <span class="fw-bold">{{ currentBalance }}</span></div>
        </div>
        <div class="card-body">
          <TransactionsTable :items="recent" />
        </div>
      </div>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppShell from '@/layout/AppShell.vue'
import BalanceOverTimeChart from '@/components/BalanceOverTimeChart.vue'
import TopSpendersDonut from '@/components/TopSpendersDonut.vue'
import SearchTransactions from '@/components/SearchTransactions.vue'
import TransactionsTable from '@/components/TransactionsTable.vue'
import { useStomp } from '@/composables/useStomp'

import {
  fetchTransactions,   // GET /transactions
  fetchBalance,        // GET /balance
  fetchTopSpenders,    // GET /biggestspenders
  toAccountSummary,
  type Transaction,
  type BalanceSeries,
  type TopSpenders,
  type AccountSummary
} from '@/services/bankApi'

const accountSummary = ref<AccountSummary | null>(null)
const recent         = ref<Transaction[]>([])
const balanceSeries  = ref<BalanceSeries>([])               // <- plus de BalanceInput
const spenders       = ref<TopSpenders>({ series: [], labels: [] }) // valeur par défaut sûre

const currentBalance = computed(() => {
  const b = accountSummary.value?.balance
  return b != null ? `${b.toLocaleString()}` : '--'
})

// Helpers pour convertir "94.671,39 €" -> 94671.39
function euroToNumber(v: unknown): number | undefined {
  if (typeof v === 'number') return v
  if (typeof v === 'string') {
    const s = v.replace(/\s/g, '').replace('€', '').replace(/\./g, '').replace(',', '.')
    const n = Number(s)
    return Number.isFinite(n) ? n : undefined
  }
  return undefined
}

function normalizeTx(raw: any): Transaction {
  const tx: any = { ...raw }
  const a = euroToNumber(tx.amount)
  if (a != null) tx.amount = a
  const b = euroToNumber(tx.balanceAfter)
  if (b != null) tx.balanceAfter = b
  return tx as Transaction
}

onMounted(async () => {
  // 1) transactions pour alimenter la table + résumé
  const txs = await fetchTransactions()
  recent.value = txs
  accountSummary.value = toAccountSummary(txs[0]) // <- on passe le 1er élément

  // 2) séries de balance
  balanceSeries.value = await fetchBalance()

  // 3) donut spenders
  spenders.value = await fetchTopSpenders()
})

// Live updates (adapte les topics aux noms réellement exposés)
useStomp({
  topics: [
    {
      dest: '/topic/transactions',
      onMessage: (t: Transaction) => {
        recent.value = [t, ...recent.value].slice(0, 50)
        accountSummary.value = toAccountSummary(t) // <- on peut se baser sur l’event
      }
    },
    {
      dest: '/topic/balance',
      onMessage: (arr: BalanceSeries) => {
        balanceSeries.value = arr
      }
    },
    {
      dest: '/topic/spenders',
      onMessage: (top: TopSpenders) => {
        spenders.value = top
      }
    },
    {
      dest: '/topic/account', // optionnel si ton back l’envoie
      onMessage: (s: AccountSummary) => {
        accountSummary.value = s
      }
    }
  ]
})
</script>