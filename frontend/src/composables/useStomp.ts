// src/composables/useStomp.ts
import { onMounted, onBeforeUnmount } from 'vue'
import { Client, IMessage } from '@stomp/stompjs'

type Handler<T> = (payload: T) => void

export function useStomp(opts: {
  url?: string
  topics: { dest: string; onMessage: Handler<any> }[]
  debug?: boolean
}) {
  const url = opts.url ?? '/ws'

  let client: Client | null = null

  onMounted(() => {
    if (client && client.active) {
      console.warn('STOMP client already active')
      return
    }

    // Construire et logguer l’URL finale
    const brokerUrl = url.startsWith('ws')
      ? url
      : (location.protocol === 'https:' ? 'wss://' : 'ws://') + location.host + url

    console.log(`[STOMP] brokerURL resolved to: ${brokerUrl}`)

    client = new Client({
      brokerURL: brokerUrl,
      reconnectDelay: 0,
      debug: msg => (opts.debug ? console.log('[STOMP]', msg) : void 0),
    })
    client.onConnect = () => {
      opts.topics.forEach(t => {
        client!.subscribe(t.dest, (frame: IMessage) => {
          let payload: any = frame.body
          try { payload = JSON.parse(frame.body) } catch { /* body = string */ }

          // <== cas RedisBank : { transaction: "<json string>" }
          if (payload && typeof payload.transaction === 'string') {
            try { payload = JSON.parse(payload.transaction) } catch { /* ignore */ }
          }
          t.onMessage(payload)
        })
      })
    }

    client.onStompError = f => {
      console.error('STOMP broker error', f.headers['message'], f.body)
    }

    client.onWebSocketClose = e => {
      console.warn('STOMP socket closed', e)
    }

    client.activate()
  })

  onBeforeUnmount(() => {
    client?.deactivate()
  })
}