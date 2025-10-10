<!-- src/components/TransactionsTable.vue -->
<template>
  <table class="table table-striped table-hover">
    <thead>
      <tr>
        <th>Date</th><th>Name</th><th>Account</th><th>Description</th><th>Amount</th><th>Type</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="t in items" :key="t.id">
        <td>{{ t.transactionDate }}</td>
        <td>{{ t.fromAccountName }}</td>
        <td>{{ t.fromAccount }}</td>
        <td>{{ t.description }}</td>
        <td>{{ t.amount }}</td>
        <td>{{ t.transactionType }}</td>
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
import type { Transaction } from '@/services/bankApi'
defineProps<{ items: Transaction[] }>()
</script>