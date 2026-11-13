import { api } from './api'

export async function sendAssistantMessage(message, sessionId) {
  const { data } = await api.post('/assistant/chat', { message, sessionId })
  return data
}
