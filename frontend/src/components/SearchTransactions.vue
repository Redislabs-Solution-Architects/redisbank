<!-- src/components/SearchTransactions.vue -->
<template>
  <div class="card">
    <div class="card-header"><h5 class="mb-0">Search transactions</h5></div>
    <div class="card-body">
      <div class="row mb-3">
        <div class="col-md-8">
          <div class="position-relative">
            <input v-model.trim="q" @input="debouncedSearch" class="form-control" placeholder="Type to search" style="padding-left: 30px;"/>
            <i class="bi bi-search position-absolute top-50 translate-middle-y ms-2"></i>
          </div>
        </div>
      </div>

      <table v-if="results.length" class="table table-striped table-hover">
        <thead>
        <tr><th>Date</th><th>Name</th><th>Account</th><th>Description</th><th>Amount</th><th>Type</th></tr>
        </thead>
        <tbody>
          <tr v-for="r in results" :key="r.id">
            <td>{{ r.transactionDate }}</td>
            <td v-html="r.fromAccountName"></td>
            <td>{{ r.fromAccount }}</td>
            <td v-html="r.description"></td>
            <td>{{ r.amount }}</td>
            <td v-html="r.transactionType"></td>
          </tr>
        </tbody>
      </table>
      <div v-else class="text-muted">No results</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { searchTransactions, type Transaction } from '@/services/bankApi'
import debounce from 'lodash/debounce'

const results = ref<Transaction[]>([])

const q = ref('')
const doSearch = async () => {
  if (!q.value) { results.value = []; return }
  results.value = await searchTransactions(q.value)
}
const debouncedSearch = debounce(doSearch, 300)
</script>