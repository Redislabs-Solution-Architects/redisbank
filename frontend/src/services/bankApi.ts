// src/services/bankApi.ts
import api from '@/services/api'

/** Transaction telle que renvoyée par /transactions et via le websocket.
 *  Certains champs sont optionnels car les payloads peuvent varier (liste vs. recherche).
 */
export interface Transaction {
  id?: string
  transactionDate: string
  fromAccountName: string
  fromAccount: string
  description: string
  amount: number
  transactionType: string
  // présents sur le premier élément et via le WS
  toAccount?: string
  balanceAfter?: number
}

/** Données de recherche (le back renvoie des champs parfois "highlightés" en HTML) */
export interface SearchItem {
  id?: string
  transactionDate: string
  fromAccountName: string // peut contenir du HTML
  fromAccount: string
  description: string // peut contenir du HTML
  amount: number
  transactionType: string // peut contenir du HTML
}

/** Série de balance pour ApexCharts. Le backend renvoie déjà le format attendu,
 *  donc on tape en "unknown[]" pour ne pas forcer une forme.
 *  (Dans la version originale, la réponse est passée telle-quelle à ApexCharts.)
 */
export type BalanceSeries = unknown[]

/** Données “biggest spenders” (pie/donut) */
export interface TopSpenders {
  series: number[]
  labels: string[]
}

/** Résumé de compte pour l’en-tête */
export interface AccountSummary {
  account: string
  balance: number
}

/** Config STOMP exposée par /config/stomp (version originale) */
export interface StompConfig {
  protocol: 'ws' | 'wss'
  host: string
  port: number
  endpoint: string           // ex: "/ws"
  transactionsTopic: string  // ex: "/topic/transactions"
}

/* =======================
 * Endpoints “v1” originaux
 * baseURL = '/api' (déjà définie dans api.ts)
 * donc on appelle SANS préfixer '/api'
 * ======================= */

/** Liste des transactions récentes (utilisée au bootstrap) */
export async function fetchTransactions(): Promise<Transaction[]> {
  const { data } = await api.get('/transactions')
  return data
}

/** Série “balance over time” pour l’area chart */
export async function fetchBalance(): Promise<BalanceSeries> {
  const { data } = await api.get('/balance')
  return data
}

/** Donut “biggest spenders” */
export async function fetchTopSpenders(): Promise<TopSpenders> {
  const { data } = await api.get('/biggestspenders')
  return data
}

/** Config STOMP retournée par le back (si tu veux reproduire le comportement original) */
export async function fetchStompConfig(): Promise<StompConfig> {
  const { data } = await api.get('/config/stomp')
  return data
}

/** Recherche plein-texte (le back attend ?term=xxx*) */
export async function searchTransactions(term: string): Promise<SearchItem[]> {
  const q = term ? `${term}*` : ''
  const { data } = await api.get('/search', { params: { term: q } })
  return data
}

/** Petit helper pour reconstruire le résumé comme dans la page d’origine
 *  (à partir du premier élément de /transactions)
 */
export function toAccountSummary(firstTx: Transaction | undefined): AccountSummary | null {
  if (!firstTx?.toAccount || firstTx.balanceAfter == null) return null
  return {
    account: firstTx.toAccount,
    balance: firstTx.balanceAfter,
  }
}